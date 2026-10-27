package com.example.syncpassapp.core.database.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "proyectos")
data class ProyectoEntity(
    @PrimaryKey val id: Long,
    val titulo: String,
    val descripcion: String,
    val areaTematica: String,
    val unidadVecinal: String,
    val fechaInicio: String,
    val fechaTermino: String,
    val estado: String
)
