package org.example;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.time.LocalDate;

/**
 * Vista gráfica principal para el registro de donaciones de sangre (Tarea T02 - US01).
 * Permite ingresar los datos del donante y el volumen extraído (ml), validando
 * entradas obligatorias y formato numérico antes de registrar la donación.
 */
public class FormularioDonacion extends JFrame {
    private JTextField txtNombreDonante;
    private JTextField txtVolumenMl;
    private JLabel lblErrorNombre;
    private JLabel lblErrorVolumen;
    private JLabel lblMensajeEstado;
    private JLabel lblFechaCaducidad;
    private JButton btnGuardar;
    private JButton btnLimpiar;
    private JButton btnVerInventario;
    private Border bordePorDefecto;
    private final Border bordeError = BorderFactory.createLineBorder(Color.RED, 2);

    /**
     * Construye e inicializa la ventana del formulario de donaciones.
     */
    public FormularioDonacion() {
        setTitle("Formulario de Donaciones - Banco de Sangre");
        setSize(500, 360);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        inicializarComponentes();
        configurarEventos();
    }

    /**
     * Maqueta y distribuye los componentes gráficos dentro del formulario.
     */
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
        JLabel lblNombre = new JLabel("Nombre donante (*):");
        panelFormulario.add(lblNombre, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.65;
        txtNombreDonante = new JTextField(18);
        txtNombreDonante.setToolTipText("Ingrese el nombre completo del donante (campo obligatorio)");
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
        JLabel lblVolumen = new JLabel("Volumen donado ml (*):");
        panelFormulario.add(lblVolumen, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.65;
        txtVolumenMl = new JTextField(18);
        txtVolumenMl.setToolTipText("Ingrese el volumen extraído en mililitros (campo obligatorio)");
        panelFormulario.add(txtVolumenMl, gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 5, 8, 5);
        lblErrorVolumen = new JLabel("Información obligatoria");
        lblErrorVolumen.setForeground(Color.RED);
        lblErrorVolumen.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblErrorVolumen.setVisible(false);
        panelFormulario.add(lblErrorVolumen, gbc);

        // Etiqueta informativa de caducidad calculada
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(6, 5, 4, 5);
        lblFechaCaducidad = new JLabel(" ", SwingConstants.CENTER);
        lblFechaCaducidad.setForeground(new Color(70, 70, 70));
        lblFechaCaducidad.setFont(new Font("SansSerif", Font.ITALIC, 12));
        panelFormulario.add(lblFechaCaducidad, gbc);

        panelPrincipal.add(panelFormulario, BorderLayout.CENTER);

        // Panel inferior: Mensaje de estado y botones Limpiar / Guardar
        JPanel panelInferior = new JPanel(new BorderLayout(5, 8));
        lblMensajeEstado = new JLabel(" ", SwingConstants.CENTER);
        lblMensajeEstado.setForeground(Color.RED);
        lblMensajeEstado.setFont(new Font("SansSerif", Font.BOLD, 12));
        panelInferior.add(lblMensajeEstado, BorderLayout.NORTH);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnVerInventario = new JButton("Ver Inventario");
        btnVerInventario.setPreferredSize(new Dimension(120, 32));

        btnLimpiar = new JButton("Limpiar");
        btnLimpiar.setPreferredSize(new Dimension(100, 32));

        btnGuardar = new JButton("Guardar");
        btnGuardar.setPreferredSize(new Dimension(110, 32));

        panelBotones.add(btnVerInventario);
        panelBotones.add(btnLimpiar);
        panelBotones.add(btnGuardar);
        panelInferior.add(panelBotones, BorderLayout.SOUTH);

        panelPrincipal.add(panelInferior, BorderLayout.SOUTH);

        add(panelPrincipal);
    }

    /**
     * Registra los listeners de acción de los botones y de edición en los campos de texto.
     */
    private void configurarEventos() {
        btnGuardar.addActionListener(e -> procesarGuardado());
        btnLimpiar.addActionListener(e -> limpiarFormulario());
        btnVerInventario.addActionListener(e -> {
            TablaInventario vistaInventario = new TablaInventario();
            vistaInventario.setVisible(true);
        });

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
            if (!lblErrorNombre.isVisible() && !lblErrorVolumen.isVisible()
                    && Color.RED.equals(lblMensajeEstado.getForeground())) {
                lblMensajeEstado.setText(" ");
            }
        }
    }

    /**
     * Ejecuta el flujo de validación y guardado de la donación cuando se presiona el botón Guardar.
     */
    public void procesarGuardado() {
        if (!validarCamposVacios()) {
            lblFechaCaducidad.setText(" ");
            return;
        }

        if (!validarFormatoVolumen()) {
            lblFechaCaducidad.setText(" ");
            return;
        }

        String nombre = txtNombreDonante.getText().trim();
        int volumen = Integer.parseInt(txtVolumenMl.getText().trim());

        LocalDate fechaHoy = LocalDate.now();
        LocalDate fechaCaducidad = CalculadoraCaducidad.calcularFechaCaducidad(fechaHoy);

        try {
            Donacion donacion = new Donacion(nombre, volumen, fechaCaducidad);
            donacion.guardarEnMongo();

            lblMensajeEstado.setForeground(new Color(0, 128, 0));
            lblMensajeEstado.setText("¡Donación registrada y guardada con éxito!");
            lblFechaCaducidad.setText("Fecha de caducidad asignada: " + fechaCaducidad);

            txtNombreDonante.setText("");
            txtVolumenMl.setText("");

        } catch (Exception ex) {
            lblMensajeEstado.setForeground(Color.RED);
            lblMensajeEstado.setText("Error al guardar la donación en la base de datos.");
            ex.printStackTrace();
        }
    }

    /**
     * Valida que los campos obligatorios no estén vacíos.
     * Si algún campo está vacío, lo marca con borde rojo y muestra "Información obligatoria".
     *
     * @return true si todos los campos tienen contenido; false en caso contrario.
     */
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

    /**
     * Valida que el volumen ingresado sea un número entero positivo mayor a 0.
     *
     * @return true si el volumen es numérico y mayor a 0; false en caso contrario.
     */
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

    /**
     * Verifica si el campo de nombre del donante se encuentra marcado en rojo con alerta visible.
     *
     * @return true si el campo nombre tiene estado de error activo.
     */
    public boolean tieneErrorEnNombre() {
        return txtNombreDonante.getBorder() == bordeError && lblErrorNombre.isVisible();
    }

    /**
     * Verifica si el campo de volumen se encuentra marcado en rojo con alerta visible.
     *
     * @return true si el campo volumen tiene estado de error activo.
     */
    public boolean tieneErrorEnVolumen() {
        return txtVolumenMl.getBorder() == bordeError && lblErrorVolumen.isVisible();
    }

    /**
     * Restablece todos los campos, bordes y etiquetas del formulario a su estado inicial.
     */
    public void limpiarFormulario() {
        txtNombreDonante.setText("");
        txtVolumenMl.setText("");
        txtNombreDonante.setBorder(bordePorDefecto);
        txtVolumenMl.setBorder(bordePorDefecto);
        lblErrorNombre.setVisible(false);
        lblErrorVolumen.setVisible(false);
        lblMensajeEstado.setText(" ");
        lblFechaCaducidad.setText(" ");
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

    public JLabel getLblFechaCaducidad() {
        return lblFechaCaducidad;
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
