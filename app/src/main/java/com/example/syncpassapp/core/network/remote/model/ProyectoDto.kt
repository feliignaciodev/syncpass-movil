package com.example.syncpassapp.core.network.remote.model

data class ProyectoDto(
    val id: Long,
    val titulo: String,
    val descripcion: String,
    val areaTematica: String,
    val unidadVecinal: String,
    val fechaInicio: String,
    val fechaTermino: String,
    val estado: String
)
