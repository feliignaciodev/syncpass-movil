package com.example.syncpassapp.util

import com.example.syncpassapp.core.network.remote.SyncApi
import com.example.syncpassapp.core.network.remote.model.AsistenciaLoteRequest
import com.example.syncpassapp.core.network.remote.model.CatalogoResponse
import com.example.syncpassapp.core.network.remote.model.EncuestaLoteRequest
import com.example.syncpassapp.core.network.remote.model.ResultadoSyncResponse

/** API en memoria: registra llamadas y permite simular respuestas y fallos. */
class FakeSyncApi : SyncApi {

    var catalogo: CatalogoResponse = CatalogoResponse(
        serverTime = 1L,
        proyectos = emptyList(),
        eventos = emptyList(),
        participantes = emptyList(),
        inscripciones = emptyList()
    )
    var resultadoAsistencias: ResultadoSyncResponse = resultadoVacio()
    var resultadoEncuestas: ResultadoSyncResponse = resultadoVacio()
    var errorCatalogo: Exception? = null
    var errorAsistencias: Exception? = null
    var errorEncuestas: Exception? = null

    var llamadasCatalogo: Int = 0
        private set
    val lotesAsistencias = mutableListOf<AsistenciaLoteRequest>()
    val lotesEncuestas = mutableListOf<EncuestaLoteRequest>()

    override suspend fun obtenerCatalogo(): CatalogoResponse {
        llamadasCatalogo++
        errorCatalogo?.let { throw it }
        return catalogo
    }

    override suspend fun sincronizarAsistencias(request: AsistenciaLoteRequest): ResultadoSyncResponse {
        lotesAsistencias.add(request)
        errorAsistencias?.let { throw it }
        return resultadoAsistencias
    }

    override suspend fun sincronizarEncuestas(request: EncuestaLoteRequest): ResultadoSyncResponse {
        lotesEncuestas.add(request)
        errorEncuestas?.let { throw it }
        return resultadoEncuestas
    }

    companion object {
        fun resultadoVacio() = ResultadoSyncResponse(
            aceptados = emptyList(),
            duplicados = emptyList(),
            rechazados = emptyList(),
            serverTime = 1L
        )
    }
}
