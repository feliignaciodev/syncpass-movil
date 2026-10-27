# SyncPassApp - Documentación y Estado del Proyecto

Aplicación móvil desarrollada en **Jetpack Compose (Material 3)** bajo arquitectura **Clean Architecture / MVVM**, integrada completamente con el backend monolítico [`monolito_backend_syncpass`](https://github.com/feliignaciodev/monolito_backend_syncpass) y autenticación mediante **AWS Cognito**.

---

## 1. Características Principales Implementadas

### 🔐 Autenticación y Seguridad (Cognito)
- **`AuthApi` & `AuthRepository`**: Conexión al endpoint `/api/auth/login` (pasarela segura hacia AWS Cognito).
- **`TokenDataStore` & `TokenInterceptor`**: Almacenamiento seguro de tokens JWT e inyección automática en las cabeceras HTTP de Retrofit.
- **Navegación por Roles**: Enrutamiento dinámico según el rol del usuario autenticado (Profesor / Estudiante / Administrador).

### 🗄️ Base de Datos Local (Room) y Sincronización Offline
- **Entidades SyncPass**:
  - `ProyectoEntity` / `EventoEntity` / `ParticipanteEntity` / `InscripcionEntity`
  - `AsistenciaEntity` / `EncuestaEntity` (con cola offline y marcas de sincronización).
- **`SyncPassDao` & `SyncRepository`**:
  - Descarga y almacenamiento local del catálogo de proyectos y eventos (`/api/sync/catalogo`).
  - Sincronización automática por lotes de asistencias y encuestas pendientes hacia el backend (`/api/sync/asistencias`, `/api/sync/encuestas`).

### 🎨 Apartado Visual (Jetpack Compose & Material 3)
- **`LoginScreen`**: Interfaz limpia de inicio de sesión con validación y estados de carga.
- **`TeacherHomeScreen`**: Panel de profesor para gestión y creación de clases (`DialogCrearClase`).
- **`StudentHomeScreen`**: Panel de estudiante con escáner QR (`DialogEscanearQr` con CameraX / ML Kit) e indicadores de asistencia en tiempo real.
- **`NavigationBar`**: Barra de navegación inferior adaptativa por rol.

---

## 2. Cómo Levantar la Aplicación

### A. Backend (`monolito_backend_syncpass`)
1. Configura tus credenciales de AWS Cognito en el backend.
2. Ejecuta el servidor Spring Boot localmente:
   ```bash
   mvn spring-boot:run
   ```
   *(Disponible en `http://localhost:8080`).*

### B. App Móvil (`SyncPassApp`)
1. La URL base está configurada en [`app/build.gradle.kts`](file:///C:/Src/SyncPassApp/app/build.gradle.kts):
   ```kotlin
   buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2:8080/\"")
   ```
2. Sincroniza Gradle en Android Studio y ejecuta la app en tu emulador o dispositivo físico.
