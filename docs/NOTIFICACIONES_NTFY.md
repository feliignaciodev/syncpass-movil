# Notificaciones push con ntfy — SyncPass (App Android)

La app recibe notificaciones push sin Firebase, usando **ntfy.sh**: se suscribe
por **Server-Sent Events (SSE)** a un tópico secreto derivado del email del usuario.

> El backend SyncPass **no publica en ntfy**. Tiene su propio módulo de
> notificaciones por FCM (`/api/notifications`), que esta app todavía no integra
> (backlog). Hoy los mensajes a ntfy se publican directamente en el tópico.

## Cómo se deriva el tópico

`NtfyTopics.generate(email)`:

- SHA-256 del email normalizado (`trim().lowercase()`).
- Prefijo `app_vibe_`.
- Primeros 20 caracteres hexadecimales del hash.

Ejemplo: `docente.dev@gmail.com` → `app_vibe_8e63635b0c5401804b4b`
(probado en `NtfyTopicsTest`).

## Flujo completo

1. El usuario inicia sesión. `LoginViewModel` (o `SesionViewModel`, al reabrir la
   app con sesión válida) pide `NotificacionesRepository.activarPara(email)`.
2. `NotificacionesRepositoryImpl` arranca `NotificationService` con ese email.
3. El servicio se declara en primer plano (`startForeground`) con un canal de
   baja prioridad y se conecta a `https://ntfy.sh/<tópico>/sse`; si se corta,
   se reconecta.
4. Al publicar un mensaje en ese tópico, ntfy lo envía por SSE y la app muestra
   una notificación de alta prioridad.
5. Al cerrar sesión (`ProfileViewModel`) o si la sesión ya no es válida
   (`SesionViewModel`), se pide `NotificacionesRepository.desactivar()` y el
   servicio se detiene.

## Cómo enviar una notificación de prueba

```bash
curl -H "Title: Prueba SyncPass" \
     -H "Priority: high" \
     -d "Hola, notificación de prueba" \
     https://ntfy.sh/app_vibe_8e63635b0c5401804b4b
```

Reemplazar el tópico por el del email con el que se inició sesión.

## Requisitos para que se vea en la app

- La app debe tener sesión iniciada con el **mismo email** del tópico.
- El permiso `POST_NOTIFICATIONS` debe estar concedido (Android 13+). Si está
  denegado, el servicio corre pero no se muestran notificaciones.
- El emulador o dispositivo debe tener internet (ntfy.sh).

## Archivos clave

- `app/src/main/java/com/example/syncpassapp/feature/notificaciones/NtfyTopics.kt`
- `app/src/main/java/com/example/syncpassapp/feature/notificaciones/NotificationService.kt`
- `app/src/main/java/com/example/syncpassapp/feature/notificaciones/data/NotificacionesRepository.kt`
- `app/src/main/AndroidManifest.xml` (permisos y servicio)
- `app/build.gradle.kts` (`NTFY_BASE_URL`)
