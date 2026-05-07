package com.dngarcia.tareasdiarias.domain.model

import java.time.LocalDateTime

data class TaskReminder(
    val taskId: Long,
    val taskTitle: String,
    val reminderAt: LocalDateTime,
    val requiresExactScheduling: Boolean,
)
