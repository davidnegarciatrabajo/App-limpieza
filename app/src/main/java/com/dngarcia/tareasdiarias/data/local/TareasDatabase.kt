package com.dngarcia.tareasdiarias.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.dngarcia.tareasdiarias.data.local.converter.DateTimeConverters
import com.dngarcia.tareasdiarias.data.local.dao.CategoriaDao
import com.dngarcia.tareasdiarias.data.local.dao.EjecucionDao
import com.dngarcia.tareasdiarias.data.local.dao.TareaDao
import com.dngarcia.tareasdiarias.data.local.entity.CategoriaEntity
import com.dngarcia.tareasdiarias.data.local.entity.EjecucionEntity
import com.dngarcia.tareasdiarias.data.local.entity.TareaEntity

@Database(
    entities = [
        TareaEntity::class,
        CategoriaEntity::class,
        EjecucionEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(DateTimeConverters::class)
abstract class TareasDatabase : RoomDatabase() {
    abstract fun tareaDao(): TareaDao
    abstract fun categoriaDao(): CategoriaDao
    abstract fun ejecucionDao(): EjecucionDao

    companion object {
        const val DB_NAME: String = "tareas_diarias.db"
    }
}

