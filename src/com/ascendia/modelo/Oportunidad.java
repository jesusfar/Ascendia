package com.ascendia.modelo;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Entidad central del sistema. Concentra la demostracion de los pilares POO:
 *  - HERENCIA: extiende EntidadBase.
 *  - ENCAPSULAMIENTO: atributos privados + getters/setters.
 *  - POLIMORFISMO: redefine validar(), descripcionCorta() y toString().
 *  - ABSTRACCION: usa enums y objetos de dominio (Universidad, AreaEstudio).
 */
public class Oportunidad extends EntidadBase {

    private String titulo;
    private String descripcion;
    private TipoOportunidad tipo;
    private Universidad universidad;
    private AreaEstudio area;
    private LocalDate fechaLimite;
    private String urlOficial;
    private String idioma;
    private Modalidad modalidad;
    private double latitud;
    private double longitud;
    private EstadoOportunidad estado;

    /** Constructor vacio (para instanciacion desde un ResultSet en el DAO JDBC). */
    public Oportunidad() {
        super();
        this.estado = EstadoOportunidad.ACTIVA;
        this.idioma = "Espanol";
    }

    /** Constructor parametrizado para alta rapida desde el menu. */
    public Oportunidad(String titulo, TipoOportunidad tipo, Universidad universidad,
                       AreaEstudio area, LocalDate fechaLimite, Modalidad modalidad) {
        this();
        this.titulo = titulo;
        this.tipo = tipo;
        this.universidad = universidad;
        this.area = area;
        this.fechaLimite = fechaLimite;
        this.modalidad = modalidad;
        if (universidad != null) {
            this.latitud = universidad.getLatitud();
            this.longitud = universidad.getLongitud();
        }
    }

    @Override
    public boolean validar() {
        // Estructura de control con multiples condiciones (RF01 / DEF001 corregido)
        if (titulo == null || titulo.isBlank()) return false;
        if (tipo == null) return false;
        if (latitud < -90 || latitud > 90) return false;
        if (longitud < -180 || longitud > 180) return false;
        return true;
    }

    /** Regla de negocio: la oportunidad esta vigente si esta activa y no vencio. */
    public boolean estaVigente() {
        return estado == EstadoOportunidad.ACTIVA
            && fechaLimite != null
            && !fechaLimite.isBefore(LocalDate.now());
    }

    /** Dias restantes hasta la fecha limite (clave para ordenar). */
    public long diasRestantes() {
        if (fechaLimite == null) return Long.MAX_VALUE;
        return ChronoUnit.DAYS.between(LocalDate.now(), fechaLimite);
    }

    @Override
    public String descripcionCorta() {
        String uni = universidad != null ? universidad.getNombre() : "s/univ.";
        return String.format("[%s] %s - %s (vence %s)",
                tipo != null ? tipo.getEtiqueta() : "?", titulo, uni, fechaLimite);
    }

    @Override
    public String toString() {
        return descripcionCorta();
    }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public TipoOportunidad getTipo() { return tipo; }
    public void setTipo(TipoOportunidad tipo) { this.tipo = tipo; }
    public Universidad getUniversidad() { return universidad; }
    public void setUniversidad(Universidad universidad) { this.universidad = universidad; }
    public AreaEstudio getArea() { return area; }
    public void setArea(AreaEstudio area) { this.area = area; }
    public LocalDate getFechaLimite() { return fechaLimite; }
    public void setFechaLimite(LocalDate fechaLimite) { this.fechaLimite = fechaLimite; }
    public String getUrlOficial() { return urlOficial; }
    public void setUrlOficial(String urlOficial) { this.urlOficial = urlOficial; }
    public String getIdioma() { return idioma; }
    public void setIdioma(String idioma) { this.idioma = idioma; }
    public Modalidad getModalidad() { return modalidad; }
    public void setModalidad(Modalidad modalidad) { this.modalidad = modalidad; }
    public double getLatitud() { return latitud; }
    public void setLatitud(double latitud) { this.latitud = latitud; }
    public double getLongitud() { return longitud; }
    public void setLongitud(double longitud) { this.longitud = longitud; }
    public EstadoOportunidad getEstado() { return estado; }
    public void setEstado(EstadoOportunidad estado) { this.estado = estado; }
}
