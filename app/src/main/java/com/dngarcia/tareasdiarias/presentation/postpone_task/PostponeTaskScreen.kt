package com.dngarcia.tareasdiarias.presentation.postpone_task

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.dngarcia.tareasdiarias.R
import com.dngarcia.tareasdiarias.presentation.common.TaskPostponeDialog

@Composable
fun PostponeTaskRoute(
    onBack: () -> Unit,
    onTaskPostponed: () -> Unit,
    viewModel: PostponeTaskViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState.collectAsState().value

    LaunchedEffect(Unit) {
        viewModel.finishEvent.collect { onTaskPostponed() }
    }

    when {
        uiState.isLoading -> Text(text = stringResource(id = R.string.task_loading))
        uiState.taskName.isBlank() -> {
            LaunchedEffect(uiState.userError) {
                onBack()
            }
        }
        else -> {
            TaskPostponeDialog(
                taskName = uiState.taskName,
                onDismiss = onBack,
                onPostpone = viewModel::postponeTask,
            )
        }
    }
}
