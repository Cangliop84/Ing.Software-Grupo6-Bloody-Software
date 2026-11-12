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
}