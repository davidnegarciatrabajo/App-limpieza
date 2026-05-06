package com.dngarcia.tareasdiarias.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dngarcia.tareasdiarias.domain.usecase.ReschedulePendingRemindersUseCase
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {
    @Inject
    lateinit var reschedulePendingRemindersUseCase: ReschedulePendingRemindersUseCase

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action
        val isBootAction = action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_LOCKED_BOOT_COMPLETED
        if (!isBootAction) return

        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                reschedulePendingRemindersUseCase()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
