package com.dngarcia.tareasdiarias.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "categoria",
    indices = [
        Index(value = ["nombre"], unique = true),
    ],
)
data class CategoriaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    @ColumnInfo(name = "nombre")
    val nombre: String,
    @ColumnInfo(name = "color")
    val color: Int?,
)

