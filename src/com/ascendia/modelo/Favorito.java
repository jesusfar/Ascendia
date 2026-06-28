package com.ascendia.modelo;

import java.time.LocalDateTime;

/** Asociacion usuario-oportunidad (resuelve la relacion N:M de favoritos). */
public class Favorito extends EntidadBase {

    private Usuario usuario;
    private Oportunidad oportunidad;
    private LocalDateTime fechaGuardado;

    public Favorito() { super(); this.fechaGuardado = LocalDateTime.now(); }

    public Favorito(int id, Usuario usuario, Oportunidad oportunidad) {
        super(id);
        this.usuario = usuario;
        this.oportunidad = oportunidad;
        this.fechaGuardado = LocalDateTime.now();
    }

    @Override
    public boolean validar() {
        return usuario != null && oportunidad != null;
    }

    @Override
    public String descripcionCorta() {
        return usuario.getNombre() + " -> " + oportunidad.getTitulo();
    }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public Oportunidad getOportunidad() { return oportunidad; }
    public void setOportunidad(Oportunidad oportunidad) { this.oportunidad = oportunidad; }
    public LocalDateTime getFechaGuardado() { return fechaGuardado; }
    public void setFechaGuardado(LocalDateTime f) { this.fechaGuardado = f; }
}
