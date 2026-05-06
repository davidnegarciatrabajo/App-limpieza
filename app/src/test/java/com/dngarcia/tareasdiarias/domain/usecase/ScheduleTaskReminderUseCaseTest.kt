package com.dngarcia.tareasdiarias.domain.usecase

import com.dngarcia.tareasdiarias.domain.model.TaskReminder
import com.dngarcia.tareasdiarias.domain.repository.TaskReminderScheduler
import java.time.LocalDateTime
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class ScheduleTaskReminderUseCaseTest {
    @Test
    fun invoke_schedulesReminder() = runBlocking {
        val fakeScheduler = FakeTaskReminderScheduler()
        val useCase = ScheduleTaskReminderUseCase(fakeScheduler)
        val reminder = TaskReminder(
            taskId = 8L,
            taskTitle = "Ventilar casa",
            reminderAt = LocalDateTime.of(2026, 5, 6, 18, 0),
        )

        useCase(reminder)

        assertEquals(reminder, fakeScheduler.lastScheduledReminder)
    }

    private class FakeTaskReminderScheduler : TaskReminderScheduler {
        var lastScheduledReminder: TaskReminder? = null

        override suspend fun schedule(reminder: TaskReminder) {
            lastScheduledReminder = reminder
        }

        override suspend fun cancel(taskId: Long) = Unit
    }
}
