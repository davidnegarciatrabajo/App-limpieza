package com.dngarcia.tareasdiarias.data.reminder

object ReminderWorkNameFactory {
    fun forTask(taskId: Long): String {
        return "${ReminderWorkConstants.REMINDER_WORK_NAME_PREFIX}$taskId"
    }
}
