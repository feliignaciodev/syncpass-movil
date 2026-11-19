# Documentación — SyncPass (App Android)

| Documento | Para qué sirve |
|---|---|
| [ARQUITECTURA.md](ARQUITECTURA.md) | Modelo C4 (C1 contexto, C2 contenedores, C3 componentes MVVM), roles, flujos de autenticación y sincronización offline, APIs nativas y cobertura. |
| [ALCANCE.md](ALCANCE.md) | Requerimientos funcionales y no funcionales, casos de uso, flujos y estado implementado / backlog. |
| [MOCKUPS.md](MOCKUPS.md) | Wireframes de todas las vistas y diálogos, flujo de navegación y trazabilidad vista → requisito → estado. |
| [NOTIFICACIONES_NTFY.md](NOTIFICACIONES_NTFY.md) | Cómo funcionan las notificaciones push con ntfy y cómo probarlas. |
| [ESTADO_EVA3.md](ESTADO_EVA3.md) | Estado de avance frente a la rúbrica. |
| [EVA3.md](EVA3.md) | Rúbrica de la evaluación. |
| [REQUERIMIENTOS.md](REQUERIMIENTOS.md) | Caso de estudio base de la asignatura ("Disco": clases, asistencia y cuestionario con QR) del que partió el modelo local de clases. Referencia histórica; el alcance actual está en `ALCANCE.md`. |
| [../CHANGELOG.md](../CHANGELOG.md) | Registro de cambios por iteración. |

## Orden de lectura sugerido

1. **ALCANCE.md** para entender qué hace la app y qué queda pendiente.
2. **ARQUITECTURA.md** para entender cómo está construida.
3. **MOCKUPS.md** para ver cada pantalla y su estado.

## Pruebas y cobertura

```bash
./gradlew testDebugUnitTest                 # pruebas JVM
./gradlew jacocoTestReport                  # reporte en app/build/reports/jacoco/jacocoTestReport/html/index.html
./gradlew jacocoTestCoverageVerification    # falla si la cobertura de líneas baja del 30 %
```
