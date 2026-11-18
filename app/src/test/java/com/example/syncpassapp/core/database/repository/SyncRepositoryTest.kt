package com.example.syncpassapp.core.database.repository

import com.example.syncpassapp.core.database.local.database.SyncPassDao
import com.example.syncpassapp.core.database.local.entities.AsistenciaEntity
import com.example.syncpassapp.core.database.local.entities.EncuestaEntity
import com.example.syncpassapp.core.network.remote.SyncApi
import com.example.syncpassapp.core.network.remote.model.AsistenciaLoteRequest
import com.example.syncpassapp.core.network.remote.model.CatalogoResponse
import com.example.syncpassapp.core.network.remote.model.EncuestaLoteRequest
import com.example.syncpassapp.core.network.remote.model.ResultadoSyncResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SyncRepositoryTest {

    private class FakeDao : SyncPassDao {
        val asistencias = mutableListOf<AsistenciaEntity>()
        val encuestas = mutableListOf<EncuestaEntity>()
        private val pendientesFlow = MutableStateFlow(0)
        private val encuestasFlow = MutableStateFlow(0)

        fun refreshCounts() {
            pendientesFlow.value = asistencias.count { !it.sincronizado }
            encuestasFlow.value = encuestas.count { !it.sincronizado }
        }

        override suspend fun insertProyectos(proyectos: List<com.example.syncpassapp.core.database.local.entities.ProyectoEntity>) {}
        override fun getAllProyectos(): Flow<List<com.example.syncpassapp.core.database.local.entities.ProyectoEntity>> = MutableStateFlow(emptyList<com.example.syncpassapp.core.database.local.entities.ProyectoEntity>()).asStateFlow()
        override suspend fun insertEventos(eventos: List<com.example.syncpassapp.core.database.local.entities.EventoEntity>) {}
        override fun getAllEventos(): Flow<List<com.example.syncpassapp.core.database.local.entities.EventoEntity>> = MutableStateFlow(emptyList<com.example.syncpassapp.core.database.local.entities.EventoEntity>()).asStateFlow()
        override suspend fun getEventoById(eventoId: Long): com.example.syncpassapp.core.database.local.entities.EventoEntity? = null
        override suspend fun insertParticipantes(participantes: List<com.example.syncpassapp.core.database.local.entities.ParticipanteEntity>) {}
        override fun getAllParticipantes(): Flow<List<com.example.syncpassapp.core.database.local.entities.ParticipanteEntity>> = MutableStateFlow(emptyList<com.example.syncpassapp.core.database.local.entities.ParticipanteEntity>()).asStateFlow()
        override suspend fun getParticipanteById(participanteId: String): com.example.syncpassapp.core.database.local.entities.ParticipanteEntity? = null
        override suspend fun insertInscripciones(inscripciones: List<com.example.syncpassapp.core.database.local.entities.InscripcionEntity>) {}
        override fun getInscripcionesByEvento(eventoId: Long): Flow<List<com.example.syncpassapp.core.database.local.entities.InscripcionEntity>> = MutableStateFlow(emptyList<com.example.syncpassapp.core.database.local.entities.InscripcionEntity>()).asStateFlow()

        override suspend fun insertAsistencia(asistencia: AsistenciaEntity) {
            asistencias += asistencia
            refreshCounts()
        }

        override suspend fun getAsistenciasPendientes(): List<AsistenciaEntity> =
            asistencias.filter { !it.sincronizado }

        override fun contarAsistenciasPendientes(): Flow<Int> = pendientesFlow

        override suspend fun markAsistenciasAsSincronizadas(uuids: List<String>) {
            val set = uuids.toSet()
            for (i in asistencias.indices) {
                if (asistencias[i].uuid in set) {
                    asistencias[i] = asistencias[i].copy(sincronizado = true)
                }
            }
            refreshCounts()
        }

        override suspend fun insertEncuesta(encuesta: EncuestaEntity) {
            encuestas += encuesta
            refreshCounts()
        }

        override suspend fun getEncuestasPendientes(): List<EncuestaEntity> =
            encuestas.filter { !it.sincronizado }

        override fun contarEncuestasPendientes(): Flow<Int> = encuestasFlow

        override suspend fun markEncuestasAsSincronizadas(uuids: List<String>) {
            val set = uuids.toSet()
            for (i in encuestas.indices) {
                if (encuestas[i].uuid in set) {
                    encuestas[i] = encuestas[i].copy(sincronizado = true)
                }
            }
            refreshCounts()
        }
    }

    private class FakeSyncApi(
        var asistenciasResponse: ResultadoSyncResponse? = null,
        var asistenciasError: Throwable? = null,
        var encuestasResponse: ResultadoSyncResponse? = null,
        var encuestasError: Throwable? = null
    ) : SyncApi {
        var asistenciasCalls = 0
        var encuestasCalls = 0
        override suspend fun obtenerCatalogo(): CatalogoResponse =
            CatalogoResponse(0L, emptyList(), emptyList(), emptyList(), emptyList())

        override suspend fun sincronizarAsistencias(request: AsistenciaLoteRequest): ResultadoSyncResponse {
            asistenciasCalls++
            asistenciasError?.let { throw it }
            return asistenciasResponse!!
        }

        override suspend fun sincronizarEncuestas(request: EncuestaLoteRequest): ResultadoSyncResponse {
            encuestasCalls++
            encuestasError?.let { throw it }
            return encuestasResponse!!
        }
    }

    private fun nuevaAsistencia(uuid: String): AsistenciaEntity =
        AsistenciaEntity(
            uuid = uuid,
            eventoId = 1L,
            participanteId = "p-1",
            tipoParticipante = "ESTUDIANTE",
            timestamp = 1L,
            latitud = null,
            longitud = null,
            distanciaMetros = null,
            estadoValidacion = "VALIDO",
            inscritoEnSitio = false,
            registradoPor = null,
            dispositivoId = null,
            sincronizado = false
        )

    @Test
    fun `sin pendientes no hace llamada HTTP`() = runTest {
        val dao = FakeDao()
        val api = FakeSyncApi()
        val repository = SyncRepository(api, dao)

        val result = repository.sincronizarPendientes()

        assertTrue(result.isSuccess)
        assertEquals(null, result.getOrNull())
        assertEquals(0, api.asistenciasCalls)
        assertEquals(0, api.encuestasCalls)
    }

    @Test
    fun `asistencias aceptadas se marcan sincronizadas`() = runTest {
        val dao = FakeDao()
        dao.asistencias += nuevaAsistencia("uuid-1")
        dao.refreshCounts()
        val api = FakeSyncApi(
            asistenciasResponse = ResultadoSyncResponse(
                aceptados = listOf("uuid-1"),
                duplicados = emptyList(),
                rechazados = emptyList(),
                serverTime = 2L
            )
        )
        val repository = SyncRepository(api, dao)

        val result = repository.sincronizarPendientes()

        assertTrue(result.isSuccess)
        assertTrue(dao.asistencias.first().sincronizado)
    }

    @Test
    fun `error de red deja los registros pendientes`() = runTest {
        val dao = FakeDao()
        dao.asistencias += nuevaAsistencia("uuid-2")
        dao.refreshCounts()
        val api = FakeSyncApi(asistenciasError = RuntimeException("sin red"))
        val repository = SyncRepository(api, dao)

        val result = repository.sincronizarPendientes()

        assertTrue(result.isFailure)
        assertTrue(!dao.asistencias.first().sincronizado)
    }

    @Test
    fun `registrarAsistenciaLocal inserta pendiente`() = runTest {
        val dao = FakeDao()
        val api = FakeSyncApi()
        val repository = SyncRepository(api, dao)
        val asistencia = nuevaAsistencia("uuid-3")

        repository.registrarAsistenciaLocal(asistencia)

        assertEquals(1, dao.asistencias.size)
        assertTrue(!dao.asistencias.first().sincronizado)
    }
}
