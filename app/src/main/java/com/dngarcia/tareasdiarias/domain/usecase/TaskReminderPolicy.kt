package com.dngarcia.tareasdiarias.domain.usecase

import com.dngarcia.tareasdiarias.domain.model.Periodicidad
import java.time.LocalDateTime

object TaskReminderPolicy {
    // Para tareas UNICA se mantiene la regla actual de recordatorio corto.
    private const val UNIQUE_TASK_REMINDER_MINUTES = 1L

    fun calculateReminderAt(
        periodicidad: Periodicidad,
        diasPeriodicidad: Int?,
        baseDateTime: LocalDateTime,
    ): LocalDateTime? {
        return when (periodicidad) {
            Periodicidad.DIARIA -> baseDateTime.plusDays(1)
            Periodicidad.SEMANAL -> baseDateTime.plusDays(7)
            Periodicidad.MENSUAL -> baseDateTime.plusMonths(1)
            Periodicidad.SEMESTRAL -> baseDateTime.plusMonths(6)
            Periodicidad.PERSONALIZADA -> {
                val days = diasPeriodicidad ?: return null
                if (days <= 0) return null
                baseDateTime.plusDays(days.toLong())
            }
            Periodicidad.UNICA -> baseDateTime.plusMinutes(UNIQUE_TASK_REMINDER_MINUTES)
        }
    }

    fun requiresExactAlarm(periodicidad: Periodicidad): Boolean {
        return periodicidad == Periodicidad.UNICA
    }
}
