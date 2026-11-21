package com.example.syncpassapp.feature.clases.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.syncpassapp.core.common.location.CoordenadasDefault
import com.example.syncpassapp.core.common.location.LocationHelper
import com.example.syncpassapp.core.database.local.entities.ProyectoEntity

/**
 * Diálogo de detalle estructurado para un curso descargado del servidor.
 * Muestra todos los campos de forma ordenada y legible.
 */
@Composable
fun DialogDetalleCurso(
    curso: ProyectoEntity,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = curso.titulo,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Estado
                DetailRow(label = "Estado", value = curso.estado, highlight = true)

                // Área temática
                DetailRow(label = "Área temática", value = curso.areaTematica)

                // Unidad vecinal
                DetailRow(label = "Unidad vecinal", value = curso.unidadVecinal)

                // Descripción
                DetailRow(label = "Descripción", value = curso.descripcion)

                // Fechas
                DetailRow(label = "Fecha inicio", value = curso.fechaInicio)
                DetailRow(label = "Fecha término", value = curso.fechaTermino)

                // Coordenada predeterminada usada para geolocalización
                DetailRow(label = "Coordenada predeterminada", value = CoordenadasDefault.NOMBRE)

                // ID
                DetailRow(label = "ID del curso", value = curso.id.toString())
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    highlight: Boolean = false
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value.ifBlank { "—" },
            style = MaterialTheme.typography.bodyMedium,
            color = if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}
