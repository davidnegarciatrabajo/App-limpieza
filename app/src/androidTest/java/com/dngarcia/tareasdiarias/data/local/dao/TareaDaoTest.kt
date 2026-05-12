package com.dngarcia.tareasdiarias.data.local.dao

import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dngarcia.tareasdiarias.data.local.TareasDatabase
import com.dngarcia.tareasdiarias.data.local.entity.CategoriaEntity
import com.dngarcia.tareasdiarias.data.local.entity.TareaEntity
import com.dngarcia.tareasdiarias.domain.model.EstadoAlerta
import com.dngarcia.tareasdiarias.domain.model.Periodicidad
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime

@RunWith(AndroidJUnit4::class)
class TareaDaoTest {
    private lateinit var database: TareasDatabase
    private lateinit var tareaDao: TareaDao
    private lateinit var categoriaDao: CategoriaDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            TareasDatabase::class.java,
        ).allowMainThreadQueries().build()
        tareaDao = database.tareaDao()
        categoriaDao = database.categoriaDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndRead_tarea_success() = runBlocking {
        val categoriaId = categoriaDao.insert(CategoriaEntity(nombre = "Hogar", color = null))
        val now = LocalDateTime.now()
        val tareaId = tareaDao.insert(
            TareaEntity(
                nombre = "Limpiar cocina",
                categoriaId = categoriaId,
                tipoPeriodicidad = Periodicidad.DIARIA,
                diasPeriodicidad = null,
                notas = "Uso diario",
                fechaInicio = now.toLocalDate(),
                fechaCreacion = now,
                fechaProximaEjecucion = now.plusDays(1),
                horaRecordatorio = null,
                cantidadPostergaciones = 0,
                estadoAlerta = EstadoAlerta.NORMAL,
                mensajeAlerta = null,
            ),
        )

        val saved = tareaDao.getById(tareaId)
        val all = tareaDao.observeAll().first()

        requireNotNull(saved)
        assertEquals("Limpiar cocina", saved.nombre)
        assertEquals(categoriaId, saved.categoriaId)
        assertEquals(1, all.size)
        assertTrue(tareaDao.existsByNombre("Limpiar cocina"))
    }

    @Test(expected = SQLiteConstraintException::class)
    fun insert_duplicateNombre_throwsConstraintException() {
        runBlocking {
            val categoriaId = categoriaDao.insert(CategoriaEntity(nombre = "General", color = 0xFFFFFF))
            val now = LocalDateTime.now()

            val first = TareaEntity(
                nombre = "Trapear piso",
                categoriaId = categoriaId,
                tipoPeriodicidad = Periodicidad.SEMANAL,
                diasPeriodicidad = null,
                notas = "",
                fechaInicio = now.toLocalDate(),
                fechaCreacion = now,
                fechaProximaEjecucion = now.plusDays(7),
                horaRecordatorio = null,
                cantidadPostergaciones = 0,
                estadoAlerta = EstadoAlerta.NORMAL,
                mensajeAlerta = null,
            )
            val duplicate = first.copy(id = 0L)

            tareaDao.insert(first)
            tareaDao.insert(duplicate)
        }
    }
}

