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
}