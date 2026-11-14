# Estado de Avance – Rúbrica EVA3 (SyncPass)

Este documento detalla el estado actual de implementación del proyecto **SyncPass** en comparación con los requerimientos y la rúbrica establecida en [EVA3.md](file:///C:/Src/syncpass-movil/docs/EVA3.md).

---

## ✅ 1. Lo que YA está realizado (Implementado)

### A. Infraestructura y Backend (Conexión Cloud)
- [x] **Backend Spring Boot & Base de Datos:** API desplegada en AWS EC2 conectada a Supabase.
- [x] **Autenticación JWT:** Implementación completa con `AuthApi`, `TokenInterceptor`, `TokenAuthenticator` y almacenamiento seguro en `TokenDataStore`.
- [x] **Control de Roles:** Soporte para perfiles de Estudiante, Docente y Administrador.

### B. Aplicación Móvil Android (Arquitectura y Offline-First)
- [x] **Arquitectura MVVM en Capas:**
  - **Model:** Entidades Room (`ProyectoEntity`, `ClaseEntity`, `AsistenciaEntity`, etc.), DAOs (`SyncPassDao`, `UniversidadDao`), Repositorios (`SyncRepository`, `UniversidadRepository`) y DTOs de red.
  - **ViewModel:** Lógica de negocio expuesta mediante `StateFlow` (`TeacherHomeViewModel`, `StudentHomeViewModel`, `SessionViewModel`, `LoginViewModel`, etc.).
  - **View:** Pantallas desarrolladas en **Jetpack Compose** con Material 3 y componentes reutilizables (`TarjetaClase`, `EstadoVacio`, `PantallaBase`).
- [x] **Persistencia Local y Sincronización Offline-First:**
  - Base de datos local Room completamente operativa para operar sin conexión.
  - Mecanismos de sincronización en `SyncRepository` para descargar el catálogo (`GET /api/sync/catalogo`) y enviar asistencias y encuestas pendientes en lote.
- [x] **APIs Nativas:**
  - **Cámara / Escáner QR:** Implementado mediante `CodigoQr` y diálogos de escaneo para verificación de clases y asistencia.
  - **Geolocalización:** Validación de distancia y ubicación del estudiante en el registro de asistencias.
  - **Notificaciones:** Servicio de notificaciones push (`NotificationService`, `NtfyTopics`).

### C. Documentación y Arquitectura (`/docs`)
- [x] **`ARQUITECTURA.md`:** Modelado completo mediante diagramas C4 (Niveles C1 Contexto, C2 Contenedores y C3 Componentes).
- [x] **`REQUERIMIENTOS.md`:** Levantamiento funcional y no funcional del sistema.
- [x] **`NOTIFICACIONES_NTFY.md`:** Documentación de integración de notificaciones.

---

## ⏳ 2. Lo que FALTA por realizar / Pendiente (Backlog EVA3)

### A. Cobertura de Pruebas Unitarias (Code Coverage ≥ 30%)
- [ ] **Tests Unitarios en ViewModels:** Ampliar los tests actuales (`ExampleUnitTest`) para cubrir la lógica de negocio en ViewModels clave (`TeacherHomeViewModel`, `StudentHomeViewModel`, `SessionViewModel`, `LoginViewModel`).
- [ ] **Tests Unitarios en Repositorios:** Añadir pruebas unitarias para mockear respuestas de red y verificar el almacenamiento en Room / Sincronización Offline.
- [ ] **Verificación de Cobertura:** Ejecutar tareas de Gradle para validar que se alcance o supere el **30% de cobertura** requerido por la rúbrica (ej. JaCoCo).

### B. Consolidación de Documentación Funcional (`ALCANCE.md`)
- [ ] Aunque [`REQUERIMIENTOS.md`](file:///C:/Src/syncpass-movil/docs/REQUERIMIENTOS.md) cubre los requerimientos, asegurar que se cuenta con el archivo específico **`ALCANCE.md`** tal como lo menciona explícitamente el punto 1.2.A de la rúbrica (incluyendo RF, RNF, casos de uso detallados y flujos principales).

### C. Validación y Pruebas End-to-End
- [ ] Realizar pruebas de integración ejecutando la app conectada al backend en AWS EC2 / Supabase para verificar flujos completos (Login -> Sincronización de Cursos/Catálogo -> Registro de Asistencia QR -> Sincronización Offline).

---

## 📊 Resumen de Estado

| Componente EVA3 | Estado | Notas |
| :--- | :---: | :--- |
| **Backend & Cloud (AWS/Supabase/Spring Boot)** | 🟢 Completado | API y base de datos operativas. |
| **App Android (Jetpack Compose, MVVM, Offline)** | 🟢 Completado | Arquitectura y flujos principales implementados. |
| **APIs Nativas (Cámara/QR, Geo, Notificaciones)** | 🟢 Completado | Integradas en la app. |
| **Documentación C4 (`ARQUITECTURA.md`)** | 🟢 Completado | Modelado estructurado. |
| **Documentación Funcional (`REQUERIMIENTOS.md`)** | 🟢 Completado | Faltaría revisar si se requiere separar como `ALCANCE.md`. |
| **Testing & Code Coverage (≥ 30%)** | 🟡 En Progreso | Requiere ampliar tests unitarios en ViewModels/Repositorios. |
| **Defensa Individual** | ⏳ Pendiente | Preparar preguntas técnicas y explicación de arquitectura. |
