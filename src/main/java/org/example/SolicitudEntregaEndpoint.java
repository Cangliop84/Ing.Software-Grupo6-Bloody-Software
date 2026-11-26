package org.example;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.bson.Document;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * Controlador HTTP que expone el endpoint POST /api/solicitudes para recibir
 * solicitudes de entrega en formato JSON (Sprint 02 - T01).
 */
public class SolicitudEntregaEndpoint {
    public static final String RUTA_SOLICITUDES = "/api/solicitudes";
    public static final String RUTA_DESPACHO = "/api/solicitudes/despachar";

    private final SolicitudEntregaService service;
    private final boolean persistirEnBd;
    private HttpServer server;

    public SolicitudEntregaEndpoint() {
        this(new SolicitudEntregaService(), true);
    }

    public SolicitudEntregaEndpoint(SolicitudEntregaService service, boolean persistirEnBd) {
        this.service = service;
        this.persistirEnBd = persistirEnBd;
    }

    /**
     * Inicia el servidor HTTP en el puerto indicado escuchando en {@value #RUTA_SOLICITUDES}.
     *
     * @param puerto Puerto TCP de escucha (0 para asignar uno disponible automáticamente).
     * @throws IOException Si ocurre un error al enlazar el puerto.
     */
    public void iniciar(int puerto) throws IOException {
        server = HttpServer.create(new InetSocketAddress(puerto), 0);
        server.createContext(RUTA_SOLICITUDES, this::manejarSolicitudHttp);
        server.createContext(RUTA_DESPACHO, this::manejarDespachoHttp);
        server.setExecutor(null);
        server.start();
    }

    public void detener() {
        if (server != null) {
            server.stop(0);
        }
    }

    public int getPuerto() {
        return server != null ? server.getAddress().getPort() : -1;
    }

    private void manejarSolicitudHttp(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            String jsonError = new Document("exito", false)
                    .append("mensaje", "Método HTTP no permitido. Use POST.")
                    .toJson();
            enviarRespuestaJson(exchange, 405, jsonError);
            return;
        }

        String cuerpoRequest;
        try (InputStream is = exchange.getRequestBody()) {
            cuerpoRequest = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }

        SolicitudEntregaService.ResultadoOperacion resultado = procesarPost(cuerpoRequest);
        String jsonRespuesta = construirJsonRespuesta(resultado);
        enviarRespuestaJson(exchange, resultado.getCodigoHttp(), jsonRespuesta);
    }

    private void manejarDespachoHttp(HttpExchange exchange) throws IOException {
        String metodo = exchange.getRequestMethod();
        if (!"POST".equalsIgnoreCase(metodo) && !"PUT".equalsIgnoreCase(metodo)) {
            String jsonError = new Document("exito", false)
                    .append("mensaje", "Método HTTP no permitido. Use POST o PUT.")
                    .toJson();
            enviarRespuestaJson(exchange, 405, jsonError);
            return;
        }

        String cuerpoRequest;
        try (InputStream is = exchange.getRequestBody()) {
            cuerpoRequest = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }

        SolicitudEntregaService.ResultadoOperacion resultado = procesarDespacho(cuerpoRequest);
        String jsonRespuesta = construirJsonRespuesta(resultado);
        enviarRespuestaJson(exchange, resultado.getCodigoHttp(), jsonRespuesta);
    }

    /**
     * Procesa el cuerpo JSON de una petición de despacho hacia {@value #RUTA_DESPACHO}.
     *
     * @param jsonBody Cuerpo JSON con el identificador de solicitud ("idSolicitud").
     * @return Resultado de la operación con código HTTP (200, 400, 404, 409 o 500).
     */
    public SolicitudEntregaService.ResultadoOperacion procesarDespacho(String jsonBody) {
        if (jsonBody == null || jsonBody.trim().isEmpty()) {
            return new SolicitudEntregaService.ResultadoOperacion(
                    false, 400, "El cuerpo del request es nulo o está vacío.", null);
        }

        Document body;
        try {
            body = Document.parse(jsonBody);
        } catch (Exception e) {
            return new SolicitudEntregaService.ResultadoOperacion(
                    false, 400, "El request no tiene un formato JSON válido.", null);
        }

        Object rawId = body.containsKey("idSolicitud") ? body.get("idSolicitud") : body.get("id");
        String idSolicitud = rawId != null ? rawId.toString() : null;

        return service.despacharSolicitud(idSolicitud, persistirEnBd);
    }

    /**
     * Procesa el cuerpo JSON de una petición POST, validando formato no nulo/válido
     * y delegando la creación al servicio de solicitudes.
     *
     * @param jsonBody Cuerpo de la petición en formato JSON.
     * @return Resultado de la operación con código HTTP 201 o código de error.
     */
    public SolicitudEntregaService.ResultadoOperacion procesarPost(String jsonBody) {
        if (jsonBody == null || jsonBody.trim().isEmpty()) {
            return new SolicitudEntregaService.ResultadoOperacion(
                    false, 400, "El cuerpo del request es nulo o está vacío.", null);
        }

        Document body;
        try {
            body = Document.parse(jsonBody);
        } catch (Exception e) {
            return new SolicitudEntregaService.ResultadoOperacion(
                    false, 400, "El request no tiene un formato JSON válido.", null);
        }

        Object rawTipo = body.get("tipoSangre");
        Object rawVolumen = body.containsKey("volumenRequeridoMl")
                ? body.get("volumenRequeridoMl")
                : body.get("volumen");

        String tipoSangre = rawTipo != null ? rawTipo.toString() : null;
        String volumenTexto = rawVolumen != null ? rawVolumen.toString() : null;

        return service.generarSolicitud(tipoSangre, volumenTexto, persistirEnBd);
    }

    private String construirJsonRespuesta(SolicitudEntregaService.ResultadoOperacion resultado) {
        Document respuesta = new Document("exito", resultado.isExito())
                .append("mensaje", resultado.getMensaje());

        if (resultado.getSolicitud() != null) {
            respuesta.append("solicitud", resultado.getSolicitud().toDocument());
        }
        return respuesta.toJson();
    }

    private void enviarRespuestaJson(HttpExchange exchange, int codigoHttp, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(codigoHttp, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
