package com.ascendia.dao.memoria;

import com.ascendia.dao.DAOException;
import com.ascendia.dao.IUsuarioDAO;
import com.ascendia.modelo.Usuario;
import com.ascendia.util.BaseMemoria;
import java.util.ArrayList;
import java.util.List;

/** Implementacion en memoria de IUsuarioDAO. */
public class UsuarioDAOMemoria implements IUsuarioDAO {

    private final BaseMemoria base = BaseMemoria.getInstancia();

    @Override
    public void insertar(Usuario u) throws DAOException {
        if (!u.validar()) {
            throw new DAOException("Usuario invalido: revise nombre, email y password.");
        }
        if (buscarPorEmail(u.getEmail()) != null) {
            throw new DAOException("El email ya esta registrado: " + u.getEmail());
        }
        if (u.getId() <= 0) {
            u.setId(base.getUsuarios().size() + 1);
        }
        base.getUsuarios().add(u);
    }

    @Override
    public void actualizar(Usuario u) throws DAOException {
        Usuario e = buscarPorId(u.getId());
        if (e == null) throw new DAOException("No existe el usuario id=" + u.getId());
        base.getUsuarios().remove(e);
        base.getUsuarios().add(u);
    }

    @Override
    public void eliminar(int id) throws DAOException {
        Usuario u = buscarPorId(id);
        if (u == null) throw new DAOException("No existe el usuario id=" + id);
        base.getUsuarios().remove(u);
    }

    @Override
    public Usuario buscarPorId(int id) throws DAOException {
        for (Usuario u : base.getUsuarios()) {
            if (u.getId() == id) return u;
        }
        return null;
    }

    @Override
    public Usuario buscarPorEmail(String email) throws DAOException {
        for (Usuario u : base.getUsuarios()) {
            if (u.getEmail().equalsIgnoreCase(email)) return u;
        }
        return null;
    }

    @Override
    public List<Usuario> listarTodos() throws DAOException {
        return new ArrayList<>(base.getUsuarios());
    }
}
