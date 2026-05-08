package com.dngarcia.tareasdiarias.domain.usecase

import com.dngarcia.tareasdiarias.domain.model.TaskReminder
import com.dngarcia.tareasdiarias.domain.model.Tarea
import com.dngarcia.tareasdiarias.domain.repository.TareaRepository
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
        val reminderAt = TaskReminderPolicy.calculateReminderAt(
            periodicidad = params.periodicidad,
            diasPeriodicidad = params.diasPeriodicidad,
            baseDateTime = now,
            horaRecordatorio = params.horaRecordatorio,
        )
        val shouldIncrementPostponements = PostponementPolicy.shouldIncrement(
            currentDueDate = currentTask.fechaProximaEjecucion,
            updatedDueDate = reminderAt,
        )

        val updatedTask = currentTask.copy(
            nombre = params.nombre.trim(),
            subtitulo = params.subtitulo.trim(),
            categoriaId = params.categoriaId,
            notas = params.notas.trim(),
            tipoPeriodicidad = params.periodicidad,
            diasPeriodicidad = params.diasPeriodicidad,
            fechaUltimaModificacion = now,
            fechaProximaEjecucion = reminderAt,
            horaRecordatorio = params.horaRecordatorio,
            cantidadPostergaciones = if (shouldIncrementPostponements) {
                currentTask.cantidadPostergaciones + 1
            } else {
                currentTask.cantidadPostergaciones
            },
        )
        tareaRepository.update(updatedTask)

        if (reminderAt == null) {
            cancelTaskReminderUseCase(params.taskId)
        } else {
            scheduleTaskReminderUseCase(
                reminder = TaskReminder(
                    taskId = params.taskId,
                    taskTitle = updatedTask.nombre,
                    reminderAt = reminderAt,
                    requiresExactScheduling = TaskReminderPolicy.requiresExactAlarm(params.periodicidad),
                ),
            )
        }
    }
}
