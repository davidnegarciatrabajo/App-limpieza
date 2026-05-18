package com.dngarcia.tareasdiarias.presentation.tareas

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.dngarcia.tareasdiarias.R
import com.dngarcia.tareasdiarias.domain.model.EstadoAlerta
import com.dngarcia.tareasdiarias.domain.model.Periodicidad
import com.dngarcia.tareasdiarias.domain.model.TaskAdvancedFilters
import com.dngarcia.tareasdiarias.domain.model.Tarea
import com.dngarcia.tareasdiarias.presentation.common.toTaskStatusItemUiModel
import com.dngarcia.tareasdiarias.ui.theme.AppLimpiezaTheme
import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Flujo principal de filtros avanzados: abrir dialogo, filtrar por estado, confirmar y ver listado.
 */
@RunWith(AndroidJUnit4::class)
class TareasAdvancedFiltersUiTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun advancedFilters_whenStatusOverdueApplied_showsOnlyOverdueTasks() {
        val now = LocalDateTime.of(2026, 5, 6, 12, 0)
        val overdue = Tarea(
            id = 1L,
            nombre = "UiTest overdue task",
            subtitulo = "",
            categoriaId = 1L,
            tipoPeriodicidad = Periodicidad.DIARIA,
            diasPeriodicidad = 1,
            notas = "",
            fechaInicio = now.minusDays(2).toLocalDate(),
            fechaCreacion = now,
            fechaUltimaModificacion = now,
            fechaProximaEjecucion = now.minusDays(2),
            fechaVisibleDesde = now.minusDays(2).toLocalDate(),
            horaRecordatorio = null,
            ultimaVezQueHiceLaTarea = null,
            cantidadPostergaciones = 0,
            estadoAlerta = EstadoAlerta.NORMAL,
            mensajeAlerta = null,
        )
        val okTask = Tarea(
            id = 2L,
            nombre = "UiTest ok task",
            subtitulo = "",
            categoriaId = 1L,
            tipoPeriodicidad = Periodicidad.DIARIA,
            diasPeriodicidad = 1,
            notas = "",
            fechaInicio = now.toLocalDate(),
            fechaCreacion = now,
            fechaUltimaModificacion = now,
            fechaProximaEjecucion = now.plusDays(5),
            fechaVisibleDesde = now.plusDays(5).toLocalDate(),
            horaRecordatorio = null,
            ultimaVezQueHiceLaTarea = null,
            cantidadPostergaciones = 0,
            estadoAlerta = EstadoAlerta.NORMAL,
            mensajeAlerta = null,
        )
        val allModels = listOf(
            overdue.toTaskStatusItemUiModel(now),
            okTask.toTaskStatusItemUiModel(now),
        )

        composeRule.setContent {
            AppLimpiezaTheme {
                var advanced by remember { mutableStateOf(TaskAdvancedFilters()) }
                val filtered = remember(advanced) {
                    if (advanced.status == null) {
                        allModels
                    } else {
                        allModels.filter { it.status == advanced.status }
                    }
                }
                TareasScreen(
                    uiState = TareasUiState(
                        filteredTasks = filtered,
                        advancedFilters = advanced,
                        isLoading = false,
                    ),
                    showNotificationPermissionCard = false,
                    canAskNotificationPermissionAgain = false,
                    showExactAlarmCard = false,
                    onRequestNotificationPermission = {},
                    onOpenNotificationSettings = {},
                    onOpenExactAlarmSettings = {},
                    onFilterSelected = {},
                    onSortOrderSelected = {},
                    onSearchQueryChange = {},
                    onToggleIncludeNotesInSearch = {},
                    onAdvancedFiltersChange = { advanced = it },
                    onClearAdvancedFilters = { advanced = TaskAdvancedFilters() },
                    onDeleteTask = {},
                    onDismissUserError = {},
                    onRetryLoadTasks = {},
                    onOpenToday = {},
                    onOpenTomorrow = {},
                    onOpenTopTen = {},
                    onOpenMenu = {},
                    onAddTaskClick = {},
                    onTaskClick = {},
                )
            }
        }

        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        val overdueLabel = ctx.getString(R.string.task_status_filter_overdue)

        fun assertTaskVisible(name: String) {
            composeRule.onNodeWithText(name)
                .performScrollTo()
                .assertIsDisplayed()
        }

        assertTaskVisible(overdue.nombre)
        assertTaskVisible(okTask.nombre)

        composeRule.onNodeWithTag(TareasScreenTestTags.ADVANCED_FILTERS_BUTTON).performClick()
        composeRule.onNodeWithText(overdueLabel).performClick()
        composeRule.onNodeWithTag(TareasScreenTestTags.ADVANCED_FILTERS_CONFIRM).performClick()

        composeRule.waitForIdle()

        assertTaskVisible(overdue.nombre)
        assertEquals(0, composeRule.onAllNodes(hasText(okTask.nombre)).fetchSemanticsNodes().size)
    }
}
