package com.dngarcia.tareasdiarias.domain.usecase

import com.dngarcia.tareasdiarias.domain.model.TaskStatus
import com.dngarcia.tareasdiarias.domain.model.Tarea
import com.dngarcia.tareasdiarias.domain.repository.CategoriaRepository
import com.dngarcia.tareasdiarias.domain.repository.EjecucionRepository
import com.dngarcia.tareasdiarias.domain.repository.TareaRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalDateTime

data class TodayWidgetTask(
    val task: Tarea,
    val categoryName: String,
    val status: TaskStatus,
    val daysDelta: Long?,
    val completedToday: Boolean,
    val completedAt: LocalDateTime?,
)

class GetTodayWidgetTasksUseCase @Inject constructor(
    private val tareaRepository: TareaRepository,
    private val categoriaRepository: CategoriaRepository,
    private val ejecucionRepository: EjecucionRepository,
) {
    suspend operator fun invoke(
        referenceTime: LocalDateTime = LocalDateTime.now(),
    ): List<TodayWidgetTask> {
        val today = referenceTime.toLocalDate()
        val dayStart = today.atStartOfDay()
        val dayEnd = today.plusDays(1).atStartOfDay().minusNanos(1)

        val tasks = tareaRepository.observeAll().first()
        val categoriesById = categoriaRepository.observeAll().first().associateBy { it.id }
        val completedByTaskId = ejecucionRepository.getCompletedBetween(
            startInclusive = dayStart,
            endInclusive = dayEnd,
        ).groupBy { it.tareaId }
            .mapValues { (_, executions) -> executions.maxByOrNull { it.fechaEjecucion } }

        return buildTodayTaskList(
            tasks = tasks,
            categoriesById = categoriesById.mapValues { it.value.nombre },
            completedByTaskId = completedByTaskId,
            referenceTime = referenceTime,
        )
    }
}

internal fun buildTodayTaskList(
    tasks: List<Tarea>,
    categoriesById: Map<Long, String>,
    completedByTaskId: Map<Long, com.dngarcia.tareasdiarias.domain.model.Ejecucion?>,
    referenceTime: LocalDateTime,
): List<TodayWidgetTask> {
    val today = referenceTime.toLocalDate()
    val pendingTasks = tasks
        .asSequence()
        .filter { task ->
            TaskTimelinePolicy.shouldAppearOnDate(task, today) && task.id !in completedByTaskId
        }
        .sortedWith(comparePendingTodayTasks(referenceTime))
        .map { task ->
            task.toTodayWidgetTask(
                categoryName = categoriesById[task.categoriaId].orEmpty(),
                completedToday = false,
                completedAt = null,
                completedForExpectedCycle = false,
                now = referenceTime,
            )
        }
        .toList()

    val completedTasks = tasks
        .asSequence()
        .filter { task -> completedByTaskId.containsKey(task.id) }
        .sortedWith(
            compareByDescending<Tarea> { completedByTaskId[it.id]?.fechaEjecucion }
                .thenBy { it.nombre.lowercase() }
        )
        .map { task ->
            task.toTodayWidgetTask(
                categoryName = categoriesById[task.categoriaId].orEmpty(),
                completedToday = true,
                completedAt = completedByTaskId[task.id]?.fechaEjecucion,
                completedForExpectedCycle = true,
                now = referenceTime,
            )
        }
        .toList()

    return pendingTasks + completedTasks
}

private fun comparePendingTodayTasks(referenceTime: LocalDateTime): Comparator<Tarea> {
    return compareByDescending<Tarea> { todayOverdueDays(it, referenceTime) }
        .thenBy { it.fechaProximaEjecucion ?: LocalDate.MAX.atStartOfDay() }
        .thenByDescending { it.fechaCreacion }
}

private fun todayOverdueDays(task: Tarea, referenceTime: LocalDateTime): Long {
    val statusInfo = TaskStatusResolver.resolve(task = task, now = referenceTime)
    return if (statusInfo.status == TaskStatus.VENCIDA) {
        statusInfo.daysDelta ?: 0L
    } else {
        -1L
    }
}

private fun Tarea.toTodayWidgetTask(
    categoryName: String,
    completedToday: Boolean,
    completedAt: LocalDateTime?,
    completedForExpectedCycle: Boolean,
    now: LocalDateTime,
): TodayWidgetTask {
    val statusInfo = TaskStatusResolver.resolve(
        task = this,
        now = now,
        completedForExpectedCycle = completedForExpectedCycle,
    )
    return TodayWidgetTask(
        task = this,
        categoryName = categoryName,
        status = if (completedToday) TaskStatus.OK else statusInfo.status,
        daysDelta = statusInfo.daysDelta,
        completedToday = completedToday,
        completedAt = completedAt,
    )
}
