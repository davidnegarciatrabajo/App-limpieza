package com.dngarcia.tareasdiarias.domain.usecase

import com.dngarcia.tareasdiarias.domain.model.Categoria
import com.dngarcia.tareasdiarias.domain.model.Ejecucion
import com.dngarcia.tareasdiarias.domain.model.EstadoAlerta
import com.dngarcia.tareasdiarias.domain.model.Periodicidad
import com.dngarcia.tareasdiarias.domain.model.TaskAdvancedFilters
import com.dngarcia.tareasdiarias.domain.model.TaskPeriodicityFilter
import com.dngarcia.tareasdiarias.domain.model.TaskSortOrder
import com.dngarcia.tareasdiarias.domain.model.TaskStatus
import com.dngarcia.tareasdiarias.domain.model.Tarea
import com.dngarcia.tareasdiarias.domain.repository.CategoriaRepository
import com.dngarcia.tareasdiarias.domain.repository.EjecucionRepository
import com.dngarcia.tareasdiarias.domain.repository.TareaRepository
import java.time.LocalDate
import java.time.LocalDateTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetTodayWidgetTasksUseCaseTest {
    @Test
    fun invoke_returnsPendingTasksFirstAndCompletedTodayAtBottom() = runBlocking {
        val useCase = GetTodayWidgetTasksUseCase(
            tareaRepository = FakeTareaRepository(),
            categoriaRepository = FakeCategoriaRepository(),
            ejecucionRepository = FakeEjecucionRepository(),
        )

        val result = useCase(referenceTime = LocalDateTime.of(2026, 5, 11, 10, 0))

        assertEquals(listOf(1L, 3L), result.map { it.task.id })
        assertEquals("Hogar", result.first().categoryName)
        assertEquals(false, result.first().completedToday)
        assertEquals(true, result.last().completedToday)
        assertEquals(TaskStatus.OK, result.last().status)
        assertTrue(result.last().completedAt != null)
    }

    private class FakeTareaRepository : TareaRepository {
        override fun observeAll(): Flow<List<Tarea>> = flowOf(
            listOf(
                baseTask(
                    id = 1L,
                    nombre = "Pagar servicios",
                    dueAt = LocalDateTime.of(2026, 5, 11, 0, 0),
                    postponements = 2,
                ),
                baseTask(
                    id = 2L,
                    nombre = "Limpiar ventanas",
                    dueAt = LocalDateTime.of(2026, 5, 13, 0, 0),
                ),
                baseTask(
                    id = 3L,
                    nombre = "Sacar basura",
                    dueAt = LocalDateTime.of(2026, 5, 12, 0, 0),
                ),
            ),
        )

        override fun observeTopPending(limit: Int): Flow<List<Tarea>> = flowOf(emptyList())
        override fun observePendingByFilterAndSort(
            filter: TaskPeriodicityFilter,
            sortOrder: TaskSortOrder,
            searchQuery: String,
            includeNotesInSearch: Boolean,
            advancedFilters: TaskAdvancedFilters,
        ): Flow<List<Tarea>> = flowOf(emptyList())

        override suspend fun getPendingReminderTasks(): List<Tarea> = emptyList()
        override suspend fun getById(id: Long): Tarea? = null
        override suspend fun create(tarea: Tarea): Long = 0L
        override suspend fun update(tarea: Tarea) = Unit
        override suspend fun deleteById(id: Long) = Unit
        override suspend fun existsByNombre(nombre: String, excludeId: Long?): Boolean = false

        private fun baseTask(
            id: Long,
            nombre: String,
            dueAt: LocalDateTime,
            postponements: Int = 0,
        ) = Tarea(
            id = id,
            nombre = nombre,
            subtitulo = "",
            categoriaId = 1L,
            tipoPeriodicidad = Periodicidad.DIARIA,
            diasPeriodicidad = null,
            notas = "",
            fechaInicio = LocalDate.of(2026, 5, 1),
            fechaCreacion = LocalDateTime.of(2026, 5, 1, 8, 0),
            fechaUltimaModificacion = LocalDateTime.of(2026, 5, 10, 9, 0),
            fechaProximaEjecucion = dueAt,
            horaRecordatorio = null,
            cantidadPostergaciones = postponements,
            estadoAlerta = EstadoAlerta.NORMAL,
            mensajeAlerta = null,
        )
    }

    private class FakeCategoriaRepository : CategoriaRepository {
        override fun observeAll(): Flow<List<Categoria>> = flowOf(
            listOf(Categoria(id = 1L, nombre = "Hogar", color = null)),
        )

        override suspend fun getById(id: Long): Categoria? = null
        override suspend fun create(categoria: Categoria): Long = 0L
        override suspend fun update(categoria: Categoria) = Unit
        override suspend fun deleteById(id: Long) = Unit
        override suspend fun existsByNombre(nombre: String, excludeId: Long?): Boolean = false
    }

    private class FakeEjecucionRepository : EjecucionRepository {
        private val executions = listOf(
            Ejecucion(
                id = 1L,
                tareaId = 3L,
                fechaEjecucion = LocalDateTime.of(2026, 5, 11, 9, 0),
                completadaPorUsuario = true,
            ),
        )

        override fun observeByTareaId(tareaId: Long): Flow<List<Ejecucion>> = flowOf(emptyList())

        override suspend fun getCompletedBetween(
            startInclusive: LocalDateTime,
            endInclusive: LocalDateTime,
        ): List<Ejecucion> = executions.filter { it.fechaEjecucion in startInclusive..endInclusive }

        override suspend fun getLatestCompletedBetween(
            tareaId: Long,
            startInclusive: LocalDateTime,
            endInclusive: LocalDateTime,
        ): Ejecucion? = executions.firstOrNull { it.tareaId == tareaId }

        override suspend fun create(ejecucion: Ejecucion): Long = 0L
        override suspend fun deleteById(id: Long) = Unit
        override suspend fun deleteByTareaId(tareaId: Long) = Unit
    }
}
