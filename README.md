# SyncPass Android

Aplicación Android nativa (Kotlin, Jetpack Compose, Hilt, Room, Retrofit) para el registro de asistencia en actividades comunitarias. Funciona **offline-first**: las asistencias y encuestas se guardan localmente y se sincronizan cuando vuelve la conexión.

> **Arquitectura (C4):** [`docs/ARQUITECTURA.md`](docs/ARQUITECTURA.md)  
> **Alcance (RF/RNF, casos de uso, backlog):** [`docs/ALCANCE.md`](docs/ALCANCE.md)  
> **Mockups y trazabilidad:** [`docs/MOCKUPS.md`](docs/MOCKUPS.md)  
> **Estado frente a la rúbrica:** [`docs/ESTADO_EVA3.md`](docs/ESTADO_EVA3.md)

## Requisitos

- Android Studio (incluye un JDK compatible con Gradle).
- Android SDK Platform 35 y las herramientas SDK que Android Studio solicite.
- Emulador Android o dispositivo físico con Android 8.1 (API 27) o posterior.
- Conexión a Internet para descargar dependencias Gradle y usar los servicios remotos.

No es necesario instalar Gradle globalmente: el proyecto incluye `gradlew.bat`.

## Abrir, compilar y ejecutar

1. Clona el repositorio y abre **la carpeta raíz** `SyncPassApp` en Android Studio.
2. Espera a que termine **Gradle Sync**. Acepta instalar el SDK o las herramientas que Android Studio indique.
3. Inicia un emulador desde **Device Manager**, o conecta un teléfono con depuración USB activada.
4. Selecciona la configuración `app` y la variante **debug** y pulsa **Run**.

También puedes hacerlo desde PowerShell, en la carpeta raíz del proyecto:

```powershell
.\gradlew.bat :app:installDebug
```

Para compilar el APK release:

```powershell
.\gradlew.bat :app:assembleRelease
```

El APK se genera en `app\build\outputs\apk\release\app-release.apk`. El APK release no queda firmado para distribución en una tienda; para publicarlo, configura una firma de release local y segura.

Para ejecutar las pruebas unitarias:

```powershell
.\gradlew.bat :app:testDebugUnitTest
```

Para generar el reporte de cobertura (JaCoCo):

```powershell
.\gradlew.bat jacocoTestReport
# Reporte HTML en: app\build\reports\jacoco\jacocoTestReport\html\index.html
.\gradlew.bat jacocoTestCoverageVerification
# Falla si la cobertura de líneas baja del 30 %
```

## Servidor configurado y ambientes

La URL base se configura en `app/build.gradle.kts`:

- **Debug y Release:** `https://fullstack2-proyecto-kt.vercel.app/` (Vercel reenvía `/api/*` al backend en EC2).

La aplicación agrega las rutas `api/...`; por ejemplo, el health check es `https://fullstack2-proyecto-kt.vercel.app/api/health`. No agregues `/api` a `API_BASE_URL`.

Para verificar que la API está disponible:

```powershell
Invoke-RestMethod "https://fullstack2-proyecto-kt.vercel.app/api/health"
```

Debe responder con estado `UP`.

### Ejecutar contra un backend local

Si la EC2 o Internet no están disponibles, puedes ejecutar el backend en tu PC. En la variante **debug**, cambia temporalmente el `API_BASE_URL` dentro del bloque `debug` de `app/build.gradle.kts` a:

```kotlin
buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2:8080/\"")
```

`10.0.2.2` es la dirección especial para que el **emulador Android** alcance el `localhost` de la PC. En un teléfono físico, reemplázala por la IP local de tu PC, por ejemplo `http://192.168.1.20:8080/`, y asegúrate de que ambos dispositivos estén en la misma red.

Después de cambiar la URL, sincroniza Gradle y vuelve a instalar debug:

```powershell
.\gradlew.bat :app:installDebug
```

## Nuevas funcionalidades (iteración 3)

- **Descarga de cursos del servidor:** botón "Descargar" en Panel Docente trae los cursos creados en la web (admin) y los muestra con detalles (título, descripción, área, fechas, estado).
- **Crear curso en servidor:** botón "Crear" en "Cursos del servidor" abre un formulario para crear cursos directamente en el backend (requiere rol ADMINISTRADOR o COORDINADOR).
- **Vincular clase local con evento remoto:** al crear una clase local, se puede seleccionar un evento remoto del catálogo (dropdown). Al marcar asistencia, también se registra en `asistencias_offline` para sincronizar.
- **Geolocalización con coordenada predeterminada:** usa Santiago (-33.4489, -70.6693) como coordenada fija. Calcula distancia Haversine al evento y determina estado (VALIDO/FUERA_DE_RADIO). Permisos declarados en manifest.
- **UI rediseñada:** logo SyncPass con gradiente, paleta de colores pasteles (verde salvia, menta, crema), bordes redondeados (16dp), tarjetas con elevación suave, secciones con encabezados y líneas decorativas, diálogo de detalle de curso estructurado.
- **Eliminados botones demo** (Demo Profesor / Demo Alumno) del login.
- **URL del backend:** ahora apunta a Vercel (`https://fullstack2-proyecto-kt.vercel.app/`) que reenvía a EC2, eliminando la dependencia directa de duckdns (bloqueado por FortiGuard).

## Qué funciona sin conexión:

- **Captura offline implementada:** `DialogRegistrarAsistenciaOffline` permite elegir evento y participante del catálogo descargado y guarda la asistencia en Room con `sincronizado=false` (`SyncRepository` → `SyncPassDao`). El cuestionario también se guarda localmente.
- **Sincronización explícita:** el botón "Sincronizar pendientes" de `ProfileScreen` llama a `SyncViewModel` → `SyncRepository`, que envía lotes de hasta 500 registros a `POST /api/sync/asistencias` y `/api/sync/encuestas`. Los UUID se generan en el dispositivo y el backend marca duplicados, por lo que reintentar no duplica asistencias.
- **Reintento automático:** `SyncPassApplication` registra un `ConnectivityManager.NetworkCallback` y vuelve a sincronizar cuando vuelve la red (mientras el proceso está vivo; no hay WorkManager para app cerrada).
- Para **iniciar sesión y descargar el catálogo** sí se necesita conectividad: hazlo una vez con Internet antes de entrar en modo avión.
- No desinstales ni borres los datos de la app si necesitas conservar datos locales; eso elimina la base Room del dispositivo.

## Seguridad y configuración

- No guardes contraseñas, credenciales temporales de AWS, claves `.pem` ni contraseñas de Supabase en el repositorio.
- La autenticación móvil se realiza mediante `POST /api/auth/login`; los tokens se almacenan en DataStore y se agregan automáticamente a las solicitudes protegidas.
- El logging HTTP está limitado a nivel `BASIC` en debug y deshabilitado en release. No registra cuerpos de login ni tokens.
- HTTP sin cifrar está permitido solo en el manifiesto debug para el backend local. Release requiere HTTPS.

## Pruebas y cobertura

- **84 pruebas JVM** con fakes de DAOs, APIs, sesión y notificaciones (sin emulador).
- Cobertura con el plugin `jacoco` de Gradle, portable (sin rutas absolutas):
  - `./gradlew jacocoTestReport` → `app/build/reports/jacoco/jacocoTestReport/html/index.html`
  - `./gradlew jacocoTestCoverageVerification` → falla si las líneas cubiertas bajan del 30 %.
- Resultado: **50,0 % de líneas**, 44,7 % de instrucciones, 30,6 % de ramas.
- Se excluye código generado (Hilt, Room `*_Impl`, `BuildConfig`, `R`) y la UI de Compose (`*Screen*`, diálogos de `feature/*/ui`, `core/designsystem`, temas), que no es lógica de negocio testeable en la JVM.

## Repositorios relacionados

- Backend: [monolito_backend_syncpass](https://github.com/feliignaciodev/monolito_backend_syncpass)
- Frontend: [fullstack2-proyecto-kt](https://github.com/BastianGomez-Dc/fullstack2-proyecto-kt)
- Producción: https://fullstack2-proyecto-kt.vercel.app
- Backend API: https://syncpass.duckdns.org
