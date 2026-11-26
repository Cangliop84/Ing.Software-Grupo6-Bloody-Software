package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;

/**
 * Vista gráfica para el despacho de solicitudes de entrega reservadas (Sprint 02 - T11 / US05).
 * Permite seleccionar una solicitud en estado "Reservado" y disparar el flujo de confirmación y despacho.
 */
public class VistaDespachoSolicitudes extends JFrame {
    public static final String[] COLUMNAS_DESPACHO = {
            "ID Solicitud", "Tipo Sanguíneo", "Volumen (ml)", "Estado"
    };

    private JTable tablaSolicitudes;
    private DefaultTableModel modeloTabla;
    private JButton btnDespachar;
    private JButton btnActualizar;
    private JLabel lblMensajeEstado;
    private String urlEndpointDespacho;

    public VistaDespachoSolicitudes() {
        this("http://localhost:8080" + SolicitudEntregaEndpoint.RUTA_DESPACHO);
    }

    public VistaDespachoSolicitudes(String urlEndpointDespacho) {
        this.urlEndpointDespacho = urlEndpointDespacho;
        setTitle("Despacho de Solicitudes - Banco de Sangre Bloody");
        setSize(720, 420);
        setMinimumSize(new Dimension(520, 320));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        inicializarComponentes();
        configurarEstilosTabla();
        configurarEventos();
    }

    private void inicializarComponentes() {
        JPanel panelPrincipal = new JPanel(new BorderLayout(12, 12));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // Panel superior: Título y botones de acción
        JPanel panelSuperior = new JPanel(new BorderLayout(10, 10));
        JLabel lblTitulo = new JLabel("Solicitudes Reservadas Listas para Despacho", SwingConstants.LEFT);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 16));
        panelSuperior.add(lblTitulo, BorderLayout.WEST);

        JPanel panelAcciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnActualizar = new JButton("Actualizar");
        btnActualizar.setPreferredSize(new Dimension(105, 30));

        btnDespachar = new JButton("Despachar");
        btnDespachar.setPreferredSize(new Dimension(115, 30));
        btnDespachar.setEnabled(false);

        panelAcciones.add(btnActualizar);
        panelAcciones.add(btnDespachar);
        panelSuperior.add(panelAcciones, BorderLayout.EAST);

        panelPrincipal.add(panelSuperior, BorderLayout.NORTH);

        // Panel central: Tabla de solicitudes reservadas
        modeloTabla = new DefaultTableModel(COLUMNAS_DESPACHO, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaSolicitudes = new JTable(modeloTabla);
        tablaSolicitudes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaSolicitudes.setRowHeight(28);
        tablaSolicitudes.setFillsViewportHeight(true);

        JScrollPane scrollPane = new JScrollPane(tablaSolicitudes);
        panelPrincipal.add(scrollPane, BorderLayout.CENTER);

        // Panel inferior: Mensaje de confirmación visual o alerta (CA3)
        JPanel panelInferior = new JPanel(new BorderLayout());
        lblMensajeEstado = new JLabel("Seleccione una solicitud en estado 'Reservado' para despachar.", SwingConstants.LEFT);
        lblMensajeEstado.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblMensajeEstado.setForeground(new Color(70, 70, 70));
        panelInferior.add(lblMensajeEstado, BorderLayout.WEST);

        panelPrincipal.add(panelInferior, BorderLayout.SOUTH);

        add(panelPrincipal);
    }

    private void configurarEstilosTabla() {
        JTableHeader header = tablaSolicitudes.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 13));
        header.setBackground(new Color(180, 35, 35));
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 32));
        header.setReorderingAllowed(false);

        DefaultTableCellRenderer centrarRenderer = new DefaultTableCellRenderer();
        centrarRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < tablaSolicitudes.getColumnCount(); i++) {
            tablaSolicitudes.getColumnModel().getColumn(i).setCellRenderer(centrarRenderer);
        }
    }

    private void configurarEventos() {
        tablaSolicitudes.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                actualizarEstadoBotonDespachar();
            }
        });

        btnDespachar.addActionListener(e -> iniciarFlujoDespacho());
    }

    private void actualizarEstadoBotonDespachar() {
        int fila = tablaSolicitudes.getSelectedRow();
        if (fila < 0) {
            btnDespachar.setEnabled(false);
            return;
        }
        Object estadoObj = tablaSolicitudes.getValueAt(fila, 3);
        String estado = estadoObj != null ? estadoObj.toString().trim() : "";
        btnDespachar.setEnabled("Reservado".equalsIgnoreCase(estado));
    }

    /**
     * Valida que exista una solicitud seleccionada en estado Reservado antes de abrir confirmación.
     */
    public void iniciarFlujoDespacho() {
        int filaSeleccionada = tablaSolicitudes.getSelectedRow();
        if (filaSeleccionada < 0) {
            lblMensajeEstado.setForeground(Color.RED);
            lblMensajeEstado.setText("Debe seleccionar una solicitud para despachar.");
            return;
        }

        String estado = String.valueOf(tablaSolicitudes.getValueAt(filaSeleccionada, 3));
        if (!"Reservado".equalsIgnoreCase(estado)) {
            lblMensajeEstado.setForeground(Color.RED);
            lblMensajeEstado.setText("Solo se pueden despachar solicitudes en estado 'Reservado'.");
            return;
        }

        // Pendiente en Avance 2: abrir modal de confirmación, enviar petición HTTP y remover fila al confirmar
        lblMensajeEstado.setForeground(new Color(0, 102, 153));
        lblMensajeEstado.setText("Solicitud " + tablaSolicitudes.getValueAt(filaSeleccionada, 0) + " lista para confirmar despacho.");
    }

    public void agregarSolicitudEnTabla(String idSolicitud, String tipoSangre, int volumenMl, String estado) {
        modeloTabla.addRow(new Object[]{idSolicitud, tipoSangre, volumenMl, estado});
    }

    public void cargarSolicitudes(List<String[]> filas) {
        modeloTabla.setRowCount(0);
        if (filas != null) {
            for (String[] fila : filas) {
                modeloTabla.addRow(fila);
            }
        }
        actualizarEstadoBotonDespachar();
    }

    public JTable getTablaSolicitudes() {
        return tablaSolicitudes;
    }

    public DefaultTableModel getModeloTabla() {
        return modeloTabla;
    }

    public JButton getBtnDespachar() {
        return btnDespachar;
    }

    public JButton getBtnActualizar() {
        return btnActualizar;
    }

    public JLabel getLblMensajeEstado() {
        return lblMensajeEstado;
    }

    public String getUrlEndpointDespacho() {
        return urlEndpointDespacho;
    }

    public void setUrlEndpointDespacho(String urlEndpointDespacho) {
        this.urlEndpointDespacho = urlEndpointDespacho;
    }
}
