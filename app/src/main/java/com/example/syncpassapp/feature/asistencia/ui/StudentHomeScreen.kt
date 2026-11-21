package com.example.syncpassapp.feature.asistencia.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.syncpassapp.core.common.qr.CodigoQr
import com.example.syncpassapp.core.database.local.entities.ClaseEntity
import com.example.syncpassapp.core.designsystem.components.DialogEscanearQr
import com.example.syncpassapp.core.designsystem.components.EstadoVacio
import com.example.syncpassapp.core.designsystem.components.TarjetaClase
import com.example.syncpassapp.feature.asistencia.ui.StudentHomeViewModel
import com.example.syncpassapp.feature.sync.ui.CursosRemotosViewModel

@Composable
fun StudentHomeScreen(
    viewModel: StudentHomeViewModel = hiltViewModel(),
    cursosRemotosViewModel: CursosRemotosViewModel = hiltViewModel(),
    onNavigateToQuestionnaire: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val cursosRemotos by cursosRemotosViewModel.cursos.collectAsState()
    val descargandoCursos by cursosRemotosViewModel.descargando.collectAsState()
    val mensajeCursos by cursosRemotosViewModel.mensaje.collectAsState()
    var showDialogEscanearClase by remember { mutableStateOf(false) }
    var cursoSeleccionado by remember { mutableStateOf<com.example.syncpassapp.core.database.local.entities.ProyectoEntity?>(null) }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Mis Cursos y Encuestas",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        uiState.usuarioInfo?.let { info ->
            Text(
                text = "Bienvenido/a, ${info.usuario.nombre}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { showDialogEscanearClase = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(imageVector = Icons.Default.Share, contentDescription = null)
            Spacer(modifier = Modifier.padding(4.dp))
            Text("Escanear QR de Clase del Profesor")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Cursos disponibles del servidor (solo lectura para estudiantes)
        Text(
            text = "Cursos disponibles",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
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

        mensajeCursos?.let { msg ->
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = msg,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (cursosRemotos.isNotEmpty()) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(cursosRemotos) { curso ->
                    com.example.syncpassapp.core.designsystem.components.TarjetaClase(
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

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.clases.isEmpty()) {
            EstadoVacio(
                texto = "No estás inscrito/a en ninguna clase actualmente.",
                modifier = Modifier.fillMaxSize()
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.clases) { clase ->
                    TarjetaClaseEstudiante(
                        clase = clase,
                        viewModel = viewModel,
                        onClick = { onNavigateToQuestionnaire(clase.claseId) }
                    )
                }
            }
        }
    }

    cursoSeleccionado?.let { curso ->
        com.example.syncpassapp.feature.clases.ui.DialogDetalleCurso(
            curso = curso,
            onDismiss = { cursoSeleccionado = null }
        )
    }

    if (showDialogEscanearClase) {
        DialogEscanearQr(
            titulo = "Escanear QR de Clase",
            prompt = "Apunta la cámara al código QR de la clase que muestra tu profesor.",
            valorSugeridoQr = CodigoQr.Clase.generar(1L),
            onDismiss = { showDialogEscanearClase = false },
            onQrEscaneado = { codigoEscaneado ->
                val claseId = CodigoQr.Clase.leer(codigoEscaneado)
                if (claseId != null) {
                    showDialogEscanearClase = false
                    onNavigateToQuestionnaire(claseId)
                } else {
                    Toast.makeText(
                        context,
                        "Código QR de clase inválido. Formato esperado: ${CodigoQr.Clase.formato}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }
}

@Composable
fun TarjetaClaseEstudiante(
    clase: ClaseEntity,
    viewModel: StudentHomeViewModel,
    onClick: () -> Unit
) {
    val asistencia by viewModel.obtenerAsistenciaEstudiante(clase.claseId)
        .collectAsState(initial = null)

    val esPresente = asistencia?.esPresente == true
    val tieneRespuesta = !asistencia?.respuestasCuestionarioJson.isNullOrBlank()

    TarjetaClase(
        nombre = clase.nombre,
        descripcion = clase.descripcion,
        onClick = onClick,
        colorContenedor = if (esPresente) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        encabezado = {
            Text(
                text = if (esPresente) "PRESENTE" else "PENDIENTE",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = if (esPresente) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
            )
        },
        pie = {
            Text(
                text = if (tieneRespuesta) "Encuesta respondida ✓" else "Toca para responder encuesta de clase ->",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    )
}

