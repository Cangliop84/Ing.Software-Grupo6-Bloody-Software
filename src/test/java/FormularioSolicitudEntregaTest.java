import org.example.FormularioSolicitudEntrega;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.*;

import static org.junit.jupiter.api.Assertions.*;

class FormularioSolicitudEntregaTest {

    private FormularioSolicitudEntrega formulario;

    @BeforeEach
    void setUp() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            formulario = new FormularioSolicitudEntrega();
            formulario.setVisible(true);
        });
    }

    @AfterEach
    void tearDown() throws Exception {
        if (formulario != null) {
            SwingUtilities.invokeAndWait(() -> formulario.dispose());
        }
    }

    @Test
    void test1_adaptacionResolucionEscritorioYMovilSinRomperTarjeta() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            formulario.setSize(720, 420);
            formulario.ajustarDisenoSegunAncho(720);
            formulario.validate();

            JPanel tarjeta = formulario.getPanelTarjeta();
            assertNotNull(tarjeta);
            assertTrue(tarjeta.getWidth() > 0 && tarjeta.getHeight() > 0);
            assertTrue(formulario.getCmbTipoSangre().isShowing());
            assertTrue(formulario.getTxtVolumenMl().isShowing());
            assertTrue(formulario.getWidth() >= tarjeta.getWidth());

            formulario.setSize(340, 360);
            formulario.ajustarDisenoSegunAncho(340);
            formulario.validate();

            assertTrue(tarjeta.getWidth() > 0 && tarjeta.getHeight() > 0);
            assertTrue(formulario.getCmbTipoSangre().isShowing());
            assertTrue(formulario.getTxtVolumenMl().isShowing());
            assertTrue(formulario.getWidth() >= tarjeta.getWidth());
        });
    }

    @Test
    void test2_botonSolicitarDeshabilitadoCuandoCamposEstanVacios() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            formulario.limpiarFormulario();
            JButton btnSolicitar = formulario.getBtnSolicitar();

            assertNotNull(btnSolicitar);
            assertFalse(btnSolicitar.isEnabled(),
                    "El botón 'Solicitar' debe mantenerse deshabilitado cuando los campos están vacíos");
        });
    }

    @Test
    void test3_limpiezaDeCamposEnInterfaz() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            //llenamos los datos en el formulario
            formulario.getCmbTipoSangre().setSelectedItem("A-");
            formulario.getTxtVolumenMl().setText("500");

            //forzamos la limpieza usando el método de la interfaz
            formulario.limpiarFormulario();

            //verificamos que los campos se hayan limpiado correctamente
            assertNull(formulario.getCmbTipoSangre().getSelectedItem(), 
                    "El combo de tipo de sangre debe quedar sin selección (null)");
                    
            assertEquals("", formulario.getTxtVolumenMl().getText(), 
                    "El campo de volumen debe estar vacío");
        });
    }
}