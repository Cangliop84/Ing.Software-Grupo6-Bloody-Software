package org.example;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio encargado de validar y generar solicitudes de entrega de sangre (Sprint 02 - T01).
 */
public class SolicitudEntregaService {

    private static final String MONGO_URI = "mongodb://rdominguez2024_db_user:Jose123@ac-15adclh-shard-00-00.ddnbvei.mongodb.net:27017,ac-15adclh-shard-00-01.ddnbvei.mongodb.net:27017,ac-15adclh-shard-00-02.ddnbvei.mongodb.net:27017/?ssl=true&replicaSet=atlas-t74n7s-shard-0&authSource=admin";
    private static final String DATABASE_NAME = "banco_sangre";
    private static final String COLLECTION_NAME = "solicitudes_entrega";

    public static class ResultadoOperacion {
        private final boolean exito;
        private final int codigoHttp;
        private final String mensaje;
        private final SolicitudEntrega solicitud;

        public ResultadoOperacion(boolean exito, int codigoHttp, String mensaje, SolicitudEntrega solicitud) {
            this.exito = exito;
            this.codigoHttp = codigoHttp;
            this.mensaje = mensaje;
            this.solicitud = solicitud;
        }

        public boolean isExito() {
            return exito;
        }

        public int getCodigoHttp() {
            return codigoHttp;
        }

        public String getMensaje() {
            return mensaje;
        }

        public SolicitudEntrega getSolicitud() {
            return solicitud;
        }
    }

    /**
     * Genera y persiste una solicitud de entrega validando tipo de sangre y volumen requerido.
     *
     * @param tipoSangre    Tipo sanguíneo solicitado (ej. "O+", "A-").
     * @param volumenTexto  Volumen requerido en mililitros expresado como texto.
     * @return Resultado de la operación con código HTTP asociado y detalle.
     */
    public ResultadoOperacion generarSolicitud(String tipoSangre, String volumenTexto) {
        return generarSolicitud(tipoSangre, volumenTexto, true);
    }

    /**
     * Valida los datos de entrada y genera una solicitud de entrega con persistencia opcional.
     *
     * @param tipoSangre    Tipo sanguíneo solicitado.
     * @param volumenTexto  Volumen requerido en mililitros.
     * @param persistirEnBd Indica si debe guardarse la solicitud en MongoDB Atlas.
     * @return Resultado de la operación con código HTTP (201, 400 o 500).
     */
    public ResultadoOperacion generarSolicitud(String tipoSangre, String volumenTexto, boolean persistirEnBd) {
        if (tipoSangre == null || tipoSangre.trim().isEmpty()) {
            return new ResultadoOperacion(false, 400, "El tipo de sangre es obligatorio y no puede estar vacío.", null);
        }

        if (volumenTexto == null || volumenTexto.trim().isEmpty()) {
            return new ResultadoOperacion(false, 400, "El volumen requerido es obligatorio y no puede estar vacío.", null);
        }

        String tipoNormalizado = tipoSangre.trim().toUpperCase();
        if (!SolicitudEntrega.TIPOS_SANGUINEOS_VALIDOS.contains(tipoNormalizado)) {
            return new ResultadoOperacion(false, 400, "Tipo de sangre inválido: " + tipoSangre, null);
        }

        int volumen;
        try {
            volumen = Integer.parseInt(volumenTexto.trim());
        } catch (NumberFormatException e) {
            return new ResultadoOperacion(false, 400, "El volumen requerido debe ser un número entero válido.", null);
        }

        if (volumen <= 0) {
            return new ResultadoOperacion(false, 400, "El volumen requerido debe ser mayor a 0 ml.", null);
        }

        SolicitudEntrega solicitud = new SolicitudEntrega(tipoNormalizado, volumen);

        if (persistirEnBd) {
            try {
                solicitud.guardarEnMongo();
            } catch (Exception e) {
                return new ResultadoOperacion(false, 500, "Error interno al guardar la solicitud: " + e.getMessage(), null);
            }
        }

        return new ResultadoOperacion(true, 201, "Solicitud de entrega generada correctamente.", solicitud);
    }

    public ResultadoOperacion despacharSolicitud(String idSolicitud) {
        return despacharSolicitud(idSolicitud, true);
    }

    public ResultadoOperacion despacharSolicitud(String idSolicitud, boolean persistirEnBd) {
        if (idSolicitud == null || idSolicitud.trim().isEmpty()) {
            return new ResultadoOperacion(false, 404, "Solicitud no encontrada: el ID es obligatorio.", null);
        }

        String idLimpio = idSolicitud.trim();
        if (!persistirEnBd) {
            SolicitudEntrega simulada = new SolicitudEntrega("O+", 450, LocalDateTime.now(), "Despachado");
            return new ResultadoOperacion(true, 200, "Solicitud actualizada a Despachado.", simulada);
        }

        try (MongoClient mongoClient = MongoClients.create(MONGO_URI)) {
            MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
            MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME);

            Bson filtro = ObjectId.isValid(idLimpio)
                    ? Filters.eq("_id", new ObjectId(idLimpio))
                    : Filters.eq("id", idLimpio);

            Document docActual = collection.find(filtro).first();
            if (docActual == null) {
                return new ResultadoOperacion(false, 404, "No existe una solicitud con el ID: " + idLimpio, null);
            }

            String estadoActual = docActual.getString("estado");
            if (!"Reservado".equalsIgnoreCase(estadoActual)) {
                return new ResultadoOperacion(
                        false,
                        409,
                        "La solicitud no está en estado 'Reservado' (estado actual: " + estadoActual + ").",
                        null
                );
            }

            collection.updateOne(filtro, Updates.set("estado", "Despachado"));
            Document docActualizado = collection.find(filtro).first();

            String tipoSangre = docActualizado != null && docActualizado.getString("tipoSangre") != null
                    ? docActualizado.getString("tipoSangre")
                    : docActual.getString("tipoSangre");
            Integer volumenObj = docActualizado != null && docActualizado.getInteger("volumenRequeridoMl") != null
                    ? docActualizado.getInteger("volumenRequeridoMl")
                    : docActual.getInteger("volumenRequeridoMl");
            int volumen = volumenObj != null ? volumenObj : 0;

            SolicitudEntrega solicitudActualizada = new SolicitudEntrega(
                    tipoSangre, volumen, LocalDateTime.now(), "Despachado"
            );

            return new ResultadoOperacion(true, 200, "Solicitud actualizada a Despachado.", solicitudActualizada);
        } catch (Exception e) {
            return new ResultadoOperacion(false, 500, "Error interno al despachar la solicitud: " + e.getMessage(), null);
        }
    }

    /**
     * Busca en la base de datos las muestras del grupo sanguíneo solicitado, las ordena
     * de manera ascendente por fecha de caducidad (FIFO) y acumula volúmenes hasta cubrir la solicitud.
     * @param grupoSanguineo Grupo sanguíneo requerido.
     * @param volumenSolicitadoMl Volumen total requerido en mililitros.
     * @return Arreglo con los ID de las muestras seleccionadas ordenadas de menor a mayor fecha de caducidad.
     */
    public String[] seleccionarMuestrasFifo(String grupoSanguineo, int volumenSolicitadoMl) {
        return seleccionarMuestrasDetalleFifo(grupoSanguineo, volumenSolicitadoMl).stream()
                .map(Donacion::getId)
                .toArray(String[]::new);
    }

    private static final String COLECCION_DONACIONES = "donaciones";

    /**
     * Actualiza en MongoDB las unidades seleccionadas a estado "Reservado" e incorpora
     * el ID del hospital solicitante en cada documento, evitando doble asignación.
     *
     * @param muestras   Unidades seleccionadas por el algoritmo de selección.
     * @param idHospital Identificador del hospital que solicita las unidades.
     */
    public void reservarMuestrasEnMongo(List<Donacion> muestras, String idHospital) {
        reservarMuestrasEnMongo(muestras, idHospital, true);
    }

    public void reservarMuestrasEnMongo(List<Donacion> muestras, String idHospital, boolean persistirEnBd) {
        if (muestras == null || muestras.isEmpty()) {
            throw new IllegalArgumentException("Debe indicar al menos una muestra para reservar.");
        }
        if (idHospital == null || idHospital.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del hospital es obligatorio.");
        }
        if (!persistirEnBd) {
            return;
        }

        String hospitalLimpio = idHospital.trim();

        try (MongoClient mongoClient = MongoClients.create(MONGO_URI)) {
            MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
            MongoCollection<Document> coleccionDonaciones = database.getCollection(COLECCION_DONACIONES);

            for (Donacion muestra : muestras) {
                if (muestra == null || muestra.getId() == null || muestra.getId().trim().isEmpty()) {
                    throw new IllegalArgumentException("Cada muestra debe tener un ID válido para ser reservada.");
                }

                String idMuestra = muestra.getId().trim();
                Bson filtroId = ObjectId.isValid(idMuestra)
                        ? Filters.eq("_id", new ObjectId(idMuestra))
                        : Filters.eq("id", idMuestra);

                // Evita doble asignación si ya fue reservada o despachada
                Bson filtroDisponible = Filters.and(
                        filtroId,
                        Filters.nin("estado", "Reservado", "Despachado")
                );

                Bson actualizacion = Updates.combine(
                        Updates.set("estado", "Reservado"),
                        Updates.set("idHospital", hospitalLimpio)
                );

                var resultado = coleccionDonaciones.updateOne(filtroDisponible, actualizacion);
                if (resultado.getMatchedCount() == 0) {
                    throw new IllegalStateException(
                            "No se pudo reservar la muestra " + idMuestra + " (no encontrada o no disponible)."
                    );
                }
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error al actualizar estado a Reservado en MongoDB: " + e.getMessage(), e);
        }
    }

    /**
     * Selecciona bajo criterio FIFO las muestras completas (con fechas y volúmenes) que cubren
     * el volumen solicitado para mostrar su detalle al usuario.
     *
     * @param grupoSanguineo      Grupo sanguíneo requerido.
     * @param volumenSolicitadoMl Volumen total requerido en mililitros.
     * @return Lista de donaciones seleccionadas ordenadas por fecha de caducidad ascendente.
     */
    public List<Donacion> seleccionarMuestrasDetalleFifo(String grupoSanguineo, int volumenSolicitadoMl) {
        if (grupoSanguineo == null || grupoSanguineo.trim().isEmpty()) {
            throw new IllegalArgumentException("El grupo sanguíneo es obligatorio.");
        }
        if (volumenSolicitadoMl <= 0) {
            throw new IllegalArgumentException("El volumen solicitado debe ser mayor a 0.");
        }

        String grupoBuscado = grupoSanguineo.trim();
        Inventario inventario = new Inventario();
        List<Donacion> stock = inventario.obtenerInventario();

        List<Donacion> muestrasOrdenadas = stock.stream()
                .filter(d -> d.getGrupoSanguineo() != null && d.getGrupoSanguineo().equalsIgnoreCase(grupoBuscado))
                .sorted(Comparator.comparing(Donacion::getFechaCaducidad))
                .toList();

        List<Donacion> seleccionadas = new ArrayList<>();
        int volumenAcumulado = 0;

        for (Donacion muestra : muestrasOrdenadas) {
            seleccionadas.add(muestra);
            volumenAcumulado += muestra.getVolumenMl();
            if (volumenAcumulado >= volumenSolicitadoMl) {
                break;
            }
        }

        if (volumenAcumulado < volumenSolicitadoMl) {
            throw new IllegalStateException("Stock insuficiente para el grupo sanguíneo " + grupoBuscado);
        }

        return seleccionadas;
    }
}