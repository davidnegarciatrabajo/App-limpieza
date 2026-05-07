package com.dngarcia.tareasdiarias.domain.sampledata

/**
 * Controla si el seed de datos de prueba (solo debug) ya se ejecuto en este dispositivo.
 */
interface DebugSampleDataGate {
    fun isDebugBuild(): Boolean
    fun wasSampleDataAlreadySeeded(): Boolean
    fun markSampleDataSeeded()
}
