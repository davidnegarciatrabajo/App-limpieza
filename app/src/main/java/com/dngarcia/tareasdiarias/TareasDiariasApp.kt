package com.dngarcia.tareasdiarias

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.dngarcia.tareasdiarias.data.notification.ReminderNotificationManager
import com.dngarcia.tareasdiarias.domain.usecase.SeedDebugSampleDataIfNeededUseCase
import com.dngarcia.tareasdiarias.widget.today.TodayWidgetDatabaseObserver
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@HiltAndroidApp
class TareasDiariasApp : Application(), Configuration.Provider {
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var reminderNotificationManager: ReminderNotificationManager

    @Inject
    lateinit var seedDebugSampleDataIfNeeded: SeedDebugSampleDataIfNeededUseCase

    @Inject
    lateinit var todayWidgetDatabaseObserver: TodayWidgetDatabaseObserver

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        reminderNotificationManager.createChannel()
        todayWidgetDatabaseObserver.start()
        applicationScope.launch {
            runCatching { seedDebugSampleDataIfNeeded() }
                .onFailure { Log.e(TAG, "No se pudo ejecutar el seed de datos de debug.", it) }
        }
    }

    private companion object {
        private const val TAG = "TareasDiariasApp"
    }
}
