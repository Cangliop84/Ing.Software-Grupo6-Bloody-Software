package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.Collections;
import java.util.List;

public class ModalConfirmacionReserva extends JDialog {
    public static final String[] COLUMNAS_RESUMEN = {
            "ID Muestra", "Donante", "Grupo Sanguíneo", "Volumen (ml)", "Fecha Caducidad"
    };

    private final List<Donacion> muestrasReservadas;
    private final Runnable alCerrarCallback;

    private JTable tablaMuestras;
    private DefaultTableModel modeloTabla;
    private JLabel lblResumenReserva;
    private JButton btnCerrar;

    /**
     * Crea el modal de confirmación sin callback de actualización posterior.
     *
     * @param owner              Ventana padre del diálogo modal.
     * @param muestrasReservadas Lista de muestras seleccionadas y reservadas.
     */
    public ModalConfirmacionReserva(Frame owner, List<Donacion> muestrasReservadas) {
        this(owner, muestrasReservadas, (Runnable) null);
    }

    /**
     * Crea el modal de confirmación conectado a la vista principal de inventario,
     * refrescando el stock al presionar "Cerrar".
     *
     * @param owner              Ventana padre del diálogo modal.
     * @param muestrasReservadas Lista de muestras seleccionadas y reservadas.
     * @param vistaInventario    Vista principal del inventario a actualizar al cerrar.
     */
    public ModalConfirmacionReserva(Frame owner, List<Donacion> muestrasReservadas, TablaInventario vistaInventario) {
        this(owner, muestrasReservadas, vistaInventario != null ? vistaInventario::actualizarStockDesdeServicio : null);
    }

    /**
     * Crea el modal de confirmación con una acción personalizada al cerrar.
     *
     * @param owner              Ventana padre del diálogo modal.
     * @param muestrasReservadas Lista de muestras seleccionadas y reservadas.
     * @param alCerrarCallback   Acción ejecutada al presionar el botón "Cerrar".
     */
    public ModalConfirmacionReserva(Frame owner, List<Donacion> muestrasReservadas, Runnable alCerrarCallback) {
        super(owner, "Confirmación de Reserva de Muestras", true);
        this.muestrasReservadas = muestrasReservadas != null ? muestrasReservadas : Collections.emptyList();
        this.alCerrarCallback = alCerrarCallback;

        setSize(680, 380);
        setMinimumSize(new Dimension(520, 300));
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(owner);

        inicializarComponentes();
        configurarEstilosTabla();
        cargarMuestrasReservadas();
        configurarEventos();
    }

    private void inicializarComponentes() {
        JPanel panelPrincipal = new JPanel(new BorderLayout(12, 12));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        // Encabezado del modal
        JPanel panelSuperior = new JPanel(new BorderLayout(4, 4));
        JLabel lblTitulo = new JLabel("Reserva Automática Confirmada", SwingConstants.LEFT);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblTitulo.setForeground(new Color(0, 110, 45));

        JLabel lblSubtitulo = new JLabel("Detalle de las unidades asignadas por prioridad de caducidad (FIFO):");
        lblSubtitulo.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblSubtitulo.setForeground(new Color(70, 70, 70));

        panelSuperior.add(lblTitulo, BorderLayout.NORTH);
        panelSuperior.add(lblSubtitulo, BorderLayout.SOUTH);
        panelPrincipal.add(panelSuperior, BorderLayout.NORTH);

        // Tabla resumen con las muestras reservadas
        modeloTabla = new DefaultTableModel(COLUMNAS_RESUMEN, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaMuestras = new JTable(modeloTabla);
        tablaMuestras.setRowHeight(26);
        tablaMuestras.setFillsViewportHeight(true);

        JScrollPane scrollPane = new JScrollPane(tablaMuestras);
        panelPrincipal.add(scrollPane, BorderLayout.CENTER);

        // Panel inferior: resumen total y botón Cerrar
        JPanel panelInferior = new JPanel(new BorderLayout(10, 0));
        lblResumenReserva = new JLabel("Muestras reservadas: 0 | Volumen total: 0 ml");
        lblResumenReserva.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblResumenReserva.setForeground(new Color(55, 55, 55));

        btnCerrar = new JButton("Cerrar");
        btnCerrar.setPreferredSize(new Dimension(110, 32));

        panelInferior.add(lblResumenReserva, BorderLayout.WEST);
        panelInferior.add(btnCerrar, BorderLayout.EAST);
        panelPrincipal.add(panelInferior, BorderLayout.SOUTH);

        add(panelPrincipal);
    }

    private void configurarEstilosTabla() {
        JTableHeader header = tablaMuestras.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 12));
        header.setBackground(new Color(180, 35, 35));
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 30));
        header.setReorderingAllowed(false);

        tablaMuestras.setGridColor(new Color(220, 220, 220));
        tablaMuestras.setShowGrid(true);

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 245, 245));
                }
                setHorizontalAlignment(column == 1 ? SwingConstants.LEFT : SwingConstants.CENTER);
                return c;
            }
        };

        for (int i = 0; i < tablaMuestras.getColumnCount(); i++) {
            tablaMuestras.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
    }

    private void cargarMuestrasReservadas() {
        modeloTabla.setRowCount(0);
        int volumenTotal = 0;

        for (Donacion muestra : muestrasReservadas) {
            String id = muestra.getId() != null ? muestra.getId() : "-";
            String donante = muestra.getNombreDonante() != null ? muestra.getNombreDonante() : "Sin nombre";
            String grupo = muestra.getGrupoSanguineo() != null ? muestra.getGrupoSanguineo() : "-";
            int volumen = muestra.getVolumenMl();
            String fechaCaducidad = muestra.getFechaCaducidad() != null
                    ? muestra.getFechaCaducidad().toString()
                    : "-";

            modeloTabla.addRow(new Object[]{id, donante, grupo, volumen, fechaCaducidad});
            volumenTotal += volumen;
        }

        lblResumenReserva.setText("Muestras reservadas: " + muestrasReservadas.size()
                + " | Volumen total: " + volumenTotal + " ml");
    }

    private void configurarEventos() {
        btnCerrar.addActionListener(e -> cerrarModal());
    }

    private void cerrarModal() {
        if (alCerrarCallback != null) {
            alCerrarCallback.run();
        }
        dispose();
    }

    public JTable getTablaMuestras() {
        return tablaMuestras;
    }

    public DefaultTableModel getModeloTabla() {
        return modeloTabla;
    }

    public JLabel getLblResumenReserva() {
        return lblResumenReserva;
    }

    public JButton getBtnCerrar() {
        return btnCerrar;
    }
}
