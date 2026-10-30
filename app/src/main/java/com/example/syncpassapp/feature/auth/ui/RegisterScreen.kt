package com.example.syncpassapp.feature.auth.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Crear Cuenta",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = uiState.username,
                    onValueChange = { viewModel.onUsernameChanged(it) },
                    label = { Text("Nombre de Usuario") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = uiState.nombre,
                    onValueChange = { viewModel.onNombreChanged(it) },
                    label = { Text("Nombre Completo") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = uiState.email,
                    onValueChange = { viewModel.onEmailChanged(it) },
                    label = { Text("Correo Electrónico") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = uiState.telefono,
                    onValueChange = { viewModel.onTelefonoChanged(it) },
                    label = { Text("Teléfono (Ej: +56912345678)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = uiState.password,
                    onValueChange = { viewModel.onPasswordChanged(it) },
                    label = { Text("Contraseña") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )

                Spacer(modifier = Modifier.height(20.dp))

                if (uiState.isLoading) {
                    CircularProgressIndicator()
                } else {
                    Button(
                        onClick = { viewModel.registrar { onNavigateBack() } },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Registrarse", fontSize = 16.sp)
                    }
                }

                uiState.errorMessage?.let { error ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = error, color = MaterialTheme.colorScheme.error)
                }

                uiState.successMessage?.let { success ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = success, color = MaterialTheme.colorScheme.primary)
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(onClick = onNavigateBack) {
                    Text("Volver al Inicio de Sesión")
                }
            }
        }
    }
}
