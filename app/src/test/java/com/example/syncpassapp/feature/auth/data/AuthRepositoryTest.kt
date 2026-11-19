package com.example.syncpassapp.feature.auth.data

import com.example.syncpassapp.core.common.auth.Rol
import com.example.syncpassapp.core.datasource.remote.AuthRemoteDataSource
import com.example.syncpassapp.core.network.remote.model.LoginResponse
import com.example.syncpassapp.core.network.remote.model.RegistroRequest
import com.example.syncpassapp.core.network.remote.model.UsuarioDto
import com.example.syncpassapp.util.FakeAuthApi
import com.example.syncpassapp.util.FakeSesionLocalDataSource
import com.example.syncpassapp.util.usuarioDePrueba
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

class AuthRepositoryTest {

    private lateinit var api: FakeAuthApi
    private lateinit var sesion: FakeSesionLocalDataSource
    private lateinit var repository: AuthRepository

    @Before
    fun setUp() {
        api = FakeAuthApi()
        sesion = FakeSesionLocalDataSource()
        repository = AuthRepository(AuthRemoteDataSource(api), sesion)
    }

    @Test
    fun `login exitoso guarda el token y usa el rol que informa el backend`() = runTest {
        api.respuestaLogin = LoginResponse(
            token = "jwt-123",
            usuario = UsuarioDto(id = 7L, username = "ana", nombre = "Ana Pérez", rol = "VOLUNTARIO")
        )
        sesion.usuario = usuarioDePrueba("BENEFICIARIO", email = "ana@x.cl")

        val resultado = repository.login("ana@x.cl", "secreta").getOrThrow()

        assertEquals("jwt-123", sesion.token)
        assertEquals("jwt-123", resultado.accessToken)
        assertEquals("7", resultado.userInfo.userId)
        assertEquals("Ana Pérez", resultado.userInfo.name)
        assertEquals(Rol.VOLUNTARIO, resultado.userInfo.rolPrincipal)
        assertEquals("ana@x.cl", api.logins.single().username)
    }

    @Test
    fun `sin usuario en la respuesta se usan los grupos del token`() = runTest {
        api.respuestaLogin = LoginResponse(token = "jwt", usuario = null)
        sesion.usuario = usuarioDePrueba("COORDINADOR", email = "doc@x.cl")

        val info = repository.login("doc@x.cl", "x").getOrThrow().userInfo

        assertEquals(Rol.DOCENTE, info.rolPrincipal)
        assertEquals("doc@x.cl", info.email)
    }

    @Test
    fun `sin usuario ni token decodificable queda como Estudiante con el correo ingresado`() = runTest {
        api.respuestaLogin = LoginResponse(token = "jwt", usuario = null)

        val info = repository.login("nuevo@x.cl", "x").getOrThrow().userInfo

        assertEquals(listOf("BENEFICIARIO"), info.groups)
        assertEquals("nuevo@x.cl", info.email)
        assertEquals("nuevo@x.cl", info.name)
    }

    @Test
    fun `token vacio es un error y no se guarda nada`() = runTest {
        api.respuestaLogin = LoginResponse(token = " ", usuario = null)

        val resultado = repository.login("a@x.cl", "x")

        assertTrue(resultado.isFailure)
        assertNull(sesion.token)
    }

    @Test
    fun `error de red en el login se devuelve como fallo`() = runTest {
        api.error = IOException("sin red")

        val resultado = repository.login("a@x.cl", "x")

        assertEquals("sin red", resultado.exceptionOrNull()?.message)
    }

    @Test
    fun `registro, confirmacion y reenvio delegan en el backend`() = runTest {
        val registrado = repository.registrar(
            RegistroRequest(email = "ana@x.cl", password = "Clave#2026", nombre = "Ana", telefono = "+56911112222")
        )
        val confirmado = repository.confirmarRegistro("ana@x.cl", "123456")
        val reenviado = repository.reenviarCodigo("ana@x.cl")

        assertEquals("ana@x.cl", registrado.getOrThrow().email)
        assertTrue(confirmado.isSuccess)
        assertTrue(reenviado.isSuccess)
        assertEquals("123456", api.confirmaciones.single().codigo)
        assertEquals("ana@x.cl", api.reenvios.single().email)
    }

    @Test
    fun `registro, confirmacion y reenvio propagan el error del backend`() = runTest {
        api.error = IllegalStateException("400")

        assertTrue(repository.registrar(RegistroRequest("a@x.cl", "x", "A", "+569")).isFailure)
        assertTrue(repository.confirmarRegistro("a@x.cl", "1").isFailure)
        assertTrue(repository.reenviarCodigo("a@x.cl").isFailure)
    }

    @Test
    fun `logout borra la sesion y la sesion expone token, usuario y expiracion`() = runTest {
        sesion.guardarToken("jwt")
        sesion.usuario = usuarioDePrueba("ADMINISTRADOR")
        sesion.expirado = true

        assertEquals("jwt", repository.getAccessToken())
        assertEquals(Rol.ADMINISTRADOR, repository.getUserInfo()?.rolPrincipal)
        assertTrue(repository.isTokenExpired())

        assertTrue(repository.logout())
        assertTrue(sesion.borrada)
        assertNull(repository.getAccessToken())
        assertFalse(sesion.token != null)
    }
}
