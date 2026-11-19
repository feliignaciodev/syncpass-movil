package com.example.syncpassapp.feature.auth.data

import com.example.syncpassapp.core.datasource.local.SesionLocalDataSource
import com.example.syncpassapp.core.datasource.remote.AuthRemoteDataSource
import com.example.syncpassapp.core.datastore.UserInfo
import com.example.syncpassapp.core.network.remote.model.RegistroRequest
import com.example.syncpassapp.core.network.remote.model.UsuarioResponse
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio de autenticación: login, registro y sesión.
 *
 * Usa [AuthRemoteDataSource] (backend → Cognito) y [SesionLocalDataSource]
 * (token en DataStore). Los ViewModels nunca tocan Retrofit ni DataStore.
 */
@Singleton
class AuthRepository @Inject constructor(
    private val remote: AuthRemoteDataSource,
    private val sesion: SesionLocalDataSource
) {

    suspend fun login(username: String, password: String): Result<AuthResult> {
        return try {
            val response = remote.login(username, password)

            val token = response.token
            if (token.isBlank()) {
                return Result.failure(Exception("Token de acceso vacío"))
            }

            sesion.guardarToken(token)

            // El rol que informa el backend manda; si no viene, se usan los grupos del token.
            val usuario = response.usuario
            val decodedUserInfo = sesion.usuarioActual()
            val rolesFromUsuario = listOfNotNull(usuario?.rol)

            val userInfo = UserInfo(
                userId = usuario?.id?.toString() ?: decodedUserInfo?.userId,
                email = decodedUserInfo?.email ?: username,
                name = usuario?.nombre ?: decodedUserInfo?.name ?: username,
                phone = decodedUserInfo?.phone,
                groups = rolesFromUsuario.ifEmpty { decodedUserInfo?.groups ?: listOf("BENEFICIARIO") }
            )

            Result.success(AuthResult(userInfo, token))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun registrar(request: RegistroRequest): Result<UsuarioResponse> {
        return try {
            Result.success(remote.registrar(request))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun confirmarRegistro(email: String, codigo: String): Result<Unit> {
        return try {
            remote.confirmarRegistro(email, codigo)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun reenviarCodigo(email: String): Result<Unit> {
        return try {
            remote.reenviarCodigo(email)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout(): Boolean {
        return try {
            sesion.borrar()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getAccessToken(): String? = sesion.accessToken()

    suspend fun getUserInfo(): UserInfo? = sesion.usuarioActual()

    suspend fun isTokenExpired(): Boolean = sesion.tokenExpirado()
}

data class AuthResult(
    val userInfo: UserInfo,
    val accessToken: String
)
