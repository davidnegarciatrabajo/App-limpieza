package com.dngarcia.tareasdiarias.domain.usecase

import com.dngarcia.tareasdiarias.domain.model.Categoria
import com.dngarcia.tareasdiarias.domain.repository.CategoriaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GetCategoriaByIdUseCaseTest {
    @Test
    fun invoke_returnsCategoriaWhenExists() = runBlocking {
        val expected = Categoria(id = 3L, nombre = "Hogar", color = null)
        val useCase = GetCategoriaByIdUseCase(
            categoriaRepository = FakeCategoriaRepository(getByIdResult = expected),
        )

        val result = useCase(3L)

        assertEquals(expected, result)
    }

    @Test
    fun invoke_returnsNullWhenMissing() = runBlocking {
        val useCase = GetCategoriaByIdUseCase(
            categoriaRepository = FakeCategoriaRepository(getByIdResult = null),
        )

        val result = useCase(99L)

        assertNull(result)
    }

    private class FakeCategoriaRepository(
        private val getByIdResult: Categoria?,
    ) : CategoriaRepository {
        override fun observeAll(): Flow<List<Categoria>> = emptyFlow()

        override suspend fun getById(id: Long): Categoria? = getByIdResult

        override suspend fun create(categoria: Categoria): Long = 0L

        override suspend fun update(categoria: Categoria) = Unit

        override suspend fun deleteById(id: Long) = Unit

        override suspend fun existsByNombre(nombre: String, excludeId: Long?): Boolean = false
    }
}
