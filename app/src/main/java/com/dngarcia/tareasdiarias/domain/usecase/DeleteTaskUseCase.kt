package com.dngarcia.tareasdiarias.domain.usecase

import com.dngarcia.tareasdiarias.domain.repository.TareaRepository
import javax.inject.Inject

class DeleteTaskUseCase @Inject constructor(
    private val tareaRepository: TareaRepository,
    private val cancelTaskReminderUseCase: CancelTaskReminderUseCase,
) {
    suspend operator fun invoke(taskId: Long) {
        val task = tareaRepository.getById(taskId) ?: return
        cancelTaskReminderUseCase(task.id)
        tareaRepository.deleteById(task.id)
    }
}
