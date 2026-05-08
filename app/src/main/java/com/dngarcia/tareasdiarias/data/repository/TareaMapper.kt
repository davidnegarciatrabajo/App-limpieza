package com.dngarcia.tareasdiarias.data.repository

import com.dngarcia.tareasdiarias.data.local.entity.TareaEntity
import com.dngarcia.tareasdiarias.domain.model.Tarea

internal fun TareaEntity.toDomain(): Tarea = Tarea(
    id = id,
    nombre = nombre,
    subtitulo = subtitulo,
    categoriaId = categoriaId,
    tipoPeriodicidad = tipoPeriodicidad,
    diasPeriodicidad = diasPeriodicidad,
    notas = notas,
    fechaCreacion = fechaCreacion,
    fechaUltimaModificacion = fechaUltimaModificacion,
    fechaProximaEjecucion = fechaProximaEjecucion,
    horaRecordatorio = horaRecordatorio,
    cantidadPostergaciones = cantidadPostergaciones,
    estadoAlerta = estadoAlerta,
    mensajeAlerta = mensajeAlerta,
)

internal fun Tarea.toEntity(): TareaEntity = TareaEntity(
    id = id,
    nombre = nombre,
    subtitulo = subtitulo,
    categoriaId = categoriaId,
    tipoPeriodicidad = tipoPeriodicidad,
    diasPeriodicidad = diasPeriodicidad,
    notas = notas,
    fechaCreacion = fechaCreacion,
    fechaUltimaModificacion = fechaUltimaModificacion,
    fechaProximaEjecucion = fechaProximaEjecucion,
    horaRecordatorio = horaRecordatorio,
    cantidadPostergaciones = cantidadPostergaciones,
    estadoAlerta = estadoAlerta,
    mensajeAlerta = mensajeAlerta,
)

