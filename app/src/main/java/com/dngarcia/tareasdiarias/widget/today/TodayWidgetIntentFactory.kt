package com.dngarcia.tareasdiarias.widget.today

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import com.dngarcia.tareasdiarias.MainActivity
import com.dngarcia.tareasdiarias.presentation.navigation.AppRoute

object TodayWidgetIntentFactory {
    const val ACTION_COMPLETE = "com.dngarcia.tareasdiarias.widget.ACTION_COMPLETE"
    const val ACTION_UNDO = "com.dngarcia.tareasdiarias.widget.ACTION_UNDO"
    const val ACTION_POSTPONE = "com.dngarcia.tareasdiarias.widget.ACTION_POSTPONE"
    const val ACTION_EDIT = "com.dngarcia.tareasdiarias.widget.ACTION_EDIT"
    const val ACTION_OPEN_TODAY = "com.dngarcia.tareasdiarias.widget.ACTION_OPEN_TODAY"

    const val EXTRA_TASK_ID = "extra_task_id"
    const val EXTRA_NAV_ROUTE = "extra_nav_route"
    const val EXTRA_WIDGET_SIZE_MODE = "extra_widget_size_mode"

    fun createTemplatePendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, TodayWidgetProvider::class.java).apply {
            `package` = context.packageName
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        }
        return PendingIntent.getBroadcast(
            context,
            10_001,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE,
        )
    }

    fun createOpenTodayPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            10_002,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    fun createOpenEditPendingIntent(context: Context, taskId: Long): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_NAV_ROUTE, AppRoute.editTaskRoute(taskId))
        }
        return PendingIntent.getActivity(
            context,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
