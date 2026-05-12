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
    // `fechaProximaEjecucion` representa la fecha base visible en Today; `horaRecordatorio`
    // solo agrega la hora exacta para la notificacion si el usuario la configuro.
    val fechaProximaEjecucion: LocalDateTime?,
    val horaRecordatorio: LocalTime?,
    val cantidadPostergaciones: Int,
    val estadoAlerta: EstadoAlerta,
    val mensajeAlerta: String?,
)

