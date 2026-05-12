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
import org.junit.Assert.assertNotNull
import org.junit.Test

class CompleteTaskUseCaseTest {
    @Test
    fun invoke_createsExecutionAndMovesTaskToNextCycle() = runBlocking {
        val repository = FakeTareaRepository()
        val executionRepository = FakeEjecucionRepository()
        val scheduler = FakeTaskReminderScheduler()
        val useCase = CompleteTaskUseCase(
            tareaRepository = repository,
            ejecucionRepository = executionRepository,
            scheduleTaskReminderUseCase = ScheduleTaskReminderUseCase(scheduler),
            cancelTaskReminderUseCase = CancelTaskReminderUseCase(scheduler),
        )
        val completedAt = LocalDateTime.of(2026, 5, 11, 9, 30)

        useCase(
            taskId = 7L,
            completedAt = completedAt,
        )

        assertEquals(1, executionRepository.executions.size)
        assertEquals(completedAt, executionRepository.executions.single().fechaEjecucion)
        assertEquals(LocalDateTime.of(2026, 5, 12, 0, 0), repository.updatedTask?.fechaProximaEjecucion)
        assertNotNull(scheduler.lastScheduledReminder)
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
            fechaUltimaModificacion = LocalDateTime.of(2026, 5, 10, 8, 0),
            fechaProximaEjecucion = LocalDateTime.of(2026, 5, 11, 0, 0),
            horaRecordatorio = LocalTime.of(8, 0),
            cantidadPostergaciones = 0,
            estadoAlerta = EstadoAlerta.NORMAL,
            mensajeAlerta = null,
        )

        override suspend fun create(tarea: Tarea): Long = 0L

        override suspend fun update(tarea: Tarea) {
            updatedTask = tarea
        }

        override suspend fun deleteById(id: Long) = Unit
        override suspend fun existsByNombre(nombre: String, excludeId: Long?): Boolean = false
    }

    private class FakeEjecucionRepository : EjecucionRepository {
        val executions = mutableListOf<Ejecucion>()

        override fun observeByTareaId(tareaId: Long): Flow<List<Ejecucion>> = emptyFlow()

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

        override suspend fun create(ejecucion: Ejecucion): Long {
            executions += ejecucion.copy(id = (executions.size + 1).toLong())
            return executions.size.toLong()
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
