package com.dngarcia.tareasdiarias.domain.usecase

import com.dngarcia.tareasdiarias.domain.model.TaskReminder
import com.dngarcia.tareasdiarias.domain.model.Tarea
import com.dngarcia.tareasdiarias.domain.repository.TareaRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

data class UpdateTaskParams(
    val taskId: Long,
    val nombre: String,
    val subtitulo: String = "",
    val categoriaId: Long,
    val notas: String,
    val diasPeriodicidad: Int?,
    val periodicidad: com.dngarcia.tareasdiarias.domain.model.Periodicidad,
    val fechaInicio: LocalDate,
    val horaRecordatorio: LocalTime?,
)

class UpdateTaskUseCase @Inject constructor(
    private val tareaRepository: TareaRepository,
    private val scheduleTaskReminderUseCase: ScheduleTaskReminderUseCase,
    private val cancelTaskReminderUseCase: CancelTaskReminderUseCase,
) {
    suspend operator fun invoke(params: UpdateTaskParams) {
        val currentTask = tareaRepository.getById(params.taskId) ?: return
        val now = LocalDateTime.now()
        val scheduleDefinitionChanged = currentTask.tipoPeriodicidad != params.periodicidad ||
            currentTask.diasPeriodicidad != params.diasPeriodicidad ||
            currentTask.fechaInicio != params.fechaInicio
        val nextExecutionAt = if (scheduleDefinitionChanged) {
            TaskReminderPolicy.calculateNextExecutionAt(
                periodicidad = params.periodicidad,
                diasPeriodicidad = params.diasPeriodicidad,
                fechaInicio = params.fechaInicio,
                referenceDate = now.toLocalDate(),
            )
        } else {
            currentTask.fechaProximaEjecucion
        }
        val visibleFrom = if (scheduleDefinitionChanged) {
            TaskTimelinePolicy.preserveVisibleFromOnUpdate(
                currentVisibleFrom = currentTask.fechaVisibleDesde,
                recalculatedExpectedCycleAt = nextExecutionAt,
                today = now.toLocalDate(),
            )
        } else {
            currentTask.fechaVisibleDesde
        }

        val updatedTask = currentTask.copy(
            nombre = params.nombre.trim(),
            subtitulo = params.subtitulo.trim(),
            categoriaId = params.categoriaId,
            notas = params.notas.trim(),
            tipoPeriodicidad = params.periodicidad,
            diasPeriodicidad = params.diasPeriodicidad,
            fechaInicio = params.fechaInicio,
            fechaUltimaModificacion = now,
            fechaProximaEjecucion = nextExecutionAt,
            fechaVisibleDesde = visibleFrom,
            horaRecordatorio = params.horaRecordatorio,
        )
        tareaRepository.update(updatedTask)

        val reminderAt = TaskReminderPolicy.calculateReminderAt(
            periodicidad = updatedTask.tipoPeriodicidad,
            diasPeriodicidad = updatedTask.diasPeriodicidad,
            fechaInicio = updatedTask.fechaInicio,
            fechaProximaEjecucion = updatedTask.fechaProximaEjecucion,
            fechaVisibleDesde = updatedTask.fechaVisibleDesde,
            horaRecordatorio = updatedTask.horaRecordatorio,
            now = now,
        )

        if (reminderAt == null) {
            cancelTaskReminderUseCase(params.taskId)
        } else {
            scheduleTaskReminderUseCase(
                reminder = TaskReminder(
                    taskId = params.taskId,
                    taskTitle = updatedTask.nombre,
                    reminderAt = reminderAt,
                    requiresExactScheduling = TaskReminderPolicy.requiresExactAlarm(updatedTask.horaRecordatorio),
                ),
            )
        }
    }
}
