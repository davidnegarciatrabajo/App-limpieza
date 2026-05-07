package com.dngarcia.tareasdiarias.domain.usecase

import com.dngarcia.tareasdiarias.domain.model.EstadoAlerta
import com.dngarcia.tareasdiarias.domain.model.Periodicidad
import com.dngarcia.tareasdiarias.domain.model.TaskStatus
import com.dngarcia.tareasdiarias.domain.model.Tarea
import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Test

class TaskStatusResolverTest {
    @Test
    fun resolve_whenDueDatePassed_returnsVencida() {
        val now = LocalDateTime.of(2026, 5, 6, 10, 0)
        val task = buildTask(dueAt = now.minusHours(2))

        val result = TaskStatusResolver.resolve(task = task, now = now)

        assertEquals(TaskStatus.VENCIDA, result.status)
    }

    @Test
    fun resolve_whenDueDateInNext24Hours_returnsProxima() {
        val now = LocalDateTime.of(2026, 5, 6, 10, 0)
        val task = buildTask(dueAt = now.plusHours(8))

        val result = TaskStatusResolver.resolve(task = task, now = now)

        assertEquals(TaskStatus.PROXIMA, result.status)
    }

    @Test
    fun resolve_whenDueDateAfterThreshold_returnsOk() {
        val now = LocalDateTime.of(2026, 5, 6, 10, 0)
        val task = buildTask(dueAt = now.plusDays(3))

        val result = TaskStatusResolver.resolve(task = task, now = now)

        assertEquals(TaskStatus.OK, result.status)
    }

    private fun buildTask(dueAt: LocalDateTime): Tarea {
        return Tarea(
            id = 1L,
            nombre = "Task",
            categoriaId = 1L,
            tipoPeriodicidad = Periodicidad.DIARIA,
            diasPeriodicidad = null,
            notas = "",
            fechaCreacion = LocalDateTime.of(2026, 5, 1, 10, 0),
            fechaUltimaModificacion = LocalDateTime.of(2026, 5, 5, 10, 0),
            fechaProximaEjecucion = dueAt,
            cantidadPostergaciones = 0,
            estadoAlerta = EstadoAlerta.NORMAL,
            mensajeAlerta = null,
        )
    }
}
