package com.dngarcia.tareasdiarias.domain.usecase

import com.dngarcia.tareasdiarias.domain.model.Categoria
import com.dngarcia.tareasdiarias.domain.repository.CategoriaRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveCategoriasUseCase @Inject constructor(
    private val categoriaRepository: CategoriaRepository,
) {
    operator fun invoke(): Flow<List<Categoria>> = categoriaRepository.observeAll()
}
