# Arquitectura — SyncPass (App Android)

> Documento vivo. Los diagramas son Mermaid. Las afirmaciones sobre nube se basan
> en el código y scripts del repositorio backend (`C:\Src\monolito_backend_syncpass`);
> la disponibilidad actual de Supabase/AWS EC2 **no está verificada** en esta entrega.

## 1. Contexto (C1)

```mermaid
flowchart LR
    Estudiante[Estudiante / Voluntario]
    Docente[Docente / Coordinador]
    Admin[Administrador]
    App[App SyncPass (Android, Kotlin)]
    Backend[API SyncPass (Spring Boot en AWS EC2)]
    Cognito[AWS Cognito (identidad)]
    Supabase[(Supabase / PostgreSQL)]
    Ntfy[ntfy (notificaciones push)]
    FCM[Firebase Cloud Messaging]

    Estudiante --> App
    Docente --> App
    Admin --> App
    App -->|HTTPS /api/*| Backend
    App -->|login SRP| Cognito
    Backend -->|JWKS / grupos| Cognito
    Backend -->|JDBC / Flyway| Supabase
    Backend -->|push| FCM
    Backend -->|SSE topic| Ntfy
    App -->|escucha SSE| Ntfy
```

**Actores y servicios externos:**
- **Estudiante / Voluntario / Docente / Administrador:** usuarios de la app móvil.
- **AWS Cognito:** gestiona credenciales y emite access tokens (grupos `ADMINISTRADOR`, `COORDINADOR`, `VOLUNTARIO`, `BENEFICIARIO`).
- **AWS EC2 + Spring Boot:** API REST desplegada tras nginx (reverse proxy + HTTPS). **No hay AWS API Gateway desplegado**; nginx cumple el rol de proxy.
- **Supabase (PostgreSQL):** base de datos relacional (Session Pooler). Conexión depende de variables de entorno; no verificada en esta entrega.
- **ntfy / FCM:** canales de notificación (FCM parcial en backend).

## 2. Contenedores (C2)

```mermaid
flowchart LR
    subgraph Dispositivo
        App[App Android\n(Kotlin, Compose, Hilt)]
        Room[(Room / SQLite\noffline)]
        DS[(DataStore\nTokens)]
    end
    subgraph Nube
        Nginx[nginx (EC2)]
        Spring[Spring Boot\n(JAR, systemd)]
        Cognito[AWS Cognito]
        Supa[(Supabase\nPostgreSQL)]
        Param[(AWS Parameter Store)]
    end

    App -->|HTTPS /api/sync/catalogo\n/api/sync/asistencias\n/api/sync/encuestas| Nginx
    App -->|/api/auth/login| Nginx
    Nginx --> Spring
    Spring -->|valida JWT| Cognito
    Spring -->|JDBC + Flyway| Supa
    Spring -->|lee secretos| Param
    App -->|DataStore| DS
    App -->|persistencia local| Room
```

**Comunicación y flujo de datos:**
1. La app se autentica contra Cognito y guarda el access token en DataStore.
2. Retrofit adjunta `Authorization: Bearer <token>` a cada request.
3. El catálogo (`/api/sync/catalogo`) se descarga a Room para operar offline.
4. Las asistencias/encuestas se guardan en `asistencias_offline` / `encuestas_offline` con `sincronizado = false`.
5. Al recuperar conexión, el usuario pulsa **"Sincronizar pendientes"** y se envían lotes (máx. 500) a `/api/sync/asistencias` y `/api/sync/encuestas`. El servidor responde con aceptados/duplicados/rechazados e idempotencia por UUID.

## 3. Componentes (C3) — MVVM

```mermaid
flowchart TB
    subgraph View
        Screens[Screens (Compose)]
        Components[Components (designsystem)]
    end
    subgraph ViewModel
        VMs[ViewModels (@HiltViewModel, StateFlow)]
    end
    subgraph Model
        Repo[Repositories]
        Local[Local: Room DAOs / DataStore]
        Remote[Remote: Retrofit APIs]
    end

    Screens --> VMs
    VMs --> Repo
    Repo --> Local
    Repo --> Remote
```

**Capas reales del proyecto:**
- `core/database/local` → DAOs (`UniversidadDao`, `SyncPassDao`) y entidades Room.
- `core/database/repository` → `UniversidadRepository`, `SyncRepository`.
- `core/network/remote` → interfaces Retrofit (`AuthApi`, `SyncApi`, `EventoApi`, `UsuarioApi`) + DTOs.
- `core/datastore` → `TokenDataStore`.
- `feature/*/ui` → Screens + ViewModels.
- `core/designsystem` → componentes Compose reutilizables y tema.

**Reglas respetadas:**
- Las Screens no llaman Retrofit/DAO directamente; usan `hiltViewModel()` y `collectAsState()`.
- Los ViewModels interactúan exclusivamente con Repositories.
- La lógica de negocio (inscripción, presencia, respuestas) vive en `UniversidadRepository`.

**Detalles conocidos (no bloqueantes):**
- No existe un paquete `domain` ni una clase llamada `Datasource`; la función de datasource está repartida entre DAOs y Retrofit, que son interfaces.
- `SessionScreen`/`StudentHomeScreen` validan el QR en la propia Screen (mejorable).
- `TokenAuthenticator` **no** renueva tokens automáticamente: ante un 401 borra tokens y devuelve `null`. La documentación previa que afirmaba refresh automático es incorrecta.
- Hay dos paquetes de tema: `ui/theme` (usado por `MainActivity`) y `core/designsystem/theme`.

## 4. Flujos principales

### 4.1 Autenticación
Login → Cognito (SRP) → access token en DataStore → restauración de sesión con `fetchAuthSession` → claims JWT (nombre, email, rol).

### 4.2 Offline-first (asistencias)
1. Desde Perfil, descargar catálogo (`/api/sync/catalogo`) → Room con eventos y participantes remotos.
2. Desde **"Registrar asistencia offline"**, elegir evento y participante del catálogo y guardar en `asistencias_offline` con UUID y `sincronizado = false`. Como no hay geolocalización se usa `estadoValidacion = SIN_UBICACION` y coordenadas nulas (valores permitidos por el backend).
3. El usuario puede pulsar **"Sincronizar pendientes"**; además, `SyncPassApplication` reintenta automáticamente cuando `ConnectivityManager.NetworkCallback` detecta recuperación de una red con internet validado.
4. Se envían lotes a `/api/sync/asistencias` y `/api/sync/encuestas`. Los UUID aceptados y duplicados se marcan `sincronizado = true`; rechazados y errores de red permanecen pendientes para el siguiente reintento.

### 4.3 Sincronización explícita + automática
La manual está implementada en `feature/sync/ui/SyncViewModel` + sección en `ProfileScreen`. La automática se registra en `SyncPassApplication`, sin modificar el esquema Room y sin duplicar sincronizaciones simultáneas en el proceso de aplicación. WorkManager queda como mejora futura para sincronizar también con la app cerrada.

## 5. APIs nativas utilizadas (≥2)
1. **Cámara + escaneo QR:** CameraX + ML Kit Barcode + ZXing (permiso `CAMERA`, solicitud en runtime).
2. **Notificaciones + servicio foreground:** canales, `NotificationManager`, foreground service, SSE de ntfy, reconexión.

> **Geolocalización NO está implementada**, aunque `ESTADO_EVA3.md` la declaraba completa. Los campos `latitud`/`longitud`/`distanciaMetros` en DTOs/entidades no constituyen integración de geolocalización.

## 6. Estado de la nube (no verificado en esta entrega)
- Supabase/EC2/Cognito/Parameter Store/FCM: código y scripts preparados; **disponibilidad actual no verificada**.
- No hay API Gateway desplegado (nginx cumple el rol de proxy).
- No hay endpoint de refresh de token.
- Notificaciones: historial devuelve `[]`; recordatorios pueden duplicarse; envío a todos los dispositivos activos.
- Datos semilla (`syncpass.datos-semilla=true`) por defecto.
