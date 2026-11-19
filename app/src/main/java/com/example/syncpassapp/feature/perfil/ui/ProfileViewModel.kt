package com.example.syncpassapp.feature.perfil.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.syncpassapp.core.common.auth.Rol
import com.example.syncpassapp.core.database.local.model.UsuarioConRoles
import com.example.syncpassapp.core.database.repository.UniversidadRepository
import com.example.syncpassapp.feature.auth.data.AuthRepository
import com.example.syncpassapp.feature.notificaciones.data.NotificacionesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val usuarioId: Long = 0L,
    val usuarioConRoles: UsuarioConRoles? = null,
    /** Perfil de la rúbrica según el token de la sesión (Estudiante, Docente, Voluntario o Administrador). */
    val rol: Rol? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: UniversidadRepository,
    private val authRepository: AuthRepository,
    private val notificacionesRepository: NotificacionesRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // El argumento de navegación puede llegar como Long o como texto.
    val usuarioId: Long = checkNotNull(
        when (val valor = savedStateHandle.get<Any>("usuarioId")) {
            is Long -> valor
            is String -> valor.toLongOrNull()
            else -> null
        }
    ) { "usuarioId es requerido" }

    private val rolSesion = flow { emit(authRepository.getUserInfo()?.rolPrincipal) }
        .onStart { emit(null) }

    val uiState: StateFlow<ProfileUiState> = combine(
        repository.obtenerUsuarioConRoles(usuarioId),
        rolSesion
    ) { usuarioConRoles, rol ->
        ProfileUiState(usuarioId = usuarioId, usuarioConRoles = usuarioConRoles, rol = rol)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProfileUiState(usuarioId = usuarioId)
    )

    /** Cierra la sesión: deja de escuchar notificaciones y borra los tokens guardados por el login. */
    fun logout() {
        viewModelScope.launch {
            notificacionesRepository.desactivar()
            authRepository.logout()
        }
    }
}
