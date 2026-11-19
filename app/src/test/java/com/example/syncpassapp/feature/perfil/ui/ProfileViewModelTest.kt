package com.example.syncpassapp.feature.perfil.ui

import androidx.lifecycle.SavedStateHandle
import com.example.syncpassapp.core.common.auth.Rol
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ProfileViewModelTest {

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

    private fun crearViewModel(argumentos: Map<String, Any?>) = ProfileViewModel(
        UniversidadRepository(UniversidadLocalDataSource(dao)),
        AuthRepository(AuthRemoteDataSource(FakeAuthApi()), sesion),
        notificaciones,
        SavedStateHandle(argumentos)
    )

    @Test
    fun `muestra el usuario local y su rol de la rubrica segun el token`() = runTest {
        val id = dao.insertUsuario(UsuarioEntity(nombre = "Vale", email = "vol@x.cl"))
        sesion.usuario = usuarioDePrueba("VOLUNTARIO", email = "vol@x.cl")
        val viewModel = crearViewModel(mapOf("usuarioId" to id))

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }

        val estado = viewModel.uiState.value
        assertEquals(id, estado.usuarioId)
        assertEquals("Vale", estado.usuarioConRoles?.usuario?.nombre)
        assertEquals(Rol.VOLUNTARIO, estado.rol)
    }

    @Test
    fun `acepta el usuarioId como texto (argumento de navegacion)`() = runTest {
        val viewModel = crearViewModel(mapOf("usuarioId" to "42"))

        assertEquals(42L, viewModel.usuarioId)
        assertNull(viewModel.uiState.value.rol)
    }

    @Test(expected = IllegalStateException::class)
    fun `sin usuarioId no se puede abrir el perfil`() {
        crearViewModel(emptyMap())
    }

    @Test
    fun `cerrar sesion apaga las notificaciones y borra el token`() = runTest {
        sesion.guardarToken("jwt")
        val viewModel = crearViewModel(mapOf("usuarioId" to 1L))

        viewModel.logout()

        assertEquals(1, notificaciones.desactivaciones)
        assertTrue(sesion.borrada)
    }
}
