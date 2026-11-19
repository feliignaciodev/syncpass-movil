package com.example.syncpassapp.feature.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.syncpassapp.core.database.local.entities.ClaseEntity
import com.example.syncpassapp.core.database.local.entities.RolEntity
import com.example.syncpassapp.core.database.local.entities.UsuarioEntity
import com.example.syncpassapp.core.database.repository.UniversidadRepository
import com.example.syncpassapp.feature.auth.data.AuthRepository
import com.example.syncpassapp.feature.auth.data.AuthResult
import com.example.syncpassapp.feature.notificaciones.data.NotificacionesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val emailInput: String = "",
    val passwordInput: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

/** Nombres de los roles del modelo local de clases (no son los roles de la rúbrica, ver Rol). */
internal const val ROL_LOCAL_PROFESOR = "Profesor"
internal const val ROL_LOCAL_ESTUDIANTE = "Estudiante"

/**
 * Login: valida el formulario, autentica contra el backend (AuthRepository),
 * registra al usuario en el modelo local y decide la navegación por rol.
 * Solo depende de repositorios.
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val universidadRepository: UniversidadRepository,
    private val notificacionesRepository: NotificacionesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChanged(newEmail: String) {
        _uiState.update { it.copy(emailInput = newEmail, errorMessage = null) }
    }

    fun onPasswordChanged(newPassword: String) {
        _uiState.update { it.copy(passwordInput = newPassword, errorMessage = null) }
    }

    fun loginDemoProfesor(
        onSuccessProfesor: (Long) -> Unit,
        onSuccessEstudiante: (Long) -> Unit
    ) {
        _uiState.update { it.copy(emailInput = "profesor@syncpass.com", passwordInput = "password123") }
        login(onSuccessProfesor, onSuccessEstudiante)
    }

    fun loginDemoEstudiante(
        onSuccessProfesor: (Long) -> Unit,
        onSuccessEstudiante: (Long) -> Unit
    ) {
        _uiState.update { it.copy(emailInput = "estudiante@syncpass.com", passwordInput = "password123") }
        login(onSuccessProfesor, onSuccessEstudiante)
    }

    /**
     * @param onSuccessProfesor navegación al inicio docente (Docente, Voluntario o Administrador).
     * @param onSuccessEstudiante navegación al inicio de estudiante.
     */
    fun login(
        onSuccessProfesor: (Long) -> Unit,
        onSuccessEstudiante: (Long) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val email = _uiState.value.emailInput.trim()
            val password = _uiState.value.passwordInput

            if (email.isEmpty() || password.isEmpty()) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Correo y contraseña son obligatorios."
                    )
                }
                return@launch
            }

            // Retrofit y Room con funciones suspend ya son main-safe: no hace falta cambiar de dispatcher.
            authRepository.login(email, password).fold(
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Error de autenticación: ${exception.message}"
                        )
                    }
                },
                onSuccess = { result ->
                    val localUserId = syncUserWithLocalDb(result)
                    notificacionesRepository.activarPara(result.userInfo.email ?: email)
                    _uiState.update { it.copy(isLoading = false) }

                    if (result.userInfo.esPersonal) {
                        onSuccessProfesor(localUserId)
                    } else {
                        onSuccessEstudiante(localUserId)
                    }
                }
            )
        }
    }

    /**
     * Crea (o reutiliza) el usuario en el modelo local de clases y le asigna su
     * rol local: "Profesor" si es personal (Docente/Voluntario/Administrador),
     * "Estudiante" si no.
     */
    private suspend fun syncUserWithLocalDb(authResult: AuthResult): Long {
        val userInfo = authResult.userInfo
        val rolLocal = if (userInfo.esPersonal) ROL_LOCAL_PROFESOR else ROL_LOCAL_ESTUDIANTE

        val existing = universidadRepository.buscarUsuarioPorEmail(userInfo.email ?: "")
        if (existing != null) {
            asignarRolLocal(existing.usuario.usuarioId, rolLocal)
            return existing.usuario.usuarioId
        }

        val nombre = userInfo.name ?: userInfo.email?.split("@")?.first() ?: "Usuario"
        val email = userInfo.email ?: ""

        val usuarioId = universidadRepository.crearUsuario(
            UsuarioEntity(nombre = nombre, email = email)
        )
        asignarRolLocal(usuarioId, rolLocal)

        // Datos demo la primera vez que se usa la app en el teléfono
        if (universidadRepository.obtenerTodasLasClases().isEmpty()) {
            crearDatosDemoIniciales(usuarioId, email)
        }

        return usuarioId
    }

    private suspend fun asignarRolLocal(usuarioId: Long, nombreRol: String) {
        universidadRepository.obtenerRolIdPorNombre(nombreRol)?.let { rolId ->
            universidadRepository.asignarRolAUsuario(usuarioId, rolId)
        }
    }

    private suspend fun crearDatosDemoIniciales(profesorId: Long, email: String) {
        universidadRepository.obtenerRolIdPorNombre(ROL_LOCAL_PROFESOR)
            ?: universidadRepository.crearRol(RolEntity(nombreRol = ROL_LOCAL_PROFESOR))
        val rolEstudianteId = universidadRepository.obtenerRolIdPorNombre(ROL_LOCAL_ESTUDIANTE)
            ?: universidadRepository.crearRol(RolEntity(nombreRol = ROL_LOCAL_ESTUDIANTE))

        val claseId = universidadRepository.crearClase(
            ClaseEntity(
                nombre = "Primer Curso",
                descripcion = "Curso de prueba de persistencia local",
                profesorId = profesorId,
                cuestionarioEstructuraJson = "{\"preguntas\":[\"¿Asististe a la clase?\", \"¿Qué aprendiste hoy?\"]}"
            )
        )

        // Crear un estudiante demo si el usuario logueado es profesor
        if (email != "docente.dev@gmail.com") {
            val estudianteId = universidadRepository.crearUsuario(
                UsuarioEntity(nombre = "Estudiante Demo", email = "demo@estudiante.edu")
            )
            universidadRepository.asignarRolAUsuario(estudianteId, rolEstudianteId)
            universidadRepository.inscribirEstudianteEnClase(estudianteId, claseId)
        }
    }
}
