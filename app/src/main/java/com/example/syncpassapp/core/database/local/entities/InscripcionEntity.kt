package com.example.syncpassapp.core.database.local.entities

import androidx.room.Entity

@Entity(tableName = "inscripciones", primaryKeys = ["eventoId", "participanteId"])
data class InscripcionEntity(
    val eventoId: Long,
    val participanteId: String,
    val estado: String
)
