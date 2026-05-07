package com.dngarcia.tareasdiarias.domain.usecase

import com.dngarcia.tareasdiarias.domain.model.Categoria
import com.dngarcia.tareasdiarias.domain.repository.CategoriaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class UpdateCategoriaUseCaseTest {
    @Test
    fun invoke_updatesThroughRepository() = runBlocking {
        val categoria = Categoria(id = 2L, nombre = "Trabajo", color = 0xFF00FF)
        val fake = FakeCategoriaRepository()
        val useCase = UpdateCategoriaUseCase(fake)

        useCase(categoria)

        assertEquals(categoria, fake.lastUpdated)
    }

    private class FakeCategoriaRepository : CategoriaRepository {
        var lastUpdated: Categoria? = null

        override fun observeAll(): Flow<List<Categoria>> = emptyFlow()

        override suspend fun getById(id: Long): Categoria? = null

        override suspend fun create(categoria: Categoria): Long = 0L

        override suspend fun update(categoria: Categoria) {
            lastUpdated = categoria
        }

        override suspend fun deleteById(id: Long) = Unit

        override suspend fun existsByNombre(nombre: String, excludeId: Long?): Boolean = false
    }
}
