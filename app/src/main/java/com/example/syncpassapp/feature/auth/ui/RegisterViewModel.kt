package com.example.syncpassapp.feature.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.syncpassapp.core.network.remote.model.RegistroRequest
import com.example.syncpassapp.feature.auth.data.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RegisterUiState(
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val nombre: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onUsernameChanged(value: String) { _uiState.update { it.copy(username = value, errorMessage = null) } }
    fun onEmailChanged(value: String) { _uiState.update { it.copy(email = value, errorMessage = null) } }
    fun onPasswordChanged(value: String) { _uiState.update { it.copy(password = value, errorMessage = null) } }
    fun onNombreChanged(value: String) { _uiState.update { it.copy(nombre = value, errorMessage = null) } }

    fun registrar(onSuccess: (String) -> Unit) {
        val state = _uiState.value
        if (state.username.isBlank() || state.email.isBlank() || state.password.isBlank() || state.nombre.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Todos los campos son obligatorios.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = authRepository.registrar(
                RegistroRequest(
                    username = state.username.trim(),
                    email = state.email.trim(),
                    password = state.password,
                    nombre = state.nombre.trim()
                )
            )

            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false, successMessage = "¡Registro exitoso! Revisa tu correo para confirmar.") }
                    onSuccess(state.email.trim())
                },
                onFailure = { err ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Error en registro: ${err.message}") }
                }
            )
        }
    }
}
