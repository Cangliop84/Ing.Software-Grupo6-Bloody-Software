import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.bson.types.ObjectId;
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

        assertDoesNotThrow(() -> service.reservarMuestrasEnMongo(tresUnidadesSeleccionadas, "HOSP-01", false));

        var idsReservados = tresUnidadesSeleccionadas.stream()
                .map(Donacion::getId)
                .collect(Collectors.toSet());

        List<Donacion> stockGeneralInicial = List.of(u1, u2, u3, u4);
        List<Donacion> stockDisponible = stockGeneralInicial.stream()
                .filter(d -> !idsReservados.contains(d.getId()))
                .toList();

        assertEquals(3, tresUnidadesSeleccionadas.size());
        assertEquals(1, stockDisponible.size());
        assertEquals("id-4", stockDisponible.get(0).getId());
        assertFalse(stockDisponible.stream().anyMatch(d -> idsReservados.contains(d.getId())));
    }

    @Test
    void test2_dadasUnidadesReservadasExitosamente_enBaseDeDatosIncluyeIdHospital() {
        String uri = System.getenv("MONGODB_URI");
        if (uri == null || uri.isBlank()) {
            uri = System.getProperty("mongodb.uri");
        }
        assertNotNull(uri, "Configura MONGODB_URI o -Dmongodb.uri para ejecutar el Test 2 contra la base de datos.");

        String idHospitalEsperado = "HOSP-TEST-6074";
        SolicitudEntregaService service = new SolicitudEntregaService();
        ObjectId idInsertado = null;

        try (MongoClient client = MongoClients.create(uri)) {
            MongoCollection<Document> donaciones = client
                    .getDatabase("banco_sangre")
                    .getCollection("donaciones");

            Document docPrueba = new Document("nombreDonante", "Donante Test T06")
                    .append("volumenMl", 450)
                    .append("grupoSanguineo", "O+")
                    .append("fechaCaducidad", LocalDate.now().plusDays(10).toString());
            donaciones.insertOne(docPrueba);
            idInsertado = docPrueba.getObjectId("_id");

            Donacion unidad = new Donacion("Donante Test T06", 450, "O+", LocalDate.now().plusDays(10));
            unidad.setId(idInsertado.toHexString());

            service.reservarMuestrasEnMongo(List.of(unidad), idHospitalEsperado);

            Document actualizado = donaciones.find(new Document("_id", idInsertado)).first();
            assertNotNull(actualizado);
            assertEquals("Reservado", actualizado.getString("estado"));
            assertEquals(idHospitalEsperado, actualizado.getString("idHospital"));
        } finally {
            if (idInsertado != null) {
                try (MongoClient cleanupClient = MongoClients.create(uri)) {
                    cleanupClient.getDatabase("banco_sangre")
                            .getCollection("donaciones")
                            .deleteOne(new Document("_id", idInsertado));
                }
            }
        }
    }
}