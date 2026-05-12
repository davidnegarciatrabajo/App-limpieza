package com.dngarcia.tareasdiarias.domain.usecase

import com.dngarcia.tareasdiarias.domain.model.Categoria
import com.dngarcia.tareasdiarias.domain.model.TaskAdvancedFilters
import com.dngarcia.tareasdiarias.domain.model.TaskPeriodicityFilter
import com.dngarcia.tareasdiarias.domain.model.TaskSortOrder
import com.dngarcia.tareasdiarias.domain.model.Tarea
import com.dngarcia.tareasdiarias.domain.repository.CategoriaRepository
import com.dngarcia.tareasdiarias.domain.repository.TareaRepository
import com.dngarcia.tareasdiarias.domain.model.EstadoAlerta
import com.dngarcia.tareasdiarias.domain.model.Periodicidad
import com.dngarcia.tareasdiarias.domain.model.TaskReminder
import com.dngarcia.tareasdiarias.domain.repository.TaskReminderScheduler
import java.time.LocalDate
import java.time.LocalDateTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DeleteCategoryUseCaseTest {
    @Test
    fun invoke_whenCategoryHasTasks_cancelsRemindersAndDeletesCategory() = runBlocking {
        val categoriaRepository = FakeCategoriaRepository()
        val tareaRepository = FakeTareaRepository(taskCount = 2)
        val scheduler = FakeTaskReminderScheduler()
        val useCase = DeleteCategoryUseCase(
            categoriaRepository = categoriaRepository,
            tareaRepository = tareaRepository,
            cancelTaskReminderUseCase = CancelTaskReminderUseCase(scheduler),
        )

        useCase(categoryId = 7L)

        assertEquals(7L, categoriaRepository.deletedCategoryId)
        assertEquals(listOf(101L, 102L), scheduler.cancelledTaskIds)
    }

    @Test
    fun invoke_whenCategoryHasNoTasks_deletesCategory() = runBlocking {
        val categoriaRepository = FakeCategoriaRepository()
        val scheduler = FakeTaskReminderScheduler()
        val useCase = DeleteCategoryUseCase(
            categoriaRepository = categoriaRepository,
            tareaRepository = FakeTareaRepository(taskCount = 0),
            cancelTaskReminderUseCase = CancelTaskReminderUseCase(scheduler),
        )

        useCase(categoryId = 7L)

        assertEquals(7L, categoriaRepository.deletedCategoryId)
        assertTrue(scheduler.cancelledTaskIds.isEmpty())
    }

    private class FakeCategoriaRepository : CategoriaRepository {
        var deletedCategoryId: Long? = null

        override fun observeAll(): Flow<List<Categoria>> = emptyFlow()
        override suspend fun getById(id: Long): Categoria? = null
        override suspend fun create(categoria: Categoria): Long = 0L
        override suspend fun update(categoria: Categoria) = Unit
        override suspend fun deleteById(id: Long) {
            deletedCategoryId = id
        }
        override suspend fun existsByNombre(nombre: String, excludeId: Long?): Boolean = false
    }

    private class FakeTareaRepository(
        private val taskCount: Int,
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

        override suspend fun getPendingReminderTasks(): List<Tarea> = emptyList()
        override suspend fun getById(id: Long): Tarea? = null
        override suspend fun create(tarea: Tarea): Long = 0L
        override suspend fun update(tarea: Tarea) = Unit
        override suspend fun deleteById(id: Long) = Unit
        override suspend fun getByCategoryId(categoryId: Long): List<Tarea> {
            return List(taskCount) { index ->
                Tarea(
                    id = 101L + index,
                    nombre = "Tarea ${index + 1}",
                    subtitulo = "",
                    categoriaId = categoryId,
                    tipoPeriodicidad = Periodicidad.DIARIA,
                    diasPeriodicidad = null,
                    notas = "",
                    fechaInicio = LocalDate.of(2026, 5, 1),
                    fechaCreacion = LocalDateTime.of(2026, 5, 1, 8, 0),
                    fechaUltimaModificacion = LocalDateTime.of(2026, 5, 1, 8, 0),
                    fechaProximaEjecucion = LocalDateTime.of(2026, 5, 11, 0, 0),
                    fechaVisibleDesde = LocalDate.of(2026, 5, 11),
                    horaRecordatorio = null,
                    ultimaVezQueHiceLaTarea = null,
                    cantidadPostergaciones = 0,
                    estadoAlerta = EstadoAlerta.NORMAL,
                    mensajeAlerta = null,
                )
            }
        }
        override suspend fun countByCategoryId(categoryId: Long): Int = taskCount
        override suspend fun existsByNombre(nombre: String, excludeId: Long?): Boolean = false
    }

    private class FakeTaskReminderScheduler : TaskReminderScheduler {
        val cancelledTaskIds = mutableListOf<Long>()

        override suspend fun schedule(reminder: TaskReminder) = Unit

        override suspend fun cancel(taskId: Long) {
            cancelledTaskIds += taskId
        }
    }
}
