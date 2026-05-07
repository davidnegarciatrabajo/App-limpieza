package com.dngarcia.tareasdiarias.domain.usecase

import com.dngarcia.tareasdiarias.domain.model.Tarea
import com.dngarcia.tareasdiarias.domain.repository.TareaRepository
import javax.inject.Inject

class GetTaskByIdUseCase @Inject constructor(
    private val tareaRepository: TareaRepository,
) {
    suspend operator fun invoke(taskId: Long): Tarea? = tareaRepository.getById(taskId)
}
