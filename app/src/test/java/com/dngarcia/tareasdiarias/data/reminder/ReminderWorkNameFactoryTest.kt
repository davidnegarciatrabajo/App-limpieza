package com.dngarcia.tareasdiarias.data.reminder

import org.junit.Assert.assertEquals
import org.junit.Test

class ReminderWorkNameFactoryTest {
    @Test
    fun forTask_buildsStableUniqueWorkName() {
        val workName = ReminderWorkNameFactory.forTask(taskId = 77L)

        assertEquals("task_reminder_77", workName)
    }
}
