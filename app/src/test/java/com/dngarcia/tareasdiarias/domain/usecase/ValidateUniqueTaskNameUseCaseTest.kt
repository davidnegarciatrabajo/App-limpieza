package com.dngarcia.tareasdiarias.domain.usecase

import com.dngarcia.tareasdiarias.domain.model.Tarea
import com.dngarcia.tareasdiarias.domain.model.TaskAdvancedFilters
import com.dngarcia.tareasdiarias.domain.model.TaskPeriodicityFilter
import com.dngarcia.tareasdiarias.domain.model.TaskSortOrder
import com.dngarcia.tareasdiarias.domain.repository.TareaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidateUniqueTaskNameUseCaseTest {
    @Test
    fun invoke_returnsFalseWhenBlank() = runBlocking {
        val useCase = ValidateUniqueTaskNameUseCase(FakeTareaRepository(existsResult = false))
        assertFalse(useCase(nombre = "  "))
    }

    @Test
    fun invoke_returnsFalseWhenNameExists() = runBlocking {
        val useCase = ValidateUniqueTaskNameUseCase(FakeTareaRepository(existsResult = true))
        assertFalse(useCase(nombre = "Lavar auto"))
    }

    @Test
    fun invoke_returnsTrueWhenNameDoesNotExist() = runBlocking {
        val useCase = ValidateUniqueTaskNameUseCase(FakeTareaRepository(existsResult = false))
        assertTrue(useCase(nombre = "Lavar auto"))
    }

    private class FakeTareaRepository(
        private val existsResult: Boolean,
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
        override suspend fun getByCategoryId(categoryId: Long): List<Tarea> = emptyList()
        override suspend fun countByCategoryId(categoryId: Long): Int = 0
        override suspend fun existsByNombre(nombre: String, excludeId: Long?): Boolean = existsResult
    }
}
