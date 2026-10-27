package com.example.syncpassapp.core.network.remote.model

data class MetricasEventoDto(
    val eventoId: Long,
    val totalAsistencias: Int,
    val totalEncuestas: Int,
    val quorumAlcanzado: Boolean,
    val promedioSatisfaccion: Double
)
