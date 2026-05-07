package com.dngarcia.tareasdiarias.domain.usecase

import com.dngarcia.tareasdiarias.domain.model.Categoria
import com.dngarcia.tareasdiarias.domain.repository.CategoriaRepository
import javax.inject.Inject

class GetCategoriaByIdUseCase @Inject constructor(
    private val categoriaRepository: CategoriaRepository,
) {
    suspend operator fun invoke(categoriaId: Long): Categoria? = categoriaRepository.getById(categoriaId)
}
