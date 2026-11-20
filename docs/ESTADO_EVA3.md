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
- [x] Arquitectura MVVM en capas (Screens → ViewModels → Repositories → DataSources → Room/DataStore/Retrofit). Los ViewModels solo dependen de repositorios.
- [x] Perfiles de la rúbrica (`Rol`: Estudiante, Docente, Voluntario, Administrador) con navegación por rol; Voluntario entra al inicio docente.
- [x] Persistencia local Room + offline-first (asistencias/encuestas pendientes).
- [x] Registro offline con evento/participante del catálogo y sincronización manual con botón (`SyncViewModel` + Perfil) y automática al recuperar red (`NetworkCallback` en la aplicación).
- [x] Descarga de catálogo remoto.
- [x] APIs nativas: cámara/QR (CameraX + ML Kit + ZXing) y notificaciones (ntfy/FCM parcial).
- [ ] Geolocalización: **NO implementada** (declarada erróneamente como completa anteriormente).

### C. Documentación
- [x] `ARQUITECTURA.md` con C1/C2 (real, sin API Gateway)/C3.
- [x] `ALCANCE.md` con RF/RNF, casos de uso, flujos, matriz, equivalencia de roles, backlog.
- [x] `MOCKUPS.md` con wireframes de las 10 vistas y 4 diálogos, flujo de navegación y trazabilidad vista → RF → estado.
- [x] `NOTIFICACIONES_NTFY.md` reescrito para SyncPass (sin restos `EOF`/`echo done` ni nombres del caso base) y sin el endpoint `/api/notificar`, que el backend SyncPass no tiene.
- [x] `REQUERIMIENTOS.md` marcado como caso de estudio base ("Disco"); el alcance vigente está en `ALCANCE.md`.

## 2. Pendiente / Backlog
- [ ] Integrar la sincronización remota con el flujo local de `SessionScreen` (hoy existe un registro offline real separado que usa el catálogo remoto desde Perfil; unificar ambos modelos sería una migración controlada).
- [x] Tests: 84 tests JVM, cobertura JaCoCo **50,0 % de líneas** (alcance: lógica de negocio; Compose/UI excluida). Reporte portable: `./gradlew jacocoTestReport` → `app/build/reports/jacoco/jacocoTestReport/html/index.html`; mínimo verificable con `./gradlew jacocoTestCoverageVerification`.
- [x] Verificación real de Supabase/EC2/Cognito y E2E Android (20-11-2026): health UP, login real OK, sync E2E OK (catálogo + asistencia aceptada + idempotencia).
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
| Testing & Coverage | Implementado (84 tests JVM, 50,0 % de líneas) |
| Defensa individual | Pendiente |
