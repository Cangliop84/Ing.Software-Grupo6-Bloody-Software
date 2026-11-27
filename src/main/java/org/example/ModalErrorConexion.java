package org.example;

import javax.swing.*;
import java.awt.*;

public class ModalErrorConexion extends JDialog {

    private final JLabel lblTitulo;
    private final JTextArea txtDetalle;
    private final JButton btnCerrar;

    public ModalErrorConexion(Frame owner, String detalleError) {
        super(owner, "Error de conexión", true);
        setSize(420, 210);
        setMinimumSize(new Dimension(320, 170));
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel raiz = new JPanel(new BorderLayout(10, 10));
        raiz.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        lblTitulo = new JLabel("No se pudo completar la conexión con el servidor.");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblTitulo.setForeground(new Color(180, 0, 0));
        raiz.add(lblTitulo, BorderLayout.NORTH);

        String texto = (detalleError != null && !detalleError.trim().isEmpty())
                ? detalleError.trim()
                : "Tiempo de espera agotado o fallo en la red.";
        txtDetalle = new JTextArea(texto);
        txtDetalle.setEditable(false);
        txtDetalle.setLineWrap(true);
        txtDetalle.setWrapStyleWord(true);
        txtDetalle.setBackground(raiz.getBackground());
        txtDetalle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        raiz.add(new JScrollPane(txtDetalle), BorderLayout.CENTER);

        JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnCerrar = new JButton("Cerrar");
        btnCerrar.addActionListener(e -> dispose());
        panelInferior.add(btnCerrar);
        raiz.add(panelInferior, BorderLayout.SOUTH);

        add(raiz);
    }

    public JTextArea getTxtDetalle() {
        return txtDetalle;
    }

    public JButton getBtnCerrar() {
        return btnCerrar;
    }
}
