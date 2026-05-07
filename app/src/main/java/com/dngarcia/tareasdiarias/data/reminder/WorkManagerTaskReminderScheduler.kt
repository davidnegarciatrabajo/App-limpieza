package com.dngarcia.tareasdiarias.data.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.dngarcia.tareasdiarias.data.receiver.TaskReminderAlarmReceiver
import com.dngarcia.tareasdiarias.data.worker.TaskReminderWorker
import com.dngarcia.tareasdiarias.domain.model.TaskReminder
import com.dngarcia.tareasdiarias.domain.repository.TaskReminderScheduler
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext

@Singleton
class WorkManagerTaskReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val workManager: WorkManager,
) : TaskReminderScheduler {
    override suspend fun schedule(reminder: TaskReminder) {
        val now = LocalDateTime.now()
        if (reminder.reminderAt <= now) {
            Log.w(TAG, "Reminder descartado por fecha en pasado. taskId=${reminder.taskId}")
            cancel(reminder.taskId)
            return
        }

        val workName = ReminderWorkNameFactory.forTask(reminder.taskId)
        val delayMillis = ReminderDelayCalculator.calculateDelayMillis(
            reminderAt = reminder.reminderAt,
            now = now,
        )
        val shouldUseExactAlarm = reminder.requiresExactScheduling && canScheduleExactAlarms()

        if (shouldUseExactAlarm) {
            enqueueExactAlarm(reminder)
            workManager.cancelUniqueWork(workName)
            Log.d(TAG, "Reminder agendado con exact alarm. taskId=${reminder.taskId}")
            return
        }

        cancelExactAlarm(reminder.taskId)
        val request = OneTimeWorkRequestBuilder<TaskReminderWorker>()
            .setInitialDelay(delayMillis, java.util.concurrent.TimeUnit.MILLISECONDS)
            .setInputData(buildInputData(reminder))
            .build()

        workManager.enqueueUniqueWork(workName, ExistingWorkPolicy.REPLACE, request)
        Log.d(
            TAG,
            "Reminder agendado con WorkManager. taskId=${reminder.taskId}, exactRequested=${reminder.requiresExactScheduling}",
        )
    }

    override suspend fun cancel(taskId: Long) {
        cancelExactAlarm(taskId)
        workManager.cancelUniqueWork(ReminderWorkNameFactory.forTask(taskId))
        Log.d(TAG, "Reminder cancelado. taskId=$taskId")
    }

    private fun buildInputData(reminder: TaskReminder): Data {
        return Data.Builder()
            .putLong(ReminderWorkConstants.INPUT_TASK_ID, reminder.taskId)
            .putString(ReminderWorkConstants.INPUT_TASK_TITLE, reminder.taskTitle)
            .build()
    }

    private fun enqueueExactAlarm(reminder: TaskReminder) {
        val triggerAtMillis = reminder.reminderAt
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val pendingIntent = buildAlarmPendingIntent(
            taskId = reminder.taskId,
            taskTitle = reminder.taskTitle,
            flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent,
        )
    }

    private fun cancelExactAlarm(taskId: Long) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val intent = Intent(context, TaskReminderAlarmReceiver::class.java).apply {
            action = ReminderWorkConstants.ACTION_EXACT_REMINDER
            putExtra(ReminderWorkConstants.INPUT_TASK_ID, taskId)
            putExtra(ReminderWorkConstants.INPUT_TASK_TITLE, "")
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
        ) ?: return
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    private fun buildAlarmPendingIntent(
        taskId: Long,
        taskTitle: String,
        flags: Int,
    ): PendingIntent {
        val intent = Intent(context, TaskReminderAlarmReceiver::class.java).apply {
            action = ReminderWorkConstants.ACTION_EXACT_REMINDER
            putExtra(ReminderWorkConstants.INPUT_TASK_ID, taskId)
            putExtra(ReminderWorkConstants.INPUT_TASK_TITLE, taskTitle)
        }
        return PendingIntent.getBroadcast(context, taskId.toInt(), intent, flags)
            ?: throw IllegalStateException("No se pudo crear PendingIntent para taskId=$taskId")
    }

    private fun canScheduleExactAlarms(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        return alarmManager.canScheduleExactAlarms()
    }

    private companion object {
        const val TAG: String = "TaskReminderScheduler"
    }
}
