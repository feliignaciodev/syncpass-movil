package com.example.syncpassapp.core.common.location

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import kotlin.math.*

/**
 * Coordenada predeterminada (Santiago, Chile) usada cuando no hay GPS ni permiso.
 */
object CoordenadasDefault {
    const val LATITUD = -33.4489
    const val LONGITUD = -70.6693
    const val NOMBRE = "Santiago (coordenada predeterminada)"
}

/**
 * Helper para calcular distancias y estados de validación de asistencia.
 * No requiere Play Services: usa una coordenada predeterminada (Santiago).
 */
object LocationHelper {

    /** Verifica si el permiso de ubicación está concedido. */
    fun tienePermiso(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    /** Devuelve la coordenada predeterminada (Santiago). */
    fun obtenerUbicacion(): Pair<Double, Double> {
        return Pair(CoordenadasDefault.LATITUD, CoordenadasDefault.LONGITUD)
    }

    /** Distancia en metros entre dos coordenadas (fórmula de Haversine). */
    fun distanciaMetros(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371000.0 // radio de la Tierra en metros
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    /** Determina el estado de validación según la distancia y el radio permitido. */
    fun estadoValidacion(distancia: Double, radioMetros: Int): String {
        return if (distancia <= radioMetros) "VALIDO" else "FUERA_DE_RADIO"
    }
}
