package com.dngarcia.tareasdiarias.widget.today

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TodayWidgetUpdater @Inject constructor(
    @ApplicationContext
    private val context: Context,
) {
    fun refreshAll() {
        TodayWidgetProvider.refreshAll(context)
    }

    fun refreshWidgetData() {
        val manager = AppWidgetManager.getInstance(context)
        val widgetIds = manager.getAppWidgetIds(
            ComponentName(context, TodayWidgetProvider::class.java),
        )
        if (widgetIds.isEmpty()) return
        manager.notifyAppWidgetViewDataChanged(widgetIds, com.dngarcia.tareasdiarias.R.id.widget_today_list)
    }
}
