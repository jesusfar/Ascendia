package com.ascendia.servicio;

import com.ascendia.modelo.Oportunidad;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Estructura de datos tipo PILA (LIFO) que registra las ultimas oportunidades
 * consultadas por el usuario, mostrando primero la mas reciente. Implementada
 * con Deque (ArrayDeque), tal como pide la consigna respecto al uso de pilas.
 */
public class HistorialConsultas {

    private static final int CAPACIDAD_MAX = 5;
    private final Deque<Oportunidad> pila = new ArrayDeque<>();

    /** Apila una consulta; descarta la mas antigua si se supera la capacidad. */
    public void registrar(Oportunidad o) {
        if (o == null) return;
        pila.push(o);                       // push = tope de la pila (LIFO)
        while (pila.size() > CAPACIDAD_MAX) {
            pila.removeLast();              // descarta el fondo (mas antiguo)
        }
    }

    public Oportunidad ultimaConsultada() {
        return pila.peek();                 // peek = ver tope sin desapilar
    }

    public List<Oportunidad> listarRecientes() {
        return new ArrayList<>(pila);       // del mas reciente al mas antiguo
    }

    public boolean estaVacio() {
        return pila.isEmpty();
    }
}
