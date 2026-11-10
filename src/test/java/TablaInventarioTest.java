import org.example.TablaInventario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import javax.swing.table.DefaultTableModel;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Pruebas unitarias para la vista del inventario de sangre (Tarea T08).
 */
public class TablaInventarioTest {

    private TablaInventario vistaInventario;

    @BeforeEach
    public void setUp() {
        vistaInventario = new TablaInventario();
    }

    /**
     * Test 1 (T08): Filtrar unidades O-.
     * Dado una tabla con muestras O+ y O-,
     * cuando selecciono el filtro O-, entonces las filas O+ se ocultan visualmente.
     */
    @Test
    public void testFiltrarUnidadesOMenos_T08() {
        DefaultTableModel modelo = vistaInventario.getModeloTabla();
        modelo.addRow(new Object[]{"Juan Pérez", "O+", "450", "2026-12-01"});
        modelo.addRow(new Object[]{"Ana Gómez", "O-", "450", "2026-12-05"});

        assertEquals(2, vistaInventario.getTablaStock().getRowCount(),
                "Inicialmente deberían ser visibles 2 filas en la tabla.");

        vistaInventario.getCmbFiltroGrupoSanguineo().setSelectedItem("O-");

        assertEquals(1, vistaInventario.getTablaStock().getRowCount(),
                "Solo debería ser visible 1 fila tras aplicar el filtro O-.");
        assertEquals("O-", vistaInventario.getTablaStock().getValueAt(0, 1),
                "El grupo sanguíneo de la fila visible debe ser O-.");
    }
}