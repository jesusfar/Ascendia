package com.ascendia.dao;

import com.ascendia.modelo.Usuario;

/** Contrato especifico para usuarios. */
public interface IUsuarioDAO extends IGenericoDAO<Usuario> {

    Usuario buscarPorEmail(String email) throws DAOException;
}
