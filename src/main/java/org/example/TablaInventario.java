package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
//agrego esto para la conexion de las tablas
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.FindIterable;
import org.bson.Document; 
/**
 * Vista gráfica principal de la tabla de inventario de stock de sangre (Tarea T07 - US02).
 * Presenta las unidades registradas en una tabla estilizada y responsiva con selector de
 * filtro por grupo sanguíneo y barra inferior de resumen.
 */
public class TablaInventario extends JFrame {
    private JPanel panelPrincipal;
    private JPanel panelSuperior;
    private JPanel panelFiltro;
    private JLabel lblTitulo;
    private JTable tablaStock;
    private TableRowSorter<DefaultTableModel> sorter;
    private DefaultTableModel modeloTabla;
    private JComboBox<String> cmbFiltroGrupoSanguineo;
    private JButton btnActualizar;
    private JScrollPane scrollPaneTabla;
    private JLabel lblResumenStock;
    private boolean modoCompactoActivo = false;

    public static final String[] GRUPOS_SANGUINEOS = {
            "Todos", "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"
    };

    public static final String[] COLUMNAS_INVENTARIO = {
            "Nombre Donante", "Grupo Sanguíneo", "Volumen (ml)", "Fecha Caducidad"
    };

    private static final double[] PROPORCIONES_COLUMNAS = { 0.35, 0.20, 0.20, 0.25 };
    private static final int UMBRAL_ANCHO_COMPACTO = 620;

    /**
     * Construye e inicializa la ventana de inventario de stock de sangre.
     */
    public TablaInventario() {
        setTitle("Inventario de Stock de Sangre - Banco de Sangre Bloody");
        setSize(740, 460);
        setMinimumSize(new Dimension(480, 320));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        inicializarComponentes();
        configurarEstilosTabla();
        configurarDisenoResponsivo();
        configurarEventos();
        actualizarResumenStock();
    }

    private void inicializarComponentes() {
        panelPrincipal = new JPanel(new BorderLayout(12, 12));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // Panel superior: Título y selector de filtro por grupo sanguíneo
        panelSuperior = new JPanel(new BorderLayout(10, 10));

        lblTitulo = new JLabel("Stock de Unidades de Sangre", SwingConstants.LEFT);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 16));
        panelSuperior.add(lblTitulo, BorderLayout.WEST);

        panelFiltro = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JLabel lblFiltro = new JLabel("Filtrar por Grupo Sanguíneo:");
        cmbFiltroGrupoSanguineo = new JComboBox<>(GRUPOS_SANGUINEOS);
        cmbFiltroGrupoSanguineo.setPreferredSize(new Dimension(110, 28));

        btnActualizar = new JButton("Actualizar");
        btnActualizar.setPreferredSize(new Dimension(100, 28));

        panelFiltro.add(lblFiltro);
        panelFiltro.add(cmbFiltroGrupoSanguineo);
        panelFiltro.add(btnActualizar);

        panelSuperior.add(panelFiltro, BorderLayout.EAST);
        panelPrincipal.add(panelSuperior, BorderLayout.NORTH);

        // Panel central: Tabla de inventario con scroll
        modeloTabla = new DefaultTableModel(COLUMNAS_INVENTARIO, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaStock = new JTable(modeloTabla);
        sorter = new TableRowSorter<>(modeloTabla);
        tablaStock.setRowSorter(sorter);
        tablaStock.setRowHeight(28);
        tablaStock.setFillsViewportHeight(true);
        tablaStock.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        scrollPaneTabla = new JScrollPane(tablaStock);
        panelPrincipal.add(scrollPaneTabla, BorderLayout.CENTER);

        // Panel inferior: Barra de resumen de unidades disponibles
        JPanel panelInferior = new JPanel(new BorderLayout());
        panelInferior.setBorder(BorderFactory.createEmptyBorder(4, 2, 0, 2));
        lblResumenStock = new JLabel("Unidades: 0 | Volumen total: 0 ml");
        lblResumenStock.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblResumenStock.setForeground(new Color(60, 60, 60));
        panelInferior.add(lblResumenStock, BorderLayout.WEST);

        panelPrincipal.add(panelInferior, BorderLayout.SOUTH);

        add(panelPrincipal);
    }

    private void configurarEstilosTabla() {
        JTableHeader header = tablaStock.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 13));
        header.setBackground(new Color(180, 35, 35));
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 32));
        header.setReorderingAllowed(false);

        tablaStock.setSelectionBackground(new Color(235, 200, 200));
        tablaStock.setSelectionForeground(Color.BLACK);
        tablaStock.setGridColor(new Color(220, 220, 220));
        tablaStock.setShowGrid(true);

        DefaultTableCellRenderer centrarRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus,
                    int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 245, 245));
                }
                setHorizontalAlignment(column == 0 ? SwingConstants.LEFT : SwingConstants.CENTER);
                return c;
            }
        };

        for (int i = 0; i < tablaStock.getColumnCount(); i++) {
            tablaStock.getColumnModel().getColumn(i).setCellRenderer(centrarRenderer);
        }
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

    /**
     * Adapta la distribución del panel superior según el ancho de la ventana.
     * Activa el modo compacto en dos filas cuando el ancho es menor al umbral definido.
     *
     * @param anchoVentana Ancho actual de la ventana en píxeles.
     */
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
            panelFiltro.setLayout(new FlowLayout(FlowLayout.CENTER, 6, 0));
            panelSuperior.add(lblTitulo);
            panelSuperior.add(panelFiltro);
        } else {
            panelSuperior.setLayout(new BorderLayout(10, 10));
            lblTitulo.setHorizontalAlignment(SwingConstants.LEFT);
            panelFiltro.setLayout(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            panelSuperior.add(lblTitulo, BorderLayout.WEST);
            panelSuperior.add(panelFiltro, BorderLayout.EAST);
        }

        panelSuperior.revalidate();
    }

    /**
     * Recalcula y asigna proporcionalmente el ancho preferido de cada columna
     * de acuerdo con el espacio disponible en el viewport de la tabla.
     */
    public void ajustarColumnasProporcionalmente() {
        int anchoDisponible = scrollPaneTabla.getViewport().getWidth();
        if (anchoDisponible <= 0) {
            anchoDisponible = getWidth() - 50;
        }
        if (anchoDisponible <= 0) {
            return;
        }

        TableColumnModel columnModel = tablaStock.getColumnModel();
        for (int i = 0; i < columnModel.getColumnCount() && i < PROPORCIONES_COLUMNAS.length; i++) {
            TableColumn columna = columnModel.getColumn(i);
            int anchoColumna = (int) Math.round(anchoDisponible * PROPORCIONES_COLUMNAS[i]);
            columna.setPreferredWidth(Math.max(anchoColumna, 70));
            columna.setMinWidth(60);
        }
    }

    /**
     * Redimensiona la ventana a una resolución dada y fuerza el reajuste de layout y columnas.
     *
     * @param ancho Ancho objetivo en píxeles.
     * @param alto  Alto objetivo en píxeles.
     */
    public void cambiarResolucion(int ancho, int alto) {
        setSize(ancho, alto);
        ajustarLayoutPanelSuperior(ancho);
        doLayout();
        panelPrincipal.doLayout();
        panelSuperior.doLayout();
        scrollPaneTabla.doLayout();
        ajustarColumnasProporcionalmente();
    }

    /**
     * Verifica que la tabla mantenga todas sus columnas visibles, sin desbordamiento
     * horizontal y con anchos mínimos válidos tras cambiar de resolución.
     *
     * @return true si la estructura responsiva es válida; false en caso contrario.
     */
    public boolean esEstructuraResponsivaValida() {
        if (getWidth() <= 0 || getHeight() <= 0) {
            return false;
        }
        boolean tablaVisible = tablaStock.getColumnCount() == COLUMNAS_INVENTARIO.length;
        boolean sinDesbordeHorizontal = scrollPaneTabla.getWidth() <= panelPrincipal.getWidth()
                && panelSuperior.getWidth() <= panelPrincipal.getWidth();
        boolean columnasConAnchoMinimo = true;

        TableColumnModel columnModel = tablaStock.getColumnModel();
        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            if (columnModel.getColumn(i).getWidth() < 0 || columnModel.getColumn(i).getPreferredWidth() < 60) {
                columnasConAnchoMinimo = false;
                break;
            }
        }

        return tablaVisible && sinDesbordeHorizontal && columnasConAnchoMinimo;
    }

    /**
     * Agrega una nueva fila de donación a la tabla de inventario.
     *
     * @param nombreDonante  Nombre del donante.
     * @param grupoSanguineo Grupo sanguíneo de la unidad.
     * @param volumenMl      Volumen donado en mililitros.
     * @param fechaCaducidad Fecha límite de caducidad en formato texto.
     */
    public void agregarFilaInventario(String nombreDonante, String grupoSanguineo, int volumenMl, String fechaCaducidad) {
        modeloTabla.addRow(new Object[]{nombreDonante, grupoSanguineo, volumenMl, fechaCaducidad});
    }

    /**
     * Elimina todas las filas actuales del modelo de la tabla.
     */
    public void limpiarTabla() {
        modeloTabla.setRowCount(0);
    }

    /**
     * Restablece el selector de grupo sanguíneo a la opción "Todos".
     */
    public void restablecerFiltro() {
        cmbFiltroGrupoSanguineo.setSelectedItem("Todos");
    }

    /**
     * Registra los listeners para los eventos de la interfaz visual.
     * Escucha el cambio de selección en el filtro de grupo sanguíneo
     * y las modificaciones en los datos de la tabla.
     */
    private void configurarEventos() {
        cmbFiltroGrupoSanguineo.addActionListener(e -> aplicarFiltro());
        modeloTabla.addTableModelListener(e -> actualizarResumenStock());
        btnActualizar.addActionListener(e -> cargarDatosDesdeMongoDB());
    }
/**
     * Se conecta a MongoDB Atlas en segundo plano, verifica si hay donaciones nuevas,
     * y actualiza la tabla desconectando los filtros para evitar el colapso (IndexOutOfBounds).
     */
    public void cargarDatosDesdeMongoDB() {
        int filasActuales = modeloTabla.getRowCount();

        new Thread(() -> {
            String uri = "mongodb://rdominguez2024_db_user:Jose123@ac-15adclh-shard-00-00.ddnbvei.mongodb.net:27017,ac-15adclh-shard-00-01.ddnbvei.mongodb.net:27017,ac-15adclh-shard-00-02.ddnbvei.mongodb.net:27017/?ssl=true&replicaSet=atlas-t74n7s-shard-0&authSource=admin"; 
            String nombreBaseDatos = "banco_sangre"; 
            String nombreColeccion = "donaciones";

            try (MongoClient mongoClient = MongoClients.create(uri)) {
                MongoDatabase database = mongoClient.getDatabase(nombreBaseDatos);
                MongoCollection<Document> coleccion = database.getCollection(nombreColeccion);

                //contamos si hay datos nuevos antes de hacer cualquier cosa, asi no actualizamos la tabla si no es necesario
                long totalDocumentos = coleccion.countDocuments();
                
                if (totalDocumentos == filasActuales) {
                    SwingUtilities.invokeLater(() -> 
                        javax.swing.JOptionPane.showMessageDialog(this, 
                        "El inventario ya está actualizado. No se han registrado nuevas donaciones.", 
                        "Actualización", javax.swing.JOptionPane.INFORMATION_MESSAGE)
                    );
                    return; 
                }

                //descargamos la lista nueva
                java.util.List<Document> listaDonaciones = new java.util.ArrayList<>();
                coleccion.find().into(listaDonaciones);

                SwingUtilities.invokeLater(() -> {
                    //encontre la solucion al error de la tabla
                    //guardamos el filtro que esta seleccionado
                    Object filtroSeleccionado = cmbFiltroGrupoSanguineo.getSelectedItem();

                    //DESCONECTAMOS el ordenador interno y soltamos cualquier click del mouse, la tabla explota si no pasa eso
                    javax.swing.RowSorter<?> sorterGuardado = tablaStock.getRowSorter();
                    tablaStock.setRowSorter(null); 
                    tablaStock.clearSelection(); 
                    
                    //ahora vaciamos la tabla con total seguridad
                    limpiarTabla(); 

                    //llenamos con los datos descargados
                    for (Document doc : listaDonaciones) {
                        String nombre = doc.getString("nombreDonante");
                        String grupo = doc.getString("grupoSanguineo");
                        String fecha = doc.getString("fechaCaducidad");
                        
                        int volumen = 0;
                        Object volObj = doc.get("volumenMl");
                        if (volObj instanceof Integer) {
                            volumen = (Integer) volObj;
                        } else if (volObj instanceof String) {
                            try {
                                volumen = Integer.parseInt((String) volObj);
                            } catch (NumberFormatException ignored) {}
                        }

                        agregarFilaInventario(
                            nombre != null ? nombre : "Sin nombre", 
                            grupo != null ? grupo : "Desconocido", 
                            volumen, 
                            fecha != null ? fecha : "Sin fecha"
                        );
                    }
                    
                    //vollvemos a conectar el ordenador interno de la tabla
                    tablaStock.setRowSorter((javax.swing.table.TableRowSorter<? extends javax.swing.table.TableModel>) sorterGuardado);
                    
                    //foorzamos un reinicio visual del filtro para evitar "filas fantasma"
                    cmbFiltroGrupoSanguineo.setSelectedItem("Todos"); 
                    if (filtroSeleccionado != null) {
                        cmbFiltroGrupoSanguineo.setSelectedItem(filtroSeleccionado);
                    }
                    
                    actualizarResumenStock();
                    
                    javax.swing.JOptionPane.showMessageDialog(this, 
                        "Inventario actualizado con éxito.", 
                        "Nuevos Datos", javax.swing.JOptionPane.INFORMATION_MESSAGE);
                });

            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> 
                    javax.swing.JOptionPane.showMessageDialog(this, 
                    "Error al conectar con la base de datos:\n" + ex.getMessage(), 
                    "Error de Conexión", javax.swing.JOptionPane.ERROR_MESSAGE)
                );
            }
        }).start();
    }
    /**
     * Aplica el filtro por grupo sanguíneo en la tabla.
     * Si se selecciona Todos, muestra todas las filas.
     * De lo contrario, filtra la columna de grupo sanguíneo por coincidencia exacta.
     */
    private void aplicarFiltro() {
        String grupoSeleccionado = (String) cmbFiltroGrupoSanguineo.getSelectedItem();

        if (grupoSeleccionado == null || grupoSeleccionado.equals("Todos")) {
            sorter.setRowFilter(null);
        } else {
            String regexSeguro = "^" + java.util.regex.Pattern.quote(grupoSeleccionado) + "$";
            sorter.setRowFilter(RowFilter.regexFilter(regexSeguro, 1));
        }
        actualizarResumenStock();
    }

    /**
     * Calcula y actualiza en la barra inferior el total de unidades visibles
     * y la suma de sus volúmenes en mililitros.
     */
    public void actualizarResumenStock() {
        int filasVisibles = tablaStock.getRowCount();
        int volumenTotal = 0;

        for (int i = 0; i < filasVisibles; i++) {
            Object valorVolumen = tablaStock.getValueAt(i, 2);
            if (valorVolumen != null) {
                try {
                    volumenTotal += Integer.parseInt(valorVolumen.toString().trim());
                } catch (NumberFormatException ignored) {
                }
            }
        }

        lblResumenStock.setText("Unidades en vista: " + filasVisibles + " | Volumen total: " + volumenTotal + " ml");
    }

    public boolean isModoCompactoActivo() {
        return modoCompactoActivo;
    }

    public JTable getTablaStock() {
        return tablaStock;
    }

    public DefaultTableModel getModeloTabla() {
        return modeloTabla;
    }

    public JComboBox<String> getCmbFiltroGrupoSanguineo() {
        return cmbFiltroGrupoSanguineo;
    }

    public JButton getBtnActualizar() {
        return btnActualizar;
    }

    public JScrollPane getScrollPaneTabla() {
        return scrollPaneTabla;
    }

    public JLabel getLblResumenStock() {
        return lblResumenStock;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TablaInventario vista = new TablaInventario();
            vista.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            vista.setVisible(true);
        });
    }
}
