package com.example.syncpassapp.feature.clases.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.syncpassapp.core.database.local.entities.AsistenciaEntity
import com.example.syncpassapp.core.database.local.model.ClaseConDetalles
import com.example.syncpassapp.core.database.repository.SyncRepository
import com.example.syncpassapp.core.database.repository.UniversidadRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SessionUiState(
    val claseId: Long = 0L,
    val detalles: ClaseConDetalles? = null
)

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val repository: UniversidadRepository,
    private val syncRepository: SyncRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val claseId: Long = checkNotNull(
        savedStateHandle.get<Long>("claseId")
            ?: savedStateHandle.get<String>("claseId")?.toLongOrNull()
    ) { "claseId es requerido" }

    val uiState: StateFlow<SessionUiState> = repository.obtenerClaseConDetalles(claseId)
        .map { SessionUiState(claseId = claseId, detalles = it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SessionUiState(claseId = claseId)
        )

    fun marcarEstudiantePresente(estudianteId: Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.registrarAsistenciaYCuestionario(
                usuarioId = estudianteId,
                claseId = claseId,
                esPresente = true,
                respuestasJson = null
            )

            // Si la clase está vinculada a un evento remoto, también registrar
            // la asistencia en asistencias_offline para sincronizarla al backend.
            val clase = repository.obtenerClasePorId(claseId)
            val remoteEventoId = clase?.remoteEventoId
            if (remoteEventoId != null) {
                val asistencia = AsistenciaEntity(
                    uuid = java.util.UUID.randomUUID().toString(),
                    eventoId = remoteEventoId,
                    participanteId = "W-$estudianteId", // formato walk-in que el backend acepta
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
}

