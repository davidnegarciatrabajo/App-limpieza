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
        val now = LocalDateTime.now()
        val today = LocalDate.now()
        return tareaDao.observePendingByFilterAndSort(
            filter = filter.name,
            sortOrder = sortOrder.name,
            searchQuery = searchQuery.trim(),
            includeNotesInSearch = includeNotesInSearch,
            statusFilter = advancedFilters.status?.name,
            datePreset = advancedFilters.datePreset.name,
            categoryId = advancedFilters.categoryId,
            upcomingThreshold = now.plusHours(24),
            todayStart = today.atStartOfDay(),
            todayEnd = today.plusDays(1).atStartOfDay().minusNanos(1),
            next7DaysEnd = now.plusDays(7),
            next30DaysEnd = now.plusDays(30),
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

    override suspend fun existsByNombre(nombre: String, excludeId: Long?): Boolean {
        return tareaDao.existsByNombre(nombre = nombre, excludeId = excludeId)
    }
}

