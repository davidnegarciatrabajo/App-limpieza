package com.dngarcia.tareasdiarias.domain.usecase

import com.dngarcia.tareasdiarias.domain.model.EstadoAlerta
import com.dngarcia.tareasdiarias.domain.model.Periodicidad
import com.dngarcia.tareasdiarias.domain.model.TaskAdvancedFilters
import com.dngarcia.tareasdiarias.domain.model.TaskPeriodicityFilter
import com.dngarcia.tareasdiarias.domain.model.TaskReminder
import com.dngarcia.tareasdiarias.domain.model.TaskSortOrder
import com.dngarcia.tareasdiarias.domain.model.Tarea
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

class PostponeTaskUseCaseTest {
    @Test
    fun invoke_keepsExpectedCycleAndMovesOnlyAppearanceDate() = runBlocking {
        val repository = FakeTareaRepository()
        val scheduler = FakeTaskReminderScheduler()
        val useCase = PostponeTaskUseCase(
            tareaRepository = repository,
            scheduleTaskReminderUseCase = ScheduleTaskReminderUseCase(scheduler),
            cancelTaskReminderUseCase = CancelTaskReminderUseCase(scheduler),
        )
        val referenceTime = LocalDateTime.of(2026, 5, 11, 10, 0)

        useCase(
            taskId = 7L,
            postponedUntil = LocalDate.of(2026, 5, 14),
            referenceTime = referenceTime,
        )

        assertEquals(LocalDateTime.of(2026, 5, 11, 0, 0), repository.updatedTask?.fechaProximaEjecucion)
        assertEquals(LocalDate.of(2026, 5, 14), repository.updatedTask?.fechaVisibleDesde)
        assertEquals(3, repository.updatedTask?.cantidadPostergaciones)
        assertNotNull(scheduler.lastScheduledReminder)
        assertEquals(LocalDateTime.of(2026, 5, 14, 9, 0), scheduler.lastScheduledReminder?.reminderAt)
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
            fechaVisibleDesde = LocalDate.of(2026, 5, 11),
            horaRecordatorio = LocalTime.of(9, 0),
            ultimaVezQueHiceLaTarea = null,
            cantidadPostergaciones = 2,
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

    private class FakeTaskReminderScheduler : TaskReminderScheduler {
        var lastScheduledReminder: TaskReminder? = null

        override suspend fun schedule(reminder: TaskReminder) {
            lastScheduledReminder = reminder
        }

        override suspend fun cancel(taskId: Long) = Unit
    }
}
