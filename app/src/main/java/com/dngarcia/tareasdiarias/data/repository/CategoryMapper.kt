package com.dngarcia.tareasdiarias.data.repository

import com.dngarcia.tareasdiarias.data.local.entity.CategoriaEntity
import com.dngarcia.tareasdiarias.domain.model.Categoria

internal fun CategoriaEntity.toDomain(): Categoria = Categoria(
    id = id,
    nombre = nombre,
    color = color,
)

internal fun Categoria.toEntity(): CategoriaEntity = CategoriaEntity(
    id = id,
    nombre = nombre,
    color = color,
)

