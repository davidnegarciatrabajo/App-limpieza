package com.dngarcia.tareasdiarias.domain.usecase

import com.dngarcia.tareasdiarias.domain.repository.TareaRepository
import javax.inject.Inject

class ValidateUniqueTaskNameUseCase @Inject constructor(
    private val tareaRepository: TareaRepository,
) {
    suspend operator fun invoke(nombre: String, excludeTaskId: Long? = null): Boolean {
        val normalizedNombre = nombre.trim()
        if (normalizedNombre.isBlank()) return false
        return !tareaRepository.existsByNombre(
            nombre = normalizedNombre,
            excludeId = excludeTaskId,
        )
    }
}
