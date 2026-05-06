package com.dngarcia.tareasdiarias.domain.usecase

import com.dngarcia.tareasdiarias.domain.model.TaskReminder
import com.dngarcia.tareasdiarias.domain.repository.TaskReminderScheduler
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class CancelTaskReminderUseCaseTest {
    @Test
    fun invoke_cancelsReminderByTaskId() = runBlocking {
        val fakeScheduler = FakeTaskReminderScheduler()
        val useCase = CancelTaskReminderUseCase(fakeScheduler)

        useCase(taskId = 33L)

        assertEquals(33L, fakeScheduler.lastCancelledTaskId)
    }

    private class FakeTaskReminderScheduler : TaskReminderScheduler {
        var lastCancelledTaskId: Long? = null

        override suspend fun schedule(reminder: TaskReminder) = Unit

        override suspend fun cancel(taskId: Long) {
            lastCancelledTaskId = taskId
        }
    }
}
