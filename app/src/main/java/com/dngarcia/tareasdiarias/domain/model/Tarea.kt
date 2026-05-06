package com.dngarcia.tareasdiarias.domain.model

import java.time.LocalDateTime

data class Tarea(
    val id: Long,
    val nombre: String,
    val categoriaId: Long,
    val tipoPeriodicidad: Periodicidad,
    val diasPeriodicidad: Int?,
    val notas: String,
    val fechaCreacion: LocalDateTime,
    val fechaProximaEjecucion: LocalDateTime?,
    val cantidadPostergaciones: Int,
    val estadoAlerta: EstadoAlerta,
    val mensajeAlerta: String?,
)

