package com.dngarcia.tareasdiarias.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.dngarcia.tareasdiarias.data.notification.ReminderNotificationManager
import com.dngarcia.tareasdiarias.data.reminder.ReminderWorkConstants
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TaskReminderAlarmReceiver : BroadcastReceiver() {
    @Inject
    lateinit var reminderNotificationManager: ReminderNotificationManager

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != ReminderWorkConstants.ACTION_EXACT_REMINDER) return

        val taskId = intent.getLongExtra(ReminderWorkConstants.INPUT_TASK_ID, -1L)
        val taskTitle = intent.getStringExtra(ReminderWorkConstants.INPUT_TASK_TITLE).orEmpty()
        if (taskId <= 0L || taskTitle.isBlank()) {
            Log.w(TAG, "Exact alarm descartada por payload invalido.")
            return
        }

        reminderNotificationManager.showTaskReminder(taskId = taskId, taskTitle = taskTitle)
    }

    private companion object {
        const val TAG: String = "ReminderAlarmReceiver"
    }
}
