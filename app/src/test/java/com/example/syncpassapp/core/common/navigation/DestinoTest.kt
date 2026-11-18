package com.example.syncpassapp.core.common.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DestinoTest {

    @Test
    fun `patron y ruta concreta de InicioProfesor`() {
        assertEquals("teacher_home/{profesorId}", Destino.InicioProfesor.patron)
        assertEquals("teacher_home/42", Destino.InicioProfesor.crearRuta(42L))
    }

    @Test
    fun `construirRuta rechaza cantidad incorrecta de argumentos`() {
        var lanzada = false
        try {
            Destino.InicioProfesor.construirRuta(1L, 2L)
        } catch (e: IllegalArgumentException) {
            lanzada = true
        }
        assertTrue(lanzada)
    }

    @Test
    fun `coincideCon reconoce base y rutas hijas`() {
        assertTrue(Destino.InicioProfesor.coincideCon("teacher_home"))
        assertTrue(Destino.InicioProfesor.coincideCon("teacher_home/42"))
        assertFalse(Destino.InicioProfesor.coincideCon("session/42"))
        assertFalse(Destino.InicioProfesor.coincideCon(null))
    }

    @Test
    fun `CuestionarioEstudiante construye ruta con dos argumentos en orden`() {
        assertEquals(
            "student_questionnaire/7/9",
            Destino.CuestionarioEstudiante.crearRuta(7L, 9L)
        )
    }

    @Test
    fun `ArgumentoRuta definicion usa el tipo indicado`() {
        assertEquals("profesorId", Destino.InicioProfesor.argumentos.single().nombre)
    }
}
