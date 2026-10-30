package com.example.syncpassapp.core.network.remote.model

data class RegistroRequest(
    val username: String,
    val email: String,
    val password: String,
    val nombre: String,
    val telefono: String
)
