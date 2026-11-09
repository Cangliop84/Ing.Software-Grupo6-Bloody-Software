package org.example;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FormularioDonacion formulario = new FormularioDonacion();
            formulario.setVisible(true);

            TablaInventario inventario = new TablaInventario();
            inventario.setVisible(true);
        });
    }
}
