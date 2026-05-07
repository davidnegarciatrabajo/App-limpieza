package com.dngarcia.tareasdiarias.presentation.categorias

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dngarcia.tareasdiarias.R
import com.dngarcia.tareasdiarias.domain.model.Categoria
import com.dngarcia.tareasdiarias.domain.usecase.CreateCategoriaUseCase
import com.dngarcia.tareasdiarias.domain.usecase.ObserveCategoriasUseCase
import com.dngarcia.tareasdiarias.presentation.common.UserError
import com.dngarcia.tareasdiarias.presentation.common.toUserError
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CategoriesUiState(
    val categorias: List<Categoria> = emptyList(),
    val showCreateDialog: Boolean = false,
    val newCategoryName: String = "",
    val nameError: String? = null,
    val isCreating: Boolean = false,
    val saveError: UserError? = null,
)

private const val TAG_CATEGORIES = "CategoriesViewModel"

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    observeCategoriasUseCase: ObserveCategoriasUseCase,
    private val createCategoriaUseCase: CreateCategoriaUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoriesUiState())
    val uiState: StateFlow<CategoriesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeCategoriasUseCase().collect { categorias ->
                _uiState.update { it.copy(categorias = categorias) }
            }
        }
    }

    fun onAddCategoryClick() {
        _uiState.update {
            it.copy(
                showCreateDialog = true,
                newCategoryName = "",
                nameError = null,
                saveError = null,
            )
        }
    }

    fun onDismissCreateDialog() {
        _uiState.update {
            it.copy(
                showCreateDialog = false,
                newCategoryName = "",
                nameError = null,
            )
        }
    }

    fun onNewCategoryNameChange(value: String) {
        _uiState.update { it.copy(newCategoryName = value, nameError = null, saveError = null) }
    }

    fun onConfirmCreateCategory() {
        viewModelScope.launch {
            val current = _uiState.value
            val nombre = current.newCategoryName.trim()
            if (nombre.isBlank()) {
                _uiState.update {
                    it.copy(nameError = context.getString(R.string.validation_new_category_required))
                }
                return@launch
            }
            val duplicate = current.categorias.any { it.nombre.equals(nombre, ignoreCase = true) }
            if (duplicate) {
                _uiState.update {
                    it.copy(nameError = context.getString(R.string.validation_category_name_exists))
                }
                return@launch
            }
            _uiState.update { it.copy(isCreating = true, saveError = null) }
            try {
                createCategoriaUseCase(nombre = nombre)
                _uiState.update {
                    it.copy(
                        isCreating = false,
                        showCreateDialog = false,
                        newCategoryName = "",
                        nameError = null,
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG_CATEGORIES, "Error al crear categoria", e)
                _uiState.update {
                    it.copy(isCreating = false, saveError = e.toUserError())
                }
            }
        }
    }

    fun dismissSaveError() {
        _uiState.update { it.copy(saveError = null) }
    }

    fun retryCreateCategory() {
        dismissSaveError()
        onConfirmCreateCategory()
    }
}
