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

public class TablaInventario extends JFrame {
    private JPanel panelPrincipal;
    private JPanel panelSuperior;
    private JTable tablaStock;
    private TableRowSorter<DefaultTableModel> sorter;
    private DefaultTableModel modeloTabla;
    private JComboBox<String> cmbFiltroGrupoSanguineo;
    private JButton btnActualizar;
    private JScrollPane scrollPaneTabla;
    private JLabel lblResumenStock;

    public static final String[] GRUPOS_SANGUINEOS = {
            "Todos", "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"
    };

    public static final String[] COLUMNAS_INVENTARIO = {
            "Nombre Donante", "Grupo Sanguíneo", "Volumen (ml)", "Fecha Caducidad"
    };

    private static final double[] PROPORCIONES_COLUMNAS = {0.35, 0.20, 0.20, 0.25};

    public TablaInventario() {
        setTitle("Inventario de Stock de Sangre - Banco de Sangre");
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

        JLabel lblTitulo = new JLabel("Stock de Unidades de Sangre", SwingConstants.LEFT);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 16));
        panelSuperior.add(lblTitulo, BorderLayout.WEST);

        JPanel panelFiltro = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
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
        lblResumenStock = new JLabel("Unidades en vista: 0 | Volumen total: 0 ml");
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
        ajustarColumnasProporcionalmente();

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                ajustarColumnasProporcionalmente();
                revalidate();
                repaint();
            }
        });
    }

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

    public void cambiarResolucion(int ancho, int alto) {
        setSize(ancho, alto);
        doLayout();
        panelPrincipal.doLayout();
        scrollPaneTabla.doLayout();
        ajustarColumnasProporcionalmente();
    }

    public boolean esEstructuraResponsivaValida() {
        if (getWidth() <= 0 || getHeight() <= 0) {
            return false;
        }
        boolean tablaVisible = tablaStock.getColumnCount() == COLUMNAS_INVENTARIO.length;
        boolean sinDesbordeHorizontal = scrollPaneTabla.getWidth() <= panelPrincipal.getWidth();
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

    private void configurarEventos() {
        cmbFiltroGrupoSanguineo.addActionListener(e -> aplicarFiltro());
        modeloTabla.addTableModelListener(e -> actualizarResumenStock());
    }

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
