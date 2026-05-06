package com.dngarcia.tareasdiarias.data.reminder

import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Test

class ReminderDelayCalculatorTest {
    @Test
    fun calculateDelayMillis_returnsPositiveDelayForFutureDate() {
        val now = LocalDateTime.of(2026, 5, 6, 10, 0, 0)
        val reminderAt = now.plusMinutes(45)

        val delayMillis = ReminderDelayCalculator.calculateDelayMillis(reminderAt, now)

        assertEquals(2_700_000L, delayMillis)
    }

    @Test
    fun calculateDelayMillis_returnsZeroForPastDate() {
        val now = LocalDateTime.of(2026, 5, 6, 10, 0, 0)
        val reminderAt = now.minusMinutes(5)

        val delayMillis = ReminderDelayCalculator.calculateDelayMillis(reminderAt, now)

        assertEquals(0L, delayMillis)
    }
}
