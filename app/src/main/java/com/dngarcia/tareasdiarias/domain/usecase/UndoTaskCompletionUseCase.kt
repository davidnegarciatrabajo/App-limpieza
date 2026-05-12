package com.dngarcia.tareasdiarias.domain.usecase

import com.dngarcia.tareasdiarias.domain.model.Periodicidad
import com.dngarcia.tareasdiarias.domain.model.TaskReminder
import com.dngarcia.tareasdiarias.domain.repository.EjecucionRepository
import com.dngarcia.tareasdiarias.domain.repository.TareaRepository
import javax.inject.Inject
import java.time.LocalDate
import java.time.LocalDateTime

class UndoTaskCompletionUseCase @Inject constructor(
    private val tareaRepository: TareaRepository,
    private val ejecucionRepository: EjecucionRepository,
    private val scheduleTaskReminderUseCase: ScheduleTaskReminderUseCase,
    private val cancelTaskReminderUseCase: CancelTaskReminderUseCase,
) {
    suspend operator fun invoke(
        taskId: Long,
        referenceTime: LocalDateTime = LocalDateTime.now(),
    ) {
        val task = tareaRepository.getById(taskId) ?: return
        val dayStart = referenceTime.toLocalDate().atStartOfDay()
        val dayEnd = referenceTime.toLocalDate().plusDays(1).atStartOfDay().minusNanos(1)
        val execution = ejecucionRepository.getLatestCompletedBetween(
            tareaId = taskId,
            startInclusive = dayStart,
            endInclusive = dayEnd,
        ) ?: return

        ejecucionRepository.deleteById(execution.id)

        val restoredDueDate = restoreDueDate(
            periodicidad = task.tipoPeriodicidad,
            diasPeriodicidad = task.diasPeriodicidad,
            currentDueDate = task.fechaProximaEjecucion?.toLocalDate(),
            fallbackDate = execution.fechaEjecucion.toLocalDate(),
        )
        val updatedTask = task.copy(
            fechaUltimaModificacion = referenceTime,
            fechaProximaEjecucion = restoredDueDate?.atStartOfDay(),
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

    private fun restoreDueDate(
        periodicidad: Periodicidad,
        diasPeriodicidad: Int?,
        currentDueDate: LocalDate?,
        fallbackDate: LocalDate,
    ): LocalDate? {
        return when {
            currentDueDate == null -> fallbackDate
            periodicidad == Periodicidad.UNICA -> fallbackDate
            periodicidad == Periodicidad.DIARIA -> currentDueDate.minusDays(1)
            periodicidad == Periodicidad.SEMANAL -> currentDueDate.minusDays(7)
            periodicidad == Periodicidad.MENSUAL -> currentDueDate.minusMonths(1)
            periodicidad == Periodicidad.SEMESTRAL -> currentDueDate.minusMonths(6)
            periodicidad == Periodicidad.PERSONALIZADA -> currentDueDate.minusDays((diasPeriodicidad ?: 1).toLong())
            else -> fallbackDate
        }
    }
}
