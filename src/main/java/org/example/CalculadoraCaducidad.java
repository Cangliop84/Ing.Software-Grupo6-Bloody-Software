package org.example;

import java.time.LocalDate;

/**
 * Clase encargada de hacer el cálculo de cuantos días útiles le quedan a la sangre del donante.
 */
public class CalculadoraCaducidad {

    /// Días de vida util estándar de una unidad de sangre
    public static final int DIAS_VIDA_UTIL_SANGRE = 42;

    /**
     * Calcula la fecha de vencimiento sumando los días de vida útil a la fecha dada.
     *
     * @param fechaDonacion Fecha de extracción de la donación.
     * @return Fecha límite de caducidad.
     */
    public static LocalDate calcularFechaCaducidad(LocalDate fechaDonacion) {
        if (fechaDonacion == null) {
            throw new IllegalArgumentException("La fecha de donación no puede ser nula");
        }
        return fechaDonacion.plusDays(DIAS_VIDA_UTIL_SANGRE);
    }
}