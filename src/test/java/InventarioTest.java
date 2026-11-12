import org.example.Inventario;
import org.example.Donacion;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;


public class InventarioTest {
    @Test
    public void testObtenerInventarioDB() {
        Inventario inventario = new Inventario();
        
        assertDoesNotThrow(() -> {
            List<Donacion> lista = inventario.obtenerInventario();
            assertNotNull(lista, "La lista de inventario no debe ser nula.");
        }, "La conexión a MongoDB para obtener el inventario no debe fallar.");
    }


    @Test
    public void testObtenerInventario() {
        Inventario inventarioDao = new Inventario();
        
        // Ejecutamos el método que consulta la base de datos
        List<Donacion> listaInventario = inventarioDao.obtenerInventario();

        // Validamos que la lista no sea nula (la conexión y consulta respondieron)
        assertNotNull(listaInventario, "El inventario no debería ser nulo.");
        
        // Mensaje en consola para verificar los registros obtenidos
        System.out.println("Registros encontrados en el inventario: " + listaInventario.size());
        for (Donacion d : listaInventario) {
            System.out.println("- Donante: " + d.getNombreDonante() + " | Grupo: " + d.getGrupoSanguineo() + " | Volumen: " + d.getVolumenMl() + " ml");
        }
    }
}