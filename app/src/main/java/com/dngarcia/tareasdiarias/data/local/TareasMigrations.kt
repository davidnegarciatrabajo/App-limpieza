package com.dngarcia.tareasdiarias.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migraciones explícitas alineadas con los schemas exportados en `app/schemas/`.
 * v1: sin `fecha_ultima_modificacion` en tarea.
 * v2: columna `fecha_ultima_modificacion` rellenada desde `fecha_creacion`.
 * v3: índice `index_tarea_fecha_ultima_modificacion`.
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
}
