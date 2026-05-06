package com.dngarcia.tareasdiarias.data.reminder

import java.time.Duration
import java.time.LocalDateTime

object ReminderDelayCalculator {
    fun calculateDelayMillis(
        reminderAt: LocalDateTime,
        now: LocalDateTime,
    ): Long {
        val delayMillis = Duration.between(now, reminderAt).toMillis()
        return delayMillis.coerceAtLeast(0L)
    }
}
