package com.example.syncpassapp.core.database.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "participantes")
data class ParticipanteEntity(
    @PrimaryKey val id: String,
    val alias: String,
    val tipo: String
)
