# PLAN.md

## 1. Objetivo

Este documento define el plan de implementación del proyecto Android.

Cada fase incluye:

- objetivo;
- alcance;
- módulos;
- entregables;
- pruebas;
- criterios de salida.

La prioridad es construir primero la lógica de dominio y después integrar el mecanismo de bloqueo, evitando desarrollar una interfaz grande sobre reglas todavía inestables.

---

# 2. Stack propuesto

```text
Lenguaje:             Kotlin
UI:                   Jetpack Compose
Navegación:           Navigation Compose
Arquitectura:         MVVM + capas Domain/Data/Platform
Asincronía:           Coroutines + Flow
Persistencia:         Room
Preferencias:         DataStore
DI:                   Hilt
Background:           WorkManager
Protección:           AccessibilityService
Uso/estadísticas:     UsageStatsManager cuando aplique
Testing:              JUnit + Kotlin test libs + Room tests + Compose UI tests
```

---

# 3. Estructura inicial de módulos/paquetes

Para un proyecto de portafolio no es necesario dividir desde el día uno en decenas de Gradle modules.

Se recomienda comenzar con un único `app` module bien separado por paquetes:

```text
app/
├── core/
│   ├── design/
│   ├── common/
│   ├── time/
│   └── testing/
│
├── data/
│   ├── local/
│   │   ├── db/
│   │   ├── dao/
│   │   └── entity/
│   ├── repository/
│   └── preferences/
│
├── domain/
│   ├── model/
│   ├── repository/
│   └── usecase/
│
├── platform/
│   ├── accessibility/
│   ├── usage/
│   ├── notifications/
│   └── workers/
│
├── feature/
│   ├── onboarding/
│   ├── home/
│   ├── planning/
│   ├── tasks/
│   ├── apps/
│   ├── shield/
│   ├── challenges/
│   ├── history/
│   ├── statistics/
│   └── settings/
│
└── navigation/
```

Si el proyecto crece, los `feature/*` podrán migrarse a módulos Gradle.

---

# 4. Convenciones de desarrollo

## 4.1 Lógica de dominio fuera de Compose

Un Composable nunca debe decidir:

- si alcanzan los puntos;
- cuánto cobrar;
- si una regla es permisiva;
- si una tarea ya acreditó;
- si una sesión expiró.

Debe delegar en casos de uso.

## 4.2 Tiempo abstraído

Crear:

```text
ClockProvider
```

para que las pruebas puedan controlar la hora.

No llamar directamente a `System.currentTimeMillis()` desde toda la aplicación.

## 4.3 Operaciones financieras atómicas

Tratar los puntos como un pequeño sistema contable.

## 4.4 Estados explícitos

Usar modelos de UI claramente definidos:

```text
Loading
Content
Empty
Error
PermissionRequired
```

## 4.5 Idempotencia

Cierre diario, aplicación de cambios pendientes y recompensas deben tolerar reintentos.

---

# 5. Fase 0 — Definición y bootstrap

## Objetivo

Crear una base de proyecto limpia y ejecutable.

## Desarrollo

- crear proyecto Android;
- Kotlin;
- Compose;
- Material 3;
- configurar minSdk/targetSdk según entorno de desarrollo;
- Hilt;
- Room;
- DataStore;
- WorkManager;
- navegación;
- estructura de paquetes;
- lint/formatting;
- build de debug;
- README inicial.

## Entregables

```text
App abre
Home placeholder
Navegación base
Build reproducible
```

## Pruebas

- build debug;
- test unitario de ejemplo;
- test Compose de arranque;
- comprobar ejecución en dispositivo físico.

## Criterio de salida

La app compila desde un clon limpio y abre sin errores.

---

# 6. Fase 1 — Dominio de puntos y tareas

## Objetivo

Construir el corazón económico antes del bloqueo.

## Modelos

```text
Task
DailyPlan
PointTransaction
UserProgress
LogicalDay
```

## Casos de uso

```text
CreateTask
UpdateTask
CompleteTask
SkipTask
CreateDailyPlan
ConfirmDailyPlan
GetCurrentBalance
GetDailyProgress
```

## Reglas

- recompensa una sola vez;
- saldo no negativo;
- XP separado;
- tareas añadidas durante el día no generan puntos en MVP;
- ledger inmutable.

## Persistencia

Crear tablas y DAOs iniciales.

## UI

- Inicio;
- lista de tareas;
- crear/editar tarea;
- saldo;
- plan del día.

## Pruebas unitarias obligatorias

### T-1
Completar una tarea de 20 genera exactamente +20.

### T-2
Completar dos veces no duplica.

### T-3
Omitir no recompensa.

### T-4
Tarea añadida durante el día no recompensa.

### T-5
XP aumenta y no depende del saldo.

## Pruebas Room

- transacción completar tarea;
- rollback ante excepción;
- relaciones plan/tareas.

## Criterio de salida

El usuario puede usar la app como gestor de tareas con economía de puntos aunque todavía no bloquee otras apps.

---

# 7. Fase 2 — Ciclo diario

## Objetivo

Introducir planificación previa y cierre lógico.

## Desarrollo

- estados de DailyPlan;
- plan de mañana;
- activación de plan;
- cierre diario;
- DailySummary;
- `ClockProvider`;
- reconciliación al abrir;
- WorkManager para trabajos oportunos;
- recordatorio de planificación.

## Casos de uso

```text
PrepareTomorrowPlan
ActivateCurrentPlan
CloseLogicalDay
ReconcileDays
GenerateDailySummary
```

## Casos límite

- app no abierta 3 días;
- teléfono reiniciado;
- trabajo de fondo retrasado;
- cambio de zona;
- ejecución duplicada del worker.

## Pruebas

### T-6
Cerrar dos veces el mismo día no duplica resumen.

### T-7
Un plan futuro se activa cuando corresponde.

### T-8
Una sesión diaria del día anterior no sobrevive.

### T-9
Reconciliar tres días produce estado correcto.

## Criterio de salida

El concepto de "día" es confiable sin depender de una ejecución exacta a medianoche.

---

# 8. Fase 3 — Catálogo de aplicaciones y reglas

## Objetivo

Permitir seleccionar y configurar apps distractoras.

## Desarrollo

- descubrimiento mediante PackageManager;
- iconos y nombres;
- búsqueda;
- RestrictedApp;
- AppRule;
- tipos de regla;
- validadores;
- pantalla de detalle.

## Casos de uso

```text
GetInstalledApps
EnableRestriction
DisableRestriction
CreateAppRule
UpdateAppRule
CompareRuleStrictness
```

## UI

### Lista de aplicaciones

```text
Buscar…
Instagram       Restringida
TFT             Restringida
WhatsApp        Libre
```

### Detalle

```text
Instagram

Tipo
Sesión temporal

Costo
30 pts

Duración
20 min
```

## Pruebas

- regla inválida rechazada;
- costo cero rechazado;
- duración fuera de rango;
- propia app excluida;
- comparación de reglas.

## Criterio de salida

Todas las reglas pueden configurarse y persistirse, aunque todavía no se ejecuten sobre otras apps.

---

# 9. Fase 4 — Motor de sesiones y compras

## Objetivo

Construir toda la lógica de "pagar para acceder" antes de conectarla al AccessibilityService.

## Modelos

```text
UnlockSession
AccessDecision
```

## Casos de uso

```text
CanUnlockApp
PurchaseUnlock
GetActiveSession
ExtendSession
ExpireSessions
EndSessionEarly
```

## Resultado esperado de evaluación

```kotlin
sealed interface AccessDecision {
    data object Allowed : AccessDecision
    data class BlockedInsufficientPoints(...) : AccessDecision
    data class BlockedPurchasable(...) : AccessDecision
}
```

## Transacción crítica

```text
LOCK/TRANSACTION
↓
leer saldo
↓
validar costo
↓
crear débito
↓
crear sesión
↓
commit
```

## Pruebas

### T-10
Saldo 40, costo 30 → saldo 10 + sesión.

### T-11
Saldo 20, costo 30 → sin débito + sin sesión.

### T-12
Dos compras concurrentes con saldo insuficiente para ambas → solo una puede confirmarse.

### T-13
Expiración por timestamp.

### T-14
Reinicio lógico no altera sesión temporal vigente.

## Criterio de salida

El motor puede simular completamente el bloqueo utilizando tests o una pantalla interna.

---

# 10. Fase 5 — AccessibilityService y shield

## Objetivo

Integrar el dominio con Android.

Esta es la fase técnicamente más sensible.

## Desarrollo

### 10.1 Servicio

Crear `AppRestrictionAccessibilityService`.

Responsabilidades limitadas:

- recibir eventos relevantes;
- identificar package en foreground;
- ignorar paquetes no restringidos;
- consultar motor de acceso;
- mostrar/ocultar shield;
- no contener reglas de negocio complejas.

### 10.2 Shield

Interfaz sobre la aplicación restringida.

Debe poder representar:

```text
Bloqueada
Comprable
Saldo insuficiente
Sesión iniciándose
Error
```

### 10.3 Navegación de salida

Acción para volver a Home/system Home.

### 10.4 Estado de protección

Detectar si el servicio está activo.

## Prueba manual esencial

Usar dispositivo físico.

Casos:

1. abrir Instagram bloqueado;
2. verificar shield;
3. salir;
4. ganar puntos;
5. volver;
6. comprar sesión;
7. acceder;
8. esperar expiración;
9. verificar rebloqueo.

## Pruebas de robustez

- rotación;
- pantalla apagada/encendida;
- cambio rápido entre apps;
- Recents;
- launcher;
- app de productividad en primer plano;
- llamada entrante;
- permiso deshabilitado.

## Criterio de salida

El ciclo real funciona repetidamente en el dispositivo personal sin polling agresivo.

---

# 11. Fase 6 — Cambios protegidos y reto de concentración

## Objetivo

Impedir rebajar reglas impulsivamente.

## Modelos

```text
PendingRuleChange
ChallengeAttempt
Challenge
ChallengeQuestion
```

## Desarrollo

### Comparador

Determinar:

```text
STRICTER
EQUIVALENT
MORE_PERMISSIVE
```

### Flujo permisivo

```text
Editar regla
   ↓
Comparar
   ↓
Más permisiva
   ↓
Challenge
   ↓
Success
   ↓
PendingRuleChange
   ↓
Aplicar mañana
```

### Retos MVP

1. aritmética;
2. secuencias.

## Modo debug

Configurar duración corta mediante build config para facilitar pruebas.

Nunca mezclar esta opción con build release/personal real.

## Pruebas

### T-15
30 → 50 pts se aplica inmediatamente.

### T-16
30 → 10 no se aplica hoy.

### T-17
Abandonar reto no crea cambio.

### T-18
Superar reto crea un único cambio pendiente.

### T-19
Cambio se aplica al nuevo día una vez.

## Criterio de salida

No existe un flujo normal de UI que permita reducir restricciones inmediatamente.

---

# 12. Fase 7 — Desbloqueo de emergencia

## Objetivo

Mantener seguridad práctica y evitar frustración.

## Desarrollo

- acceso secundario desde shield;
- confirmación deliberada;
- motivo opcional;
- penalización;
- EmergencyUnlock;
- sesión de emergencia;
- registro histórico.

## Pruebas

### T-20
Emergencia queda registrada.

### T-21
Penalización no genera saldo negativo.

### T-22
La sesión expira.

### T-23
Cancelar confirmación no hace nada.

## UX

No ocultar la función, pero tampoco convertirla en CTA principal.

## Criterio de salida

El usuario tiene una vía de escape deliberada y auditable.

---

# 13. Fase 8 — Historial y estadísticas

## Objetivo

Cerrar el ciclo de reflexión.

## Consultas

- día;
- semana;
- aplicación;
- transacciones;
- tareas;
- emergencias.

## UI

### Resumen

```text
Hoy
4/5 tareas
+70 pts
-40 pts
30 pts netos
30 min comprados
```

### Semana

- cumplimiento;
- puntos;
- aplicaciones;
- emergencias.

## Implementación

Preferir queries Room y transformaciones de dominio.

No duplicar datos calculables salvo `DailySummary` como snapshot deliberado.

## Pruebas

- agregaciones con datos conocidos;
- semana vacía;
- varios unlocks;
- cambios de día.

## Criterio de salida

Las estadísticas coinciden con el ledger.

---

# 14. Fase 9 — Sistema visual y temas

## Objetivo

Convertir el MVP funcional en una app de portafolio pulida.

## Desarrollo

Crear design system:

```text
AppTheme
AppColors
AppTypography
AppSpacing
AppShapes
AppElevation
```

## Temas

- System;
- Light;
- Dark;
- acento configurable.

No usar colores hardcodeados en features.

## Componentes

```text
PrimaryButton
SecondaryButton
PointBadge
TaskRow
AppRuleCard
ProgressHeader
EmptyState
PermissionBanner
BottomSheet
ConfirmDialog
```

## Pruebas

- previews;
- font scale;
- claro/oscuro;
- varios accent;
- pantallas estrechas;
- contraste visual.

## Criterio de salida

La app mantiene jerarquía y legibilidad bajo cualquier tema soportado.

---

# 15. Fase 10 — Endurecimiento y pruebas

## Objetivo

Preparar versión presentable.

## Unit tests

Cobertura prioritaria:

- Point ledger;
- CompleteTask;
- PurchaseUnlock;
- session expiration;
- CompareRuleStrictness;
- CloseLogicalDay;
- pending changes;
- emergency penalty.

No perseguir un porcentaje arbitrario; cubrir reglas críticas.

## Integration tests

- Room + repositories;
- WorkManager;
- DataStore.

## UI tests

- crear tarea;
- completar;
- configurar app;
- comprar;
- cambiar tema;
- challenge.

## Manual Android tests

Matriz sugerida:

```text
Android actual del dispositivo personal
Android emulator API anterior
Android emulator API objetivo
```

## Regresión obligatoria

Cada release:

1. ganar puntos;
2. bloquear;
3. comprar;
4. expirar;
5. reiniciar;
6. cambio diferido;
7. emergencia;
8. modo avión.

## Criterio de salida

No existen defectos P0 conocidos.

---

# 16. Fase 11 — Documentación de portafolio

## Objetivo

Mostrar el razonamiento técnico, no solo screenshots.

## README

Debe incluir:

- problema;
- solución;
- arquitectura;
- stack;
- GIF/video;
- privacidad;
- decisiones técnicas;
- limitaciones;
- cómo ejecutar.

## Diagramas sugeridos

### Arquitectura

```text
Compose
   ↓
ViewModels
   ↓
Use Cases
   ↓
Repositories
   ↓
Room / Android Platform
```

### Economía

```text
Task → Reward → Ledger → Balance → Unlock → Debit
```

### Restricción

```text
Foreground Event
      ↓
Restricted?
      ↓
Active Session?
   ↙       ↘
 YES       NO
 allow    shield
```

## ADRs recomendados

```text
ADR-001 Why local-only
ADR-002 Why Room
ADR-003 Why AccessibilityService
ADR-004 Why points are consumable
ADR-005 Why permissive changes are delayed
```

## Criterio de salida

Una persona técnica puede comprender el proyecto sin leer todo el código.

---

# 17. Estrategia de commits

Ejemplo:

```text
feat(domain): add point transaction ledger
feat(tasks): reward completed planned tasks
feat(apps): add restricted app rules
feat(access): add unlock session engine
feat(platform): add restriction accessibility service
feat(shield): add blocked app overlay
feat(challenge): protect permissive rule changes
feat(stats): add weekly summary
```

Evitar commits gigantes como:

```text
"finished app"
```

---

# 18. Orden recomendado real

No comenzar por diseñar 20 pantallas.

Orden:

```text
1. Dominio
2. DB
3. Tareas
4. Ledger
5. Reglas
6. Sesiones
7. Pruebas
8. Accessibility
9. Shield
10. Challenge
11. Estadísticas
12. Pulido visual
```

La razón es que el principal riesgo del proyecto está en:

- reglas consistentes;
- bloqueo Android;
- sesiones;
- antitrampa.

No en las tarjetas del dashboard.

---

# 19. Backlog resumido

## Epic A — Foundation
- proyecto;
- DI;
- navegación;
- tema;
- Room.

## Epic B — Productivity
- planes;
- tareas;
- recompensas.

## Epic C — Economy
- ledger;
- saldo;
- XP.

## Epic D — App control
- catálogo;
- reglas;
- engine;
- sessions.

## Epic E — Android protection
- permissions;
- accessibility;
- shield.

## Epic F — Commitment
- challenge;
- pending changes;
- daily rollover.

## Epic G — Safety
- emergency unlock;
- permission health.

## Epic H — Insights
- history;
- stats.

## Epic I — Polish
- themes;
- animations;
- accessibility;
- docs.

---

# 20. Definition of Done por historia

Una historia no está terminada hasta cumplir:

- comportamiento implementado;
- estados de error;
- persistencia cuando aplique;
- prueba unitaria de lógica;
- prueba manual;
- sin strings de UI hardcodeados;
- compatible con tema claro/oscuro;
- no rompe modo offline;
- documentación actualizada si cambia dominio.

---

# 21. Riesgos y mitigaciones

## R-01 — Bloqueo inconsistente por comportamiento Android

**Mitigación:** prototipar AccessibilityService temprano, en fase 5 como máximo.

## R-02 — Batería

**Mitigación:** eventos del sistema, no polling de alta frecuencia.

## R-03 — Usuario desactiva protección

**Mitigación:** estado visible y notificación opcional; aceptar que no es un adversario externo.

## R-04 — Economía explotable

**Mitigación:** ledger, tareas del día sin recompensa, cambios diferidos.

## R-05 — Timers frágiles

**Mitigación:** timestamps persistidos, no depender de countdown en memoria.

## R-06 — Scope creep

**Mitigación:** respetar P0/P1/P2 de `REQUIREMENTS.md`.

## R-07 — UX demasiado gamificada

**Mitigación:** XP y niveles secundarios; foco en tareas y decisiones.

---

# 22. Hito MVP

El MVP no necesita estadísticas avanzadas ni una interfaz final.

Se alcanza cuando el siguiente demo real funciona:

```text
1. Crear tarea de 30 pts.
2. Completarla.
3. Ver saldo 30.
4. Instagram está configurado a 30 pts / 5 min.
5. Abrir Instagram.
6. Ver shield.
7. Gastar 30.
8. Usar Instagram.
9. Esperar 5 min.
10. Volver a quedar bloqueado.
11. Ver transacción en historial.
```

Ese hito debe alcanzarse antes de invertir tiempo significativo en animaciones o branding.

---

# 23. Hito 1.0 de portafolio

La versión 1.0 estará completa cuando incluya:

- MVP end-to-end;
- planificación del día siguiente;
- cambios diferidos;
- retos;
- emergencia;
- historial;
- estadísticas semanales;
- temas;
- onboarding;
- estados de permisos;
- pruebas críticas;
- README/diagramas;
- video corto de demostración.

---

# 24. Política de cambios a los requisitos

Si durante el desarrollo se descubre que una regla debe cambiar:

1. modificar primero `REQUIREMENTS.md`;
2. documentar por qué;
3. ajustar tests;
4. implementar;
5. actualizar UX si corresponde.

No cambiar silenciosamente reglas de dominio directamente en código.

---

# 25. Resultado esperado

El proyecto debe terminar siendo pequeño en infraestructura pero profundo en producto.

Debe demostrar que se puede construir una aplicación Android seria sin backend innecesario, utilizando correctamente:

- estado;
- dominio;
- persistencia;
- permisos;
- servicios del sistema;
- concurrencia;
- transacciones;
- UX;
- testing.

El objetivo técnico final no es "tener muchas features".

Es que el ciclo central sea **predecible, comprobable y difícil de romper accidentalmente**.
