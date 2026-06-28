package com.ascendia.modelo;

/** Enumeracion de los tipos de oportunidad (refleja el ENUM de la BD). */
public enum TipoOportunidad {
    BECA("Beca"),
    CURSO("Curso"),
    INTERCAMBIO("Intercambio"),
    PASANTIA("Pasantia"),
    VOLUNTARIADO("Voluntariado");

    private final String etiqueta;
    TipoOportunidad(String etiqueta) { this.etiqueta = etiqueta; }
    public String getEtiqueta() { return etiqueta; }

    /** Conversion segura desde texto (entrada de usuario). */
    public static TipoOportunidad desdeTexto(String t) {
        if (t == null) return null;
        for (TipoOportunidad x : values()) {
            if (x.name().equalsIgnoreCase(t.trim()) || x.etiqueta.equalsIgnoreCase(t.trim())) {
                return x;
            }
        }
        return null;
    }
}
