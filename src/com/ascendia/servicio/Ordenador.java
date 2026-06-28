package com.ascendia.servicio;

import com.ascendia.modelo.Oportunidad;
import java.util.ArrayList;
import java.util.List;

/**
 * Algoritmos de ORDENACION aplicados a oportunidades.
 * Se implementa explicitamente el "ordenamiento por seleccion" (selection
 * sort) para evidenciar el manejo de estructuras repetitivas anidadas y de
 * intercambio de elementos, segun lo pedido por la consigna del TP3.
 */
public class Ordenador {

    /**
     * Ordena la lista por dias restantes hasta la fecha limite (ascendente):
     * las oportunidades mas proximas a vencer quedan primero.
     */
    public static void ordenarPorFechaLimite(List<Oportunidad> lista) {
        int n = lista.size();
        for (int i = 0; i < n - 1; i++) {              // recorre posiciones
            int indiceMin = i;
            for (int j = i + 1; j < n; j++) {          // busca el minimo restante
                if (clave(lista.get(j)) < clave(lista.get(indiceMin))) {
                    indiceMin = j;
                }
            }
            if (indiceMin != i) {                      // intercambio (swap)
                Oportunidad tmp = lista.get(i);
                lista.set(i, lista.get(indiceMin));
                lista.set(indiceMin, tmp);
            }
        }
    }

    private static long clave(Oportunidad o) {
        return o.diasRestantes();
    }

    /**
     * Retorna las N oportunidades mas proximas a vencer.
     * Uso COMPLEMENTARIO de arreglo y ArrayList: la coleccion de entrada
     * (ArrayList) se vuelca a un ARREGLO de tamano fijo para ordenarlo por
     * indices con insertion sort; el resultado se devuelve en un ArrayList
     * para integrarse con el resto del sistema (RF07 / pilares POO).
     *
     * @param origen lista fuente de oportunidades activas
     * @param n      cantidad maxima de resultados
     * @return lista con las N oportunidades de menor dias restantes
     */
    public static List<Oportunidad> topNPorVencer(List<Oportunidad> origen, int n) {
        Oportunidad[] arr = origen.toArray(new Oportunidad[0]); // ArrayList -> arreglo
        for (int i = 1; i < arr.length; i++) {                  // insertion sort sobre arreglo
            Oportunidad actual = arr[i];
            int k = i - 1;
            while (k >= 0 && arr[k].diasRestantes() > actual.diasRestantes()) {
                arr[k + 1] = arr[k];
                k--;
            }
            arr[k + 1] = actual;
        }
        List<Oportunidad> top = new ArrayList<>();               // arreglo -> ArrayList
        int limite = Math.min(n, arr.length);
        for (int i = 0; i < limite; i++) top.add(arr[i]);
        return top;
    }

    /** Ordena por id ascendente (precondicion para la busqueda binaria). */
    public static void ordenarPorId(List<Oportunidad> lista) {
        int n = lista.size();
        for (int i = 1; i < n; i++) {                  // insertion sort
            Oportunidad actual = lista.get(i);
            int k = i - 1;
            while (k >= 0 && lista.get(k).getId() > actual.getId()) {
                lista.set(k + 1, lista.get(k));
                k--;
            }
            lista.set(k + 1, actual);
        }
    }
}
