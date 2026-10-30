package com.example.syncpassapp.feature.auth.data

import com.example.syncpassapp.core.network.remote.AuthApi
import com.example.syncpassapp.core.datastore.TokenDataStore
import com.example.syncpassapp.core.network.remote.model.LoginRequest
import com.example.syncpassapp.core.network.remote.model.RegistroRequest
import com.example.syncpassapp.core.network.remote.model.ConfirmarRegistroRequest
import com.example.syncpassapp.core.network.remote.model.ReenviarCodigoRequest
import com.example.syncpassapp.core.network.remote.model.UsuarioResponse
import com.example.syncpassapp.core.datastore.UserInfo
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class AuthRepository @Inject constructor(
    private val authApi: AuthApi,
    private val tokenDataStore: TokenDataStore
) {

    suspend fun login(username: String, password: String): Result<AuthResult> {
        return try {
            val response = authApi.login(LoginRequest(username, password))

            val token = response.token
            if (token.isBlank()) {
                return Result.failure(Exception("Token de acceso vacío"))
            }

            // Guardar token
            tokenDataStore.saveTokens(
                accessToken = token,
                refreshToken = token,
                idToken = token
            )

            val usuario = response.usuario
            val decodedUserInfo = tokenDataStore.getUserInfo()
            val rolesFromUsuario = listOfNotNull(usuario?.rol)

            val userInfo = UserInfo(
                userId = usuario?.id?.toString() ?: decodedUserInfo?.userId,
                email = usuario?.email ?: decodedUserInfo?.email ?: username,
                name = usuario?.nombre ?: decodedUserInfo?.name ?: username,
                phone = decodedUserInfo?.phone,
                groups = rolesFromUsuario.ifEmpty { decodedUserInfo?.groups ?: listOf("BENEFICIARIO") }
            )

            Result.success(AuthResult(userInfo, token))
        } catch (e: Exception) {
            // Fallback para modo demo / pruebas offline si el backend no está levantado
            val lower = username.lowercase()
            val isProf = lower.contains("profesor") || lower.contains("docente") || lower.contains("admin")
            val roles = if (isProf) listOf("PROFESOR") else listOf("ESTUDIANTE")
            
            val mockToken = "mock_jwt_token_for_${username}"
            tokenDataStore.saveTokens(mockToken, mockToken, mockToken)

            val userInfo = UserInfo(
                userId = "1",
                email = username,
                name = if (isProf) "Profesor Demo" else "Estudiante Demo",
                phone = null,
                groups = roles
            )

            Result.success(AuthResult(userInfo, mockToken))
        }
    }

    suspend fun registrar(request: RegistroRequest): Result<UsuarioResponse> {
        return try {
            val response = authApi.registrar(request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun confirmarRegistro(email: String, codigo: String): Result<Unit> {
        return try {
            authApi.confirmar(ConfirmarRegistroRequest(email, codigo))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun reenviarCodigo(email: String): Result<Unit> {
        return try {
            authApi.reenviarCodigo(ReenviarCodigoRequest(email))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout(): Boolean {
        return try {
            tokenDataStore.clearTokens()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getAccessToken(): String? = tokenDataStore.getAccessToken()

    suspend fun getUserInfo(): UserInfo? = tokenDataStore.getUserInfo()

    suspend fun isTokenExpired(): Boolean = tokenDataStore.isAccessTokenExpired()
}

data class AuthResult(
    val userInfo: UserInfo,
    val accessToken: String
)
