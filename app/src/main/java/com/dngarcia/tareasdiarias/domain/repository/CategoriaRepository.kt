package com.dngarcia.tareasdiarias.domain.repository

import com.dngarcia.tareasdiarias.domain.model.Categoria
import kotlinx.coroutines.flow.Flow

interface CategoriaRepository {
    fun observeAll(): Flow<List<Categoria>>
    suspend fun getById(id: Long): Categoria?
    suspend fun create(categoria: Categoria): Long
    suspend fun update(categoria: Categoria)
    suspend fun deleteById(id: Long)
    suspend fun existsByNombre(nombre: String, excludeId: Long? = null): Boolean
}

