package org.example;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.List;

/**
 * Formulario de solicitud de entrega de sangre (US03 - T02).
 * Incluye selector de tipo de sangre, input de volumen en ml, validación del botón solicitar
 * y ajuste responsivo del layout.
 */
public class FormularioSolicitudEntrega extends JFrame {
    private static final String[] GRUPOS_SANGUINEOS = {
            "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"
    };
    private static final int UMBRAL_ANCHO_COMPACTO = 460;

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
    private boolean modoCompacto = false;

    public FormularioSolicitudEntrega() {
        this(new SolicitudEntregaService(), null);
    }

    public FormularioSolicitudEntrega(SolicitudEntregaService servicioSolicitud) {
        this(servicioSolicitud, null);
    }

    public FormularioSolicitudEntrega(SolicitudEntregaService servicioSolicitud, TablaInventario vistaInventario) {
        this.servicioSolicitud = servicioSolicitud;
        this.vistaInventario = vistaInventario;
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
        cmbTipoSangre.setSelectedIndex(-1); // Sin selección inicial hasta que el usuario elija

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
            // Vista estrecha / móvil: etiqueta arriba y control abajo en una columna
            panelCampos.setLayout(new GridLayout(4, 1, 4, 4));
            panelCampos.add(lblTipoSangre);
            panelCampos.add(cmbTipoSangre);
            panelCampos.add(lblVolumen);
            panelCampos.add(txtVolumenMl);
        } else {
            // Vista escritorio: dos columnas alineadas
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
        String tipo = seleccionado != null ? seleccionado.toString() : "";
        String volumen = txtVolumenMl.getText();

        SolicitudEntregaService.ResultadoOperacion resultado =
                servicioSolicitud.generarSolicitud(tipo, volumen, true);

        if (resultado.isExito()) {
            lblMensajeEstado.setForeground(new Color(0, 128, 0));
            lblMensajeEstado.setText(resultado.getMensaje());

            try {
                int volumenSolicitado = Integer.parseInt(volumen.trim());
                List<Donacion> muestrasReservadas = servicioSolicitud.seleccionarMuestrasDetalleFifo(tipo, volumenSolicitado);
                ModalConfirmacionReserva modal = new ModalConfirmacionReserva(this, muestrasReservadas, vistaInventario);
                modal.setVisible(true);
            } catch (Exception ignored) {
                // Si aún no hay stock suficiente o conexión disponible para detallar la reserva, se mantiene la confirmación
            }

            limpiarFormulario();
        } else {
            lblMensajeEstado.setForeground(Color.RED);
            lblMensajeEstado.setText(resultado.getMensaje());
        }
    }

    public void limpiarFormulario() {
        cmbTipoSangre.setSelectedIndex(-1);
        txtVolumenMl.setText("");
        lblMensajeEstado.setText(" ");
        actualizarEstadoBotonSolicitar();
    }

    // Getters útiles para pruebas unitarias / UI
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
}