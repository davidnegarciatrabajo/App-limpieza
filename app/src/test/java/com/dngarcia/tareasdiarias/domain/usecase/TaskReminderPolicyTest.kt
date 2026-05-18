package com.dngarcia.tareasdiarias.domain.usecase

import com.dngarcia.tareasdiarias.domain.model.Periodicidad
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TaskReminderPolicyTest {
    @Test
    fun calculateNextExecutionAt_usesFechaInicioAsAnchorForRecurringTasks() {
        val result = TaskReminderPolicy.calculateNextExecutionAt(
            periodicidad = Periodicidad.DIARIA,
            diasPeriodicidad = null,
            fechaInicio = LocalDate.of(2026, 5, 5),
            referenceDate = LocalDate.of(2026, 5, 8),
        )

        assertEquals(LocalDateTime.of(2026, 5, 8, 0, 0), result)
    }

    @Test
    fun calculateNextExecutionAt_forFutureStartKeepsFutureDate() {
        val result = TaskReminderPolicy.calculateNextExecutionAt(
            periodicidad = Periodicidad.SEMANAL,
            diasPeriodicidad = null,
            fechaInicio = LocalDate.of(2026, 5, 15),
            referenceDate = LocalDate.of(2026, 5, 8),
        )

        assertEquals(LocalDateTime.of(2026, 5, 15, 0, 0), result)
    }

    @Test
    fun calculateReminderAt_withFutureDueDateAndTime_usesSameDayAndHour() {
        val result = TaskReminderPolicy.calculateReminderAt(
            periodicidad = Periodicidad.SEMANAL,
            diasPeriodicidad = null,
            fechaInicio = LocalDate.of(2026, 5, 1),
            fechaProximaEjecucion = LocalDateTime.of(2026, 5, 15, 0, 0),
            fechaVisibleDesde = LocalDate.of(2026, 5, 15),
            horaRecordatorio = LocalTime.of(9, 15),
            now = LocalDateTime.of(2026, 5, 8, 14, 30),
        )

        assertEquals(LocalDateTime.of(2026, 5, 15, 9, 15), result)
    }

    @Test
    fun calculateReminderAt_whenCurrentCycleTimeAlreadyPassed_movesToNextValidCycle() {
        val result = TaskReminderPolicy.calculateReminderAt(
            periodicidad = Periodicidad.SEMANAL,
            diasPeriodicidad = null,
            fechaInicio = LocalDate.of(2026, 5, 1),
            fechaProximaEjecucion = LocalDateTime.of(2026, 5, 8, 0, 0),
            fechaVisibleDesde = LocalDate.of(2026, 5, 8),
            horaRecordatorio = LocalTime.of(9, 0),
            now = LocalDateTime.of(2026, 5, 8, 14, 30),
        )

        assertEquals(LocalDateTime.of(2026, 5, 15, 9, 0), result)
    }

    @Test
    fun calculateReminderAt_forPastUniqueTaskReturnsNull() {
        val result = TaskReminderPolicy.calculateReminderAt(
            periodicidad = Periodicidad.UNICA,
            diasPeriodicidad = null,
            fechaInicio = LocalDate.of(2026, 5, 8),
            fechaProximaEjecucion = LocalDateTime.of(2026, 5, 8, 0, 0),
            fechaVisibleDesde = LocalDate.of(2026, 5, 8),
            horaRecordatorio = LocalTime.of(8, 0),
            now = LocalDateTime.of(2026, 5, 8, 14, 30),
        )

        assertNull(result)
    }

    @Test
    fun calculateNextExecutionAfterResolution_forUniqueTask_returnsNull() {
        val result = TaskReminderPolicy.calculateNextExecutionAfterResolution(
            periodicidad = Periodicidad.UNICA,
            diasPeriodicidad = null,
            fechaInicio = LocalDate.of(2026, 5, 8),
            resolvedAt = LocalDate.of(2026, 5, 8),
        )

        assertNull(result)
    }

    @Test
    fun calculateNextExecutionAt_withInvalidCustomDays_returnsNull() {
        val result = TaskReminderPolicy.calculateNextExecutionAt(
            periodicidad = Periodicidad.PERSONALIZADA,
            diasPeriodicidad = 0,
            fechaInicio = LocalDate.of(2026, 5, 8),
            referenceDate = LocalDate.of(2026, 5, 8),
        )

        assertNull(result)
    }

    @Test
    fun calculateReminderAt_whenTaskIsPostponed_usesAppearanceDateForNotification() {
        val result = TaskReminderPolicy.calculateReminderAt(
            periodicidad = Periodicidad.DIARIA,
            diasPeriodicidad = null,
            fechaInicio = LocalDate.of(2026, 5, 1),
            fechaProximaEjecucion = LocalDateTime.of(2026, 5, 8, 0, 0),
            fechaVisibleDesde = LocalDate.of(2026, 5, 11),
            horaRecordatorio = LocalTime.of(9, 0),
            now = LocalDateTime.of(2026, 5, 8, 14, 30),
        )

        assertEquals(LocalDateTime.of(2026, 5, 11, 9, 0), result)
    }

    @Test
    fun requiresExactAlarm_returnsTrueWhenReminderHasTime() {
        assertEquals(true, TaskReminderPolicy.requiresExactAlarm(LocalTime.of(9, 0)))
        assertEquals(false, TaskReminderPolicy.requiresExactAlarm(null))
    }

    @Test
    fun calculateNextExecutionFloatingAfterCompletion_daily_addsOneDay() {
        val result = TaskReminderPolicy.calculateNextExecutionFloatingAfterCompletion(
            periodicidad = Periodicidad.DIARIA,
            diasPeriodicidad = null,
            completedDate = LocalDate.of(2026, 5, 11),
        )
        assertEquals(LocalDateTime.of(2026, 5, 12, 0, 0), result)
    }

    @Test
    fun calculateNextExecutionFloatingAfterCompletion_weekly_addsSevenDays() {
        val result = TaskReminderPolicy.calculateNextExecutionFloatingAfterCompletion(
            periodicidad = Periodicidad.SEMANAL,
            diasPeriodicidad = null,
            completedDate = LocalDate.of(2026, 5, 14),
        )
        assertEquals(LocalDateTime.of(2026, 5, 21, 0, 0), result)
    }

    @Test
    fun calculateNextExecutionFloatingAfterCompletion_custom_usesIntervalDays() {
        val result = TaskReminderPolicy.calculateNextExecutionFloatingAfterCompletion(
            periodicidad = Periodicidad.PERSONALIZADA,
            diasPeriodicidad = 10,
            completedDate = LocalDate.of(2026, 5, 1),
        )
        assertEquals(LocalDateTime.of(2026, 5, 11, 0, 0), result)
    }

    @Test
    fun calculateNextExecutionFloatingAfterCompletion_unique_returnsNull() {
        val result = TaskReminderPolicy.calculateNextExecutionFloatingAfterCompletion(
            periodicidad = Periodicidad.UNICA,
            diasPeriodicidad = null,
            completedDate = LocalDate.of(2026, 5, 1),
        )
        assertNull(result)
    }
}
