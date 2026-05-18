package com.dngarcia.tareasdiarias.presentation.tomorrow

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.dngarcia.tareasdiarias.R
import com.dngarcia.tareasdiarias.presentation.common.MainBottomDestination
import com.dngarcia.tareasdiarias.presentation.today.DayAgendaScreen

@Composable
fun TomorrowRoute(
    onOpenToday: () -> Unit,
    onOpenTomorrow: () -> Unit,
    onOpenTopTen: () -> Unit,
    onOpenTasks: () -> Unit,
    onOpenMenu: () -> Unit,
    onAddTask: () -> Unit,
    onEditTask: (Long) -> Unit,
    viewModel: TomorrowViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
) {
    val uiState = viewModel.uiState.collectAsState().value
    DayAgendaScreen(
        uiState = uiState,
        titleResId = R.string.tomorrow_title,
        emptyStateResId = R.string.tomorrow_empty_state,
        toolbarTrailingIconResId = R.drawable.ic_tomorrow_tab_calendar,
        toolbarIconContentDescriptionResId = R.string.tomorrow_title,
        selectedDestination = MainBottomDestination.TOMORROW,
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
