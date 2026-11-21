package com.example.syncpassapp.feature.clases.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.syncpassapp.core.database.local.entities.ClaseEntity
import com.example.syncpassapp.core.database.repository.UniversidadRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val syncRepository: com.example.syncpassapp.core.database.repository.SyncRepository,
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

    fun marcarEstudiantePresente(claseId: Long, estudianteId: Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.registrarAsistenciaYCuestionario(
                usuarioId = estudianteId,
                claseId = claseId,
                esPresente = true,
                respuestasJson = null
            )
            // Si la clase está vinculada a un evento remoto, también registrar en sync
            val clase = repository.obtenerClasePorId(claseId)
            val remoteEventoId = clase?.remoteEventoId
            if (remoteEventoId != null) {
                val asistencia = com.example.syncpassapp.core.database.local.entities.AsistenciaEntity(
                    uuid = java.util.UUID.randomUUID().toString(),
                    eventoId = remoteEventoId,
                    participanteId = "W-$estudianteId",
                    tipoParticipante = "ESTUDIANTE",
                    timestamp = System.currentTimeMillis(),
                    latitud = null,
                    longitud = null,
                    distanciaMetros = null,
                    estadoValidacion = "SIN_UBICACION",
                    inscritoEnSitio = true,
                    registradoPor = null,
                    dispositivoId = null,
                    sincronizado = false
                )
                syncRepository.registrarAsistenciaLocal(asistencia)
            }
            onSuccess()
        }
    }

    fun crearClase(nombre: String, descripcion: String, cuestionarioJson: String, estado: String, remoteEventoId: Long? = null) {
        viewModelScope.launch {
            val nuevaClase = ClaseEntity(
                nombre = nombre,
                descripcion = descripcion,
                profesorId = profesorId,
                cuestionarioEstructuraJson = cuestionarioJson,
                estado = estado,
                remoteEventoId = remoteEventoId
            )
            repository.crearClase(nuevaClase)
        }
    }
}
