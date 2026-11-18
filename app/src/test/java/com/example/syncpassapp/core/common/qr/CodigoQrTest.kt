package com.example.syncpassapp.core.common.qr

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CodigoQrTest {

    @Test
    fun `Clase genera y lee id valido`() {
        val contenido = CodigoQr.Clase.generar(42L)
        assertEquals("CLASE_42", contenido)
        assertEquals(42L, CodigoQr.Clase.leer(contenido))
    }

    @Test
    fun `Estudiante genera y lee id valido`() {
        val contenido = CodigoQr.Estudiante.generar(7L)
        assertEquals("ESTUDIANTE_7", contenido)
        assertEquals(7L, CodigoQr.Estudiante.leer(contenido))
    }

    @Test
    fun `prefijo incorrecto devuelve null`() {
        assertNull(CodigoQr.Clase.leer("ESTUDIANTE_42"))
        assertNull(CodigoQr.Estudiante.leer("CLASE_7"))
    }

    @Test
    fun `id invalido devuelve null`() {
        assertNull(CodigoQr.Clase.leer("CLASE_abc"))
        assertNull(CodigoQr.Clase.leer("CLASE_"))
        assertNull(CodigoQr.Clase.leer("   "))
    }

    @Test
    fun `trim de espacios`() {
        assertEquals(9L, CodigoQr.Clase.leer("  CLASE_9  "))
    }
}
