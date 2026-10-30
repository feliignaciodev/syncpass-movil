package com.example.syncpassapp.core.network.remote.model

data class UsuarioResponse(
    val id: Long,
    val username: String,
    val email: String,
    val nombre: String,
    val rol: String?,
    val activo: Boolean
)
