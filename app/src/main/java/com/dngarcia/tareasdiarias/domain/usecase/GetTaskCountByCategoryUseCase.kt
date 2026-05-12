package com.dngarcia.tareasdiarias.domain.usecase

import com.dngarcia.tareasdiarias.domain.repository.TareaRepository
import javax.inject.Inject

class GetTaskCountByCategoryUseCase @Inject constructor(
    private val tareaRepository: TareaRepository,
) {
    suspend operator fun invoke(categoryId: Long): Int = tareaRepository.countByCategoryId(categoryId)
}
