package com.dngarcia.tareasdiarias.domain.model

import java.time.LocalDateTime
import java.time.LocalDate

data class Ejecucion(
    val id: Long,
    val tareaId: Long,
    val fechaEjecucion: LocalDateTime,
    val fechaCicloResuelto: LocalDate?,
    val completadaPorUsuario: Boolean,
    /** Valor de [Tarea.cantidadPostergaciones] antes de completar; se usa al deshacer. */
    val cantidadPostergacionesPrevias: Int = 0,
)

