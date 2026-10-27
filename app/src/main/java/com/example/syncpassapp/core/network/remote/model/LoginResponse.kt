package com.example.syncpassapp.core.network.remote.model

data class LoginResponse(
    val token: String,
    val usuario: UsuarioDto?
)
