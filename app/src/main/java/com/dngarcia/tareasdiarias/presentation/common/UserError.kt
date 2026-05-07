package com.dngarcia.tareasdiarias.presentation.common

import androidx.annotation.StringRes

/**
 * Mensaje de error listo para mostrar al usuario (recurso de string, sin texto hardcodeado en UI).
 */
data class UserError(
    @param:StringRes val messageResId: Int,
    val formatArgs: Array<Any> = emptyArray(),
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UserError

        if (messageResId != other.messageResId) return false
        if (!formatArgs.contentEquals(other.formatArgs)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = messageResId
        result = 31 * result + formatArgs.contentHashCode()
        return result
    }
}
