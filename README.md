# SyncPassApp - Guía de Integración y Siguientes Pasos

Este documento detalla el estado actual de la aplicación móvil **SyncPassApp**, su integración con el backend monolítico (`monolito_backend_syncpass` conectado a AWS Cognito), y los pasos pendientes para completar el flujo funcional completo.

---

## 1. Arquitectura y Estado Actual
- **Clean Architecture / MVVM**: Estructura organizada en `core/` (red, base de datos, datastore, diseño) y `feature/` (autenticación, asistencia, clases, notificaciones, perfil).
- **Capa de Red (Retrofit + OkHttp)**:
  - `AuthApi`: Login, registro, confirmación y reenvío de código (`/api/auth/...`).
  - `SyncApi`: Descarga de catálogo offline y sincronización por lotes de asistencias y encuestas (`/api/sync/...`).
  - `EventoApi`: Métricas en vivo de eventos (`/api/eventos/...`).
  - `UsuarioApi`: Gestión de usuarios y roles (`/api/usuarios/...`).
- **Autenticación (AWS Cognito vía Backend)**: El backend actúa como pasarela hacia AWS Cognito. El endpoint `/api/auth/login` recibe las credenciales (`username`, `password`) y retorna el token JWT y el perfil del usuario (`UsuarioDto`).

---

## 2. Cómo Levantar la App y Conectarla al Backend

### A. Configuración del Backend (`monolito_backend_syncpass`)
1. Asegúrate de tener configuradas las variables de entorno para AWS Cognito en el backend (`.env` o `application.properties`).
2. Levanta el backend localmente con Maven:
   ```bash
   mvn spring-boot:run
   ```
   El servidor estará disponible por defecto en `http://localhost:8080`.

### B. Configuración de la App Móvil (`SyncPassApp`)
1. **URL Base para Emulador Android**:
   En [`app/build.gradle.kts`](file:///C:/Src/SyncPassApp/app/build.gradle.kts), la URL base para debug está configurada como:
   ```kotlin
   buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2:8080/\"")
   ```
   *(Si pruebas en un dispositivo físico, cambia `10.0.2.2` por la IP local de tu máquina en la red Wi-Fi).*
2. **Ejecutar la App**:
   - Abre el proyecto en Android Studio.
   - Sincroniza Gradle (`Sync Project with Gradle Files`).
   - Conecta un emulador o dispositivo físico y ejecuta la tarea de compilación/instalación (`Debug`).

---

## 3. ¿Qué Falta para Completar el Flujo Funcional?

Para finalizar la integración y asegurar que todo el flujo end-to-end funcione sin fricciones, se deben completar los siguientes puntos:

### Paso 1: Conexión de la Interfaz de Usuario (UI) de Login
- Asegurar que `LoginScreen` y `LoginViewModel` consuman correctamente `AuthRepository.login()` con los campos `username` y `password`.
- Manejar los estados de éxito, error y navegación hacia la pantalla principal (`StudentHomeScreen` / `TeacherHomeScreen`) según el rol del usuario devuelto por el token o Cognito.

### Paso 2: Sincronización del Catálogo Offline (Room)
- Implementar la llamada a `SyncApi.obtenerCatalogo()` al iniciar sesión o al pulsar sincronizar.
- Almacenar los proyectos, eventos, participantes e inscripciones en la base de datos local (Room) para permitir operación offline.

### Paso 3: Escaneo QR y Registro de Asistencias / Encuestas
- Utilizar CameraX and ML Kit Barcode Scanning (ya configurados en las dependencias) para escanear códigos QR de los participantes en los eventos.
- Enviar las marcas de asistencia y encuestas acumuladas en el dispositivo hacia el backend mediante `SyncApi.sincronizarAsistencias()` y `SyncApi.sincronizarEncuestas()`.

### Paso 4: Pruebas End-to-End (E2E)
- Registrar un usuario de prueba en Cognito a través del backend.
- Iniciar sesión en la app móvil.
- Sincronizar catálogo, registrar una asistencia escaneando un QR y verificar que el backend registre el estado correctamente.
