package com.ascendia.dao.jdbc;

import com.ascendia.dao.DAOException;
import com.ascendia.dao.IOportunidadDAO;
import com.ascendia.modelo.*;
import com.ascendia.util.ConexionDB;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementacion JDBC de IOportunidadDAO: persistencia REAL contra la base
 * MySQL "ascendia" definida en el TP2. Usa PreparedStatement para prevenir
 * inyeccion SQL (RNF09) y traduce toda SQLException a DAOException (RNF10).
 *
 * Requiere el driver MySQL Connector/J en el classpath y la base creada.
 */
public class OportunidadDAOJDBC implements IOportunidadDAO {

    @Override
    public void insertar(Oportunidad o) throws DAOException {
        if (!o.validar()) {
            throw new DAOException("Oportunidad invalida: titulo, tipo o coordenadas.");
        }
        String sql = "INSERT INTO oportunidad "
                + "(titulo, descripcion, tipo, universidad_id, area_estudio_id, "
                + " fecha_limite, url_oficial, idioma, modalidad, latitud, longitud, estado) "
                + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection cn = ConexionDB.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, o.getTitulo());
            ps.setString(2, o.getDescripcion());
            ps.setString(3, o.getTipo().name().toLowerCase());
            if (o.getUniversidad() != null) ps.setInt(4, o.getUniversidad().getId()); else ps.setNull(4, Types.INTEGER);
            if (o.getArea() != null) ps.setInt(5, o.getArea().getId()); else ps.setNull(5, Types.INTEGER);
            ps.setDate(6, o.getFechaLimite() != null ? Date.valueOf(o.getFechaLimite()) : null);
            ps.setString(7, o.getUrlOficial());
            ps.setString(8, o.getIdioma());
            ps.setString(9, o.getModalidad() != null ? o.getModalidad().name().toLowerCase() : null);
            ps.setDouble(10, o.getLatitud());
            ps.setDouble(11, o.getLongitud());
            ps.setString(12, o.getEstado().name().toLowerCase());
            ps.executeUpdate();

            try (ResultSet gk = ps.getGeneratedKeys()) {
                if (gk.next()) o.setId(gk.getInt(1));
            }
        } catch (SQLException e) {
            throw new DAOException("Error al insertar oportunidad", e);
        }
    }

    @Override
    public void actualizar(Oportunidad o) throws DAOException {
        String sql = "UPDATE oportunidad SET titulo=?, fecha_limite=?, estado=? WHERE id=?";
        try (Connection cn = ConexionDB.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, o.getTitulo());
            ps.setDate(2, o.getFechaLimite() != null ? Date.valueOf(o.getFechaLimite()) : null);
            ps.setString(3, o.getEstado().name().toLowerCase());
            ps.setInt(4, o.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Error al actualizar oportunidad", e);
        }
    }

    @Override
    public void eliminar(int id) throws DAOException {
        // Borrado LOGICO: cambia el estado a 'eliminada' (no DELETE fisico).
        String sql = "UPDATE oportunidad SET estado='eliminada' WHERE id=?";
        try (Connection cn = ConexionDB.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Error al eliminar (logico) oportunidad", e);
        }
    }

    @Override
    public Oportunidad buscarPorId(int id) throws DAOException {
        String sql = "SELECT * FROM oportunidad WHERE id=?";
        try (Connection cn = ConexionDB.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw new DAOException("Error al buscar oportunidad id=" + id, e);
        }
    }

    @Override
    public List<Oportunidad> listarTodos() throws DAOException {
        return ejecutarConsulta("SELECT * FROM oportunidad ORDER BY fecha_limite ASC", new ArrayList<>());
    }

    @Override
    public List<Oportunidad> listarActivas() throws DAOException {
        // DEF-004: excluir filas con fecha_limite ya vencida; nulls se incluyen (sin limite fijo).
        return ejecutarConsulta(
            "SELECT * FROM oportunidad WHERE estado='activa' "
            + "AND (fecha_limite IS NULL OR fecha_limite >= CURDATE()) "
            + "ORDER BY fecha_limite ASC",
            new ArrayList<>());
    }

    @Override
    public List<Oportunidad> buscarConFiltros(TipoOportunidad tipo, String continente,
                                              Modalidad modalidad) throws DAOException {
        // Construccion DINAMICA del WHERE (CU-07): solo se agregan los filtros usados.
        // DEF-004: misma condicion de vigencia que listarActivas().
        StringBuilder sql = new StringBuilder(
            "SELECT o.* FROM oportunidad o "
            + "JOIN universidad u ON o.universidad_id = u.id "
            + "JOIN pais p ON u.pais_id = p.id "
            + "WHERE o.estado='activa' "
            + "AND (o.fecha_limite IS NULL OR o.fecha_limite >= CURDATE())");
        List<Object> params = new ArrayList<>();
        if (tipo != null) { sql.append(" AND o.tipo=?"); params.add(tipo.name().toLowerCase()); }
        if (modalidad != null) { sql.append(" AND o.modalidad=?"); params.add(modalidad.name().toLowerCase()); }
        if (continente != null && !continente.isBlank()) { sql.append(" AND p.continente=?"); params.add(continente); }
        sql.append(" ORDER BY o.fecha_limite ASC");
        return ejecutarConsulta(sql.toString(), params);
    }

    @Override
    public List<Oportunidad> sugerirPorAreas(List<Integer> idsAreas) throws DAOException {
        if (idsAreas == null || idsAreas.isEmpty()) return new ArrayList<>();
        StringBuilder marcadores = new StringBuilder();
        for (int i = 0; i < idsAreas.size(); i++) marcadores.append(i == 0 ? "?" : ",?");
        String sql = "SELECT * FROM oportunidad WHERE estado='activa' AND area_estudio_id IN ("
                + marcadores + ") ORDER BY fecha_limite ASC";
        return ejecutarConsulta(sql, new ArrayList<>(idsAreas));
    }

    @Override
    public Map<String, Integer> contarActivasPorPais() throws DAOException {
        // RF13: agrupacion por pais resuelta en SQL (los objetos JDBC no traen la
        // universidad/pais cargada). Misma vigencia que listarActivas().
        String sql = "SELECT p.nombre AS clave, COUNT(*) AS cantidad "
                + "FROM oportunidad o "
                + "JOIN universidad u ON o.universidad_id = u.id "
                + "JOIN pais p ON u.pais_id = p.id "
                + "WHERE o.estado='activa' "
                + "AND (o.fecha_limite IS NULL OR o.fecha_limite >= CURDATE()) "
                + "GROUP BY p.nombre ORDER BY cantidad DESC, p.nombre ASC";
        return agruparConteo(sql);
    }

    @Override
    public Map<String, Integer> contarActivasPorArea() throws DAOException {
        // RF13: agrupacion por area de estudio resuelta en SQL (GROUP BY).
        String sql = "SELECT a.nombre AS clave, COUNT(*) AS cantidad "
                + "FROM oportunidad o "
                + "JOIN area_estudio a ON o.area_estudio_id = a.id "
                + "WHERE o.estado='activa' "
                + "AND (o.fecha_limite IS NULL OR o.fecha_limite >= CURDATE()) "
                + "GROUP BY a.nombre ORDER BY cantidad DESC, a.nombre ASC";
        return agruparConteo(sql);
    }

    /** Auxiliar: ejecuta una consulta de agregacion (clave, cantidad) preservando el orden. */
    private Map<String, Integer> agruparConteo(String sql) throws DAOException {
        Map<String, Integer> res = new LinkedHashMap<>();
        try (Connection cn = ConexionDB.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) res.put(rs.getString("clave"), rs.getInt("cantidad"));
        } catch (SQLException e) {
            throw new DAOException("Error al agrupar reporte de oportunidades", e);
        }
        return res;
    }

    /** Metodo auxiliar reutilizable: ejecuta una consulta parametrizada. */
    private List<Oportunidad> ejecutarConsulta(String sql, List<Object> params) throws DAOException {
        List<Oportunidad> res = new ArrayList<>();
        try (Connection cn = ConexionDB.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) res.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw new DAOException("Error al ejecutar consulta de oportunidades", e);
        }
        return res;
    }

    /** Convierte una fila del ResultSet en un objeto Oportunidad. */
    private Oportunidad mapear(ResultSet rs) throws SQLException {
        Oportunidad o = new Oportunidad();
        o.setId(rs.getInt("id"));
        o.setTitulo(rs.getString("titulo"));
        o.setDescripcion(rs.getString("descripcion"));
        o.setTipo(TipoOportunidad.desdeTexto(rs.getString("tipo")));
        Date f = rs.getDate("fecha_limite");
        o.setFechaLimite(f != null ? f.toLocalDate() : null);
        o.setUrlOficial(rs.getString("url_oficial"));
        o.setIdioma(rs.getString("idioma"));
        String mod = rs.getString("modalidad");
        if (mod != null) o.setModalidad(Modalidad.valueOf(mod.toUpperCase()));
        o.setLatitud(rs.getDouble("latitud"));
        o.setLongitud(rs.getDouble("longitud"));
        o.setEstado(EstadoOportunidad.valueOf(rs.getString("estado").toUpperCase()));
        return o;
    }
}
