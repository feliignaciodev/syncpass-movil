package com.example.syncpassapp.core.network.remote

import com.example.syncpassapp.core.network.remote.model.CursoCreateRequest
import com.example.syncpassapp.core.network.remote.model.ProyectoDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface CursoApi {
    @GET("api/cursos")
    suspend fun listar(): List<ProyectoDto>

    @POST("api/cursos")
    suspend fun crear(@Body request: CursoCreateRequest): ProyectoDto
}
