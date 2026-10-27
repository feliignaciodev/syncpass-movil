package com.example.syncpassapp.core.network.remote.model

data class AsistenciaDto(
    val uuid: String,
    val eventoId: Long,
    val participanteId: String,
    val tipoParticipante: String,
    val timestamp: Long,
    val latitud: Double?,
    val longitud: Double?,
    val distanciaMetros: Double?,
    val estadoValidacion: String,
    val inscritoEnSitio: Boolean,
    val registradoPor: Long?,
    val dispositivoId: String?
)
