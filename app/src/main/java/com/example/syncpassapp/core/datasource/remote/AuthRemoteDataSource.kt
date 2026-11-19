package com.example.syncpassapp.core.datasource.remote

import com.example.syncpassapp.core.network.remote.AuthApi
import com.example.syncpassapp.core.network.remote.model.ConfirmarRegistroRequest
import com.example.syncpassapp.core.network.remote.model.LoginRequest
import com.example.syncpassapp.core.network.remote.model.LoginResponse
import com.example.syncpassapp.core.network.remote.model.ReenviarCodigoRequest
import com.example.syncpassapp.core.network.remote.model.RegistroRequest
import com.example.syncpassapp.core.network.remote.model.UsuarioResponse
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Datasource remoto de autenticación (Retrofit → `/api/auth/...`). El backend
 * habla con Cognito; la app nunca llama a Cognito directamente.
 */
@Singleton
class AuthRemoteDataSource @Inject constructor(
    private val api: AuthApi
) {

    suspend fun login(usuario: String, password: String): LoginResponse =
        api.login(LoginRequest(usuario, password))

    suspend fun registrar(request: RegistroRequest): UsuarioResponse = api.registrar(request)

    suspend fun confirmarRegistro(email: String, codigo: String) =
        api.confirmar(ConfirmarRegistroRequest(email, codigo))

    suspend fun reenviarCodigo(email: String) = api.reenviarCodigo(ReenviarCodigoRequest(email))
}
