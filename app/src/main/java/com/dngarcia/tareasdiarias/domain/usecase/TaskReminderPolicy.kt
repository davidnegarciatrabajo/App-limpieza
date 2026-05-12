package com.dngarcia.tareasdiarias.domain.usecase

import com.dngarcia.tareasdiarias.domain.model.Periodicidad
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

object TaskReminderPolicy {
    fun calculateNextExecutionAt(
        periodicidad: Periodicidad,
        diasPeriodicidad: Int?,
        fechaInicio: LocalDate,
        referenceDate: LocalDate,
    ): LocalDateTime? {
        val dueDate = when (periodicidad) {
            Periodicidad.UNICA -> fechaInicio
            else -> {
                if (fechaInicio.isAfter(referenceDate)) {
                    fechaInicio
                } else {
                    latestOccurrenceOnOrBefore(
                        periodicidad = periodicidad,
                        diasPeriodicidad = diasPeriodicidad,
                        fechaInicio = fechaInicio,
                        referenceDate = referenceDate,
                    ) ?: return null
                }
            }
        }

        return dueDate.atStartOfDay()
    }

    fun calculateNextExecutionAfterResolution(
        periodicidad: Periodicidad,
        diasPeriodicidad: Int?,
        fechaInicio: LocalDate,
        resolvedAt: LocalDate,
    ): LocalDateTime? {
        if (periodicidad == Periodicidad.UNICA) return null

        var candidate = fechaInicio
        while (!candidate.isAfter(resolvedAt)) {
            candidate = advanceOneCycle(
                periodicidad = periodicidad,
                diasPeriodicidad = diasPeriodicidad,
                currentDate = candidate,
            ) ?: return null
        }
        return candidate.atStartOfDay()
    }

    fun calculateReminderAt(
        periodicidad: Periodicidad,
        diasPeriodicidad: Int?,
        fechaInicio: LocalDate,
        fechaProximaEjecucion: LocalDateTime?,
        horaRecordatorio: LocalTime?,
        now: LocalDateTime,
    ): LocalDateTime? {
        if (horaRecordatorio == null) return null

        val dueDate = fechaProximaEjecucion?.toLocalDate()
            ?: calculateNextExecutionAt(
                periodicidad = periodicidad,
                diasPeriodicidad = diasPeriodicidad,
                fechaInicio = fechaInicio,
                referenceDate = now.toLocalDate(),
            )?.toLocalDate()
            ?: return null

        val scheduledAt = dueDate.atTime(horaRecordatorio)
        if (scheduledAt.isAfter(now)) return scheduledAt
        if (periodicidad == Periodicidad.UNICA) return null

        var nextDate = dueDate
        while (!nextDate.atTime(horaRecordatorio).isAfter(now)) {
            nextDate = advanceOneCycle(
                periodicidad = periodicidad,
                diasPeriodicidad = diasPeriodicidad,
                currentDate = nextDate,
            ) ?: return null
        }
        return nextDate.atTime(horaRecordatorio)
    }

    fun requiresExactAlarm(periodicidad: Periodicidad): Boolean {
        return periodicidad == Periodicidad.UNICA
    }

    private fun latestOccurrenceOnOrBefore(
        periodicidad: Periodicidad,
        diasPeriodicidad: Int?,
        fechaInicio: LocalDate,
        referenceDate: LocalDate,
    ): LocalDate? {
        var current = fechaInicio
        while (true) {
            val next = advanceOneCycle(
                periodicidad = periodicidad,
                diasPeriodicidad = diasPeriodicidad,
                currentDate = current,
            ) ?: return null
            if (next.isAfter(referenceDate)) {
                return current
            }
            current = next
        }
    }

    private fun advanceOneCycle(
        periodicidad: Periodicidad,
        diasPeriodicidad: Int?,
        currentDate: LocalDate,
    ): LocalDate? {
        return when (periodicidad) {
            Periodicidad.DIARIA -> currentDate.plusDays(1)
            Periodicidad.SEMANAL -> currentDate.plusDays(7)
            Periodicidad.MENSUAL -> currentDate.plusMonths(1)
            Periodicidad.SEMESTRAL -> currentDate.plusMonths(6)
            Periodicidad.PERSONALIZADA -> {
                val days = diasPeriodicidad ?: return null
                if (days <= 0) return null
                currentDate.plusDays(days.toLong())
            }
            Periodicidad.UNICA -> null
        }
    }
}
