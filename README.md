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

La especificación funcional se encuentra en [REQUIREMENTS.md](REQUIREMENTS.md) y el orden de implementación en [PLAN.md](PLAN.md).

## Requisitos de desarrollo

- Android Studio compatible con AGP 9.3;
- JDK 17;
- Android SDK 37.

El proyecto es offline-first y no requiere backend ni credenciales.
