package org.example;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio encargado de validar y generar solicitudes de entrega de sangre (Sprint 02 - T01).
 */
public class SolicitudEntregaService {

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

    /**
     * Busca en la base de datos las muestras del grupo sanguíneo solicitado, las ordena
     * de manera ascendente por fecha de caducidad (FIFO) y acumula volúmenes hasta cubrir la solicitud.
     * @param grupoSanguineo Grupo sanguíneo requerido.
     * @param volumenSolicitadoMl Volumen total requerido en mililitros.
     * @return Arreglo con los ID de las muestras seleccionadas ordenadas de menor a mayor fecha de caducidad.
     */
    public String[] seleccionarMuestrasFifo(String grupoSanguineo, int volumenSolicitadoMl) {
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

        List<String> idsSeleccionados = new ArrayList<>();
        int volumenAcumulado = 0;

        for (Donacion muestra : muestrasOrdenadas) {
            idsSeleccionados.add(muestra.getId());
            volumenAcumulado += muestra.getVolumenMl();
            if (volumenAcumulado >= volumenSolicitadoMl) {
                break;
            }
        }

        if (volumenAcumulado < volumenSolicitadoMl) {
            throw new IllegalStateException("Stock insuficiente para el grupo sanguíneo " + grupoBuscado);
        }

        return idsSeleccionados.toArray(new String[0]);
    }
}
