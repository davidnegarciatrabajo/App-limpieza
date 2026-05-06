package com.dngarcia.tareasdiarias.presentation.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dngarcia.tareasdiarias.domain.model.TaskReminder
import com.dngarcia.tareasdiarias.domain.usecase.CancelTaskReminderUseCase
import com.dngarcia.tareasdiarias.domain.usecase.ScheduleTaskReminderUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDateTime

data class TodayTaskUiModel(
    val id: Int,
    val title: String,
    val isChecked: Boolean
)

data class TodayUiState(
    val tasks: List<TodayTaskUiModel> = emptyList()
)

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val scheduleTaskReminderUseCase: ScheduleTaskReminderUseCase,
    private val cancelTaskReminderUseCase: CancelTaskReminderUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(TodayUiState())
    val uiState: StateFlow<TodayUiState> = _uiState.asStateFlow()

    fun loadMockTasks(titles: List<String>) {
        if (_uiState.value.tasks.isNotEmpty()) return
        _uiState.value = TodayUiState(
            tasks = titles.mapIndexed { index, title ->
                TodayTaskUiModel(
                    id = index,
                    title = title,
                    isChecked = false
                )
            }
        )
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
