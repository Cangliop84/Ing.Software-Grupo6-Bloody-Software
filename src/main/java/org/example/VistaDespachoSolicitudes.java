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
import java.awt.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Vista gráfica para el despacho de solicitudes de entrega reservadas (Sprint 02 - T11 / US05).
 * Permite seleccionar una solicitud en estado "Reservado", confirmar el despacho en un modal
 * y consumir el endpoint HTTP del backend removiendo dinámicamente la fila al confirmar éxito.
 */
public class VistaDespachoSolicitudes extends JFrame {
    public static final String[] COLUMNAS_DESPACHO = {
            "ID Solicitud", "Tipo Sanguíneo", "Volumen (ml)", "Estado"
    };

    private static final String MONGO_URI = "mongodb://rdominguez2024_db_user:Jose123@ac-15adclh-shard-00-00.ddnbvei.mongodb.net:27017,ac-15adclh-shard-00-01.ddnbvei.mongodb.net:27017,ac-15adclh-shard-00-02.ddnbvei.mongodb.net:27017/?ssl=true&replicaSet=atlas-t74n7s-shard-0&authSource=admin";
    private static final String DATABASE_NAME = "banco_sangre";
    private static final String COLLECTION_SOLICITUDES = "solicitudes_entrega";
    private static final Duration TIMEOUT_CONEXION = Duration.ofSeconds(3);
    private static final Duration TIMEOUT_RESPUESTA = Duration.ofSeconds(5);

    private JTable tablaSolicitudes;
    private DefaultTableModel modeloTabla;
    private JButton btnDespachar;
    private JButton btnActualizar;
    private JLabel lblMensajeEstado;
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
     * remueve dinámicamente la fila de la tabla y emite la confirmación visual (CA3).
     *
     * @param filaModelo  Índice de la fila en el modelo de tabla.
     * @param idSolicitud Identificador de la solicitud a despachar.
     * @return true si el backend confirmó el despacho exitosamente; false en caso de error.
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
                            estado != null ? estado : "Reservado"
                    });
                }

                SwingUtilities.invokeLater(() -> {
                    modeloTabla.setRowCount(0);
                    for (Object[] fila : filas) {
                        modeloTabla.addRow(fila);
                    }
                    actualizarEstadoBotonDespachar();
                    lblMensajeEstado.setForeground(new Color(70, 70, 70));
                    lblMensajeEstado.setText("Solicitudes reservadas cargadas: " + filas.size());
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
        modeloTabla.addRow(new Object[]{idSolicitud, tipoSangre, volumenMl, estado});
    }

    public void cargarSolicitudes(List<Object[]> filas) {
        modeloTabla.setRowCount(0);
        if (filas != null) {
            for (Object[] fila : filas) {
                modeloTabla.addRow(fila);
            }
        }
        actualizarEstadoBotonDespachar();
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
