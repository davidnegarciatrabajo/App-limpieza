package com.dngarcia.tareasdiarias.presentation.tareas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dngarcia.tareasdiarias.domain.model.Categoria
import com.dngarcia.tareasdiarias.domain.model.TaskAdvancedFilters
import com.dngarcia.tareasdiarias.domain.model.TaskPeriodicityFilter
import com.dngarcia.tareasdiarias.domain.model.TaskSortOrder
import com.dngarcia.tareasdiarias.domain.usecase.DeleteTaskUseCase
import com.dngarcia.tareasdiarias.domain.usecase.ObserveCategoriasUseCase
import com.dngarcia.tareasdiarias.domain.usecase.ObservePendingTasksUseCase
import com.dngarcia.tareasdiarias.domain.usecase.ObserveTopPendingTasksUseCase
import com.dngarcia.tareasdiarias.presentation.common.TaskStatusItemUiModel
import com.dngarcia.tareasdiarias.presentation.common.UserError
import com.dngarcia.tareasdiarias.presentation.common.toTaskStatusItemUiModel
import com.dngarcia.tareasdiarias.presentation.common.toUserError
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TareasUiState(
    val selectedFilter: TaskPeriodicityFilter = TaskPeriodicityFilter.ALL,
    val selectedSortOrder: TaskSortOrder = TaskSortOrder.RECENT,
    val searchQuery: String = "",
    val includeNotesInSearch: Boolean = true,
    val advancedFilters: TaskAdvancedFilters = TaskAdvancedFilters(),
    val categorias: List<Categoria> = emptyList(),
    val topPendingTasks: List<TaskStatusItemUiModel> = emptyList(),
    val filteredTasks: List<TaskStatusItemUiModel> = emptyList(),
    val isLoading: Boolean = true,
    val userError: UserError? = null,
) {
    val hasActiveAdvancedFilters: Boolean = advancedFilters.hasActiveFilters()
    val hasActiveSearch: Boolean = searchQuery.isNotBlank()
}

private data class SearchInputs(
    val periodicityFilter: TaskPeriodicityFilter,
    val sortOrder: TaskSortOrder,
    val query: String,
    val includeNotesInSearch: Boolean,
    val advancedFilters: TaskAdvancedFilters,
)

private const val SEARCH_DEBOUNCE_MS = 300L

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class TareasViewModel @Inject constructor(
    observeTopPendingTasksUseCase: ObserveTopPendingTasksUseCase,
    observeCategoriasUseCase: ObserveCategoriasUseCase,
    private val observePendingTasksUseCase: ObservePendingTasksUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
) : ViewModel() {
    private val selectedFilter = MutableStateFlow(TaskPeriodicityFilter.ALL)
    private val selectedSortOrder = MutableStateFlow(TaskSortOrder.RECENT)
    private val rawSearchQuery = MutableStateFlow("")
    private val includeNotesInSearch = MutableStateFlow(true)
    private val advancedFilters = MutableStateFlow(TaskAdvancedFilters())
    private val userError = MutableStateFlow<UserError?>(null)
    private val refreshSignal = MutableStateFlow(0)
    private val debouncedSearchQuery = rawSearchQuery.debounce(SEARCH_DEBOUNCE_MS)

    private val searchInputs = combine(
        selectedFilter,
        selectedSortOrder,
        debouncedSearchQuery,
        includeNotesInSearch,
        advancedFilters,
    ) { filter, sortOrder, query, includeNotes, filters ->
        SearchInputs(
            periodicityFilter = filter,
            sortOrder = sortOrder,
            query = query,
            includeNotesInSearch = includeNotes,
            advancedFilters = filters,
        )
    }

    private val filteredTasksFlow = combine(searchInputs, refreshSignal) { inputs, _ -> inputs }
        .flatMapLatest { inputs ->
            observePendingTasksUseCase(
                filter = inputs.periodicityFilter,
                sortOrder = inputs.sortOrder,
                searchQuery = inputs.query,
                includeNotesInSearch = inputs.includeNotesInSearch,
                advancedFilters = inputs.advancedFilters,
            ).catch { throwable ->
                userError.value = throwable.toUserError()
                emit(emptyList())
            }
        }

    private val topPendingSafe = refreshSignal.flatMapLatest {
        observeTopPendingTasksUseCase(limit = 10).catch { throwable ->
            userError.value = throwable.toUserError()
            emit(emptyList())
        }
    }

    private val categoriasSafe = refreshSignal.flatMapLatest {
        observeCategoriasUseCase().catch { throwable ->
            userError.value = throwable.toUserError()
            emit(emptyList())
        }
    }

    private val searchUiInputs = combine(
        rawSearchQuery,
        searchInputs,
    ) { query, inputs ->
        query to inputs
    }

    val uiState: StateFlow<TareasUiState> = combine(
        searchUiInputs,
        filteredTasksFlow,
        topPendingSafe,
        categoriasSafe,
        userError,
    ) { searchState, filteredTasks, topTasks, categorias, currentError ->
        val (query, inputs) = searchState
        TareasUiState(
            selectedFilter = inputs.periodicityFilter,
            selectedSortOrder = inputs.sortOrder,
            searchQuery = query,
            includeNotesInSearch = inputs.includeNotesInSearch,
            advancedFilters = inputs.advancedFilters,
            categorias = categorias,
            topPendingTasks = topTasks.map { it.toTaskStatusItemUiModel() },
            filteredTasks = filteredTasks.map { it.toTaskStatusItemUiModel() },
            isLoading = false,
            userError = currentError,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TareasUiState(),
    )

    fun onFilterSelected(filter: TaskPeriodicityFilter) {
        selectedFilter.value = filter
    }

    fun onSortOrderSelected(sortOrder: TaskSortOrder) {
        selectedSortOrder.value = sortOrder
    }

    fun onSearchQueryChange(query: String) {
        rawSearchQuery.value = query
    }

    fun onToggleIncludeNotesInSearch() {
        includeNotesInSearch.value = !includeNotesInSearch.value
    }

    fun onAdvancedFiltersChange(filters: TaskAdvancedFilters) {
        advancedFilters.value = filters
    }

    fun clearAdvancedFilters() {
        advancedFilters.value = TaskAdvancedFilters()
    }

    fun dismissUserError() {
        userError.value = null
    }

    fun retryLoadTasks() {
        userError.value = null
        refreshSignal.value = refreshSignal.value + 1
    }

    fun deleteTask(taskId: Long) {
        viewModelScope.launch {
            runCatching {
                deleteTaskUseCase(taskId)
            }.onFailure { throwable ->
                userError.value = throwable.toUserError()
            }
        }
    }
}
