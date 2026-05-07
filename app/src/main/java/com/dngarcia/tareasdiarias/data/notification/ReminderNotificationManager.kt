package com.dngarcia.tareasdiarias.data.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
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
        if (!notificationManagerCompat.areNotificationsEnabled()) {
            Log.w(TAG, "Notificacion omitida: canal/bloqueo del sistema. taskId=$taskId")
            return
        }
        if (!hasNotificationPermission()) {
            Log.w(TAG, "Notificacion omitida: permiso POST_NOTIFICATIONS denegado. taskId=$taskId")
            return
        }

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

    private fun hasNotificationPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        const val REMINDER_CHANNEL_ID: String = "task_reminders"
        private const val TAG: String = "ReminderNotification"
    }
}
