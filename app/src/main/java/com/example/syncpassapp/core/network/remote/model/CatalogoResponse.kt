package com.example.syncpassapp.core.network.remote.model

data class CatalogoResponse(
    val serverTime: Long,
    val proyectos: List<ProyectoDto>,
    val eventos: List<EventoDto>,
    val participantes: List<ParticipanteDto>,
    val inscripciones: List<InscripcionDto>
)
