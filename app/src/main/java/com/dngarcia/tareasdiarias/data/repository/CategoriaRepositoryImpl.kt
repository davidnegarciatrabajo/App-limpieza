package com.dngarcia.tareasdiarias.data.repository

import com.dngarcia.tareasdiarias.data.local.dao.CategoriaDao
import com.dngarcia.tareasdiarias.domain.model.Categoria
import com.dngarcia.tareasdiarias.domain.repository.CategoriaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CategoriaRepositoryImpl @Inject constructor(
    private val categoriaDao: CategoriaDao,
) : CategoriaRepository {
    override fun observeAll(): Flow<List<Categoria>> {
        return categoriaDao.observeAll().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getById(id: Long): Categoria? {
        return categoriaDao.getById(id)?.toDomain()
    }

    override suspend fun create(categoria: Categoria): Long = categoriaDao.insert(categoria.toEntity())

    override suspend fun update(categoria: Categoria) {
        categoriaDao.update(categoria.toEntity())
    }

    override suspend fun deleteById(id: Long) {
        categoriaDao.deleteById(id)
    }

    override suspend fun existsByNombre(nombre: String, excludeId: Long?): Boolean {
        return categoriaDao.existsByNombre(nombre = nombre, excludeId = excludeId)
    }
}

