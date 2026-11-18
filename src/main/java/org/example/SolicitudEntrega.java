package org.example;

import java.time.LocalDateTime;
import java.util.Set;

public class SolicitudEntrega {
    public static final Set<String> TIPOS_SANGUINEOS_VALIDOS = Set.of(
            "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"
    );

    private String tipoSangre;
    private int volumenRequeridoMl;
    private LocalDateTime fechaSolicitud;
    private String estado;

    public SolicitudEntrega(String tipoSangre, int volumenRequeridoMl) {
        this.tipoSangre = tipoSangre;
        this.volumenRequeridoMl = volumenRequeridoMl;
        this.fechaSolicitud = LocalDateTime.now();
        this.estado = "PENDIENTE";
    }

    public SolicitudEntrega(String tipoSangre, int volumenRequeridoMl, LocalDateTime fechaSolicitud, String estado) {
        this.tipoSangre = tipoSangre;
        this.volumenRequeridoMl = volumenRequeridoMl;
        this.fechaSolicitud = fechaSolicitud;
        this.estado = estado;
    }

    public String getTipoSangre() {
        return tipoSangre;
    }

    public int getVolumenRequeridoMl() {
        return volumenRequeridoMl;
    }

    public LocalDateTime getFechaSolicitud() {
        return fechaSolicitud;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
