import org.bson.Document;
import org.example.SolicitudEntregaEndpoint;
import org.example.SolicitudEntregaService;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

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
}
