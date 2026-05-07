package com.dngarcia.tareasdiarias.presentation.common

import android.database.SQLException
import android.database.sqlite.SQLiteException
import com.dngarcia.tareasdiarias.R

/**
 * Mapea fallos técnicos a mensajes seguros para el usuario.
 */
fun Throwable.toUserError(): UserError = when (this) {
    is SQLiteException, is SQLException -> UserError(R.string.error_storage)
    else -> UserError(R.string.error_generic)
}
