package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class TablaInventario extends JFrame {
    private JTable tablaStock;
    private DefaultTableModel modeloTabla;
    private JComboBox<String> cmbFiltroGrupoSanguineo;
    private JButton btnActualizar;
    private JScrollPane scrollPaneTabla;

    public static final String[] GRUPOS_SANGUINEOS = {
            "Todos", "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"
    };

    public static final String[] COLUMNAS_INVENTARIO = {
            "Nombre Donante", "Grupo Sanguíneo", "Volumen (ml)", "Fecha Caducidad"
    };

    public TablaInventario() {
        setTitle("Inventario de Stock de Sangre - Banco de Sangre");
        setSize(700, 420);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        inicializarComponentes();
    }

    private void inicializarComponentes() {
        JPanel panelPrincipal = new JPanel(new BorderLayout(12, 12));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // Panel superior: Título y selector de filtro por grupo sanguíneo
        JPanel panelSuperior = new JPanel(new BorderLayout(10, 10));

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
        tablaStock.setRowHeight(24);
        tablaStock.setFillsViewportHeight(true);

        scrollPaneTabla = new JScrollPane(tablaStock);
        panelPrincipal.add(scrollPaneTabla, BorderLayout.CENTER);

        add(panelPrincipal);
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
}

