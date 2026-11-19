package com.example.syncpassapp.util

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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** DAO en memoria con solo el estado que necesitan las reglas de negocio. */
class FakeUniversidadDao : UniversidadDao {

    val clases = LinkedHashMap<Long, ClaseEntity>()
    val asistencias = LinkedHashMap<Pair<Long, Long>, AsistenciaClaseEntity>()
    private val _clases = MutableStateFlow<List<ClaseEntity>>(emptyList())

    private var siguienteClaseId = 1L

    // Usuarios, roles locales y su relación (para login, sesión y perfil).
    val usuarios = LinkedHashMap<Long, UsuarioEntity>()
    val roles = LinkedHashMap<Long, RolEntity>()
    val usuarioRoles = LinkedHashSet<UsuarioRolCrossRef>()
    private val _usuarios = MutableStateFlow<List<UsuarioEntity>>(emptyList())
    private var siguienteUsuarioId = 1L
    private var siguienteRolId = 1L

    override suspend fun insertUsuario(usuario: UsuarioEntity): Long {
        val id = if (usuario.usuarioId == 0L) siguienteUsuarioId++ else usuario.usuarioId
        usuarios[id] = usuario.copy(usuarioId = id)
        _usuarios.value = usuarios.values.toList()
        return id
    }

    override suspend fun insertRol(rol: RolEntity): Long {
        val id = if (rol.rolId == 0L) siguienteRolId++ else rol.rolId
        roles[id] = rol.copy(rolId = id)
        return id
    }

    override suspend fun insertUsuarioRolCrossRef(crossRef: UsuarioRolCrossRef) {
        usuarioRoles += crossRef
    }

    override fun getAllUsuarios(): Flow<List<UsuarioEntity>> = _usuarios

    override fun getAllRoles(): Flow<List<RolEntity>> = MutableStateFlow(roles.values.toList())

    override fun getUsuarioConRoles(usuarioId: Long): Flow<UsuarioConRoles?> =
        _usuarios.map { usuarioConRoles(usuarioId) }

    override suspend fun getUsuarioConRolesByEmail(email: String): UsuarioConRoles? =
        usuarios.values.firstOrNull { it.email == email }?.let { usuarioConRoles(it.usuarioId) }

    override fun getRolConUsuarios(rolId: Long): Flow<RolConUsuarios?> =
        MutableStateFlow(null)

    override suspend fun getRolIdByNombre(nombreRol: String): Long? =
        roles.values.firstOrNull { it.nombreRol == nombreRol }?.rolId

    private fun usuarioConRoles(usuarioId: Long): UsuarioConRoles? =
        usuarios[usuarioId]?.let { usuario ->
            UsuarioConRoles(
                usuario = usuario,
                roles = usuarioRoles.filter { it.usuarioId == usuarioId }.mapNotNull { roles[it.rolId] }
            )
        }

    override suspend fun getAllClasesList(): List<ClaseEntity> = clases.values.toList()

    override suspend fun insertClase(clase: ClaseEntity): Long {
        val id = if (clase.claseId == 0L) siguienteClaseId++ else clase.claseId
        val guardada = clase.copy(claseId = id)
        clases[id] = guardada
        publicarClases()
        return id
    }

    override suspend fun updateClase(clase: ClaseEntity) {
        clases[clase.claseId] = clase
        publicarClases()
    }

    override suspend fun getClaseById(claseId: Long): ClaseEntity? = clases[claseId]

    override fun getClasesByProfesor(profesorId: Long): Flow<List<ClaseEntity>> =
        _clases.mapFiltradas { it.profesorId == profesorId }

    override fun getClasesByEstudiante(estudianteId: Long): Flow<List<ClaseEntity>> =
        _clases.mapFiltradas { clase ->
            asistencias.keys.any { (usuarioId, claseId) ->
                usuarioId == estudianteId && claseId == clase.claseId
            }
        }

    override fun getAsistenciaEstudiante(
        estudianteId: Long,
        claseId: Long
    ): Flow<AsistenciaClaseEntity?> =
        MutableStateFlow(asistencias[estudianteId to claseId])

    override fun getAllClases(): Flow<List<ClaseEntity>> = _clases

    override fun getClaseConDetalles(claseId: Long): Flow<ClaseConDetalles?> =
        MutableStateFlow(null)

    override suspend fun insertAsistencia(asistencia: AsistenciaClaseEntity) {
        asistencias[asistencia.usuarioId to asistencia.claseId] = asistencia
    }

    override fun getEstudiantesPresentesEnClase(claseId: Long): Flow<List<AsistenciaClaseEntity>> =
        MutableStateFlow(asistencias.values.filter { it.claseId == claseId && it.esPresente })

    override suspend fun deleteUsuario(usuario: UsuarioEntity) = Unit

    override suspend fun deleteClase(clase: ClaseEntity) {
        clases.remove(clase.claseId)
        asistencias.keys.removeIf { it.second == clase.claseId }
        publicarClases()
    }

    private fun publicarClases() {
        _clases.value = clases.values.toList()
    }

    private fun MutableStateFlow<List<ClaseEntity>>.mapFiltradas(
        predicado: (ClaseEntity) -> Boolean
    ): Flow<List<ClaseEntity>> = map { lista -> lista.filter(predicado) }
}
