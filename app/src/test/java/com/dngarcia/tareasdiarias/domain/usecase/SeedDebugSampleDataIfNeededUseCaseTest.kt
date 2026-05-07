package com.dngarcia.tareasdiarias.domain.usecase

import com.dngarcia.tareasdiarias.domain.model.TaskAdvancedFilters
import com.dngarcia.tareasdiarias.domain.model.Tarea
import com.dngarcia.tareasdiarias.domain.model.TaskPeriodicityFilter
import com.dngarcia.tareasdiarias.domain.model.TaskSortOrder
import com.dngarcia.tareasdiarias.domain.model.Categoria
import com.dngarcia.tareasdiarias.domain.repository.CategoriaRepository
import com.dngarcia.tareasdiarias.domain.repository.TareaRepository
import com.dngarcia.tareasdiarias.domain.sampledata.DebugSampleDataGate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SeedDebugSampleDataIfNeededUseCaseTest {
    @Test
    fun invoke_debugFirstRun_createsCategoriesAndAtLeastTwentyTasks() = runBlocking {
        val fakeTareas = FakeTareaRepository()
        val fakeCategorias = FakeCategoriaRepository()
        val gate = FakeGate(isDebug = true, alreadySeeded = false)
        val useCase = SeedDebugSampleDataIfNeededUseCase(
            tareaRepository = fakeTareas,
            categoriaRepository = fakeCategorias,
            debugSampleDataGate = gate,
        )

        useCase()

        assertTrue(gate.markedSeeded)
        assertEquals(5, fakeCategorias.createdCount)
        assertTrue(fakeTareas.createdTasks.size >= 20)
    }

    @Test
    fun invoke_releaseBuild_doesNothing() = runBlocking {
        val fakeTareas = FakeTareaRepository()
        val fakeCategorias = FakeCategoriaRepository()
        val gate = FakeGate(isDebug = false, alreadySeeded = false)
        val useCase = SeedDebugSampleDataIfNeededUseCase(
            tareaRepository = fakeTareas,
            categoriaRepository = fakeCategorias,
            debugSampleDataGate = gate,
        )

        useCase()

        assertTrue(!gate.markedSeeded)
        assertEquals(0, fakeCategorias.createdCount)
        assertTrue(fakeTareas.createdTasks.isEmpty())
    }

    @Test
    fun invoke_debugAlreadySeeded_skips() = runBlocking {
        val fakeTareas = FakeTareaRepository()
        val fakeCategorias = FakeCategoriaRepository()
        val gate = FakeGate(isDebug = true, alreadySeeded = true)
        val useCase = SeedDebugSampleDataIfNeededUseCase(
            tareaRepository = fakeTareas,
            categoriaRepository = fakeCategorias,
            debugSampleDataGate = gate,
        )

        useCase()

        assertTrue(!gate.markedSeeded)
        assertEquals(0, fakeCategorias.createdCount)
    }

    private class FakeGate(
        private val isDebug: Boolean,
        private val alreadySeeded: Boolean,
    ) : DebugSampleDataGate {
        var markedSeeded = false

        override fun isDebugBuild(): Boolean = isDebug

        override fun wasSampleDataAlreadySeeded(): Boolean = alreadySeeded

        override fun markSampleDataSeeded() {
            markedSeeded = true
        }
    }

    private class FakeTareaRepository : TareaRepository {
        val createdTasks = mutableListOf<Tarea>()
        private var nextId = 1L

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
            createdTasks.add(tarea)
            return nextId++
        }

        override suspend fun update(tarea: Tarea) = Unit
        override suspend fun deleteById(id: Long) = Unit
        override suspend fun existsByNombre(nombre: String, excludeId: Long?): Boolean = false
    }

    private class FakeCategoriaRepository : CategoriaRepository {
        var createdCount = 0
        private var nextId = 10L

        override fun observeAll(): Flow<List<Categoria>> = emptyFlow()
        override suspend fun getById(id: Long): Categoria? = null
        override suspend fun create(categoria: Categoria): Long {
            createdCount++
            return nextId++
        }

        override suspend fun update(categoria: Categoria) = Unit
        override suspend fun deleteById(id: Long) = Unit
        override suspend fun existsByNombre(nombre: String, excludeId: Long?): Boolean = false
    }
}
