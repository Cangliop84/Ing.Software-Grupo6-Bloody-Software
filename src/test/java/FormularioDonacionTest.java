import org.example.FormularioDonacion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class FormularioDonacionTest {

    private FormularioDonacion formulario;

    @BeforeEach
    public void setUp() {
        //instanciamos el formulario antes de cada test
        formulario = new FormularioDonacion();
    }

    @Test
    public void testRechazarFormularioVacio_Test1_T02() {
        // Test 1 (T02): Rechazar formulario vacío:
        // Dado el formulario cargado en pantalla, cuando presiono guardar sin llenar los campos,
        // entonces los campos se marcan en rojo pidiendo información obligatoria.
        formulario.getTxtNombreDonante().setText("");
        formulario.getTxtVolumenMl().setText("");

        formulario.getBtnGuardar().doClick();

        assertTrue(formulario.tieneErrorEnNombre(),
                "El campo de nombre del donante debe marcarse con borde rojo.");
        assertTrue(formulario.tieneErrorEnVolumen(),
                "El campo de volumen debe marcarse con borde rojo.");
        assertEquals("Información obligatoria", formulario.getLblErrorNombre().getText(),
                "Debe pedir información obligatoria en el campo nombre.");
        assertEquals("Información obligatoria", formulario.getLblErrorVolumen().getText(),
                "Debe pedir información obligatoria en el campo volumen.");
    }

    @Test
    public void testRechazarRegistroSinVolumen_CA2() {
        //simulación del CA2 de la US01:volumen vacio
        formulario.getTxtNombreDonante().setText("Donante Valido");
        formulario.getTxtVolumenMl().setText(""); //vacio 

        boolean esValido = formulario.validarCamposVacios();

        assertFalse(esValido, "El sistema debe rechazar la operación si el volumen está vacío.");
        assertTrue(formulario.tieneErrorEnVolumen(), "El campo de volumen debe marcarse con error visual (rojo).");
    }

    @Test
    public void testRechazarRegistroSinNombre() {
        formulario.getTxtNombreDonante().setText("");
        formulario.getTxtVolumenMl().setText("450");

        boolean esValido = formulario.validarCamposVacios();

        assertFalse(esValido, "El sistema debe rechazar la operación si el nombre está vacío.");
        assertTrue(formulario.tieneErrorEnNombre(), "El campo de nombre debe marcarse con error.");
    }

    @Test
    public void testRechazarVolumenNegativoOInvalido() {
        //caso de prueba para volumen negativo
        formulario.getTxtVolumenMl().setText("-100");
        boolean formatoValido = formulario.validarFormatoVolumen();
        assertFalse(formatoValido, "El sistema debe rechazar volúmenes negativos.");

        //caso de prueba para texto en vez de numeros
        formulario.getTxtVolumenMl().setText("letras");
        boolean formatoLetrasValido = formulario.validarFormatoVolumen();
        assertFalse(formatoLetrasValido, "El sistema debe rechazar texto no numérico en el volumen.");
    }

    @Test
    public void testLimpiarFormularioRestauraEstadoInicial() {
        formulario.getBtnGuardar().doClick();
        assertTrue(formulario.tieneErrorEnNombre());
        assertTrue(formulario.tieneErrorEnVolumen());

        formulario.getBtnLimpiar().doClick();

        assertFalse(formulario.tieneErrorEnNombre(),
                "Al limpiar el formulario se debe restaurar el borde original del nombre.");
        assertFalse(formulario.tieneErrorEnVolumen(),
                "Al limpiar el formulario se debe restaurar el borde original del volumen.");
    }
}
