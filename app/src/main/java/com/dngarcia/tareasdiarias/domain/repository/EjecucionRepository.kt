package com.dngarcia.tareasdiarias.domain.repository

import com.dngarcia.tareasdiarias.domain.model.Ejecucion
import kotlinx.coroutines.flow.Flow

interface EjecucionRepository {
    fun observeByTareaId(tareaId: Long): Flow<List<Ejecucion>>
    suspend fun create(ejecucion: Ejecucion): Long
    suspend fun deleteById(id: Long)
    suspend fun deleteByTareaId(tareaId: Long)
}

