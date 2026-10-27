package com.example.syncpassapp.core.database.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "encuestas_offline")
data class EncuestaEntity(
    @PrimaryKey val uuid: String,
    val eventoId: Long,
    val participanteId: String?,
    val satisfaccion: Int,
    val utilidad: Int,
    val comentario: String?,
    val fechaEnvio: Long,
    val sincronizado: Boolean = false
)
