package com.ascendia;

import com.ascendia.util.FabricaDAO;
import com.ascendia.vista.MenuConsola;

/**
 * Punto de entrada del prototipo Ascendia.
 *
 * Sin argumentos: pregunta al usuario la fuente de datos (MySQL o memoria).
 * Con argumento "jdbc" o "memoria": selecciona directamente sin preguntar.
 * Si MySQL no esta disponible, cae automaticamente a memoria sin interrumpir.
 *
 * Uso:
 *   java -cp "bin;lib/mysql-connector-j.jar" com.ascendia.Main          (interactivo)
 *   java -cp "bin;lib/mysql-connector-j.jar" com.ascendia.Main jdbc     (fuerza MySQL)
 *   java -cp "bin;lib/mysql-connector-j.jar" com.ascendia.Main memoria  (fuerza memoria)
 */
public class Main {

    public static void main(String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("jdbc")) {
            FabricaDAO.setModo(FabricaDAO.Modo.JDBC);
        } else if (args.length > 0 && args[0].equalsIgnoreCase("memoria")) {
            FabricaDAO.setModo(FabricaDAO.Modo.MEMORIA);
        } else {
            // Seleccion interactiva: permite elegir el backend al iniciar.
            try (java.util.Scanner sc = new java.util.Scanner(System.in)) {
                System.out.println("Fuente de datos:  [1] MySQL (JDBC)   [2] Memoria");
                System.out.print("Seleccione (ENTER = memoria): ");
                String op = sc.nextLine().trim();
                FabricaDAO.setModo("1".equals(op) ? FabricaDAO.Modo.JDBC : FabricaDAO.Modo.MEMORIA);
            }
        }

        // Si se eligio JDBC, verificar la conexion antes de construir el menu.
        if (FabricaDAO.getModo() == FabricaDAO.Modo.JDBC) {
            try (var cn = com.ascendia.util.ConexionDB.getConexion()) {
                System.out.println(">> Conexion a MySQL 'ascendia' establecida.");
            } catch (Exception e) {
                System.out.println(">> No se pudo conectar a MySQL (" + e.getMessage() + ").");
                System.out.println(">> Se utilizara el repositorio en MEMORIA.");
                FabricaDAO.setModo(FabricaDAO.Modo.MEMORIA);
            }
        }

        new MenuConsola().iniciar();
    }
}
