package com.dngarcia.tareasdiarias.domain.usecase

import com.dngarcia.tareasdiarias.domain.model.EstadoAlerta
import com.dngarcia.tareasdiarias.domain.model.Periodicidad
import com.dngarcia.tareasdiarias.domain.model.TaskReminder
import com.dngarcia.tareasdiarias.domain.model.TaskAdvancedFilters
import com.dngarcia.tareasdiarias.domain.model.Tarea
import com.dngarcia.tareasdiarias.domain.model.TaskPeriodicityFilter
import com.dngarcia.tareasdiarias.domain.model.TaskSortOrder
import com.dngarcia.tareasdiarias.domain.repository.TareaRepository
import com.dngarcia.tareasdiarias.domain.repository.TaskReminderScheduler
import java.time.LocalDateTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class UpdateTaskUseCaseTest {
    @Test
    fun invoke_updatesTaskAndSchedulesReminder() = runBlocking {
        val fakeRepository = FakeTareaRepository()
        val fakeScheduler = FakeTaskReminderScheduler()
        val useCase = UpdateTaskUseCase(
            tareaRepository = fakeRepository,
            scheduleTaskReminderUseCase = ScheduleTaskReminderUseCase(fakeScheduler),
            cancelTaskReminderUseCase = CancelTaskReminderUseCase(fakeScheduler),
        )

        useCase(
            params = UpdateTaskParams(
                taskId = 44L,
                nombre = "Tarea editada",
                categoriaId = 3L,
                notas = "Notas editadas",
                periodicidad = Periodicidad.SEMANAL,
                diasPeriodicidad = null,
            ),
        )

        assertEquals("Tarea editada", fakeRepository.lastUpdatedTask?.nombre)
        assertNotNull(fakeScheduler.lastScheduledReminder)
        assertEquals(false, fakeScheduler.lastScheduledReminder?.requiresExactScheduling)
    }

    private class FakeTareaRepository : TareaRepository {
        var lastUpdatedTask: Tarea? = null

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
        override suspend fun getById(id: Long): Tarea {
            return Tarea(
                id = id,
                nombre = "Base",
                categoriaId = 1L,
                tipoPeriodicidad = Periodicidad.DIARIA,
                diasPeriodicidad = null,
                notas = "",
                fechaCreacion = LocalDateTime.now(),
                fechaProximaEjecucion = LocalDateTime.now().plusDays(1),
                cantidadPostergaciones = 0,
                estadoAlerta = EstadoAlerta.NORMAL,
                mensajeAlerta = null,
            )
        }
        override suspend fun create(tarea: Tarea): Long = 0L
        override suspend fun update(tarea: Tarea) {
            lastUpdatedTask = tarea
        }
        override suspend fun deleteById(id: Long) = Unit
        override suspend fun existsByNombre(nombre: String, excludeId: Long?): Boolean = false
    }

    private class FakeTaskReminderScheduler : TaskReminderScheduler {
        var lastScheduledReminder: TaskReminder? = null
        override suspend fun schedule(reminder: TaskReminder) {
            lastScheduledReminder = reminder
        }
        override suspend fun cancel(taskId: Long) = Unit
    }
}
