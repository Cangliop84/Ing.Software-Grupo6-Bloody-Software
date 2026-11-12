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
    //agrego test para validar que  al seleccionar "Todos" en el ComboBox, el filtro se limpie y vuelva a mostrar la totalidad de los registros de la tabla
    @Test
    public void testRestablecerFiltroTodos() {
        javax.swing.table.DefaultTableModel modelo = vistaInventario.getModeloTabla();
        modelo.addRow(new Object[]{"Juan", "O+", "450", "2026-12-01"});
        modelo.addRow(new Object[]{"Ana", "A-", "450", "2026-12-05"});

        //aplicar el filtro primero
        vistaInventario.getCmbFiltroGrupoSanguineo().setSelectedItem("O+");
        org.junit.jupiter.api.Assertions.assertEquals(1, vistaInventario.getTablaStock().getRowCount(), "Debe mostrar 1 fila.");

        //vuelve a seleccionar "Todos"
        vistaInventario.getCmbFiltroGrupoSanguineo().setSelectedItem("Todos");
        org.junit.jupiter.api.Assertions.assertEquals(2, vistaInventario.getTablaStock().getRowCount(), 
                "Al seleccionar 'Todos', deben volver a ser visibles todas las filas.");
    }

    @Test
    public void testActualizarResumenStockSumaVolumenes() {
        javax.swing.table.DefaultTableModel modelo = vistaInventario.getModeloTabla();
        //agregar dos unidades de 450ml
        modelo.addRow(new Object[]{"Juan", "O+", "450", "2026-12-01"});
        modelo.addRow(new Object[]{"Ana", "O+", "450", "2026-12-05"});
        
        //forzar la actualización del resumen
        vistaInventario.actualizarResumenStock();
        
        String textoResumen = vistaInventario.getLblResumenStock().getText();
        org.junit.jupiter.api.Assertions.assertTrue(textoResumen.contains("900"), 
                "El resumen debe sumar correctamente los volúmenes (900 ml) de las filas visibles.");
    }
}