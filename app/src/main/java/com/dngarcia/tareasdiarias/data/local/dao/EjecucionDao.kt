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

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(ejecucion: EjecucionEntity): Long

    @Delete
    suspend fun delete(ejecucion: EjecucionEntity)

    @Query("DELETE FROM ejecucion WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM ejecucion WHERE tarea_id = :tareaId")
    suspend fun deleteByTareaId(tareaId: Long)
}

