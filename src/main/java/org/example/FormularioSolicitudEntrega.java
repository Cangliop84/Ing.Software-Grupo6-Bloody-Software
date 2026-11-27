package org.example;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.bson.Document;

public class FormularioSolicitudEntrega extends JFrame {
    private static final String[] GRUPOS_SANGUINEOS = {
            "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"
    };
    private static final int UMBRAL_ANCHO_COMPACTO = 460;
    private static final String URL_ENDPOINT_DEFECTO = "http://localhost:8080/api/solicitudes";
    private static final Duration TIMEOUT_CONEXION = Duration.ofSeconds(3);
    private static final Duration TIMEOUT_RESPUESTA = Duration.ofSeconds(5);

    private JPanel panelTarjeta;
    private JPanel panelCampos;
    private JLabel lblTitulo;
    private JLabel lblTipoSangre;
    private JComboBox<String> cmbTipoSangre;
    private JLabel lblVolumen;
    private JTextField txtVolumenMl;
    private JLabel lblMensajeEstado;
    private JButton btnSolicitar;
    private JButton btnLimpiar;

    private final SolicitudEntregaService servicioSolicitud;
    private final TablaInventario vistaInventario;
    private final String urlEndpoint;
    private final HttpClient httpClient;
    private ModalErrorConexion ultimoModalError;
    private boolean modoCompacto = false;

    public FormularioSolicitudEntrega() {
        this(new SolicitudEntregaService(), null, URL_ENDPOINT_DEFECTO);
    }

    public FormularioSolicitudEntrega(SolicitudEntregaService servicioSolicitud) {
        this(servicioSolicitud, null, URL_ENDPOINT_DEFECTO);
    }

    public FormularioSolicitudEntrega(SolicitudEntregaService servicioSolicitud, TablaInventario vistaInventario) {
        this(servicioSolicitud, vistaInventario, URL_ENDPOINT_DEFECTO);
    }

    public FormularioSolicitudEntrega(
            SolicitudEntregaService servicioSolicitud,
            TablaInventario vistaInventario,
            String urlEndpoint
    ) {
        this.servicioSolicitud = servicioSolicitud;
        this.vistaInventario = vistaInventario;
        this.urlEndpoint = (urlEndpoint != null && !urlEndpoint.trim().isEmpty())
                ? urlEndpoint.trim()
                : URL_ENDPOINT_DEFECTO;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(TIMEOUT_CONEXION)
                .build();
        setTitle("Solicitud de Entrega de Sangre");
        setSize(520, 320);
        setMinimumSize(new Dimension(320, 260));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        inicializarComponentes();
        configurarResponsividad();
        configurarEventos();
    }

    private void inicializarComponentes() {
        JPanel fondo = new JPanel(new GridBagLayout());
        fondo.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        panelTarjeta = new JPanel(new BorderLayout(10, 10));
        panelTarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(16, 18, 16, 18)
        ));

        lblTitulo = new JLabel("Solicitud de Entrega", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 16));
        panelTarjeta.add(lblTitulo, BorderLayout.NORTH);

        lblTipoSangre = new JLabel("Tipo de sangre:");
        cmbTipoSangre = new JComboBox<>(GRUPOS_SANGUINEOS);
        cmbTipoSangre.setSelectedIndex(-1);

        lblVolumen = new JLabel("Volumen (ml):");
        txtVolumenMl = new JTextField(12);
        txtVolumenMl.setToolTipText("Ingrese el volumen requerido en mililitros");

        panelCampos = new JPanel();
        reconstruirPanelCampos(false);
        panelTarjeta.add(panelCampos, BorderLayout.CENTER);

        JPanel panelSur = new JPanel(new BorderLayout(6, 8));
        lblMensajeEstado = new JLabel(" ", SwingConstants.CENTER);
        lblMensajeEstado.setFont(new Font("SansSerif", Font.PLAIN, 12));
        panelSur.add(lblMensajeEstado, BorderLayout.NORTH);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnLimpiar = new JButton("Limpiar");
        btnSolicitar = new JButton("Solicitar");
        panelBotones.add(btnLimpiar);
        panelBotones.add(btnSolicitar);
        panelSur.add(panelBotones, BorderLayout.SOUTH);

        panelTarjeta.add(panelSur, BorderLayout.SOUTH);

        GridBagConstraints gbcTarjeta = new GridBagConstraints();
        gbcTarjeta.gridx = 0;
        gbcTarjeta.gridy = 0;
        gbcTarjeta.weightx = 1.0;
        gbcTarjeta.weighty = 1.0;
        gbcTarjeta.fill = GridBagConstraints.BOTH;
        fondo.add(panelTarjeta, gbcTarjeta);

        add(fondo);
    }

    private void configurarResponsividad() {
        ajustarDisenoSegunAncho(getWidth());
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                ajustarDisenoSegunAncho(getWidth());
            }
        });
    }

    public void ajustarDisenoSegunAncho(int ancho) {
        boolean compacto = ancho > 0 && ancho < UMBRAL_ANCHO_COMPACTO;
        if (compacto == modoCompacto && panelCampos.getComponentCount() > 0) {
            return;
        }
        modoCompacto = compacto;
        reconstruirPanelCampos(compacto);
        panelTarjeta.revalidate();
        panelTarjeta.repaint();
    }

    private void reconstruirPanelCampos(boolean compacto) {
        panelCampos.removeAll();
        if (compacto) {
            panelCampos.setLayout(new GridLayout(4, 1, 4, 4));
            panelCampos.add(lblTipoSangre);
            panelCampos.add(cmbTipoSangre);
            panelCampos.add(lblVolumen);
            panelCampos.add(txtVolumenMl);
        } else {
            panelCampos.setLayout(new GridLayout(2, 2, 10, 10));
            panelCampos.add(lblTipoSangre);
            panelCampos.add(cmbTipoSangre);
            panelCampos.add(lblVolumen);
            panelCampos.add(txtVolumenMl);
        }
    }

    private void configurarEventos() {
        btnSolicitar.setEnabled(false);

        cmbTipoSangre.addActionListener(e -> actualizarEstadoBotonSolicitar());
        txtVolumenMl.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                actualizarEstadoBotonSolicitar();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                actualizarEstadoBotonSolicitar();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                actualizarEstadoBotonSolicitar();
            }
        });

        btnLimpiar.addActionListener(e -> limpiarFormulario());
        btnSolicitar.addActionListener(e -> enviarSolicitud());
    }

    public void actualizarEstadoBotonSolicitar() {
        Object seleccionado = cmbTipoSangre.getSelectedItem();
        boolean tipoValido = seleccionado != null && !seleccionado.toString().trim().isEmpty();

        String textoVolumen = txtVolumenMl.getText().trim();
        boolean volumenValido = false;
        if (!textoVolumen.isEmpty()) {
            try {
                volumenValido = Integer.parseInt(textoVolumen) > 0;
            } catch (NumberFormatException ignored) {
                volumenValido = false;
            }
        }

        btnSolicitar.setEnabled(tipoValido && volumenValido);
    }

    private void enviarSolicitud() {
        Object seleccionado = cmbTipoSangre.getSelectedItem();
        String tipo = seleccionado != null ? seleccionado.toString().trim() : "";
        String volumen = txtVolumenMl.getText().trim();

        String payload = new Document("tipoSangre", tipo)
                .append("volumenRequeridoMl", volumen)
                .toJson();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlEndpoint))
                .timeout(TIMEOUT_RESPUESTA)
                .header("Content-Type", "application/json; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            Document body = (response.body() != null && !response.body().trim().isEmpty())
                    ? Document.parse(response.body())
                    : new Document();

            boolean exito = body.containsKey("exito")
                    ? body.getBoolean("exito", false)
                    : (response.statusCode() >= 200 && response.statusCode() < 300);

            String mensaje = body.getString("mensaje");
            if (mensaje == null || mensaje.trim().isEmpty()) {
                mensaje = exito
                        ? "Solicitud de entrega generada correctamente."
                        : "Error al procesar la solicitud (HTTP " + response.statusCode() + ").";
            }

            if (exito) {
                lblMensajeEstado.setForeground(new Color(0, 128, 0));
                lblMensajeEstado.setText(mensaje);
                mostrarAlertaConfirmacion(mensaje);

                try {
                    int volumenSolicitado = Integer.parseInt(volumen);
                    List<Donacion> muestrasReservadas =
                            servicioSolicitud.seleccionarMuestrasDetalleFifo(tipo, volumenSolicitado);

                    String idHospital = "HOSP-DEFAULT";
                    servicioSolicitud.reservarMuestrasEnMongo(muestrasReservadas, idHospital);

                    mostrarModalConfirmacionReserva(muestrasReservadas);
                } catch (Exception ex) {
                    lblMensajeEstado.setForeground(Color.RED);
                    lblMensajeEstado.setText("Error al reservar muestras: " + ex.getMessage());
                }

                limpiarFormulario();
            } else {
                lblMensajeEstado.setForeground(Color.RED);
                lblMensajeEstado.setText(mensaje);
            }
        } catch (Exception ex) {
            String detalle = ex.getMessage() != null && !ex.getMessage().trim().isEmpty()
                    ? ex.getMessage().trim()
                    : ex.getClass().getSimpleName();
            lblMensajeEstado.setForeground(Color.RED);
            lblMensajeEstado.setText("Error de conexión: " + detalle);
            mostrarModalErrorConexion(detalle);
        }
    }

    protected void mostrarAlertaConfirmacion(String mensaje) {
        JOptionPane.showMessageDialog(
                this,
                mensaje,
                "Confirmación de solicitud",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    protected void mostrarModalConfirmacionReserva(List<Donacion> muestrasReservadas) {
        ModalConfirmacionReserva modal =
                new ModalConfirmacionReserva(this, muestrasReservadas, vistaInventario);
        modal.setVisible(true);
    }

    public void mostrarModalErrorConexion(String detalle) {
        ultimoModalError = new ModalErrorConexion(this, detalle);
        ultimoModalError.setVisible(true);
    }

    public void limpiarFormulario() {
        cmbTipoSangre.setSelectedIndex(-1);
        txtVolumenMl.setText("");
        lblMensajeEstado.setText(" ");
        actualizarEstadoBotonSolicitar();
    }

    public JComboBox<String> getCmbTipoSangre() {
        return cmbTipoSangre;
    }

    public JTextField getTxtVolumenMl() {
        return txtVolumenMl;
    }

    public JButton getBtnSolicitar() {
        return btnSolicitar;
    }

    public JButton getBtnLimpiar() {
        return btnLimpiar;
    }

    public JPanel getPanelTarjeta() {
        return panelTarjeta;
    }

    public JLabel getLblMensajeEstado() {
        return lblMensajeEstado;
    }

    public ModalErrorConexion getUltimoModalError() {
        return ultimoModalError;
    }
}