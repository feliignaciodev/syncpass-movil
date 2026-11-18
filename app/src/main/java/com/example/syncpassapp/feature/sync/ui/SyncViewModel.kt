package com.example.syncpassapp.feature.sync.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.syncpassapp.core.database.local.entities.AsistenciaEntity
import com.example.syncpassapp.core.database.local.entities.EventoEntity
import com.example.syncpassapp.core.database.local.entities.ParticipanteEntity
import com.example.syncpassapp.core.database.repository.ResumenSync
import com.example.syncpassapp.core.database.repository.SyncRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Estado de sincronización esperado por [com.example.syncpassapp.feature.perfil.ui.ProfileScreen].
 * Se mantiene esta forma para no romper la integración ya existente en la UI.
 */
data class SyncUiState(
    val pendientesAsistencias: Int = 0,
    val pendientesEncuestas: Int = 0,
    val sincronizando: Boolean = false,
    val isDownloading: Boolean = false,
    val mensaje: String? = null,
    val esError: Boolean = false,
    val aceptados: Int = 0,
    val duplicados: Int = 0,
    val rechazados: Int = 0,
    val eventos: List<EventoEntity> = emptyList(),
    val participantes: List<ParticipanteEntity> = emptyList()
) {
    val totalPendientes: Int get() = pendientesAsistencias + pendientesEncuestas
}

@HiltViewModel
class SyncViewModel @Inject constructor(
    private val syncRepository: SyncRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SyncUiState())
    val uiState: StateFlow<SyncUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            syncRepository.asistenciasPendientes.collectLatest { count ->
                _uiState.value = _uiState.value.copy(pendientesAsistencias = count)
            }
        }
        viewModelScope.launch {
            syncRepository.encuestasPendientes.collectLatest { count ->
                _uiState.value = _uiState.value.copy(pendientesEncuestas = count)
            }
        }
        viewModelScope.launch {
            syncRepository.obtenerEventosLocales().collectLatest { lista ->
                _uiState.value = _uiState.value.copy(eventos = lista)
            }
        }
        viewModelScope.launch {
            syncRepository.obtenerParticipantesLocales().collectLatest { lista ->
                _uiState.value = _uiState.value.copy(participantes = lista)
            }
        }
    }

    fun descargarCatalogo() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDownloading = true, mensaje = null, esError = false)
            val result = syncRepository.descargarYGuardarCatalogo()
            _uiState.value = if (result.isSuccess) {
                _uiState.value.copy(isDownloading = false, mensaje = "Catálogo descargado correctamente.")
            } else {
                _uiState.value.copy(
                    isDownloading = false,
                    mensaje = result.exceptionOrNull()?.message ?: "No se pudo descargar el catálogo.",
                    esError = true
                )
            }
        }
    }

    fun sincronizarPendientes() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(sincronizando = true, mensaje = null, esError = false)
            val result = syncRepository.sincronizarPendientes()
            _uiState.value = if (result.isSuccess) {
                val resumen = result.getOrNull()
                if (resumen == null) {
                    _uiState.value.copy(sincronizando = false, mensaje = "No hay registros pendientes.")
                } else {
                    _uiState.value.copy(
                        sincronizando = false,
                        mensaje = "Sincronización completada.",
                        esError = false,
                        aceptados = resumen.aceptados,
                        duplicados = resumen.duplicados,
                        rechazados = resumen.rechazados
                    )
                }
            } else {
                _uiState.value.copy(
                    sincronizando = false,
                    mensaje = result.exceptionOrNull()?.message?.let {
                        "$it. Los registros pendientes se conservan."
                    } ?: "Error de red. Los registros pendientes se conservan.",
                    esError = true
                )
            }
        }
    }

    /**
     * Registra una asistencia pendiente usando IDs del catálogo remoto.
     * No modifica el flujo de clases existente: es una captura adicional que
     * demuestra el ciclo offline -> pendiente -> sync sin inventar IDs.
     */
    fun registrarAsistenciaOffline(
        eventoId: Long,
        participanteId: String,
        tipoParticipante: String,
        latitud: Double? = null,
        longitud: Double? = null,
        distanciaMetros: Double? = null,
        estadoValidacion: String = "SIN_UBICACION",
        inscritoEnSitio: Boolean = false,
        registradoPor: Long? = null,
        dispositivoId: String? = null
    ) {
        viewModelScope.launch {
            val asistencia = AsistenciaEntity(
                uuid = UUID.randomUUID().toString(),
                eventoId = eventoId,
                participanteId = participanteId,
                tipoParticipante = tipoParticipante,
                timestamp = System.currentTimeMillis(),
                latitud = latitud,
                longitud = longitud,
                distanciaMetros = distanciaMetros,
                estadoValidacion = estadoValidacion,
                inscritoEnSitio = inscritoEnSitio,
                registradoPor = registradoPor,
                dispositivoId = dispositivoId,
                sincronizado = false
            )
            syncRepository.registrarAsistenciaLocal(asistencia)
            _uiState.value = _uiState.value.copy(mensaje = "Asistencia guardada como pendiente.")
        }
    }

    fun limpiarMensaje() {
        _uiState.value = _uiState.value.copy(mensaje = null, esError = false)
    }
}
