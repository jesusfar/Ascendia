package com.ascendia.dao.memoria;

import com.ascendia.dao.DAOException;
import com.ascendia.dao.IOportunidadDAO;
import com.ascendia.modelo.*;
import com.ascendia.util.BaseMemoria;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementacion EN MEMORIA de IOportunidadDAO. Opera sobre las colecciones
 * del Singleton BaseMemoria. Permite ejecutar el prototipo sin servidor MySQL.
 */
public class OportunidadDAOMemoria implements IOportunidadDAO {

    private final BaseMemoria base = BaseMemoria.getInstancia();

    @Override
    public void insertar(Oportunidad o) throws DAOException {
        if (!o.validar()) {
            throw new DAOException("Oportunidad invalida: revise titulo, tipo y coordenadas.");
        }
        if (o.getId() <= 0) {
            o.setId(base.siguienteIdOportunidad());
        }
        base.getOportunidades().add(o);
    }

    @Override
    public void actualizar(Oportunidad o) throws DAOException {
        Oportunidad existente = buscarPorId(o.getId());
        if (existente == null) {
            throw new DAOException("No existe la oportunidad id=" + o.getId());
        }
        base.getOportunidades().remove(existente);
        base.getOportunidades().add(o);
    }

    @Override
    public void eliminar(int id) throws DAOException {
        // Borrado LOGICO (coherente con el diagrama de estados del TP2).
        Oportunidad o = buscarPorId(id);
        if (o == null) {
            throw new DAOException("No existe la oportunidad id=" + id);
        }
        o.setEstado(EstadoOportunidad.ELIMINADA);
    }

    @Override
    public Oportunidad buscarPorId(int id) throws DAOException {
        for (Oportunidad o : base.getOportunidades()) {
            if (o.getId() == id) return o;
        }
        return null;
    }

    @Override
    public List<Oportunidad> listarTodos() throws DAOException {
        return new ArrayList<>(base.getOportunidades());
    }

    @Override
    public List<Oportunidad> listarActivas() throws DAOException {
        List<Oportunidad> res = new ArrayList<>();
        for (Oportunidad o : base.getOportunidades()) {       // estructura repetitiva
            // DEF-004: se filtra por vigencia real (estado activa + fecha no vencida).
            if (o.estaVigente()) {                            // estructura condicional
                res.add(o);
            }
        }
        return res;
    }

    @Override
    public List<Oportunidad> buscarConFiltros(TipoOportunidad tipo, String continente,
                                              Modalidad modalidad) throws DAOException {
        List<Oportunidad> res = new ArrayList<>();
        for (Oportunidad o : listarActivas()) {
            // WHERE condicional replicado en logica Java (filtros combinables).
            if (tipo != null && o.getTipo() != tipo) continue;
            if (modalidad != null && o.getModalidad() != modalidad) continue;
            if (continente != null && !continente.isBlank()) {
                Universidad u = o.getUniversidad();
                if (u == null || u.getPais() == null
                        || !u.getPais().getContinente().equalsIgnoreCase(continente)) {
                    continue;
                }
            }
            res.add(o);
        }
        return res;
    }

    @Override
    public List<Oportunidad> sugerirPorAreas(List<Integer> idsAreas) throws DAOException {
        List<Oportunidad> res = new ArrayList<>();
        if (idsAreas == null || idsAreas.isEmpty()) return res;
        for (Oportunidad o : listarActivas()) {
            if (o.getArea() != null && idsAreas.contains(o.getArea().getId())) {
                res.add(o);
            }
        }
        return res;
    }

    @Override
    public Map<String, Integer> contarActivasPorPais() throws DAOException {
        // RF13: los objetos en memoria si traen la universidad (con su pais), se agrupa en Java.
        Map<String, Integer> res = new LinkedHashMap<>();
        for (Oportunidad o : listarActivas()) {
            Universidad u = o.getUniversidad();
            if (u != null && u.getPais() != null) {
                res.merge(u.getPais().getNombre(), 1, Integer::sum);
            }
        }
        return ordenarPorCantidadDesc(res);
    }

    @Override
    public Map<String, Integer> contarActivasPorArea() throws DAOException {
        // RF13: agrupacion por area de estudio sobre los objetos en memoria.
        Map<String, Integer> res = new LinkedHashMap<>();
        for (Oportunidad o : listarActivas()) {
            if (o.getArea() != null) {
                res.merge(o.getArea().getNombre(), 1, Integer::sum);
            }
        }
        return ordenarPorCantidadDesc(res);
    }

    /** Ordena un conteo por cantidad descendente (y clave asc), igual que el GROUP BY de JDBC. */
    private Map<String, Integer> ordenarPorCantidadDesc(Map<String, Integer> in) {
        List<Map.Entry<String, Integer>> entradas = new ArrayList<>(in.entrySet());
        entradas.sort((a, b) -> {
            int c = Integer.compare(b.getValue(), a.getValue());
            return c != 0 ? c : a.getKey().compareTo(b.getKey());
        });
        Map<String, Integer> out = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> e : entradas) out.put(e.getKey(), e.getValue());
        return out;
    }
}
