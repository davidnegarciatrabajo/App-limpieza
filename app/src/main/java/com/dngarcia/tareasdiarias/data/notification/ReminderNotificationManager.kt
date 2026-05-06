package com.dngarcia.tareasdiarias.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.dngarcia.tareasdiarias.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationManagerCompat: NotificationManagerCompat,
) {
    fun createChannel() {
        val channel = NotificationChannel(
            REMINDER_CHANNEL_ID,
            context.getString(R.string.reminder_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = context.getString(R.string.reminder_channel_description)
        }
        notificationManagerCompat.createNotificationChannel(channel)
    }

    fun showTaskReminder(
        taskId: Long,
        taskTitle: String,
    ) {
        val notification = NotificationCompat.Builder(context, REMINDER_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(context.getString(R.string.reminder_notification_title))
            .setContentText(
                context.getString(
                    R.string.reminder_notification_message,
                    taskTitle,
                ),
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManagerCompat.notify(taskId.toInt(), notification)
    }

    companion object {
        const val REMINDER_CHANNEL_ID: String = "task_reminders"
    }
}
