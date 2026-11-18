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

        // Pendiente en Avance 2: validación numérica estricta de volumen y persistencia en MongoDB
        int volumen = Integer.parseInt(volumenTexto.trim());
        SolicitudEntrega solicitud = new SolicitudEntrega(tipoNormalizado, volumen);

        return new ResultadoOperacion(true, 201, "Solicitud de entrega generada correctamente.", solicitud);
    }
}
