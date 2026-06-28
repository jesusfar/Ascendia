package com.ascendia.dao.jdbc;

import com.ascendia.dao.DAOException;
import com.ascendia.dao.IUsuarioDAO;
import com.ascendia.modelo.RolUsuario;
import com.ascendia.modelo.Usuario;
import com.ascendia.util.ConexionDB;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Implementacion JDBC de IUsuarioDAO (persistencia real en MySQL). */
public class UsuarioDAOJDBC implements IUsuarioDAO {

    @Override
    public void insertar(Usuario u) throws DAOException {
        if (!u.validar()) {
            throw new DAOException("Usuario invalido: nombre, email o password.");
        }
        String sql = "INSERT INTO usuario (nombre, email, password_hash, pais_id, "
                + "nivel_educativo, rol) VALUES (?,?,?,?,?,?)";
        try (Connection cn = ConexionDB.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getNombre());
            ps.setString(2, u.getEmail());
            ps.setString(3, u.getPasswordHash());
            if (u.getPais() != null) ps.setInt(4, u.getPais().getId()); else ps.setNull(4, Types.INTEGER);
            ps.setString(5, u.getNivelEducativo());
            ps.setString(6, u.getRol().name().toLowerCase());
            ps.executeUpdate();
            try (ResultSet gk = ps.getGeneratedKeys()) {
                if (gk.next()) u.setId(gk.getInt(1));
            }
        } catch (SQLException e) {
            throw new DAOException("Error al insertar usuario", e);
        }
    }

    @Override
    public void actualizar(Usuario u) throws DAOException {
        String sql = "UPDATE usuario SET nombre=?, nivel_educativo=? WHERE id=?";
        try (Connection cn = ConexionDB.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, u.getNombre());
            ps.setString(2, u.getNivelEducativo());
            ps.setInt(3, u.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Error al actualizar usuario", e);
        }
    }

    @Override
    public void eliminar(int id) throws DAOException {
        try (Connection cn = ConexionDB.getConexion();
             PreparedStatement ps = cn.prepareStatement("DELETE FROM usuario WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Error al eliminar usuario", e);
        }
    }

    @Override
    public Usuario buscarPorId(int id) throws DAOException {
        return buscar("SELECT * FROM usuario WHERE id=?", id, null);
    }

    @Override
    public Usuario buscarPorEmail(String email) throws DAOException {
        return buscar("SELECT * FROM usuario WHERE email=?", 0, email);
    }

    private Usuario buscar(String sql, int id, String email) throws DAOException {
        try (Connection cn = ConexionDB.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            if (email != null) ps.setString(1, email); else ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw new DAOException("Error al buscar usuario", e);
        }
    }

    @Override
    public List<Usuario> listarTodos() throws DAOException {
        List<Usuario> res = new ArrayList<>();
        try (Connection cn = ConexionDB.getConexion();
             PreparedStatement ps = cn.prepareStatement("SELECT * FROM usuario");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) res.add(mapear(rs));
        } catch (SQLException e) {
            throw new DAOException("Error al listar usuarios", e);
        }
        return res;
    }

    private Usuario mapear(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setId(rs.getInt("id"));
        u.setNombre(rs.getString("nombre"));
        u.setEmail(rs.getString("email"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setNivelEducativo(rs.getString("nivel_educativo"));
        String rol = rs.getString("rol");
        u.setRol(rol != null ? RolUsuario.valueOf(rol.toUpperCase()) : RolUsuario.USUARIO);
        return u;
    }
}
