package com.example.syncpassapp.core.network.remote

import com.example.syncpassapp.core.network.remote.model.MetricasEventoDto
import retrofit2.http.GET
import retrofit2.http.Path

interface EventoApi {
    @GET("api/eventos/{id}/metricas")
    suspend fun metricasEvento(@Path("id") id: Long): MetricasEventoDto
}
