package com.dngarcia.tareasdiarias.domain.usecase

import com.dngarcia.tareasdiarias.domain.model.EstadoAlerta
import com.dngarcia.tareasdiarias.domain.model.Periodicidad
import com.dngarcia.tareasdiarias.domain.model.TaskAdvancedFilters
import com.dngarcia.tareasdiarias.domain.model.TaskPeriodicityFilter
import com.dngarcia.tareasdiarias.domain.model.TaskSortOrder
import com.dngarcia.tareasdiarias.domain.model.Tarea
import com.dngarcia.tareasdiarias.domain.repository.TareaRepository
import com.dngarcia.tareasdiarias.domain.repository.TaskReminderScheduler
import java.time.LocalDate
import java.time.LocalDateTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class DeleteTaskUseCaseTest {
    @Test
    fun invoke_cancelsReminderAndDeletesTask() = runBlocking {
        val repository = FakeTareaRepository()
        val scheduler = FakeTaskReminderScheduler()
        val useCase = DeleteTaskUseCase(
            tareaRepository = repository,
            cancelTaskReminderUseCase = CancelTaskReminderUseCase(scheduler),
        )

        useCase(taskId = 15L)

        assertEquals(15L, repository.deletedTaskId)
        assertEquals(15L, scheduler.cancelledTaskId)
    }

    private class FakeTareaRepository : TareaRepository {
        var deletedTaskId: Long? = null

        override fun observeAll(): Flow<List<Tarea>> = emptyFlow()
        override fun observeTopPending(limit: Int): Flow<List<Tarea>> = emptyFlow()
        override fun observePendingByFilterAndSort(
            filter: TaskPeriodicityFilter,
            sortOrder: TaskSortOrder,
            searchQuery: String,
            includeNotesInSearch: Boolean,
            advancedFilters: TaskAdvancedFilters,
        ): Flow<List<Tarea>> = emptyFlow()

        override suspend fun getPendingReminderTasks(): List<Tarea> = emptyList()

        override suspend fun getById(id: Long): Tarea = Tarea(
            id = id,
            nombre = "Limpiar patio",
            subtitulo = "",
            categoriaId = 1L,
            tipoPeriodicidad = Periodicidad.DIARIA,
            diasPeriodicidad = null,
            notas = "",
            fechaInicio = LocalDate.of(2026, 5, 1),
            fechaCreacion = LocalDateTime.of(2026, 5, 1, 9, 0),
            fechaUltimaModificacion = LocalDateTime.of(2026, 5, 2, 9, 0),
            fechaProximaEjecucion = LocalDateTime.of(2026, 5, 12, 0, 0),
            fechaVisibleDesde = LocalDate.of(2026, 5, 12),
            horaRecordatorio = java.time.LocalTime.of(8, 0),
            ultimaVezQueHiceLaTarea = null,
            cantidadPostergaciones = 0,
            estadoAlerta = EstadoAlerta.NORMAL,
            mensajeAlerta = null,
        )

        override suspend fun create(tarea: Tarea): Long = 0L
        override suspend fun update(tarea: Tarea) = Unit
        override suspend fun deleteById(id: Long) {
            deletedTaskId = id
        }
        override suspend fun getByCategoryId(categoryId: Long): List<Tarea> = emptyList()
        override suspend fun countByCategoryId(categoryId: Long): Int = 0
        override suspend fun existsByNombre(nombre: String, excludeId: Long?): Boolean = false
    }

    private class FakeTaskReminderScheduler : TaskReminderScheduler {
        var cancelledTaskId: Long? = null

        override suspend fun schedule(reminder: com.dngarcia.tareasdiarias.domain.model.TaskReminder) = Unit

        override suspend fun cancel(taskId: Long) {
            cancelledTaskId = taskId
        }
    }
}
