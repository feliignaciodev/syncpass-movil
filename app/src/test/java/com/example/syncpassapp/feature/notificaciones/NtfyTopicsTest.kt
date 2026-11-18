package com.example.syncpassapp.feature.notificaciones

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NtfyTopicsTest {

    @Test
    fun `topico conocido coincide con el backend`() {
        assertEquals("app_vibe_8e63635b0c5401804b4b", NtfyTopics.generate("docente.dev@gmail.com"))
    }

    @Test
    fun `normaliza mayusculas y espacios`() {
        assertEquals(
            NtfyTopics.generate("user@example.com"),
            NtfyTopics.generate("  USER@Example.COM  ")
        )
    }

    @Test
    fun `topico es determinista`() {
        val correo = "estudiante@ejemplo.cl"
        assertEquals(NtfyTopics.generate(correo), NtfyTopics.generate(correo))
    }

    @Test
    fun `topico tiene prefijo y longitud fija`() {
        val topico = NtfyTopics.generate("docente.dev@gmail.com")
        assertTrue(topico.startsWith("app_vibe_"))
        assertEquals("app_vibe_".length + 20, topico.length)
        assertTrue(topico.removePrefix("app_vibe_").all { it in '0'..'9' || it in 'a'..'f' })
    }
}
