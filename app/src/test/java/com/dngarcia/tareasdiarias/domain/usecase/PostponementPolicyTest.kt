package com.dngarcia.tareasdiarias.domain.usecase

import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Test

class PostponementPolicyTest {
    @Test
    fun shouldIncrement_whenDueDateMovesForwardOneMinute_returnsTrue() {
        val current = LocalDateTime.of(2026, 5, 6, 10, 0)
        val updated = current.plusMinutes(1)

        val result = PostponementPolicy.shouldIncrement(
            currentDueDate = current,
            updatedDueDate = updated,
        )

        assertEquals(true, result)
    }

    @Test
    fun shouldIncrement_whenDueDateMovesBackward_returnsFalse() {
        val current = LocalDateTime.of(2026, 5, 6, 10, 0)
        val updated = current.minusMinutes(15)

        val result = PostponementPolicy.shouldIncrement(
            currentDueDate = current,
            updatedDueDate = updated,
        )

        assertEquals(false, result)
    }
}
