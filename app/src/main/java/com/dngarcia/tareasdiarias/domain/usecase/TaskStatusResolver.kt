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
        val dueDate = dueDateTime?.toLocalDate()
        val today = now.toLocalDate()
        val status = when {
            dueDateTime == null -> TaskStatus.OK
            dueDate != null && dueDate.isBefore(today) -> TaskStatus.VENCIDA
            dueDate == today -> TaskStatus.PROXIMA
            dueDateTime.isBefore(now.plusHours(UPCOMING_THRESHOLD_HOURS)) -> TaskStatus.PROXIMA
            else -> TaskStatus.OK
        }

        val hoursUntilDue = dueDateTime?.let { Duration.between(now, it).toHours().coerceAtLeast(0) }
        val daysDelta = dueDate?.let { kotlin.math.abs(java.time.temporal.ChronoUnit.DAYS.between(it, today)) }

        return TaskStatusInfo(
            status = status,
            hoursUntilDue = hoursUntilDue,
            daysDelta = daysDelta,
            lastModifiedAt = task.fechaUltimaModificacion,
        )
    }
}
