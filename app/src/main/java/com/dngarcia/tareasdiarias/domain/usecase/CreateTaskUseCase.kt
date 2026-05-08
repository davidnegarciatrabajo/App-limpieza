package com.dngarcia.tareasdiarias.domain.usecase

import com.dngarcia.tareasdiarias.domain.model.EstadoAlerta
import com.dngarcia.tareasdiarias.domain.model.Periodicidad
import com.dngarcia.tareasdiarias.domain.model.TaskReminder
import com.dngarcia.tareasdiarias.domain.model.Tarea
import com.dngarcia.tareasdiarias.domain.repository.TareaRepository
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

data class CreateTaskParams(
    val nombre: String,
    val subtitulo: String = "",
    val categoriaId: Long,
    val periodicidad: Periodicidad,
    val diasPeriodicidad: Int?,
    val notas: String,
    val horaRecordatorio: LocalTime?,
)

class CreateTaskUseCase @Inject constructor(
    private val tareaRepository: TareaRepository,
    private val scheduleTaskReminderUseCase: ScheduleTaskReminderUseCase,
) {
    suspend operator fun invoke(params: CreateTaskParams): Long {
        val now = LocalDateTime.now()
        val reminderAt = TaskReminderPolicy.calculateReminderAt(
            periodicidad = params.periodicidad,
            diasPeriodicidad = params.diasPeriodicidad,
            baseDateTime = now,
            horaRecordatorio = params.horaRecordatorio,
        )
        val taskId = tareaRepository.create(
            tarea = Tarea(
                id = 0L,
                nombre = params.nombre.trim(),
                subtitulo = params.subtitulo.trim(),
                categoriaId = params.categoriaId,
                tipoPeriodicidad = params.periodicidad,
                diasPeriodicidad = params.diasPeriodicidad,
                notas = params.notas.trim(),
                fechaCreacion = now,
                fechaUltimaModificacion = now,
                fechaProximaEjecucion = reminderAt,
                horaRecordatorio = params.horaRecordatorio,
                cantidadPostergaciones = 0,
                estadoAlerta = EstadoAlerta.NORMAL,
                mensajeAlerta = null,
            ),
        )

        if (reminderAt != null) {
            scheduleTaskReminderUseCase(
                reminder = TaskReminder(
                    taskId = taskId,
                    taskTitle = params.nombre.trim(),
                    reminderAt = reminderAt,
                    requiresExactScheduling = TaskReminderPolicy.requiresExactAlarm(params.periodicidad),
                ),
            )
        }

        return taskId
    }
}
