import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.example.Donacion;
import org.example.Inventario;
import org.junit.jupiter.api.Test;

public class InventarioTest {

    @Test
    public void testObtenerInventarioNoNulo() {
        Inventario inventario = new Inventario();
        List<Donacion> lista = assertDoesNotThrow(() -> inventario.obtenerInventario());
        assertNotNull(lista);
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