# Estado de Avance — Rúbrica EVA3 (SyncPass)

> Documento corregido para reflejar el estado real del código (no sobrestimar).

## 1. Lo que YA está realizado

### A. Infraestructura y Backend (código preparado; nube NO verificada en esta entrega)
- [x] Backend Spring Boot + Flyway + PostgreSQL (Supabase) preparado.
- [x] Autenticación con Cognito (access token, grupos/roles).
- [x] Control de roles: `ADMINISTRADOR`, `COORDINADOR`, `VOLUNTARIO`, `BENEFICIARIO` (equivalencias con la rúbrica documentadas en `ALCANCE.md`).
- [ ] Despliegue AWS EC2 verificado actualmente (scripts listos, disponibilidad no comprobada).
- [ ] API Gateway: **no desplegado** (nginx actúa como reverse proxy).

### B. Aplicación Móvil Android
- [x] Arquitectura MVVM en capas (Screens → ViewModels → Repositories → Room/Retrofit).
- [x] Persistencia local Room + offline-first (asistencias/encuestas pendientes).
- [x] Sincronización explícita con botón (`SyncViewModel` + sección en Perfil).
- [x] Descarga de catálogo remoto.
- [x] APIs nativas: cámara/QR (CameraX + ML Kit + ZXing) y notificaciones (ntfy/FCM parcial).
- [ ] Geolocalización: **NO implementada** (declarada erróneamente como completa anteriormente).

### C. Documentación
- [x] `ARQUITECTURA.md` con C1/C2 (real, sin API Gateway)/C3.
- [x] `ALCANCE.md` con RF/RNF, casos de uso, flujos, matriz, equivalencia de roles, backlog.
- [x] `MOCKUPS.md` con trazabilidad de las 9 vistas.
- [ ] `NOTIFICACIONES_NTFY.md`: restos `EOF`/`echo done` y nombres `Disco`/`com.rojas.disco` deben eliminarse (pendiente).

## 2. Pendiente / Backlog
- [ ] Conectar la captura de asistencia de `SessionScreen` con `SyncRepository` sin migración de Room, o migración controlada para `remoteEventoId`/`remoteParticipanteId`.
- [ ] Tests adicionales para alcanzar ≥30% de cobertura verificable (actualmente ~9.8% líneas sobre el módulo completo; tests enfocados en repositorios/lógica pura).
- [ ] Verificación real de Supabase/EC2/Cognito y E2E Android.
- [ ] Notificaciones: historial persistido, deduplicación de recordatorios, credenciales FCM.
- [ ] Refresh de token automático (actualmente `TokenAuthenticator` borra tokens ante 401).

## 3. Resumen

| Componente | Estado |
|---|---|
| Backend & Cloud (código) | Preparado (nube no verificada) |
| App Android (Compose, MVVM, Offline) | Implementado |
| APIs nativas (Cámara/QR, Notificaciones) | Implementado (2 APIs) |
| Geolocalización | No implementado |
| Documentación C4 / Alcance / Mockups | Implementado |
| Testing & Coverage | Parcial (36 tests JVM) |
| Defensa individual | Pendiente |
