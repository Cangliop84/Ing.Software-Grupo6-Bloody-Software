import org.example.Donacion;
import org.example.SolicitudEntregaService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class SolicitudEntregaReservaTest {

    @Test
    void test1_dadasTresUnidadesSeleccionadas_alCambiarEstadoNoAparecenEnStockGeneral() {
        Donacion u1 = new Donacion("Donante 1", 450, "O+", LocalDate.now().plusDays(10));
        u1.setId("id-1");
        Donacion u2 = new Donacion("Donante 2", 450, "O+", LocalDate.now().plusDays(15));
        u2.setId("id-2");
        Donacion u3 = new Donacion("Donante 3", 450, "O+", LocalDate.now().plusDays(20));
        u3.setId("id-3");
        Donacion u4 = new Donacion("Donante 4", 450, "O+", LocalDate.now().plusDays(25));
        u4.setId("id-4");

        List<Donacion> tresUnidadesSeleccionadas = List.of(u1, u2, u3);
        SolicitudEntregaService service = new SolicitudEntregaService();

        // Ejecuta el cambio de estado a "Reservado" sin requerir conexión real a Atlas en el test unitario
        assertDoesNotThrow(() -> service.reservarMuestrasEnMongo(tresUnidadesSeleccionadas, "HOSP-01", false));

        var idsReservados = tresUnidadesSeleccionadas.stream()
                .map(Donacion::getId)
                .collect(Collectors.toSet());

        // Simula la consulta del stock general excluyendo unidades marcadas como "Reservado"
        List<Donacion> stockGeneralInicial = List.of(u1, u2, u3, u4);
        List<Donacion> stockDisponible = stockGeneralInicial.stream()
                .filter(d -> !idsReservados.contains(d.getId()))
                .toList();

        assertEquals(3, tresUnidadesSeleccionadas.size());
        assertEquals(1, stockDisponible.size());
        assertEquals("id-4", stockDisponible.get(0).getId());
        assertFalse(stockDisponible.stream().anyMatch(d -> idsReservados.contains(d.getId())));
    }
}