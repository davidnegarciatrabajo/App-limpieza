package com.dngarcia.tareasdiarias.presentation.common

import com.dngarcia.tareasdiarias.domain.model.TaskStatus
import com.dngarcia.tareasdiarias.domain.model.Tarea
import com.dngarcia.tareasdiarias.domain.usecase.TaskStatusResolver
import java.time.LocalDateTime

data class TaskStatusItemUiModel(
    val task: Tarea,
    val status: TaskStatus,
    val daysDelta: Long?,
)

fun Tarea.toTaskStatusItemUiModel(now: LocalDateTime = LocalDateTime.now()): TaskStatusItemUiModel {
    val statusInfo = TaskStatusResolver.resolve(task = this, now = now)
    return TaskStatusItemUiModel(
        task = this,
        status = statusInfo.status,
        daysDelta = statusInfo.daysDelta,
    )
}
