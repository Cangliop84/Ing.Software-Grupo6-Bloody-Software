import org.example.CalculadoraCaducidad;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test 1 (T03):
 * Pruebas unitarias para la calculadora de fechas de caducidad.
 */
public class CalculadoraCaducidadTest {

    /**
     * Valida que la fecha de caducidad se calcule correctamente sumando los días de vida útil.
     */
    @Test
    public void testCalcularCaducidadCorrecta() {
        LocalDate fechaHoy = LocalDate.now();
        LocalDate fechaCaducidadCalculada = CalculadoraCaducidad.calcularFechaCaducidad(fechaHoy);
        LocalDate fechaEsperada = fechaHoy.plusDays(CalculadoraCaducidad.DIAS_VIDA_UTIL_SANGRE);

        assertEquals(fechaEsperada, fechaCaducidadCalculada,
                "La fecha de caducidad debe ser la fecha actual más los días de vida útil.");
    }
    /**
     * Valida que se lance una excepción si la fecha de donación es nula.
     */
    @Test
    public void testCalcularCaducidadFechaNulaLanzaExcepcion() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            CalculadoraCaducidad.calcularFechaCaducidad(null);
        });

        assertEquals("La fecha de donación no puede ser nula", exception.getMessage(),
                "Debe lanzar una excepción informando que la fecha no puede ser nula.");
    }
}