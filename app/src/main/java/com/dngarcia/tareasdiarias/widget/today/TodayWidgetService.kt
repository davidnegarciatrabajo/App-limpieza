package com.dngarcia.tareasdiarias.widget.today

import android.content.Intent
import android.widget.RemoteViewsService

class TodayWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        val sizeMode = TodayWidgetSizeMode.fromStoredValue(
            intent.getStringExtra(TodayWidgetIntentFactory.EXTRA_WIDGET_SIZE_MODE),
        )
        return TodayWidgetRemoteViewsFactory(
            context = applicationContext,
            sizeMode = sizeMode,
        )
    }
}
