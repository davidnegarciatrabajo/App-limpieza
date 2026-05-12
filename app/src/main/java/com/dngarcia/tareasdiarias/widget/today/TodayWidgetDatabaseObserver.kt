package com.dngarcia.tareasdiarias.widget.today

import androidx.room.InvalidationTracker
import com.dngarcia.tareasdiarias.data.local.TareasDatabase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TodayWidgetDatabaseObserver @Inject constructor(
    private val database: TareasDatabase,
    private val todayWidgetUpdater: TodayWidgetUpdater,
) {
    @Volatile
    private var started = false

    private val observer = object : InvalidationTracker.Observer("tarea", "ejecucion", "categoria") {
        override fun onInvalidated(tables: Set<String>) {
            if (tables.isNotEmpty()) {
                todayWidgetUpdater.refreshAll()
            }
        }
    }

    fun start() {
        if (started) return
        synchronized(this) {
            if (started) return
            database.invalidationTracker.addObserver(observer)
            started = true
        }
    }
}
