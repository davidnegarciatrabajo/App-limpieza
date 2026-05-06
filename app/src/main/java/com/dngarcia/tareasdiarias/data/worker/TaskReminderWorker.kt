package com.dngarcia.tareasdiarias.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dngarcia.tareasdiarias.data.notification.ReminderNotificationManager
import com.dngarcia.tareasdiarias.data.reminder.ReminderWorkConstants
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class TaskReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val reminderNotificationManager: ReminderNotificationManager,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val taskId = inputData.getLong(ReminderWorkConstants.INPUT_TASK_ID, -1L)
        val taskTitle = inputData.getString(ReminderWorkConstants.INPUT_TASK_TITLE).orEmpty()

        if (taskId <= 0L || taskTitle.isBlank()) {
            return Result.failure()
        }

        return runCatching {
            reminderNotificationManager.showTaskReminder(
                taskId = taskId,
                taskTitle = taskTitle,
            )
            Result.success()
        }.getOrElse {
            Result.retry()
        }
    }
}
