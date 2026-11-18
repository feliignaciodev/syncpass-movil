# Alcance — SyncPass (App Android)

## 1. Requerimientos Funcionales (RF)

| ID | Descripción | Estado |
|---|---|---|
| RF-01 | Login con email/contraseña contra Cognito | Implementado |
| RF-02 | Registro y confirmación de código | Implementado (sin vista de confirmación dedicada) |
| RF-03 | Restauración de sesión y logout | Implementado |
| RF-04 | Crear/editar/finalizar clases (docente) | Implementado (local) |
| RF-05 | Inscribir estudiantes en una clase | Implementado (local) |
| RF-06 | Escanear QR de clase (cámara) | Implementado |
| RF-07 | Registrar asistencia presente/ausente | Implementado (local) |
| RF-08 | Cuestionario por clase (solo si presente) | Implementado |
| RF-09 | Descargar catálogo remoto | Implementado (`SyncRepository.descargarYGuardarCatalogo`) |
| RF-10 | Registrar asistencia offline (pendiente) | Implementado (`SyncRepository.registrarAsistenciaLocal`) |
| RF-11 | Sincronizar pendientes con botón explícito | Implementado (`SyncViewModel.sincronizarPendientes` + UI) |
| RF-12 | Notificaciones push (ntfy/FCM) | Parcial |
| RF-13 | Perfil y QR de estudiante | Implementado |
| RF-14 | Geolocalización | **Backlog (no implementado)** |

## 2. Requerimientos No Funcionales (RNF)

| ID | Descripción |
|---|---|
| RNF-01 | Lenguaje Kotlin íntegramente. |
| RNF-02 | Arquitectura MVVM con separación de capas. |
| RNF-03 | Persistencia local offline-first (Room). |
| RNF-04 | Sincronización explícita (botón) y reintento automático al recuperar red. |
| RNF-05 | Al menos 2 APIs nativas (cámara/QR, notificaciones). |
| RNF-06 | Pruebas unitarias + cobertura mínima 30% (JaCoCo). |
| RNF-07 | Documentación C4 y alcance. |
| RNF-08 | Coherencia entre docs e implementación. |

## 3. Casos de uso principales

### CU-01 Login
- Actor: Docente/Estudiante.
- Flujo: ingresar credenciales → Cognito → guardar token en DataStore → navegar a home según rol.

### CU-02 Crear clase (docente)
- Flujo: nombre, descripción, cuestionario JSON, estado → Room → listado filtrable.

### CU-03 Registrar asistencia (online local)
- Flujo: escanear QR de clase → marcar presente/ausente → guardar respuestas si presente.

### CU-04 Offline + sincronización
- Precondición: catálogo descargado desde Perfil.
- Flujo: abrir "Registrar asistencia offline", elegir evento y participante remotos → guardar `sincronizado=false` con `estadoValidacion=SIN_UBICACION` → al recuperar internet se ejecuta la subida automática (`NetworkCallback`) o el usuario pulsa "Sincronizar pendientes" → lotes al backend → aceptados/duplicados marcados, rechazados quedan pendientes.

### CU-05 Descargar catálogo
- Flujo: botón "Descargar catálogo" → GET /api/sync/catalogo → persistir proyectos/eventos/participantes/inscripciones en Room.

## 4. Flujos

- **Online:** login → home → clases → asistencia → (opcional) sync.
- **Offline:** catálogo ya descargado → registrar asistencia pendiente → sync al recuperar conexión.

## 5. Estado implementado / parcial / backlog

| Funcionalidad | Estado |
|---|---|
| Auth (login/registro) | Implementado |
| Clases (CRUD local) | Implementado |
| Asistencia local | Implementado |
| Cámara/QR | Implementado |
| Notificaciones | Parcial |
| Offline sync (infra + botón) | Implementado |
| Registro offline con catálogo remoto + botón/reintento automático | Implementado (no reemplaza el modelo local de clases; sin migración Room) |
| Geolocalización | Backlog |
| C4 / alcance / mockups | Implementado (este directorio) |
| Tests + JaCoCo ≥30% | Parcial (36 tests, reporte disponible) |

## 6. Equivalencia de roles con backend

| Rol app (Cognito) | Rol backend |
|---|---|
| ADMINISTRADOR | ADMINISTRADOR |
| COORDINADOR (docente) | COORDINADOR |
| VOLUNTARIO | VOLUNTARIO |
| BENEFICIARIO (estudiante) | BENEFICIARIO |

`TipoParticipante.ESTUDIANTE/DOCENTE` del backend clasifica asistentes, no es autorización Cognito.
