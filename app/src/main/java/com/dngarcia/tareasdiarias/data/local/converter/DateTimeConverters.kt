package com.dngarcia.tareasdiarias.data.local.converter

import androidx.room.TypeConverter
import com.dngarcia.tareasdiarias.domain.model.EstadoAlerta
import com.dngarcia.tareasdiarias.domain.model.Periodicidad
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset

class DateTimeConverters {
    @TypeConverter
    fun localDateTimeToEpochMillis(value: LocalDateTime?): Long? {
        return value?.toInstant(ZoneOffset.UTC)?.toEpochMilli()
    }

    @TypeConverter
    fun epochMillisToLocalDateTime(value: Long?): LocalDateTime? {
        return value?.let {
            LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneOffset.UTC)
        }
    }

    @TypeConverter
    fun localTimeToSecondsOfDay(value: LocalTime?): Int? {
        return value?.toSecondOfDay()
    }

    @TypeConverter
    fun secondsOfDayToLocalTime(value: Int?): LocalTime? {
        return value?.let { LocalTime.ofSecondOfDay(it.toLong()) }
    }

    @TypeConverter
    fun periodicidadToString(value: Periodicidad): String = value.name

    @TypeConverter
    fun stringToPeriodicidad(value: String): Periodicidad = Periodicidad.valueOf(value)

    @TypeConverter
    fun estadoAlertaToString(value: EstadoAlerta): String = value.name

    @TypeConverter
    fun stringToEstadoAlerta(value: String): EstadoAlerta = EstadoAlerta.valueOf(value)
}

