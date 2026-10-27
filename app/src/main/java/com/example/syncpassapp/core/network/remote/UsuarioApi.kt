package com.example.syncpassapp.core.network.remote

import com.example.syncpassapp.core.network.remote.model.ActivoPatchRequest
import com.example.syncpassapp.core.network.remote.model.UsuarioCreateRequest
import com.example.syncpassapp.core.network.remote.model.UsuarioResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface UsuarioApi {
    @GET("api/usuarios")
    suspend fun listar(): List<UsuarioResponse>

    @GET("api/usuarios/{id}")
    suspend fun obtener(@Path("id") id: Long): UsuarioResponse

    @POST("api/usuarios")
    suspend fun crear(@Body request: UsuarioCreateRequest): UsuarioResponse

    @PATCH("api/usuarios/{id}/activo")
    suspend fun cambiarActivo(@Path("id") id: Long, @Body request: ActivoPatchRequest): UsuarioResponse
}
