package com.dngarcia.tareasdiarias.domain.usecase

import com.dngarcia.tareasdiarias.domain.model.Tarea
import com.dngarcia.tareasdiarias.domain.model.TaskAdvancedFilters
import com.dngarcia.tareasdiarias.domain.model.TaskPeriodicityFilter
import com.dngarcia.tareasdiarias.domain.model.TaskSortOrder
import com.dngarcia.tareasdiarias.domain.repository.TareaRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObservePendingTasksUseCase @Inject constructor(
    private val tareaRepository: TareaRepository,
) {
    operator fun invoke(
        filter: TaskPeriodicityFilter,
        sortOrder: TaskSortOrder,
        searchQuery: String = "",
        includeNotesInSearch: Boolean = true,
        advancedFilters: TaskAdvancedFilters = TaskAdvancedFilters(),
    ): Flow<List<Tarea>> {
        return tareaRepository.observePendingByFilterAndSort(
            filter = filter,
            sortOrder = sortOrder,
            searchQuery = searchQuery,
            includeNotesInSearch = includeNotesInSearch,
            advancedFilters = advancedFilters,
        )
    }
}
