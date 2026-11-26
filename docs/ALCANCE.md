# Alcance — SyncPass (App Android)

## 1. Requerimientos Funcionales (RF)

| ID | Descripción | Estado |
|---|---|---|
| RF-01 | Login con email/contraseña (el backend autentica contra Cognito) | Implementado |
| RF-02 | Registro y confirmación de código | Implementado (sin vista de confirmación dedicada) |
| RF-03 | Restauración de sesión y logout | Implementado |
| RF-04 | Crear/editar clases y filtrarlas por estado (docente) | Implementado (local); el estado se elige al crear, la edición no lo cambia |
| RF-15 | Descargar cursos del servidor (web → app) | Implementado (botón "Descargar" en Panel Docente) |
| RF-16 | Visualizar cursos remotos con detalles (fechas, área, estado) | Implementado (sección "Cursos del servidor") |
| RF-17 | Crear curso en servidor desde la app | Implementado (botón "Crear" en "Cursos del servidor", requiere rol ADMINISTRADOR o COORDINADOR) |
| RF-18 | Geolocalización con coordenada predeterminada | Implementado (Santiago -33.4489, -70.6693; cálculo de distancia Haversine; estado VALIDO/FUERA_DE_RADIO) |
| RF-19 | Vincular clase local con evento remoto | Implementado (dropdown en DialogCrearClase; al marcar asistencia también registra en asistencias_offline) |
| RF-05 | Inscribir estudiantes en una clase | Parcial (local): sin pantalla propia; se inscribe al escanear el QR o al responder el cuestionario |
| RF-06 | Escanear QR de clase (cámara) | Implementado |
| RF-07 | Registrar asistencia presente/ausente | Implementado (local) |
| RF-08 | Cuestionario por clase (solo si presente) | Implementado |
| RF-09 | Descargar catálogo remoto | Implementado (`SyncRepository.descargarYGuardarCatalogo`) |
| RF-10 | Registrar asistencia offline (pendiente) | Implementado (`SyncRepository.registrarAsistenciaLocal`) |
| RF-11 | Sincronizar pendientes con botón explícito | Implementado (`SyncViewModel.sincronizarPendientes` + UI) |
| RF-12 | Notificaciones push (ntfy/FCM) | Parcial |
| RF-13 | Perfil y QR de estudiante | Implementado |
| RF-14 | Geolocalización con GPS real | **Backlog (no implementado)** |
| RF-15 | Perfiles Estudiante, Docente, Voluntario y Administrador con navegación por rol | Implementado (`core/common/auth/Rol`, §6) |

## 2. Requerimientos No Funcionales (RNF)

| ID | Descripción |
|---|---|
| RNF-01 | Lenguaje Kotlin íntegramente. |
| RNF-02 | Arquitectura MVVM en capas: Screen → ViewModel → Repository → DataSource → Room/DataStore/Retrofit. |
| RNF-03 | Persistencia local offline-first (Room). |
| RNF-04 | Sincronización explícita (botón) y reintento automático al recuperar red. |
| RNF-05 | Al menos 2 APIs nativas (cámara/QR, notificaciones, estado de red). |
| RNF-06 | Pruebas unitarias + cobertura mínima 30 % verificable (`./gradlew jacocoTestCoverageVerification`). |
| RNF-07 | Documentación C4 y alcance. |
| RNF-08 | Coherencia entre docs e implementación. |

## 3. Casos de uso principales

### CU-01 Login
- Actor: Estudiante, Docente, Voluntario o Administrador.
- Flujo: ingresar credenciales → `POST /api/auth/login` (el backend autentica contra Cognito) → guardar token en DataStore → navegar al inicio según el rol: Docente, Voluntario y Administrador al inicio docente; Estudiante al inicio de estudiante.
- Alternativo: campos vacíos o credenciales inválidas → mensaje de error, sin navegar.

### CU-02 Crear clase (docente, modelo local)
- Actor: Docente, Voluntario o Administrador (inicio docente).
- Flujo: nombre, descripción, cuestionario JSON, estado → Room → listado filtrable por estado (Todos, En curso, Finalizados).
- Nota: las clases son del **modelo local** del teléfono (no se envían al backend). Lo que se sincroniza con el backend son las asistencias y encuestas del catálogo SyncPass (CU-04).

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
| Roles de la rúbrica y navegación por rol | Implementado |
| Clases (CRUD local) | Implementado |
| Asistencia local | Implementado |
| Cámara/QR | Implementado |
| Notificaciones | Parcial |
| Offline sync (infra + botón) | Implementado |
| Registro offline con catálogo remoto + botón/reintento automático | Implementado (no reemplaza el modelo local de clases; sin migración Room) |
| Descarga de cursos del servidor (web → app) | Implementado (botón "Descargar" en Panel Docente) |
| Visualización de cursos remotos con detalles | Implementado (título, descripción, área, fechas, estado) |
| Crear curso en servidor desde la app | Implementado (botón "Crear", requiere rol ADMINISTRADOR o COORDINADOR) |
| Vincular clase local con evento remoto | Implementado (dropdown en DialogCrearClase) |
| Geolocalización con coordenada predeterminada | Implementado (Santiago; cálculo Haversine; estado VALIDO/FUERA_DE_RADIO) |
| Geolocalización con GPS real | Backlog (requiere Play Services; no cached offline) |
| C4 / alcance / mockups | Implementado (este directorio) |
| Tests + JaCoCo ≥30 % | Implementado: 84 pruebas JVM, **50,0 % de líneas** (`./gradlew jacocoTestReport`) |

## 6. Equivalencia de roles (rúbrica ↔ Cognito ↔ app)

| Perfil (rúbrica) | Grupo Cognito / backend | `Rol` en la app | Inicio en la app |
|---|---|---|---|
| Estudiante | `BENEFICIARIO` | `ESTUDIANTE` | Inicio estudiante |
| Docente | `COORDINADOR` | `DOCENTE` | Inicio docente |
| Voluntario | `VOLUNTARIO` | `VOLUNTARIO` | Inicio docente |
| Administrador | `ADMINISTRADOR` | `ADMINISTRADOR` | Inicio docente |

`TipoParticipante.ESTUDIANTE/DOCENTE` del backend clasifica asistentes, no es autorización Cognito.
