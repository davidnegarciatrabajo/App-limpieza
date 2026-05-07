package com.dngarcia.tareasdiarias.presentation.home

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class HomeUiState(
    val cards: List<HomeCardItem> = listOf(
        HomeCardItem.Tasks,
        HomeCardItem.NewTask,
        HomeCardItem.Categories,
        HomeCardItem.Today
    )
)

enum class HomeCardItem {
    Tasks,
    NewTask,
    Categories,
    Today
}

class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
}
