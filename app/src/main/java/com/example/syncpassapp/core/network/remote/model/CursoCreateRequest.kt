package com.example.syncpassapp.core.network.remote.model

/**
 * Solicitud de creación de un curso (Proyecto) en el backend.
 * Solo ADMINISTRADOR y COORDINADOR pueden crear cursos.
 */
data class CursoCreateRequest(
    val titulo: String,
    val descripcion: String? = null,
    val areaTematica: String,
    val unidadVecinal: String,
    val fechaInicio: String,   // ISO: yyyy-MM-dd
    val fechaTermino: String,  // ISO: yyyy-MM-dd
    val estado: String = "PLANIFICADO"
)
