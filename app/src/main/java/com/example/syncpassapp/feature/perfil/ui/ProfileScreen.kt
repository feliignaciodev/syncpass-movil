package com.example.syncpassapp.feature.perfil.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.syncpassapp.core.common.qr.CodigoQr
import com.example.syncpassapp.core.designsystem.components.DialogMostrarQr
import com.example.syncpassapp.core.designsystem.components.TarjetaSeccion
import com.example.syncpassapp.feature.perfil.ui.ProfileViewModel
import com.example.syncpassapp.feature.sync.ui.SyncViewModel
import com.example.syncpassapp.feature.sync.ui.DialogRegistrarAsistenciaOffline

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    syncViewModel: SyncViewModel = hiltViewModel(),
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val syncUiState by syncViewModel.uiState.collectAsState()
    var showDialogQrEstudiante by remember { mutableStateOf(false) }
    var showDialogOffline by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Perfil de Usuario",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        uiState.usuarioConRoles?.let { info ->
            TarjetaSeccion {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Avatar de Usuario",
                        modifier = Modifier.height(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = info.usuario.nombre,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = info.usuario.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                    // Perfil de la rúbrica según el token (Estudiante, Docente, Voluntario o Administrador);
                    // mientras se lee la sesión se muestran los roles locales.
                    Text(
                        text = "Rol: ${uiState.rol?.etiqueta ?: info.roles.joinToString { it.nombreRol }}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = { showDialogQrEstudiante = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null)
                        Spacer(modifier = Modifier.padding(4.dp))
                        Text("Mi Código QR de Estudiante")
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    viewModel.logout()
                    onLogout()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cerrar Sesión")
            }
        } ?: run {
            Text("Cargando perfil...")
        }

        // Sincronización manual: la pantalla solo habla con SyncViewModel.
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        Text(
            text = "Sincronización",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Pendientes: ${syncUiState.pendientesAsistencias} asistencias, " +
                "${syncUiState.pendientesEncuestas} encuestas",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Button(
            onClick = { syncViewModel.sincronizarPendientes() },
            enabled = !syncUiState.sincronizando,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (syncUiState.sincronizando) {
                CircularProgressIndicator(
                    modifier = Modifier.height(20.dp).width(20.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text("Sincronizar pendientes")
        }
        syncUiState.mensaje?.let { mensaje ->
            Text(
                text = mensaje,
                style = MaterialTheme.typography.bodyMedium,
                color = if (syncUiState.esError) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                }
            )
            if (!syncUiState.esError) {
                Text(
                    text = "Aceptados: ${syncUiState.aceptados} · " +
                        "Duplicados: ${syncUiState.duplicados} · Rechazados: ${syncUiState.rechazados}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        OutlinedButton(
            onClick = { showDialogOffline = true },
            enabled = !syncUiState.sincronizando && !syncUiState.isDownloading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Registrar asistencia offline")
        }
        OutlinedButton(
            onClick = { syncViewModel.descargarCatalogo() },
            enabled = !syncUiState.isDownloading && !syncUiState.sincronizando,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (syncUiState.isDownloading) "Descargando catálogo..." else "Descargar catálogo")
        }

        if (showDialogOffline) {
            DialogRegistrarAsistenciaOffline(
                eventos = syncUiState.eventos,
                participantes = syncUiState.participantes,
                onDismiss = { showDialogOffline = false },
                onRegistrar = { eventoId, participanteId, tipo, lat, lon, dist, estado, inscrito, registradoPor, dispositivo ->
                    syncViewModel.registrarAsistenciaOffline(
                        eventoId = eventoId,
                        participanteId = participanteId,
                        tipoParticipante = tipo,
                        latitud = lat,
                        longitud = lon,
                        distanciaMetros = dist,
                        estadoValidacion = estado,
                        inscritoEnSitio = inscrito,
                        registradoPor = registradoPor,
                        dispositivoId = dispositivo
                    )
                    showDialogOffline = false
                }
            )
        }

        if (showDialogQrEstudiante) {
            DialogMostrarQr(
                titulo = "Mi Código QR de Estudiante",
                subtitulo = "Muestra este código al profesor durante la clase para registrar tu asistencia.",
                contenidoQr = CodigoQr.Estudiante.generar(uiState.usuarioId),
                onDismiss = { showDialogQrEstudiante = false }
            )
        }
    }
}

