package com.dngarcia.tareasdiarias.domain.usecase

import com.dngarcia.tareasdiarias.domain.model.TaskReminder
import com.dngarcia.tareasdiarias.domain.repository.TareaRepository
import com.dngarcia.tareasdiarias.domain.repository.TaskReminderScheduler
import javax.inject.Inject

class ReschedulePendingRemindersUseCase @Inject constructor(
    private val tareaRepository: TareaRepository,
    private val taskReminderScheduler: TaskReminderScheduler,
) {
    suspend operator fun invoke() {
        val pendingTasks = tareaRepository.getPendingReminderTasks()
        pendingTasks.forEach { task ->
            val reminderAt = task.fechaProximaEjecucion ?: return@forEach
            taskReminderScheduler.schedule(
                reminder = TaskReminder(
                    taskId = task.id,
                    taskTitle = task.nombre,
                    reminderAt = reminderAt,
                ),
            )
        }
    }
}
