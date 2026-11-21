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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.syncpassapp.core.common.qr.CodigoQr
import com.example.syncpassapp.core.designsystem.components.DialogEscanearQr
import com.example.syncpassapp.core.designsystem.components.EstadoVacio
import com.example.syncpassapp.core.designsystem.components.TarjetaClase
import com.example.syncpassapp.core.designsystem.components.SeccionHeader
import com.example.syncpassapp.core.designsystem.theme.BackgroundMint
import com.example.syncpassapp.core.designsystem.theme.PrimarySage
import com.example.syncpassapp.feature.sync.ui.CursosRemotosViewModel


@Composable
fun TeacherHomeScreen(
    viewModel: TeacherHomeViewModel = hiltViewModel(),
    cursosRemotosViewModel: CursosRemotosViewModel = hiltViewModel(),
    onNavigateToSession: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val cursosRemotos by cursosRemotosViewModel.cursos.collectAsState()
    val descargandoCursos by cursosRemotosViewModel.descargando.collectAsState()
    val creandoCurso by cursosRemotosViewModel.creando.collectAsState()
    val mensajeCursos by cursosRemotosViewModel.mensaje.collectAsState()
    var showDialogCrearClase by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var showDialogCrearCursoRemoto by remember { mutableStateOf(false) }
    var showDialogEscanearEstudiante by remember { mutableStateOf(false) }
    var estudianteEscaneado by remember { mutableStateOf<Long?>(null) }
    var cursoSeleccionado by remember { mutableStateOf<com.example.syncpassapp.core.database.local.entities.ProyectoEntity?>(null) }

    Scaffold(
        containerColor = BackgroundMint,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialogCrearClase = true },
                containerColor = PrimarySage,
                contentColor = Color.White
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Crear nueva clase")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
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
                }
                OutlinedButton(onClick = { showDialogEscanearEstudiante = true }) {
                    Icon(imageVector = Icons.Default.Person, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Escanear QR")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Filtros de estado
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
                    label = { Text("En Curso") }
                )
                FilterChip(
                    selected = uiState.filtroEstado == "FINALIZADO",
                    onClick = { viewModel.cambiarFiltro("FINALIZADO") },
                    label = { Text("Finalizados") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Clases locales
            if (uiState.clases.isEmpty()) {
                EstadoVacio(
                    texto = "No hay clases en esta categoría.",
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(onClick = { showDialogCrearClase = true }) {
                        Text("Crear Primera Clase")
                    }
                }
            } else {
                SeccionHeader(titulo = "Mis clases")

                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
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
                                    text = if (esEnCurso) "En Curso" else "Finalizado",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (esEnCurso) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                                )
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Cursos descargados del servidor
            SeccionHeader(titulo = "Cursos del servidor")

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (cursosRemotos.isEmpty()) {
                        "Sin cursos descargados"
                    } else {
                        "${cursosRemotos.size} curso(s) disponible(s)"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { showDialogCrearCursoRemoto = true },
                        enabled = !creandoCurso
                    ) {
                        if (creandoCurso) {
                            CircularProgressIndicator(modifier = Modifier.height(16.dp))
                        } else {
                            Text("Crear")
                        }
                    }
                    OutlinedButton(
                        onClick = { cursosRemotosViewModel.descargar() },
                        enabled = !descargandoCursos
                    ) {
                        if (descargandoCursos) {
                            CircularProgressIndicator(modifier = Modifier.height(16.dp))
                        } else {
                            Text("Descargar")
                        }
                    }
                }
            }

            mensajeCursos?.let { msg ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = msg,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (cursosRemotos.isEmpty()) {
                Text(
                    text = "Pulsa \"Descargar\" para traer los cursos creados en la web.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(cursosRemotos) { curso ->
                        TarjetaClase(
                            nombre = curso.titulo,
                            descripcion = buildString {
                                append(curso.descripcion)
                                append("\nÁrea: ${curso.areaTematica}")
                                append(" | ${curso.fechaInicio} → ${curso.fechaTermino}")
                                append("\nEstado: ${curso.estado}")
                            },
                            onClick = { cursoSeleccionado = curso },
                            colorContenedor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        )
                    }
                }
            }
        }

        cursoSeleccionado?.let { curso ->
            DialogDetalleCurso(
                curso = curso,
                onDismiss = { cursoSeleccionado = null }
            )
        }

        if (showDialogEscanearEstudiante) {
            DialogEscanearQr(
                titulo = "Escanear QR de Estudiante",
                prompt = "Apunta la cámara al código QR que muestra el estudiante en su perfil.",
                valorSugeridoQr = CodigoQr.Estudiante.generar(2L),
                onDismiss = { showDialogEscanearEstudiante = false },
                onQrEscaneado = { codigoEscaneado ->
                    val estudianteId = CodigoQr.Estudiante.leer(codigoEscaneado)
                    if (estudianteId != null) {
                        // Marcar presente en la primera clase disponible
                        val primeraClase = uiState.clases.firstOrNull()
                        if (primeraClase != null) {
                            viewModel.marcarEstudiantePresente(primeraClase.claseId, estudianteId) {
                                Toast.makeText(
                                    context,
                                    "Estudiante $estudianteId marcado como PRESENTE en ${primeraClase.nombre}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        } else {
                            Toast.makeText(
                                context,
                                "No hay clases creadas. Crea una clase primero.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        showDialogEscanearEstudiante = false
                    } else {
                        Toast.makeText(
                            context,
                            "Código QR inválido. Formato esperado: ${CodigoQr.Estudiante.formato}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            )
        }

        if (showDialogCrearCursoRemoto) {
            DialogCrearCursoRemoto(
                onDismiss = { showDialogCrearCursoRemoto = false },
                onCrear = { titulo, descripcion, area, unidad, fechaInicio, fechaTermino, estado ->
                    cursosRemotosViewModel.crearCurso(
                        titulo = titulo,
                        descripcion = descripcion,
                        areaTematica = area,
                        unidadVecinal = unidad,
                        fechaInicio = fechaInicio,
                        fechaTermino = fechaTermino,
                        estado = estado
                    )
                    showDialogCrearCursoRemoto = false
                }
            )
        }

        if (showDialogCrearClase) {
            DialogCrearClase(
                eventos = cursosRemotosViewModel.eventos.collectAsState().value,
                onDismiss = { showDialogCrearClase = false },
                onClaseCreada = { nombre, descripcion, cuestionarioJson, estado, remoteEventoId ->
                    viewModel.crearClase(nombre, descripcion, cuestionarioJson, estado, remoteEventoId)
                    showDialogCrearClase = false
                }
            )
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun DialogCrearClase(
    eventos: List<com.example.syncpassapp.core.database.local.entities.EventoEntity> = emptyList(),
    onDismiss: () -> Unit,
    onClaseCreada: (String, String, String, String, Long?) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var estadoSeleccionado by remember { mutableStateOf("EN_CURSO") }
    var cuestionarioJson by remember {
        mutableStateOf("{\"preguntas\":[\"¿Asististe a la clase?\", \"¿Tienes alguna duda?\"]}")
    }
    var eventoSeleccionado by remember { mutableStateOf<com.example.syncpassapp.core.database.local.entities.EventoEntity?>(null) }
    var expandedEvento by remember { mutableStateOf(false) }

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

                // Selector de evento remoto (opcional, para vincular con el backend)
                if (eventos.isNotEmpty()) {
                    Text("Vincular con evento remoto (opcional):", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    androidx.compose.material3.ExposedDropdownMenuBox(
                        expanded = expandedEvento,
                        onExpandedChange = { expandedEvento = it }
                    ) {
                        OutlinedTextField(
                            value = eventoSeleccionado?.nombre ?: "Sin vincular",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { androidx.compose.material3.ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedEvento) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedEvento,
                            onDismissRequest = { expandedEvento = false }
                        ) {
                            androidx.compose.material3.DropdownMenuItem(
                                text = { Text("Sin vincular") },
                                onClick = {
                                    eventoSeleccionado = null
                                    expandedEvento = false
                                }
                            )
                            eventos.forEach { evento ->
                                androidx.compose.material3.DropdownMenuItem(
                                    text = { Text(evento.nombre) },
                                    onClick = {
                                        eventoSeleccionado = evento
                                        expandedEvento = false
                                    }
                                )
                            }
                        }
                    }
                }

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
                    onClaseCreada(nombre, descripcion, cuestionarioJson, estadoSeleccionado, eventoSeleccionado?.id)
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
