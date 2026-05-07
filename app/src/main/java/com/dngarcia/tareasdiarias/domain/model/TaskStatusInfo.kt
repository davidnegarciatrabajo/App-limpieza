package com.dngarcia.tareasdiarias.domain.model

import java.time.LocalDateTime

data class TaskStatusInfo(
    val status: TaskStatus,
    val hoursUntilDue: Long?,
    val daysDelta: Long?,
    val lastModifiedAt: LocalDateTime,
)
