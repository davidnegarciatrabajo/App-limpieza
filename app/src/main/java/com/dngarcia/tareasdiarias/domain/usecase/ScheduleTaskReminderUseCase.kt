package com.dngarcia.tareasdiarias.domain.usecase

import com.dngarcia.tareasdiarias.domain.model.TaskReminder
import com.dngarcia.tareasdiarias.domain.repository.TaskReminderScheduler
import javax.inject.Inject

class ScheduleTaskReminderUseCase @Inject constructor(
    private val taskReminderScheduler: TaskReminderScheduler,
) {
    suspend operator fun invoke(reminder: TaskReminder) {
        taskReminderScheduler.schedule(reminder)
    }
}
