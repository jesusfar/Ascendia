package com.ascendia.dao;

/**
 * Excepcion personalizada (checked) para errores de la capa de acceso a datos.
 * Permite encapsular y propagar de forma controlada las SQLException de JDBC,
 * cumpliendo el RNF10 (manejo controlado de excepciones de BD).
 */
public class DAOException extends Exception {

    public DAOException(String mensaje) {
        super(mensaje);
    }

    public DAOException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
