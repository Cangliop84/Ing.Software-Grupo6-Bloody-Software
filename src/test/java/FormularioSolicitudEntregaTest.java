import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.bson.Document;
import org.example.Donacion;
import org.example.FormularioSolicitudEntrega;
import org.example.SolicitudEntregaService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpServer;

class FormularioSolicitudEntregaTest {

    private FormularioSolicitudEntrega formulario;

    @BeforeEach
    void setUp() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            formulario = new FormularioSolicitudEntrega();
            formulario.setVisible(true);
        });
    }

    @AfterEach
    void tearDown() throws Exception {
        if (formulario != null) {
            SwingUtilities.invokeAndWait(() -> formulario.dispose());
        }
    }

    @Test
    void test1_adaptacionResolucionEscritorioYMovilSinRomperTarjeta() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            formulario.setSize(720, 420);
            formulario.ajustarDisenoSegunAncho(720);
            formulario.revalidate();
            formulario.repaint();
        });
        SwingUtilities.invokeAndWait(() -> {
            JPanel tarjeta = formulario.getPanelTarjeta();
            assertNotNull(tarjeta);
            assertTrue(tarjeta.getWidth() > 0 && tarjeta.getHeight() > 0);
            assertTrue(formulario.getCmbTipoSangre().isShowing());
            assertTrue(formulario.getTxtVolumenMl().isShowing());
            assertTrue(formulario.getWidth() >= tarjeta.getWidth());
        });

        SwingUtilities.invokeAndWait(() -> {
            formulario.setSize(340, 360);
            formulario.ajustarDisenoSegunAncho(340);
            formulario.revalidate();
            formulario.repaint();
        });
        SwingUtilities.invokeAndWait(() -> {
            JPanel tarjeta = formulario.getPanelTarjeta();
            assertTrue(tarjeta.getWidth() > 0 && tarjeta.getHeight() > 0);
            assertTrue(formulario.getCmbTipoSangre().isShowing());
            assertTrue(formulario.getTxtVolumenMl().isShowing());
            assertTrue(formulario.getWidth() >= tarjeta.getWidth());
        });
    }

    @Test
    void test2_botonSolicitarDeshabilitadoCuandoCamposEstanVacios() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            formulario.limpiarFormulario();
            JButton btnSolicitar = formulario.getBtnSolicitar();

            assertNotNull(btnSolicitar);
            assertFalse(btnSolicitar.isEnabled(),
                    "El botón 'Solicitar' debe mantenerse deshabilitado cuando los campos están vacíos");
        });
    }

    @Test
    void test3_limpiezaDeCamposEnInterfaz() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            formulario.getCmbTipoSangre().setSelectedItem("A-");
            formulario.getTxtVolumenMl().setText("500");

            formulario.limpiarFormulario();

            assertNull(formulario.getCmbTipoSangre().getSelectedItem(),
                    "El combo de tipo de sangre debe quedar sin selección (null)");

            assertEquals("", formulario.getTxtVolumenMl().getText(),
                    "El campo de volumen debe estar vacío");
        });
    }

    @Test
    void test4_envioConOPlusYVolumen5LlegaAlServidorYConfirma() throws Exception {
        AtomicReference<String> cuerpoRecibido = new AtomicReference<>();
        AtomicReference<String> metodoRecibido = new AtomicReference<>();
        AtomicBoolean alertaMostrada = new AtomicBoolean(false);
        AtomicReference<String> mensajeAlerta = new AtomicReference<>();
        AtomicReference<FormularioSolicitudEntrega> formPruebaRef = new AtomicReference<>();

        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/api/solicitudes", exchange -> {
            metodoRecibido.set(exchange.getRequestMethod());
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            cuerpoRecibido.set(body);

            String respuestaJson = new Document("exito", true)
                    .append("mensaje", "Solicitud recibida en servidor para test")
                    .toJson();
            byte[] bytes = respuestaJson.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        });
        server.start();

        try {
            String url = "http://localhost:" + server.getAddress().getPort() + "/api/solicitudes";
            SwingUtilities.invokeAndWait(() -> {
                if (formulario != null) {
                    formulario.dispose();
                    formulario = null;
                }
                FormularioSolicitudEntrega creada = new FormularioSolicitudEntrega(new SolicitudEntregaService(), null, url) {
                    @Override
                    protected void mostrarAlertaConfirmacion(String mensaje) {
                        alertaMostrada.set(true);
                        mensajeAlerta.set(mensaje);
                    }

                    @Override
                    protected void mostrarModalConfirmacionReserva(List<Donacion> muestrasReservadas) {
                    }
                };
                formPruebaRef.set(creada);
                creada.setVisible(true);
                creada.getCmbTipoSangre().setSelectedItem("O+");
                creada.getTxtVolumenMl().setText("5");
                creada.getBtnSolicitar().doClick();
            });

            assertEquals("POST", metodoRecibido.get());
            assertNotNull(cuerpoRecibido.get());
            Document payloadDoc = Document.parse(cuerpoRecibido.get());
            assertEquals("O+", payloadDoc.getString("tipoSangre"));
            assertEquals("5", payloadDoc.getString("volumenRequeridoMl"));
            assertTrue(alertaMostrada.get());
            assertEquals("Solicitud recibida en servidor para test", mensajeAlerta.get());
        } finally {
            server.stop(0);
            FormularioSolicitudEntrega formPrueba = formPruebaRef.get();
            if (formPrueba != null) {
                SwingUtilities.invokeAndWait(formPrueba::dispose);
            }
        }
    }

    @Test
    void test5_falloConexionCapturaTimeoutYMuestraModalError() throws Exception {
        AtomicBoolean modalInvocado = new AtomicBoolean(false);
        AtomicReference<String> detalleModal = new AtomicReference<>();
        AtomicReference<FormularioSolicitudEntrega> formSinServidorRef = new AtomicReference<>();

        try {
            SwingUtilities.invokeAndWait(() -> {
                if (formulario != null) {
                    formulario.dispose();
                    formulario = null;
                }
                FormularioSolicitudEntrega creada = new FormularioSolicitudEntrega(
                        new SolicitudEntregaService(),
                        null,
                        "http://127.0.0.1:65432/api/solicitudes"
                ) {
                    @Override
                    public void mostrarModalErrorConexion(String detalle) {
                        modalInvocado.set(true);
                        detalleModal.set(detalle);
                    }
                };
                formSinServidorRef.set(creada);
                creada.setVisible(true);
                creada.getCmbTipoSangre().setSelectedItem("O+");
                creada.getTxtVolumenMl().setText("5");
                creada.getBtnSolicitar().doClick();
            });

            assertTrue(modalInvocado.get());
            assertNotNull(detalleModal.get());
            assertFalse(detalleModal.get().trim().isEmpty());
            assertTrue(formSinServidorRef.get().getLblMensajeEstado().getText().contains("Error de conexión"));
        } finally {
            FormularioSolicitudEntrega formSinServidor = formSinServidorRef.get();
            if (formSinServidor != null) {
                SwingUtilities.invokeAndWait(formSinServidor::dispose);
            }
        }
    }
}