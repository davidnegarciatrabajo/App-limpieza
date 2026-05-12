package com.dngarcia.tareasdiarias.domain.usecase

import com.dngarcia.tareasdiarias.domain.model.Categoria
import com.dngarcia.tareasdiarias.domain.model.EstadoAlerta
import com.dngarcia.tareasdiarias.domain.model.Periodicidad
import com.dngarcia.tareasdiarias.domain.model.Tarea
import com.dngarcia.tareasdiarias.domain.repository.CategoriaRepository
import com.dngarcia.tareasdiarias.domain.repository.TareaRepository
import com.dngarcia.tareasdiarias.domain.sampledata.DebugSampleDataGate
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * Inserta categorias y al menos 20 tareas variadas para pruebas.
 *
 * Solo corre en build debug. Una vez por instalacion: persiste un flag en SharedPreferences
 * (ver [DebugSampleDataGate]). Si en debug se usa migracion destructiva de Room y la BD queda
 * vacia pero el flag sigue en true, hace falta borrar datos de la app para volver a sembrar.
 */
class SeedDebugSampleDataIfNeededUseCase @Inject constructor(
    private val tareaRepository: TareaRepository,
    private val categoriaRepository: CategoriaRepository,
    private val debugSampleDataGate: DebugSampleDataGate,
) {
    suspend operator fun invoke() {
        if (!debugSampleDataGate.isDebugBuild()) return
        if (debugSampleDataGate.wasSampleDataAlreadySeeded()) return

        val now = LocalDateTime.now()
        val limpiezaId = categoriaRepository.create(
            Categoria(id = 0L, nombre = "Demo Limpieza", color = 0xFFE3F2FD.toInt()),
        )
        val oficinaId = categoriaRepository.create(
            Categoria(id = 0L, nombre = "Demo Oficina", color = 0xFFFFF3E0.toInt()),
        )
        val saludId = categoriaRepository.create(
            Categoria(id = 0L, nombre = "Demo Salud", color = 0xFFE8F5E9.toInt()),
        )
        val hogarId = categoriaRepository.create(
            Categoria(id = 0L, nombre = "Demo Hogar", color = 0xFFF3E5F5.toInt()),
        )
        val personalId = categoriaRepository.create(
            Categoria(id = 0L, nombre = "Demo Personal", color = null),
        )

        val seeds = demoTasks(
            now = now,
            limpiezaId = limpiezaId,
            oficinaId = oficinaId,
            saludId = saludId,
            hogarId = hogarId,
            personalId = personalId,
        )
        for (t in seeds) {
            tareaRepository.create(t)
        }

        debugSampleDataGate.markSampleDataSeeded()
    }

    private fun demoTasks(
        now: LocalDateTime,
        limpiezaId: Long,
        oficinaId: Long,
        saludId: Long,
        hogarId: Long,
        personalId: Long,
    ): List<Tarea> {
        fun t(
            nombre: String,
            categoriaId: Long,
            periodicidad: Periodicidad,
            dias: Int?,
            notas: String,
            proxima: LocalDateTime?,
            postergaciones: Int,
            alerta: EstadoAlerta,
            creado: LocalDateTime = now.minusDays(14),
        ) = Tarea(
            id = 0L,
            nombre = nombre,
            categoriaId = categoriaId,
            tipoPeriodicidad = periodicidad,
            diasPeriodicidad = dias,
            notas = notas,
            fechaInicio = (proxima ?: creado).toLocalDate(),
            fechaCreacion = creado,
            fechaUltimaModificacion = creado.plusDays(1),
            fechaProximaEjecucion = proxima,
            fechaVisibleDesde = proxima?.toLocalDate(),
            horaRecordatorio = proxima?.toLocalTime(),
            ultimaVezQueHiceLaTarea = null,
            cantidadPostergaciones = postergaciones,
            estadoAlerta = alerta,
            mensajeAlerta = null,
        )

        return listOf(
            t(
                "Demo: Trapear cocina",
                limpiezaId,
                Periodicidad.DIARIA,
                null,
                "Pendiente desde ayer",
                now.minusDays(2),
                4,
                EstadoAlerta.VENCIDA,
            ),
            t(
                "Demo: Limpiar ventanas",
                limpiezaId,
                Periodicidad.SEMANAL,
                null,
                "Proxima en pocas horas",
                now.plusHours(3),
                1,
                EstadoAlerta.PROXIMA,
            ),
            t(
                "Demo: Descalcificar cafetera",
                hogarId,
                Periodicidad.MENSUAL,
                null,
                "En una semana",
                now.plusDays(7),
                0,
                EstadoAlerta.NORMAL,
            ),
            t(
                "Demo: Revision calefaccion",
                hogarId,
                Periodicidad.SEMESTRAL,
                null,
                "Cada seis meses",
                now.plusDays(45),
                0,
                EstadoAlerta.NORMAL,
            ),
            t(
                "Demo: Caminar 30 min",
                saludId,
                Periodicidad.PERSONALIZADA,
                2,
                "Cada 2 dias",
                now.plusDays(1),
                2,
                EstadoAlerta.NORMAL,
            ),
            t(
                "Demo: Donacion ropa",
                personalId,
                Periodicidad.UNICA,
                null,
                "Una sola vez",
                now.plusDays(3),
                0,
                EstadoAlerta.NORMAL,
            ),
            t(
                "Demo: Revisar bandeja",
                oficinaId,
                Periodicidad.DIARIA,
                null,
                "Vencida hace varios dias",
                now.minusDays(6),
                9,
                EstadoAlerta.VENCIDA,
            ),
            t(
                "Demo: Backup portatil",
                oficinaId,
                Periodicidad.SEMANAL,
                null,
                "Menos de 24 h",
                now.plusHours(20),
                0,
                EstadoAlerta.PROXIMA,
            ),
            t(
                "Demo: Fregar bano",
                limpiezaId,
                Periodicidad.PERSONALIZADA,
                5,
                "Cada 5 dias",
                now.minusHours(6),
                3,
                EstadoAlerta.VENCIDA,
            ),
            t(
                "Demo: Cortar pasto",
                hogarId,
                Periodicidad.SEMANAL,
                null,
                "La proxima semana",
                now.plusDays(10),
                1,
                EstadoAlerta.NORMAL,
            ),
            t(
                "Demo: Dentista",
                saludId,
                Periodicidad.MENSUAL,
                null,
                "Mensual de control",
                now.minusDays(1),
                0,
                EstadoAlerta.VENCIDA,
            ),
            t(
                "Demo: Pagar servicios",
                hogarId,
                Periodicidad.MENSUAL,
                null,
                "En 12 horas",
                now.plusHours(12),
                5,
                EstadoAlerta.PROXIMA,
            ),
            t(
                "Demo: Curso online",
                personalId,
                Periodicidad.PERSONALIZADA,
                10,
                "Cada 10 dias",
                now.plusDays(4),
                0,
                EstadoAlerta.NORMAL,
            ),
            t(
                "Demo: Organizar escritorio",
                oficinaId,
                Periodicidad.DIARIA,
                null,
                "Mas de 24 h OK",
                now.plusHours(30),
                0,
                EstadoAlerta.NORMAL,
            ),
            t(
                "Demo: Cambiar sabanas",
                hogarId,
                Periodicidad.SEMANAL,
                null,
                "Atrasada",
                now.minusDays(4),
                6,
                EstadoAlerta.VENCIDA,
            ),
            t(
                "Demo: Medir presion",
                saludId,
                Periodicidad.PERSONALIZADA,
                7,
                "Semanal aproximado",
                now.plusHours(8),
                1,
                EstadoAlerta.PROXIMA,
            ),
            t(
                "Demo: Evento unico",
                personalId,
                Periodicidad.UNICA,
                null,
                "Ya paso la fecha",
                now.minusDays(2),
                2,
                EstadoAlerta.VENCIDA,
            ),
            t(
                "Demo: Barrer entrada",
                limpiezaId,
                Periodicidad.DIARIA,
                null,
                "Hoy tarde",
                now.plusHours(5),
                0,
                EstadoAlerta.PROXIMA,
            ),
            t(
                "Demo: Auditoria anual",
                oficinaId,
                Periodicidad.SEMESTRAL,
                null,
                "Largo plazo",
                now.plusDays(120),
                0,
                EstadoAlerta.NORMAL,
            ),
            t(
                "Demo: Podar plantas",
                hogarId,
                Periodicidad.PERSONALIZADA,
                14,
                "Quincenal",
                now.plusDays(12),
                2,
                EstadoAlerta.NORMAL,
            ),
            t(
                "Demo: Yoga 15 min",
                saludId,
                Periodicidad.DIARIA,
                null,
                "Manana temprano",
                now.plusDays(1).withHour(7).withMinute(30).withSecond(0).withNano(0),
                0,
                EstadoAlerta.NORMAL,
            ),
        )
    }
}
