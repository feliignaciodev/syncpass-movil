package com.example.syncpassapp.core.common.auth

import com.example.syncpassapp.util.usuarioDePrueba
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RolTest {

    @Test
    fun `cada grupo de Cognito se traduce a su perfil de la rubrica`() {
        assertEquals(setOf(Rol.ESTUDIANTE), Rol.desdeGrupos(listOf("BENEFICIARIO")))
        assertEquals(setOf(Rol.DOCENTE), Rol.desdeGrupos(listOf("COORDINADOR")))
        assertEquals(setOf(Rol.VOLUNTARIO), Rol.desdeGrupos(listOf("VOLUNTARIO")))
        assertEquals(setOf(Rol.ADMINISTRADOR), Rol.desdeGrupos(listOf("ADMINISTRADOR")))
    }

    @Test
    fun `acepta minusculas, espacios y los nombres antiguos de la app`() {
        assertEquals(setOf(Rol.DOCENTE), Rol.desdeGrupos(listOf(" profesor ")))
        assertEquals(setOf(Rol.ESTUDIANTE), Rol.desdeGrupos(listOf("estudiante")))
        assertEquals(setOf(Rol.ADMINISTRADOR), Rol.desdeGrupos(listOf("admin")))
    }

    @Test
    fun `ignora grupos desconocidos`() {
        assertTrue(Rol.desdeGrupos(listOf("INVITADO", "")).isEmpty())
    }

    @Test
    fun `con varios grupos el principal respeta la prioridad Admin, Docente, Voluntario, Estudiante`() {
        assertEquals(Rol.ADMINISTRADOR, Rol.principal(Rol.desdeGrupos(listOf("BENEFICIARIO", "ADMINISTRADOR"))))
        assertEquals(Rol.DOCENTE, Rol.principal(Rol.desdeGrupos(listOf("VOLUNTARIO", "COORDINADOR"))))
        assertEquals(Rol.VOLUNTARIO, Rol.principal(Rol.desdeGrupos(listOf("BENEFICIARIO", "VOLUNTARIO"))))
    }

    @Test
    fun `sin grupos reconocidos el principal es Estudiante (rol del auto-registro)`() {
        assertEquals(Rol.ESTUDIANTE, Rol.principal(emptySet()))
    }

    @Test
    fun `solo Estudiante no es personal`() {
        assertFalse(Rol.ESTUDIANTE.esPersonal)
        assertTrue(Rol.DOCENTE.esPersonal)
        assertTrue(Rol.VOLUNTARIO.esPersonal)
        assertTrue(Rol.ADMINISTRADOR.esPersonal)
    }

    @Test
    fun `etiquetas usan los nombres de la rubrica`() {
        assertEquals(
            listOf("Estudiante", "Docente", "Voluntario", "Administrador"),
            Rol.entries.map { it.etiqueta }
        )
    }

    @Test
    fun `un Voluntario entra al inicio docente y no como estudiante`() {
        val voluntario = usuarioDePrueba("VOLUNTARIO")

        assertEquals(Rol.VOLUNTARIO, voluntario.rolPrincipal)
        assertTrue(voluntario.esPersonal)
    }

    @Test
    fun `UserInfo de un beneficiario es Estudiante y no es personal`() {
        val estudiante = usuarioDePrueba("BENEFICIARIO")

        assertEquals(setOf(Rol.ESTUDIANTE), estudiante.roles)
        assertFalse(estudiante.esPersonal)
    }
}
