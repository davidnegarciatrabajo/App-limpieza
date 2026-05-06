package com.dngarcia.tareasdiarias.data.repository

import com.dngarcia.tareasdiarias.data.local.entity.EjecucionEntity
import com.dngarcia.tareasdiarias.domain.model.Ejecucion

internal fun EjecucionEntity.toDomain(): Ejecucion = Ejecucion(
    id = id,
    tareaId = tareaId,
    fechaEjecucion = fechaEjecucion,
    completadaPorUsuario = completadaPorUsuario,
)

internal fun Ejecucion.toEntity(): EjecucionEntity = EjecucionEntity(
    id = id,
    tareaId = tareaId,
    fechaEjecucion = fechaEjecucion,
    completadaPorUsuario = completadaPorUsuario,
)

