package com.dngarcia.tareasdiarias.domain.usecase

import com.dngarcia.tareasdiarias.domain.model.Ejecucion
import com.dngarcia.tareasdiarias.domain.model.EstadoAlerta
import com.dngarcia.tareasdiarias.domain.model.Periodicidad
import com.dngarcia.tareasdiarias.domain.model.TaskAdvancedFilters
import com.dngarcia.tareasdiarias.domain.model.TaskPeriodicityFilter
import com.dngarcia.tareasdiarias.domain.model.TaskReminder
import com.dngarcia.tareasdiarias.domain.model.TaskSortOrder
import com.dngarcia.tareasdiarias.domain.model.Tarea
import com.dngarcia.tareasdiarias.domain.repository.EjecucionRepository
import com.dngarcia.tareasdiarias.domain.repository.TareaRepository
import com.dngarcia.tareasdiarias.domain.repository.TaskReminderScheduler
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertNull
import org.junit.Test

class UndoTaskCompletionUseCaseTest {
    @Test
    fun invoke_deletesTodayExecutionAndRestoresDueDate() = runBlocking {
        val repository = FakeTareaRepository()
        val executionRepository = FakeEjecucionRepository()
        val scheduler = FakeTaskReminderScheduler()
        val useCase = UndoTaskCompletionUseCase(
            tareaRepository = repository,
            ejecucionRepository = executionRepository,
            scheduleTaskReminderUseCase = ScheduleTaskReminderUseCase(scheduler),
            cancelTaskReminderUseCase = CancelTaskReminderUseCase(scheduler),
        )

        useCase(
            taskId = 7L,
            referenceTime = LocalDateTime.of(2026, 5, 11, 10, 0),
        )

        assertTrue(executionRepository.executions.isEmpty())
        assertEquals(LocalDateTime.of(2026, 5, 11, 0, 0), repository.updatedTask?.fechaProximaEjecucion)
        assertEquals(LocalDate.of(2026, 5, 11), repository.updatedTask?.fechaVisibleDesde)
        assertNull(repository.updatedTask?.ultimaVezQueHiceLaTarea)
        assertEquals(6, repository.updatedTask?.cantidadPostergaciones)
        assertEquals(LocalDateTime.of(2026, 5, 12, 8, 0), scheduler.lastScheduledReminder?.reminderAt)
    }

    private class FakeTareaRepository : TareaRepository {
        var updatedTask: Tarea? = null

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
            nombre = "Limpiar cocina",
            subtitulo = "",
            categoriaId = 1L,
            tipoPeriodicidad = Periodicidad.DIARIA,
            diasPeriodicidad = null,
            notas = "",
            fechaInicio = LocalDate.of(2026, 5, 1),
            fechaCreacion = LocalDateTime.of(2026, 5, 1, 8, 0),
            fechaUltimaModificacion = LocalDateTime.of(2026, 5, 11, 9, 30),
            fechaProximaEjecucion = LocalDateTime.of(2026, 5, 12, 0, 0),
            fechaVisibleDesde = LocalDate.of(2026, 5, 12),
            horaRecordatorio = LocalTime.of(8, 0),
            ultimaVezQueHiceLaTarea = LocalDateTime.of(2026, 5, 11, 9, 30),
            cantidadPostergaciones = 0,
            estadoAlerta = EstadoAlerta.NORMAL,
            mensajeAlerta = null,
        )

        override suspend fun create(tarea: Tarea): Long = 0L

        override suspend fun update(tarea: Tarea) {
            updatedTask = tarea
        }

        override suspend fun deleteById(id: Long) = Unit
        override suspend fun getByCategoryId(categoryId: Long): List<Tarea> = emptyList()
        override suspend fun countByCategoryId(categoryId: Long): Int = 0
        override suspend fun existsByNombre(nombre: String, excludeId: Long?): Boolean = false
    }

    private class FakeEjecucionRepository : EjecucionRepository {
        val executions = mutableListOf(
            Ejecucion(
                id = 1L,
                tareaId = 7L,
                fechaEjecucion = LocalDateTime.of(2026, 5, 11, 9, 30),
                fechaCicloResuelto = LocalDate.of(2026, 5, 11),
                completadaPorUsuario = true,
                cantidadPostergacionesPrevias = 6,
            ),
        )

        override fun observeByTareaId(tareaId: Long): Flow<List<Ejecucion>> = emptyFlow()

        override fun observeCompletedBetween(
            startInclusive: LocalDateTime,
            endInclusive: LocalDateTime,
        ): Flow<List<Ejecucion>> = emptyFlow()

        override suspend fun getCompletedBetween(
            startInclusive: LocalDateTime,
            endInclusive: LocalDateTime,
        ): List<Ejecucion> = executions.filter { it.fechaEjecucion in startInclusive..endInclusive }

        override suspend fun getLatestCompletedBetween(
            tareaId: Long,
            startInclusive: LocalDateTime,
            endInclusive: LocalDateTime,
        ): Ejecucion? = executions
            .filter { it.tareaId == tareaId && it.fechaEjecucion in startInclusive..endInclusive }
            .maxByOrNull { it.fechaEjecucion }

        override suspend fun getLatestCompletedForCycle(
            tareaId: Long,
            cycleDate: LocalDate,
        ): Ejecucion? = executions
            .filter { it.tareaId == tareaId && it.fechaCicloResuelto == cycleDate }
            .maxByOrNull { it.fechaEjecucion }

        override suspend fun getLatestCompletedByTaskId(tareaId: Long): Ejecucion? = executions
            .filter { it.tareaId == tareaId }
            .maxByOrNull { it.fechaEjecucion }

        override suspend fun create(ejecucion: Ejecucion): Long {
            executions += ejecucion
            return ejecucion.id
        }

        override suspend fun deleteById(id: Long) {
            executions.removeAll { it.id == id }
        }

        override suspend fun deleteByTareaId(tareaId: Long) {
            executions.removeAll { it.tareaId == tareaId }
        }
    }

    private class FakeTaskReminderScheduler : TaskReminderScheduler {
        var lastScheduledReminder: TaskReminder? = null

        override suspend fun schedule(reminder: TaskReminder) {
            lastScheduledReminder = reminder
        }

        override suspend fun cancel(taskId: Long) = Unit
    }
}
