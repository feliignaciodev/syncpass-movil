package com.example.syncpassapp.core.network.remote.model

data class RegistroRequest(
    val email: String,
    val password: String,
    val nombre: String,
    val telefono: String
)
