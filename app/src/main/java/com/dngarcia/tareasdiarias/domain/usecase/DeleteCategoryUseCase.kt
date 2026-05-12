package com.dngarcia.tareasdiarias.domain.usecase

import com.dngarcia.tareasdiarias.domain.repository.CategoriaRepository
import com.dngarcia.tareasdiarias.domain.repository.TareaRepository
import javax.inject.Inject

class DeleteCategoryUseCase @Inject constructor(
    private val categoriaRepository: CategoriaRepository,
    private val tareaRepository: TareaRepository,
    private val cancelTaskReminderUseCase: CancelTaskReminderUseCase,
) {
    suspend operator fun invoke(categoryId: Long) {
        val tasks = tareaRepository.getByCategoryId(categoryId)
        tasks.forEach { task ->
            cancelTaskReminderUseCase(task.id)
        }
        categoriaRepository.deleteById(categoryId)
    }
}
