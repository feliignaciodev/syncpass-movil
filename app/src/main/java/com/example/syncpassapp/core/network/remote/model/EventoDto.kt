package com.example.syncpassapp.core.network.remote.model

data class EventoDto(
    val id: Long,
    val proyectoId: Long,
    val nombre: String,
    val fechaHora: Long,
    val cuposMaximos: Int,
    val quorumMinimo: Int,
    val recinto: String,
    val direccion: String,
    val latitud: Double,
    val longitud: Double,
    val radioMetros: Int
)
