package com.example.syncpassapp.feature.clases.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.syncpassapp.core.designsystem.components.EstadoVacio
import com.example.syncpassapp.core.designsystem.components.TarjetaClase

@Composable
fun TeacherHomeScreen(
    viewModel: TeacherHomeViewModel = hiltViewModel(),
    onNavigateToSession: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDialogCrearClase by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialogCrearClase = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Crear nueva clase")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp)
        ) {
            Text(
                text = "Panel Docente",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Gestión de clases y eventos SyncPass",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Filtros de estado (Todos, En Curso, Finalizados)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.filtroEstado == "TODOS",
                    onClick = { viewModel.cambiarFiltro("TODOS") },
                    label = { Text("Todos") }
                )
                FilterChip(
                    selected = uiState.filtroEstado == "EN_CURSO",
                    onClick = { viewModel.cambiarFiltro("EN_CURSO") },
                    label = { Text("🟢 En Curso") }
                )
                FilterChip(
                    selected = uiState.filtroEstado == "FINALIZADO",
                    onClick = { viewModel.cambiarFiltro("FINALIZADO") },
                    label = { Text("🏁 Finalizados") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.clases.isEmpty()) {
                EstadoVacio(
                    texto = "No hay clases en esta categoría.",
                    modifier = Modifier.fillMaxSize()
                ) {
                    Button(onClick = { showDialogCrearClase = true }) {
                        Text("Crear Primera Clase")
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.clases) { clase ->
                        val esEnCurso = clase.estado == "EN_CURSO"
                        TarjetaClase(
                            nombre = clase.nombre,
                            descripcion = clase.descripcion,
                            onClick = { onNavigateToSession(clase.claseId) },
                            colorContenedor = if (esEnCurso) {
                                MaterialTheme.colorScheme.surfaceVariant
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                            },
                            encabezado = {
                                Text(
                                    text = if (esEnCurso) "🟢 En Curso" else "🏁 Finalizado",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (esEnCurso) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                                )
                            }
                        )
                    }
                }
            }
        }

        if (showDialogCrearClase) {
            DialogCrearClase(
                onDismiss = { showDialogCrearClase = false },
                onClaseCreada = { nombre, descripcion, cuestionarioJson, estado ->
                    viewModel.crearClase(nombre, descripcion, cuestionarioJson, estado)
                    showDialogCrearClase = false
                }
            )
        }
    }
}

@Composable
fun DialogCrearClase(
    onDismiss: () -> Unit,
    onClaseCreada: (String, String, String, String) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var estadoSeleccionado by remember { mutableStateOf("EN_CURSO") }
    var cuestionarioJson by remember {
        mutableStateOf("{\"preguntas\":[\"¿Asististe a la sesión?\", \"¿Tienes alguna duda?\"]}")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Crear Nueva Clase") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre de la Clase") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Estado Inicial:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = estadoSeleccionado == "EN_CURSO",
                        onClick = { estadoSeleccionado = "EN_CURSO" }
                    )
                    Text("En Curso")
                    Spacer(modifier = Modifier.width(16.dp))
                    RadioButton(
                        selected = estadoSeleccionado == "FINALIZADO",
                        onClick = { estadoSeleccionado = "FINALIZADO" }
                    )
                    Text("Finalizado")
                }

                OutlinedTextField(
                    value = cuestionarioJson,
                    onValueChange = { cuestionarioJson = it },
                    label = { Text("Cuestionario Estructura (JSON)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                enabled = nombre.isNotBlank(),
                onClick = {
                    onClaseCreada(nombre, descripcion, cuestionarioJson, estadoSeleccionado)
                }
            ) {
                Text("Crear")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
