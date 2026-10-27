package com.example.syncpassapp.core.database.local.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.syncpassapp.core.database.local.entities.AsistenciaEntity
import com.example.syncpassapp.core.database.local.entities.EncuestaEntity
import com.example.syncpassapp.core.database.local.entities.EventoEntity
import com.example.syncpassapp.core.database.local.entities.InscripcionEntity
import com.example.syncpassapp.core.database.local.entities.ParticipanteEntity
import com.example.syncpassapp.core.database.local.entities.ProyectoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncPassDao {

    // --- PROYECTOS ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProyectos(proyectos: List<ProyectoEntity>)

    @Query("SELECT * FROM proyectos")
    fun getAllProyectos(): Flow<List<ProyectoEntity>>

    // --- EVENTOS ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEventos(eventos: List<EventoEntity>)

    @Query("SELECT * FROM eventos")
    fun getAllEventos(): Flow<List<EventoEntity>>

    @Query("SELECT * FROM eventos WHERE id = :eventoId")
    suspend fun getEventoById(eventoId: Long): EventoEntity?

    // --- PARTICIPANTES ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParticipantes(participantes: List<ParticipanteEntity>)

    @Query("SELECT * FROM participantes")
    fun getAllParticipantes(): Flow<List<ParticipanteEntity>>

    @Query("SELECT * FROM participantes WHERE id = :participanteId")
    suspend fun getParticipanteById(participanteId: String): ParticipanteEntity?

    // --- INSCRIPCIONES ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInscripciones(inscripciones: List<InscripcionEntity>)

    @Query("SELECT * FROM inscripciones WHERE eventoId = :eventoId")
    fun getInscripcionesByEvento(eventoId: Long): Flow<List<InscripcionEntity>>

    // --- ASISTENCIAS OFFLINE ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAsistencia(asistencia: AsistenciaEntity)

    @Query("SELECT * FROM asistencias_offline WHERE sincronizado = 0")
    suspend fun getAsistenciasPendientes(): List<AsistenciaEntity>

    @Query("UPDATE asistencias_offline SET sincronizado = 1 WHERE uuid IN (:uuids)")
    suspend fun markAsistenciasAsSincronizadas(uuids: List<String>)

    // --- ENCUESTAS OFFLINE ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEncuesta(encuesta: EncuestaEntity)

    @Query("SELECT * FROM encuestas_offline WHERE sincronizado = 0")
    suspend fun getEncuestasPendientes(): List<EncuestaEntity>

    @Query("UPDATE encuestas_offline SET sincronizado = 1 WHERE uuid IN (:uuids)")
    suspend fun markEncuestasAsSincronizadas(uuids: List<String>)
}
