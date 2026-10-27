package com.example.syncpassapp.core.network.remote.model

data class UsuarioCreateRequest(
    val username: String,
    val email: String,
    val password: String,
    val nombre: String,
    val roles: List<String>
)
