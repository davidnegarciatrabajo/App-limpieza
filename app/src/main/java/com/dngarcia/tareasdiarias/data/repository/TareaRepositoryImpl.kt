package com.dngarcia.tareasdiarias.data.repository

import com.dngarcia.tareasdiarias.data.local.dao.TareaDao
import com.dngarcia.tareasdiarias.domain.model.Tarea
import com.dngarcia.tareasdiarias.domain.repository.TareaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TareaRepositoryImpl @Inject constructor(
    private val tareaDao: TareaDao,
) : TareaRepository {
    override fun observeAll(): Flow<List<Tarea>> {
        return tareaDao.observeAll().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getById(id: Long): Tarea? {
        return tareaDao.getById(id)?.toDomain()
    }

    override suspend fun create(tarea: Tarea): Long = tareaDao.insert(tarea.toEntity())

    override suspend fun update(tarea: Tarea) {
        tareaDao.update(tarea.toEntity())
    }

    override suspend fun deleteById(id: Long) {
        tareaDao.deleteById(id)
    }

    override suspend fun existsByNombre(nombre: String, excludeId: Long?): Boolean {
        return tareaDao.existsByNombre(nombre = nombre, excludeId = excludeId)
    }
}

