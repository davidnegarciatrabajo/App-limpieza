package com.dngarcia.tareasdiarias.domain.usecase

import com.dngarcia.tareasdiarias.domain.repository.TaskReminderScheduler
import javax.inject.Inject

class CancelTaskReminderUseCase @Inject constructor(
    private val taskReminderScheduler: TaskReminderScheduler,
) {
    suspend operator fun invoke(taskId: Long) {
        taskReminderScheduler.cancel(taskId)
    }
}
