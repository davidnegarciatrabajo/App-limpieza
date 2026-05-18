package com.dngarcia.tareasdiarias.presentation.editar_tarea

import android.content.Context
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dngarcia.tareasdiarias.R
import com.dngarcia.tareasdiarias.presentation.navigation.AppRoute
import com.dngarcia.tareasdiarias.domain.model.Categoria
import com.dngarcia.tareasdiarias.domain.model.ModoProximoCiclo
import com.dngarcia.tareasdiarias.domain.model.Periodicidad
import com.dngarcia.tareasdiarias.domain.model.Tarea
import com.dngarcia.tareasdiarias.domain.usecase.GetTaskByIdUseCase
import com.dngarcia.tareasdiarias.domain.usecase.DeleteTaskUseCase
import com.dngarcia.tareasdiarias.domain.usecase.ObserveCategoriasUseCase
import com.dngarcia.tareasdiarias.domain.usecase.UpdateTaskParams
import com.dngarcia.tareasdiarias.domain.usecase.UpdateTaskUseCase
import com.dngarcia.tareasdiarias.domain.usecase.ValidateUniqueTaskNameUseCase
import com.dngarcia.tareasdiarias.presentation.common.UserError
import com.dngarcia.tareasdiarias.presentation.common.toUserError
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
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

private const val TAG_EDITAR_TAREA = "EditarTareaViewModel"

data class EditarTareaUiState(
    val taskId: Long = -1L,
    val nombre: String = "",
    val subtitulo: String = "",
    val categoriaId: Long? = null,
    val periodicidad: Periodicidad = Periodicidad.DIARIA,
    val diasPersonalizados: String = "",
    val notas: String = "",
    val fechaInicio: LocalDate = LocalDate.now(),
    val horaRecordatorio: LocalTime? = null,
    val modoProximoCiclo: ModoProximoCiclo = ModoProximoCiclo.ANCLADO_FECHA_INICIO,
    val categorias: List<Categoria> = emptyList(),
    val nombreError: String? = null,
    val categoriaError: String? = null,
    val periodicidadError: String? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val showConfirmDialog: Boolean = false,
    val showDeleteConfirmDialog: Boolean = false,
    val loadError: UserError? = null,
    val saveError: UserError? = null,
    val deleteError: UserError? = null,
    val isTaskReady: Boolean = false,
)

@HiltViewModel
class EditarTareaViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle,
    observeCategoriasUseCase: ObserveCategoriasUseCase,
    private val getTaskByIdUseCase: GetTaskByIdUseCase,
    private val validateUniqueTaskNameUseCase: ValidateUniqueTaskNameUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
) : ViewModel() {
    private val taskId: Long = savedStateHandle[AppRoute.TASK_ID_ARG] ?: -1L
    private var originalTask: Tarea? = null

    private val _uiState = MutableStateFlow(EditarTareaUiState(taskId = taskId))
    val uiState: StateFlow<EditarTareaUiState> = _uiState.asStateFlow()

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
        loadTask()
    }

    private fun loadTask() {
        viewModelScope.launch {
            try {
                val task = getTaskByIdUseCase(taskId)
                if (task == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isTaskReady = false,
                            loadError = UserError(R.string.task_not_found),
                        )
                    }
                    return@launch
                }
                originalTask = task
                _uiState.update {
                    it.copy(
                        nombre = task.nombre,
                        subtitulo = task.subtitulo,
                        categoriaId = task.categoriaId,
                        periodicidad = task.tipoPeriodicidad,
                        diasPersonalizados = task.diasPeriodicidad?.toString().orEmpty(),
                        notas = task.notas,
                        fechaInicio = task.fechaInicio,
                        horaRecordatorio = task.horaRecordatorio,
                        modoProximoCiclo = task.modoProximoCiclo,
                        isLoading = false,
                        loadError = null,
                        isTaskReady = true,
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG_EDITAR_TAREA, "Error al cargar tarea", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isTaskReady = false,
                        loadError = e.toUserError(),
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
                modoProximoCiclo = if (periodicidad == Periodicidad.UNICA) {
                    ModoProximoCiclo.ANCLADO_FECHA_INICIO
                } else {
                    it.modoProximoCiclo
                },
            )
        }
    }

    fun onDiasPersonalizadosChange(value: String) {
        _uiState.update { it.copy(diasPersonalizados = value, periodicidadError = null, saveError = null) }
    }

    fun onNotasChange(value: String) {
        _uiState.update { it.copy(notas = value, saveError = null) }
    }

    fun onFechaInicioChange(value: LocalDate) {
        _uiState.update { it.copy(fechaInicio = value, saveError = null) }
    }

    fun onHoraRecordatorioChange(value: LocalTime) {
        _uiState.update { it.copy(horaRecordatorio = value, saveError = null) }
    }

    fun onClearHoraRecordatorio() {
        _uiState.update { it.copy(horaRecordatorio = null, saveError = null) }
    }

    fun onModoProximoCicloSelected(modo: ModoProximoCiclo) {
        _uiState.update { it.copy(modoProximoCiclo = modo, saveError = null) }
    }

    fun onConfirmModificationClick() {
        _uiState.update { it.copy(showConfirmDialog = true) }
    }

    fun onDismissConfirmDialog() {
        _uiState.update { it.copy(showConfirmDialog = false) }
    }

    fun onDeleteTaskClick() {
        _uiState.update { it.copy(showDeleteConfirmDialog = true, deleteError = null) }
    }

    fun onDismissDeleteDialog() {
        _uiState.update { it.copy(showDeleteConfirmDialog = false) }
    }

    fun onConfirmSave() {
        viewModelScope.launch {
            val current = _uiState.value
            val nombre = current.nombre.trim()
            if (nombre.isBlank()) {
                _uiState.update {
                    it.copy(
                        nombreError = context.getString(R.string.validation_name_required),
                        showConfirmDialog = false,
                    )
                }
                return@launch
            }
            val isUnique = validateUniqueTaskNameUseCase(
                nombre = nombre,
                excludeTaskId = current.taskId,
            )
            if (!isUnique) {
                _uiState.update {
                    it.copy(
                        nombreError = context.getString(R.string.validation_other_task_name_exists),
                        showConfirmDialog = false,
                    )
                }
                return@launch
            }

            val periodicidadDays = if (current.periodicidad == Periodicidad.PERSONALIZADA) {
                val days = current.diasPersonalizados.toIntOrNull()
                if (days == null || days <= 0) {
                    _uiState.update {
                        it.copy(
                            periodicidadError = context.getString(R.string.validation_custom_days_invalid),
                            showConfirmDialog = false,
                        )
                    }
                    return@launch
                }
                days
            } else {
                null
            }

            val selectedCategoryId = current.categoriaId
            if (selectedCategoryId == null) {
                _uiState.update {
                    it.copy(
                        categoriaError = context.getString(R.string.validation_category_required),
                        showConfirmDialog = false,
                    )
                }
                return@launch
            }

            _uiState.update { it.copy(isSaving = true, showConfirmDialog = false, saveError = null) }
            try {
                updateTaskUseCase(
                    params = UpdateTaskParams(
                        taskId = current.taskId,
                        nombre = nombre,
                        subtitulo = current.subtitulo,
                        categoriaId = selectedCategoryId,
                        notas = current.notas,
                        periodicidad = current.periodicidad,
                        diasPeriodicidad = periodicidadDays,
                        fechaInicio = current.fechaInicio,
                        horaRecordatorio = current.horaRecordatorio,
                        modoProximoCiclo = if (current.periodicidad == Periodicidad.UNICA) {
                            ModoProximoCiclo.ANCLADO_FECHA_INICIO
                        } else {
                            current.modoProximoCiclo
                        },
                    ),
                )
                _finishEvent.emit(Unit)
            } catch (e: Exception) {
                Log.e(TAG_EDITAR_TAREA, "Error al actualizar tarea", e)
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

    fun dismissDeleteError() {
        _uiState.update { it.copy(deleteError = null) }
    }

    fun retryLoadTask() {
        dismissLoadError()
        _uiState.update { it.copy(isLoading = true) }
        loadTask()
    }

    fun retrySave() {
        dismissSaveError()
        onConfirmSave()
    }

    fun onConfirmDeleteTask() {
        viewModelScope.launch {
            val current = _uiState.value
            _uiState.update { it.copy(isSaving = true, showDeleteConfirmDialog = false, deleteError = null) }
            try {
                deleteTaskUseCase(current.taskId)
                _finishEvent.emit(Unit)
            } catch (e: Exception) {
                Log.e(TAG_EDITAR_TAREA, "Error al eliminar tarea", e)
                _uiState.update { it.copy(deleteError = e.toUserError()) }
            } finally {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }
}
