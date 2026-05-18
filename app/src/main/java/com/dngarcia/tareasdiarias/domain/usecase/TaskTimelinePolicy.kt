package com.dngarcia.tareasdiarias.domain.usecase

import com.dngarcia.tareasdiarias.domain.model.Tarea
import java.time.LocalDate
import java.time.LocalDateTime

object TaskTimelinePolicy {
    fun expectedCycleDate(task: Tarea): LocalDate? = task.fechaProximaEjecucion?.toLocalDate()

    fun effectiveAppearanceDate(task: Tarea): LocalDate? {
        return effectiveAppearanceDate(
            expectedCycleAt = task.fechaProximaEjecucion,
            visibleFrom = task.fechaVisibleDesde,
        )
    }

    fun effectiveAppearanceDate(
        expectedCycleAt: LocalDateTime?,
        visibleFrom: LocalDate?,
    ): LocalDate? {
        val expectedDate = expectedCycleAt?.toLocalDate()
        return when {
            expectedDate == null -> visibleFrom
            visibleFrom == null -> expectedDate
            visibleFrom.isAfter(expectedDate) -> visibleFrom
            else -> expectedDate
        }
    }

    fun shouldAppearOnDate(task: Tarea, date: LocalDate): Boolean {
        val appearanceDate = effectiveAppearanceDate(task) ?: return false
        return !appearanceDate.isAfter(date)
    }

    fun overdueDays(task: Tarea, date: LocalDate): Long? {
        val expectedDate = expectedCycleDate(task) ?: return null
        if (expectedDate.isAfter(date)) return null
        return java.time.temporal.ChronoUnit.DAYS.between(expectedDate, date)
    }

    /** True cuando el próximo ciclo programado coincide con [day] (por fecha civil). Usado por la vista Mañana. */
    fun isExpectedCycleOnCalendarDay(task: Tarea, day: LocalDate): Boolean {
        return expectedCycleDate(task)?.isEqual(day) == true
    }

    fun nextCycleDateAfter(
        task: Tarea,
        resolvedCycleDate: LocalDate,
    ): LocalDate? = TaskReminderPolicy.calculateNextExecutionAfterResolution(
        periodicidad = task.tipoPeriodicidad,
        diasPeriodicidad = task.diasPeriodicidad,
        fechaInicio = task.fechaInicio,
        resolvedAt = resolvedCycleDate,
    )?.toLocalDate()

    fun defaultVisibleFrom(expectedCycleAt: LocalDateTime?): LocalDate? = expectedCycleAt?.toLocalDate()

    fun preserveVisibleFromOnUpdate(
        currentVisibleFrom: LocalDate?,
        recalculatedExpectedCycleAt: LocalDateTime?,
        today: LocalDate,
    ): LocalDate? {
        val baseVisibleFrom = if (currentVisibleFrom != null && currentVisibleFrom.isAfter(today)) {
            currentVisibleFrom
        } else {
            defaultVisibleFrom(recalculatedExpectedCycleAt)
        }
        return effectiveAppearanceDate(
            expectedCycleAt = recalculatedExpectedCycleAt,
            visibleFrom = baseVisibleFrom,
        )
    }
}
