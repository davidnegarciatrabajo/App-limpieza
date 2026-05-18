package com.dngarcia.tareasdiarias.presentation.tomorrow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dngarcia.tareasdiarias.domain.usecase.CompleteTaskUseCase
import com.dngarcia.tareasdiarias.domain.usecase.ObserveTomorrowTasksUseCase
import com.dngarcia.tareasdiarias.domain.usecase.PostponeTaskUseCase
import com.dngarcia.tareasdiarias.domain.usecase.UndoTaskCompletionUseCase
import com.dngarcia.tareasdiarias.presentation.common.TaskStatusItemUiModel
import com.dngarcia.tareasdiarias.presentation.common.UserError
import com.dngarcia.tareasdiarias.presentation.common.toUserError
import com.dngarcia.tareasdiarias.presentation.today.DayAgendaTaskUiModel
import com.dngarcia.tareasdiarias.presentation.today.DayAgendaUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class TomorrowViewModel @Inject constructor(
    observeTomorrowTasksUseCase: ObserveTomorrowTasksUseCase,
    private val completeTaskUseCase: CompleteTaskUseCase,
    private val undoTaskCompletionUseCase: UndoTaskCompletionUseCase,
    private val postponeTaskUseCase: PostponeTaskUseCase,
) : ViewModel() {
    private val userError = MutableStateFlow<UserError?>(null)
    private val refreshSignal = MutableStateFlow(0)

    private val tasksFlow = refreshSignal.flatMapLatest {
        observeTomorrowTasksUseCase().map { tasks ->
            tasks.map { task ->
                DayAgendaTaskUiModel(
                    item = TaskStatusItemUiModel(
                        task = task.task,
                        status = task.status,
                        daysDelta = task.daysDelta,
                    ),
                    categoryName = task.categoryName,
                    completedOnAgendaDay = task.completedToday,
                )
            }
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
