package com.dngarcia.tareasdiarias.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class TareaWithCategoria(
    @Embedded
    val tarea: TareaEntity,
    @Relation(
        parentColumn = "categoria_id",
        entityColumn = "id",
    )
    val categoria: CategoriaEntity,
)

