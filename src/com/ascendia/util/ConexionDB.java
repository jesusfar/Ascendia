package com.ascendia.util;

import com.ascendia.dao.DAOException;
import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Utilitario de conexion JDBC a MySQL (esquema "ascendia" del TP2).
 * Centraliza la cadena de conexion y el cierre seguro de recursos.
 * Las credenciales por defecto corresponden a un entorno XAMPP local.
 */
public class ConexionDB {

    // Retrocompatible: si no hay variables de entorno (uso normal de la consola)
    // se mantienen exactamente los valores por defecto del entorno XAMPP local.
    // La vista web (RF11) las sobreescribe via DB_URL / DB_USER / DB_PASS.
    private static final String URL = System.getenv().getOrDefault("DB_URL",
        "jdbc:mysql://localhost:3306/ascendia?useSSL=false&serverTimezone=UTC");
    private static final String USER = System.getenv().getOrDefault("DB_USER", "root");
    private static final String PASS = System.getenv().getOrDefault("DB_PASS", "");

    public static Connection getConexion() throws DAOException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASS);
        } catch (Exception e) {
            // Encapsula cualquier fallo de conexion en la excepcion de dominio.
            throw new DAOException("No se pudo conectar a MySQL (ascendia).", e);
        }
    }

    /** Cierre seguro de cualquier recurso JDBC (varargs + AutoCloseable). */
    public static void cerrar(AutoCloseable... recursos) {
        for (AutoCloseable r : recursos) {
            if (r != null) {
                try {
                    r.close();
                } catch (Exception e) {
                    System.err.println("Aviso: error al cerrar recurso: " + e.getMessage());
                }
            }
        }
    }
}
