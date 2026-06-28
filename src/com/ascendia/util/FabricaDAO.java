package com.ascendia.util;

import com.ascendia.dao.IOportunidadDAO;
import com.ascendia.dao.IUsuarioDAO;
import com.ascendia.dao.jdbc.OportunidadDAOJDBC;
import com.ascendia.dao.jdbc.UsuarioDAOJDBC;
import com.ascendia.dao.memoria.OportunidadDAOMemoria;
import com.ascendia.dao.memoria.UsuarioDAOMemoria;

/**
 * Fabrica de DAOs (patron Factory). Decide en tiempo de ejecucion que
 * implementacion concreta entregar, devolviendo siempre el tipo INTERFAZ.
 *
 * Este es el corazon del POLIMORFISMO del prototipo: el resto del sistema
 * trabaja contra IOportunidadDAO / IUsuarioDAO sin saber si por detras hay
 * MySQL (JDBC) o el repositorio en memoria.
 */
public class FabricaDAO {

    public enum Modo { MEMORIA, JDBC }

    private static Modo modo = Modo.MEMORIA;

    public static void setModo(Modo nuevo) { modo = nuevo; }
    public static Modo getModo() { return modo; }

    public static IOportunidadDAO getOportunidadDAO() {
        return (modo == Modo.JDBC) ? new OportunidadDAOJDBC() : new OportunidadDAOMemoria();
    }

    public static IUsuarioDAO getUsuarioDAO() {
        return (modo == Modo.JDBC) ? new UsuarioDAOJDBC() : new UsuarioDAOMemoria();
    }
}
