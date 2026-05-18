package com.dngarcia.tareasdiarias.presentation.today

import com.dngarcia.tareasdiarias.presentation.common.TaskStatusItemUiModel
import com.dngarcia.tareasdiarias.presentation.common.UserError

data class DayAgendaUiState(
    val tasks: List<DayAgendaTaskUiModel> = emptyList(),
    val userError: UserError? = null,
    val isLoading: Boolean = true,
)

data class DayAgendaTaskUiModel(
    val item: TaskStatusItemUiModel,
    val categoryName: String,
    val completedOnAgendaDay: Boolean,
)
