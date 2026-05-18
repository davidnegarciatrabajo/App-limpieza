package com.dngarcia.tareasdiarias.domain.usecase

import com.dngarcia.tareasdiarias.domain.model.EstadoAlerta
import com.dngarcia.tareasdiarias.domain.model.ModoProximoCiclo
import com.dngarcia.tareasdiarias.domain.model.Periodicidad
import com.dngarcia.tareasdiarias.domain.model.TaskReminder
import com.dngarcia.tareasdiarias.domain.model.Tarea
import java.time.LocalDate
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
    val fechaInicio: LocalDate,
    val horaRecordatorio: LocalTime?,
    val modoProximoCiclo: ModoProximoCiclo = ModoProximoCiclo.ANCLADO_FECHA_INICIO,
)

class CreateTaskUseCase @Inject constructor(
    private val tareaRepository: TareaRepository,
    private val scheduleTaskReminderUseCase: ScheduleTaskReminderUseCase,
) {
    suspend operator fun invoke(params: CreateTaskParams): Long {
        val now = LocalDateTime.now()
        val nextExecutionAt = TaskReminderPolicy.calculateNextExecutionAt(
            periodicidad = params.periodicidad,
            diasPeriodicidad = params.diasPeriodicidad,
            fechaInicio = params.fechaInicio,
            referenceDate = now.toLocalDate(),
        )
        val reminderAt = TaskReminderPolicy.calculateReminderAt(
            periodicidad = params.periodicidad,
            diasPeriodicidad = params.diasPeriodicidad,
            fechaInicio = params.fechaInicio,
            fechaProximaEjecucion = nextExecutionAt,
            fechaVisibleDesde = TaskTimelinePolicy.defaultVisibleFrom(nextExecutionAt),
            horaRecordatorio = params.horaRecordatorio,
            now = now,
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
                fechaInicio = params.fechaInicio,
                fechaCreacion = now,
                fechaUltimaModificacion = now,
                modoProximoCiclo = params.modoProximoCiclo,
                fechaProximaEjecucion = nextExecutionAt,
                fechaVisibleDesde = TaskTimelinePolicy.defaultVisibleFrom(nextExecutionAt),
                horaRecordatorio = params.horaRecordatorio,
                ultimaVezQueHiceLaTarea = null,
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
                    requiresExactScheduling = TaskReminderPolicy.requiresExactAlarm(params.horaRecordatorio),
                ),
            )
        }

        return taskId
    }
}
