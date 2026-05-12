package com.dngarcia.tareasdiarias.presentation.nueva_tarea

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun NuevaTareaRoute(
    onBack: () -> Unit,
    onOpenCategories: () -> Unit,
    onTaskCreated: () -> Unit,
    viewModel: NuevaTareaViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState.collectAsState().value
    LaunchedEffect(Unit) {
        viewModel.finishEvent.collect { onTaskCreated() }
    }
    NuevaTareaScreen(
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
        onOpenCategories = onOpenCategories,
        onGuardar = viewModel::onGuardarClick,
        onDismissSaveError = viewModel::dismissSaveError,
        onRetrySave = viewModel::retrySave,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevaTareaScreen(
    uiState: NuevaTareaUiState,
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
    onOpenCategories: () -> Unit,
    onGuardar: () -> Unit,
    onDismissSaveError: () -> Unit,
    onRetrySave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

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

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = { AppTopBar(title = stringResource(id = R.string.new_task_title)) },
    ) { innerPadding ->
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
                supportingText = {
                    uiState.nombreError?.let { Text(it) }
                },
            )

            OutlinedTextField(
                value = uiState.subtitulo,
                onValueChange = onSubtituloChange,
                label = { Text(stringResource(id = R.string.task_field_subtitle_optional)) },
                modifier = Modifier.fillMaxWidth(),
            )

            CategorySection(
                uiState = uiState,
                onCategoriaSelected = onCategoriaSelected,
                onOpenCategories = onOpenCategories,
            )
            uiState.categoriaError?.let { Text(it) }

            PeriodicidadSection(
                uiState = uiState,
                onPeriodicidadSelected = onPeriodicidadSelected,
                onDiasPersonalizadosChange = onDiasPersonalizadosChange,
            )
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
                onClick = onGuardar,
                enabled = !uiState.isSaving,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(id = R.string.task_save))
            }
            TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(id = R.string.task_cancel))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategorySection(
    uiState: NuevaTareaUiState,
    onCategoriaSelected: (Long) -> Unit,
    onOpenCategories: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
        ) {
            OutlinedTextField(
                value = uiState.categorias.firstOrNull { it.id == uiState.categoriaId }?.nombre.orEmpty(),
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(id = R.string.task_field_category)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor(
                        type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                        enabled = true,
                    )
                    .fillMaxWidth(),
            )
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                uiState.categorias.forEach { categoria ->
                    DropdownMenuItem(
                        text = { Text(categoria.nombre) },
                        onClick = {
                            onCategoriaSelected(categoria.id)
                            expanded = false
                        },
                    )
                }
            }
        }
        TextButton(
            onClick = onOpenCategories,
            modifier = Modifier.align(Alignment.End),
        ) {
            Text(text = stringResource(id = R.string.task_category_create_new))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PeriodicidadSection(
    uiState: NuevaTareaUiState,
    onPeriodicidadSelected: (Periodicidad) -> Unit,
    onDiasPersonalizadosChange: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = periodicidadLabelResource(uiState.periodicidad)
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
    ) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(id = R.string.task_field_periodicity)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(
                    type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                    enabled = true,
                )
                .fillMaxWidth(),
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            Periodicidad.entries.forEach { periodicidad ->
                DropdownMenuItem(
                    text = { Text(periodicidadLabelResource(periodicidad)) },
                    onClick = {
                        onPeriodicidadSelected(periodicidad)
                        expanded = false
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
}

@Composable
fun periodicidadLabelResource(periodicidad: Periodicidad): String {
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
