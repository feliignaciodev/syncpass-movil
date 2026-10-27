package com.example.syncpassapp.core.database.local.model

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.syncpassapp.core.database.local.entities.RolEntity
import com.example.syncpassapp.core.database.local.entities.UsuarioEntity
import com.example.syncpassapp.core.database.local.entities.UsuarioRolCrossRef

data class RolConUsuarios(
    @Embedded
    val rol: RolEntity,

    @Relation(
        parentColumn = "rolId",
        entityColumn = "usuarioId",
        associateBy = Junction(UsuarioRolCrossRef::class)
    )
    val usuarios: List<UsuarioEntity>
)

