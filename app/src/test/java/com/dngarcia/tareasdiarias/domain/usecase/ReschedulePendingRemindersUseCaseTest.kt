package com.dngarcia.tareasdiarias.domain.usecase

import com.dngarcia.tareasdiarias.domain.model.EstadoAlerta
import com.dngarcia.tareasdiarias.domain.model.Periodicidad
import com.dngarcia.tareasdiarias.domain.model.TaskReminder
import com.dngarcia.tareasdiarias.domain.model.TaskPeriodicityFilter
import com.dngarcia.tareasdiarias.domain.model.TaskSortOrder
import com.dngarcia.tareasdiarias.domain.model.TaskAdvancedFilters
import com.dngarcia.tareasdiarias.domain.model.Tarea
import com.dngarcia.tareasdiarias.domain.repository.TareaRepository
import com.dngarcia.tareasdiarias.domain.repository.TaskReminderScheduler
import java.time.LocalDateTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class ReschedulePendingRemindersUseCaseTest {
    @Test
    fun invoke_reschedulesAllPendingReminderTasks() = runBlocking {
        val now = LocalDateTime.of(2026, 5, 6, 12, 0)
        val pendingTasks = listOf(
            sampleTask(id = 1L, nombre = "Regar plantas", reminderAt = now.plusHours(1)),
            sampleTask(id = 2L, nombre = "Lavar platos", reminderAt = now.plusHours(2)),
        )
        val fakeRepository = FakeTareaRepository(pendingTasks)
        val fakeScheduler = FakeTaskReminderScheduler()
        val useCase = ReschedulePendingRemindersUseCase(fakeRepository, fakeScheduler)

        useCase()

        assertEquals(2, fakeScheduler.scheduledReminders.size)
        assertEquals("Regar plantas", fakeScheduler.scheduledReminders.first().taskTitle)
        assertEquals(false, fakeScheduler.scheduledReminders.first().requiresExactScheduling)
    }

    private fun sampleTask(id: Long, nombre: String, reminderAt: LocalDateTime): Tarea {
        return Tarea(
            id = id,
            nombre = nombre,
            subtitulo = "",
            categoriaId = 1L,
            tipoPeriodicidad = Periodicidad.DIARIA,
            diasPeriodicidad = null,
            notas = "",
            fechaInicio = reminderAt.toLocalDate(),
            fechaCreacion = reminderAt.minusDays(1),
            fechaUltimaModificacion = reminderAt.minusHours(12),
            fechaProximaEjecucion = reminderAt,
            horaRecordatorio = reminderAt.toLocalTime(),
            cantidadPostergaciones = 0,
            estadoAlerta = EstadoAlerta.NORMAL,
            mensajeAlerta = null,
        )
    }

    private class FakeTareaRepository(
        private val pendingTasks: List<Tarea>,
    ) : TareaRepository {
        override fun observeAll(): Flow<List<Tarea>> = emptyFlow()
        override fun observeTopPending(limit: Int): Flow<List<Tarea>> = emptyFlow()
        override fun observePendingByFilterAndSort(
            filter: TaskPeriodicityFilter,
            sortOrder: TaskSortOrder,
            searchQuery: String,
            includeNotesInSearch: Boolean,
            advancedFilters: TaskAdvancedFilters,
        ): Flow<List<Tarea>> = emptyFlow()
        override suspend fun getPendingReminderTasks(): List<Tarea> = pendingTasks
        override suspend fun getById(id: Long): Tarea? = null
        override suspend fun create(tarea: Tarea): Long = 0L
        override suspend fun update(tarea: Tarea) = Unit
        override suspend fun deleteById(id: Long) = Unit
        override suspend fun existsByNombre(nombre: String, excludeId: Long?): Boolean = false
    }

    private class FakeTaskReminderScheduler : TaskReminderScheduler {
        val scheduledReminders: MutableList<TaskReminder> = mutableListOf()

        override suspend fun schedule(reminder: TaskReminder) {
            scheduledReminders += reminder
        }

        override suspend fun cancel(taskId: Long) = Unit
    }
}
