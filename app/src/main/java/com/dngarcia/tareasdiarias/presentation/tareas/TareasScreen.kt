package com.dngarcia.tareasdiarias.presentation.tareas

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.app.ActivityCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.dngarcia.tareasdiarias.R
import com.dngarcia.tareasdiarias.domain.model.Periodicidad
import com.dngarcia.tareasdiarias.domain.model.Tarea
import com.dngarcia.tareasdiarias.domain.model.TaskAdvancedFilters
import com.dngarcia.tareasdiarias.domain.model.TaskDateFilterPreset
import com.dngarcia.tareasdiarias.domain.model.TaskStatus
import com.dngarcia.tareasdiarias.domain.model.TaskPeriodicityFilter
import com.dngarcia.tareasdiarias.domain.model.TaskSortOrder
import com.dngarcia.tareasdiarias.presentation.common.TaskStatusItemUiModel
import com.dngarcia.tareasdiarias.presentation.common.toTaskStatusItemUiModel
import com.dngarcia.tareasdiarias.presentation.common.AppFilterChip
import com.dngarcia.tareasdiarias.presentation.common.AppTaskCard
import com.dngarcia.tareasdiarias.presentation.common.AppTopBar
import com.dngarcia.tareasdiarias.presentation.common.MetaPill
import com.dngarcia.tareasdiarias.presentation.common.StatusDot
import com.dngarcia.tareasdiarias.presentation.common.StatusText
import com.dngarcia.tareasdiarias.presentation.common.toUiColor
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun TareasRoute(
    onAddTaskClick: () -> Unit,
    onTaskClick: (Long) -> Unit,
    viewModel: TareasViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var hasRequestedNotificationPermission by rememberSaveable { mutableStateOf(false) }
    var notificationsGranted by remember { mutableStateOf(context.hasPostNotificationsPermission()) }
    val canAskPermissionAgain = context.canAskNotificationPermissionAgain(hasRequestedNotificationPermission)
    val exactAlarmAvailable = context.canScheduleExactReminderAlarms()
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        hasRequestedNotificationPermission = true
        notificationsGranted = granted
    }

    val uiState = viewModel.uiState.collectAsState().value
    TareasScreen(
        uiState = uiState,
        showNotificationPermissionCard = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !notificationsGranted,
        canAskNotificationPermissionAgain = canAskPermissionAgain,
        showExactAlarmCard = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !exactAlarmAvailable,
        onRequestNotificationPermission = {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        },
        onOpenNotificationSettings = {
            context.openAppNotificationSettings()
        },
        onOpenExactAlarmSettings = {
            context.openExactAlarmSettings()
        },
        onFilterSelected = viewModel::onFilterSelected,
        onSortOrderSelected = viewModel::onSortOrderSelected,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onToggleIncludeNotesInSearch = viewModel::onToggleIncludeNotesInSearch,
        onAdvancedFiltersChange = viewModel::onAdvancedFiltersChange,
        onClearAdvancedFilters = viewModel::clearAdvancedFilters,
        onDismissUserError = viewModel::dismissUserError,
        onRetryLoadTasks = viewModel::retryLoadTasks,
        onAddTaskClick = onAddTaskClick,
        onTaskClick = onTaskClick,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TareasScreen(
    uiState: TareasUiState,
    showNotificationPermissionCard: Boolean,
    canAskNotificationPermissionAgain: Boolean,
    showExactAlarmCard: Boolean,
    onRequestNotificationPermission: () -> Unit,
    onOpenNotificationSettings: () -> Unit,
    onOpenExactAlarmSettings: () -> Unit,
    onFilterSelected: (TaskPeriodicityFilter) -> Unit,
    onSortOrderSelected: (TaskSortOrder) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onToggleIncludeNotesInSearch: () -> Unit,
    onAdvancedFiltersChange: (TaskAdvancedFilters) -> Unit,
    onClearAdvancedFilters: () -> Unit,
    onDismissUserError: () -> Unit,
    onRetryLoadTasks: () -> Unit,
    onAddTaskClick: () -> Unit,
    onTaskClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showAdvancedFiltersDialog by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

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
        topBar = {
            AppTopBar(title = stringResource(id = R.string.tasks_title))
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTaskClick) {
                Text(text = stringResource(id = R.string.tasks_add))
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (showNotificationPermissionCard) {
                item {
                    NotificationPermissionCard(
                        canAskAgain = canAskNotificationPermissionAgain,
                        onRequestPermission = onRequestNotificationPermission,
                        onOpenSettings = onOpenNotificationSettings,
                    )
                }
            }
            if (showExactAlarmCard) {
                item {
                    ExactAlarmPermissionCard(
                        onOpenSettings = onOpenExactAlarmSettings,
                    )
                }
            }
            item {
                Text(
                    text = stringResource(id = R.string.tasks_top_pending),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            item {
                if (uiState.topPendingTasks.isEmpty()) {
                    Text(text = stringResource(id = R.string.tasks_empty_state))
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        uiState.topPendingTasks.forEach { task ->
                            TaskListRow(
                                task = task,
                                onClick = { onTaskClick(task.task.id) },
                            )
                        }
                    }
                }
            }
            item {
                Text(
                    text = stringResource(id = R.string.tasks_search_title),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            item {
                SearchSection(
                    searchQuery = uiState.searchQuery,
                    includeNotesInSearch = uiState.includeNotesInSearch,
                    hasActiveAdvancedFilters = uiState.hasActiveAdvancedFilters,
                    onSearchQueryChange = onSearchQueryChange,
                    onToggleIncludeNotesInSearch = onToggleIncludeNotesInSearch,
                    onOpenAdvancedFilters = { showAdvancedFiltersDialog = true },
                    onClearAdvancedFilters = onClearAdvancedFilters,
                )
            }
            item {
                Text(
                    text = stringResource(id = R.string.tasks_filter_title),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            item {
                PeriodicityFilterRow(
                    selectedFilter = uiState.selectedFilter,
                    onFilterSelected = onFilterSelected,
                )
            }
            item {
                Text(
                    text = stringResource(id = R.string.tasks_sort_title),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            item {
                SortOrderRow(
                    selectedSortOrder = uiState.selectedSortOrder,
                    onSortOrderSelected = onSortOrderSelected,
                )
            }
            item {
                Text(
                    text = stringResource(id = R.string.tasks_list_title),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            if (uiState.isLoading) {
                item {
                    Text(text = stringResource(id = R.string.task_loading))
                }
            }
            if (uiState.filteredTasks.isEmpty()) {
                item {
                    val emptyMessage = if (uiState.hasActiveSearch || uiState.hasActiveAdvancedFilters) {
                        stringResource(id = R.string.tasks_empty_filters_state)
                    } else {
                        stringResource(id = R.string.tasks_empty_state)
                    }
                    Text(text = emptyMessage)
                }
            } else {
                items(
                    items = uiState.filteredTasks,
                    key = { it.task.id },
                ) { task ->
                    TaskListRow(
                        task = task,
                        onClick = { onTaskClick(task.task.id) },
                    )
                }
            }
        }
    }

    if (showAdvancedFiltersDialog) {
        AdvancedFiltersDialog(
            currentFilters = uiState.advancedFilters,
            categories = uiState.categorias,
            onDismiss = { showAdvancedFiltersDialog = false },
            onApply = { filters ->
                onAdvancedFiltersChange(filters)
                showAdvancedFiltersDialog = false
            },
            onClear = {
                onClearAdvancedFilters()
                showAdvancedFiltersDialog = false
            },
        )
    }
}

@Composable
private fun NotificationPermissionCard(
    canAskAgain: Boolean,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(id = R.string.notification_permission_title),
                style = MaterialTheme.typography.titleSmall,
            )
            Text(text = stringResource(id = R.string.notification_permission_description))
            if (canAskAgain) {
                Button(onClick = onRequestPermission) {
                    Text(text = stringResource(id = R.string.notification_permission_action_retry))
                }
            } else {
                Button(onClick = onOpenSettings) {
                    Text(text = stringResource(id = R.string.notification_permission_action_settings))
                }
            }
        }
    }
}

@Composable
private fun ExactAlarmPermissionCard(
    onOpenSettings: () -> Unit,
) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(id = R.string.exact_alarm_permission_title),
                style = MaterialTheme.typography.titleSmall,
            )
            Text(text = stringResource(id = R.string.exact_alarm_permission_description))
            Button(onClick = onOpenSettings) {
                Text(text = stringResource(id = R.string.exact_alarm_permission_action))
            }
        }
    }
}

@Composable
private fun SearchSection(
    searchQuery: String,
    includeNotesInSearch: Boolean,
    hasActiveAdvancedFilters: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onToggleIncludeNotesInSearch: () -> Unit,
    onOpenAdvancedFilters: () -> Unit,
    onClearAdvancedFilters: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text(text = stringResource(id = R.string.tasks_search_hint)) },
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = includeNotesInSearch,
                onCheckedChange = { onToggleIncludeNotesInSearch() },
            )
            Text(text = stringResource(id = R.string.tasks_search_include_notes))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = onOpenAdvancedFilters,
                modifier = Modifier.testTag(TareasScreenTestTags.ADVANCED_FILTERS_BUTTON),
            ) {
                Text(text = stringResource(id = R.string.tasks_advanced_filters_action))
            }
            if (hasActiveAdvancedFilters) {
                TextButton(onClick = onClearAdvancedFilters) {
                    Text(text = stringResource(id = R.string.tasks_clear_filters_action))
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AdvancedFiltersDialog(
    currentFilters: TaskAdvancedFilters,
    categories: List<com.dngarcia.tareasdiarias.domain.model.Categoria>,
    onDismiss: () -> Unit,
    onApply: (TaskAdvancedFilters) -> Unit,
    onClear: () -> Unit,
) {
    var status by remember(currentFilters) { mutableStateOf(currentFilters.status) }
    var datePreset by remember(currentFilters) { mutableStateOf(currentFilters.datePreset) }
    var categoryId by remember(currentFilters) { mutableStateOf(currentFilters.categoryId) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(id = R.string.tasks_advanced_filters_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = stringResource(id = R.string.tasks_filter_status))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = status == null,
                        onClick = { status = null },
                        label = { Text(text = stringResource(id = R.string.tasks_filter_all)) },
                    )
                    TaskStatus.entries.forEach { item ->
                        FilterChip(
                            selected = status == item,
                            onClick = { status = item },
                            label = { Text(text = item.toLabel()) },
                        )
                    }
                }
                Text(text = stringResource(id = R.string.tasks_filter_date))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TaskDateFilterPreset.entries.forEach { preset ->
                        FilterChip(
                            selected = datePreset == preset,
                            onClick = { datePreset = preset },
                            label = { Text(text = preset.toLabel()) },
                        )
                    }
                }
                Text(text = stringResource(id = R.string.tasks_filter_category))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = categoryId == null,
                        onClick = { categoryId = null },
                        label = { Text(text = stringResource(id = R.string.tasks_filter_all)) },
                    )
                    categories.forEach { category ->
                        FilterChip(
                            selected = categoryId == category.id,
                            onClick = { categoryId = category.id },
                            label = { Text(text = category.nombre) },
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onApply(
                        TaskAdvancedFilters(
                            status = status,
                            datePreset = datePreset,
                            categoryId = categoryId,
                        ),
                    )
                },
                modifier = Modifier.testTag(TareasScreenTestTags.ADVANCED_FILTERS_CONFIRM),
            ) {
                Text(text = stringResource(id = R.string.task_confirm))
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onClear) {
                    Text(text = stringResource(id = R.string.tasks_clear_filters_action))
                }
                TextButton(onClick = onDismiss) {
                    Text(text = stringResource(id = R.string.task_cancel))
                }
            }
        },
    )
}

@Composable
private fun PeriodicityFilterRow(
    selectedFilter: TaskPeriodicityFilter,
    onFilterSelected: (TaskPeriodicityFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AppFilterChip(
                label = stringResource(id = R.string.tasks_filter_all),
                selected = selectedFilter == TaskPeriodicityFilter.ALL,
                onClick = { onFilterSelected(TaskPeriodicityFilter.ALL) },
            )
            AppFilterChip(
                label = stringResource(id = R.string.tasks_filter_daily),
                selected = selectedFilter == TaskPeriodicityFilter.DAILY,
                onClick = { onFilterSelected(TaskPeriodicityFilter.DAILY) },
            )
            AppFilterChip(
                label = stringResource(id = R.string.tasks_filter_weekly),
                selected = selectedFilter == TaskPeriodicityFilter.WEEKLY,
                onClick = { onFilterSelected(TaskPeriodicityFilter.WEEKLY) },
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AppFilterChip(
                label = stringResource(id = R.string.tasks_filter_monthly),
                selected = selectedFilter == TaskPeriodicityFilter.MONTHLY,
                onClick = { onFilterSelected(TaskPeriodicityFilter.MONTHLY) },
            )
            AppFilterChip(
                label = stringResource(id = R.string.tasks_filter_semiannual),
                selected = selectedFilter == TaskPeriodicityFilter.SEMIANNUAL,
                onClick = { onFilterSelected(TaskPeriodicityFilter.SEMIANNUAL) },
            )
            AppFilterChip(
                label = stringResource(id = R.string.tasks_filter_unique),
                selected = selectedFilter == TaskPeriodicityFilter.UNIQUE,
                onClick = { onFilterSelected(TaskPeriodicityFilter.UNIQUE) },
            )
        }
    }
}

@Composable
private fun SortOrderRow(
    selectedSortOrder: TaskSortOrder,
    onSortOrderSelected: (TaskSortOrder) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AssistChip(
            onClick = { onSortOrderSelected(TaskSortOrder.HIGHEST_DELAY) },
            label = { Text(text = stringResource(id = R.string.tasks_sort_highest_delay)) },
            leadingIcon = {
                if (selectedSortOrder == TaskSortOrder.HIGHEST_DELAY) {
                    Text(text = "✓")
                }
            },
        )
        AssistChip(
            onClick = { onSortOrderSelected(TaskSortOrder.OLDEST_FIRST) },
            label = { Text(text = stringResource(id = R.string.tasks_sort_oldest)) },
            leadingIcon = {
                if (selectedSortOrder == TaskSortOrder.OLDEST_FIRST) {
                    Text(text = "✓")
                }
            },
        )
    }
}

@Composable
private fun TaskListRow(
    task: TaskStatusItemUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
    AppTaskCard(
        modifier = modifier
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top,
        ) {
            StatusDot(status = task.status, modifier = Modifier.padding(top = 6.dp).width(10.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = task.task.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.testTag(TareasScreenTestTags.taskRowTestTag(task.task.id)),
                )
                MetaPill(
                    text = stringResource(
                        id = R.string.tasks_postponements_count,
                        task.task.cantidadPostergaciones,
                    ),
                )
                Text(
                    text = StatusText(
                        status = task.status,
                        daysDelta = task.daysDelta,
                        hoursUntilDue = task.hoursUntilDue,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = task.status.toUiColor(),
                )
                Text(
                    text = stringResource(
                        id = R.string.task_last_modified,
                        task.lastModifiedAt.format(dateFormatter),
                    ),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TareasScreenPreview() {
    TareasScreen(
        uiState = TareasUiState(
            topPendingTasks = listOf(mockTask(id = 1)),
            filteredTasks = listOf(mockTask(id = 1), mockTask(id = 2)),
        ),
        showNotificationPermissionCard = true,
        canAskNotificationPermissionAgain = true,
        showExactAlarmCard = true,
        onRequestNotificationPermission = {},
        onOpenNotificationSettings = {},
        onOpenExactAlarmSettings = {},
        onFilterSelected = {},
        onSortOrderSelected = {},
        onSearchQueryChange = {},
        onToggleIncludeNotesInSearch = {},
        onAdvancedFiltersChange = {},
        onClearAdvancedFilters = {},
        onDismissUserError = {},
        onRetryLoadTasks = {},
        onAddTaskClick = {},
        onTaskClick = {},
    )
}

/** Tags de prueba para Compose UI (listado de tareas). */
object TareasScreenTestTags {
    const val ADVANCED_FILTERS_BUTTON = "tasks_advanced_filters_button"
    const val ADVANCED_FILTERS_CONFIRM = "tasks_advanced_filters_confirm"

    fun taskRowTestTag(taskId: Long): String = "task_row_$taskId"
}

private fun Context.hasPostNotificationsPermission(): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
    return ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.POST_NOTIFICATIONS,
    ) == PackageManager.PERMISSION_GRANTED
}

private fun Context.canAskNotificationPermissionAgain(hasRequestedPermission: Boolean): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return false
    if (!hasRequestedPermission) return true
    val activity = this.findActivity() ?: return true
    return ActivityCompat.shouldShowRequestPermissionRationale(
        activity,
        Manifest.permission.POST_NOTIFICATIONS,
    )
}

private fun Context.canScheduleExactReminderAlarms(): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
    val alarmManager = getSystemService(AlarmManager::class.java)
    return alarmManager.canScheduleExactAlarms()
}

private fun Context.openAppNotificationSettings() {
    startActivity(
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        },
    )
}

private fun Context.openExactAlarmSettings() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
    startActivity(
        Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
            data = android.net.Uri.parse("package:$packageName")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        },
    )
}

private fun Context.findActivity(): Activity? {
    var current = this
    while (current is android.content.ContextWrapper) {
        if (current is Activity) return current
        val base = current.baseContext ?: return null
        if (base === current) return null
        current = base
    }
    return null
}

@Composable
private fun TaskStatus.toLabel(): String = when (this) {
    TaskStatus.OK -> stringResource(id = R.string.task_status_filter_ok)
    TaskStatus.PROXIMA -> stringResource(id = R.string.task_status_filter_upcoming)
    TaskStatus.VENCIDA -> stringResource(id = R.string.task_status_filter_overdue)
}

@Composable
private fun TaskDateFilterPreset.toLabel(): String = when (this) {
    TaskDateFilterPreset.ALL -> stringResource(id = R.string.tasks_filter_date_all)
    TaskDateFilterPreset.TODAY -> stringResource(id = R.string.tasks_filter_date_today)
    TaskDateFilterPreset.NEXT_7_DAYS -> stringResource(id = R.string.tasks_filter_date_next_7_days)
    TaskDateFilterPreset.NEXT_30_DAYS -> stringResource(id = R.string.tasks_filter_date_next_30_days)
    TaskDateFilterPreset.OVERDUE -> stringResource(id = R.string.tasks_filter_date_overdue)
}

private fun mockTask(id: Long): TaskStatusItemUiModel {
    val task = Tarea(
        id = id,
        nombre = "Tarea $id",
        categoriaId = 1L,
        tipoPeriodicidad = Periodicidad.DIARIA,
        diasPeriodicidad = 1,
        notas = "",
        fechaCreacion = LocalDateTime.now(),
        fechaUltimaModificacion = LocalDateTime.now(),
        fechaProximaEjecucion = LocalDateTime.now().plusHours(8),
        cantidadPostergaciones = 2,
        estadoAlerta = com.dngarcia.tareasdiarias.domain.model.EstadoAlerta.NORMAL,
        mensajeAlerta = null,
    )
    return task.toTaskStatusItemUiModel()
}
