package com.dngarcia.tareasdiarias.presentation.topten

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.dngarcia.tareasdiarias.R
import com.dngarcia.tareasdiarias.presentation.common.MainBottomDestination
import com.dngarcia.tareasdiarias.presentation.today.DayAgendaScreen

@Composable
fun TopTenRoute(
    onOpenToday: () -> Unit,
    onOpenTomorrow: () -> Unit,
    onOpenTopTen: () -> Unit,
    onOpenTasks: () -> Unit,
    onOpenMenu: () -> Unit,
    onAddTask: () -> Unit,
    onEditTask: (Long) -> Unit,
    viewModel: TopTenViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
) {
    val uiState = viewModel.uiState.collectAsState().value
    DayAgendaScreen(
        uiState = uiState,
        titleResId = R.string.tasks_top_pending,
        emptyStateResId = R.string.top10_empty_state,
        toolbarTrailingIconResId = R.drawable.ic_top10_tab_calendar,
        toolbarIconContentDescriptionResId = R.string.tasks_top_pending,
        selectedDestination = MainBottomDestination.TOP_TEN,
        onOpenToday = onOpenToday,
        onOpenTomorrow = onOpenTomorrow,
        onOpenTopTen = onOpenTopTen,
        onOpenTasks = onOpenTasks,
        onOpenMenu = onOpenMenu,
        onAddTask = onAddTask,
        onEditTask = onEditTask,
        onCompleteTask = viewModel::completeTask,
        onUndoTask = viewModel::undoTask,
        onPostponeTask = viewModel::postponeTask,
        onDismissUserError = viewModel::dismissUserError,
        onRetryLoadTasks = viewModel::retryLoadTasks,
        modifier = modifier,
    )
}
