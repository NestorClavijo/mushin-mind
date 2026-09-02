# Mind

Aplicación Android local para convertir tareas productivas en puntos consumibles y usar esos puntos para acceder conscientemente a aplicaciones distractoras.

## Estado

Desarrollo inicial:

- base Android con Kotlin y Jetpack Compose;
- navegación principal;
- sistema visual claro/oscuro;
- dependencias de Hilt, Room, DataStore y WorkManager;
- primer modelo de dominio para tareas, planes, puntos y XP;
- reglas unitarias de finalización de tareas.
- persistencia Room del ciclo diario y esquema versionado;
- preparación y confirmación del plan de mañana;
- activación, cierre y reconciliación idempotente de días lógicos;
- recordatorio nocturno opcional con DataStore y WorkManager.
- catálogo de aplicaciones instaladas con búsqueda, iconos y exclusión de Mind;
- reglas persistentes de sesión temporal, acceso hasta fin del día y tiempo acumulable;
- clasificación de cambios estrictos y permisivos con protección para futuras relajaciones;
- advertencias antes de restringir aplicaciones sensibles o críticas.
- motor transaccional de compra de acceso con protección frente a gasto concurrente;
- sesiones temporales, hasta fin del día y extensiones acumulables persistentes;
- evaluación de acceso, expiración por timestamp y finalización anticipada sin reembolso.
- protección mediante AccessibilityService orientada solo a cambios de ventana;
- shield de bloqueo con saldo, confirmación de compra y salida segura al Home;
- detección visible del estado del servicio desde Configuración.
- retos locales de aritmética y secuencias para proteger cambios permisivos;
- cambios de regla pendientes, reemplazables y cancelables con aplicación al día siguiente;
- aplicación idempotente de cambios pendientes durante la reconciliación diaria.
- desbloqueo de emergencia secundario con advertencia y confirmación mediante pulsación prolongada;
- sesiones de emergencia temporales con motivo opcional, penalización limitada al saldo y auditoría persistente;
- configuración local de duración y penalización de emergencia.
- resumen diario y semanal calculado desde tareas, transacciones, sesiones y emergencias;
- desglose semanal por aplicación e historiales recientes de actividad.
- sistema visual con tokens reutilizables de color, tipografía, espaciado, formas y elevación;
- temas claro, oscuro o del sistema con cuatro acentos persistentes y configurables.
- creación y finalización de tareas conectadas de extremo a extremo con saldo y XP persistentes;
- transacción Room atómica para acreditar cada tarea una sola vez y revertir fallos del ledger;
- cobertura de integración para Room, DataStore y solicitudes de WorkManager;
- pruebas de UI para tareas, reglas, apariencia y retos, más un checklist de release reproducible.

La especificación funcional se encuentra en [REQUIREMENTS.md](REQUIREMENTS.md) y el orden de implementación en [PLAN.md](PLAN.md).
La validación manual previa a una release se encuentra en [docs/RELEASE_CHECKLIST.md](docs/RELEASE_CHECKLIST.md).

## Requisitos de desarrollo

- Android Studio compatible con AGP 9.3;
- JDK 17;
- Android SDK 37.

El proyecto es offline-first y no requiere backend ni credenciales.

## Verificación

```powershell
.\gradlew.bat testDebugUnitTest compileDebugAndroidTestSources
.\gradlew.bat assembleDebug assembleRelease
.\gradlew.bat lintDebug
```

Las pruebas instrumentadas se ejecutan con `connectedDebugAndroidTest` cuando hay un dispositivo o emulador conectado.
