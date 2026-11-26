import org.example.Donacion;
import org.example.ModalConfirmacionReserva;
import org.example.TablaInventario;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias y de aceptación para el modal de confirmación de reserva (Sprint 02 - T07).
 */
public class ModalConfirmacionReservaTest {

    private TablaInventario vistaInventario;

    @BeforeEach
    void setUp() throws Exception {
        SwingUtilities.invokeAndWait(() -> vistaInventario = new TablaInventario());
    }

    @AfterEach
    void tearDown() throws Exception {
        if (vistaInventario != null) {
            SwingUtilities.invokeAndWait(() -> vistaInventario.dispose());
        }
    }

    /**
     * Test 1 (T07): Dada una asignación automática exitosa, cuando el backend responde con los detalles,
     * entonces se renderiza una tabla resumen con las muestras exactas reservadas.
     */
    @Test
    void testRenderizaTablaResumenConMuestrasExactas_Test1_T07() throws Exception {
        Donacion muestra1 = new Donacion("Ana Pérez", 450, "O+", LocalDate.of(2026, 12, 1));
        muestra1.setId("M-001");
        Donacion muestra2 = new Donacion("Luis Soto", 300, "O+", LocalDate.of(2026, 12, 5));
        muestra2.setId("M-002");

        List<Donacion> reservadas = List.of(muestra1, muestra2);

        SwingUtilities.invokeAndWait(() -> {
            ModalConfirmacionReserva modal = new ModalConfirmacionReserva(vistaInventario, reservadas);
            DefaultTableModel modelo = modal.getModeloTabla();

            assertEquals(2, modelo.getRowCount(),
                    "La tabla resumen debe renderizar exactamente las 2 muestras reservadas.");

            assertEquals("M-001", modelo.getValueAt(0, 0));
            assertEquals("Ana Pérez", modelo.getValueAt(0, 1));
            assertEquals("O+", modelo.getValueAt(0, 2));
            assertEquals(450, modelo.getValueAt(0, 3));
            assertEquals("2026-12-01", modelo.getValueAt(0, 4));

            assertEquals("M-002", modelo.getValueAt(1, 0));
            assertEquals("Luis Soto", modelo.getValueAt(1, 1));
            assertEquals("O+", modelo.getValueAt(1, 2));
            assertEquals(300, modelo.getValueAt(1, 3));
            assertEquals("2026-12-05", modelo.getValueAt(1, 4));

            assertTrue(modal.getLblResumenReserva().getText().contains("750 ml"),
                    "El resumen del modal debe indicar el volumen total reservado (750 ml).");

            modal.dispose();
        });
    }

    /**
     * Test 2 (T07): Dado el modal de confirmación abierto, cuando el usuario presiona "Cerrar",
     * la vista principal del inventario se actualiza reflejando el nuevo stock disponible.
     */
    @Test
    void testCerrarModalActualizaVistaPrincipalInventario_Test2_T07() throws Exception {
        Donacion donacion1 = new Donacion("Ana Pérez", 450, "O+", LocalDate.of(2026, 12, 1));
        donacion1.setId("M-001");
        Donacion donacion2 = new Donacion("Luis Soto", 300, "O+", LocalDate.of(2026, 12, 5));
        donacion2.setId("M-002");
        Donacion donacion3 = new Donacion("Carla Díaz", 450, "A+", LocalDate.of(2026, 12, 20));
        donacion3.setId("M-003");

        SwingUtilities.invokeAndWait(() -> {
            // Stock inicial visible en la tabla principal: 3 unidades (1200 ml)
            vistaInventario.reemplazarStockEnTabla(List.of(donacion1, donacion2, donacion3));
            assertEquals(3, vistaInventario.getTablaStock().getRowCount());

            // Se abre el modal con M-001 reservada y al cerrar se actualiza el inventario con el nuevo stock disponible
            List<Donacion> nuevoStockDisponible = List.of(donacion2, donacion3);
            AtomicBoolean actualizacionEjecutada = new AtomicBoolean(false);

            ModalConfirmacionReserva modal = new ModalConfirmacionReserva(
                    vistaInventario,
                    List.of(donacion1),
                    () -> {
                        vistaInventario.reemplazarStockEnTabla(nuevoStockDisponible);
                        actualizacionEjecutada.set(true);
                    }
            );

            // Simulamos que el usuario presiona el botón "Cerrar" estando abierto
            modal.getBtnCerrar().doClick();

            assertTrue(actualizacionEjecutada.get(),
                    "Al presionar Cerrar debe ejecutarse la actualización de la vista principal.");
            assertFalse(modal.isDisplayable(),
                    "El modal debe cerrarse tras presionar el botón Cerrar.");
            assertEquals(2, vistaInventario.getTablaStock().getRowCount(),
                    "La vista principal debe reflejar solo las 2 unidades restantes en stock.");
            assertTrue(vistaInventario.getLblResumenStock().getText().contains("750 ml"),
                    "El resumen de stock principal debe actualizarse a 750 ml disponibles.");
        });
    }
}
