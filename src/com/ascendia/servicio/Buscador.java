package com.ascendia.servicio;

import com.ascendia.modelo.Oportunidad;
import java.util.ArrayList;
import java.util.List;

/**
 * Algoritmos de BUSQUEDA sobre oportunidades:
 *  - Busqueda LINEAL por palabra clave en el titulo (sin precondiciones).
 *  - Busqueda BINARIA por id (requiere la lista ordenada por id).
 */
public class Buscador {

    /** Busqueda lineal: recorre toda la coleccion comparando texto. */
    public static List<Oportunidad> porPalabraClave(List<Oportunidad> lista, String clave) {
        List<Oportunidad> res = new ArrayList<>();
        if (clave == null || clave.isBlank()) return res;
        String c = clave.toLowerCase();
        for (Oportunidad o : lista) {
            if (o.getTitulo() != null && o.getTitulo().toLowerCase().contains(c)) {
                res.add(o);
            }
        }
        return res;
    }

    /**
     * Busqueda binaria por id. La lista DEBE estar ordenada por id
     * (Ordenador.ordenarPorId). Devuelve la oportunidad o null.
     */
    public static Oportunidad binariaPorId(List<Oportunidad> ordenadasPorId, int id) {
        int inicio = 0;
        int fin = ordenadasPorId.size() - 1;
        while (inicio <= fin) {
            int medio = (inicio + fin) / 2;
            int actual = ordenadasPorId.get(medio).getId();
            if (actual == id) {
                return ordenadasPorId.get(medio);
            } else if (actual < id) {
                inicio = medio + 1;
            } else {
                fin = medio - 1;
            }
        }
        return null;
    }
}
