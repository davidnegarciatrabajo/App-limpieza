package com.dngarcia.tareasdiarias.widget.today

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TodayWidgetUpdater @Inject constructor(
    @ApplicationContext
    private val context: Context,
) {
    /** Delega en [TodayWidgetProvider.refreshAll] (hilo principal + coalescing). */
    fun refreshAll() {
        TodayWidgetProvider.refreshAll(context)
    }
}
