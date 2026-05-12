package com.dngarcia.tareasdiarias.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migraciones explícitas alineadas con los schemas exportados en `app/schemas/`.
 * v1: sin `fecha_ultima_modificacion` en tarea.
 * v2: columna `fecha_ultima_modificacion` rellenada desde `fecha_creacion`.
 * v3: índice `index_tarea_fecha_ultima_modificacion`.
 * v4: columna opcional `subtitulo` con default vacío.
 * v5: columna opcional `hora_recordatorio` para preservar la hora elegida por el usuario.
 * v6: columna obligatoria `fecha_inicio` para anclar la periodicidad sin perder datos previos.
 * v7: `fecha_visible_desde` separa la aparicion efectiva en Today y
 *     `ultima_vez_que_hice_la_tarea` persiste la ultima realizacion real.
 * v8: `ejecucion.fecha_ciclo_resuelto` permite deshacer con semantica correcta y
 *     `tarea.categoria_id` pasa a borrar en cascada.
 * v9: `ejecucion.cantidad_postergaciones_previas` para restaurar postergaciones al deshacer completado.
 */
object TareasMigrations {

    val MIGRATION_1_2: Migration = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "ALTER TABLE tarea ADD COLUMN fecha_ultima_modificacion INTEGER NOT NULL DEFAULT 0",
            )
            db.execSQL("UPDATE tarea SET fecha_ultima_modificacion = fecha_creacion")
        }
    }

    val MIGRATION_2_3: Migration = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_tarea_fecha_ultima_modificacion` " +
                    "ON `tarea` (`fecha_ultima_modificacion`)",
            )
        }
    }

    val MIGRATION_3_4: Migration = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "ALTER TABLE tarea ADD COLUMN subtitulo TEXT NOT NULL DEFAULT ''",
            )
        }
    }

    val MIGRATION_4_5: Migration = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "ALTER TABLE tarea ADD COLUMN hora_recordatorio INTEGER",
            )
        }
    }

    val MIGRATION_5_6: Migration = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "ALTER TABLE tarea ADD COLUMN fecha_inicio INTEGER NOT NULL DEFAULT 0",
            )
            db.execSQL(
                """
                UPDATE tarea
                SET fecha_inicio = CAST(
                    julianday(
                        COALESCE(
                            date(fecha_proxima_ejecucion / 1000, 'unixepoch'),
                            date(fecha_creacion / 1000, 'unixepoch')
                        )
                    ) - 2440587.5 AS INTEGER
                )
                """.trimIndent(),
            )
        }
    }

    val MIGRATION_6_7: Migration = object : Migration(6, 7) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "ALTER TABLE tarea ADD COLUMN fecha_visible_desde INTEGER",
            )
            db.execSQL(
                "ALTER TABLE tarea ADD COLUMN ultima_vez_que_hice_la_tarea INTEGER",
            )
            db.execSQL(
                """
                UPDATE tarea
                SET fecha_visible_desde = CAST(
                    julianday(
                        date(COALESCE(fecha_proxima_ejecucion / 1000, fecha_inicio * 86400), 'unixepoch')
                    ) - 2440587.5 AS INTEGER
                )
                WHERE fecha_proxima_ejecucion IS NOT NULL
                """.trimIndent(),
            )
            db.execSQL(
                """
                CREATE INDEX IF NOT EXISTS `index_tarea_fecha_visible_desde`
                ON `tarea` (`fecha_visible_desde`)
                """.trimIndent(),
            )
        }
    }

    val MIGRATION_7_8: Migration = object : Migration(7, 8) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "ALTER TABLE ejecucion ADD COLUMN fecha_ciclo_resuelto INTEGER",
            )
            db.execSQL(
                """
                UPDATE ejecucion
                SET fecha_ciclo_resuelto = CAST(
                    julianday(date(fecha_ejecucion / 1000, 'unixepoch')) - 2440587.5 AS INTEGER
                )
                WHERE completada_por_usuario = 1
                """.trimIndent(),
            )

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `tarea_new` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `nombre` TEXT NOT NULL,
                    `subtitulo` TEXT NOT NULL,
                    `categoria_id` INTEGER NOT NULL,
                    `tipo_periodicidad` TEXT NOT NULL,
                    `dias_periodicidad` INTEGER,
                    `notas` TEXT NOT NULL,
                    `fecha_inicio` INTEGER NOT NULL,
                    `fecha_creacion` INTEGER NOT NULL,
                    `fecha_ultima_modificacion` INTEGER NOT NULL,
                    `fecha_proxima_ejecucion` INTEGER,
                    `fecha_visible_desde` INTEGER,
                    `hora_recordatorio` INTEGER,
                    `ultima_vez_que_hice_la_tarea` INTEGER,
                    `cantidad_postergaciones` INTEGER NOT NULL,
                    `estado_alerta` TEXT NOT NULL,
                    `mensaje_alerta` TEXT,
                    FOREIGN KEY(`categoria_id`) REFERENCES `categoria`(`id`) ON UPDATE CASCADE ON DELETE CASCADE
                )
                """.trimIndent(),
            )
            db.execSQL(
                """
                INSERT INTO `tarea_new` (
                    `id`, `nombre`, `subtitulo`, `categoria_id`, `tipo_periodicidad`,
                    `dias_periodicidad`, `notas`, `fecha_inicio`, `fecha_creacion`,
                    `fecha_ultima_modificacion`, `fecha_proxima_ejecucion`, `fecha_visible_desde`,
                    `hora_recordatorio`, `ultima_vez_que_hice_la_tarea`, `cantidad_postergaciones`,
                    `estado_alerta`, `mensaje_alerta`
                )
                SELECT
                    `id`, `nombre`, `subtitulo`, `categoria_id`, `tipo_periodicidad`,
                    `dias_periodicidad`, `notas`, `fecha_inicio`, `fecha_creacion`,
                    `fecha_ultima_modificacion`, `fecha_proxima_ejecucion`, `fecha_visible_desde`,
                    `hora_recordatorio`, `ultima_vez_que_hice_la_tarea`, `cantidad_postergaciones`,
                    `estado_alerta`, `mensaje_alerta`
                FROM `tarea`
                """.trimIndent(),
            )
            db.execSQL("DROP TABLE `tarea`")
            db.execSQL("ALTER TABLE `tarea_new` RENAME TO `tarea`")
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_tarea_nombre` ON `tarea` (`nombre`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_tarea_categoria_id` ON `tarea` (`categoria_id`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_tarea_fecha_proxima_ejecucion` ON `tarea` (`fecha_proxima_ejecucion`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_tarea_fecha_visible_desde` ON `tarea` (`fecha_visible_desde`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_tarea_fecha_ultima_modificacion` ON `tarea` (`fecha_ultima_modificacion`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_tarea_tipo_periodicidad` ON `tarea` (`tipo_periodicidad`)")
        }
    }

    val MIGRATION_8_9: Migration = object : Migration(8, 9) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "ALTER TABLE ejecucion ADD COLUMN cantidad_postergaciones_previas INTEGER NOT NULL DEFAULT 0",
            )
        }
    }
}
