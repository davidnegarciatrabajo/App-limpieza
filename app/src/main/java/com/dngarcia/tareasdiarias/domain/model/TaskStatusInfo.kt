package com.dngarcia.tareasdiarias.domain.model

data class TaskStatusInfo(
    val status: TaskStatus,
    val daysDelta: Long?,
)
