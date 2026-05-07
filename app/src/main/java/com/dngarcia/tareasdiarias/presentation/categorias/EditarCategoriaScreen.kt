package com.dngarcia.tareasdiarias.presentation.categorias

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.dngarcia.tareasdiarias.R

@Composable
fun EditarCategoriaRoute(
    onBack: () -> Unit,
    onCategoryUpdated: () -> Unit,
    viewModel: EditarCategoriaViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState.collectAsState().value
    LaunchedEffect(Unit) {
        viewModel.finishEvent.collect { onCategoryUpdated() }
    }
    EditarCategoriaScreen(
        uiState = uiState,
        onBack = onBack,
        onNombreChange = viewModel::onNombreChange,
        onGuardar = viewModel::onGuardarClick,
        onDismissLoadError = viewModel::dismissLoadError,
        onDismissSaveError = viewModel::dismissSaveError,
        onRetryLoadCategoria = viewModel::retryLoadCategoria,
        onRetrySave = viewModel::retrySave,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarCategoriaScreen(
    uiState: EditarCategoriaUiState,
    onBack: () -> Unit,
    onNombreChange: (String) -> Unit,
    onGuardar: () -> Unit,
    onDismissLoadError: () -> Unit,
    onDismissSaveError: () -> Unit,
    onRetryLoadCategoria: () -> Unit,
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
            SnackbarResult.ActionPerformed -> onRetryLoadCategoria()
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

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.categories_edit_title)) },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text(stringResource(id = R.string.categories_back))
                    }
                },
            )
        },
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Column(modifier = Modifier.padding(innerPadding).padding(16.dp)) {
                    Text(stringResource(id = R.string.categories_loading))
                }
            }
            !uiState.isCategoriaReady -> {
                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                        .padding(16.dp)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        text = stringResource(id = R.string.categories_not_found),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(id = R.string.task_cancel))
                    }
                }
            }
            else -> {
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
                        singleLine = true,
                    )
                    Button(
                        onClick = onGuardar,
                        enabled = !uiState.isSaving && uiState.nombreError == null,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(id = R.string.categories_save))
                    }
                    TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(id = R.string.task_cancel))
                    }
                }
            }
        }
    }
}
