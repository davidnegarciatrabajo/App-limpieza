package com.dngarcia.tareasdiarias.domain.usecase

import com.dngarcia.tareasdiarias.domain.usecase.TodayWidgetTask
import com.dngarcia.tareasdiarias.domain.repository.CategoriaRepository
import com.dngarcia.tareasdiarias.domain.repository.EjecucionRepository
import com.dngarcia.tareasdiarias.domain.repository.TareaRepository
import java.time.LocalDateTime
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ObserveTodayTasksUseCase @Inject constructor(
    private val tareaRepository: TareaRepository,
    private val categoriaRepository: CategoriaRepository,
    private val ejecucionRepository: EjecucionRepository,
) {
    operator fun invoke(
        referenceTime: LocalDateTime = LocalDateTime.now(),
    ): Flow<List<TodayWidgetTask>> {
        val today = referenceTime.toLocalDate()
        val dayStart = today.atStartOfDay()
        val dayEnd = today.plusDays(1).atStartOfDay().minusNanos(1)
        return combine(
            tareaRepository.observeAll(),
            categoriaRepository.observeAll(),
            ejecucionRepository.observeCompletedBetween(
                startInclusive = dayStart,
                endInclusive = dayEnd,
            ),
        ) { tasks, categories, executions ->
            buildTodayTaskList(
                tasks = tasks,
                categoriesById = categories.associate { it.id to it.nombre },
                completedByTaskId = executions.groupBy { it.tareaId }
                    .mapValues { (_, items) -> items.maxByOrNull { it.fechaEjecucion } },
                referenceTime = referenceTime,
            )
        }
    }
}
