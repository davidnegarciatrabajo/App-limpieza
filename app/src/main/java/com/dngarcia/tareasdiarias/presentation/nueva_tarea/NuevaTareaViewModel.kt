package com.dngarcia.tareasdiarias.presentation.nueva_tarea

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dngarcia.tareasdiarias.R
import com.dngarcia.tareasdiarias.domain.model.Categoria
import com.dngarcia.tareasdiarias.domain.model.Periodicidad
import com.dngarcia.tareasdiarias.domain.usecase.CreateCategoriaUseCase
import com.dngarcia.tareasdiarias.domain.usecase.CreateTaskParams
import com.dngarcia.tareasdiarias.domain.usecase.CreateTaskUseCase
import com.dngarcia.tareasdiarias.domain.usecase.ObserveCategoriasUseCase
import com.dngarcia.tareasdiarias.domain.usecase.ValidateUniqueTaskNameUseCase
import com.dngarcia.tareasdiarias.presentation.common.UserError
import com.dngarcia.tareasdiarias.presentation.common.toUserError
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import java.time.LocalTime
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NuevaTareaUiState(
    val nombre: String = "",
    val subtitulo: String = "",
    val categoriaId: Long? = null,
    val periodicidad: Periodicidad = Periodicidad.DIARIA,
    val diasPersonalizados: String = "",
    val notas: String = "",
    val horaRecordatorio: LocalTime? = null,
    val categorias: List<Categoria> = emptyList(),
    val crearNuevaCategoria: Boolean = false,
    val nuevaCategoriaNombre: String = "",
    val nombreError: String? = null,
    val categoriaError: String? = null,
    val periodicidadError: String? = null,
    val isSaving: Boolean = false,
    val saveError: UserError? = null,
)

private const val TAG_NUEVA_TAREA = "NuevaTareaViewModel"

@HiltViewModel
class NuevaTareaViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    observeCategoriasUseCase: ObserveCategoriasUseCase,
    private val createCategoriaUseCase: CreateCategoriaUseCase,
    private val validateUniqueTaskNameUseCase: ValidateUniqueTaskNameUseCase,
    private val createTaskUseCase: CreateTaskUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(NuevaTareaUiState())
    val uiState: StateFlow<NuevaTareaUiState> = _uiState.asStateFlow()

    private val _finishEvent = MutableSharedFlow<Unit>()
    val finishEvent: SharedFlow<Unit> = _finishEvent.asSharedFlow()

    init {
        viewModelScope.launch {
            observeCategoriasUseCase().collect { categorias ->
                _uiState.update { state ->
                    state.copy(
                        categorias = categorias,
                        categoriaId = state.categoriaId ?: categorias.firstOrNull()?.id,
                    )
                }
            }
        }
    }

    fun onNombreChange(value: String) {
        _uiState.update { it.copy(nombre = value, nombreError = null, saveError = null) }
    }

    fun onSubtituloChange(value: String) {
        _uiState.update { it.copy(subtitulo = value, saveError = null) }
    }

    fun onCategoriaSelected(categoriaId: Long) {
        _uiState.update { it.copy(categoriaId = categoriaId, categoriaError = null, saveError = null) }
    }

    fun onPeriodicidadSelected(periodicidad: Periodicidad) {
        _uiState.update {
            it.copy(
                periodicidad = periodicidad,
                periodicidadError = null,
                saveError = null,
            )
        }
    }

    fun onDiasPersonalizadosChange(value: String) {
        _uiState.update { it.copy(diasPersonalizados = value, periodicidadError = null, saveError = null) }
    }

    fun onNotasChange(value: String) {
        _uiState.update { it.copy(notas = value, saveError = null) }
    }

    fun onHoraRecordatorioChange(value: LocalTime) {
        _uiState.update { it.copy(horaRecordatorio = value, saveError = null) }
    }

    fun onClearHoraRecordatorio() {
        _uiState.update { it.copy(horaRecordatorio = null, saveError = null) }
    }

    fun onCrearNuevaCategoriaChange(enabled: Boolean) {
        _uiState.update {
            it.copy(
                crearNuevaCategoria = enabled,
                categoriaError = null,
                saveError = null,
            )
        }
    }

    fun onNuevaCategoriaNombreChange(value: String) {
        _uiState.update { it.copy(nuevaCategoriaNombre = value, categoriaError = null, saveError = null) }
    }

    fun onGuardarClick() {
        viewModelScope.launch {
            val current = _uiState.value
            val nombre = current.nombre.trim()
            if (nombre.isBlank()) {
                _uiState.update { it.copy(nombreError = context.getString(R.string.validation_name_required)) }
                return@launch
            }
            val isUnique = validateUniqueTaskNameUseCase(nombre = nombre)
            if (!isUnique) {
                _uiState.update { it.copy(nombreError = context.getString(R.string.validation_task_name_exists)) }
                return@launch
            }

            val periodicidadDays = if (current.periodicidad == Periodicidad.PERSONALIZADA) {
                val days = current.diasPersonalizados.toIntOrNull()
                if (days == null || days <= 0) {
                    _uiState.update {
                        it.copy(periodicidadError = context.getString(R.string.validation_custom_days_invalid))
                    }
                    return@launch
                }
                days
            } else {
                null
            }

            val categoriaId = resolveCategoriaId(current) ?: return@launch

            _uiState.update { it.copy(isSaving = true, saveError = null) }
            try {
                createTaskUseCase(
                    params = CreateTaskParams(
                        nombre = nombre,
                        subtitulo = current.subtitulo,
                        categoriaId = categoriaId,
                        periodicidad = current.periodicidad,
                        diasPeriodicidad = periodicidadDays,
                        notas = current.notas,
                        horaRecordatorio = current.horaRecordatorio,
                    ),
                )
                _finishEvent.emit(Unit)
            } catch (e: Exception) {
                Log.e(TAG_NUEVA_TAREA, "Error al crear tarea", e)
                _uiState.update { it.copy(saveError = e.toUserError()) }
            } finally {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    fun dismissSaveError() {
        _uiState.update { it.copy(saveError = null) }
    }

    fun retrySave() {
        dismissSaveError()
        onGuardarClick()
    }

    private suspend fun resolveCategoriaId(state: NuevaTareaUiState): Long? {
        if (!state.crearNuevaCategoria) {
            val selected = state.categoriaId
            if (selected == null) {
                _uiState.update { it.copy(categoriaError = context.getString(R.string.validation_category_required)) }
            }
            return selected
        }

        val nuevaCategoria = state.nuevaCategoriaNombre.trim()
        if (nuevaCategoria.isBlank()) {
            _uiState.update { it.copy(categoriaError = context.getString(R.string.validation_new_category_required)) }
            return null
        }

        val existingCategoria = state.categorias.firstOrNull {
            it.nombre.equals(nuevaCategoria, ignoreCase = true)
        }
        if (existingCategoria != null) {
            return existingCategoria.id
        }

        return createCategoriaUseCase(nombre = nuevaCategoria)
    }
}
