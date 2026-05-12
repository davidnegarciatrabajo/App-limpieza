package com.dngarcia.tareasdiarias.domain.repository

import com.dngarcia.tareasdiarias.domain.model.Ejecucion
import kotlinx.coroutines.flow.Flow

interface EjecucionRepository {
    fun observeByTareaId(tareaId: Long): Flow<List<Ejecucion>>
    suspend fun getCompletedBetween(
        startInclusive: java.time.LocalDateTime,
        endInclusive: java.time.LocalDateTime,
    ): List<Ejecucion>
    suspend fun getLatestCompletedBetween(
        tareaId: Long,
        startInclusive: java.time.LocalDateTime,
        endInclusive: java.time.LocalDateTime,
    ): Ejecucion?
    suspend fun create(ejecucion: Ejecucion): Long
    suspend fun deleteById(id: Long)
    suspend fun deleteByTareaId(tareaId: Long)
}

