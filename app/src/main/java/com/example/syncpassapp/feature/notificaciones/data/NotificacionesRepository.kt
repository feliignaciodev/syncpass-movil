package com.example.syncpassapp.feature.notificaciones.data

import android.content.Context
import com.example.syncpassapp.feature.notificaciones.NotificationService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Suscripción del usuario a las notificaciones remotas (ntfy).
 *
 * Los ViewModels piden "activar para este usuario" o "desactivar" a este
 * repositorio; el detalle de Android (arrancar o detener el servicio en primer
 * plano, que necesita un Context) queda en la implementación. Así ningún
 * ViewModel recibe un Context y en las pruebas se usa un fake.
 */
interface NotificacionesRepository {
    /** Empieza a escuchar los temas del usuario (los suyos y los generales). */
    fun activarPara(email: String?)

    /** Deja de escuchar (al cerrar sesión o si la sesión ya no es válida). */
    fun desactivar()
}

@Singleton
class NotificacionesRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : NotificacionesRepository {

    override fun activarPara(email: String?) = NotificationService.start(context, email)

    override fun desactivar() = NotificationService.stop(context)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class NotificacionesModule {

    @Binds
    @Singleton
    abstract fun bindNotificacionesRepository(impl: NotificacionesRepositoryImpl): NotificacionesRepository
}
