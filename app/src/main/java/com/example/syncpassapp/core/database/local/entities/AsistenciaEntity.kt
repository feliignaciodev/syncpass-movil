package com.example.syncpassapp.core.database.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "asistencias_offline")
data class AsistenciaEntity(
    @PrimaryKey val uuid: String,
    val eventoId: Long,
    val participanteId: String,
    val tipoParticipante: String,
    val timestamp: Long,
    val latitud: Double?,
    val longitud: Double?,
    val distanciaMetros: Double?,
    val estadoValidacion: String,
    val inscritoEnSitio: Boolean,
    val registradoPor: Long?,
    val dispositivoId: String?,
    val sincronizado: Boolean = false
)
