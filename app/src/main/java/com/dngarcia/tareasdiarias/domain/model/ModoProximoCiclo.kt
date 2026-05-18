package com.dngarcia.tareasdiarias.domain.model

/**
 * Cómo se calcula [Tarea.fechaProximaEjecucion] al **completar** una tarea recurrente.
 *
 * **Postergar** no usa este modo: solo ajusta visibilidad (`fechaVisibleDesde`), sin mover el ciclo.
 *
 * - [ANCLADO_FECHA_INICIO]: primera ocurrencia del calendario de periodicidad anclada en [Tarea.fechaInicio]
 *   que cae **estrictamente después** del día en que se completó (comportamiento histórico de la app).
 * - [INTERVALO_DESDE_COMPLETADO]: el siguiente ciclo es el **día de completado** más un paso del período
 *   (diario +1 día, semanal +7, mensual +1 mes con reglas de [java.time.LocalDate.plusMonths],
 *   semestral +6 meses, personalizado +N días). No fuerza el día de la semana del inicio.
 */
enum class ModoProximoCiclo {
    ANCLADO_FECHA_INICIO,
    INTERVALO_DESDE_COMPLETADO,
}
