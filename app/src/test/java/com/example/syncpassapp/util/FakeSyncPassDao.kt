package com.example.syncpassapp.util

import com.example.syncpassapp.core.database.local.database.SyncPassDao
import com.example.syncpassapp.core.database.local.entities.AsistenciaEntity
import com.example.syncpassapp.core.database.local.entities.EncuestaEntity
import com.example.syncpassapp.core.database.local.entities.EventoEntity
import com.example.syncpassapp.core.database.local.entities.InscripcionEntity
import com.example.syncpassapp.core.database.local.entities.ParticipanteEntity
import com.example.syncpassapp.core.database.local.entities.ProyectoEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** DAO en memoria para tests: no usa Android ni SQLite. */
class FakeSyncPassDao : SyncPassDao {

    private val asistencias = LinkedHashMap<String, AsistenciaEntity>()
    private val encuestas = LinkedHashMap<String, EncuestaEntity>()
    private val _asistencias = MutableStateFlow<List<AsistenciaEntity>>(emptyList())
    private val _encuestas = MutableStateFlow<List<EncuestaEntity>>(emptyList())

    var proyectosGuardados: List<ProyectoEntity> = emptyList()
        private set
    var eventosGuardados: List<EventoEntity> = emptyList()
        private set
    var participantesGuardados: List<ParticipanteEntity> = emptyList()
        private set
    var inscripcionesGuardadas: List<InscripcionEntity> = emptyList()
        private set

    val marcasAsistencias = mutableListOf<List<String>>()
    val marcasEncuestas = mutableListOf<List<String>>()

    override suspend fun insertProyectos(proyectos: List<ProyectoEntity>) {
        proyectosGuardados = proyectos
    }

    override fun getAllProyectos(): Flow<List<ProyectoEntity>> =
        MutableStateFlow(proyectosGuardados)

    override suspend fun insertEventos(eventos: List<EventoEntity>) {
        eventosGuardados = eventos
    }

    override fun getAllEventos(): Flow<List<EventoEntity>> =
        MutableStateFlow(eventosGuardados)

    override suspend fun getEventoById(eventoId: Long): EventoEntity? =
        eventosGuardados.firstOrNull { it.id == eventoId }

    override suspend fun insertParticipantes(participantes: List<ParticipanteEntity>) {
        participantesGuardados = participantes
    }

    override fun getAllParticipantes(): Flow<List<ParticipanteEntity>> =
        MutableStateFlow(participantesGuardados)

    override suspend fun getParticipanteById(participanteId: String): ParticipanteEntity? =
        participantesGuardados.firstOrNull { it.id == participanteId }

    override suspend fun insertInscripciones(inscripciones: List<InscripcionEntity>) {
        inscripcionesGuardadas = inscripciones
    }

    override fun getInscripcionesByEvento(eventoId: Long): Flow<List<InscripcionEntity>> =
        MutableStateFlow(inscripcionesGuardadas.filter { it.eventoId == eventoId })

    override suspend fun insertAsistencia(asistencia: AsistenciaEntity) {
        asistencias[asistencia.uuid] = asistencia
        publicarAsistencias()
    }

    override suspend fun getAsistenciasPendientes(): List<AsistenciaEntity> =
        asistencias.values.filter { !it.sincronizado }

    override fun contarAsistenciasPendientes(): Flow<Int> =
        _asistencias.map { lista -> lista.count { !it.sincronizado } }

    override suspend fun markAsistenciasAsSincronizadas(uuids: List<String>) {
        marcasAsistencias.add(uuids)
        uuids.forEach { uuid ->
            asistencias[uuid]?.let { asistencias[uuid] = it.copy(sincronizado = true) }
        }
        publicarAsistencias()
    }

    override suspend fun insertEncuesta(encuesta: EncuestaEntity) {
        encuestas[encuesta.uuid] = encuesta
        publicarEncuestas()
    }

    override suspend fun getEncuestasPendientes(): List<EncuestaEntity> =
        encuestas.values.filter { !it.sincronizado }

    override fun contarEncuestasPendientes(): Flow<Int> =
        _encuestas.map { lista -> lista.count { !it.sincronizado } }

    override suspend fun markEncuestasAsSincronizadas(uuids: List<String>) {
        marcasEncuestas.add(uuids)
        uuids.forEach { uuid ->
            encuestas[uuid]?.let { encuestas[uuid] = it.copy(sincronizado = true) }
        }
        publicarEncuestas()
    }

    private fun publicarAsistencias() {
        _asistencias.value = asistencias.values.toList()
    }

    private fun publicarEncuestas() {
        _encuestas.value = encuestas.values.toList()
    }
}
