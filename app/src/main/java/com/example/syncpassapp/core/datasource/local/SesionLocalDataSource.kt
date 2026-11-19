package com.example.syncpassapp.core.datasource.local

import com.example.syncpassapp.core.datastore.TokenDataStore
import com.example.syncpassapp.core.datastore.UserInfo
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Datasource local de la sesión: guarda el token que devuelve el login y lo
 * decodifica para saber quién es el usuario y su rol.
 *
 * Es una interfaz (a diferencia de los demás datasources, que envuelven DAOs o
 * APIs de Retrofit, que ya son interfaces) porque DataStore escribe en un archivo
 * del teléfono: en las pruebas JVM se reemplaza por una sesión en memoria.
 */
interface SesionLocalDataSource {
    /** El backend entrega un solo token (access token de Cognito); se guarda en los tres campos. */
    suspend fun guardarToken(token: String)

    suspend fun usuarioActual(): UserInfo?

    suspend fun accessToken(): String?

    suspend fun tokenExpirado(): Boolean

    suspend fun borrar()
}

/** Implementación real sobre DataStore (Preferences) vía [TokenDataStore]. */
@Singleton
class DataStoreSesionLocalDataSource @Inject constructor(
    private val tokenDataStore: TokenDataStore
) : SesionLocalDataSource {

    override suspend fun guardarToken(token: String) {
        tokenDataStore.saveTokens(accessToken = token, refreshToken = token, idToken = token)
    }

    override suspend fun usuarioActual(): UserInfo? = tokenDataStore.getUserInfo()

    override suspend fun accessToken(): String? = tokenDataStore.getAccessToken()

    override suspend fun tokenExpirado(): Boolean = tokenDataStore.isAccessTokenExpired()

    override suspend fun borrar() = tokenDataStore.clearTokens()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class SesionDataSourceModule {

    @Binds
    @Singleton
    abstract fun bindSesionLocalDataSource(impl: DataStoreSesionLocalDataSource): SesionLocalDataSource
}
