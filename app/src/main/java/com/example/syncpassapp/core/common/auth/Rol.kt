package com.example.syncpassapp.core.common.auth

/**
 * Perfiles de la rúbrica y su equivalencia con los grupos de Cognito.
 *
 * El backend y Cognito usan los grupos BENEFICIARIO, COORDINADOR, VOLUNTARIO y
 * ADMINISTRADOR; la app los traduce a estos cuatro perfiles para decidir la
 * navegación y mostrarlos al usuario. También se aceptan los nombres antiguos
 * de la app (PROFESOR, ESTUDIANTE, ADMIN) para no romper sesiones guardadas.
 */
enum class Rol(val etiqueta: String, private val grupos: Set<String>) {
    ESTUDIANTE("Estudiante", setOf("BENEFICIARIO", "ESTUDIANTE")),
    DOCENTE("Docente", setOf("COORDINADOR", "DOCENTE", "PROFESOR")),
    VOLUNTARIO("Voluntario", setOf("VOLUNTARIO")),
    ADMINISTRADOR("Administrador", setOf("ADMINISTRADOR", "ADMIN"));

    /**
     * Docente, Voluntario y Administrador trabajan en terreno: entran al inicio
     * docente (registrar asistencia y sincronizar). Estudiante entra al inicio de estudiante.
     */
    val esPersonal: Boolean
        get() = this != ESTUDIANTE

    companion object {
        /** Traduce los grupos de Cognito (sin importar mayúsculas) a perfiles; ignora los desconocidos. */
        fun desdeGrupos(grupos: Collection<String>): Set<Rol> {
            val normalizados = grupos.map { it.trim().uppercase() }.toSet()
            return entries.filter { rol -> rol.grupos.any { it in normalizados } }.toSet()
        }

        /**
         * Perfil principal para la navegación y la UI cuando un usuario tiene varios:
         * Administrador > Docente > Voluntario > Estudiante. Sin grupos reconocidos, Estudiante
         * (es el rol que el backend asigna en el auto-registro).
         */
        fun principal(roles: Set<Rol>): Rol = when {
            ADMINISTRADOR in roles -> ADMINISTRADOR
            DOCENTE in roles -> DOCENTE
            VOLUNTARIO in roles -> VOLUNTARIO
            else -> ESTUDIANTE
        }
    }
}
