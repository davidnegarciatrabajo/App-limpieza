package com.dngarcia.tareasdiarias.presentation.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dngarcia.tareasdiarias.domain.usecase.CompleteTaskUseCase
import com.dngarcia.tareasdiarias.domain.usecase.ObserveTodayTasksUseCase
import com.dngarcia.tareasdiarias.domain.usecase.PostponeTaskUseCase
import com.dngarcia.tareasdiarias.domain.usecase.UndoTaskCompletionUseCase
import com.dngarcia.tareasdiarias.presentation.common.TaskStatusItemUiModel
import com.dngarcia.tareasdiarias.presentation.common.UserError
import com.dngarcia.tareasdiarias.presentation.common.toUserError
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

data class TodayUiState(
    val tasks: List<TodayTaskUiModel> = emptyList(),
    val userError: UserError? = null,
    val isLoading: Boolean = true,
)

data class TodayTaskUiModel(
    val item: TaskStatusItemUiModel,
    val categoryName: String,
    val completedToday: Boolean,
)

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class TodayViewModel @Inject constructor(
    observeTodayTasksUseCase: ObserveTodayTasksUseCase,
    private val completeTaskUseCase: CompleteTaskUseCase,
    private val undoTaskCompletionUseCase: UndoTaskCompletionUseCase,
    private val postponeTaskUseCase: PostponeTaskUseCase,
) : ViewModel() {
    private val userError = MutableStateFlow<UserError?>(null)
    private val refreshSignal = MutableStateFlow(0)

    private val tasksFlow = refreshSignal.flatMapLatest {
        observeTodayTasksUseCase().map { tasks ->
            tasks.map { task ->
                TodayTaskUiModel(
                    item = TaskStatusItemUiModel(
                        task = task.task,
                        status = task.status,
                        daysDelta = task.daysDelta,
                    ),
                    categoryName = task.categoryName,
                    completedToday = task.completedToday,
                )
            }
        }.catch { throwable ->
            userError.value = throwable.toUserError()
            emit(emptyList())
        }
    }

    val uiState: StateFlow<TodayUiState> = combine(
        tasksFlow,
        userError,
    ) { tasks, err ->
        TodayUiState(
            tasks = tasks,
            userError = err,
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TodayUiState(),
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
