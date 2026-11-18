package com.example.syncpassapp.core.database.repository

import com.example.syncpassapp.core.database.local.entities.ClaseEntity
import com.example.syncpassapp.core.database.local.model.UsuarioConRoles
import com.example.syncpassapp.core.database.local.entities.UsuarioEntity
import com.example.syncpassapp.util.FakeUniversidadDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UniversidadRepositoryTest {

    private lateinit var dao: FakeUniversidadDao
    private lateinit var repository: UniversidadRepository

    @Before
    fun setUp() {
        dao = FakeUniversidadDao()
        repository = UniversidadRepository(dao)
    }

    private suspend fun crearClase(profesorId: Long = 10L): Long = repository.crearClase(
        ClaseEntity(
            nombre = "Clase de prueba",
            descripcion = "Descripción",
            profesorId = profesorId,
            cuestionarioEstructuraJson = "{}"
        )
    )

    @Test
    fun `el profesor no puede inscribirse como estudiante de su clase`() = runTest {
        val claseId = crearClase(profesorId = 10L)

        var error: Throwable? = null
        try {
            repository.inscribirEstudianteEnClase(10L, claseId)
        } catch (e: IllegalArgumentException) {
            error = e
        }

        assertNotNull(error)
        assertTrue(dao.asistencias.isEmpty())
    }

    @Test
    fun `inscribir estudiante crea asistencia inicial ausente sin respuestas`() = runTest {
        val claseId = crearClase()

        repository.inscribirEstudianteEnClase(usuarioId = 20L, claseId = claseId)

        val asistencia = dao.asistencias[20L to claseId]
        assertNotNull(asistencia)
        assertFalse(asistencia!!.esPresente)
        assertNull(asistencia.respuestasCuestionarioJson)
    }

    @Test
    fun `inscribir en clase inexistente falla`() = runTest {
        var error: Throwable? = null
        try {
            repository.inscribirEstudianteEnClase(20L, 999L)
        } catch (e: IllegalArgumentException) {
            error = e
        }

        assertNotNull(error)
        assertTrue(dao.asistencias.isEmpty())
    }

    @Test
    fun `registrar presente guarda las respuestas del cuestionario`() = runTest {
        val claseId = crearClase()

        repository.registrarAsistenciaYCuestionario(20L, claseId, true, "{\"r\":1}")

        val asistencia = dao.asistencias[20L to claseId]!!
        assertTrue(asistencia.esPresente)
        assertEquals("{\"r\":1}", asistencia.respuestasCuestionarioJson)
    }

    @Test
    fun `registrar ausente descarta las respuestas aunque vengan llenas`() = runTest {
        val claseId = crearClase()

        repository.registrarAsistenciaYCuestionario(20L, claseId, false, "{\"r\":1}")

        val asistencia = dao.asistencias[20L to claseId]!!
        assertFalse(asistencia.esPresente)
        assertNull(asistencia.respuestasCuestionarioJson)
    }

    @Test
    fun `crear clase asigna id y la expone en la lista`() = runTest {
        val id = crearClase()

        assertTrue(id > 0)
        assertEquals(1, dao.getAllClasesList().size)
        assertEquals("Clase de prueba", repository.obtenerClasePorId(id)?.nombre)
    }

    @Test
    fun `solo estudiantes presentes aparecen en el listado`() = runTest {
        val claseId = crearClase()
        repository.registrarAsistenciaYCuestionario(20L, claseId, true, null)
        repository.registrarAsistenciaYCuestionario(21L, claseId, false, null)

        val presentes = repository.obtenerEstudiantesPresentes(claseId).first()

        assertEquals(1, presentes.size)
        assertEquals(20L, presentes.single().usuarioId)
    }

    @Test
    fun `flujo publico expone los usuarios del dao`() = runTest {
        assertEquals(0, repository.todosLosUsuarios.first().size)
    }

    @Test
    fun `modelo UsuarioConRoles conserva usuario y roles`() {
        val usuario = UsuarioEntity(usuarioId = 1L, nombre = "Ana", email = "ana@x.cl")
        val info = UsuarioConRoles(usuario, emptyList())

        assertEquals("Ana", info.usuario.nombre)
        assertTrue(info.roles.isEmpty())
    }

    @Test
    fun `crear usuario delega en el dao y devuelve su id`() = runTest {
        val id = repository.crearUsuario(UsuarioEntity(usuarioId = 5L, nombre = "Bob", email = "bob@x.cl"))
        assertEquals(5L, id)
    }

    @Test
    fun `crear y actualizar clase cambia su nombre`() = runTest {
        val id = crearClase()
        val original = repository.obtenerClasePorId(id)!!
        repository.actualizarClase(original.copy(nombre = "Renombrada"))

        assertEquals("Renombrada", repository.obtenerClasePorId(id)?.nombre)
    }

    @Test
    fun `eliminar clase la quita de la lista`() = runTest {
        val id = crearClase()
        assertEquals(1, dao.getAllClasesList().size)

        repository.eliminarClase(dao.clases[id]!!)

        assertTrue(dao.getAllClasesList().isEmpty())
    }

    @Test
    fun `obtener todas las clases devuelve las creadas`() = runTest {
        crearClase()
        crearClase()

        assertEquals(2, repository.obtenerTodasLasClases().size)
    }

    @Test
    fun `obtener clases por profesor filtra por profesorId`() = runTest {
        crearClase(profesorId = 10L)
        crearClase(profesorId = 20L)

        val delProfesor = repository.obtenerClasesPorProfesor(10L).first()
        assertEquals(1, delProfesor.size)
    }

    @Test
    fun `obtener clases por estudiante incluye las que tiene asistencia`() = runTest {
        val claseId = crearClase()
        repository.inscribirEstudianteEnClase(20L, claseId)

        val clases = repository.obtenerClasesPorEstudiante(20L).first()
        assertEquals(1, clases.size)
    }

    @Test
    fun `obtener asistencia de estudiante devuelve el registro existente`() = runTest {
        val claseId = crearClase()
        repository.inscribirEstudianteEnClase(20L, claseId)

        val asistencia = repository.obtenerAsistenciaEstudiante(20L, claseId).first()
        assertNotNull(asistencia)
        assertEquals(20L, asistencia?.usuarioId)
    }

    @Test
    fun `obtener estudiantes presentes solo incluye presentes`() = runTest {
        val claseId = crearClase()
        repository.registrarAsistenciaYCuestionario(20L, claseId, true, null)
        repository.registrarAsistenciaYCuestionario(21L, claseId, false, null)

        val presentes = repository.obtenerEstudiantesPresentes(claseId).first()
        assertEquals(1, presentes.size)
    }

    @Test
    fun `eliminar usuario delega en el dao sin error`() = runTest {
        repository.eliminarUsuario(UsuarioEntity(usuarioId = 1L, nombre = "Ana", email = "ana@x.cl"))
        // Sin excepción es suficiente para el fake.
    }
}
