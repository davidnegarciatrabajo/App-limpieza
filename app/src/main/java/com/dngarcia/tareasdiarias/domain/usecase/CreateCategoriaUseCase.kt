package com.dngarcia.tareasdiarias.domain.usecase

import com.dngarcia.tareasdiarias.domain.model.Categoria
import com.dngarcia.tareasdiarias.domain.repository.CategoriaRepository
import javax.inject.Inject

class CreateCategoriaUseCase @Inject constructor(
    private val categoriaRepository: CategoriaRepository,
) {
    suspend operator fun invoke(nombre: String): Long {
        val normalizedNombre = nombre.trim()
        return categoriaRepository.create(
            categoria = Categoria(
                id = 0L,
                nombre = normalizedNombre,
                color = null,
            ),
        )
    }
}
