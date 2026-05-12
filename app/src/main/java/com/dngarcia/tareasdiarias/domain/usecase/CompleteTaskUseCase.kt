package com.dngarcia.tareasdiarias.domain.usecase

import com.dngarcia.tareasdiarias.domain.model.Ejecucion
import com.dngarcia.tareasdiarias.domain.model.TaskReminder
import com.dngarcia.tareasdiarias.domain.repository.EjecucionRepository
import com.dngarcia.tareasdiarias.domain.repository.TareaRepository
import javax.inject.Inject
import java.time.LocalDateTime

class CompleteTaskUseCase @Inject constructor(
    private val tareaRepository: TareaRepository,
    private val ejecucionRepository: EjecucionRepository,
    private val scheduleTaskReminderUseCase: ScheduleTaskReminderUseCase,
    private val cancelTaskReminderUseCase: CancelTaskReminderUseCase,
) {
    suspend operator fun invoke(
        taskId: Long,
        completedAt: LocalDateTime = LocalDateTime.now(),
    ) {
        val task = tareaRepository.getById(taskId) ?: return
        val resolvedCycleDate = TaskTimelinePolicy.expectedCycleDate(task) ?: completedAt.toLocalDate()
        val dayStart = completedAt.toLocalDate().atStartOfDay()
        val dayEnd = completedAt.toLocalDate().plusDays(1).atStartOfDay().minusNanos(1)
        val existingExecution = ejecucionRepository.getLatestCompletedBetween(
            tareaId = taskId,
            startInclusive = dayStart,
            endInclusive = dayEnd,
        )
        if (existingExecution != null) return

        ejecucionRepository.create(
            ejecucion = Ejecucion(
                id = 0L,
                tareaId = taskId,
                fechaEjecucion = completedAt,
                fechaCicloResuelto = resolvedCycleDate,
                completadaPorUsuario = true,
                cantidadPostergacionesPrevias = task.cantidadPostergaciones,
            ),
        )

        val nextExecutionAt = TaskReminderPolicy.calculateNextExecutionAfterResolution(
            periodicidad = task.tipoPeriodicidad,
            diasPeriodicidad = task.diasPeriodicidad,
            fechaInicio = task.fechaInicio,
            resolvedAt = completedAt.toLocalDate(),
        )
        val updatedTask = task.copy(
            fechaUltimaModificacion = completedAt,
            fechaProximaEjecucion = nextExecutionAt,
            fechaVisibleDesde = TaskTimelinePolicy.defaultVisibleFrom(nextExecutionAt),
            ultimaVezQueHiceLaTarea = completedAt,
            cantidadPostergaciones = 0,
        )
        tareaRepository.update(updatedTask)

        val reminderAt = TaskReminderPolicy.calculateReminderAt(
            periodicidad = updatedTask.tipoPeriodicidad,
            diasPeriodicidad = updatedTask.diasPeriodicidad,
            fechaInicio = updatedTask.fechaInicio,
            fechaProximaEjecucion = updatedTask.fechaProximaEjecucion,
            fechaVisibleDesde = updatedTask.fechaVisibleDesde,
            horaRecordatorio = updatedTask.horaRecordatorio,
            now = completedAt,
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
