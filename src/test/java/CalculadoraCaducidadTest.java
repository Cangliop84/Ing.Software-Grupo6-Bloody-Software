import org.example.CalculadoraCaducidad;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CalculadoraCaducidadTest {

    @Test
    public void testCalcularCaducidadCorrecta() {
        LocalDate fechaHoy = LocalDate.now();
        LocalDate fechaCaducidadCalculada = CalculadoraCaducidad.calcularFechaCaducidad(fechaHoy);
        LocalDate fechaEsperada = fechaHoy.plusDays(CalculadoraCaducidad.DIAS_VIDA_UTIL_SANGRE);

        assertEquals(fechaEsperada, fechaCaducidadCalculada,
                "La fecha de caducidad debe ser la fecha actual más los días de vida útil.");
    }
}