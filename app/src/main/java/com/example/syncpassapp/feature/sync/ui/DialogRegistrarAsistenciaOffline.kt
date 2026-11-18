package com.example.syncpassapp.feature.sync.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.syncpassapp.core.database.local.entities.EventoEntity
import com.example.syncpassapp.core.database.local.entities.ParticipanteEntity

/**
 * Diálogo para crear un registro offline REAL usando IDs del catálogo remoto.
 *
 * No se usa ningún ID local de las clases de ejemplo: se eligen evento y participante
 * remotos, que es el contrato que exige `POST /api/sync/asistencias`.
 *
 * La geolocalización no está implementada: `estadoValidacion` se guarda como
 * `SIN_UBICACION` y los campos de coordenadas/distancia van en null.
 */
@Composable
fun DialogRegistrarAsistenciaOffline(
    eventos: List<EventoEntity>,
    participantes: List<ParticipanteEntity>,
    onDismiss: () -> Unit,
    onRegistrar: (
        eventoId: Long,
        participanteId: String,
        tipoParticipante: String,
        latitud: Double?,
        longitud: Double?,
        distanciaMetros: Double?,
        estadoValidacion: String,
        inscritoEnSitio: Boolean,
        registradoPor: Long?,
        dispositivoId: String?
    ) -> Unit
) {
    var eventoSeleccionado by remember(eventos) { mutableStateOf(eventos.firstOrNull()) }
    var participanteSeleccionado by remember(participantes) { mutableStateOf(participantes.firstOrNull()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registrar asistencia offline") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Se guardará como pendiente de sincronización.")
                Text(
                    if (eventos.isEmpty()) {
                        "Descarga el catálogo para poder registrar asistencias."
                    } else {
                        "Elige el evento y el participante del catálogo remoto:"
                    }
                )

                Selector(
                    etiqueta = "Evento",
                    selectedText = eventoSeleccionado?.nombre ?: "Evento",
                    enabled = eventos.isNotEmpty()
                ) {
                    eventos.forEach { evento ->
                        DropdownMenuItem(
                            text = { Text(evento.nombre) },
                            onClick = {
                                eventoSeleccionado = evento
                                it() // cerrar el menú
                            }
                        )
                    }
                }

                Selector(
                    etiqueta = "Participante",
                    selectedText = participanteSeleccionado?.alias ?: "Participante",
                    enabled = participantes.isNotEmpty()
                ) {
                    participantes.forEach { participante ->
                        DropdownMenuItem(
                            text = { Text("${participante.alias} (${participante.tipo})") },
                            onClick = {
                                participanteSeleccionado = participante
                                it()
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = eventoSeleccionado != null && participanteSeleccionado != null,
                onClick = {
                    val evento = eventoSeleccionado ?: return@Button
                    val participante = participanteSeleccionado ?: return@Button
                    val tipo = participante.tipo.ifBlank { "ESTUDIANTE" }
                    onRegistrar(
                        evento.id,
                        participante.id,
                        tipo,
                        null,
                        null,
                        null,
                        "SIN_UBICACION",
                        false,
                        null,
                        null
                    )
                }
            ) {
                Text("Guardar pendiente")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

/** Selector sencillo con DropdownMenu anclado a un OutlinedButton. */
@Composable
private fun Selector(
    etiqueta: String,
    selectedText: String,
    enabled: Boolean,
    contenido: @Composable (close: () -> Unit) -> Unit
) {
    var expandido by remember { mutableStateOf(false) }
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(
            onClick = { if (enabled) expandido = true },
            enabled = enabled,
            modifier = Modifier.weight(1f)
        ) {
            Text("$etiqueta: $selectedText")
        }
        DropdownMenu(
            expanded = expandido,
            onDismissRequest = { expandido = false }
        ) {
            contenido { expandido = false }
        }
    }
}