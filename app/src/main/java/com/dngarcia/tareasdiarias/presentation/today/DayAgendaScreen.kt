package com.dngarcia.tareasdiarias.presentation.today

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.dngarcia.tareasdiarias.R
import com.dngarcia.tareasdiarias.presentation.common.MainBottomBar
import com.dngarcia.tareasdiarias.presentation.common.MainBottomDestination
import com.dngarcia.tareasdiarias.presentation.common.StatusText
import com.dngarcia.tareasdiarias.presentation.common.TaskPostponeDialog
import com.dngarcia.tareasdiarias.presentation.common.formatDueDateLabel
import com.dngarcia.tareasdiarias.presentation.common.formatLastCompletionLabel
import com.dngarcia.tareasdiarias.presentation.common.toUiColor
import com.dngarcia.tareasdiarias.domain.model.Periodicidad
import com.dngarcia.tareasdiarias.domain.model.TaskStatus
import com.dngarcia.tareasdiarias.domain.usecase.TaskTimelinePolicy
import com.dngarcia.tareasdiarias.ui.theme.StatusOk
import com.dngarcia.tareasdiarias.ui.theme.StatusOverdue
import java.time.LocalDate

@Composable
fun DayAgendaScreen(
    uiState: DayAgendaUiState,
    titleResId: Int,
    emptyStateResId: Int,
    toolbarTrailingIconResId: Int,
    toolbarIconContentDescriptionResId: Int,
    selectedDestination: MainBottomDestination,
    onOpenToday: () -> Unit,
    onOpenTomorrow: () -> Unit,
    onOpenTopTen: () -> Unit,
    onOpenTasks: () -> Unit,
    onOpenMenu: () -> Unit,
    onAddTask: () -> Unit,
    onEditTask: (Long) -> Unit,
    onCompleteTask: (Long) -> Unit,
    onUndoTask: (Long) -> Unit,
    onPostponeTask: (Long, LocalDate) -> Unit,
    onDismissUserError: () -> Unit,
    onRetryLoadTasks: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var postponeTask by remember { mutableStateOf<DayAgendaTaskUiModel?>(null) }

    LaunchedEffect(uiState.userError) {
        val err = uiState.userError ?: return@LaunchedEffect
        val message = context.getString(err.messageResId, *err.formatArgs)
        val retryLabel = context.getString(R.string.action_retry)
        when (
            snackbarHostState.showSnackbar(
                message = message,
                actionLabel = retryLabel,
            )
        ) {
            SnackbarResult.ActionPerformed -> onRetryLoadTasks()
            SnackbarResult.Dismissed -> onDismissUserError()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        contentWindowInsets = WindowInsets(0),
        topBar = {
            DayAgendaTopBar(
                titleResId = titleResId,
                trailingIconResId = toolbarTrailingIconResId,
                trailingIconContentDescriptionResId = toolbarIconContentDescriptionResId,
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTask,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(28.dp),
            ) {
                Text(text = stringResource(id = R.string.tasks_add))
            }
        },
        bottomBar = {
            MainBottomBar(
                selectedDestination = selectedDestination,
                onOpenToday = onOpenToday,
                onOpenTomorrow = onOpenTomorrow,
                onOpenTopTen = onOpenTopTen,
                onOpenTasks = onOpenTasks,
                onOpenMenu = onOpenMenu,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            if (uiState.isLoading) {
                Text(
                    text = stringResource(id = R.string.task_loading),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp),
                )
            } else if (uiState.tasks.isEmpty()) {
                Text(
                    text = stringResource(id = emptyStateResId),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                ) {
                    items(
                        items = uiState.tasks,
                        key = { it.item.task.id },
                    ) { task ->
                        DayAgendaTaskRow(
                            task = task,
                            onToggleTaskCompletion = {
                                if (task.completedOnAgendaDay) {
                                    onUndoTask(task.item.task.id)
                                } else {
                                    onCompleteTask(task.item.task.id)
                                }
                            },
                            onEditTask = { onEditTask(task.item.task.id) },
                            onPostponeTask = { postponeTask = task },
                        )
                    }
                }
            }
        }
    }

    postponeTask?.let { task ->
        TaskPostponeDialog(
            taskName = task.item.task.nombre,
            onDismiss = { postponeTask = null },
            onPostpone = { selectedDate ->
                onPostponeTask(task.item.task.id, selectedDate)
                postponeTask = null
            },
        )
    }
}

@Composable
private fun DayAgendaTaskRow(
    task: DayAgendaTaskUiModel,
    onToggleTaskCompletion: () -> Unit,
    onEditTask: () -> Unit,
    onPostponeTask: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val taskItem = task.item
    val completedDay = task.completedOnAgendaDay
    Row(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f),
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        DayAgendaTaskCheckbox(
            checked = completedDay,
            onClick = onToggleTaskCompletion,
        )
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(taskItem.status.toUiColor()),
                )
                Text(
                    text = taskItem.task.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (completedDay) TextDecoration.LineThrough else TextDecoration.None,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            if (taskItem.task.subtitulo.isNotBlank()) {
                Text(
                    text = taskItem.task.subtitulo,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textDecoration = if (completedDay) TextDecoration.LineThrough else TextDecoration.None,
                )
            }
            Text(
                text = formatLastCompletionLabel(taskItem.task.ultimaVezQueHiceLaTarea),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = formatDueDateLabel(TaskTimelinePolicy.expectedCycleDate(taskItem.task)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(
                    id = R.string.tasks_postponements_count,
                    taskItem.task.cantidadPostergaciones,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (taskItem.task.notas.isNotBlank() && !completedDay) {
                Text(
                    text = taskItem.task.notas,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textDecoration = TextDecoration.None,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                StatusChip(
                    text = task.categoryName.ifBlank { stringResource(id = R.string.task_field_category) },
                    background = MaterialTheme.colorScheme.surfaceContainerHigh,
                    content = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                StatusChip(
                    text = periodicityLabel(taskItem.task.tipoPeriodicidad),
                    background = MaterialTheme.colorScheme.surfaceContainerLowest,
                    content = MaterialTheme.colorScheme.onSurfaceVariant,
                    border = MaterialTheme.colorScheme.outlineVariant,
                )
                StatusChip(
                    text = StatusText(taskItem.status, taskItem.daysDelta),
                    background = taskItem.status.chipBackground(),
                    content = taskItem.status.toUiColor(),
                )
            }
            if (!completedDay) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextButton(onClick = onEditTask) { Text(text = stringResource(id = R.string.today_action_edit)) }
                    TextButton(onClick = onPostponeTask) { Text(text = stringResource(id = R.string.today_action_postpone)) }
                }
            }
        }
    }
}

@Composable
private fun DayAgendaTopBar(
    titleResId: Int,
    trailingIconResId: Int,
    trailingIconContentDescriptionResId: Int,
) {
    Row(
        modifier = Modifier
            .statusBarsPadding()
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(id = titleResId),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(start = 4.dp),
        )
        Icon(
            painter = painterResource(id = trailingIconResId),
            contentDescription = stringResource(id = trailingIconContentDescriptionResId),
            tint = Color.Unspecified,
            modifier = Modifier.size(24.dp),
        )
    }
}

@Composable
private fun periodicityLabel(periodicidad: Periodicidad): String {
    val resId = when (periodicidad) {
        Periodicidad.DIARIA -> R.string.task_periodicity_daily
        Periodicidad.SEMANAL -> R.string.task_periodicity_weekly
        Periodicidad.MENSUAL -> R.string.task_periodicity_monthly
        Periodicidad.SEMESTRAL -> R.string.task_periodicity_semiannual
        Periodicidad.PERSONALIZADA -> R.string.task_periodicity_custom
        Periodicidad.UNICA -> R.string.task_periodicity_unique
    }
    return stringResource(id = resId)
}

@Composable
private fun DayAgendaTaskCheckbox(
    checked: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clickable(onClick = onClick)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(2.dp))
            .background(if (checked) MaterialTheme.colorScheme.primary else Color.Transparent),
        contentAlignment = Alignment.Center,
    ) {
        if (checked) {
            Text(
                text = "✓",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        }
    }
}

@Composable
private fun StatusChip(
    text: String,
    background: Color,
    content: Color,
    border: Color? = null,
) {
    Surface(
        color = background,
        shape = RoundedCornerShape(8.dp),
        modifier = if (border != null) Modifier.border(1.dp, border, RoundedCornerShape(8.dp)) else Modifier,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = content,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
        )
    }
}

private fun TaskStatus.chipBackground(): Color = when (this) {
    TaskStatus.VENCIDA -> StatusOverdue.copy(alpha = 0.12f)
    TaskStatus.OK -> StatusOk.copy(alpha = 0.18f)
}
