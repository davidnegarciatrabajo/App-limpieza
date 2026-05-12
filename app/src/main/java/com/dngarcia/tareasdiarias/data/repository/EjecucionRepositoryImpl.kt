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

    override fun observeCompletedBetween(
        startInclusive: java.time.LocalDateTime,
        endInclusive: java.time.LocalDateTime,
    ): Flow<List<Ejecucion>> {
        return ejecucionDao.observeCompletedBetween(
            startInclusive = startInclusive,
            endInclusive = endInclusive,
        ).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getCompletedBetween(
        startInclusive: java.time.LocalDateTime,
        endInclusive: java.time.LocalDateTime,
    ): List<Ejecucion> {
        return ejecucionDao.getCompletedBetween(
            startInclusive = startInclusive,
            endInclusive = endInclusive,
        ).map { it.toDomain() }
    }

    override suspend fun getLatestCompletedBetween(
        tareaId: Long,
        startInclusive: java.time.LocalDateTime,
        endInclusive: java.time.LocalDateTime,
    ): Ejecucion? {
        return ejecucionDao.getLatestCompletedBetween(
            tareaId = tareaId,
            startInclusive = startInclusive,
            endInclusive = endInclusive,
        )?.toDomain()
    }

    override suspend fun getLatestCompletedForCycle(
        tareaId: Long,
        cycleDate: java.time.LocalDate,
    ): Ejecucion? {
        return ejecucionDao.getLatestCompletedForCycle(
            tareaId = tareaId,
            cycleDate = cycleDate,
        )?.toDomain()
    }

    override suspend fun getLatestCompletedByTaskId(tareaId: Long): Ejecucion? {
        return ejecucionDao.getLatestCompletedByTaskId(tareaId)?.toDomain()
    }

    override suspend fun create(ejecucion: Ejecucion): Long = ejecucionDao.insert(ejecucion.toEntity())

    override suspend fun deleteById(id: Long) {
        ejecucionDao.deleteById(id)
    }

    override suspend fun deleteByTareaId(tareaId: Long) {
        ejecucionDao.deleteByTareaId(tareaId)
    }
}

