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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ObserveTomorrowTasksUseCaseTest {
    @Test
    fun invoke_listsOnlyTasksWithExpectedCycleOnTomorrow_day() = runBlocking {
        val useCase = ObserveTomorrowTasksUseCase(
            tareaRepository = FakeTareaRepository(),
            categoriaRepository = FakeCategoriaRepository(),
            ejecucionRepository = FakeEjecucionRepository(),
        )

        val result = useCase(referenceTime = LocalDateTime.of(2026, 5, 11, 10, 0)).first()

        val completedOnMay12 = result.filter { it.completedToday }
        assertEquals(1, completedOnMay12.size)
        assertEquals(3L, completedOnMay12.first().task.id)
        assertEquals(TaskStatus.OK, completedOnMay12.first().status)

        val pending = result.filter { !it.completedToday }
        assertEquals(listOf(4L), pending.map { it.task.id })
        assertTrue(result.none { !it.completedToday && it.task.id == 1L })
        assertTrue(result.none { !it.completedToday && it.task.id == 2L })
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
                    dueAt = LocalDateTime.of(2026, 5, 11, 0, 0),
                    visibleFrom = LocalDate.of(2026, 5, 13),
                ),
                baseTask(
                    id = 3L,
                    nombre = "Sacar basura",
                    dueAt = LocalDateTime.of(2026, 5, 12, 0, 0),
                ),
                baseTask(
                    id = 4L,
                    nombre = "Regar plantas",
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
        override suspend fun getByCategoryId(categoryId: Long): List<Tarea> = emptyList()
        override suspend fun countByCategoryId(categoryId: Long): Int = 0
        override suspend fun existsByNombre(nombre: String, excludeId: Long?): Boolean = false

        private fun baseTask(
            id: Long,
            nombre: String,
            dueAt: LocalDateTime,
            visibleFrom: LocalDate = dueAt.toLocalDate(),
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
            fechaVisibleDesde = visibleFrom,
            horaRecordatorio = null,
            ultimaVezQueHiceLaTarea = null,
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
                fechaEjecucion = LocalDateTime.of(2026, 5, 12, 9, 0),
                fechaCicloResuelto = LocalDate.of(2026, 5, 12),
                completadaPorUsuario = true,
            ),
        )

        override fun observeByTareaId(tareaId: Long): Flow<List<Ejecucion>> = flowOf(emptyList())

        override fun observeCompletedBetween(
            startInclusive: LocalDateTime,
            endInclusive: LocalDateTime,
        ): Flow<List<Ejecucion>> = flowOf(
            executions.filter { it.fechaEjecucion >= startInclusive && it.fechaEjecucion <= endInclusive },
        )

        override suspend fun getCompletedBetween(
            startInclusive: LocalDateTime,
            endInclusive: LocalDateTime,
        ): List<Ejecucion> =
            executions.filter { it.fechaEjecucion >= startInclusive && it.fechaEjecucion <= endInclusive }

        override suspend fun getLatestCompletedBetween(
            tareaId: Long,
            startInclusive: LocalDateTime,
            endInclusive: LocalDateTime,
        ): Ejecucion? = executions.firstOrNull { it.tareaId == tareaId }

        override suspend fun getLatestCompletedForCycle(
            tareaId: Long,
            cycleDate: LocalDate,
        ): Ejecucion? =
            executions.firstOrNull {
                it.tareaId == tareaId && it.fechaCicloResuelto == cycleDate
            }

        override suspend fun getLatestCompletedByTaskId(tareaId: Long): Ejecucion? =
            executions.firstOrNull { it.tareaId == tareaId }

        override suspend fun create(ejecucion: Ejecucion): Long = 0L
        override suspend fun deleteById(id: Long) = Unit
        override suspend fun deleteByTareaId(tareaId: Long) = Unit
    }
}
