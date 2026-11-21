package com.example.syncpassapp.feature.sync.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.syncpassapp.core.common.location.CoordenadasDefault
import com.example.syncpassapp.core.common.location.LocationHelper
import com.example.syncpassapp.core.database.local.entities.EventoEntity
import com.example.syncpassapp.core.database.local.entities.ParticipanteEntity

/**
 * Diálogo para crear un registro offline REAL usando IDs del catálogo remoto.
 *
 * Usa una coordenada predeterminada (Santiago) para calcular la distancia al evento
 * y determinar si el registro queda dentro del radio permitido.
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

    // Coordenada predeterminada (Santiago)
    val lat = CoordenadasDefault.LATITUD
    val lon = CoordenadasDefault.LONGITUD

    // Calcular distancia al evento seleccionado
    val distancia: Double? = eventoSeleccionado?.let { evento: EventoEntity ->
        LocationHelper.distanciaMetros(lat, lon, evento.latitud, evento.longitud)
    }
    val estadoVal: String = if (distancia != null && eventoSeleccionado != null) {
        LocationHelper.estadoValidacion(distancia, eventoSeleccionado!!.radioMetros)
    } else {
        "SIN_UBICACION"
    }

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
                                it()
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

                // Mostrar distancia y estado de validación
                if (eventoSeleccionado != null && distancia != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Distancia al evento: ${distancia.toInt()} m (radio: ${eventoSeleccionado!!.radioMetros} m)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Estado: $estadoVal",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = if (estadoVal == "VALIDO") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "Coordenada usada: ${CoordenadasDefault.NOMBRE}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
                        lat,
                        lon,
                        distancia,
                        estadoVal,
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
