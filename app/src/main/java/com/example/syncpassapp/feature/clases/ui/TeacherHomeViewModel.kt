package com.example.syncpassapp.feature.clases.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.syncpassapp.core.database.local.entities.ClaseEntity
import com.example.syncpassapp.core.database.repository.UniversidadRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TeacherHomeUiState(
    val profesorId: Long = 0L,
    val clases: List<ClaseEntity> = emptyList(),
    val filtroEstado: String = "TODOS" // "TODOS", "EN_CURSO", "FINALIZADO"
)

@HiltViewModel
class TeacherHomeViewModel @Inject constructor(
    private val repository: UniversidadRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val profesorId: Long = checkNotNull(
        savedStateHandle.get<Long>("profesorId")
            ?: savedStateHandle.get<String>("profesorId")?.toLongOrNull()
    ) { "profesorId es requerido" }

    private val _filtroEstado = MutableStateFlow("TODOS")

    val uiState: StateFlow<TeacherHomeUiState> = combine(
        repository.obtenerClasesPorProfesor(profesorId),
        _filtroEstado
    ) { clases, filtro ->
        val clasesFiltradas = when (filtro) {
            "EN_CURSO" -> clases.filter { it.estado == "EN_CURSO" }
            "FINALIZADO" -> clases.filter { it.estado == "FINALIZADO" }
            else -> clases
        }
        TeacherHomeUiState(
            profesorId = profesorId,
            clases = clasesFiltradas,
            filtroEstado = filtro
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TeacherHomeUiState(profesorId = profesorId)
    )

    fun cambiarFiltro(filtro: String) {
        _filtroEstado.update { filtro }
    }

    fun crearClase(nombre: String, descripcion: String, cuestionarioJson: String, estado: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val nuevaClase = ClaseEntity(
                nombre = nombre,
                descripcion = descripcion,
                profesorId = profesorId,
                cuestionarioEstructuraJson = cuestionarioJson,
                estado = estado
            )
            repository.crearClase(nuevaClase)
        }
    }
}
