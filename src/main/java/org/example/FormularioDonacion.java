package org.example;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class FormularioDonacion extends JFrame {
    private JTextField txtNombreDonante;
    private JTextField txtVolumenMl;
    private JLabel lblErrorNombre;
    private JLabel lblErrorVolumen;
    private JLabel lblMensajeEstado;
    private JButton btnGuardar;
    private Border bordePorDefecto;

    public FormularioDonacion() {
        setTitle("Formulario de Donaciones");
        setSize(450, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        inicializarComponentes();
        configurarEventos();
    }

    private void inicializarComponentes() {
        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        JLabel lblTitulo = new JLabel("Registro de Donación", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 16));
        panelPrincipal.add(lblTitulo, BorderLayout.NORTH);

        JPanel panelFormulario = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 2, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Campo: Nombre del donante
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.35;
        JLabel lblNombre = new JLabel("Nombre donante:");
        panelFormulario.add(lblNombre, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.65;
        txtNombreDonante = new JTextField(18);
        bordePorDefecto = txtNombreDonante.getBorder();
        panelFormulario.add(txtNombreDonante, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 5, 8, 5);
        lblErrorNombre = new JLabel("Información obligatoria");
        lblErrorNombre.setForeground(Color.RED);
        lblErrorNombre.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblErrorNombre.setVisible(false);
        panelFormulario.add(lblErrorNombre, gbc);

        // Campo: Volumen donado (ml)
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.35;
        gbc.insets = new Insets(5, 5, 2, 5);
        JLabel lblVolumen = new JLabel("Volumen donado (ml):");
        panelFormulario.add(lblVolumen, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.65;
        txtVolumenMl = new JTextField(18);
        panelFormulario.add(txtVolumenMl, gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 5, 8, 5);
        lblErrorVolumen = new JLabel("Información obligatoria");
        lblErrorVolumen.setForeground(Color.RED);
        lblErrorVolumen.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblErrorVolumen.setVisible(false);
        panelFormulario.add(lblErrorVolumen, gbc);

        panelPrincipal.add(panelFormulario, BorderLayout.CENTER);

        // Panel inferior: Mensaje de estado y botón Guardar
        JPanel panelInferior = new JPanel(new BorderLayout(5, 8));
        lblMensajeEstado = new JLabel(" ", SwingConstants.CENTER);
        lblMensajeEstado.setForeground(Color.RED);
        lblMensajeEstado.setFont(new Font("SansSerif", Font.BOLD, 12));
        panelInferior.add(lblMensajeEstado, BorderLayout.NORTH);

        JPanel panelBoton = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnGuardar = new JButton("Guardar");
        btnGuardar.setPreferredSize(new Dimension(110, 32));
        panelBoton.add(btnGuardar);
        panelInferior.add(panelBoton, BorderLayout.SOUTH);

        panelPrincipal.add(panelInferior, BorderLayout.SOUTH);

        add(panelPrincipal);
    }

    private void configurarEventos() {
        btnGuardar.addActionListener(e -> procesarGuardado());
    }

    private void procesarGuardado() {
        // Pendiente: lógica de validación de entradas vacías en el próximo avance
    }

    public JTextField getTxtNombreDonante() {
        return txtNombreDonante;
    }

    public JTextField getTxtVolumenMl() {
        return txtVolumenMl;
    }

    public JLabel getLblErrorNombre() {
        return lblErrorNombre;
    }

    public JLabel getLblErrorVolumen() {
        return lblErrorVolumen;
    }

    public JLabel getLblMensajeEstado() {
        return lblMensajeEstado;
    }

    public JButton getBtnGuardar() {
        return btnGuardar;
    }

    public Border getBordePorDefecto() {
        return bordePorDefecto;
    }
}
