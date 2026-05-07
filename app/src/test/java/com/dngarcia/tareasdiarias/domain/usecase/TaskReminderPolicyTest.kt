package com.dngarcia.tareasdiarias.domain.usecase

import com.dngarcia.tareasdiarias.domain.model.Periodicidad
import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Test

class TaskReminderPolicyTest {
    @Test
    fun calculateReminderAt_forUniqueTask_addsOneMinute() {
        val now = LocalDateTime.of(2026, 5, 6, 10, 0)

        val reminderAt = TaskReminderPolicy.calculateReminderAt(
            periodicidad = Periodicidad.UNICA,
            diasPeriodicidad = null,
            baseDateTime = now,
        )

        assertEquals(now.plusMinutes(1), reminderAt)
    }

    @Test
    fun requiresExactAlarm_returnsTrueOnlyForUniqueTasks() {
        assertEquals(true, TaskReminderPolicy.requiresExactAlarm(Periodicidad.UNICA))
        assertEquals(false, TaskReminderPolicy.requiresExactAlarm(Periodicidad.DIARIA))
    }
}
