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
import java.time.LocalDateTime
import kotlinx.coroutines.flow.Flow

@Dao
interface TareaDao {
    @Query("SELECT * FROM tarea ORDER BY fecha_creacion DESC")
    fun observeAll(): Flow<List<TareaEntity>>

    @Query(
        """
        SELECT * FROM tarea
        WHERE fecha_proxima_ejecucion IS NOT NULL
        ORDER BY fecha_proxima_ejecucion ASC, fecha_creacion ASC
        LIMIT :limit
        """
    )
    fun observeTopPending(limit: Int): Flow<List<TareaEntity>>

    @Query(
        """
        SELECT * FROM tarea
        WHERE fecha_proxima_ejecucion IS NOT NULL
        AND (
            :searchQuery = ''
            OR lower(nombre) LIKE '%' || lower(:searchQuery) || '%'
            OR (:includeNotesInSearch = 1 AND lower(notas) LIKE '%' || lower(:searchQuery) || '%')
        )
        AND (
            :filter = 'ALL'
            OR (:filter = 'DAILY' AND (
                tipo_periodicidad = 'DIARIA'
                OR (tipo_periodicidad = 'PERSONALIZADA' AND dias_periodicidad BETWEEN 1 AND 7)
            ))
            OR (:filter = 'WEEKLY' AND (
                tipo_periodicidad = 'SEMANAL'
                OR (tipo_periodicidad = 'PERSONALIZADA' AND dias_periodicidad BETWEEN 8 AND 30)
            ))
            OR (:filter = 'MONTHLY' AND (
                tipo_periodicidad = 'MENSUAL'
                OR (tipo_periodicidad = 'PERSONALIZADA' AND dias_periodicidad BETWEEN 31 AND 179)
            ))
            OR (:filter = 'SEMIANNUAL' AND (
                tipo_periodicidad = 'SEMESTRAL'
                OR (tipo_periodicidad = 'PERSONALIZADA' AND dias_periodicidad >= 180)
            ))
            OR (:filter = 'UNIQUE' AND tipo_periodicidad = 'UNICA')
        )
        AND (
            :statusFilter IS NULL
            OR (
                :statusFilter = 'VENCIDA'
                AND fecha_proxima_ejecucion IS NOT NULL
                AND date(datetime(fecha_proxima_ejecucion / 1000, 'unixepoch', 'utc'))
                    <= date(datetime(:todayStart / 1000, 'unixepoch', 'utc'))
            )
            OR (
                :statusFilter = 'OK'
                AND (
                    fecha_proxima_ejecucion IS NULL
                    OR date(datetime(fecha_proxima_ejecucion / 1000, 'unixepoch', 'utc'))
                        > date(datetime(:todayStart / 1000, 'unixepoch', 'utc'))
                )
            )
        )
        AND (
            :datePreset = 'ALL'
            OR (
                :datePreset = 'TODAY'
                AND fecha_proxima_ejecucion >= :todayStart
                AND fecha_proxima_ejecucion <= :todayEnd
            )
            OR (
                :datePreset = 'NEXT_7_DAYS'
                AND fecha_proxima_ejecucion >= :todayStart
                AND fecha_proxima_ejecucion <= :next7DaysEnd
            )
            OR (
                :datePreset = 'NEXT_30_DAYS'
                AND fecha_proxima_ejecucion >= :todayStart
                AND fecha_proxima_ejecucion <= :next30DaysEnd
            )
            OR (
                :datePreset = 'OVERDUE'
                AND fecha_proxima_ejecucion IS NOT NULL
                AND date(datetime(fecha_proxima_ejecucion / 1000, 'unixepoch', 'utc'))
                    <= date(datetime(:todayStart / 1000, 'unixepoch', 'utc'))
            )
        )
        AND (
            :categoryId IS NULL
            OR categoria_id = :categoryId
        )
        ORDER BY
            CASE WHEN :sortOrder = 'DUE_DATE' THEN
                CASE
                    WHEN fecha_proxima_ejecucion IS NOT NULL
                        AND date(datetime(fecha_proxima_ejecucion / 1000, 'unixepoch', 'utc'))
                            <= date(datetime(:todayStart / 1000, 'unixepoch', 'utc'))
                    THEN CAST(
                        julianday(date(datetime(:todayStart / 1000, 'unixepoch', 'utc')))
                        - julianday(date(datetime(fecha_proxima_ejecucion / 1000, 'unixepoch', 'utc')))
                        AS INTEGER
                    )
                    ELSE -1
                END
            END DESC,
            CASE WHEN :sortOrder = 'DUE_DATE' THEN fecha_proxima_ejecucion END ASC,
            CASE WHEN :sortOrder = 'POSTPONED' THEN cantidad_postergaciones END DESC,
            CASE WHEN :sortOrder = 'POSTPONED' THEN fecha_visible_desde END DESC,
            CASE WHEN :sortOrder = 'POSTPONED' THEN fecha_ultima_modificacion END DESC,
            CASE WHEN :sortOrder = 'OLDEST' THEN fecha_creacion END ASC,
            CASE WHEN :sortOrder = 'RECENT' THEN fecha_creacion END DESC,
            fecha_creacion DESC
        """
    )
    fun observePendingByFilterAndSort(
        filter: String,
        sortOrder: String,
        searchQuery: String,
        includeNotesInSearch: Boolean,
        statusFilter: String?,
        datePreset: String,
        categoryId: Long?,
        todayStart: LocalDateTime,
        todayEnd: LocalDateTime,
        next7DaysEnd: LocalDateTime,
        next30DaysEnd: LocalDateTime,
    ): Flow<List<TareaEntity>>

    @Query("SELECT * FROM tarea WHERE fecha_proxima_ejecucion IS NOT NULL")
    suspend fun getPendingReminderTasks(): List<TareaEntity>

    @Query("SELECT COUNT(*) FROM tarea WHERE categoria_id = :categoryId")
    suspend fun countByCategoryId(categoryId: Long): Int

    @Query("SELECT * FROM tarea WHERE categoria_id = :categoryId ORDER BY fecha_creacion DESC")
    suspend fun getByCategoryId(categoryId: Long): List<TareaEntity>

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

