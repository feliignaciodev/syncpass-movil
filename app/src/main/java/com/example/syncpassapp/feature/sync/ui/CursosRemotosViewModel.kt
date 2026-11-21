package com.example.syncpassapp.feature.sync.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.syncpassapp.core.database.local.entities.ProyectoEntity
import com.example.syncpassapp.core.database.repository.SyncRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Expone los cursos (proyectos) descargados del backend y permite refrescarlos.
 * Los ViewModels de la app móvil no llaman a Retrofit directamente; delegan en [SyncRepository].
 */
@HiltViewModel
class CursosRemotosViewModel @Inject constructor(
    private val syncRepository: SyncRepository
) : ViewModel() {

    val cursos: StateFlow<List<ProyectoEntity>> = syncRepository.obtenerCursosRemotos()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _descargando = MutableStateFlow(false)
    val descargando: StateFlow<Boolean> = _descargando

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje

    fun descargar() {
        viewModelScope.launch {
            _descargando.value = true
            _mensaje.value = null
            val result = syncRepository.descargarYGuardarCatalogo()
            _descargando.value = false
            _mensaje.value = if (result.isSuccess) {
                "Cursos actualizados desde el servidor."
            } else {
                result.exceptionOrNull()?.message ?: "No se pudieron descargar los cursos."
            }
        }
    }

    fun limpiarMensaje() {
        _mensaje.value = null
    }
}
