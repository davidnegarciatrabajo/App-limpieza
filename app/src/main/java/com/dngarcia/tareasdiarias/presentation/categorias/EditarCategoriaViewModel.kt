package com.dngarcia.tareasdiarias.presentation.categorias

import android.content.Context
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dngarcia.tareasdiarias.R
import com.dngarcia.tareasdiarias.domain.model.Categoria
import com.dngarcia.tareasdiarias.domain.usecase.GetCategoriaByIdUseCase
import com.dngarcia.tareasdiarias.domain.usecase.ObserveCategoriasUseCase
import com.dngarcia.tareasdiarias.domain.usecase.UpdateCategoriaUseCase
import com.dngarcia.tareasdiarias.presentation.common.UserError
import com.dngarcia.tareasdiarias.presentation.common.toUserError
import com.dngarcia.tareasdiarias.presentation.navigation.AppRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditarCategoriaUiState(
    val categoriaId: Long = -1L,
    val nombre: String = "",
    val categorias: List<Categoria> = emptyList(),
    val nombreError: String? = null,
    val isLoading: Boolean = true,
    val isCategoriaReady: Boolean = false,
    val loadError: UserError? = null,
    val isSaving: Boolean = false,
    val saveError: UserError? = null,
)

private const val TAG_EDITAR_CATEGORIA = "EditarCategoriaViewModel"

@HiltViewModel
class EditarCategoriaViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle,
    observeCategoriasUseCase: ObserveCategoriasUseCase,
    private val getCategoriaByIdUseCase: GetCategoriaByIdUseCase,
    private val updateCategoriaUseCase: UpdateCategoriaUseCase,
) : ViewModel() {

    private val categoriaId: Long = savedStateHandle[AppRoute.CATEGORY_ID_ARG] ?: -1L
    private var loadedCategoria: Categoria? = null

    private val _uiState = MutableStateFlow(EditarCategoriaUiState(categoriaId = categoriaId))
    val uiState: StateFlow<EditarCategoriaUiState> = _uiState.asStateFlow()

    private val _finishEvent = MutableSharedFlow<Unit>()
    val finishEvent: SharedFlow<Unit> = _finishEvent.asSharedFlow()

    init {
        viewModelScope.launch {
            observeCategoriasUseCase().collect { categorias ->
                _uiState.update { state ->
                    state.copy(
                        categorias = categorias,
                        nombreError = nombreDuplicadoMessage(
                            nombre = state.nombre,
                            categorias = categorias,
                            currentId = categoriaId,
                        ),
                    )
                }
            }
        }
        loadCategoria()
    }

    private fun loadCategoria() {
        viewModelScope.launch {
            try {
                val categoria = getCategoriaByIdUseCase(categoriaId)
                if (categoria == null) {
                    _uiState.update {
                        it.copy(isLoading = false, isCategoriaReady = false)
                    }
                    return@launch
                }
                loadedCategoria = categoria
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isCategoriaReady = true,
                        nombre = categoria.nombre,
                        loadError = null,
                        nombreError = nombreDuplicadoMessage(
                            nombre = categoria.nombre,
                            categorias = it.categorias,
                            currentId = categoriaId,
                        ),
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG_EDITAR_CATEGORIA, "Error al cargar categoria", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isCategoriaReady = false,
                        loadError = e.toUserError(),
                    )
                }
            }
        }
    }

    fun onNombreChange(value: String) {
        _uiState.update { state ->
            state.copy(
                nombre = value,
                saveError = null,
                nombreError = nombreDuplicadoMessage(
                    nombre = value,
                    categorias = state.categorias,
                    currentId = categoriaId,
                ),
            )
        }
    }

    fun onGuardarClick() {
        viewModelScope.launch {
            val base = loadedCategoria ?: return@launch
            val state = _uiState.value
            val trimmed = state.nombre.trim()
            if (trimmed.isBlank()) {
                _uiState.update {
                    it.copy(nombreError = context.getString(R.string.validation_name_required))
                }
                return@launch
            }
            val duplicado = nombreDuplicadoMessage(
                nombre = state.nombre,
                categorias = state.categorias,
                currentId = categoriaId,
            )
            if (duplicado != null) {
                _uiState.update { it.copy(nombreError = duplicado) }
                return@launch
            }
            _uiState.update { it.copy(isSaving = true, saveError = null) }
            try {
                updateCategoriaUseCase(
                    Categoria(
                        id = base.id,
                        nombre = trimmed,
                        color = base.color,
                    ),
                )
                _finishEvent.emit(Unit)
            } catch (e: Exception) {
                Log.e(TAG_EDITAR_CATEGORIA, "Error al guardar categoria", e)
                _uiState.update { it.copy(saveError = e.toUserError()) }
            } finally {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    fun dismissLoadError() {
        _uiState.update { it.copy(loadError = null) }
    }

    fun dismissSaveError() {
        _uiState.update { it.copy(saveError = null) }
    }

    fun retryLoadCategoria() {
        dismissLoadError()
        _uiState.update { it.copy(isLoading = true) }
        loadCategoria()
    }

    fun retrySave() {
        dismissSaveError()
        onGuardarClick()
    }

    private fun nombreDuplicadoMessage(
        nombre: String,
        categorias: List<Categoria>,
        currentId: Long,
    ): String? {
        val trimmed = nombre.trim()
        if (trimmed.isEmpty()) return null
        val clash = categorias.any {
            it.id != currentId && it.nombre.equals(trimmed, ignoreCase = true)
        }
        return if (clash) {
            context.getString(R.string.validation_category_name_exists)
        } else {
            null
        }
    }
}
