package com.ascendia.vista;

import com.ascendia.dao.DAOException;
import com.ascendia.dao.IOportunidadDAO;
import com.ascendia.dao.IUsuarioDAO;
import com.ascendia.modelo.*;
import com.ascendia.servicio.Buscador;
import com.ascendia.servicio.ExportadorReportes;
import com.ascendia.servicio.HistorialConsultas;
import com.ascendia.servicio.Ordenador;
import com.ascendia.util.FabricaDAO;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * VISTA del patron MVC: menu interactivo por consola. Concentra las
 * estructuras de control (switch, while, for), el manejo de excepciones de
 * entrada y la interaccion con las capas de servicio y de datos (DAO).
 *
 * Trabaja siempre contra las INTERFACES IOportunidadDAO / IUsuarioDAO, sin
 * conocer si la implementacion concreta es en memoria o JDBC (polimorfismo).
 */
public class MenuConsola {

    private final Scanner sc = new Scanner(System.in);
    private final IOportunidadDAO oportunidadDAO = FabricaDAO.getOportunidadDAO();
    private final IUsuarioDAO usuarioDAO = FabricaDAO.getUsuarioDAO();
    private final HistorialConsultas historial = new HistorialConsultas();

    // Opciones del menu almacenadas en un ARREGLO (recorrido con for-each).
    private static final String[] OPCIONES = {
        "1.  Listar oportunidades activas",
        "2.  Buscar por filtros (tipo / continente)",
        "3.  Buscar por palabra clave",
        "4.  Ver detalle de una oportunidad (por ID)",
        "5.  Ordenar oportunidades por fecha limite",
        "6.  Buscar por ID (busqueda binaria)",
        "7.  Top 3 mas proximas a vencer (arreglo + ArrayList)",
        "8.  Registrar nueva oportunidad (admin)",
        "9.  Modificar oportunidad (admin)",
        "10. Eliminar oportunidad - borrado logico (admin)",
        "11. Ver historial de consultas (pila)",
        "12. Sugerencias para un usuario",
        "13. Reporte estadistico: por tipo, pais y area",
        "14. Exportar reporte de activas a archivo CSV",
        "0.  Salir"
    };

    public void iniciar() {
        System.out.println("==============================================");
        System.out.println("   ASCENDIA - Prototipo operacional (consola)  ");
        System.out.println("   Fuente de datos: " + FabricaDAO.getModo());
        System.out.println("==============================================");

        boolean continuar = true;
        while (continuar) {                       // estructura repetitiva principal
            mostrarMenu();
            int opcion = leerEntero("Seleccione una opcion: ");
            try {
                switch (opcion) {                 // estructura de seleccion
                    case 1  -> listarActivas();
                    case 2  -> buscarPorFiltros();
                    case 3  -> buscarPorPalabra();
                    case 4  -> verDetalle();
                    case 5  -> ordenarPorFecha();
                    case 6  -> buscarBinaria();
                    case 7  -> topProximas();
                    case 8  -> registrarOportunidad();
                    case 9  -> modificarOportunidad();
                    case 10 -> eliminarOportunidad();
                    case 11 -> verHistorial();
                    case 12 -> sugerencias();
                    case 13 -> reporteEstadistico();
                    case 14 -> exportarReporte();
                    case 0  -> { continuar = false; System.out.println("\nGracias por usar Ascendia."); }
                    default -> System.out.println(">> Opcion invalida. Intente nuevamente.");
                }
            } catch (DAOException e) {
                // Manejo controlado de errores de la capa de datos (RNF10).
                System.out.println(">> Error de datos: " + e.getMessage());
            }
        }
    }

    private void mostrarMenu() {
        System.out.println("\n----------------- MENU -----------------");
        for (String op : OPCIONES) {              // recorrido del arreglo
            System.out.println("  " + op);
        }
    }

    // ---------------- Opciones ----------------

    private void listarActivas() throws DAOException {
        imprimirLista("OPORTUNIDADES ACTIVAS", oportunidadDAO.listarActivas());
    }

    private void buscarPorFiltros() throws DAOException {
        String tipoTxt = leerLinea("Tipo (beca/curso/intercambio/pasantia/voluntariado, ENTER=todos): ");
        TipoOportunidad tipo = TipoOportunidad.desdeTexto(tipoTxt);
        String continente = leerLinea("Continente (ENTER=todos): ");
        if (continente.isBlank()) continente = null;
        List<Oportunidad> res = oportunidadDAO.buscarConFiltros(tipo, continente, null);
        imprimirLista("RESULTADOS DEL FILTRO", res);
    }

    private void buscarPorPalabra() throws DAOException {
        String clave = leerLinea("Palabra clave en el titulo: ");
        List<Oportunidad> res = Buscador.porPalabraClave(oportunidadDAO.listarActivas(), clave);
        imprimirLista("RESULTADOS PARA '" + clave + "'", res);
    }

    private void verDetalle() throws DAOException {
        int id = leerEntero("ID de la oportunidad: ");
        Oportunidad o = oportunidadDAO.buscarPorId(id);
        if (o == null) {
            System.out.println(">> No se encontro la oportunidad id=" + id);
            return;
        }
        historial.registrar(o);                   // se apila la consulta (pila)
        System.out.println("\n===== DETALLE DE OPORTUNIDAD =====");
        System.out.println("Titulo     : " + o.getTitulo());
        System.out.println("Tipo       : " + (o.getTipo() != null ? o.getTipo().getEtiqueta() : "-"));
        System.out.println("Universidad: " + (o.getUniversidad() != null ? o.getUniversidad().descripcionCorta() : "-"));
        System.out.println("Area       : " + (o.getArea() != null ? o.getArea().getNombre() : "-"));
        System.out.println("Modalidad  : " + o.getModalidad());
        System.out.println("Idioma     : " + o.getIdioma());
        System.out.println("Fecha lim. : " + o.getFechaLimite() + " (dias restantes: " + o.diasRestantes() + ")");
        System.out.println("Estado     : " + o.getEstado() + " | Vigente: " + o.estaVigente());
    }

    private void ordenarPorFecha() throws DAOException {
        List<Oportunidad> lista = oportunidadDAO.listarActivas();
        Ordenador.ordenarPorFechaLimite(lista);   // selection sort
        imprimirLista("ACTIVAS ORDENADAS POR FECHA LIMITE", lista);
    }

    private void buscarBinaria() throws DAOException {
        int id = leerEntero("ID a buscar (busqueda binaria): ");
        List<Oportunidad> lista = oportunidadDAO.listarTodos();
        Ordenador.ordenarPorId(lista);            // precondicion: ordenado por id
        Oportunidad o = Buscador.binariaPorId(lista, id);
        System.out.println(o != null
            ? ">> Encontrada: " + o.descripcionCorta()
            : ">> No existe una oportunidad con id=" + id);
    }

    private void registrarOportunidad() throws DAOException {
        if (!FabricaDAO.getModo().equals(FabricaDAO.Modo.MEMORIA)
                && !FabricaDAO.getModo().equals(FabricaDAO.Modo.JDBC)) {
            return;
        }
        System.out.println("\n--- Alta de nueva oportunidad ---");
        String titulo = leerLinea("Titulo: ");
        TipoOportunidad tipo = TipoOportunidad.desdeTexto(leerLinea("Tipo: "));
        String fechaTxt = leerLinea("Fecha limite (AAAA-MM-DD): ");
        LocalDate fecha = parsearFecha(fechaTxt);

        Oportunidad nueva = new Oportunidad(titulo, tipo, null, null, fecha, Modalidad.VIRTUAL);
        try {
            oportunidadDAO.insertar(nueva);       // valida internamente
            System.out.println(">> Oportunidad registrada con id=" + nueva.getId());
        } catch (DAOException e) {
            System.out.println(">> No se pudo registrar: " + e.getMessage());
        }
    }

    private void verHistorial() {
        if (historial.estaVacio()) {
            System.out.println(">> El historial esta vacio (consulte alguna con la opcion 4).");
            return;
        }
        System.out.println("\nHISTORIAL (mas reciente primero):");
        int i = 1;
        for (Oportunidad o : historial.listarRecientes()) {
            System.out.println("  " + (i++) + ") " + o.descripcionCorta());
        }
    }

    private void sugerencias() throws DAOException {
        int idUsuario = leerEntero("ID de usuario: ");
        Usuario u = usuarioDAO.buscarPorId(idUsuario);
        if (u == null) {
            System.out.println(">> Usuario inexistente.");
            return;
        }
        List<Integer> idsAreas = new ArrayList<>();
        for (AreaEstudio a : u.getPreferencias()) {
            idsAreas.add(a.getId());
        }
        List<Oportunidad> sug = oportunidadDAO.sugerirPorAreas(idsAreas);
        imprimirLista("SUGERENCIAS PARA " + u.getNombre(), sug);
    }

    private void topProximas() throws DAOException {
        imprimirLista("TOP 3 MAS PROXIMAS A VENCER",
                Ordenador.topNPorVencer(oportunidadDAO.listarActivas(), 3));
    }

    private void modificarOportunidad() throws DAOException {
        int id = leerEntero("ID de la oportunidad a modificar: ");
        Oportunidad o = oportunidadDAO.buscarPorId(id);
        if (o == null) {
            System.out.println(">> No existe la oportunidad id=" + id);
            return;
        }
        System.out.println("\n-- Valores actuales --");
        System.out.println("   Titulo : " + o.getTitulo());
        System.out.println("   Fecha  : " + o.getFechaLimite());
        System.out.println("   Estado : " + o.getEstado());

        String t = leerLinea("Nuevo titulo (ENTER = mantener): ");
        if (!t.isBlank()) o.setTitulo(t);
        String f = leerLinea("Nueva fecha limite AAAA-MM-DD (ENTER = mantener): ");
        if (!f.isBlank()) o.setFechaLimite(parsearFecha(f));
        String e = leerLinea("Nuevo estado (activa/vencida/borrador/eliminada, ENTER = mantener): ");
        if (!e.isBlank()) {
            try {
                o.setEstado(EstadoOportunidad.valueOf(e.trim().toUpperCase()));
            } catch (IllegalArgumentException ex) {
                System.out.println(">> Estado invalido, se mantiene el actual.");
            }
        }
        oportunidadDAO.actualizar(o);
        Oportunidad v = oportunidadDAO.buscarPorId(id);
        System.out.println(">> Actualizada: " + v.descripcionCorta() + " | Estado: " + v.getEstado());
    }

    private void eliminarOportunidad() throws DAOException {
        int id = leerEntero("ID a eliminar (borrado logico): ");
        Oportunidad o = oportunidadDAO.buscarPorId(id);
        if (o == null) {
            System.out.println(">> No existe la oportunidad id=" + id);
            return;
        }
        String conf = leerLinea("Confirmar baja logica de '" + o.getTitulo() + "' (s/n): ");
        if (!conf.equalsIgnoreCase("s")) {
            System.out.println(">> Operacion cancelada.");
            return;
        }
        oportunidadDAO.eliminar(id);
        Oportunidad v = oportunidadDAO.buscarPorId(id);
        System.out.println(">> Estado actual: " + (v != null ? v.getEstado() : "(no encontrada)"));
    }

    private void exportarReporte() throws DAOException {
        try {
            String ruta = ExportadorReportes.exportarActivasCSV(oportunidadDAO.listarActivas());
            int filas = ExportadorReportes.contarRegistros(ruta);
            System.out.println(">> Reporte guardado en: " + ruta);
            System.out.println(">> Registros recuperados del archivo: " + filas);
        } catch (java.io.IOException e) {
            System.out.println(">> Error de archivo: " + e.getMessage());
        }
    }

    /** RF13: reporte estadistico de activas por tipo, pais y area (CU-11). */
    private void reporteEstadistico() throws DAOException {
        reportePorTipo();   // dimension original (intacta)
        imprimirConteo("REPORTE - OPORTUNIDADES ACTIVAS POR PAIS",
                oportunidadDAO.contarActivasPorPais());
        imprimirConteo("REPORTE - OPORTUNIDADES ACTIVAS POR AREA DE ESTUDIO",
                oportunidadDAO.contarActivasPorArea());
    }

    private void reportePorTipo() throws DAOException {
        // Conteo por tipo usando un ARREGLO indexado por el ordinal del enum.
        int[] conteo = new int[TipoOportunidad.values().length];
        for (Oportunidad o : oportunidadDAO.listarActivas()) {
            if (o.getTipo() != null) {
                conteo[o.getTipo().ordinal()]++;
            }
        }
        System.out.println("\nREPORTE - OPORTUNIDADES ACTIVAS POR TIPO:");
        for (TipoOportunidad t : TipoOportunidad.values()) {
            System.out.printf("  %-13s : %d%n", t.getEtiqueta(), conteo[t.ordinal()]);
        }
    }

    /** Imprime un conteo (clave -> cantidad) ya ordenado por la capa de datos. */
    private void imprimirConteo(String titulo, Map<String, Integer> conteo) {
        System.out.println("\n" + titulo + ":");
        if (conteo.isEmpty()) {
            System.out.println("  (sin datos)");
            return;
        }
        for (Map.Entry<String, Integer> e : conteo.entrySet()) {
            System.out.printf("  %-30s : %d%n", e.getKey(), e.getValue());
        }
    }

    // ---------------- Utilidades de E/S ----------------

    private void imprimirLista(String titulo, List<Oportunidad> lista) {
        System.out.println("\n== " + titulo + " (" + lista.size() + ") ==");
        if (lista.isEmpty()) {
            System.out.println("  (sin resultados; pruebe ampliar los criterios)");
            return;
        }
        for (Oportunidad o : lista) {
            System.out.println("  - " + o.descripcionCorta());
        }
    }

    private int leerEntero(String prompt) {
        System.out.print(prompt);
        try {
            String linea = sc.nextLine();
            return Integer.parseInt(linea.trim());
        } catch (NumberFormatException | InputMismatchException e) {
            System.out.println(">> Debe ingresar un numero entero. Se asume -1.");
            return -1;
        } catch (Exception e) {
            return 0;     // fin de entrada (EOF)
        }
    }

    private String leerLinea(String prompt) {
        System.out.print(prompt);
        try {
            return sc.nextLine().trim();
        } catch (Exception e) {
            return "";
        }
    }

    private LocalDate parsearFecha(String txt) {
        try {
            return LocalDate.parse(txt);
        } catch (DateTimeParseException e) {
            System.out.println(">> Fecha invalida; se usa la fecha de hoy.");
            return LocalDate.now();
        }
    }
}
