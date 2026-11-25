import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.example.SolicitudEntregaEndpoint;
import org.example.SolicitudEntregaService;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias y de aceptación para la generación de solicitudes de entrega (Sprint 02 - T01).
 */
public class SolicitudEntregaTest {

    /**
     * Test 1 (T01): Dado un request con formato inválido o nulo, cuando se envía al endpoint post,
     * entonces retorna un error.
     */
    @Test
    public void testRequestFormatoInvalidoONuloRetornaError_Test1_T01() throws Exception {
        SolicitudEntregaEndpoint endpoint = new SolicitudEntregaEndpoint(new SolicitudEntregaService(), false);
        endpoint.iniciar(0);
        int puerto = endpoint.getPuerto();

        try {
            // 1. Request nulo / vacío en procesarPost
            SolicitudEntregaService.ResultadoOperacion resNulo = endpoint.procesarPost(null);
            assertFalse(resNulo.isExito(), "Un request nulo debe retornar error.");
            assertEquals(400, resNulo.getCodigoHttp(), "Un request nulo debe retornar HTTP 400.");

            // 2. Request con JSON malformado enviado al endpoint POST HTTP real
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest reqJsonInvalido = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + puerto + SolicitudEntregaEndpoint.RUTA_SOLICITUDES))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString("{tipoSangre: 'O+',"))
                    .build();

            HttpResponse<String> respJsonInvalido = client.send(reqJsonInvalido, HttpResponse.BodyHandlers.ofString());
            assertEquals(400, respJsonInvalido.statusCode(),
                    "Un JSON malformado enviado al POST debe retornar HTTP 400.");
            Document docErrorJson = Document.parse(respJsonInvalido.body());
            assertFalse(docErrorJson.getBoolean("exito"));

            // 3. Request vacío enviado al endpoint POST HTTP real
            HttpRequest reqVacio = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + puerto + SolicitudEntregaEndpoint.RUTA_SOLICITUDES))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(""))
                    .build();

            HttpResponse<String> respVacio = client.send(reqVacio, HttpResponse.BodyHandlers.ofString());
            assertEquals(400, respVacio.statusCode(),
                    "Un request vacío enviado al POST debe retornar HTTP 400.");

            // 4. Request con tipo de sangre inválido y volumen no numérico
            SolicitudEntregaService.ResultadoOperacion resTipoInvalido = endpoint.procesarPost(
                    "{\"tipoSangre\": \"Z+\", \"volumenRequeridoMl\": 450}");
            assertFalse(resTipoInvalido.isExito());
            assertEquals(400, resTipoInvalido.getCodigoHttp());

            SolicitudEntregaService.ResultadoOperacion resVolumenInvalido = endpoint.procesarPost(
                    "{\"tipoSangre\": \"O+\", \"volumenRequeridoMl\": \"no_numero\"}");
            assertFalse(resVolumenInvalido.isExito());
            assertEquals(400, resVolumenInvalido.getCodigoHttp());

            SolicitudEntregaService.ResultadoOperacion resVolumenNegativo = endpoint.procesarPost(
                    "{\"tipoSangre\": \"O+\", \"volumenRequeridoMl\": -200}");
            assertFalse(resVolumenNegativo.isExito());
            assertEquals(400, resVolumenNegativo.getCodigoHttp());
        } finally {
            endpoint.detener();
        }
    }

    /**
     * Valida el criterio de aceptación CA1 (US03): se genera la orden cuando tipo de sangre
     * y volumen requerido son válidos.
     */
    @Test
    public void testGenerarSolicitudValidaEndpointPost_CA1() throws Exception {
        SolicitudEntregaEndpoint endpoint = new SolicitudEntregaEndpoint(new SolicitudEntregaService(), false);
        endpoint.iniciar(0);
        int puerto = endpoint.getPuerto();

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest requestValido = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + puerto + SolicitudEntregaEndpoint.RUTA_SOLICITUDES))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString("{\"tipoSangre\": \"o+\", \"volumenRequeridoMl\": 450}"))
                    .build();

            HttpResponse<String> response = client.send(requestValido, HttpResponse.BodyHandlers.ofString());
            assertEquals(201, response.statusCode(),
                    "Una solicitud válida debe retornar HTTP 201.");

            Document body = Document.parse(response.body());
            assertTrue(body.getBoolean("exito"));
            Document solicitudDoc = body.get("solicitud", Document.class);
            assertNotNull(solicitudDoc);
            assertEquals("O+", solicitudDoc.getString("tipoSangre"));
            assertEquals(450, solicitudDoc.getInteger("volumenRequeridoMl"));
            assertEquals("PENDIENTE", solicitudDoc.getString("estado"));
        } finally {
            endpoint.detener();
        }
    }

    /**
     * Verifica que el endpoint rechace métodos HTTP distintos a POST con código 405.
     */
    @Test
    public void testMetodoDistintoAPostRetorna405() throws Exception {
        SolicitudEntregaEndpoint endpoint = new SolicitudEntregaEndpoint(new SolicitudEntregaService(), false);
        endpoint.iniciar(0);
        int puerto = endpoint.getPuerto();

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest getRequest = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + puerto + SolicitudEntregaEndpoint.RUTA_SOLICITUDES))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
            assertEquals(405, response.statusCode(),
                    "Una petición GET al endpoint debe retornar HTTP 405 Method Not Allowed.");
        } finally {
            endpoint.detener();
        }
    }

    /**
     * Test 1 (T05): Dado un stock con una unidad que vence en 2 días y otra en 10 días,
     * cuando se procesa la solicitud de 1 unidad, entonces el algoritmo retorna exclusivamente
     * el ID de la unidad de 2 días.
     */
    @Test
    public void testSeleccionarMuestrasFifoUnidadMasProximaAVencer_Test1_T05() {
        String uriMongo = "mongodb://rdominguez2024_db_user:Jose123@ac-15adclh-shard-00-00.ddnbvei.mongodb.net:27017,ac-15adclh-shard-00-01.ddnbvei.mongodb.net:27017,ac-15adclh-shard-00-02.ddnbvei.mongodb.net:27017/?ssl=true&replicaSet=atlas-t74n7s-shard-0&authSource=admin";
        String grupoPrueba = "AB-";
        int volumenUnidad = 450;

        ObjectId idVenceEn2Dias = new ObjectId();
        ObjectId idVenceEn10Dias = new ObjectId();

        try (MongoClient mongoClient = MongoClients.create(uriMongo)) {
            MongoDatabase database = mongoClient.getDatabase("banco_sangre");
            MongoCollection<Document> collection = database.getCollection("donaciones");

            Document muestra10Dias = new Document("_id", idVenceEn10Dias)
                    .append("nombreDonante", "Donante Test 10 Dias")
                    .append("volumenMl", volumenUnidad)
                    .append("grupoSanguineo", grupoPrueba)
                    .append("fechaCaducidad", LocalDate.now().plusDays(10).toString());

            Document muestra2Dias = new Document("_id", idVenceEn2Dias)
                    .append("nombreDonante", "Donante Test 2 Dias")
                    .append("volumenMl", volumenUnidad)
                    .append("grupoSanguineo", grupoPrueba)
                    .append("fechaCaducidad", LocalDate.now().plusDays(2).toString());

            collection.insertMany(List.of(muestra10Dias, muestra2Dias));

            SolicitudEntregaService service = new SolicitudEntregaService();
            String[] resultado = service.seleccionarMuestrasFifo(grupoPrueba, volumenUnidad);

            assertNotNull(resultado);
            assertEquals(1, resultado.length, "Debe retornar exactamente 1 muestra.");
            assertEquals(idVenceEn2Dias.toHexString(), resultado[0],
                    "Debe seleccionar exclusivamente la muestra que vence en 2 días.");
        } finally {
            try (MongoClient mongoClient = MongoClients.create(uriMongo)) {
                MongoDatabase database = mongoClient.getDatabase("banco_sangre");
                MongoCollection<Document> collection = database.getCollection("donaciones");
                collection.deleteMany(Filters.in("_id", idVenceEn2Dias, idVenceEn10Dias));
            }
        }
    }
}
