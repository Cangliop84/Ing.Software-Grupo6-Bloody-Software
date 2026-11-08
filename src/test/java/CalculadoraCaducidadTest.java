import org.example.CalculadoraCaducidad;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CalculadoraCaducidadTest {

    @Test
    public void testCalcularCaducidadCorrecta() {
        LocalDate fechaHoy = LocalDate.now();
        LocalDate fechaCaducidadCalculada = CalculadoraCaducidad.calcularFechaCaducidad(fechaHoy);
        LocalDate fechaEsperada = fechaHoy.plusDays(CalculadoraCaducidad.DIAS_VIDA_UTIL_SANGRE);

        assertEquals(fechaEsperada, fechaCaducidadCalculada,
                "La fecha de caducidad debe ser la fecha actual más los días de vida útil.");
    }
    @Test
    public void testCalcularCaducidadFechaNulaLanzaExcepcion() {
        //caso borde del backend exigido en T04
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            CalculadoraCaducidad.calcularFechaCaducidad(null);
        });

        assertEquals("La fecha de donación no puede ser nula", exception.getMessage(),
                "Debe lanzar una excepción informando que la fecha no puede ser nula.");
    }
}