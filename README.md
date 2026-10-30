# SyncPass Android

Aplicación Android nativa hecha con Kotlin, Jetpack Compose, Retrofit, Room y Hilt. La app usa el backend SyncPass para autenticación, catálogo y sincronización; las asistencias y encuestas pendientes se guardan localmente para poder enviarlas cuando vuelva la conexión.

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

## Servidor configurado y ambientes

La URL base actual se configura en `app/build.gradle.kts`:

- **Debug:** `https://syncpass.duckdns.org/`
- **Release:** `https://syncpass.duckdns.org/`

La aplicación agrega las rutas `api/...`; por ejemplo, el health check es `https://syncpass.duckdns.org/api/health`. No agregues `/api` a `API_BASE_URL`.

Para verificar que la API está disponible, abre esta dirección en un navegador o ejecútala desde PowerShell:

```powershell
Invoke-RestMethod "https://syncpass.duckdns.org/api/health"
```

Debe responder con estado `UP`. El login requiere que el backend y Cognito estén disponibles; descargar el catálogo y sincronizar datos también requiere el backend y la base de datos configurados.

### Ejecutar contra un backend local

Si la EC2 o Internet no están disponibles, puedes ejecutar el backend en tu PC, siempre que tengas sus dependencias y variables de entorno configuradas. En la variante **debug**, cambia temporalmente el `API_BASE_URL` dentro del bloque `debug` de `app/build.gradle.kts` a:

```kotlin
buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2:8080/\"")
```

`10.0.2.2` es la dirección especial para que el **emulador Android** alcance el `localhost` de la PC. En un teléfono físico, reemplázala por la IP local de tu PC, por ejemplo `http://192.168.1.20:8080/`, y asegúrate de que ambos dispositivos estén en la misma red.

Después de cambiar la URL, sincroniza Gradle y vuelve a instalar debug:

```powershell
.\gradlew.bat :app:installDebug
```

El manifiesto de debug permite HTTP para esta prueba local. **No cambies la URL de release a HTTP**: release bloquea tráfico HTTP sin cifrar. Antes de volver a usar la EC2, restaura la URL HTTPS de debug y recompila/reinstala.

## Si el backend remoto deja de responder

1. Comprueba `https://syncpass.duckdns.org/api/health`.
2. Si no responde, verifica en AWS que el Learner Lab esté iniciado y que la instancia EC2 esté en estado **Running**. En el entorno de laboratorio, al terminar/caducar el lab la instancia puede detenerse; al volver a iniciar el lab, espera a que la instancia arranque y revisa de nuevo el health check.
3. Si la instancia está encendida pero la API no responde, revisa el servicio backend en la EC2:

   ```bash
   sudo systemctl status syncpass
   sudo systemctl restart syncpass
   sudo systemctl status nginx
   sudo nginx -t
   ```

   Si el servicio no inicia, revisa sus logs localmente con `sudo journalctl -u syncpass -n 100 --no-pager`. **No publiques ni compartas logs que puedan contener datos sensibles.**

4. Si Nginx está activo pero HTTPS o el dominio no responden, verifica el DNS, la Elastic IP y las reglas de entrada 80/443 del Security Group. No abras el puerto 8080 a Internet.
5. Si el backend reporta problemas de base de datos, revisa que el proyecto Supabase esté activo y que el Session pooler, SSL y parámetros de Parameter Store sigan disponibles.
6. Si la API vuelve a responder pero la app sigue fallando, ciérrala y vuelve a abrirla. Reintenta la descarga/sincronización cuando haya conexión y una sesión válida.

Reiniciar la EC2 no debería requerir recompilar ni reinstalar la app si se conserva el dominio y la Elastic IP. Si el dominio o la URL de la API cambian, actualiza ambos valores `API_BASE_URL` en `app/build.gradle.kts` y genera un nuevo build.

## Qué funciona sin conexión

- Room y `SyncRepository` incluyen almacenamiento y métodos para asistencias/encuestas pendientes. Sin embargo, en el código actual no encontré pantallas o tareas que invoquen esos métodos; **no asumas que la captura offline o el envío automático al recuperar conexión estén completos**.
- Para iniciar sesión, obtener el catálogo y comunicarse con el backend se necesita conectividad. Antes de usar la app en una actividad sin Internet, verifica el flujo offline en el dispositivo y confirma cómo se conservarán y enviarán los registros.
- No desinstales ni borres los datos de la app si necesitas conservar datos locales; eso elimina la base Room del dispositivo.

## Seguridad y configuración

- No guardes contraseñas, credenciales temporales de AWS, claves `.pem` ni contraseñas de Supabase en el repositorio.
- La autenticación móvil se realiza mediante `POST /api/auth/login`; los tokens se almacenan en DataStore y se agregan automáticamente a las solicitudes protegidas.
- El logging HTTP está limitado a nivel `BASIC` en debug y deshabilitado en release. No registra cuerpos de login ni tokens.
- HTTP sin cifrar está permitido solo en el manifiesto debug para el backend local. Release requiere HTTPS.

## Repositorios relacionados

- Backend: [monolito_backend_syncpass](https://github.com/feliignaciodev/monolito_backend_syncpass)
- Frontend: [fullstack2-proyecto-kt](https://github.com/BastianGomez-Dc/fullstack2-proyecto-kt)
