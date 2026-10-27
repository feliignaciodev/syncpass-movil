package com.example.syncpassapp.core.database.repository

import com.example.syncpassapp.core.database.local.database.SyncPassDao
import com.example.syncpassapp.core.database.local.entities.AsistenciaEntity
import com.example.syncpassapp.core.database.local.entities.EncuestaEntity
import com.example.syncpassapp.core.database.local.entities.EventoEntity
import com.example.syncpassapp.core.database.local.entities.InscripcionEntity
import com.example.syncpassapp.core.database.local.entities.ParticipanteEntity
import com.example.syncpassapp.core.database.local.entities.ProyectoEntity
import com.example.syncpassapp.core.network.remote.SyncApi
import com.example.syncpassapp.core.network.remote.model.AsistenciaDto
import com.example.syncpassapp.core.network.remote.model.AsistenciaLoteRequest
import com.example.syncpassapp.core.network.remote.model.EncuestaDto
import com.example.syncpassapp.core.network.remote.model.EncuestaLoteRequest
import com.example.syncpassapp.core.network.remote.model.ResultadoSyncResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncRepository @Inject constructor(
    private val syncApi: SyncApi,
    private val syncPassDao: SyncPassDao
) {

    suspend fun descargarYGuardarCatalogo(): Result<Unit> {
        return try {
            val response = syncApi.obtenerCatalogo()

            val proyectos = response.proyectos.map {
                ProyectoEntity(
                    id = it.id,
                    titulo = it.titulo,
                    descripcion = it.descripcion,
                    areaTematica = it.areaTematica,
                    unidadVecinal = it.unidadVecinal,
                    fechaInicio = it.fechaInicio,
                    fechaTermino = it.fechaTermino,
                    estado = it.estado
                )
            }

            val eventos = response.eventos.map {
                EventoEntity(
                    id = it.id,
                    proyectoId = it.proyectoId,
                    nombre = it.nombre,
                    fechaHora = it.fechaHora,
                    cuposMaximos = it.cuposMaximos,
                    quorumMinimo = it.quorumMinimo,
                    recinto = it.recinto,
                    direccion = it.direccion,
                    latitud = it.latitud,
                    longitud = it.longitud,
                    radioMetros = it.radioMetros
                )
            }

            val participantes = response.participantes.map {
                ParticipanteEntity(
                    id = it.id,
                    alias = it.alias,
                    tipo = it.tipo
                )
            }

            val inscripciones = response.inscripciones.map {
                InscripcionEntity(
                    eventoId = it.eventoId,
                    participanteId = it.participanteId,
                    estado = it.estado
                )
            }

            syncPassDao.insertProyectos(proyectos)
            syncPassDao.insertEventos(eventos)
            syncPassDao.insertParticipantes(participantes)
            syncPassDao.insertInscripciones(inscripciones)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sincronizarAsistenciasPendientes(): Result<ResultadoSyncResponse?> {
        return try {
            val pendientes = syncPassDao.getAsistenciasPendientes()
            if (pendientes.isEmpty()) return Result.success(null)

            val dtos = pendientes.map {
                AsistenciaDto(
                    uuid = it.uuid,
                    eventoId = it.eventoId,
                    participanteId = it.participanteId,
                    tipoParticipante = it.tipoParticipante,
                    timestamp = it.timestamp,
                    latitud = it.latitud,
                    longitud = it.longitud,
                    distanciaMetros = it.distanciaMetros,
                    estadoValidacion = it.estadoValidacion,
                    inscritoEnSitio = it.inscritoEnSitio,
                    registradoPor = it.registradoPor,
                    dispositivoId = it.dispositivoId
                )
            }

            val resultado = syncApi.sincronizarAsistencias(AsistenciaLoteRequest(dtos))
            
            val uuidsAceptados = resultado.aceptados + resultado.duplicados
            if (uuidsAceptados.isNotEmpty()) {
                syncPassDao.markAsistenciasAsSincronizadas(uuidsAceptados)
            }

            Result.success(resultado)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sincronizarEncuestasPendientes(): Result<ResultadoSyncResponse?> {
        return try {
            val pendientes = syncPassDao.getEncuestasPendientes()
            if (pendientes.isEmpty()) return Result.success(null)

            val dtos = pendientes.map {
                EncuestaDto(
                    uuid = it.uuid,
                    eventoId = it.eventoId,
                    participanteId = it.participanteId,
                    satisfaccion = it.satisfaccion,
                    utilidad = it.utilidad,
                    comentario = it.comentario,
                    fechaEnvio = it.fechaEnvio
                )
            }

            val resultado = syncApi.sincronizarEncuestas(EncuestaLoteRequest(dtos))

            val uuidsAceptados = resultado.aceptados + resultado.duplicados
            if (uuidsAceptados.isNotEmpty()) {
                syncPassDao.markEncuestasAsSincronizadas(uuidsAceptados)
            }

            Result.success(resultado)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun registrarAsistenciaLocal(asistencia: AsistenciaEntity) {
        syncPassDao.insertAsistencia(asistencia)
    }

    suspend fun registrarEncuestaLocal(encuesta: EncuestaEntity) {
        syncPassDao.insertEncuesta(encuesta)
    }
}
