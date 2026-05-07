package com.dngarcia.tareasdiarias.data.sampledata

import android.content.Context
import com.dngarcia.tareasdiarias.BuildConfig
import com.dngarcia.tareasdiarias.domain.sampledata.DebugSampleDataGate
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DebugSampleDataGateImpl @Inject constructor(
    @ApplicationContext context: Context,
) : DebugSampleDataGate {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun isDebugBuild(): Boolean = BuildConfig.DEBUG

    override fun wasSampleDataAlreadySeeded(): Boolean =
        prefs.getBoolean(KEY_SEEDED, false)

    override fun markSampleDataSeeded() {
        prefs.edit().putBoolean(KEY_SEEDED, true).apply()
    }

    companion object {
        private const val PREFS_NAME = "debug_sample_data"
        private const val KEY_SEEDED = "sample_seeded_v1"
    }
}
