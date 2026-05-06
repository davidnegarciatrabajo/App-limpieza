package com.dngarcia.tareasdiarias.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.dngarcia.tareasdiarias.data.local.entity.CategoriaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoriaDao {
    @Query("SELECT * FROM categoria ORDER BY nombre ASC")
    fun observeAll(): Flow<List<CategoriaEntity>>

    @Query("SELECT * FROM categoria WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): CategoriaEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(categoria: CategoriaEntity): Long

    @Update
    suspend fun update(categoria: CategoriaEntity)

    @Delete
    suspend fun delete(categoria: CategoriaEntity)

    @Query("DELETE FROM categoria WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query(
        """
        SELECT EXISTS(
            SELECT 1 FROM categoria
            WHERE nombre = :nombre
            AND (:excludeId IS NULL OR id != :excludeId)
        )
        """
    )
    suspend fun existsByNombre(nombre: String, excludeId: Long? = null): Boolean
}

