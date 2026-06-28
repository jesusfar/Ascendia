package com.ascendia.servicio;

import com.ascendia.modelo.Oportunidad;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Persistencia COMPLEMENTARIA en archivos de texto plano (java.nio).
 * Genera un CSV de las oportunidades activas y permite releer el archivo
 * para verificar los datos persistidos. Maneja IOException de forma
 * controlada, de modo independiente a la capa MySQL (RNF10).
 *
 * Demuestra el uso de BufferedWriter / BufferedReader con try-with-resources
 * y la API java.nio.file.Files para operaciones de E/S (RF14).
 */
public class ExportadorReportes {

    private static final DateTimeFormatter TS =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    /**
     * Exporta las oportunidades activas a un archivo CSV con nombre unico
     * basado en la fecha y hora de generacion.
     *
     * @param activas lista de oportunidades a exportar
     * @return ruta absoluta del archivo generado
     * @throws IOException si no se puede escribir en el sistema de archivos
     */
    public static String exportarActivasCSV(List<Oportunidad> activas) throws IOException {
        Path dir = Paths.get("reportes");
        Files.createDirectories(dir);
        Path archivo = dir.resolve(
                "ascendia_activas_" + LocalDateTime.now().format(TS) + ".csv");

        try (BufferedWriter bw = Files.newBufferedWriter(archivo)) {
            bw.write("id;titulo;tipo;fecha_limite;dias_restantes;estado");
            bw.newLine();
            for (Oportunidad o : activas) {
                bw.write(o.getId() + ";"
                        + o.getTitulo() + ";"
                        + (o.getTipo() != null ? o.getTipo().name() : "") + ";"
                        + o.getFechaLimite() + ";"
                        + o.diasRestantes() + ";"
                        + o.getEstado());
                bw.newLine();
            }
        }
        return archivo.toString();
    }

    /**
     * Lee el archivo generado y cuenta las filas de datos (excluye cabecera).
     * Permite evidenciar que la lectura recupera los mismos registros escritos.
     *
     * @param ruta ruta del CSV a leer
     * @return cantidad de registros de datos (sin contar la cabecera)
     * @throws IOException si el archivo no existe o no se puede leer
     */
    public static int contarRegistros(String ruta) throws IOException {
        int filas = 0;
        try (BufferedReader br = Files.newBufferedReader(Paths.get(ruta))) {
            br.readLine();                          // saltar cabecera
            while (br.readLine() != null) filas++;
        }
        return filas;
    }
}
