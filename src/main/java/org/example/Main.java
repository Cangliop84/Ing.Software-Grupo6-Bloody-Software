package org.example;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FormularioDonacion formulario = new FormularioDonacion();
            formulario.setVisible(true);

            TablaInventario inventario = new TablaInventario();
            inventario.setVisible(true);

            FormularioSolicitudEntrega solicitud = new FormularioSolicitudEntrega(new SolicitudEntregaService(), inventario);
            solicitud.setVisible(true);

            VistaDespachoSolicitudes despacho = new VistaDespachoSolicitudes(
                    "http://localhost:8080" + SolicitudEntregaEndpoint.RUTA_DESPACHO,
                    inventario
            );
            despacho.setVisible(true);
        });
    }
}
