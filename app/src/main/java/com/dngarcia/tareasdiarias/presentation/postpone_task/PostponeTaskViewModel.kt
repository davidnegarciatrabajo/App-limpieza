package com.dngarcia.tareasdiarias.presentation.postpone_task

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dngarcia.tareasdiarias.presentation.navigation.AppRoute
import com.dngarcia.tareasdiarias.domain.usecase.GetTaskByIdUseCase
import com.dngarcia.tareasdiarias.domain.usecase.PostponeTaskUseCase
import com.dngarcia.tareasdiarias.presentation.common.UserError
import com.dngarcia.tareasdiarias.presentation.common.toUserError
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PostponeTaskUiState(
    val taskId: Long = -1L,
    val taskName: String = "",
    val isLoading: Boolean = true,
    val userError: UserError? = null,
)

@HiltViewModel
class PostponeTaskViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getTaskByIdUseCase: GetTaskByIdUseCase,
    private val postponeTaskUseCase: PostponeTaskUseCase,
) : ViewModel() {
    private val taskId: Long = savedStateHandle[AppRoute.TASK_ID_ARG] ?: -1L

    private val _uiState = MutableStateFlow(PostponeTaskUiState(taskId = taskId))
    val uiState: StateFlow<PostponeTaskUiState> = _uiState.asStateFlow()

    private val _finishEvent = MutableSharedFlow<Unit>()
    val finishEvent: SharedFlow<Unit> = _finishEvent.asSharedFlow()

    init {
        loadTask()
    }

    private fun loadTask() {
        viewModelScope.launch {
            runCatching {
                getTaskByIdUseCase(taskId)
            }.onSuccess { task ->
                _uiState.update {
                    it.copy(
                        taskName = task?.nombre.orEmpty(),
                        isLoading = false,
                        userError = if (task == null) UserError(com.dngarcia.tareasdiarias.R.string.task_not_found) else null,
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        userError = throwable.toUserError(),
                    )
                }
            }
        }
    }

    fun postponeTask(postponedUntil: LocalDate) {
        viewModelScope.launch {
            runCatching {
                postponeTaskUseCase(taskId = taskId, postponedUntil = postponedUntil)
            }.onSuccess {
                _finishEvent.emit(Unit)
            }.onFailure { throwable ->
                _uiState.update { it.copy(userError = throwable.toUserError()) }
            }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(userError = null) }
    }
}
