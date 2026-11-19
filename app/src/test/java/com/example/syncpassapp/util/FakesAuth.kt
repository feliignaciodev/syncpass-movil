package com.example.syncpassapp.util

import com.example.syncpassapp.core.datasource.local.SesionLocalDataSource
import com.example.syncpassapp.core.datastore.UserInfo
import com.example.syncpassapp.core.network.remote.AuthApi
import com.example.syncpassapp.core.network.remote.model.ConfirmarRegistroRequest
import com.example.syncpassapp.core.network.remote.model.LoginRequest
import com.example.syncpassapp.core.network.remote.model.LoginResponse
import com.example.syncpassapp.core.network.remote.model.ReenviarCodigoRequest
import com.example.syncpassapp.core.network.remote.model.RegistroRequest
import com.example.syncpassapp.core.network.remote.model.UsuarioResponse
import com.example.syncpassapp.feature.notificaciones.data.NotificacionesRepository

/** API de autenticación en memoria: responde lo configurado o lanza el error indicado. */
class FakeAuthApi : AuthApi {
    var respuestaLogin: LoginResponse = LoginResponse(token = "token", usuario = null)
    var respuestaRegistro: UsuarioResponse = UsuarioResponse(
        id = 1L, username = "ana", email = "ana@x.cl", nombre = "Ana", rol = "BENEFICIARIO", activo = true
    )
    var error: Exception? = null

    val logins = mutableListOf<LoginRequest>()
    val confirmaciones = mutableListOf<ConfirmarRegistroRequest>()
    val reenvios = mutableListOf<ReenviarCodigoRequest>()

    override suspend fun login(request: LoginRequest): LoginResponse {
        logins += request
        error?.let { throw it }
        return respuestaLogin
    }

    override suspend fun registrar(request: RegistroRequest): UsuarioResponse {
        error?.let { throw it }
        return respuestaRegistro
    }

    override suspend fun confirmar(request: ConfirmarRegistroRequest) {
        error?.let { throw it }
        confirmaciones += request
    }

    override suspend fun reenviarCodigo(request: ReenviarCodigoRequest) {
        error?.let { throw it }
        reenvios += request
    }
}

/** Sesión en memoria: el "token" guardado se traduce al [UserInfo] que se configure. */
class FakeSesionLocalDataSource(
    var usuario: UserInfo? = null,
    var expirado: Boolean = false
) : SesionLocalDataSource {
    var token: String? = null
        private set
    var borrada = false
        private set

    override suspend fun guardarToken(token: String) {
        this.token = token
        borrada = false
    }

    override suspend fun usuarioActual(): UserInfo? = usuario

    override suspend fun accessToken(): String? = token

    override suspend fun tokenExpirado(): Boolean = expirado

    override suspend fun borrar() {
        token = null
        usuario = null
        borrada = true
    }
}

/** Registra si se activaron o desactivaron las notificaciones (sin servicio Android). */
class FakeNotificacionesRepository : NotificacionesRepository {
    val activadasPara = mutableListOf<String?>()
    var desactivaciones = 0
        private set

    override fun activarPara(email: String?) {
        activadasPara += email
    }

    override fun desactivar() {
        desactivaciones++
    }
}

/** UserInfo de prueba con los grupos de Cognito indicados. */
fun usuarioDePrueba(vararg grupos: String, email: String = "ana@x.cl") = UserInfo(
    userId = "sub-1",
    email = email,
    name = "Ana",
    phone = null,
    groups = grupos.toList()
)
