package com.dngarcia.tareasdiarias.domain.usecase

import com.dngarcia.tareasdiarias.domain.model.Periodicidad
import com.dngarcia.tareasdiarias.domain.model.TaskReminder
import com.dngarcia.tareasdiarias.domain.model.TaskAdvancedFilters
import com.dngarcia.tareasdiarias.domain.model.Tarea
import com.dngarcia.tareasdiarias.domain.model.TaskPeriodicityFilter
import com.dngarcia.tareasdiarias.domain.model.TaskSortOrder
import com.dngarcia.tareasdiarias.domain.repository.TareaRepository
import com.dngarcia.tareasdiarias.domain.repository.TaskReminderScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class CreateTaskUseCaseTest {
    @Test
    fun invoke_createsTaskAndSchedulesReminder() = runBlocking {
        val fakeRepository = FakeTareaRepository()
        val fakeScheduler = FakeTaskReminderScheduler()
        val useCase = CreateTaskUseCase(
            tareaRepository = fakeRepository,
            scheduleTaskReminderUseCase = ScheduleTaskReminderUseCase(fakeScheduler),
        )

        val taskId = useCase(
            params = CreateTaskParams(
                nombre = "Limpiar cocina",
                subtitulo = "Encimera y hornallas",
                categoriaId = 2L,
                periodicidad = Periodicidad.DIARIA,
                diasPeriodicidad = null,
                notas = "Con detergente",
                horaRecordatorio = null,
            ),
        )

        assertEquals(99L, taskId)
        assertEquals("Limpiar cocina", fakeRepository.lastCreatedTask?.nombre)
        assertEquals("Encimera y hornallas", fakeRepository.lastCreatedTask?.subtitulo)
        assertNotNull(fakeScheduler.lastScheduledReminder)
        assertEquals(false, fakeScheduler.lastScheduledReminder?.requiresExactScheduling)
    }

    private class FakeTareaRepository : TareaRepository {
        var lastCreatedTask: Tarea? = null

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
        override suspend fun getById(id: Long): Tarea? = null
        override suspend fun create(tarea: Tarea): Long {
            lastCreatedTask = tarea
            return 99L
        }
        override suspend fun update(tarea: Tarea) = Unit
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
