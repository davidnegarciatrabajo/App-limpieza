package com.dngarcia.tareasdiarias.domain.repository

import com.dngarcia.tareasdiarias.domain.model.TaskReminder

interface TaskReminderScheduler {
    suspend fun schedule(reminder: TaskReminder)
    suspend fun cancel(taskId: Long)
}
