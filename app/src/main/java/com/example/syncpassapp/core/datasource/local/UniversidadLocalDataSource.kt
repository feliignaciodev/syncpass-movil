package com.example.syncpassapp.core.datasource.local

import com.example.syncpassapp.core.database.local.database.UniversidadDao
import com.example.syncpassapp.core.database.local.entities.AsistenciaClaseEntity
import com.example.syncpassapp.core.database.local.entities.ClaseEntity
import com.example.syncpassapp.core.database.local.entities.RolEntity
import com.example.syncpassapp.core.database.local.entities.UsuarioEntity
import com.example.syncpassapp.core.database.local.entities.UsuarioRolCrossRef
import com.example.syncpassapp.core.database.local.model.ClaseConDetalles
import com.example.syncpassapp.core.database.local.model.RolConUsuarios
import com.example.syncpassapp.core.database.local.model.UsuarioConRoles
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Datasource local del modelo de clases (Room): usuarios, roles locales, clases
 * y asistencias de clase. Es la única clase que usa [UniversidadDao]; las reglas
 * de negocio viven en [com.example.syncpassapp.core.database.repository.UniversidadRepository].
 *
 * Este modelo solo existe en el teléfono (no se sincroniza con el backend).
 */
@Singleton
class UniversidadLocalDataSource @Inject constructor(
    private val dao: UniversidadDao
) {

    // --- Usuarios y roles locales ---

    fun usuarios(): Flow<List<UsuarioEntity>> = dao.getAllUsuarios()

    fun roles(): Flow<List<RolEntity>> = dao.getAllRoles()

    suspend fun insertarUsuario(usuario: UsuarioEntity): Long = dao.insertUsuario(usuario)

    suspend fun insertarRol(rol: RolEntity): Long = dao.insertRol(rol)

    suspend fun asignarRol(usuarioId: Long, rolId: Long) =
        dao.insertUsuarioRolCrossRef(UsuarioRolCrossRef(usuarioId = usuarioId, rolId = rolId))

    fun usuarioConRoles(usuarioId: Long): Flow<UsuarioConRoles?> = dao.getUsuarioConRoles(usuarioId)

    suspend fun usuarioConRolesPorEmail(email: String): UsuarioConRoles? =
        dao.getUsuarioConRolesByEmail(email)

    fun rolConUsuarios(rolId: Long): Flow<RolConUsuarios?> = dao.getRolConUsuarios(rolId)

    suspend fun rolIdPorNombre(nombreRol: String): Long? = dao.getRolIdByNombre(nombreRol)

    suspend fun eliminarUsuario(usuario: UsuarioEntity) = dao.deleteUsuario(usuario)

    // --- Clases ---

    fun clases(): Flow<List<ClaseEntity>> = dao.getAllClases()

    suspend fun listaClases(): List<ClaseEntity> = dao.getAllClasesList()

    suspend fun insertarClase(clase: ClaseEntity): Long = dao.insertClase(clase)

    suspend fun actualizarClase(clase: ClaseEntity) = dao.updateClase(clase)

    suspend fun clasePorId(claseId: Long): ClaseEntity? = dao.getClaseById(claseId)

    fun clasesPorProfesor(profesorId: Long): Flow<List<ClaseEntity>> = dao.getClasesByProfesor(profesorId)

    fun clasesPorEstudiante(estudianteId: Long): Flow<List<ClaseEntity>> =
        dao.getClasesByEstudiante(estudianteId)

    fun claseConDetalles(claseId: Long): Flow<ClaseConDetalles?> = dao.getClaseConDetalles(claseId)

    suspend fun eliminarClase(clase: ClaseEntity) = dao.deleteClase(clase)

    // --- Asistencias de clase ---

    suspend fun guardarAsistencia(asistencia: AsistenciaClaseEntity) = dao.insertAsistencia(asistencia)

    fun asistenciaEstudiante(estudianteId: Long, claseId: Long): Flow<AsistenciaClaseEntity?> =
        dao.getAsistenciaEstudiante(estudianteId, claseId)

    fun estudiantesPresentes(claseId: Long): Flow<List<AsistenciaClaseEntity>> =
        dao.getEstudiantesPresentesEnClase(claseId)
}
