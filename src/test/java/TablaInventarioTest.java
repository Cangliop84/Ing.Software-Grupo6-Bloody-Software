import org.example.TablaInventario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import javax.swing.table.DefaultTableModel;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas unitarias y de aceptación para la vista del inventario de sangre (Tareas T07 y T08).
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

    /**
     * Test 1 (T07): Diseño responsivo.
     * Dado la vista construida, cuando abro la ventana en diferentes resoluciones,
     * entonces la tabla se adapta sin romper la estructura.
     */
    @Test
    public void testDisenoResponsivoDiferentesResoluciones_T07() {
        vistaInventario.agregarFilaInventario("Carlos Ruiz", "A+", 450, "2026-12-10");
        vistaInventario.agregarFilaInventario("Lucía Fernández", "O-", 400, "2026-12-15");

        // Resolución compacta (< 620px): activa modo compacto del panel superior sin desbordar
        vistaInventario.cambiarResolucion(500, 360);
        assertTrue(vistaInventario.isModoCompactoActivo(),
                "En resolución compacta (500x360) debe activarse el modo compacto del panel superior.");
        assertTrue(vistaInventario.esEstructuraResponsivaValida(),
                "La tabla debe adaptarse sin romper su estructura en 500x360.");

        // Resoluciones estándar y panorámica
        int[][] resoluciones = {
                {800, 600},
                {1366, 768},
                {1920, 1080}
        };

        for (int[] resolucion : resoluciones) {
            vistaInventario.cambiarResolucion(resolucion[0], resolucion[1]);
            assertFalse(vistaInventario.isModoCompactoActivo(),
                    "En resolución " + resolucion[0] + "x" + resolucion[1] + " debe usarse el layout horizontal normal.");
            assertTrue(vistaInventario.esEstructuraResponsivaValida(),
                    "La tabla debe mantener su estructura válida en " + resolucion[0] + "x" + resolucion[1] + ".");
        }
    }

    /**
     * Verifica la inserción y limpieza de filas en la vista mediante sus utilidades (T07).
     */
    @Test
    public void testAgregarYLimpiarFilasInventario_T07() {
        vistaInventario.agregarFilaInventario("Marta Díaz", "B+", 450, "2026-12-20");
        assertEquals(1, vistaInventario.getTablaStock().getRowCount(),
                "Debe existir 1 fila tras agregar una unidad.");

        vistaInventario.limpiarTabla();
        assertEquals(0, vistaInventario.getTablaStock().getRowCount(),
                "La tabla debe quedar vacía tras ejecutar limpiarTabla().");
        assertTrue(vistaInventario.getLblResumenStock().getText().contains("Unidades en vista: 0"),
                "El resumen debe reflejar 0 unidades visibles tras limpiar la tabla.");
    }
    /**
     * Dado el inventario cargado, cuando cambio el filtro tres veces seguidas,
     * entonces los datos mostrados corresponden a la última selección sin duplicarse.
     */
    @Test //cambios de filtros sucesivos 
    public void testCambiosDeFiltroSucesivos_T09() {
        vistaInventario.agregarFilaInventario("Juan", "A+", 450, "2026-12-01");
        vistaInventario.agregarFilaInventario("Pedro", "O-", 450, "2026-12-01");
        vistaInventario.agregarFilaInventario("Maria", "A+", 450, "2026-12-01");
        vistaInventario.agregarFilaInventario("Ana", "B-", 450, "2026-12-01");

        //cambios sucesivos simulando al usuario real
        vistaInventario.getCmbFiltroGrupoSanguineo().setSelectedItem("O-");
        vistaInventario.getCmbFiltroGrupoSanguineo().setSelectedItem("B-");
        vistaInventario.getCmbFiltroGrupoSanguineo().setSelectedItem("A+"); //ultima seleccion

        int filasVisibles = vistaInventario.getTablaStock().getRowCount();
        assertEquals(2, filasVisibles, "Deberían verse solo 2 filas para el grupo A+ tras múltiples filtrados");
        assertEquals("A+", vistaInventario.getTablaStock().getValueAt(0, 1));
        assertEquals("A+", vistaInventario.getTablaStock().getValueAt(1, 1));
    }
}