package com.example.syncpassapp.feature.sync.ui
import com.example.syncpassapp.core.datasource.local.SyncLocalDataSource
import com.example.syncpassapp.core.datasource.remote.SyncRemoteDataSource

import com.example.syncpassapp.core.database.local.entities.AsistenciaEntity
import com.example.syncpassapp.core.database.local.entities.EncuestaEntity
import com.example.syncpassapp.core.database.repository.SyncRepository
import com.example.syncpassapp.core.network.remote.model.ResultadoSyncResponse
import com.example.syncpassapp.util.FakeSyncApi
import com.example.syncpassapp.util.FakeSyncPassDao
import com.example.syncpassapp.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class SyncViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var dao: FakeSyncPassDao
    private lateinit var api: FakeSyncApi
    private lateinit var viewModel: SyncViewModel

    @Before
    fun setUp() {
        dao = FakeSyncPassDao()
        api = FakeSyncApi()
        viewModel = SyncViewModel(SyncRepository(SyncRemoteDataSource(api), SyncLocalDataSource(dao)))
    }

    /** `stateIn` solo produce valores mientras hay un suscriptor. */
    private fun TestScope.observarUiState() {
        backgroundScope.launch(mainDispatcherRule.testDispatcher) {
            viewModel.uiState.collect { }
        }
    }

    private fun asistencia(uuid: String) = AsistenciaEntity(
        uuid = uuid,
        eventoId = 1L,
        participanteId = "P-0001",
        tipoParticipante = "ESTUDIANTE",
        timestamp = 1_700_000_000_000L,
        latitud = null,
        longitud = null,
        distanciaMetros = null,
        estadoValidacion = "VALIDA",
        inscritoEnSitio = false,
        registradoPor = null,
        dispositivoId = null
    )

    private fun encuesta(uuid: String) = EncuestaEntity(
        uuid = uuid,
        eventoId = 1L,
        participanteId = "P-0001",
        satisfaccion = 5,
        utilidad = 5,
        comentario = null,
        fechaEnvio = 1_700_000_000_000L
    )

    @Test
    fun `expone los contadores de pendientes`() = runTest {
        observarUiState()

        assertEquals(0, viewModel.uiState.value.totalPendientes)
        dao.insertAsistencia(asistencia("a1"))
        dao.insertAsistencia(asistencia("a2"))
        dao.insertEncuesta(encuesta("e1"))

        assertEquals(2, viewModel.uiState.value.pendientesAsistencias)
        assertEquals(1, viewModel.uiState.value.pendientesEncuestas)
        assertEquals(3, viewModel.uiState.value.totalPendientes)
    }

    @Test
    fun `sincronizar muestra aceptados duplicados y rechazados`() = runTest {
        observarUiState()
        dao.insertAsistencia(asistencia("a1"))
        dao.insertEncuesta(encuesta("e1"))
        dao.insertEncuesta(encuesta("e2"))
        api.resultadoAsistencias = ResultadoSyncResponse(listOf("a1"), emptyList(), emptyList(), 1L)
        api.resultadoEncuestas = ResultadoSyncResponse(
            emptyList(),
            listOf("e1"),
            listOf(com.example.syncpassapp.core.network.remote.model.RechazoDto("e2", "Inválida")),
            1L
        )

        viewModel.sincronizarPendientes()

        val estado = viewModel.uiState.value
        assertFalse(estado.sincronizando)
        assertFalse(estado.esError)
        assertEquals(1, estado.aceptados)
        assertEquals(1, estado.duplicados)
        assertEquals(1, estado.rechazados)
        assertEquals(1, estado.pendientesEncuestas) // el rechazado sigue pendiente
        assertEquals(0, estado.pendientesAsistencias)
    }

    @Test
    fun `sin pendientes no llama a la API y lo indica`() = runTest {
        observarUiState()

        viewModel.sincronizarPendientes()

        assertEquals("No hay registros pendientes.", viewModel.uiState.value.mensaje)
        assertTrue(api.lotesAsistencias.isEmpty())
        assertTrue(api.lotesEncuestas.isEmpty())
    }

    @Test
    fun `registrar offline crea un pendiente con ids del catalogo y sin ubicacion`() = runTest {
        observarUiState()

        viewModel.registrarAsistenciaOffline(
            eventoId = 99L,
            participanteId = "P-0099",
            tipoParticipante = "ESTUDIANTE"
        )

        val registradas = dao.getAsistenciasPendientes()
        assertEquals(1, registradas.size)
        val registro = registradas.single()
        assertEquals(99L, registro.eventoId)
        assertEquals("P-0099", registro.participanteId)
        assertEquals("SIN_UBICACION", registro.estadoValidacion)
        assertFalse(registro.sincronizado)
        assertTrue(registro.uuid.isNotBlank())
        assertEquals(1, viewModel.uiState.value.totalPendientes)
    }

    @Test
    fun `descargar catalogo lo guarda localmente para el registro offline`() = runTest {
        observarUiState()

        viewModel.descargarCatalogo()

        assertFalse(viewModel.uiState.value.isDownloading)
        assertFalse(viewModel.uiState.value.esError)
        assertEquals("Catálogo descargado correctamente.", viewModel.uiState.value.mensaje)
        assertEquals(1, api.llamadasCatalogo)
    }

    @Test
    fun `error de red conserva pendientes y marca el estado de error`() = runTest {
        observarUiState()
        dao.insertAsistencia(asistencia("a1"))
        api.errorAsistencias = IOException("sin red")

        viewModel.sincronizarPendientes()

        val estado = viewModel.uiState.value
        assertFalse(estado.sincronizando)
        assertTrue(estado.esError)
        assertTrue(estado.mensaje!!.contains("conservan"))
        assertEquals(1, estado.pendientesAsistencias)
        assertEquals(listOf("a1"), dao.getAsistenciasPendientes().map { it.uuid })
    }
}
