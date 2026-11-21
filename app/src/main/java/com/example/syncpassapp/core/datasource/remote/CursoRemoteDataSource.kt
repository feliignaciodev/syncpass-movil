package com.example.syncpassapp.core.datasource.remote

import com.example.syncpassapp.core.network.remote.CursoApi
import com.example.syncpassapp.core.network.remote.model.CursoCreateRequest
import com.example.syncpassapp.core.network.remote.model.ProyectoDto
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Datasource remoto de cursos (Retrofit → `/api/cursos` del backend).
 * Es la única clase que usa [CursoApi].
 */
@Singleton
class CursoRemoteDataSource @Inject constructor(
    private val api: CursoApi
) {

    suspend fun listar(): List<ProyectoDto> = api.listar()

    suspend fun crear(request: CursoCreateRequest): ProyectoDto = api.crear(request)
}
