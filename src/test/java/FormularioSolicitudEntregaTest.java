import org.example.FormularioSolicitudEntrega;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.*;


import static org.junit.jupiter.api.Assertions.*;

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
            // Resolución de escritorio
            formulario.setSize(720, 420);
            formulario.ajustarDisenoSegunAncho(720);
            formulario.validate();

            JPanel tarjeta = formulario.getPanelTarjeta();
            assertNotNull(tarjeta);
            assertTrue(tarjeta.getWidth() > 0 && tarjeta.getHeight() > 0);
            assertTrue(formulario.getCmbTipoSangre().isShowing());
            assertTrue(formulario.getTxtVolumenMl().isShowing());
            assertTrue(formulario.getWidth() >= tarjeta.getWidth());

            // Resolución móvil / estrecha
            formulario.setSize(340, 360);
            formulario.ajustarDisenoSegunAncho(340);
            formulario.validate();

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
}