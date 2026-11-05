package org.example;

import javax.swing.*;
import java.awt.*;

public class FormularioDonacion extends JFrame {
    private JTextField txtNombreDonante;
    private JTextField txtVolumenMl;
    private JButton btnGuardar;

    public FormularioDonacion() {
        setTitle("Formulario de Donaciones");
        setSize(400, 220);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        inicializarComponentes();
    }

    private void inicializarComponentes() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel lblNombre = new JLabel("Nombre donante:");
        txtNombreDonante = new JTextField();

        JLabel lblVolumen = new JLabel("Volumen donado (ml):");
        txtVolumenMl = new JTextField();

        btnGuardar = new JButton("Guardar");

        panel.add(lblNombre);
        panel.add(txtNombreDonante);
        panel.add(lblVolumen);
        panel.add(txtVolumenMl);
        panel.add(new JLabel());
        panel.add(btnGuardar);

        add(panel);
    }

    public JTextField getTxtNombreDonante() {
        return txtNombreDonante;
    }

    public JTextField getTxtVolumenMl() {
        return txtVolumenMl;
    }

    public JButton getBtnGuardar() {
        return btnGuardar;
    }
}
