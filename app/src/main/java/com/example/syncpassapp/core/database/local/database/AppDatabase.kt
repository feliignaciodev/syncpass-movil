package com.example.syncpassapp.core.database.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.syncpassapp.core.database.local.entities.AsistenciaClaseEntity
import com.example.syncpassapp.core.database.local.entities.AsistenciaEntity
import com.example.syncpassapp.core.database.local.entities.ClaseEntity
import com.example.syncpassapp.core.database.local.entities.EncuestaEntity
import com.example.syncpassapp.core.database.local.entities.EventoEntity
import com.example.syncpassapp.core.database.local.entities.InscripcionEntity
import com.example.syncpassapp.core.database.local.entities.ParticipanteEntity
import com.example.syncpassapp.core.database.local.entities.ProyectoEntity
import com.example.syncpassapp.core.database.local.entities.RolEntity
import com.example.syncpassapp.core.database.local.entities.UsuarioEntity
import com.example.syncpassapp.core.database.local.entities.UsuarioRolCrossRef

@Database(
    entities = [
        UsuarioEntity::class,
        RolEntity::class,
        UsuarioRolCrossRef::class,
        ClaseEntity::class,
        AsistenciaClaseEntity::class,
        ProyectoEntity::class,
        EventoEntity::class,
        ParticipanteEntity::class,
        InscripcionEntity::class,
        AsistenciaEntity::class,
        EncuestaEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun universidadDao(): UniversidadDao
    abstract fun syncPassDao(): SyncPassDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "syncpass_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
