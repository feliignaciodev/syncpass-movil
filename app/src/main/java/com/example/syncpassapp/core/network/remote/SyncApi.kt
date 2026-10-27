package com.example.syncpassapp.core.network.remote

import com.example.syncpassapp.core.network.remote.model.AsistenciaLoteRequest
import com.example.syncpassapp.core.network.remote.model.CatalogoResponse
import com.example.syncpassapp.core.network.remote.model.EncuestaLoteRequest
import com.example.syncpassapp.core.network.remote.model.ResultadoSyncResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface SyncApi {
    @GET("api/sync/catalogo")
    suspend fun obtenerCatalogo(): CatalogoResponse

    @POST("api/sync/asistencias")
    suspend fun sincronizarAsistencias(@Body request: AsistenciaLoteRequest): ResultadoSyncResponse

    @POST("api/sync/encuestas")
    suspend fun sincronizarEncuestas(@Body request: EncuestaLoteRequest): ResultadoSyncResponse
}
