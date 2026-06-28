package com.ascendia.web;

import com.ascendia.dao.IOportunidadDAO;
import com.ascendia.modelo.Oportunidad;
import com.ascendia.util.FabricaDAO;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;

import java.util.ArrayList;
import java.util.List;

/**
 * VISTA WEB de Ascendia (RF11 - Globo 3D): expone los DAO existentes como API REST
 * JSON y sirve el globo. Corre en modo JDBC (persistencia real en MySQL). Es otra
 * vista sobre la MISMA capa de datos; no modifica el prototipo de consola.
 *
 * La API REST se implementa con Javalin sobre un contenedor de servlets embebido
 * (Jetty), reutilizando los DAO/JDBC del prototipo sin duplicar logica.
 */
public class ServidorWeb {

    // Tipos simples => el JSON no necesita configurar serializacion de fechas.
    public record OportunidadDTO(int id, String titulo, String tipo, double lat, double lng,
                                 String fechaApertura, String fechaLimite, long diasRestantes,
                                 String modalidad, String idioma) {}

    public static void main(String[] args) {
        // La vista web siempre trabaja contra la persistencia real (MySQL).
        FabricaDAO.setModo(FabricaDAO.Modo.JDBC);
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));

        Javalin app = Javalin.create(cfg ->
            cfg.staticFiles.add("/public", Location.CLASSPATH)
        ).start("0.0.0.0", port);

        IOportunidadDAO dao = FabricaDAO.getOportunidadDAO();

        app.get("/api/health", ctx -> ctx.result("ok"));

        // RF11: faros del globo. Devuelve solo oportunidades vigentes (listarActivas()).
        app.get("/api/oportunidades", ctx -> {
            List<OportunidadDTO> out = new ArrayList<>();
            for (Oportunidad o : dao.listarActivas()) out.add(aDTO(o));
            ctx.json(out);
        });

        // Detalle individual (usado por el panel del globo).
        app.get("/api/oportunidades/{id}", ctx -> {
            Oportunidad o = dao.buscarPorId(Integer.parseInt(ctx.pathParam("id")));
            if (o == null) { ctx.status(404).result("No encontrada"); return; }
            ctx.json(aDTO(o));
        });

        System.out.println("Ascendia Web (globo) en el puerto " + port);
    }

    /** Mapea la entidad de dominio al DTO que consume app.js (shape exacto del front). */
    private static OportunidadDTO aDTO(Oportunidad o) {
        return new OportunidadDTO(
            o.getId(),
            o.getTitulo(),
            o.getTipo() != null ? o.getTipo().name() : null,   // MAYUSCULAS: BECA, CURSO, ...
            o.getLatitud(),
            o.getLongitud(),
            null,  // fechaApertura: el modelo no la persiste; el front lo tolera (null)
            o.getFechaLimite() != null ? o.getFechaLimite().toString() : null, // ISO yyyy-MM-dd
            o.diasRestantes(),
            o.getModalidad() != null ? o.getModalidad().name() : null,  // PRESENCIAL / VIRTUAL / HIBRIDA
            o.getIdioma());
    }
}
