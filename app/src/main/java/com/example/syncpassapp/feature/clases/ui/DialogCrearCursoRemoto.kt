package com.example.syncpassapp.feature.clases.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Diálogo para crear un curso (proyecto) en el backend.
 * Solo ADMINISTRADOR y COORDINADOR pueden crear cursos.
 */
@Composable
fun DialogCrearCursoRemoto(
    onDismiss: () -> Unit,
    onCrear: (titulo: String, descripcion: String?, areaTematica: String, unidadVecinal: String, fechaInicio: String, fechaTermino: String, estado: String) -> Unit
) {
    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var area by remember { mutableStateOf("") }
    var unidad by remember { mutableStateOf("") }
    var fechaInicio by remember { mutableStateOf("2026-01-01") }
    var fechaTermino by remember { mutableStateOf("2026-06-30") }
    var estado by remember { mutableStateOf("PLANIFICADO") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Crear curso en el servidor") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Título *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = area,
                    onValueChange = { area = it },
                    label = { Text("Área temática *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = unidad,
                    onValueChange = { unidad = it },
                    label = { Text("Unidad vecinal *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = fechaInicio,
                        onValueChange = { fechaInicio = it },
                        label = { Text("Fecha inicio (yyyy-MM-dd)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = fechaTermino,
                        onValueChange = { fechaTermino = it },
                        label = { Text("Fecha término (yyyy-MM-dd)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Text("Estado inicial:", style = MaterialTheme.typography.bodyMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = estado == "PLANIFICADO", onClick = { estado = "PLANIFICADO" })
                    Text("Planificado")
                    Spacer(modifier = Modifier.width(12.dp))
                    RadioButton(selected = estado == "EN_CURSO", onClick = { estado = "EN_CURSO" })
                    Text("En Curso")
                }
            }
        },
        confirmButton = {
            Button(
                enabled = titulo.isNotBlank() && area.isNotBlank() && unidad.isNotBlank(),
                onClick = {
                    onCrear(
                        titulo.trim(),
                        descripcion.trim().ifBlank { null },
                        area.trim(),
                        unidad.trim(),
                        fechaInicio.trim(),
                        fechaTermino.trim(),
                        estado
                    )
                }
            ) {
                Text("Crear")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
