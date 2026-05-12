package com.dngarcia.tareasdiarias.domain.usecase

import com.dngarcia.tareasdiarias.domain.model.TaskReminder
import com.dngarcia.tareasdiarias.domain.repository.EjecucionRepository
import com.dngarcia.tareasdiarias.domain.repository.TareaRepository
import javax.inject.Inject
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
        val latestRealCompletion = ejecucionRepository.getLatestCompletedByTaskId(taskId)?.fechaEjecucion
        val restoredDueDate = execution.fechaCicloResuelto
            ?: TaskTimelinePolicy.expectedCycleDate(task)
            ?: referenceTime.toLocalDate()
        val updatedTask = task.copy(
            fechaUltimaModificacion = referenceTime,
            fechaProximaEjecucion = restoredDueDate.atStartOfDay(),
            fechaVisibleDesde = restoredDueDate,
            ultimaVezQueHiceLaTarea = latestRealCompletion,
            cantidadPostergaciones = execution.cantidadPostergacionesPrevias,
        )
        tareaRepository.update(updatedTask)

        val reminderAt = TaskReminderPolicy.calculateReminderAt(
            periodicidad = updatedTask.tipoPeriodicidad,
            diasPeriodicidad = updatedTask.diasPeriodicidad,
            fechaInicio = updatedTask.fechaInicio,
            fechaProximaEjecucion = updatedTask.fechaProximaEjecucion,
            fechaVisibleDesde = updatedTask.fechaVisibleDesde,
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
                    requiresExactScheduling = TaskReminderPolicy.requiresExactAlarm(updatedTask.horaRecordatorio),
                ),
            )
        }
    }
}
