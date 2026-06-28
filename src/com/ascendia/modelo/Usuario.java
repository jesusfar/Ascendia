package com.ascendia.modelo;

import java.util.ArrayList;
import java.util.List;

/**
 * Usuario del sistema. Usa una ArrayList para sus preferencias (relacion N:M
 * con AreaEstudio) y demuestra ENCAPSULAMIENTO sobre la contrasena (solo se
 * almacena el hash, nunca el texto plano; RNF09).
 */
public class Usuario extends EntidadBase {

    private String nombre;
    private String email;
    private String passwordHash;
    private Pais pais;
    private String nivelEducativo;
    private RolUsuario rol;
    private final List<AreaEstudio> preferencias = new ArrayList<>();

    public Usuario() {
        super();
        this.rol = RolUsuario.USUARIO;
    }

    public Usuario(int id, String nombre, String email, String passwordHash,
                   Pais pais, String nivelEducativo, RolUsuario rol) {
        super(id);
        this.nombre = nombre;
        this.email = email;
        this.passwordHash = passwordHash;
        this.pais = pais;
        this.nivelEducativo = nivelEducativo;
        this.rol = rol;
    }

    @Override
    public boolean validar() {
        return nombre != null && !nombre.isBlank()
            && email != null && email.contains("@")
            && passwordHash != null && !passwordHash.isBlank();
    }

    @Override
    public String descripcionCorta() {
        return nombre + " <" + email + "> [" + rol + "]";
    }

    /** Simula la verificacion de password (en produccion se usa bcrypt). */
    public boolean verificarPassword(String hashIngresado) {
        return passwordHash != null && passwordHash.equals(hashIngresado);
    }

    /**
     * DEF-002: valida la fortaleza minima de la contrasena en TEXTO PLANO,
     * antes de calcular el hash. Politica minima: al menos 8 caracteres,
     * con al menos una letra y al menos un digito. Centraliza la regla para
     * el alta de usuario (RF04 / CU-04) y el eventual cambio de clave.
     *
     * @param passwordPlano contrasena ingresada por el usuario (sin hashear)
     * @return true si cumple la politica de seguridad minima
     */
    public static boolean esPasswordSeguro(String passwordPlano) {
        if (passwordPlano == null || passwordPlano.length() < 8) {
            return false;
        }
        boolean tieneLetra = false;
        boolean tieneDigito = false;
        for (char c : passwordPlano.toCharArray()) {
            if (Character.isLetter(c)) {
                tieneLetra = true;
            } else if (Character.isDigit(c)) {
                tieneDigito = true;
            }
        }
        return tieneLetra && tieneDigito;
    }

    public boolean esAdmin() { return rol == RolUsuario.ADMIN; }

    public void agregarPreferencia(AreaEstudio area) {
        if (area != null && !preferencias.contains(area)) {
            preferencias.add(area);
        }
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public Pais getPais() { return pais; }
    public void setPais(Pais pais) { this.pais = pais; }
    public String getNivelEducativo() { return nivelEducativo; }
    public void setNivelEducativo(String nivelEducativo) { this.nivelEducativo = nivelEducativo; }
    public RolUsuario getRol() { return rol; }
    public void setRol(RolUsuario rol) { this.rol = rol; }
    public List<AreaEstudio> getPreferencias() { return preferencias; }
}
