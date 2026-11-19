package com.example.syncpassapp.feature.auth.ui

import com.example.syncpassapp.core.database.local.entities.RolEntity
import com.example.syncpassapp.core.database.local.entities.UsuarioEntity
import com.example.syncpassapp.core.database.local.entities.UsuarioRolCrossRef
import com.example.syncpassapp.core.database.repository.UniversidadRepository
import com.example.syncpassapp.core.datasource.local.UniversidadLocalDataSource
import com.example.syncpassapp.core.datasource.remote.AuthRemoteDataSource
import com.example.syncpassapp.core.network.remote.model.LoginResponse
import com.example.syncpassapp.core.network.remote.model.UsuarioDto
import com.example.syncpassapp.feature.auth.data.AuthRepository
import com.example.syncpassapp.util.FakeAuthApi
import com.example.syncpassapp.util.FakeNotificacionesRepository
import com.example.syncpassapp.util.FakeSesionLocalDataSource
import com.example.syncpassapp.util.FakeUniversidadDao
import com.example.syncpassapp.util.MainDispatcherRule
import com.example.syncpassapp.util.usuarioDePrueba
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var api: FakeAuthApi
    private lateinit var sesion: FakeSesionLocalDataSource
    private lateinit var dao: FakeUniversidadDao
    private lateinit var notificaciones: FakeNotificacionesRepository
    private lateinit var viewModel: LoginViewModel

    private val inicioDocente = mutableListOf<Long>()
    private val inicioEstudiante = mutableListOf<Long>()

    @Before
    fun setUp() {
        api = FakeAuthApi()
        sesion = FakeSesionLocalDataSource()
        dao = FakeUniversidadDao()
        notificaciones = FakeNotificacionesRepository()
        viewModel = LoginViewModel(
            AuthRepository(AuthRemoteDataSource(api), sesion),
            UniversidadRepository(UniversidadLocalDataSource(dao)),
            notificaciones
        )
    }

    private fun ingresar(email: String, password: String) {
        viewModel.onEmailChanged(email)
        viewModel.onPasswordChanged(password)
    }

    private fun iniciarSesion() = viewModel.login({ inicioDocente += it }, { inicioEstudiante += it })

    private fun backendResponde(rol: String, email: String) {
        api.respuestaLogin = LoginResponse(
            token = "jwt",
            usuario = UsuarioDto(id = 10L, username = email, nombre = "Vale", rol = rol)
        )
        sesion.usuario = usuarioDePrueba(rol, email = email)
    }

    @Test
    fun `campos vacios muestran error y no llaman al backend`() = runTest {
        ingresar("  ", "")

        iniciarSesion()

        val estado = viewModel.uiState.value
        assertEquals("Correo y contraseña son obligatorios.", estado.errorMessage)
        assertFalse(estado.isLoading)
        assertTrue(api.logins.isEmpty())
    }

    @Test
    fun `error del backend se muestra y no navega`() = runTest {
        api.error = IOException("sin red")
        ingresar("ana@x.cl", "secreta")

        iniciarSesion()

        assertEquals("Error de autenticación: sin red", viewModel.uiState.value.errorMessage)
        assertTrue(inicioDocente.isEmpty() && inicioEstudiante.isEmpty())
        assertTrue(notificaciones.activadasPara.isEmpty())
    }

    @Test
    fun `un Voluntario entra al inicio docente y activa sus notificaciones`() = runTest {
        backendResponde("VOLUNTARIO", "vol@x.cl")
        ingresar(" vol@x.cl ", "secreta")

        iniciarSesion()

        assertEquals(1, inicioDocente.size)
        assertTrue(inicioEstudiante.isEmpty())
        assertEquals(listOf<String?>("vol@x.cl"), notificaciones.activadasPara)
        assertEquals("vol@x.cl", api.logins.single().username)
        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `primer login crea el usuario local y los datos demo`() = runTest {
        backendResponde("COORDINADOR", "doc@x.cl")
        ingresar("doc@x.cl", "secreta")

        iniciarSesion()

        val usuarioId = inicioDocente.single()
        assertEquals("doc@x.cl", dao.usuarios.getValue(usuarioId).email)
        assertEquals(1, dao.clases.size)
        assertEquals(usuarioId, dao.clases.values.single().profesorId)
        assertTrue(dao.roles.values.any { it.nombreRol == ROL_LOCAL_PROFESOR })
    }

    @Test
    fun `un Estudiante entra al inicio de estudiante`() = runTest {
        backendResponde("BENEFICIARIO", "est@x.cl")
        ingresar("est@x.cl", "secreta")

        iniciarSesion()

        assertEquals(1, inicioEstudiante.size)
        assertTrue(inicioDocente.isEmpty())
    }

    @Test
    fun `usuario ya registrado se reutiliza y recibe su rol local`() = runTest {
        val rolProfesorId = dao.insertRol(RolEntity(nombreRol = ROL_LOCAL_PROFESOR))
        val existenteId = dao.insertUsuario(UsuarioEntity(nombre = "Doc", email = "doc@x.cl"))
        backendResponde("COORDINADOR", "doc@x.cl")
        ingresar("doc@x.cl", "secreta")

        iniciarSesion()

        assertEquals(listOf(existenteId), inicioDocente)
        assertTrue(UsuarioRolCrossRef(existenteId, rolProfesorId) in dao.usuarioRoles)
        assertTrue("no crea datos demo para un usuario existente", dao.clases.isEmpty())
    }

    @Test
    fun `editar un campo limpia el error anterior`() = runTest {
        iniciarSesion()
        assertTrue(viewModel.uiState.value.errorMessage != null)

        viewModel.onEmailChanged("a@x.cl")

        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `los botones demo completan el formulario e inician sesion`() = runTest {
        backendResponde("COORDINADOR", "profesor@syncpass.com")

        viewModel.loginDemoProfesor({ inicioDocente += it }, { inicioEstudiante += it })
        viewModel.loginDemoEstudiante({ inicioDocente += it }, { inicioEstudiante += it })

        assertEquals(
            listOf("profesor@syncpass.com", "estudiante@syncpass.com"),
            api.logins.map { it.username }
        )
    }
}
