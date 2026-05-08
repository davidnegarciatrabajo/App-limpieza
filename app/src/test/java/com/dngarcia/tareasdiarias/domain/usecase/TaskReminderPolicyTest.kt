package com.dngarcia.tareasdiarias.domain.usecase

import com.dngarcia.tareasdiarias.domain.model.Periodicidad
import java.time.LocalDateTime
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TaskReminderPolicyTest {
    @Test
    fun calculateReminderAt_withoutExplicitTime_keepsLegacyBehavior() {
        // Arrange
        val baseDateTime = LocalDateTime.of(2026, 5, 8, 14, 30)

        // Act
        val result = TaskReminderPolicy.calculateReminderAt(
            periodicidad = Periodicidad.DIARIA,
            diasPeriodicidad = null,
            baseDateTime = baseDateTime,
            horaRecordatorio = null,
        )

        // Assert
        assertEquals(LocalDateTime.of(2026, 5, 9, 14, 30), result)
    }

    @Test
    fun calculateReminderAt_withExplicitTime_combinesNextCycleDateWithSelectedHour() {
        // Arrange
        val baseDateTime = LocalDateTime.of(2026, 5, 8, 14, 30)

        // Act
        val result = TaskReminderPolicy.calculateReminderAt(
            periodicidad = Periodicidad.SEMANAL,
            diasPeriodicidad = null,
            baseDateTime = baseDateTime,
            horaRecordatorio = LocalTime.of(9, 15),
        )

        // Assert
        assertEquals(LocalDateTime.of(2026, 5, 15, 9, 15), result)
    }

    @Test
    fun calculateReminderAt_whenUniqueTimeAlreadyPassed_movesReminderToNextDay() {
        // Arrange
        val baseDateTime = LocalDateTime.of(2026, 5, 8, 14, 30)

        // Act
        val result = TaskReminderPolicy.calculateReminderAt(
            periodicidad = Periodicidad.UNICA,
            diasPeriodicidad = null,
            baseDateTime = baseDateTime,
            horaRecordatorio = LocalTime.of(9, 0),
        )

        // Assert
        assertEquals(LocalDateTime.of(2026, 5, 9, 9, 0), result)
    }

    @Test
    fun calculateReminderAt_forUniqueTask_withoutExplicitTime_addsOneMinute() {
        // Arrange
        val baseDateTime = LocalDateTime.of(2026, 5, 8, 14, 30)

        // Act
        val result = TaskReminderPolicy.calculateReminderAt(
            periodicidad = Periodicidad.UNICA,
            diasPeriodicidad = null,
            baseDateTime = baseDateTime,
            horaRecordatorio = null,
        )

        // Assert
        assertEquals(LocalDateTime.of(2026, 5, 8, 14, 31), result)
    }

    @Test
    fun calculateReminderAt_withInvalidCustomDays_returnsNull() {
        // Arrange
        val baseDateTime = LocalDateTime.of(2026, 5, 8, 14, 30)

        // Act
        val result = TaskReminderPolicy.calculateReminderAt(
            periodicidad = Periodicidad.PERSONALIZADA,
            diasPeriodicidad = 0,
            baseDateTime = baseDateTime,
            horaRecordatorio = LocalTime.of(8, 0),
        )

        // Assert
        assertNull(result)
    }

    @Test
    fun requiresExactAlarm_returnsTrueOnlyForUniqueTasks() {
        assertEquals(true, TaskReminderPolicy.requiresExactAlarm(Periodicidad.UNICA))
        assertEquals(false, TaskReminderPolicy.requiresExactAlarm(Periodicidad.DIARIA))
    }
}
