package com.example.syncpassapp.feature.auth.data

import com.example.syncpassapp.core.network.remote.AuthApi
import com.example.syncpassapp.core.datastore.TokenDataStore
import com.example.syncpassapp.core.network.remote.model.LoginRequest
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

            if (response.token.isBlank()) {
                return Result.failure(Exception("Token de acceso vacío"))
            }

            // Guardar token
            tokenDataStore.saveTokens(
                accessToken = response.token,
                refreshToken = response.token,
                idToken = response.token
            )

            val usuario = response.usuario
            val decodedUserInfo = tokenDataStore.getUserInfo()

            val userInfo = UserInfo(
                userId = usuario?.id?.toString() ?: decodedUserInfo?.userId,
                email = usuario?.email ?: decodedUserInfo?.email ?: username,
                name = usuario?.nombre ?: decodedUserInfo?.name ?: username,
                phone = decodedUserInfo?.phone,
                groups = usuario?.roles?.ifEmpty { null } ?: decodedUserInfo?.groups ?: listOf("ESTUDIANTE")
            )

            Result.success(AuthResult(userInfo, response.token))
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
