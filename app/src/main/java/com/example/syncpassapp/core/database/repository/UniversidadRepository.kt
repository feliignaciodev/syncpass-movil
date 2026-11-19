package com.example.syncpassapp.core.database.repository

import com.example.syncpassapp.core.database.local.entities.AsistenciaClaseEntity
import com.example.syncpassapp.core.database.local.entities.ClaseEntity
import com.example.syncpassapp.core.database.local.entities.RolEntity
import com.example.syncpassapp.core.database.local.entities.UsuarioEntity
import com.example.syncpassapp.core.database.local.model.ClaseConDetalles
import com.example.syncpassapp.core.database.local.model.RolConUsuarios
import com.example.syncpassapp.core.database.local.model.UsuarioConRoles
import com.example.syncpassapp.core.datasource.local.UniversidadLocalDataSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio del modelo de clases (local): reglas de negocio de inscripción,
 * asistencia y cuestionario. Accede a Room solo a través de [UniversidadLocalDataSource].
 */
@Singleton
class UniversidadRepository @Inject constructor(
    private val local: UniversidadLocalDataSource
) {
    val todosLosUsuarios: Flow<List<UsuarioEntity>> = local.usuarios()
    val todosLosRoles: Flow<List<RolEntity>> = local.roles()
    val todasLasClases: Flow<List<ClaseEntity>> = local.clases()

    // --- GESTIÓN DE USUARIOS Y ROLES ---

    suspend fun crearUsuario(usuario: UsuarioEntity): Long {
        return local.insertarUsuario(usuario)
    }

    suspend fun crearRol(rol: RolEntity): Long {
        return local.insertarRol(rol)
    }

    suspend fun asignarRolAUsuario(usuarioId: Long, rolId: Long) {
        local.asignarRol(usuarioId, rolId)
    }

    fun obtenerUsuarioConRoles(usuarioId: Long): Flow<UsuarioConRoles?> {
        return local.usuarioConRoles(usuarioId)
    }

    suspend fun buscarUsuarioPorEmail(email: String): UsuarioConRoles? {
        return local.usuarioConRolesPorEmail(email)
    }

    fun obtenerRolConUsuarios(rolId: Long): Flow<RolConUsuarios?> {
        return local.rolConUsuarios(rolId)
    }

    suspend fun obtenerRolIdPorNombre(nombreRol: String): Long? {
        return local.rolIdPorNombre(nombreRol)
    }

    suspend fun obtenerTodasLasClases(): List<ClaseEntity> {
        return local.listaClases()
    }

    // --- GESTIÓN DE CLASES, ASISTENCIA Y CUESTIONARIOS ---

    suspend fun crearClase(clase: ClaseEntity): Long {
        return local.insertarClase(clase)
    }

    suspend fun actualizarClase(clase: ClaseEntity) {
        local.actualizarClase(clase)
    }

    fun obtenerClasesPorProfesor(profesorId: Long): Flow<List<ClaseEntity>> {
        return local.clasesPorProfesor(profesorId)
    }

    fun obtenerClasesPorEstudiante(estudianteId: Long): Flow<List<ClaseEntity>> {
        return local.clasesPorEstudiante(estudianteId)
    }

    fun obtenerAsistenciaEstudiante(estudianteId: Long, claseId: Long): Flow<AsistenciaClaseEntity?> {
        return local.asistenciaEstudiante(estudianteId, claseId)
    }

    fun obtenerClaseConDetalles(claseId: Long): Flow<ClaseConDetalles?> {
        return local.claseConDetalles(claseId)
    }

    suspend fun obtenerClasePorId(claseId: Long): ClaseEntity? {
        return local.clasePorId(claseId)
    }

    /**
     * Inscribe a un usuario en una clase como estudiante.
     * REGLA DE NEGOCIO: Un usuario no puede ser estudiante y profesor de la misma clase al mismo tiempo.
     */
    suspend fun inscribirEstudianteEnClase(usuarioId: Long, claseId: Long) {
        val clase = local.clasePorId(claseId)
            ?: throw IllegalArgumentException("La clase con ID $claseId no existe.")

        if (clase.profesorId == usuarioId) {
            throw IllegalArgumentException("El usuario $usuarioId es el profesor de esta clase y no puede inscribirse como estudiante.")
        }

        val asistenciaInicial = AsistenciaClaseEntity(
            usuarioId = usuarioId,
            claseId = claseId,
            esPresente = false,
            respuestasCuestionarioJson = null
        )
        local.guardarAsistencia(asistenciaInicial)
    }

    /**
     * Registra la asistencia y respuesta de cuestionario.
     * REGLA DE NEGOCIO: Solo si el estudiante está presente puede responder el cuestionario.
     */
    suspend fun registrarAsistenciaYCuestionario(
        usuarioId: Long,
        claseId: Long,
        esPresente: Boolean,
        respuestasJson: String?
    ) {
        val respuestasFinales = if (esPresente) respuestasJson else null

        val asistencia = AsistenciaClaseEntity(
            usuarioId = usuarioId,
            claseId = claseId,
            esPresente = esPresente,
            respuestasCuestionarioJson = respuestasFinales
        )
        local.guardarAsistencia(asistencia)
    }

    fun obtenerEstudiantesPresentes(claseId: Long): Flow<List<AsistenciaClaseEntity>> {
        return local.estudiantesPresentes(claseId)
    }

    suspend fun eliminarUsuario(usuario: UsuarioEntity) {
        local.eliminarUsuario(usuario)
    }

    suspend fun eliminarClase(clase: ClaseEntity) {
        local.eliminarClase(clase)
    }
}
