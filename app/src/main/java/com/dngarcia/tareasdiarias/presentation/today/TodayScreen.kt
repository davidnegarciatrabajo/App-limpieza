package com.dngarcia.tareasdiarias.presentation.today

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.dngarcia.tareasdiarias.R
import com.dngarcia.tareasdiarias.presentation.common.AppTaskCard
import com.dngarcia.tareasdiarias.presentation.common.AppTopBar
import com.dngarcia.tareasdiarias.presentation.common.MetaPill
import com.dngarcia.tareasdiarias.presentation.common.StatusDot
import com.dngarcia.tareasdiarias.presentation.common.StatusText
import com.dngarcia.tareasdiarias.presentation.common.TaskStatusItemUiModel
import com.dngarcia.tareasdiarias.presentation.common.toUiColor
import java.time.format.DateTimeFormatter

@Composable
fun TodayRoute(
    onBackHome: () -> Unit,
    viewModel: TodayViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState = viewModel.uiState.collectAsState().value
    TodayScreen(
        uiState = uiState,
        onBackHome = onBackHome,
        onDismissUserError = viewModel::dismissUserError,
        onRetryLoadTasks = viewModel::retryLoadTasks,
        modifier = modifier,
    )
}

@Composable
fun TodayScreen(
    uiState: TodayUiState,
    onBackHome: () -> Unit,
    onDismissUserError: () -> Unit,
    onRetryLoadTasks: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
            AppTopBar(title = stringResource(id = R.string.today_title))
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (uiState.isLoading) {
                Text(
                    text = stringResource(id = R.string.task_loading),
                    style = MaterialTheme.typography.bodyLarge,
                )
            } else if (uiState.tasks.isEmpty()) {
                Text(
                    text = stringResource(id = R.string.today_empty_state),
                    style = MaterialTheme.typography.bodyLarge,
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(
                        items = uiState.tasks,
                        key = { it.task.id },
                    ) { task ->
                        TodayTaskItem(task = task)
                    }
                }
            }

            OutlinedButton(
                onClick = onBackHome,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = stringResource(id = R.string.today_back_home))
            }
        }
    }
}

@Composable
private fun TodayTaskItem(
    task: TaskStatusItemUiModel,
    modifier: Modifier = Modifier
) {
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
    AppTaskCard(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top,
        ) {
            StatusDot(status = task.status, modifier = Modifier.padding(top = 6.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(text = task.task.nombre, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = StatusText(
                        status = task.status,
                        daysDelta = task.daysDelta,
                        hoursUntilDue = task.hoursUntilDue,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = task.status.toUiColor(),
                )
                MetaPill(text = task.task.tipoPeriodicidad.name)
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
