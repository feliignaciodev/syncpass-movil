package com.example.syncpassapp.core.network.remote

import com.example.syncpassapp.core.network.remote.model.ConfirmarRegistroRequest
import com.example.syncpassapp.core.network.remote.model.LoginRequest
import com.example.syncpassapp.core.network.remote.model.LoginResponse
import com.example.syncpassapp.core.network.remote.model.ReenviarCodigoRequest
import com.example.syncpassapp.core.network.remote.model.RegistroRequest
import com.example.syncpassapp.core.network.remote.model.UsuarioResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("api/auth/registro")
    suspend fun registrar(@Body request: RegistroRequest): UsuarioResponse

    @POST("api/auth/registro/confirmar")
    suspend fun confirmar(@Body request: ConfirmarRegistroRequest)

    @POST("api/auth/registro/reenviar-codigo")
    suspend fun reenviarCodigo(@Body request: ReenviarCodigoRequest)
}
