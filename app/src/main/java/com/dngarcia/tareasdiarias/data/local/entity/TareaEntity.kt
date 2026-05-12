package com.dngarcia.tareasdiarias.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.dngarcia.tareasdiarias.domain.model.EstadoAlerta
import com.dngarcia.tareasdiarias.domain.model.Periodicidad
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Entity(
    tableName = "tarea",
    foreignKeys = [
        ForeignKey(
            entity = CategoriaEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoria_id"],
            onDelete = ForeignKey.RESTRICT,
            onUpdate = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["nombre"], unique = true),
        Index(value = ["categoria_id"]),
        Index(value = ["fecha_proxima_ejecucion"]),
        Index(value = ["fecha_ultima_modificacion"]),
        Index(value = ["tipo_periodicidad"]),
    ],
)
data class TareaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    @ColumnInfo(name = "nombre")
    val nombre: String,
    @ColumnInfo(name = "subtitulo")
    val subtitulo: String = "",
    @ColumnInfo(name = "categoria_id")
    val categoriaId: Long,
    @ColumnInfo(name = "tipo_periodicidad")
    val tipoPeriodicidad: Periodicidad,
    @ColumnInfo(name = "dias_periodicidad")
    val diasPeriodicidad: Int?,
    @ColumnInfo(name = "notas")
    val notas: String,
    @ColumnInfo(name = "fecha_inicio")
    val fechaInicio: LocalDate,
    @ColumnInfo(name = "fecha_creacion")
    val fechaCreacion: LocalDateTime,
    @ColumnInfo(name = "fecha_ultima_modificacion")
    val fechaUltimaModificacion: LocalDateTime = fechaCreacion,
    @ColumnInfo(name = "fecha_proxima_ejecucion")
    val fechaProximaEjecucion: LocalDateTime?,
    @ColumnInfo(name = "hora_recordatorio")
    val horaRecordatorio: LocalTime?,
    @ColumnInfo(name = "cantidad_postergaciones")
    val cantidadPostergaciones: Int = 0,
    @ColumnInfo(name = "estado_alerta")
    val estadoAlerta: EstadoAlerta = EstadoAlerta.NORMAL,
    @ColumnInfo(name = "mensaje_alerta")
    val mensajeAlerta: String?,
)

