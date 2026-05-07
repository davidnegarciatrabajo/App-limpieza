# App-limpieza

Aplicacion Android de tareas diarias con recordatorios locales.

## Base de datos (Room)

- Version actual del schema: **3** (ver `app/schemas/` y `TareasDatabase`).
- Migraciones explicitas: `1 -> 2` (columna `fecha_ultima_modificacion` en `tarea`, inicializada desde `fecha_creacion`) y `2 -> 3` (indice `index_tarea_fecha_ultima_modificacion`).
- **Release**: no se usa `fallbackToDestructiveMigration`; las actualizaciones deben pasar por las migraciones para no perder datos.
- **Debug**: se mantiene `fallbackToDestructiveMigration()` solo como red de seguridad en desarrollo (p. ej. schema corrupto o bump de version sin migracion local). En equipos, preferir desinstalar la app o corregir la migracion antes de depender del destructive.

## Rendimiento (hipotesis, sin medicion formal en esta fase)

- La consulta principal de tareas (`observePendingByFilterAndSort`) combina filtros dinamicos; con miles de filas podria valorarse un **indice compuesto** (por ejemplo `categoria_id` + `fecha_proxima_ejecucion`) si los planes de consulta muestran barridos amplios. Hoy los indices simples en `TareaEntity` cubren el volumen esperado de la v1.

## Pruebas

- Unit tests (VM / dominio): `./gradlew testDebugUnitTest`
- UI Compose (instrumentadas): `./gradlew connectedDebugAndroidTest` (requiere dispositivo o emulador)

## Regresion manual sugerida (corta)

1. Abrir **Today**: lista o estado vacio coherente; sin crash.
2. **Tareas**: busqueda, filtros de periodicidad, **filtros avanzados** (estado/fecha/categoria) y orden.
3. **Nueva tarea** / **Editar tarea**: guardado, validaciones, snackbar si falla persistencia (simular con avion modo offline no aplica a Room local; revisar logs si hay error real).
4. Navegacion atras desde formularios.
5. Tras cambiar version de BD en un build release, abrir app con BD v1/v2 de prueba y comprobar que los datos siguen accesibles (migracion).
