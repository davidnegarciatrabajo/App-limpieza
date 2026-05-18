package com.dngarcia.tareasdiarias.domain.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class Tarea(
    val id: Long,
    val nombre: String,
    val subtitulo: String = "",
    val categoriaId: Long,
    val tipoPeriodicidad: Periodicidad,
    val diasPeriodicidad: Int?,
    val notas: String,
    val fechaInicio: LocalDate,
    val fechaCreacion: LocalDateTime,
    val fechaUltimaModificacion: LocalDateTime = fechaCreacion,
    val modoProximoCiclo: ModoProximoCiclo = ModoProximoCiclo.ANCLADO_FECHA_INICIO,
    // Próximo ciclo esperado; al completar, su cálculo depende de `modoProximoCiclo`.
    val fechaProximaEjecucion: LocalDateTime?,
    // `fechaVisibleDesde` controla cuando vuelve a aparecer en Today tras una postergacion.
    val fechaVisibleDesde: LocalDate?,
    val horaRecordatorio: LocalTime?,
    val ultimaVezQueHiceLaTarea: LocalDateTime?,
    val cantidadPostergaciones: Int,
    val estadoAlerta: EstadoAlerta,
    val mensajeAlerta: String?,
)

