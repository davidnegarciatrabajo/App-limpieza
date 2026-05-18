package com.dngarcia.tareasdiarias.presentation.topten

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dngarcia.tareasdiarias.domain.usecase.CompleteTaskUseCase
import com.dngarcia.tareasdiarias.domain.usecase.ObserveCategoriasUseCase
import com.dngarcia.tareasdiarias.domain.usecase.ObserveTodayTasksUseCase
import com.dngarcia.tareasdiarias.domain.usecase.ObserveTopPendingTasksUseCase
import com.dngarcia.tareasdiarias.domain.usecase.PostponeTaskUseCase
import com.dngarcia.tareasdiarias.domain.usecase.TodayWidgetTask
import com.dngarcia.tareasdiarias.domain.usecase.UndoTaskCompletionUseCase
import com.dngarcia.tareasdiarias.domain.model.Tarea
import com.dngarcia.tareasdiarias.presentation.common.TaskStatusItemUiModel
import com.dngarcia.tareasdiarias.presentation.common.UserError
import com.dngarcia.tareasdiarias.presentation.common.toTaskStatusItemUiModel
import com.dngarcia.tareasdiarias.presentation.common.toUserError
import com.dngarcia.tareasdiarias.presentation.today.DayAgendaTaskUiModel
import com.dngarcia.tareasdiarias.presentation.today.DayAgendaUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class TopTenViewModel @Inject constructor(
    observeTopPendingTasksUseCase: ObserveTopPendingTasksUseCase,
    observeTodayTasksUseCase: ObserveTodayTasksUseCase,
    observeCategoriasUseCase: ObserveCategoriasUseCase,
    private val completeTaskUseCase: CompleteTaskUseCase,
    private val undoTaskCompletionUseCase: UndoTaskCompletionUseCase,
    private val postponeTaskUseCase: PostponeTaskUseCase,
) : ViewModel() {
    private val userError = MutableStateFlow<UserError?>(null)
    private val refreshSignal = MutableStateFlow(0)

    private val tasksFlow = refreshSignal.flatMapLatest {
        combine(
            observeTopPendingTasksUseCase(limit = 10),
            observeTodayTasksUseCase(),
            observeCategoriasUseCase(),
        ) { topTasks, todayTasks, categorias ->
            mapTopTenRows(
                topTasks = topTasks,
                todayById = todayTasks.associateBy { it.task.id },
                categoriasById = categorias.associate { it.id to it.nombre },
                now = LocalDateTime.now(),
            )
        }.catch { throwable ->
            userError.value = throwable.toUserError()
            emit(emptyList())
        }
    }

    val uiState: StateFlow<DayAgendaUiState> = combine(
        tasksFlow,
        userError,
    ) { tasks, err ->
        DayAgendaUiState(
            tasks = tasks,
            userError = err,
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DayAgendaUiState(),
    )

    fun dismissUserError() {
        userError.value = null
    }

    fun retryLoadTasks() {
        userError.value = null
        refreshSignal.value = refreshSignal.value + 1
    }

    fun completeTask(taskId: Long) {
        viewModelScope.launch {
            runCatching {
                completeTaskUseCase(taskId = taskId)
            }.onFailure { throwable ->
                userError.value = throwable.toUserError()
            }
        }
    }

    fun undoTask(taskId: Long) {
        viewModelScope.launch {
            runCatching {
                undoTaskCompletionUseCase(taskId = taskId)
            }.onFailure { throwable ->
                userError.value = throwable.toUserError()
            }
        }
    }

    fun postponeTask(taskId: Long, postponedUntil: LocalDate) {
        viewModelScope.launch {
            runCatching {
                postponeTaskUseCase(
                    taskId = taskId,
                    postponedUntil = postponedUntil,
                )
            }.onFailure { throwable ->
                userError.value = throwable.toUserError()
            }
        }
    }
}

private fun mapTopTenRows(
    topTasks: List<Tarea>,
    todayById: Map<Long, TodayWidgetTask>,
    categoriasById: Map<Long, String>,
    now: LocalDateTime,
): List<DayAgendaTaskUiModel> {
    return topTasks.map { tarea ->
        val tw = todayById[tarea.id]
        val item = if (tw != null) {
            TaskStatusItemUiModel(
                task = tarea,
                status = tw.status,
                daysDelta = tw.daysDelta,
            )
        } else {
            tarea.toTaskStatusItemUiModel(now)
        }
        DayAgendaTaskUiModel(
            item = item,
            categoryName = categoriasById[tarea.categoriaId].orEmpty(),
            completedOnAgendaDay = tw?.completedToday == true,
        )
    }
}
