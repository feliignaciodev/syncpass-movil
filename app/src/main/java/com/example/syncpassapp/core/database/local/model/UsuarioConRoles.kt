package com.example.syncpassapp.core.database.local.model

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.syncpassapp.core.database.local.entities.RolEntity
import com.example.syncpassapp.core.database.local.entities.UsuarioEntity
import com.example.syncpassapp.core.database.local.entities.UsuarioRolCrossRef

data class UsuarioConRoles(
    @Embedded
    val usuario: UsuarioEntity,

    @Relation(
        parentColumn = "usuarioId",
        entityColumn = "rolId",
        associateBy = Junction(UsuarioRolCrossRef::class)
    )
    val roles: List<RolEntity>
)

