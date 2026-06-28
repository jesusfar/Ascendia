package com.ascendia.util;

import com.ascendia.modelo.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Repositorio en memoria (patron Singleton). Mantiene las colecciones del
 * sistema y las inicializa con datos de prueba realistas equivalentes a los
 * del TP2 (becas DAAD, Erasmus, Fulbright; universidades y paises reales).
 *
 * Permite que el prototipo COMPILE Y SE EJECUTE en cualquier maquina sin
 * necesidad de un servidor MySQL, mientras que la implementacion JDBC
 * (OportunidadDAOJDBC) provee la persistencia real contra la base ascendia.
 */
public final class BaseMemoria {

    private static BaseMemoria instancia;     // Singleton

    private final List<Pais> paises = new ArrayList<>();
    private final List<AreaEstudio> areas = new ArrayList<>();
    private final List<Universidad> universidades = new ArrayList<>();
    private final List<Usuario> usuarios = new ArrayList<>();
    private final List<Oportunidad> oportunidades = new ArrayList<>();
    private final List<Favorito> favoritos = new ArrayList<>();

    private BaseMemoria() { sembrarDatos(); }

    public static synchronized BaseMemoria getInstancia() {
        if (instancia == null) {
            instancia = new BaseMemoria();
        }
        return instancia;
    }

    private void sembrarDatos() {
        // ---- Paises (coordenadas reales de capital) ----
        Pais arg = new Pais(1, "Argentina", "ARG", "America del Sur", -34.6037389, -58.3815704);
        Pais deu = new Pais(2, "Alemania", "DEU", "Europa", 52.5200066, 13.4049540);
        Pais esp = new Pais(3, "Espana", "ESP", "Europa", 40.4167754, -3.7037902);
        Pais usa = new Pais(4, "Estados Unidos", "USA", "America del Norte", 38.9071923, -77.0368707);
        Pais fra = new Pais(6, "Francia", "FRA", "Europa", 48.8566140, 2.3522219);
        Pais jpn = new Pais(7, "Japon", "JPN", "Asia", 35.6894875, 139.6917064);
        Pais col = new Pais(10, "Colombia", "COL", "America del Sur", 4.7109886, -74.0721064);
        paises.add(arg); paises.add(deu); paises.add(esp);
        paises.add(usa); paises.add(fra); paises.add(jpn); paises.add(col);

        // ---- Areas de estudio ----
        AreaEstudio ing = new AreaEstudio(1, "Ingenieria y Tecnologia", "Ingenieria, informatica y tecnologia aplicada.");
        AreaEstudio soc = new AreaEstudio(2, "Ciencias Sociales", "Sociologia, ciencias politicas, relaciones internacionales.");
        AreaEstudio eco = new AreaEstudio(4, "Economia y Negocios", "Administracion, finanzas, comercio internacional.");
        AreaEstudio exa = new AreaEstudio(6, "Ciencias Exactas", "Matematica, fisica, quimica, ciencias ambientales.");
        AreaEstudio der = new AreaEstudio(7, "Derecho", "Derecho internacional, derechos humanos.");
        AreaEstudio edu = new AreaEstudio(8, "Educacion", "Pedagogia, didactica, tecnologia educativa.");
        areas.add(ing); areas.add(soc); areas.add(eco); areas.add(exa); areas.add(der); areas.add(edu);

        // ---- Universidades (coordenadas reales) ----
        Universidad tuBerlin = new Universidad(2, "Technische Universitat Berlin", deu, "Berlin", 52.5125300, 13.3269300, "https://www.tu.berlin");
        Universidad usal = new Universidad(3, "Universidad de Salamanca", esp, "Salamanca", 40.9607800, -5.6689500, "https://www.usal.es");
        Universidad mit = new Universidad(4, "MIT", usa, "Cambridge, MA", 42.3601000, -71.0942000, "https://www.mit.edu");
        Universidad sorbonne = new Universidad(6, "Sorbonne Universite", fra, "Paris", 48.8479400, 2.3563900, "https://www.sorbonne-universite.fr");
        Universidad tokyo = new Universidad(7, "University of Tokyo", jpn, "Tokio", 35.7126000, 139.7620000, "https://www.u-tokyo.ac.jp");
        Universidad andes = new Universidad(10, "Universidad de los Andes", col, "Bogota", 4.6012700, -74.0649200, "https://www.uniandes.edu.co");
        universidades.add(tuBerlin); universidades.add(usal); universidades.add(mit);
        universidades.add(sorbonne); universidades.add(tokyo); universidades.add(andes);

        // ---- Usuarios ----
        Usuario admin = new Usuario(1, "Jesus Farina", "jesus.farina@email.com", "hash_admin", arg, "Universitario", RolUsuario.ADMIN);
        Usuario maria = new Usuario(2, "Maria Garcia", "maria.garcia@email.com", "hash_maria", arg, "Universitario", RolUsuario.USUARIO);
        Usuario hans = new Usuario(3, "Hans Muller", "hans.muller@email.com", "hash_hans", deu, "Posgrado", RolUsuario.USUARIO);
        maria.agregarPreferencia(ing); maria.agregarPreferencia(eco);
        hans.agregarPreferencia(ing); hans.agregarPreferencia(exa);
        usuarios.add(admin); usuarios.add(maria); usuarios.add(hans);

        // ---- Oportunidades ----
        agregar(1, "Beca DAAD para Maestria en Ingenieria", TipoOportunidad.BECA, tuBerlin, ing,
                LocalDate.of(2026, 10, 15), Modalidad.PRESENCIAL, EstadoOportunidad.ACTIVA, "Aleman/Ingles");
        agregar(2, "Erasmus Mundus Joint Masters", TipoOportunidad.BECA, sorbonne, soc,
                LocalDate.of(2026, 9, 1), Modalidad.PRESENCIAL, EstadoOportunidad.ACTIVA, "Ingles/Frances");
        agregar(3, "Curso Online: Inteligencia Artificial (MIT)", TipoOportunidad.CURSO, mit, ing,
                LocalDate.of(2026, 12, 31), Modalidad.VIRTUAL, EstadoOportunidad.ACTIVA, "Ingles");
        agregar(5, "Pasantia en Investigacion - University of Tokyo", TipoOportunidad.PASANTIA, tokyo, exa,
                LocalDate.of(2026, 7, 1), Modalidad.PRESENCIAL, EstadoOportunidad.ACTIVA, "Ingles/Japones");
        agregar(6, "Voluntariado Educativo en Comunidades Rurales", TipoOportunidad.VOLUNTARIADO, andes, edu,
                LocalDate.of(2026, 11, 30), Modalidad.PRESENCIAL, EstadoOportunidad.ACTIVA, "Espanol");
        agregar(7, "Beca Fulbright para Doctorado en EE.UU.", TipoOportunidad.BECA, mit, exa,
                LocalDate.of(2026, 6, 30), Modalidad.PRESENCIAL, EstadoOportunidad.ACTIVA, "Ingles");
        agregar(8, "Curso de Derecho Internacional Humanitario", TipoOportunidad.CURSO, usal, der,
                LocalDate.of(2026, 5, 20), Modalidad.HIBRIDA, EstadoOportunidad.VENCIDA, "Espanol");
    }

    private void agregar(int id, String titulo, TipoOportunidad tipo, Universidad uni,
                         AreaEstudio area, LocalDate fecha, Modalidad mod,
                         EstadoOportunidad estado, String idioma) {
        Oportunidad o = new Oportunidad(titulo, tipo, uni, area, fecha, mod);
        o.setId(id);
        o.setEstado(estado);
        o.setIdioma(idioma);
        oportunidades.add(o);
    }

    public List<Pais> getPaises() { return paises; }
    public List<AreaEstudio> getAreas() { return areas; }
    public List<Universidad> getUniversidades() { return universidades; }
    public List<Usuario> getUsuarios() { return usuarios; }
    public List<Oportunidad> getOportunidades() { return oportunidades; }
    public List<Favorito> getFavoritos() { return favoritos; }

    public int siguienteIdOportunidad() {
        int max = 0;
        for (Oportunidad o : oportunidades) {
            if (o.getId() > max) max = o.getId();
        }
        return max + 1;
    }
}
