package com.dngarcia.tareasdiarias.presentation.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dngarcia.tareasdiarias.domain.model.TaskReminder
import com.dngarcia.tareasdiarias.domain.model.TaskPeriodicityFilter
import com.dngarcia.tareasdiarias.domain.model.TaskSortOrder
import com.dngarcia.tareasdiarias.domain.usecase.CancelTaskReminderUseCase
import com.dngarcia.tareasdiarias.domain.usecase.ObserveCategoriasUseCase
import com.dngarcia.tareasdiarias.domain.usecase.ObservePendingTasksUseCase
import com.dngarcia.tareasdiarias.domain.usecase.ScheduleTaskReminderUseCase
import com.dngarcia.tareasdiarias.presentation.common.TaskStatusItemUiModel
import com.dngarcia.tareasdiarias.presentation.common.UserError
import com.dngarcia.tareasdiarias.presentation.common.toTaskStatusItemUiModel
import com.dngarcia.tareasdiarias.presentation.common.toUserError
import dagger.hilt.android.lifecycle.HiltViewModel
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
import java.time.LocalDateTime

data class TodayUiState(
    val tasks: List<TodayTaskUiModel> = emptyList(),
    val userError: UserError? = null,
    val isLoading: Boolean = true,
)

data class TodayTaskUiModel(
    val item: TaskStatusItemUiModel,
    val categoryName: String,
)

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class TodayViewModel @Inject constructor(
    observePendingTasksUseCase: ObservePendingTasksUseCase,
    observeCategoriasUseCase: ObserveCategoriasUseCase,
    private val scheduleTaskReminderUseCase: ScheduleTaskReminderUseCase,
    private val cancelTaskReminderUseCase: CancelTaskReminderUseCase,
) : ViewModel() {
    private val userError = MutableStateFlow<UserError?>(null)
    private val refreshSignal = MutableStateFlow(0)

    private val tasksFlow = refreshSignal.flatMapLatest {
        observePendingTasksUseCase(
            filter = TaskPeriodicityFilter.ALL,
            sortOrder = TaskSortOrder.HIGHEST_DELAY,
        ).catch { throwable ->
            userError.value = throwable.toUserError()
            emit(emptyList())
        }
    }

    val uiState: StateFlow<TodayUiState> = combine(
        tasksFlow.map { tasks -> tasks.map { it.toTaskStatusItemUiModel() } },
        observeCategoriasUseCase(),
        userError,
    ) { tasks, categories, err ->
        val categoriesById = categories.associateBy { it.id }
        TodayUiState(
            tasks = tasks.map { task ->
                TodayTaskUiModel(
                    item = task,
                    categoryName = categoriesById[task.task.categoriaId]?.nombre.orEmpty(),
                )
            },
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

    fun upsertTaskReminder(
        taskId: Long,
        taskTitle: String,
        reminderAt: LocalDateTime?,
    ) {
        viewModelScope.launch {
            if (reminderAt == null) {
                cancelTaskReminderUseCase(taskId)
                return@launch
            }

            scheduleTaskReminderUseCase(
                reminder = TaskReminder(
                    taskId = taskId,
                    taskTitle = taskTitle,
                    reminderAt = reminderAt,
                    requiresExactScheduling = false,
                ),
            )
        }
    }

    fun deleteTask(taskId: Long) {
        viewModelScope.launch {
            cancelTaskReminderUseCase(taskId)
        }
    }
}
