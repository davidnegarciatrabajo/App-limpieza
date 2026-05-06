package com.dngarcia.tareasdiarias.domain.repository

import com.dngarcia.tareasdiarias.domain.model.Tarea
import kotlinx.coroutines.flow.Flow

interface TareaRepository {
    fun observeAll(): Flow<List<Tarea>>
    suspend fun getPendingReminderTasks(): List<Tarea>
    suspend fun getById(id: Long): Tarea?
    suspend fun create(tarea: Tarea): Long
    suspend fun update(tarea: Tarea)
    suspend fun deleteById(id: Long)
    suspend fun existsByNombre(nombre: String, excludeId: Long? = null): Boolean
}

