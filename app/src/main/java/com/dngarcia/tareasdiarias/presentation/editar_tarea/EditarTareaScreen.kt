package com.dngarcia.tareasdiarias.presentation.editar_tarea

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.dngarcia.tareasdiarias.R
import com.dngarcia.tareasdiarias.domain.model.Periodicidad
import com.dngarcia.tareasdiarias.presentation.common.AppTopBar
import com.dngarcia.tareasdiarias.presentation.common.OptionalReminderTimeField
import com.dngarcia.tareasdiarias.presentation.common.RequiredTaskDateField
import com.dngarcia.tareasdiarias.presentation.nueva_tarea.periodicidadLabelResource
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun EditarTareaRoute(
    onBack: () -> Unit,
    onTaskUpdated: () -> Unit,
    viewModel: EditarTareaViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState.collectAsState().value
    LaunchedEffect(Unit) {
        viewModel.finishEvent.collect { onTaskUpdated() }
    }
    EditarTareaScreen(
        uiState = uiState,
        onBack = onBack,
        onNombreChange = viewModel::onNombreChange,
        onSubtituloChange = viewModel::onSubtituloChange,
        onCategoriaSelected = viewModel::onCategoriaSelected,
        onPeriodicidadSelected = viewModel::onPeriodicidadSelected,
        onDiasPersonalizadosChange = viewModel::onDiasPersonalizadosChange,
        onNotasChange = viewModel::onNotasChange,
        onFechaInicioChange = viewModel::onFechaInicioChange,
        onHoraRecordatorioChange = viewModel::onHoraRecordatorioChange,
        onClearHoraRecordatorio = viewModel::onClearHoraRecordatorio,
        onConfirmModificationClick = viewModel::onConfirmModificationClick,
        onDismissConfirmDialog = viewModel::onDismissConfirmDialog,
        onConfirmSave = viewModel::onConfirmSave,
        onDeleteTaskClick = viewModel::onDeleteTaskClick,
        onDismissDeleteDialog = viewModel::onDismissDeleteDialog,
        onConfirmDeleteTask = viewModel::onConfirmDeleteTask,
        onDismissLoadError = viewModel::dismissLoadError,
        onDismissSaveError = viewModel::dismissSaveError,
        onDismissDeleteError = viewModel::dismissDeleteError,
        onRetryLoadTask = viewModel::retryLoadTask,
        onRetrySave = viewModel::retrySave,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarTareaScreen(
    uiState: EditarTareaUiState,
    onBack: () -> Unit,
    onNombreChange: (String) -> Unit,
    onSubtituloChange: (String) -> Unit,
    onCategoriaSelected: (Long) -> Unit,
    onPeriodicidadSelected: (Periodicidad) -> Unit,
    onDiasPersonalizadosChange: (String) -> Unit,
    onNotasChange: (String) -> Unit,
    onFechaInicioChange: (LocalDate) -> Unit,
    onHoraRecordatorioChange: (LocalTime) -> Unit,
    onClearHoraRecordatorio: () -> Unit,
    onConfirmModificationClick: () -> Unit,
    onDismissConfirmDialog: () -> Unit,
    onConfirmSave: () -> Unit,
    onDeleteTaskClick: () -> Unit,
    onDismissDeleteDialog: () -> Unit,
    onConfirmDeleteTask: () -> Unit,
    onDismissLoadError: () -> Unit,
    onDismissSaveError: () -> Unit,
    onDismissDeleteError: () -> Unit,
    onRetryLoadTask: () -> Unit,
    onRetrySave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(uiState.loadError) {
        val err = uiState.loadError ?: return@LaunchedEffect
        val message = context.getString(err.messageResId, *err.formatArgs)
        val retryLabel = context.getString(R.string.action_retry)
        when (
            snackbarHostState.showSnackbar(
                message = message,
                actionLabel = retryLabel,
            )
        ) {
            SnackbarResult.ActionPerformed -> onRetryLoadTask()
            SnackbarResult.Dismissed -> onDismissLoadError()
        }
    }

    LaunchedEffect(uiState.saveError) {
        val err = uiState.saveError ?: return@LaunchedEffect
        val message = context.getString(err.messageResId, *err.formatArgs)
        val retryLabel = context.getString(R.string.action_retry)
        when (
            snackbarHostState.showSnackbar(
                message = message,
                actionLabel = retryLabel,
            )
        ) {
            SnackbarResult.ActionPerformed -> onRetrySave()
            SnackbarResult.Dismissed -> onDismissSaveError()
        }
    }

    LaunchedEffect(uiState.deleteError) {
        val err = uiState.deleteError ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(
            message = context.getString(err.messageResId, *err.formatArgs),
        )
        onDismissDeleteError()
    }

    if (uiState.showConfirmDialog) {
        AlertDialog(
            onDismissRequest = onDismissConfirmDialog,
            title = { Text(stringResource(id = R.string.task_confirm_modification)) },
            text = { Text(stringResource(id = R.string.task_confirm_dialog_message)) },
            confirmButton = {
                TextButton(onClick = onConfirmSave) {
                    Text(stringResource(id = R.string.task_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissConfirmDialog) {
                    Text(stringResource(id = R.string.task_cancel))
                }
            },
        )
    }

    if (uiState.showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = onDismissDeleteDialog,
            title = { Text(stringResource(id = R.string.task_delete_confirm_title)) },
            text = { Text(stringResource(id = R.string.task_delete_confirm_message)) },
            confirmButton = {
                TextButton(onClick = onConfirmDeleteTask) {
                    Text(stringResource(id = R.string.task_delete_confirm_action))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissDeleteDialog) {
                    Text(stringResource(id = R.string.task_cancel))
                }
            },
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = { AppTopBar(title = stringResource(id = R.string.edit_task_title)) },
    ) { innerPadding ->
        if (uiState.isLoading) {
            Column(modifier = Modifier.padding(innerPadding).padding(16.dp)) {
                Text(stringResource(id = R.string.task_loading))
            }
            return@Scaffold
        }
        if (!uiState.isTaskReady) {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(16.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = stringResource(id = R.string.task_not_found),
                    style = MaterialTheme.typography.bodyLarge,
                )
                TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(id = R.string.task_cancel))
                }
            }
            return@Scaffold
        }
        var categoryExpanded by remember { mutableStateOf(false) }
        var periodicidadExpanded by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = uiState.nombre,
                onValueChange = onNombreChange,
                label = { Text(stringResource(id = R.string.task_field_name)) },
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.nombreError != null,
                supportingText = { uiState.nombreError?.let { Text(it) } },
            )

            OutlinedTextField(
                value = uiState.subtitulo,
                onValueChange = onSubtituloChange,
                label = { Text(stringResource(id = R.string.task_field_subtitle_optional)) },
                modifier = Modifier.fillMaxWidth(),
            )

            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = !categoryExpanded },
            ) {
                OutlinedTextField(
                    value = uiState.categorias.firstOrNull { it.id == uiState.categoriaId }?.nombre.orEmpty(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(id = R.string.task_field_category)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    modifier = Modifier
                        .menuAnchor(
                            type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                            enabled = true,
                        )
                        .fillMaxWidth(),
                )
                DropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                    uiState.categorias.forEach { categoria ->
                        DropdownMenuItem(
                            text = { Text(categoria.nombre) },
                            onClick = {
                                onCategoriaSelected(categoria.id)
                                categoryExpanded = false
                            },
                        )
                    }
                }
            }
            uiState.categoriaError?.let { Text(it) }

            ExposedDropdownMenuBox(
                expanded = periodicidadExpanded,
                onExpandedChange = { periodicidadExpanded = !periodicidadExpanded },
            ) {
                OutlinedTextField(
                    value = periodicidadLabelResource(uiState.periodicidad),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(id = R.string.task_field_periodicity)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = periodicidadExpanded) },
                    modifier = Modifier
                        .menuAnchor(
                            type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                            enabled = true,
                        )
                        .fillMaxWidth(),
                )
                DropdownMenu(
                    expanded = periodicidadExpanded,
                    onDismissRequest = { periodicidadExpanded = false },
                ) {
                    Periodicidad.entries.forEach { periodicidad ->
                        DropdownMenuItem(
                            text = { Text(periodicidadLabelResource(periodicidad)) },
                            onClick = {
                                onPeriodicidadSelected(periodicidad)
                                periodicidadExpanded = false
                            },
                        )
                    }
                }
            }
            if (uiState.periodicidad == Periodicidad.PERSONALIZADA) {
                OutlinedTextField(
                    value = uiState.diasPersonalizados,
                    onValueChange = onDiasPersonalizadosChange,
                    label = { Text(stringResource(id = R.string.task_custom_days)) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            uiState.periodicidadError?.let { Text(it) }

            OutlinedTextField(
                value = uiState.notas,
                onValueChange = onNotasChange,
                label = { Text(stringResource(id = R.string.task_field_notes)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
            )

            RequiredTaskDateField(
                selectedDate = uiState.fechaInicio,
                onDateSelected = onFechaInicioChange,
                modifier = Modifier.fillMaxWidth(),
            )

            OptionalReminderTimeField(
                selectedTime = uiState.horaRecordatorio,
                onTimeSelected = onHoraRecordatorioChange,
                onClearTime = onClearHoraRecordatorio,
                modifier = Modifier.fillMaxWidth(),
            )

            Button(
                onClick = onConfirmModificationClick,
                enabled = !uiState.isSaving,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(id = R.string.task_confirm_modification))
            }
            TextButton(
                onClick = onDeleteTaskClick,
                enabled = !uiState.isSaving,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(id = R.string.task_delete_action))
            }
            TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(id = R.string.task_back_without_save))
            }
        }
    }
}
