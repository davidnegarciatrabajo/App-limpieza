package com.dngarcia.tareasdiarias.presentation.categorias

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.dngarcia.tareasdiarias.R
import com.dngarcia.tareasdiarias.domain.model.Categoria
import com.dngarcia.tareasdiarias.presentation.common.AppTaskCard
import com.dngarcia.tareasdiarias.presentation.common.AppTopBar

@Composable
fun CategoriesRoute(
    onBack: () -> Unit,
    onCategoryClick: (Long) -> Unit,
    viewModel: CategoriesViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState.collectAsState().value
    CategoriesScreen(
        uiState = uiState,
        onBack = onBack,
        onCategoryClick = onCategoryClick,
        onAddCategoryClick = viewModel::onAddCategoryClick,
        onDismissCreateDialog = viewModel::onDismissCreateDialog,
        onNewCategoryNameChange = viewModel::onNewCategoryNameChange,
        onConfirmCreateCategory = viewModel::onConfirmCreateCategory,
        onDismissSaveError = viewModel::dismissSaveError,
        onRetryCreateCategory = viewModel::retryCreateCategory,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    uiState: CategoriesUiState,
    onBack: () -> Unit,
    onCategoryClick: (Long) -> Unit,
    onAddCategoryClick: () -> Unit,
    onDismissCreateDialog: () -> Unit,
    onNewCategoryNameChange: (String) -> Unit,
    onConfirmCreateCategory: () -> Unit,
    onDismissSaveError: () -> Unit,
    onRetryCreateCategory: () -> Unit,
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
            SnackbarResult.ActionPerformed -> onRetryCreateCategory()
            SnackbarResult.Dismissed -> onDismissSaveError()
        }
    }

    if (uiState.showCreateDialog) {
        AlertDialog(
            onDismissRequest = onDismissCreateDialog,
            title = { Text(text = stringResource(id = R.string.categories_create_dialog_title)) },
            text = {
                OutlinedTextField(
                    value = uiState.newCategoryName,
                    onValueChange = onNewCategoryNameChange,
                    label = { Text(stringResource(id = R.string.task_category_new_name)) },
                    modifier = Modifier.fillMaxWidth(),
                    isError = uiState.nameError != null,
                    supportingText = {
                        uiState.nameError?.let { Text(it) }
                    },
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = onConfirmCreateCategory,
                    enabled = !uiState.isCreating,
                ) {
                    Text(stringResource(id = R.string.categories_create_confirm))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismissCreateDialog,
                    enabled = !uiState.isCreating,
                ) {
                    Text(stringResource(id = R.string.task_cancel))
                }
            },
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            AppTopBar(title = stringResource(id = R.string.categories_title))
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddCategoryClick) {
                Text(text = stringResource(id = R.string.categories_fab_add))
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = stringResource(id = R.string.categories_back))
                }
            }
            if (uiState.categorias.isEmpty()) {
                item {
                    Text(
                        text = stringResource(id = R.string.categories_empty),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            } else {
                items(uiState.categorias, key = { it.id }) { categoria ->
                    CategoryListItem(
                        categoria = categoria,
                        onClick = { onCategoryClick(categoria.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryListItem(
    categoria: Categoria,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppTaskCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Text(
            text = categoria.nombre,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CategoriesScreenPreview() {
    CategoriesScreen(
        uiState = CategoriesUiState(
            categorias = listOf(
                Categoria(id = 1L, nombre = "Hogar", color = null),
                Categoria(id = 2L, nombre = "Trabajo", color = null),
            ),
        ),
        onBack = {},
        onCategoryClick = {},
        onAddCategoryClick = {},
        onDismissCreateDialog = {},
        onNewCategoryNameChange = {},
        onConfirmCreateCategory = {},
        onDismissSaveError = {},
        onRetryCreateCategory = {},
    )
}
