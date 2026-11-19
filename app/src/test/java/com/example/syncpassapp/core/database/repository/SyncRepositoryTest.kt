package com.example.syncpassapp.core.database.repository
import com.example.syncpassapp.core.datasource.local.SyncLocalDataSource
import com.example.syncpassapp.core.datasource.remote.SyncRemoteDataSource

import com.example.syncpassapp.core.database.local.database.SyncPassDao
import com.example.syncpassapp.core.database.local.entities.AsistenciaEntity
import com.example.syncpassapp.core.database.local.entities.EncuestaEntity
import com.example.syncpassapp.core.network.remote.SyncApi
import com.example.syncpassapp.core.network.remote.model.ResultadoSyncResponse
import com.example.syncpassapp.util.FakeSyncApi
import com.example.syncpassapp.util.FakeSyncPassDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SyncRepositoryTest {

    private fun nuevaAsistencia(uuid: String): AsistenciaEntity =
        AsistenciaEntity(
            uuid = uuid,
            eventoId = 1L,
            participanteId = "P1",
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
        val dao = FakeSyncPassDao()
        val api = FakeSyncApi()
        val repository = SyncRepository(SyncRemoteDataSource(api), SyncLocalDataSource(dao))

        val result = repository.sincronizarPendientes()

        assertTrue(result.isSuccess)
        assertEquals(null, result.getOrNull())
        assertTrue(api.lotesAsistencias.isEmpty())
        assertTrue(api.lotesEncuestas.isEmpty())
    }

    @Test
    fun `asistencias aceptadas se marcan sincronizadas`() = runTest {
        val dao = FakeSyncPassDao()
        dao.insertAsistencia(nuevaAsistencia("uuid-1"))
        val api = FakeSyncApi()
        api.resultadoAsistencias = ResultadoSyncResponse(
            aceptados = listOf("uuid-1"), duplicados = emptyList(),
            rechazados = emptyList(), serverTime = 2L
        )
        val repository = SyncRepository(SyncRemoteDataSource(api), SyncLocalDataSource(dao))

        val result = repository.sincronizarPendientes()

        assertTrue(result.isSuccess)
        assertTrue(dao.getAsistenciasPendientes().isEmpty())
    }

    @Test
    fun `error de red deja los registros pendientes`() = runTest {
        val dao = FakeSyncPassDao()
        dao.insertAsistencia(nuevaAsistencia("uuid-2"))
        val api = FakeSyncApi()
        api.errorAsistencias = RuntimeException("sin red")
        val repository = SyncRepository(SyncRemoteDataSource(api), SyncLocalDataSource(dao))

        val result = repository.sincronizarPendientes()

        assertTrue(result.isFailure)
        assertEquals(1, dao.getAsistenciasPendientes().size)
    }

    @Test
    fun `registrarAsistenciaLocal inserta pendiente`() = runTest {
        val dao = FakeSyncPassDao()
        val api = FakeSyncApi()
        val repository = SyncRepository(SyncRemoteDataSource(api), SyncLocalDataSource(dao))
        val asistencia = nuevaAsistencia("uuid-3")

        repository.registrarAsistenciaLocal(asistencia)

        assertEquals(1, dao.getAsistenciasPendientes().size)
    }

    @Test
    fun `descargar catalogo persiste datos remotos`() = runTest {
        val dao = FakeSyncPassDao()
        val api = FakeSyncApi()
        api.catalogo = com.example.syncpassapp.core.network.remote.model.CatalogoResponse(
            serverTime = 5L,
            proyectos = listOf(com.example.syncpassapp.core.network.remote.model.ProyectoDto(
                id = 1L, titulo = "P", descripcion = "D", areaTematica = "A", unidadVecinal = "U",
                fechaInicio = "2025-01-01", fechaTermino = "2025-01-02", estado = "EN_CURSO"
            )),
            eventos = listOf(com.example.syncpassapp.core.network.remote.model.EventoDto(
                id = 10L, proyectoId = 1L, nombre = "E", fechaHora = 3L, cuposMaximos = 5,
                quorumMinimo = 1, recinto = "R", direccion = "D", latitud = 0.0, longitud = 0.0, radioMetros = 10
            )),
            participantes = listOf(com.example.syncpassapp.core.network.remote.model.ParticipanteDto(
                id = "P1", alias = "alias", tipo = "ESTUDIANTE"
            )),
            inscripciones = listOf(com.example.syncpassapp.core.network.remote.model.InscripcionDto(
                eventoId = 10L, participanteId = "P1", estado = "INSCRITO"
            ))
        )
        val repository = SyncRepository(SyncRemoteDataSource(api), SyncLocalDataSource(dao))

        val result = repository.descargarYGuardarCatalogo()

        assertTrue(result.isSuccess)
        assertEquals(1, dao.proyectosGuardados.size)
        assertEquals(1, dao.eventosGuardados.size)
        assertEquals(1, dao.participantesGuardados.size)
        assertEquals(1, dao.inscripcionesGuardadas.size)
        assertEquals(1, api.llamadasCatalogo)
    }

    @Test
    fun `encuestas duplicadas se marcan sincronizadas`() = runTest {
        val dao = FakeSyncPassDao()
        val encuesta = com.example.syncpassapp.core.database.local.entities.EncuestaEntity(
            uuid = "e-1", eventoId = 1L, participanteId = "P1", satisfaccion = 5, utilidad = 4,
            comentario = null, fechaEnvio = 1L, sincronizado = false
        )
        dao.insertEncuesta(encuesta)
        val api = FakeSyncApi()
        api.resultadoEncuestas = ResultadoSyncResponse(
            aceptados = emptyList(), duplicados = listOf("e-1"), rechazados = emptyList(), serverTime = 2L
        )
        val repository = SyncRepository(SyncRemoteDataSource(api), SyncLocalDataSource(dao))

        val result = repository.sincronizarPendientes()

        assertTrue(result.isSuccess)
        assertTrue(dao.getEncuestasPendientes().isEmpty())
    }
}
