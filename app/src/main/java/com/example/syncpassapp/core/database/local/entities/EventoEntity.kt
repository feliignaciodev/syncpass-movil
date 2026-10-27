package com.example.syncpassapp.core.database.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "eventos")
data class EventoEntity(
    @PrimaryKey val id: Long,
    val proyectoId: Long,
    val nombre: String,
    val fechaHora: Long,
    val cuposMaximos: Int,
    val quorumMinimo: Int,
    val recinto: String,
    val direccion: String,
    val latitud: Double,
    val longitud: Double,
    val radioMetros: Int
)
