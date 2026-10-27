package com.example.syncpassapp.core.network.remote.model

data class EncuestaDto(
    val uuid: String,
    val eventoId: Long,
    val participanteId: String?,
    val satisfaccion: Int,
    val utilidad: Int,
    val comentario: String?,
    val fechaEnvio: Long
)
