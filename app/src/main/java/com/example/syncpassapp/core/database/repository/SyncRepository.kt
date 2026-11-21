package com.example.syncpassapp.core.database.repository

import com.example.syncpassapp.core.database.local.entities.AsistenciaEntity
import com.example.syncpassapp.core.database.local.entities.EncuestaEntity
import com.example.syncpassapp.core.database.local.entities.EventoEntity
import com.example.syncpassapp.core.database.local.entities.InscripcionEntity
import com.example.syncpassapp.core.database.local.entities.ParticipanteEntity
import com.example.syncpassapp.core.database.local.entities.ProyectoEntity
import com.example.syncpassapp.core.datasource.local.SyncLocalDataSource
import com.example.syncpassapp.core.datasource.remote.SyncRemoteDataSource
import com.example.syncpassapp.core.network.remote.model.AsistenciaDto
import com.example.syncpassapp.core.network.remote.model.EncuestaDto
import com.example.syncpassapp.core.network.remote.model.ResultadoSyncResponse
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/** Resumen agregado de una corrida de sincronización de todos los pendientes. */
data class ResumenSync(
    val aceptados: Int = 0,
    val duplicados: Int = 0,
    val rechazados: Int = 0
)

/**
 * Repositorio offline-first del modelo SyncPass.
 *
 * Coordina dos datasources: [SyncLocalDataSource] (Room) y [SyncRemoteDataSource]
 * (Retrofit). Convierte DTO ↔ entidad y decide qué registros quedan sincronizados.
 */
@Singleton
class SyncRepository @Inject constructor(
    private val remote: SyncRemoteDataSource,
    private val local: SyncLocalDataSource
) {

    /** Contadores reactivos para que la UI muestre los pendientes. */
    val asistenciasPendientes: Flow<Int> = local.contarAsistenciasPendientes()
    val encuestasPendientes: Flow<Int> = local.contarEncuestasPendientes()

    /** Descarga el catálogo del backend y lo guarda en Room para trabajar sin conexión. */
    suspend fun descargarYGuardarCatalogo(): Result<Unit> {
        return try {
            val response = remote.obtenerCatalogo()

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

            local.guardarCatalogo(proyectos, eventos, participantes, inscripciones)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sincronizarAsistenciasPendientes(): Result<ResultadoSyncResponse?> {
        return try {
            val pendientes = local.asistenciasPendientes()
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

            val resultado = remote.enviarAsistencias(dtos)

            // Aceptados y duplicados ya están en el servidor: se marcan para no reenviarlos.
            local.marcarAsistenciasSincronizadas(resultado.aceptados + resultado.duplicados)

            Result.success(resultado)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sincronizarEncuestasPendientes(): Result<ResultadoSyncResponse?> {
        return try {
            val pendientes = local.encuestasPendientes()
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

            val resultado = remote.enviarEncuestas(dtos)

            local.marcarEncuestasSincronizadas(resultado.aceptados + resultado.duplicados)

            Result.success(resultado)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sube todos los pendientes (asistencias y encuestas) y devuelve el resumen.
     *
     * Si no hay pendientes no se hace ninguna llamada HTTP. Los registros
     * aceptados y duplicados se marcan como sincronizados dentro de los métodos
     * específicos; los rechazados y los que fallan por red quedan pendientes
     * para el siguiente intento. Si una de las dos subidas falla, se propaga el
     * error aunque la otra haya podido completarse.
     */
    suspend fun sincronizarPendientes(): Result<ResumenSync?> {
        return try {
            val asistencias = sincronizarAsistenciasPendientes()
            val encuestas = sincronizarEncuestasPendientes()

            val error = asistencias.exceptionOrNull() ?: encuestas.exceptionOrNull()
            if (error != null) {
                Result.failure(error)
            } else {
                val a = asistencias.getOrNull()
                val e = encuestas.getOrNull()
                if (a == null && e == null) {
                    // No había nada pendiente en ninguna de las dos tablas.
                    Result.success(null)
                } else {
                    Result.success(
                        ResumenSync(
                            aceptados = (a?.aceptados?.size ?: 0) + (e?.aceptados?.size ?: 0),
                            duplicados = (a?.duplicados?.size ?: 0) + (e?.duplicados?.size ?: 0),
                            rechazados = (a?.rechazados?.size ?: 0) + (e?.rechazados?.size ?: 0)
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Guarda una asistencia en Room con `sincronizado = false` (funciona sin red). */
    suspend fun registrarAsistenciaLocal(asistencia: AsistenciaEntity) {
        local.guardarAsistencia(asistencia)
    }

    /** Guarda una encuesta en Room con `sincronizado = false` (funciona sin red). */
    suspend fun registrarEncuestaLocal(encuesta: EncuestaEntity) {
        local.guardarEncuesta(encuesta)
    }

    /** Lectura del catálogo ya descargado (offline). */
    fun obtenerEventosLocales(): Flow<List<EventoEntity>> = local.eventos()

    fun obtenerParticipantesLocales(): Flow<List<ParticipanteEntity>> = local.participantes()

    /** Cursos (proyectos) remotos descargados desde el backend. */
    fun obtenerCursosRemotos(): Flow<List<ProyectoEntity>> = local.proyectos()
}
