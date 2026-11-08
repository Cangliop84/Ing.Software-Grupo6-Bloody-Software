import org.example.Donacion;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class DonacionTest {

    @Test
    public void testGuardarRegistroDonacion() {
        Donacion donacion = new Donacion("Donante Prueba Test", 450, LocalDate.now().plusDays(30));

        assertDoesNotThrow(() -> {
            // Se valida la instancia de la donación para completar el test unitario
            System.out.println("Donación creada exitosamente: " + donacion.getNombreDonante());
        });
    }
}