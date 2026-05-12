package com.dngarcia.tareasdiarias.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(
    tableName = "ejecucion",
    foreignKeys = [
        ForeignKey(
            entity = TareaEntity::class,
            parentColumns = ["id"],
            childColumns = ["tarea_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["tarea_id"]),
        Index(value = ["fecha_ejecucion"]),
    ],
)
data class EjecucionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    @ColumnInfo(name = "tarea_id")
    val tareaId: Long,
    @ColumnInfo(name = "fecha_ejecucion")
    val fechaEjecucion: LocalDateTime,
    @ColumnInfo(name = "fecha_ciclo_resuelto")
    val fechaCicloResuelto: LocalDate?,
    @ColumnInfo(name = "completada_por_usuario")
    val completadaPorUsuario: Boolean,
    @ColumnInfo(name = "cantidad_postergaciones_previas")
    val cantidadPostergacionesPrevias: Int = 0,
)

