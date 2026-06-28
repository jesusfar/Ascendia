package com.ascendia.modelo;

/** Area de estudio (categoria de las oportunidades). */
public class AreaEstudio extends EntidadBase {

    private String nombre;
    private String descripcion;

    public AreaEstudio() { super(); }

    public AreaEstudio(int id, String nombre, String descripcion) {
        super(id);
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    @Override
    public boolean validar() {
        return nombre != null && !nombre.isBlank();
    }

    @Override
    public String descripcionCorta() {
        return nombre;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}
