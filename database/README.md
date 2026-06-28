# Base de datos MySQL de Ascendia

## Nombre de la base de datos

La base de datos del proyecto se llama `ascendia`.

## Finalidad

Esta base de datos almacena la informacion principal del sistema Ascendia, una plataforma orientada a gestionar oportunidades educativas globales para jovenes. El modelo permite registrar paises, universidades, areas de estudio, usuarios, oportunidades educativas, favoritos y preferencias de usuario.

## Importacion en MySQL Workbench

1. Abrir MySQL Workbench y conectarse al servidor MySQL 8.0.
2. Ir a `File > Open SQL Script`.
3. Seleccionar el archivo `database/ascendia_database.sql`.
4. Ejecutar el script completo con el boton de ejecucion.
5. Verificar que se haya creado el esquema `ascendia` y sus tablas.

## Importacion por consola

Desde la raiz del repositorio, ejecutar:

```bash
mysql -u root -p < database/ascendia_database.sql
```

Luego ingresar la contrasena del usuario MySQL cuando la consola la solicite.

## Tablas principales

- `pais`: paises asociados a usuarios y universidades.
- `area_estudio`: categorias academicas de las oportunidades.
- `universidad`: instituciones educativas relacionadas con paises.
- `usuario`: usuarios registrados en el sistema.
- `oportunidad`: becas, cursos, intercambios, pasantias y voluntariados.
- `favorito`: relacion entre usuarios y oportunidades guardadas.
- `preferencia_usuario`: relacion entre usuarios y areas de estudio preferidas.

## Operaciones SQL incluidas

El script esta orientado a MySQL 8.0 e incluye:

- creacion de la base de datos `ascendia`;
- creacion de tablas con motor InnoDB;
- claves primarias;
- claves foraneas;
- relaciones uno a muchos y muchos a muchos;
- insercion de datos de ejemplo con `INSERT`;
- consultas funcionales con `SELECT`;
- actualizaciones con `UPDATE`;
- eliminaciones y borrado logico con `DELETE` y cambio de estado;
- comentarios explicativos sobre el diseno y los requerimientos funcionales.

## Relacion con el TP2

La base de datos documenta el modelo relacional necesario para el TP2 de Seminario de Practica en Informatica. El script permite demostrar la creacion del esquema, la normalizacion de entidades principales, la definicion de relaciones mediante claves foraneas y el uso de operaciones SQL basicas para insertar, consultar, actualizar y eliminar registros.
