package com.ascendia.modelo;

/**
 * Entidad Pais. Demuestra HERENCIA (extends EntidadBase) y POLIMORFISMO
 * (redefine validar() y descripcionCorta() con @Override).
 */
public class Pais extends EntidadBase {

    private String nombre;
    private String codigoIso;
    private String continente;
    private double latitud;
    private double longitud;

    public Pais() { super(); }

    public Pais(int id, String nombre, String codigoIso, String continente,
                double latitud, double longitud) {
        super(id);                  // invoca al constructor de la superclase
        this.nombre = nombre;
        this.codigoIso = codigoIso;
        this.continente = continente;
        this.latitud = latitud;
        this.longitud = longitud;
    }

    @Override
    public boolean validar() {
        return nombre != null && !nombre.isBlank()
            && codigoIso != null && codigoIso.length() == 3;
    }

    @Override
    public String descripcionCorta() {
        return nombre + " (" + codigoIso + ")";
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getCodigoIso() { return codigoIso; }
    public void setCodigoIso(String codigoIso) { this.codigoIso = codigoIso; }
    public String getContinente() { return continente; }
    public void setContinente(String continente) { this.continente = continente; }
    public double getLatitud() { return latitud; }
    public void setLatitud(double latitud) { this.latitud = latitud; }
    public double getLongitud() { return longitud; }
    public void setLongitud(double longitud) { this.longitud = longitud; }
}
