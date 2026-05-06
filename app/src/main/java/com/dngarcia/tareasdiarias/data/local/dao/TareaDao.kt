package com.dngarcia.tareasdiarias.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.dngarcia.tareasdiarias.data.local.entity.TareaEntity
import com.dngarcia.tareasdiarias.data.local.entity.TareaWithCategoria
import com.dngarcia.tareasdiarias.data.local.entity.TareaWithEjecuciones
import kotlinx.coroutines.flow.Flow

@Dao
interface TareaDao {
    @Query("SELECT * FROM tarea ORDER BY fecha_creacion DESC")
    fun observeAll(): Flow<List<TareaEntity>>

    @Query("SELECT * FROM tarea WHERE fecha_proxima_ejecucion IS NOT NULL")
    suspend fun getPendingReminderTasks(): List<TareaEntity>

    @Query("SELECT * FROM tarea WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): TareaEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(tarea: TareaEntity): Long

    @Update
    suspend fun update(tarea: TareaEntity)

    @Delete
    suspend fun delete(tarea: TareaEntity)

    @Query("DELETE FROM tarea WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Transaction
    @Query("SELECT * FROM tarea WHERE id = :id LIMIT 1")
    suspend fun getWithCategoriaById(id: Long): TareaWithCategoria?

    @Transaction
    @Query("SELECT * FROM tarea WHERE id = :id LIMIT 1")
    suspend fun getWithEjecucionesById(id: Long): TareaWithEjecuciones?

    @Query(
        """
        SELECT EXISTS(
            SELECT 1 FROM tarea
            WHERE nombre = :nombre
            AND (:excludeId IS NULL OR id != :excludeId)
        )
        """
    )
    suspend fun existsByNombre(nombre: String, excludeId: Long? = null): Boolean
}

