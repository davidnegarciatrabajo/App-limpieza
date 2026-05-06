package com.dngarcia.tareasdiarias.domain.model

import java.time.LocalDateTime

data class Ejecucion(
    val id: Long,
    val tareaId: Long,
    val fechaEjecucion: LocalDateTime,
    val completadaPorUsuario: Boolean,
)

