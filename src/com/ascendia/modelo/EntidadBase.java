package com.ascendia.modelo;

import java.time.LocalDateTime;

/**
 * Clase ABSTRACTA base de todas las entidades del dominio de Ascendia.
 *
 * Demuestra el pilar de ABSTRACCION: concentra los atributos y el
 * comportamiento comunes a toda entidad persistible (identidad y fecha de
 * creacion) y obliga a las subclases a definir su propia logica mediante
 * los metodos abstractos validar() y descripcionCorta().
 *
 * Al no poder instanciarse directamente, fuerza a trabajar siempre con
 * tipos concretos, sentando la base de la HERENCIA y el POLIMORFISMO.
 */
public abstract class EntidadBase {

    // ENCAPSULAMIENTO: atributos privados, accesibles solo via getters/setters.
    private int id;
    private LocalDateTime fechaCreacion;

    /** Constructor por defecto: inicializa la fecha de creacion. */
    protected EntidadBase() {
        this.fechaCreacion = LocalDateTime.now();
    }

    /** Constructor parametrizado: reutiliza el anterior con this(). */
    protected EntidadBase(int id) {
        this();
        this.id = id;
    }

    /** Cada entidad concreta define sus propias reglas de validacion. */
    public abstract boolean validar();

    /** Representacion breve para listados (se redefine por polimorfismo). */
    public abstract String descripcionCorta();

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
