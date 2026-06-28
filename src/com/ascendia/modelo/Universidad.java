package com.ascendia.modelo;

/** Universidad o institucion. Asocia (composicion) un objeto Pais. */
public class Universidad extends EntidadBase {

    private String nombre;
    private Pais pais;
    private String ciudad;
    private double latitud;
    private double longitud;
    private String sitioWeb;

    public Universidad() { super(); }

    public Universidad(int id, String nombre, Pais pais, String ciudad,
                       double latitud, double longitud, String sitioWeb) {
        super(id);
        this.nombre = nombre;
        this.pais = pais;
        this.ciudad = ciudad;
        this.latitud = latitud;
        this.longitud = longitud;
        this.sitioWeb = sitioWeb;
    }

    @Override
    public boolean validar() {
        return nombre != null && !nombre.isBlank() && pais != null;
    }

    @Override
    public String descripcionCorta() {
        return nombre + (pais != null ? " - " + pais.getNombre() : "");
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Pais getPais() { return pais; }
    public void setPais(Pais pais) { this.pais = pais; }
    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }
    public double getLatitud() { return latitud; }
    public void setLatitud(double latitud) { this.latitud = latitud; }
    public double getLongitud() { return longitud; }
    public void setLongitud(double longitud) { this.longitud = longitud; }
    public String getSitioWeb() { return sitioWeb; }
    public void setSitioWeb(String sitioWeb) { this.sitioWeb = sitioWeb; }
}
