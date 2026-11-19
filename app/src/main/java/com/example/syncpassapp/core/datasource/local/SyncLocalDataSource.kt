package com.example.syncpassapp.core.datasource.local

import com.example.syncpassapp.core.database.local.database.SyncPassDao
import com.example.syncpassapp.core.database.local.entities.AsistenciaEntity
import com.example.syncpassapp.core.database.local.entities.EncuestaEntity
import com.example.syncpassapp.core.database.local.entities.EventoEntity
import com.example.syncpassapp.core.database.local.entities.InscripcionEntity
import com.example.syncpassapp.core.database.local.entities.ParticipanteEntity
import com.example.syncpassapp.core.database.local.entities.ProyectoEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Datasource local del modelo SyncPass (Room): catálogo descargado y registros
 * offline pendientes de sincronizar. Es la única clase que usa [SyncPassDao];
 * [com.example.syncpassapp.core.database.repository.SyncRepository] trabaja contra esta.
 */
@Singleton
class SyncLocalDataSource @Inject constructor(
    private val dao: SyncPassDao
) {

    // --- Catálogo (proyectos/cursos, eventos, participantes e inscripciones) ---

    suspend fun guardarCatalogo(
        proyectos: List<ProyectoEntity>,
        eventos: List<EventoEntity>,
        participantes: List<ParticipanteEntity>,
        inscripciones: List<InscripcionEntity>
    ) {
        dao.insertProyectos(proyectos)
        dao.insertEventos(eventos)
        dao.insertParticipantes(participantes)
        dao.insertInscripciones(inscripciones)
    }

    fun eventos(): Flow<List<EventoEntity>> = dao.getAllEventos()

    fun participantes(): Flow<List<ParticipanteEntity>> = dao.getAllParticipantes()

    // --- Registros offline ---

    suspend fun guardarAsistencia(asistencia: AsistenciaEntity) = dao.insertAsistencia(asistencia)

    suspend fun guardarEncuesta(encuesta: EncuestaEntity) = dao.insertEncuesta(encuesta)

    suspend fun asistenciasPendientes(): List<AsistenciaEntity> = dao.getAsistenciasPendientes()

    suspend fun encuestasPendientes(): List<EncuestaEntity> = dao.getEncuestasPendientes()

    fun contarAsistenciasPendientes(): Flow<Int> = dao.contarAsistenciasPendientes()

    fun contarEncuestasPendientes(): Flow<Int> = dao.contarEncuestasPendientes()

    suspend fun marcarAsistenciasSincronizadas(uuids: List<String>) {
        if (uuids.isNotEmpty()) dao.markAsistenciasAsSincronizadas(uuids)
    }

    suspend fun marcarEncuestasSincronizadas(uuids: List<String>) {
        if (uuids.isNotEmpty()) dao.markEncuestasAsSincronizadas(uuids)
    }
}
