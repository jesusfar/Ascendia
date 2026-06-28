package com.ascendia.dao;

import com.ascendia.modelo.EntidadBase;
import java.util.List;

/**
 * Interfaz GENERICA del patron DAO. Demuestra ABSTRACCION (define el contrato
 * CRUD sin atarse a una tecnologia de persistencia) y uso de GENERICOS
 * acotados (T extends EntidadBase). Cualquier entidad del dominio puede
 * gestionarse de forma uniforme.
 */
public interface IGenericoDAO<T extends EntidadBase> {

    void insertar(T entidad) throws DAOException;
    void actualizar(T entidad) throws DAOException;
    void eliminar(int id) throws DAOException;
    T buscarPorId(int id) throws DAOException;
    List<T> listarTodos() throws DAOException;
}
