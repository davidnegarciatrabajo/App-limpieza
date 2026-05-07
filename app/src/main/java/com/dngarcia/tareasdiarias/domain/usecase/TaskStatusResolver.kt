package com.dngarcia.tareasdiarias.domain.usecase

import com.dngarcia.tareasdiarias.domain.model.TaskStatus
import com.dngarcia.tareasdiarias.domain.model.TaskStatusInfo
import com.dngarcia.tareasdiarias.domain.model.Tarea
import java.time.Duration
import java.time.LocalDateTime

object TaskStatusResolver {
    private const val UPCOMING_THRESHOLD_HOURS = 24L

    fun resolve(task: Tarea, now: LocalDateTime): TaskStatusInfo {
        val dueDateTime = task.fechaProximaEjecucion
        val status = when {
            dueDateTime == null -> TaskStatus.OK
            dueDateTime.isBefore(now) -> TaskStatus.VENCIDA
            Duration.between(now, dueDateTime).toHours() <= UPCOMING_THRESHOLD_HOURS -> TaskStatus.PROXIMA
            else -> TaskStatus.OK
        }

        val hoursUntilDue = dueDateTime?.let { Duration.between(now, it).toHours() }
        val daysDelta = hoursUntilDue?.let { kotlin.math.abs(it) / 24 }

        return TaskStatusInfo(
            status = status,
            hoursUntilDue = hoursUntilDue,
            daysDelta = daysDelta,
            lastModifiedAt = task.fechaUltimaModificacion,
        )
    }
}
