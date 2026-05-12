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
    val hoursUntilDue: Long?,
    val daysDelta: Long?,
    val lastModifiedAt: LocalDateTime,
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

        val pendingTasks = tasks
            .asSequence()
            .filter { task ->
                val dueDate = task.fechaProximaEjecucion?.toLocalDate() ?: return@filter false
                !dueDate.isAfter(today) && task.id !in completedByTaskId
            }
            .sortedWith(comparePendingTasks())
            .map { task ->
                task.toTodayWidgetTask(
                    categoryName = categoriesById[task.categoriaId]?.nombre.orEmpty(),
                    completedToday = false,
                    completedAt = null,
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
                    categoryName = categoriesById[task.categoriaId]?.nombre.orEmpty(),
                    completedToday = true,
                    completedAt = completedByTaskId[task.id]?.fechaEjecucion,
                    now = referenceTime,
                )
            }
            .toList()

        return pendingTasks + completedTasks
    }

    private fun comparePendingTasks(): Comparator<Tarea> {
        return compareByDescending<Tarea> { it.cantidadPostergaciones }
            .thenBy { it.fechaProximaEjecucion ?: LocalDate.MAX.atStartOfDay() }
            .thenByDescending { it.fechaCreacion }
    }
}

private fun Tarea.toTodayWidgetTask(
    categoryName: String,
    completedToday: Boolean,
    completedAt: LocalDateTime?,
    now: LocalDateTime,
): TodayWidgetTask {
    val statusInfo = TaskStatusResolver.resolve(task = this, now = now)
    return TodayWidgetTask(
        task = this,
        categoryName = categoryName,
        status = if (completedToday) TaskStatus.OK else statusInfo.status,
        hoursUntilDue = statusInfo.hoursUntilDue,
        daysDelta = statusInfo.daysDelta,
        lastModifiedAt = fechaUltimaModificacion,
        completedToday = completedToday,
        completedAt = completedAt,
    )
}
