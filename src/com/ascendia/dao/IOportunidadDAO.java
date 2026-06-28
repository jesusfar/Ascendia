package com.ascendia.dao;

import com.ascendia.modelo.Modalidad;
import com.ascendia.modelo.Oportunidad;
import com.ascendia.modelo.TipoOportunidad;
import java.util.List;
import java.util.Map;

/** Contrato especifico para oportunidades (extiende el CRUD generico). */
public interface IOportunidadDAO extends IGenericoDAO<Oportunidad> {

    List<Oportunidad> listarActivas() throws DAOException;

    /** Busqueda multi-criterio (CU-07). Parametros nulos = sin filtrar. */
    List<Oportunidad> buscarConFiltros(TipoOportunidad tipo, String continente,
                                       Modalidad modalidad) throws DAOException;

    /** Sugerencias por area de interes (CU-12 / RF14). */
    List<Oportunidad> sugerirPorAreas(List<Integer> idsAreas) throws DAOException;

    /** RF13: conteo de oportunidades activas agrupadas por nombre de pais. */
    Map<String, Integer> contarActivasPorPais() throws DAOException;

    /** RF13: conteo de oportunidades activas agrupadas por nombre de area de estudio. */
    Map<String, Integer> contarActivasPorArea() throws DAOException;
}
