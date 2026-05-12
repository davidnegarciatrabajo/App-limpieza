package com.dngarcia.tareasdiarias.data.repository

import com.dngarcia.tareasdiarias.data.local.dao.TareaDao
import com.dngarcia.tareasdiarias.domain.model.TaskAdvancedFilters
import com.dngarcia.tareasdiarias.domain.model.TaskDateFilterPreset
import com.dngarcia.tareasdiarias.domain.model.TaskStatus
import com.dngarcia.tareasdiarias.domain.model.Tarea
import com.dngarcia.tareasdiarias.domain.model.TaskPeriodicityFilter
import com.dngarcia.tareasdiarias.domain.model.TaskSortOrder
import com.dngarcia.tareasdiarias.domain.repository.TareaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

class TareaRepositoryImpl @Inject constructor(
    private val tareaDao: TareaDao,
) : TareaRepository {
    override fun observeAll(): Flow<List<Tarea>> {
        return tareaDao.observeAll().map { list -> list.map { it.toDomain() } }
    }

    override fun observeTopPending(limit: Int): Flow<List<Tarea>> {
        return tareaDao.observeTopPending(limit = limit).map { list -> list.map { it.toDomain() } }
    }

    override fun observePendingByFilterAndSort(
        filter: TaskPeriodicityFilter,
        sortOrder: TaskSortOrder,
        searchQuery: String,
        includeNotesInSearch: Boolean,
        advancedFilters: TaskAdvancedFilters,
    ): Flow<List<Tarea>> {
        val today = LocalDate.now()
        val todayStart = today.atStartOfDay()
        val todayEnd = today.plusDays(1).atStartOfDay().minusNanos(1)
        return tareaDao.observePendingByFilterAndSort(
            filter = filter.name,
            sortOrder = sortOrder.name,
            searchQuery = searchQuery.trim(),
            includeNotesInSearch = includeNotesInSearch,
            statusFilter = advancedFilters.status?.name,
            datePreset = advancedFilters.datePreset.name,
            categoryId = advancedFilters.categoryId,
            todayStart = todayStart,
            todayEnd = todayEnd,
            next7DaysEnd = today.plusDays(7).atTime(23, 59, 59, 999_999_999),
            next30DaysEnd = today.plusDays(30).atTime(23, 59, 59, 999_999_999),
        ).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getPendingReminderTasks(): List<Tarea> {
        return tareaDao.getPendingReminderTasks().map { it.toDomain() }
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

    override suspend fun getByCategoryId(categoryId: Long): List<Tarea> {
        return tareaDao.getByCategoryId(categoryId).map { it.toDomain() }
    }

    override suspend fun countByCategoryId(categoryId: Long): Int {
        return tareaDao.countByCategoryId(categoryId)
    }

    override suspend fun existsByNombre(nombre: String, excludeId: Long?): Boolean {
        return tareaDao.existsByNombre(nombre = nombre, excludeId = excludeId)
    }
}

