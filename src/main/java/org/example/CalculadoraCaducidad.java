package org.example;

import java.time.LocalDate;

public class CalculadoraCaducidad {

    /// Dias de vida util estandar de una unidad de sangre
    public static final int DIAS_VIDA_UTIL_SANGRE = 42;

    public static LocalDate calcularFechaCaducidad(LocalDate fechaDonacion) {
        if (fechaDonacion == null) {
            throw new IllegalArgumentException("La fecha de donación no puede ser nula");
        }
        return fechaDonacion.plusDays(DIAS_VIDA_UTIL_SANGRE);
    }
}