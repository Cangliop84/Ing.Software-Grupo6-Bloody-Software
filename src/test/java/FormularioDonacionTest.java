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
}