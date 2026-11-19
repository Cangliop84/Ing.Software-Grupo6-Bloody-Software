package org.example;

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

    public ResultadoOperacion generarSolicitud(String tipoSangre, String volumenTexto) {
        return generarSolicitud(tipoSangre, volumenTexto, true);
    }

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
}
