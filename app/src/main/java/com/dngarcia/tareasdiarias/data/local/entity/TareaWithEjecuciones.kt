package com.dngarcia.tareasdiarias.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class TareaWithEjecuciones(
    @Embedded
    val tarea: TareaEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "tarea_id",
    )
    val ejecuciones: List<EjecucionEntity>,
)

