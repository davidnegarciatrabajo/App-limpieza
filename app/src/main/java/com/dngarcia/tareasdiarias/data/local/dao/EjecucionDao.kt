package com.dngarcia.tareasdiarias.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dngarcia.tareasdiarias.data.local.entity.EjecucionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EjecucionDao {
    @Query("SELECT * FROM ejecucion WHERE tarea_id = :tareaId ORDER BY fecha_ejecucion DESC")
    fun observeByTareaId(tareaId: Long): Flow<List<EjecucionEntity>>

    @Query(
        """
        SELECT * FROM ejecucion
        WHERE completada_por_usuario = 1
        AND fecha_ejecucion >= :startInclusive
        AND fecha_ejecucion <= :endInclusive
        ORDER BY fecha_ejecucion DESC
        """
    )
    fun observeCompletedBetween(
        startInclusive: java.time.LocalDateTime,
        endInclusive: java.time.LocalDateTime,
    ): Flow<List<EjecucionEntity>>

    @Query(
        """
        SELECT * FROM ejecucion
        WHERE completada_por_usuario = 1
        AND fecha_ejecucion >= :startInclusive
        AND fecha_ejecucion <= :endInclusive
        ORDER BY fecha_ejecucion DESC
        """
    )
    suspend fun getCompletedBetween(
        startInclusive: java.time.LocalDateTime,
        endInclusive: java.time.LocalDateTime,
    ): List<EjecucionEntity>

    @Query(
        """
        SELECT * FROM ejecucion
        WHERE tarea_id = :tareaId
        AND completada_por_usuario = 1
        AND fecha_ejecucion >= :startInclusive
        AND fecha_ejecucion <= :endInclusive
        ORDER BY fecha_ejecucion DESC
        LIMIT 1
        """
    )
    suspend fun getLatestCompletedBetween(
        tareaId: Long,
        startInclusive: java.time.LocalDateTime,
        endInclusive: java.time.LocalDateTime,
    ): EjecucionEntity?

    @Query(
        """
        SELECT * FROM ejecucion
        WHERE tarea_id = :tareaId
        AND completada_por_usuario = 1
        AND fecha_ciclo_resuelto = :cycleDate
        ORDER BY fecha_ejecucion DESC
        LIMIT 1
        """
    )
    suspend fun getLatestCompletedForCycle(
        tareaId: Long,
        cycleDate: java.time.LocalDate,
    ): EjecucionEntity?

    @Query(
        """
        SELECT * FROM ejecucion
        WHERE tarea_id = :tareaId
        AND completada_por_usuario = 1
        ORDER BY fecha_ejecucion DESC
        LIMIT 1
        """
    )
    suspend fun getLatestCompletedByTaskId(tareaId: Long): EjecucionEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(ejecucion: EjecucionEntity): Long

    @Delete
    suspend fun delete(ejecucion: EjecucionEntity)

    @Query("DELETE FROM ejecucion WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM ejecucion WHERE tarea_id = :tareaId")
    suspend fun deleteByTareaId(tareaId: Long)
}

