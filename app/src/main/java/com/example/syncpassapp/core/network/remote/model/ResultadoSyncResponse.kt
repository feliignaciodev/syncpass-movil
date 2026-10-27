package com.example.syncpassapp.core.network.remote.model

data class ResultadoSyncResponse(
    val aceptados: List<String>,
    val duplicados: List<String>,
    val rechazados: List<RechazoDto>,
    val serverTime: Long
)
