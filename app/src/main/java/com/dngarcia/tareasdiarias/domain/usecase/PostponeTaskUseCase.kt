package com.dngarcia.tareasdiarias.domain.usecase

import com.dngarcia.tareasdiarias.domain.model.TaskReminder
import com.dngarcia.tareasdiarias.domain.repository.TareaRepository
import javax.inject.Inject
import java.time.LocalDateTime

class PostponeTaskUseCase @Inject constructor(
    private val tareaRepository: TareaRepository,
    private val scheduleTaskReminderUseCase: ScheduleTaskReminderUseCase,
    private val cancelTaskReminderUseCase: CancelTaskReminderUseCase,
) {
    suspend operator fun invoke(
        taskId: Long,
        referenceTime: LocalDateTime = LocalDateTime.now(),
    ) {
        val task = tareaRepository.getById(taskId) ?: return
        val baseDate = maxOf(
            task.fechaProximaEjecucion?.toLocalDate() ?: referenceTime.toLocalDate(),
            referenceTime.toLocalDate(),
        )
        val updatedDueDate = baseDate.plusDays(1).atStartOfDay()
        val updatedTask = task.copy(
            fechaUltimaModificacion = referenceTime,
            fechaProximaEjecucion = updatedDueDate,
            cantidadPostergaciones = task.cantidadPostergaciones + 1,
        )
        tareaRepository.update(updatedTask)

        val reminderAt = TaskReminderPolicy.calculateReminderAt(
            periodicidad = updatedTask.tipoPeriodicidad,
            diasPeriodicidad = updatedTask.diasPeriodicidad,
            fechaInicio = updatedTask.fechaInicio,
            fechaProximaEjecucion = updatedTask.fechaProximaEjecucion,
            horaRecordatorio = updatedTask.horaRecordatorio,
            now = referenceTime,
        )
        if (reminderAt == null) {
            cancelTaskReminderUseCase(taskId)
        } else {
            scheduleTaskReminderUseCase(
                reminder = TaskReminder(
                    taskId = taskId,
                    taskTitle = updatedTask.nombre,
                    reminderAt = reminderAt,
                    requiresExactScheduling = TaskReminderPolicy.requiresExactAlarm(updatedTask.tipoPeriodicidad),
                ),
            )
        }
    }
}
