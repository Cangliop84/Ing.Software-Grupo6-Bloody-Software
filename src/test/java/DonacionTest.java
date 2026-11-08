import org.example.Donacion;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class DonacionTest {

    // Tu test original intacto
    @Test
    public void testGuardarRegistroDonacion() {
        Donacion donacion = new Donacion("Donante Prueba Test", 450, LocalDate.now().plusDays(30));

        assertDoesNotThrow(() -> {
            System.out.println("Donación creada exitosamente: " + donacion.getNombreDonante());
        });
    }

    //test agregado para validar conexion a la base de datos (Tarea T01/T04)
    @Test
    public void testGuardarRegistroDonacionDB() {
        Donacion donacion = new Donacion("Tester Prueba Conexion", 450, LocalDate.now().plusDays(42));

        assertDoesNotThrow(() -> {
            donacion.guardarEnMongo();
        }, "La conexión e inserción en MongoDB no debería lanzar ninguna excepción.");
    }
}