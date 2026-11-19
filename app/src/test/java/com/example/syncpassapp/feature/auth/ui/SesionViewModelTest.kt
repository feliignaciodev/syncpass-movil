package com.example.syncpassapp.feature.auth.ui

import com.example.syncpassapp.core.database.local.entities.UsuarioEntity
import com.example.syncpassapp.core.database.repository.UniversidadRepository
import com.example.syncpassapp.core.datasource.local.UniversidadLocalDataSource
import com.example.syncpassapp.core.datasource.remote.AuthRemoteDataSource
import com.example.syncpassapp.feature.auth.data.AuthRepository
import com.example.syncpassapp.util.FakeAuthApi
import com.example.syncpassapp.util.FakeNotificacionesRepository
import com.example.syncpassapp.util.FakeSesionLocalDataSource
import com.example.syncpassapp.util.FakeUniversidadDao
import com.example.syncpassapp.util.MainDispatcherRule
import com.example.syncpassapp.util.usuarioDePrueba
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SesionViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var sesion: FakeSesionLocalDataSource
    private lateinit var dao: FakeUniversidadDao
    private lateinit var notificaciones: FakeNotificacionesRepository

    @Before
    fun setUp() {
        sesion = FakeSesionLocalDataSource()
        dao = FakeUniversidadDao()
        notificaciones = FakeNotificacionesRepository()
    }

    /** El ViewModel restaura la sesión en su `init`, así que se crea después de preparar los fakes. */
    private fun restaurar(): SesionRestaurada = SesionViewModel(
        AuthRepository(AuthRemoteDataSource(FakeAuthApi()), sesion),
        UniversidadRepository(UniversidadLocalDataSource(dao)),
        notificaciones
    ).sesion.value

    private suspend fun usuarioLocal(email: String): Long =
        dao.insertUsuario(UsuarioEntity(nombre = "Ana", email = email))

    @Test
    fun `sin sesion guardada pide login y apaga las notificaciones`() = runTest {
        assertEquals(SesionRestaurada.SinSesion, restaurar())
        assertEquals(1, notificaciones.desactivaciones)
    }

    @Test
    fun `token expirado pide login`() = runTest {
        usuarioLocal("ana@x.cl")
        sesion.usuario = usuarioDePrueba("BENEFICIARIO", email = "ana@x.cl")
        sesion.expirado = true

        assertEquals(SesionRestaurada.SinSesion, restaurar())
        assertTrue(notificaciones.activadasPara.isEmpty())
    }

    @Test
    fun `usuario sin registro local pide login`() = runTest {
        sesion.usuario = usuarioDePrueba("BENEFICIARIO", email = "otro@x.cl")

        assertEquals(SesionRestaurada.SinSesion, restaurar())
    }

    @Test
    fun `Voluntario con sesion valida vuelve al inicio docente`() = runTest {
        val id = usuarioLocal("vol@x.cl")
        sesion.usuario = usuarioDePrueba("VOLUNTARIO", email = "vol@x.cl")

        assertEquals(SesionRestaurada.ConSesion(usuarioId = id, esEstudiante = false), restaurar())
        assertEquals(listOf<String?>("vol@x.cl"), notificaciones.activadasPara)
    }

    @Test
    fun `Estudiante con sesion valida vuelve al inicio de estudiante`() = runTest {
        val id = usuarioLocal("est@x.cl")
        sesion.usuario = usuarioDePrueba("BENEFICIARIO", email = "est@x.cl")

        assertEquals(SesionRestaurada.ConSesion(usuarioId = id, esEstudiante = true), restaurar())
    }
}
