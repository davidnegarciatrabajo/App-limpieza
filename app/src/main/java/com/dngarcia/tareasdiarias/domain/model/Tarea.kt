package com.dngarcia.tareasdiarias.domain.model

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
    val fechaCreacion: LocalDateTime,
    val fechaUltimaModificacion: LocalDateTime = fechaCreacion,
    // `fechaProximaEjecucion` guarda el proximo vencimiento real y `horaRecordatorio` conserva la hora elegida para recalcular ciclos futuros.
    val fechaProximaEjecucion: LocalDateTime?,
    val horaRecordatorio: LocalTime?,
    val cantidadPostergaciones: Int,
    val estadoAlerta: EstadoAlerta,
    val mensajeAlerta: String?,
)

