package com.dngarcia.tareasdiarias.widget.today

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import androidx.core.os.bundleOf
import com.dngarcia.tareasdiarias.R
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class TodayWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        updateWidgets(
            context = context,
            appWidgetManager = appWidgetManager,
            appWidgetIds = appWidgetIds,
        )
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: android.os.Bundle,
    ) {
        updateWidgets(
            context = context,
            appWidgetManager = appWidgetManager,
            appWidgetIds = intArrayOf(appWidgetId),
        )
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            TodayWidgetIntentFactory.ACTION_COMPLETE,
            TodayWidgetIntentFactory.ACTION_UNDO,
            TodayWidgetIntentFactory.ACTION_POSTPONE,
            TodayWidgetIntentFactory.ACTION_EDIT,
            TodayWidgetIntentFactory.ACTION_OPEN_TODAY -> handleWidgetAction(context, intent)

            else -> super.onReceive(context, intent)
        }
    }

    private fun handleWidgetAction(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(TodayWidgetIntentFactory.EXTRA_TASK_ID, -1L)
        if (intent.action == TodayWidgetIntentFactory.ACTION_EDIT && taskId > 0L) {
            context.startActivity(
                Intent(context, com.dngarcia.tareasdiarias.MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra(
                        TodayWidgetIntentFactory.EXTRA_NAV_ROUTE,
                        com.dngarcia.tareasdiarias.presentation.navigation.AppRoute.editTaskRoute(taskId),
                    )
                },
            )
            refreshAll(context)
            return
        }
        if (intent.action == TodayWidgetIntentFactory.ACTION_OPEN_TODAY) {
            context.startActivity(
                Intent(context, com.dngarcia.tareasdiarias.MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                },
            )
            return
        }
        if (taskId <= 0L) {
            refreshAll(context)
            return
        }

        val pendingResult = goAsync()
        widgetScope.launch {
            try {
                val entryPoint = EntryPointAccessors.fromApplication(
                    context.applicationContext,
                    TodayWidgetEntryPoint::class.java,
                )
                when (intent.action) {
                    TodayWidgetIntentFactory.ACTION_COMPLETE -> entryPoint.completeTaskUseCase()(taskId)
                    TodayWidgetIntentFactory.ACTION_UNDO -> entryPoint.undoTaskCompletionUseCase()(taskId)
                    TodayWidgetIntentFactory.ACTION_POSTPONE -> entryPoint.postponeTaskUseCase()(taskId)
                }
            } finally {
                refreshAll(context)
                pendingResult.finish()
            }
        }
    }

    companion object {
        private val widgetScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        fun refreshAll(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                ComponentName(context, TodayWidgetProvider::class.java),
            )
            if (appWidgetIds.isEmpty()) return
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_today_list)
            updateWidgets(
                context = context,
                appWidgetManager = appWidgetManager,
                appWidgetIds = appWidgetIds,
            )
        }

        internal fun updateWidgets(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetIds: IntArray,
        ) {
            appWidgetIds.forEach { appWidgetId ->
                val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
                val sizeMode = TodayWidgetSizeMode.fromOptions(options)
                val serviceIntent = Intent(context, TodayWidgetService::class.java).apply {
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                    putExtras(
                        bundleOf(TodayWidgetIntentFactory.EXTRA_WIDGET_SIZE_MODE to sizeMode.name),
                    )
                    data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
                }
                val remoteViews = RemoteViews(context.packageName, R.layout.widget_today).apply {
                    setTextViewText(
                        R.id.widget_today_title,
                        context.getString(R.string.widget_today_title),
                    )
                    setTextViewText(
                        R.id.widget_today_subtitle,
                        context.getString(R.string.widget_today_subtitle),
                    )
                    setRemoteAdapter(R.id.widget_today_list, serviceIntent)
                    setEmptyView(R.id.widget_today_list, R.id.widget_today_empty)
                    setPendingIntentTemplate(
                        R.id.widget_today_list,
                        TodayWidgetIntentFactory.createTemplatePendingIntent(context),
                    )
                    setOnClickPendingIntent(
                        R.id.widget_today_header,
                        TodayWidgetIntentFactory.createOpenTodayPendingIntent(context),
                    )
                    setOnClickPendingIntent(
                        R.id.widget_today_empty,
                        TodayWidgetIntentFactory.createOpenTodayPendingIntent(context),
                    )
                }
                appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
            }
        }
    }
}
