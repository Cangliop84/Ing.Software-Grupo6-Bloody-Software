package org.example;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Vista gráfica del panel de control de solicitudes pendientes de despacho (Sprint 02 - T10 y T11 / US05).
 * Filtra y renderiza únicamente las solicitudes en estado "Reservado", ofrece diseño responsivo
 * y dispone un botón de acción "Despachar" por fila para confirmar y ejecutar la entrega.
 */
public class VistaDespachoSolicitudes extends JFrame {
    public static final String[] COLUMNAS_DESPACHO = {
            "ID Solicitud", "Tipo Sanguíneo", "Volumen (ml)", "Estado", "Acción"
    };

    private static final double[] PROPORCIONES_COLUMNAS = {0.26, 0.18, 0.18, 0.18, 0.20};
    private static final int UMBRAL_ANCHO_COMPACTO = 580;

    private static final String MONGO_URI = "mongodb://rdominguez2024_db_user:Jose123@ac-15adclh-shard-00-00.ddnbvei.mongodb.net:27017,ac-15adclh-shard-00-01.ddnbvei.mongodb.net:27017,ac-15adclh-shard-00-02.ddnbvei.mongodb.net:27017/?ssl=true&replicaSet=atlas-t74n7s-shard-0&authSource=admin";
    private static final String DATABASE_NAME = "banco_sangre";
    private static final String COLLECTION_SOLICITUDES = "solicitudes_entrega";
    private static final Duration TIMEOUT_CONEXION = Duration.ofSeconds(3);
    private static final Duration TIMEOUT_RESPUESTA = Duration.ofSeconds(5);

    private JPanel panelPrincipal;
    private JPanel panelSuperior;
    private JPanel panelAcciones;
    private JLabel lblTitulo;
    private JTable tablaSolicitudes;
    private DefaultTableModel modeloTabla;
    private TableRowSorter<DefaultTableModel> sorter;
    private JScrollPane scrollPane;
    private JButton btnDespachar;
    private JButton btnActualizar;
    private JLabel lblMensajeEstado;
    private boolean modoCompactoActivo = false;

    private String urlEndpointDespacho;
    private final HttpClient httpClient;
    private final TablaInventario vistaInventario;
    private ModalConfirmacionDespacho ultimoModalConfirmacion;

    public VistaDespachoSolicitudes() {
        this("http://localhost:8080" + SolicitudEntregaEndpoint.RUTA_DESPACHO, null);
    }

    public VistaDespachoSolicitudes(String urlEndpointDespacho) {
        this(urlEndpointDespacho, null);
    }

    public VistaDespachoSolicitudes(String urlEndpointDespacho, TablaInventario vistaInventario) {
        this.urlEndpointDespacho = (urlEndpointDespacho != null && !urlEndpointDespacho.trim().isEmpty())
                ? urlEndpointDespacho.trim()
                : "http://localhost:8080" + SolicitudEntregaEndpoint.RUTA_DESPACHO;
        this.vistaInventario = vistaInventario;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(TIMEOUT_CONEXION)
                .build();

        setTitle("Panel de Solicitudes Pendientes - Banco de Sangre Bloody");
        setSize(760, 440);
        setMinimumSize(new Dimension(480, 320));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        inicializarComponentes();
        configurarEstilosTabla();
        configurarDisenoResponsivo();
        configurarEventos();
    }

    private void inicializarComponentes() {
        panelPrincipal = new JPanel(new BorderLayout(12, 12));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // Panel superior: Título y botones de acción
        panelSuperior = new JPanel(new BorderLayout(10, 10));
        lblTitulo = new JLabel("Solicitudes Reservadas Listas para Despacho", SwingConstants.LEFT);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 16));
        panelSuperior.add(lblTitulo, BorderLayout.WEST);

        panelAcciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnActualizar = new JButton("Actualizar");
        btnActualizar.setPreferredSize(new Dimension(105, 30));

        btnDespachar = new JButton("Despachar");
        btnDespachar.setPreferredSize(new Dimension(115, 30));
        btnDespachar.setEnabled(false);

        panelAcciones.add(btnActualizar);
        panelAcciones.add(btnDespachar);
        panelSuperior.add(panelAcciones, BorderLayout.EAST);

        panelPrincipal.add(panelSuperior, BorderLayout.NORTH);

        // Panel central: Tabla responsiva filtrada únicamente por estado "Reservado"
        modeloTabla = new DefaultTableModel(COLUMNAS_DESPACHO, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4;
            }
        };

        tablaSolicitudes = new JTable(modeloTabla);
        sorter = new TableRowSorter<>(modeloTabla);
        sorter.setRowFilter(RowFilter.regexFilter("(?i)^Reservado$", 3));
        tablaSolicitudes.setRowSorter(sorter);

        tablaSolicitudes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaSolicitudes.setRowHeight(32);
        tablaSolicitudes.setFillsViewportHeight(true);
        tablaSolicitudes.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        scrollPane = new JScrollPane(tablaSolicitudes);
        panelPrincipal.add(scrollPane, BorderLayout.CENTER);

        // Panel inferior: Mensaje de estado
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

        tablaSolicitudes.setSelectionBackground(new Color(235, 200, 200));
        tablaSolicitudes.setSelectionForeground(Color.BLACK);
        tablaSolicitudes.setGridColor(new Color(220, 220, 220));
        tablaSolicitudes.setShowGrid(true);

        DefaultTableCellRenderer centrarRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 245, 245));
                }
                setHorizontalAlignment(SwingConstants.CENTER);
                return c;
            }
        };

        for (int i = 0; i < tablaSolicitudes.getColumnCount() - 1; i++) {
            tablaSolicitudes.getColumnModel().getColumn(i).setCellRenderer(centrarRenderer);
        }

        // Renderizador y editor para el botón de acción "Despachar" en cada fila
        TableColumn columnaAccion = tablaSolicitudes.getColumnModel().getColumn(4);
        columnaAccion.setCellRenderer(new BotonDespacharRenderer());
        columnaAccion.setCellEditor(new BotonDespacharEditor());
    }

    private void configurarDisenoResponsivo() {
        ajustarVistaSegunResolucion();

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                ajustarVistaSegunResolucion();
            }
        });
    }

    private void ajustarVistaSegunResolucion() {
        ajustarLayoutPanelSuperior(getWidth());
        ajustarColumnasProporcionalmente();
        revalidate();
        repaint();
    }

    public void ajustarLayoutPanelSuperior(int anchoVentana) {
        boolean requiereModoCompacto = anchoVentana > 0 && anchoVentana < UMBRAL_ANCHO_COMPACTO;
        if (requiereModoCompacto == modoCompactoActivo) {
            return;
        }

        modoCompactoActivo = requiereModoCompacto;
        panelSuperior.removeAll();

        if (modoCompactoActivo) {
            panelSuperior.setLayout(new GridLayout(2, 1, 4, 6));
            lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
            panelAcciones.setLayout(new FlowLayout(FlowLayout.CENTER, 6, 0));
            panelSuperior.add(lblTitulo);
            panelSuperior.add(panelAcciones);
        } else {
            panelSuperior.setLayout(new BorderLayout(10, 10));
            lblTitulo.setHorizontalAlignment(SwingConstants.LEFT);
            panelAcciones.setLayout(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            panelSuperior.add(lblTitulo, BorderLayout.WEST);
            panelSuperior.add(panelAcciones, BorderLayout.EAST);
        }

        panelSuperior.revalidate();
    }

    public void ajustarColumnasProporcionalmente() {
        int anchoDisponible = scrollPane.getViewport().getWidth();
        if (anchoDisponible <= 0) {
            anchoDisponible = getWidth() - 50;
        }
        if (anchoDisponible <= 0) {
            return;
        }

        TableColumnModel columnModel = tablaSolicitudes.getColumnModel();
        for (int i = 0; i < columnModel.getColumnCount() && i < PROPORCIONES_COLUMNAS.length; i++) {
            TableColumn columna = columnModel.getColumn(i);
            int anchoColumna = (int) Math.round(anchoDisponible * PROPORCIONES_COLUMNAS[i]);
            columna.setPreferredWidth(Math.max(anchoColumna, 75));
            columna.setMinWidth(65);
        }
    }

    public void cambiarResolucion(int ancho, int alto) {
        setSize(ancho, alto);
        ajustarLayoutPanelSuperior(ancho);
        doLayout();
        panelPrincipal.doLayout();
        panelSuperior.doLayout();
        scrollPane.doLayout();
        ajustarColumnasProporcionalmente();
    }

    public boolean esEstructuraResponsivaValida() {
        if (getWidth() <= 0 || getHeight() <= 0) {
            return false;
        }
        boolean tablaVisible = tablaSolicitudes.getColumnCount() == COLUMNAS_DESPACHO.length;
        boolean sinDesbordeHorizontal = scrollPane.getWidth() <= panelPrincipal.getWidth()
                && panelSuperior.getWidth() <= panelPrincipal.getWidth();
        boolean columnasConAnchoMinimo = true;

        TableColumnModel columnModel = tablaSolicitudes.getColumnModel();
        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            if (columnModel.getColumn(i).getWidth() < 0 || columnModel.getColumn(i).getPreferredWidth() < 65) {
                columnasConAnchoMinimo = false;
                break;
            }
        }

        return tablaVisible && sinDesbordeHorizontal && columnasConAnchoMinimo;
    }

    private void configurarEventos() {
        tablaSolicitudes.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                actualizarEstadoBotonDespachar();
            }
        });

        btnDespachar.addActionListener(e -> iniciarFlujoDespacho());
        btnActualizar.addActionListener(e -> cargarSolicitudesReservadasDesdeMongo());
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
     * Valida la fila seleccionada y despliega el modal de confirmación antes de despachar.
     */
    public void iniciarFlujoDespacho() {
        int filaVista = tablaSolicitudes.getSelectedRow();
        if (filaVista < 0) {
            lblMensajeEstado.setForeground(Color.RED);
            lblMensajeEstado.setText("Debe seleccionar una solicitud para despachar.");
            return;
        }

        int filaModelo = tablaSolicitudes.convertRowIndexToModel(filaVista);
        String idSolicitud = String.valueOf(modeloTabla.getValueAt(filaModelo, 0));
        String tipoSangre = String.valueOf(modeloTabla.getValueAt(filaModelo, 1));
        int volumenMl = Integer.parseInt(String.valueOf(modeloTabla.getValueAt(filaModelo, 2)));
        String estado = String.valueOf(modeloTabla.getValueAt(filaModelo, 3));

        if (!"Reservado".equalsIgnoreCase(estado)) {
            lblMensajeEstado.setForeground(Color.RED);
            lblMensajeEstado.setText("Solo se pueden despachar solicitudes en estado 'Reservado'.");
            return;
        }

        ultimoModalConfirmacion = new ModalConfirmacionDespacho(
                this,
                idSolicitud,
                tipoSangre,
                volumenMl,
                () -> ejecutarDespachoHttp(filaModelo, idSolicitud)
        );
        mostrarModalConfirmacion(ultimoModalConfirmacion);
    }

    protected void mostrarModalConfirmacion(ModalConfirmacionDespacho modal) {
        modal.setVisible(true);
    }

    /**
     * Envía la petición HTTP POST al endpoint de despacho y, si es exitosa,
     * remueve dinámicamente la fila de la tabla y emite la confirmación visual.
     */
    public boolean ejecutarDespachoHttp(int filaModelo, String idSolicitud) {
        String payload = new Document("idSolicitud", idSolicitud).toJson();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlEndpointDespacho))
                .timeout(TIMEOUT_RESPUESTA)
                .header("Content-Type", "application/json; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
            );
            Document body = (response.body() != null && !response.body().trim().isEmpty())
                    ? Document.parse(response.body())
                    : new Document();

            boolean exito = body.containsKey("exito")
                    ? body.getBoolean("exito", false)
                    : (response.statusCode() >= 200 && response.statusCode() < 300);

            String mensaje = body.getString("mensaje");
            if (mensaje == null || mensaje.trim().isEmpty()) {
                mensaje = exito
                        ? "Solicitud " + idSolicitud + " despachada y stock descontado exitosamente."
                        : "Error al despachar la solicitud (HTTP " + response.statusCode() + ").";
            }

            if (exito) {
                if (filaModelo >= 0 && filaModelo < modeloTabla.getRowCount()
                        && idSolicitud.equals(String.valueOf(modeloTabla.getValueAt(filaModelo, 0)))) {
                    modeloTabla.removeRow(filaModelo);
                } else {
                    removerFilaPorId(idSolicitud);
                }
                actualizarEstadoBotonDespachar();
                lblMensajeEstado.setForeground(new Color(0, 128, 0));
                lblMensajeEstado.setText(mensaje);

                if (vistaInventario != null) {
                    vistaInventario.actualizarStockDesdeServicio();
                }
                return true;
            } else {
                lblMensajeEstado.setForeground(Color.RED);
                lblMensajeEstado.setText(mensaje);
                return false;
            }
        } catch (Exception ex) {
            String detalle = ex.getMessage() != null && !ex.getMessage().trim().isEmpty()
                    ? ex.getMessage().trim()
                    : ex.getClass().getSimpleName();
            lblMensajeEstado.setForeground(Color.RED);
            lblMensajeEstado.setText("Error de conexión al despachar: " + detalle);
            return false;
        }
    }

    private void removerFilaPorId(String idSolicitud) {
        for (int i = 0; i < modeloTabla.getRowCount(); i++) {
            if (idSolicitud.equals(String.valueOf(modeloTabla.getValueAt(i, 0)))) {
                modeloTabla.removeRow(i);
                break;
            }
        }
    }

    public void cargarSolicitudesReservadasDesdeMongo() {
        new Thread(() -> {
            try (MongoClient mongoClient = MongoClients.create(MONGO_URI)) {
                MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
                MongoCollection<Document> coleccion = database.getCollection(COLLECTION_SOLICITUDES);

                List<Object[]> filas = new ArrayList<>();
                for (Document doc : coleccion.find(Filters.eq("estado", "Reservado"))) {
                    Object rawId = doc.get("_id");
                    String id = rawId != null ? rawId.toString() : "";
                    String tipo = doc.getString("tipoSangre");
                    Integer volumen = doc.getInteger("volumenRequeridoMl");
                    String estado = doc.getString("estado");
                    filas.add(new Object[]{
                            id,
                            tipo != null ? tipo : "-",
                            volumen != null ? volumen : 0,
                            estado != null ? estado : "Reservado",
                            "Despachar"
                    });
                }

                SwingUtilities.invokeLater(() -> {
                    modeloTabla.setRowCount(0);
                    for (Object[] fila : filas) {
                        modeloTabla.addRow(fila);
                    }
                    actualizarEstadoBotonDespachar();
                    lblMensajeEstado.setForeground(new Color(70, 70, 70));
                    lblMensajeEstado.setText("Solicitudes reservadas cargadas: " + tablaSolicitudes.getRowCount());
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    lblMensajeEstado.setForeground(Color.RED);
                    lblMensajeEstado.setText("Error al cargar solicitudes reservadas: " + ex.getMessage());
                });
            }
        }).start();
    }

    public void agregarSolicitudEnTabla(String idSolicitud, String tipoSangre, int volumenMl, String estado) {
        modeloTabla.addRow(new Object[]{idSolicitud, tipoSangre, volumenMl, estado, "Despachar"});
    }

    public void cargarSolicitudes(List<Object[]> filas) {
        modeloTabla.setRowCount(0);
        if (filas != null) {
            for (Object[] fila : filas) {
                if (fila != null && fila.length >= 4) {
                    String accion = (fila.length >= 5 && fila[4] != null) ? fila[4].toString() : "Despachar";
                    modeloTabla.addRow(new Object[]{fila[0], fila[1], fila[2], fila[3], accion});
                }
            }
        }
        actualizarEstadoBotonDespachar();
    }

    /**
     * Renderizador que dibuja el botón "Despachar" en la columna de acción de cada fila.
     */
    private static class BotonDespacharRenderer extends JButton implements TableCellRenderer {
        public BotonDespacharRenderer() {
            setOpaque(true);
            setFont(new Font("SansSerif", Font.BOLD, 12));
            setBackground(new Color(180, 35, 35));
            setForeground(Color.WHITE);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            setText(value != null ? value.toString() : "Despachar");
            return this;
        }
    }

    /**
     * Editor de celda que permite presionar el botón "Despachar" directamente sobre una fila.
     */
    private class BotonDespacharEditor extends DefaultCellEditor {
        private final JButton boton;
        private String etiqueta;
        private int filaActual = -1;

        public BotonDespacharEditor() {
            super(new JCheckBox());
            boton = new JButton("Despachar");
            boton.setOpaque(true);
            boton.setFont(new Font("SansSerif", Font.BOLD, 12));
            boton.setBackground(new Color(180, 35, 35));
            boton.setForeground(Color.WHITE);
            boton.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            etiqueta = value != null ? value.toString() : "Despachar";
            boton.setText(etiqueta);
            filaActual = row;
            return boton;
        }

        @Override
        public Object getCellEditorValue() {
            if (filaActual >= 0 && filaActual < tablaSolicitudes.getRowCount()) {
                int filaSeleccionar = filaActual;
                SwingUtilities.invokeLater(() -> {
                    if (filaSeleccionar < tablaSolicitudes.getRowCount()) {
                        tablaSolicitudes.setRowSelectionInterval(filaSeleccionar, filaSeleccionar);
                        iniciarFlujoDespacho();
                    }
                });
            }
            return etiqueta;
        }
    }

    public boolean isModoCompactoActivo() {
        return modoCompactoActivo;
    }

    public TableRowSorter<DefaultTableModel> getSorter() {
        return sorter;
    }

    public ModalConfirmacionDespacho getUltimoModalConfirmacion() {
        return ultimoModalConfirmacion;
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
