package com.dngarcia.tareasdiarias.data.reminder

import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.dngarcia.tareasdiarias.data.worker.TaskReminderWorker
import com.dngarcia.tareasdiarias.domain.model.TaskReminder
import com.dngarcia.tareasdiarias.domain.repository.TaskReminderScheduler
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkManagerTaskReminderScheduler @Inject constructor(
    private val workManager: WorkManager,
) : TaskReminderScheduler {
    override suspend fun schedule(reminder: TaskReminder) {
        val workName = ReminderWorkNameFactory.forTask(reminder.taskId)
        val delayMillis = ReminderDelayCalculator.calculateDelayMillis(
            reminderAt = reminder.reminderAt,
            now = LocalDateTime.now(),
        )
        val inputData = Data.Builder()
            .putLong(ReminderWorkConstants.INPUT_TASK_ID, reminder.taskId)
            .putString(ReminderWorkConstants.INPUT_TASK_TITLE, reminder.taskTitle)
            .build()

        val request = OneTimeWorkRequestBuilder<TaskReminderWorker>()
            .setInitialDelay(delayMillis, java.util.concurrent.TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .build()

        workManager.enqueueUniqueWork(
            workName,
            ExistingWorkPolicy.REPLACE,
            request,
        )
    }

    override suspend fun cancel(taskId: Long) {
        workManager.cancelUniqueWork(ReminderWorkNameFactory.forTask(taskId))
    }
}
