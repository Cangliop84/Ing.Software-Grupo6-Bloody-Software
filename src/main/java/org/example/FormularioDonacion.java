package org.example;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

public class FormularioDonacion extends JFrame {
    private JTextField txtNombreDonante;
    private JTextField txtVolumenMl;
    private JLabel lblErrorNombre;
    private JLabel lblErrorVolumen;
    private JLabel lblMensajeEstado;
    private JButton btnGuardar;
    private JButton btnLimpiar;
    private Border bordePorDefecto;
    private final Border bordeError = BorderFactory.createLineBorder(Color.RED, 2);

    public FormularioDonacion() {
        setTitle("Formulario de Donaciones - Banco de Sangre");
        setSize(480, 330);
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

        // Panel inferior: Mensaje de estado y botones Limpiar / Guardar
        JPanel panelInferior = new JPanel(new BorderLayout(5, 8));
        lblMensajeEstado = new JLabel(" ", SwingConstants.CENTER);
        lblMensajeEstado.setForeground(Color.RED);
        lblMensajeEstado.setFont(new Font("SansSerif", Font.BOLD, 12));
        panelInferior.add(lblMensajeEstado, BorderLayout.NORTH);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnLimpiar = new JButton("Limpiar");
        btnLimpiar.setPreferredSize(new Dimension(100, 32));

        btnGuardar = new JButton("Guardar");
        btnGuardar.setPreferredSize(new Dimension(110, 32));

        panelBotones.add(btnLimpiar);
        panelBotones.add(btnGuardar);
        panelInferior.add(panelBotones, BorderLayout.SOUTH);

        panelPrincipal.add(panelInferior, BorderLayout.SOUTH);

        add(panelPrincipal);
    }

    private void configurarEventos() {
        btnGuardar.addActionListener(e -> procesarGuardado());
        btnLimpiar.addActionListener(e -> limpiarFormulario());

        agregarListenerLimpiezaError(txtNombreDonante, lblErrorNombre);
        agregarListenerLimpiezaError(txtVolumenMl, lblErrorVolumen);
    }

    private void agregarListenerLimpiezaError(JTextField campo, JLabel etiquetaError) {
        campo.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                restaurarEstadoCampo(campo, etiquetaError);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                restaurarEstadoCampo(campo, etiquetaError);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                restaurarEstadoCampo(campo, etiquetaError);
            }
        });
    }

    private void restaurarEstadoCampo(JTextField campo, JLabel etiquetaError) {
        if (!campo.getText().trim().isEmpty()) {
            campo.setBorder(bordePorDefecto);
            etiquetaError.setVisible(false);
            if (!lblErrorNombre.isVisible() && !lblErrorVolumen.isVisible()) {
                lblMensajeEstado.setText(" ");
            }
        }
    }

    private void procesarGuardado() {
        if (!validarCamposVacios()) {
            return;
        }
        if (!validarFormatoVolumen()) {
            return;
        }

        lblMensajeEstado.setForeground(new Color(0, 128, 0));
        lblMensajeEstado.setText("Datos validados correctamente.");
        // Pendiente en próximo avance: integrar con CalculadoraCaducidad y
        // Donacion.guardarEnMongo()
    }

    public boolean validarCamposVacios() {
        boolean esValido = true;

        if (txtNombreDonante.getText().trim().isEmpty()) {
            marcarCampoConError(txtNombreDonante, lblErrorNombre, "Información obligatoria");
            esValido = false;
        } else {
            txtNombreDonante.setBorder(bordePorDefecto);
            lblErrorNombre.setVisible(false);
        }

        if (txtVolumenMl.getText().trim().isEmpty()) {
            marcarCampoConError(txtVolumenMl, lblErrorVolumen, "Información obligatoria");
            esValido = false;
        } else {
            txtVolumenMl.setBorder(bordePorDefecto);
            lblErrorVolumen.setVisible(false);
        }

        if (!esValido) {
            lblMensajeEstado.setForeground(Color.RED);
            lblMensajeEstado.setText("Complete la información obligatoria marcada en rojo.");
        } else {
            lblMensajeEstado.setText(" ");
        }

        return esValido;
    }

    public boolean validarFormatoVolumen() {
        String textoVolumen = txtVolumenMl.getText().trim();
        try {
            int volumen = Integer.parseInt(textoVolumen);
            if (volumen <= 0) {
                marcarCampoConError(txtVolumenMl, lblErrorVolumen, "El volumen debe ser mayor a 0 ml");
                lblMensajeEstado.setForeground(Color.RED);
                lblMensajeEstado.setText("Corrija el volumen ingresado.");
                return false;
            }
            txtVolumenMl.setBorder(bordePorDefecto);
            lblErrorVolumen.setVisible(false);
            return true;
        } catch (NumberFormatException ex) {
            marcarCampoConError(txtVolumenMl, lblErrorVolumen, "Ingrese un valor numérico válido");
            lblMensajeEstado.setForeground(Color.RED);
            lblMensajeEstado.setText("El volumen debe ser un número entero.");
            return false;
        }
    }

    private void marcarCampoConError(JTextField campo, JLabel etiquetaError, String mensaje) {
        campo.setBorder(bordeError);
        etiquetaError.setText(mensaje);
        etiquetaError.setVisible(true);
    }

    public void limpiarFormulario() {
        txtNombreDonante.setText("");
        txtVolumenMl.setText("");
        txtNombreDonante.setBorder(bordePorDefecto);
        txtVolumenMl.setBorder(bordePorDefecto);
        lblErrorNombre.setVisible(false);
        lblErrorVolumen.setVisible(false);
        lblMensajeEstado.setText(" ");
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

    public JButton getBtnLimpiar() {
        return btnLimpiar;
    }

    public Border getBordePorDefecto() {
        return bordePorDefecto;
    }

    public Border getBordeError() {
        return bordeError;
    }
}
