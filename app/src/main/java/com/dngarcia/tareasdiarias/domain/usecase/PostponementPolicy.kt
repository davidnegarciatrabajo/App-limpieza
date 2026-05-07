package com.dngarcia.tareasdiarias.domain.usecase

import java.time.Duration
import java.time.LocalDateTime

object PostponementPolicy {
    private const val MIN_POSTPONE_MINUTES = 1L

    fun shouldIncrement(
        currentDueDate: LocalDateTime?,
        updatedDueDate: LocalDateTime?,
    ): Boolean {
        if (currentDueDate == null || updatedDueDate == null) return false
        if (!updatedDueDate.isAfter(currentDueDate)) return false
        val increasedMinutes = Duration.between(currentDueDate, updatedDueDate).toMinutes()
        return increasedMinutes >= MIN_POSTPONE_MINUTES
    }
}
