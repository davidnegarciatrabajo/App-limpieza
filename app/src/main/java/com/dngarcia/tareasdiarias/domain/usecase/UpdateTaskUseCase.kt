package com.dngarcia.tareasdiarias.domain.usecase

import com.dngarcia.tareasdiarias.domain.model.TaskReminder
import com.dngarcia.tareasdiarias.domain.model.Tarea
import com.dngarcia.tareasdiarias.domain.repository.TareaRepository
import javax.inject.Inject
import java.time.LocalDateTime

data class UpdateTaskParams(
    val taskId: Long,
    val nombre: String,
    val categoriaId: Long,
    val notas: String,
    val diasPeriodicidad: Int?,
    val periodicidad: com.dngarcia.tareasdiarias.domain.model.Periodicidad,
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
        )
        val shouldIncrementPostponements = PostponementPolicy.shouldIncrement(
            currentDueDate = currentTask.fechaProximaEjecucion,
            updatedDueDate = reminderAt,
        )

        val updatedTask = currentTask.copy(
            nombre = params.nombre.trim(),
            categoriaId = params.categoriaId,
            notas = params.notas.trim(),
            tipoPeriodicidad = params.periodicidad,
            diasPeriodicidad = params.diasPeriodicidad,
            fechaUltimaModificacion = now,
            fechaProximaEjecucion = reminderAt,
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
