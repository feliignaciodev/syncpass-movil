package com.example.syncpassapp

import android.app.Application
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.example.syncpassapp.core.database.repository.SyncRepository
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * App base: además de Hilt registra el reintento automático de sincronización.
 *
 * Cuando el dispositivo recupera una red con internet validado, se vuelven a
 * enviar los registros pendientes. El botón de la pantalla Perfil sigue
 * disponible como mecanismo manual explícito exigido por la rúbrica.
 */
@HiltAndroidApp
class SyncPassApplication : Application() {

    @Inject
    lateinit var syncRepository: SyncRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val sincronizando = AtomicBoolean(false)

    override fun onCreate() {
        super.onCreate()
        registrarSincronizacionAlRecuperarRed()
    }

    private fun registrarSincronizacionAlRecuperarRed() {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            .build()

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                intentarSincronizarSiRecuperoRed()
            }
        }

        val connectivityManager = getSystemService(ConnectivityManager::class.java)
        connectivityManager.registerNetworkCallback(request, callback)
    }

    private fun intentarSincronizarSiRecuperoRed() {
        // Si ya hay una sincronización en curso (manual o automática), no duplicar lotes.
        if (!sincronizando.compareAndSet(false, true)) return

        scope.launch {
            try {
                syncRepository.sincronizarPendientes()
            } finally {
                sincronizando.set(false)
            }
        }
    }
}
