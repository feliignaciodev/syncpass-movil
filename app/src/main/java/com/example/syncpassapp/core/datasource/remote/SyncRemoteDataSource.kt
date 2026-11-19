package com.example.syncpassapp.core.datasource.remote

import com.example.syncpassapp.core.network.remote.SyncApi
import com.example.syncpassapp.core.network.remote.model.AsistenciaDto
import com.example.syncpassapp.core.network.remote.model.AsistenciaLoteRequest
import com.example.syncpassapp.core.network.remote.model.CatalogoResponse
import com.example.syncpassapp.core.network.remote.model.EncuestaDto
import com.example.syncpassapp.core.network.remote.model.EncuestaLoteRequest
import com.example.syncpassapp.core.network.remote.model.ResultadoSyncResponse
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Datasource remoto de sincronización (Retrofit → `/api/sync/...` del backend).
 * Es la única clase que usa [SyncApi]; arma los lotes que espera el backend.
 */
@Singleton
class SyncRemoteDataSource @Inject constructor(
    private val api: SyncApi
) {

    /** `GET /api/sync/catalogo`: cursos activos, eventos, participantes e inscripciones. */
    suspend fun obtenerCatalogo(): CatalogoResponse = api.obtenerCatalogo()

    /** `POST /api/sync/asistencias`: el backend responde aceptados, duplicados (por uuid) y rechazados. */
    suspend fun enviarAsistencias(asistencias: List<AsistenciaDto>): ResultadoSyncResponse =
        api.sincronizarAsistencias(AsistenciaLoteRequest(asistencias))

    /** `POST /api/sync/encuestas`: misma respuesta que las asistencias. */
    suspend fun enviarEncuestas(encuestas: List<EncuestaDto>): ResultadoSyncResponse =
        api.sincronizarEncuestas(EncuestaLoteRequest(encuestas))
}
