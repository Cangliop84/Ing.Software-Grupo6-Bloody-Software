package org.example;

import javax.swing.*;
import java.awt.*;

/**
 * Modal de confirmación previo al despacho de una solicitud de entrega (Sprint 02 - T11).
 * Solicita confirmación explícita del personal antes de enviar la petición HTTP al backend.
 */
public class ModalConfirmacionDespacho extends JDialog {
    private final JLabel lblMensajeConfirmacion;
    private final JButton btnConfirmar;
    private final JButton btnCancelar;
    private boolean confirmado = false;
    private Runnable onConfirmarCallback;

    public ModalConfirmacionDespacho(Frame owner, String idSolicitud, String tipoSangre, int volumenMl) {
        this(owner, idSolicitud, tipoSangre, volumenMl, null);
    }

    public ModalConfirmacionDespacho(
            Frame owner,
            String idSolicitud,
            String tipoSangre,
            int volumenMl,
            Runnable onConfirmarCallback
    ) {
        super(owner, "Confirmar Despacho de Solicitud", true);
        this.onConfirmarCallback = onConfirmarCallback;

        setSize(460, 190);
        setMinimumSize(new Dimension(380, 160));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(owner);

        JPanel raiz = new JPanel(new BorderLayout(12, 12));
        raiz.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

        JLabel lblTitulo = new JLabel("¿Confirmar despacho permanente de unidades?");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblTitulo.setForeground(new Color(160, 30, 30));
        raiz.add(lblTitulo, BorderLayout.NORTH);

        String detalleHtml = String.format(
                "<html>Solicitud <b>%s</b> — Grupo: <b>%s</b> | Volumen: <b>%d ml</b>.<br>"
                        + "Al confirmar, las unidades se marcarán como 'Despachadas' y desaparecerán del inventario.</html>",
                idSolicitud, tipoSangre, volumenMl
        );
        lblMensajeConfirmacion = new JLabel(detalleHtml);
        lblMensajeConfirmacion.setFont(new Font("SansSerif", Font.PLAIN, 12));
        raiz.add(lblMensajeConfirmacion, BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnCancelar = new JButton("Cancelar");
        btnCancelar.setPreferredSize(new Dimension(100, 30));

        btnConfirmar = new JButton("Confirmar");
        btnConfirmar.setPreferredSize(new Dimension(115, 30));
        btnConfirmar.setBackground(new Color(180, 35, 35));
        btnConfirmar.setForeground(Color.WHITE);

        btnCancelar.addActionListener(e -> cancelar());
        btnConfirmar.addActionListener(e -> confirmar());

        panelBotones.add(btnCancelar);
        panelBotones.add(btnConfirmar);
        raiz.add(panelBotones, BorderLayout.SOUTH);

        add(raiz);
    }

    public void confirmar() {
        this.confirmado = true;
        dispose();
        if (onConfirmarCallback != null) {
            onConfirmarCallback.run();
        }
    }

    public void cancelar() {
        this.confirmado = false;
        dispose();
    }

    public void setOnConfirmarCallback(Runnable onConfirmarCallback) {
        this.onConfirmarCallback = onConfirmarCallback;
    }

    public boolean isConfirmado() {
        return confirmado;
    }

    public JLabel getLblMensajeConfirmacion() {
        return lblMensajeConfirmacion;
    }

    public JButton getBtnConfirmar() {
        return btnConfirmar;
    }

    public JButton getBtnCancelar() {
        return btnCancelar;
    }
}
