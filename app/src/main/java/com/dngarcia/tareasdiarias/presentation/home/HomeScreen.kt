package com.dngarcia.tareasdiarias.presentation.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dngarcia.tareasdiarias.R
import com.dngarcia.tareasdiarias.presentation.common.AppTaskCard
import com.dngarcia.tareasdiarias.presentation.common.AppTopBar

@Composable
fun HomeRoute(
    onTasksClick: () -> Unit,
    onNewTaskClick: () -> Unit,
    onTodayClick: () -> Unit,
    onTomorrowClick: () -> Unit,
    onTopTenClick: () -> Unit,
    onCategoriesClick: () -> Unit,
    viewModel: HomeViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState = viewModel.uiState.collectAsState().value
    HomeScreen(
        uiState = uiState,
        onTasksClick = onTasksClick,
        onNewTaskClick = onNewTaskClick,
        onTodayClick = onTodayClick,
        onTomorrowClick = onTomorrowClick,
        onTopTenClick = onTopTenClick,
        onCategoriesClick = onCategoriesClick,
        modifier = modifier
    )
}

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onTasksClick: () -> Unit,
    onNewTaskClick: () -> Unit,
    onTodayClick: () -> Unit,
    onTomorrowClick: () -> Unit,
    onTopTenClick: () -> Unit,
    onCategoriesClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            AppTopBar(title = stringResource(id = R.string.home_title))
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            uiState.cards.forEach { card ->
                HomeAccessCard(
                    cardItem = card,
                    onTasksClick = onTasksClick,
                    onNewTaskClick = onNewTaskClick,
                    onTodayClick = onTodayClick,
                    onTomorrowClick = onTomorrowClick,
                    onTopTenClick = onTopTenClick,
                    onCategoriesClick = onCategoriesClick,
                )
            }
        }
    }
}

@Composable
private fun HomeAccessCard(
    cardItem: HomeCardItem,
    onTasksClick: () -> Unit,
    onNewTaskClick: () -> Unit,
    onTodayClick: () -> Unit,
    onTomorrowClick: () -> Unit,
    onTopTenClick: () -> Unit,
    onCategoriesClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val titleRes = when (cardItem) {
        HomeCardItem.Tasks -> R.string.home_card_tasks
        HomeCardItem.TopTen -> R.string.home_card_top_ten
        HomeCardItem.NewTask -> R.string.home_card_new_task
        HomeCardItem.Categories -> R.string.home_card_categories
        HomeCardItem.Today -> R.string.home_card_today
        HomeCardItem.Tomorrow -> R.string.home_card_tomorrow
    }
    val isEnabledCard = cardItem == HomeCardItem.Today ||
        cardItem == HomeCardItem.Tomorrow ||
        cardItem == HomeCardItem.TopTen ||
        cardItem == HomeCardItem.Tasks ||
        cardItem == HomeCardItem.NewTask ||
        cardItem == HomeCardItem.Categories

    AppTaskCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = isEnabledCard) {
                when (cardItem) {
                    HomeCardItem.Tasks -> onTasksClick()
                    HomeCardItem.TopTen -> onTopTenClick()
                    HomeCardItem.NewTask -> onNewTaskClick()
                    HomeCardItem.Today -> onTodayClick()
                    HomeCardItem.Tomorrow -> onTomorrowClick()
                    HomeCardItem.Categories -> onCategoriesClick()
                }
            },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = stringResource(id = titleRes),
                style = MaterialTheme.typography.titleMedium
            )
            if (!isEnabledCard) {
                Text(
                    text = stringResource(id = R.string.placeholder_coming_soon),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
