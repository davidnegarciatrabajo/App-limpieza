package com.dngarcia.tareasdiarias.domain.usecase

import com.dngarcia.tareasdiarias.domain.model.Tarea
import com.dngarcia.tareasdiarias.domain.repository.TareaRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveTopPendingTasksUseCase @Inject constructor(
    private val tareaRepository: TareaRepository,
) {
    operator fun invoke(limit: Int = 10): Flow<List<Tarea>> {
        return tareaRepository.observeTopPending(limit = limit)
    }
}
