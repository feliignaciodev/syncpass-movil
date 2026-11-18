# Mockups y trazabilidad visual — SyncPass (Android)

Wireframes textuales (Mermaid/Markdown). No se incluyen imágenes.

## Vistas existentes

| # | Vista | Archivo | Estado | Pendiente |
|---|---|---|---|---|
| 1 | Login | `feature/auth/ui/LoginScreen.kt` | Implementada | — |
| 2 | Registro | `feature/auth/ui/RegisterScreen.kt` | Parcial | Vista de confirmación de código |
| 3 | Home docente | `feature/clases/ui/TeacherHomeScreen.kt` | Implementada | — |
| 4 | Sesión/clase | `feature/clases/ui/SessionScreen.kt` | Implementada | Captura pendiente remota |
| 5 | Detalle de sesión | `feature/clases/ui/SessionDetailScreen.kt` | Implementada | — |
| 6 | Edición de sesión | `feature/clases/ui/SessionEditScreen.kt` | Implementada | — |
| 7 | Home estudiante | `feature/asistencia/ui/StudentHomeScreen.kt` | Implementada | — |
| 8 | Cuestionario estudiante | `feature/asistencia/ui/StudentQuestionnaireScreen.kt` | Implementada | — |
| 9 | Perfil | `feature/perfil/ui/ProfileScreen.kt` | Implementada | Botón de sincronización presente |

## Diálogos
- `DialogCrearClase` (TeacherHomeScreen): creación de clase.
- `DialogMostrarQr` (ProfileScreen): QR de estudiante.
- Escaneo QR (SessionScreen/StudentHomeScreen): cámara + entrada manual.

## Control de sincronización
- Sección "Sincronización" en `ProfileScreen` con conteo de pendientes, botón "Descargar catálogo", botón "Sincronizar pendientes", resultado/error.

## Estados
- Vacío (EstadoVacio), carga, error de red, éxito de sync, acceso denegado (no aplica por rol en esta app).

## Trazabilidad mockup → implementación
Cada vista listada tiene su Screen correspondiente. Las funcionalidades marcadas "Parcial/Backlog" se detallan en `ALCANCE.md`.
