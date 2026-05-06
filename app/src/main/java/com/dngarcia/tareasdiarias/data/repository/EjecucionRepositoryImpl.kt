package com.dngarcia.tareasdiarias.data.repository

import com.dngarcia.tareasdiarias.data.local.dao.EjecucionDao
import com.dngarcia.tareasdiarias.domain.model.Ejecucion
import com.dngarcia.tareasdiarias.domain.repository.EjecucionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class EjecucionRepositoryImpl @Inject constructor(
    private val ejecucionDao: EjecucionDao,
) : EjecucionRepository {
    override fun observeByTareaId(tareaId: Long): Flow<List<Ejecucion>> {
        return ejecucionDao.observeByTareaId(tareaId).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun create(ejecucion: Ejecucion): Long = ejecucionDao.insert(ejecucion.toEntity())

    override suspend fun deleteById(id: Long) {
        ejecucionDao.deleteById(id)
    }

    override suspend fun deleteByTareaId(tareaId: Long) {
        ejecucionDao.deleteByTareaId(tareaId)
    }
}

