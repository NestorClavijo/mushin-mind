# REQUIREMENTS.md

## 1. Propósito

Este documento constituye la especificación funcional principal del producto.

Define:

- qué puede hacer el usuario;
- qué no puede hacer;
- reglas de negocio;
- estados;
- restricciones;
- casos límite;
- comportamiento del bloqueo;
- criterios de aceptación;
- requisitos no funcionales;
- alcance inicial;
- exclusiones.

Cuando exista una ambigüedad durante el desarrollo, este documento debe prevalecer sobre decisiones improvisadas de interfaz.

---

# 2. Alcance del producto

Aplicación Android, de uso personal y portafolio, desarrollada en Kotlin.

Características principales:

1. planificación de tareas;
2. recompensa de tareas mediante puntos;
3. saldo acumulable;
4. XP histórico;
5. selección de aplicaciones restringidas;
6. reglas de costo y duración;
7. bloqueo local de aplicaciones;
8. compra de sesiones con puntos;
9. cambios protegidos mediante fricción cognitiva;
10. desbloqueo de emergencia;
11. historial y estadísticas;
12. funcionamiento completamente local.

---

# 3. Actores

## ACT-01 — Usuario principal

Existe un único usuario local.

No se requiere autenticación.

El propietario del dispositivo:

- crea tareas;
- define reglas;
- gana y gasta puntos;
- habilita permisos;
- consulta estadísticas;
- puede desactivar técnicamente el sistema desde Android.

No se implementan roles.

---

# 4. Conceptos del dominio

## 4.1 Punto

Moneda virtual consumible.

## 4.2 XP

Progreso histórico no consumible.

## 4.3 Plan diario

Conjunto de tareas planificadas para un día lógico.

## 4.4 Aplicación restringida

Paquete Android al que se le ha configurado una regla de acceso.

## 4.5 Regla de acceso

Condición económica/temporal que determina cómo se desbloquea una aplicación.

## 4.6 Sesión de desbloqueo

Autorización temporal o diaria que permite utilizar una aplicación restringida.

## 4.7 Cambio restrictivo

Cambio que hace más difícil acceder a una aplicación.

## 4.8 Cambio permisivo

Cambio que hace más fácil acceder a una aplicación.

## 4.9 Reto de concentración

Actividad local requerida antes de solicitar un cambio permisivo.

## 4.10 Día lógico

Periodo utilizado por la aplicación para planes, estadísticas y desbloqueos diarios.

---

# 5. Reglas globales de negocio

## RN-001 — Los puntos son consumibles

Comprar acceso a una aplicación descuenta puntos.

## RN-002 — El XP no se gasta

El XP solo aumenta por eventos definidos y no disminuye al utilizar aplicaciones.

## RN-003 — No se permiten saldos negativos

Una transacción que dejaría el saldo debajo de cero debe rechazarse.

## RN-004 — Una tarea recompensa una sola vez

Completar repetidamente una tarea no puede duplicar la recompensa.

## RN-005 — Toda variación de saldo debe tener transacción

No se modifica el saldo sin registrar su causa.

## RN-006 — Una aplicación restringida sin sesión válida permanece bloqueada

## RN-007 — Una sesión válida tiene prioridad sobre el bloqueo

## RN-008 — Al finalizar una sesión la aplicación vuelve a estado restringido

## RN-009 — Los cambios permisivos requieren fricción

## RN-010 — Los cambios más restrictivos pueden aplicarse inmediatamente

## RN-011 — Las reglas se evalúan localmente

No existe dependencia de Internet.

## RN-012 — El usuario debe poder recuperar el control del dispositivo

Debe existir salida de emergencia y nunca se diseñará el producto como un sistema imposible de desactivar.

---

# 6. Onboarding y permisos

## RF-001 — Mostrar onboarding inicial

El sistema debe explicar brevemente:

- qué hace la app;
- qué son los puntos;
- por qué necesita permisos;
- que funciona localmente;
- que el usuario puede desactivarla desde Android.

### Criterios de aceptación

- Se muestra solo como flujo inicial salvo que el usuario decida revisarlo.
- No debe ser obligatorio crear cuenta.
- Debe poder completarse sin conexión.

---

## RF-002 — Solicitar acceso de accesibilidad de forma guiada

La app debe guiar al usuario hacia la configuración necesaria para habilitar el servicio de accesibilidad usado por el mecanismo de restricción.

### Criterios

- La app detecta si el servicio está habilitado.
- Explica para qué se utiliza antes de abrir Ajustes.
- Tras regresar, actualiza el estado.
- No declara que el bloqueo está activo si el servicio no está disponible.

---

## RF-003 — Solicitar acceso a estadísticas de uso cuando sea necesario

Si se utiliza `UsageStatsManager`, la app debe guiar al usuario para conceder acceso.

### Criterios

- La funcionalidad principal debe degradarse de forma controlada si falta el permiso.
- Debe mostrarse qué estadísticas o comprobaciones no estarán disponibles.

---

## RF-004 — Solicitar permiso de notificaciones

En versiones de Android que lo requieran, la app debe solicitar permiso antes de enviar notificaciones.

### Criterios

- Si se rechaza, la app sigue funcionando.
- Los recordatorios quedan identificados como desactivados.

---

## RF-005 — Mostrar estado de protección

Debe existir un estado global:

```text
PROTECCIÓN ACTIVA
PROTECCIÓN PARCIAL
PROTECCIÓN DESACTIVADA
```

### Ejemplos

- servicio habilitado + configuración correcta → activa;
- falta Usage Access pero el shield funciona → parcial;
- AccessibilityService deshabilitado → desactivada.

---

# 7. Perfil local y progreso

## RF-010 — Crear perfil local automáticamente

Debe existir un perfil local único sin credenciales.

Debe almacenar como mínimo:

- saldo;
- XP;
- nivel calculado;
- fecha de creación;
- preferencias.

---

## RF-011 — Mostrar saldo disponible

El saldo debe ser visible desde Inicio y desde la pantalla de desbloqueo.

---

## RF-012 — Mostrar XP y nivel

La aplicación debe mostrar el progreso histórico de forma secundaria.

El nivel no debe afectar inicialmente las reglas de acceso.

---

## RF-013 — Calcular nivel

Se debe utilizar una fórmula determinista y versionable.

Para MVP puede emplearse una progresión simple.

La fórmula exacta debe centralizarse en dominio y no codificarse en UI.

---

# 8. Planificación diaria

## RF-020 — Crear Plan del Día

El usuario debe poder crear un plan para una fecha.

Un plan contiene cero o más tareas.

---

## RF-021 — Preparar el día siguiente

La pantalla de planificación debe priorizar la creación del plan del día siguiente.

---

## RF-022 — Confirmar plan

El usuario debe poder confirmar explícitamente un plan.

Estados:

```text
BORRADOR
CONFIRMADO
ACTIVO
CERRADO
```

---

## RF-023 — Activar automáticamente el plan correspondiente al día actual

Cuando comience un nuevo día lógico, el plan confirmado para esa fecha pasa a estado activo.

---

## RF-024 — Crear recordatorio nocturno

El usuario puede configurar un recordatorio diario para planificar el siguiente día.

Parámetros:

- habilitado/deshabilitado;
- hora;
- días aplicables.

---

## RF-025 — Reconciliar planes si la app no se abrió

Al volver a abrir la app después de uno o varios días:

- debe cerrar planes anteriores;
- aplicar cambios pendientes correspondientes;
- activar el plan correcto;
- mantener consistencia del ledger.

---

# 9. Tareas

## RF-030 — Crear tarea

Campos mínimos:

- título obligatorio;
- descripción opcional;
- recompensa;
- fecha;
- estado.

---

## RF-031 — Editar tarea antes de activarse

Una tarea futura en un plan no activo puede modificarse libremente.

---

## RF-032 — Eliminar tarea antes de activarse

Permitido mientras no haya generado transacciones.

---

## RF-033 — Completar tarea activa

El usuario puede marcar una tarea pendiente como completada.

Resultado atómico:

1. estado → COMPLETADA;
2. registrar `completedAt`;
3. crear transacción `TASK_REWARD`;
4. sumar puntos;
5. sumar XP.

---

## RF-034 — Impedir doble recompensa

Marcar nuevamente una tarea ya completada no debe crear otra transacción.

---

## RF-035 — Desmarcar una tarea completada

Para MVP, **no se permite revertir libremente una tarea que ya acreditó puntos**.

Si se incorpora posteriormente, deberá crear una transacción compensatoria y no borrar el historial.

---

## RF-036 — Omitir tarea

El usuario puede marcar una tarea pendiente como omitida.

No genera puntos.

---

## RF-037 — Cancelar tarea

Puede cancelarse una tarea cuando exista una razón para diferenciarla de una omisión.

No genera puntos.

---

## RF-038 — Crear tarea durante el día

Debe permitirse añadir tareas no previstas.

Por defecto, una tarea creada después de activar el plan **no genera puntos**, o utiliza una recompensa limitada por política.

### Decisión MVP

Las tareas añadidas durante el día:

- pueden registrarse;
- pueden completarse;
- **no generan puntos por defecto**.

Esto reduce la posibilidad de crear recompensas oportunistas.

La UI debe identificar claramente:

> "Añadida hoy · sin recompensa"

---

## RF-039 — Mostrar progreso diario

Debe mostrar:

- completadas;
- total;
- puntos posibles restantes;
- puntos obtenidos;
- porcentaje.

---

# 10. Escala de recompensa

## RF-040 — Definir recompensa por tarea

Valores permitidos MVP:

```text
5 a 100 puntos
```

Incrementos recomendados de 5.

---

## RF-041 — Mostrar sugerencias de dificultad

La app puede presentar etiquetas:

```text
Muy pequeña
Sencilla
Media
Exigente
Importante
```

Las etiquetas son ayuda visual, no una clasificación objetiva.

---

## RF-042 — Validar valores

No permitir:

- negativos;
- cero;
- cantidades fuera del máximo configurado;
- valores no numéricos.

---

# 11. Ledger y saldo

## RF-050 — Registrar transacciones de puntos

Tipos mínimos:

```text
TASK_REWARD
APP_UNLOCK
EMERGENCY_PENALTY
CORRECTION
```

---

## RF-051 — Transacción inmutable

Una transacción creada no debe editarse.

Si hay una corrección, se crea otra transacción compensatoria.

---

## RF-052 — Calcular saldo consistentemente

El saldo almacenado, si existe como cache, debe coincidir con el resultado de las transacciones.

---

## RF-053 — Mostrar historial de movimientos

Cada movimiento debe indicar:

- tipo;
- cantidad;
- fecha/hora;
- descripción;
- referencia.

Ejemplo:

```text
+20  Completar "Estudiar Kotlin"
-30  Instagram · 20 min
```

---

# 12. Descubrimiento de aplicaciones instaladas

## RF-060 — Listar aplicaciones seleccionables

Debe mostrarse una lista de aplicaciones instaladas que sean razonablemente seleccionables por el usuario.

Datos:

- nombre;
- icono;
- package name;
- estado de restricción.

---

## RF-061 — Buscar aplicaciones

Debe existir búsqueda por nombre.

---

## RF-062 — Excluir la propia aplicación

La aplicación de productividad no debe poder agregarse a la lista de bloqueos.

---

## RF-063 — Advertir aplicaciones críticas

Al intentar restringir una aplicación crítica o del sistema, mostrar advertencia.

No se debe bloquear automáticamente todo el dispositivo.

---

# 13. Configuración de regla por aplicación

## RF-070 — Activar control

El usuario debe poder habilitar/deshabilitar el control de una aplicación.

La desactivación de una regla ya activa se considera cambio permisivo.

---

## RF-071 — Tipo SESIÓN_TEMPORAL

Campos:

- costo;
- duración en minutos.

Ejemplo:

```text
Instagram
30 pts
20 min
```

---

## RF-072 — Tipo HASTA_FIN_DEL_DÍA

Campos:

- costo.

La sesión expira al cierre lógico del día.

---

## RF-073 — Tipo TIEMPO_COMPRABLE

Campos:

- costo por unidad;
- minutos por unidad.

El usuario puede comprar una o varias unidades.

Para MVP puede representarse como sesiones acumuladas consecutivas.

---

## RF-074 — Validar costo

Costo mínimo MVP:

```text
5 pts
```

Costo máximo configurable técnicamente.

Valor recomendado de UI:

```text
5 a 500 pts
```

---

## RF-075 — Validar duración

Rango MVP recomendado:

```text
5 a 240 minutos
```

Para `HASTA_FIN_DEL_DÍA` no aplica duración manual.

---

## RF-076 — Mostrar resumen antes de guardar

Ejemplo:

```text
Instagram
30 pts → 20 minutos
```

---

# 14. Clasificación de cambios de reglas

## RF-080 — Detectar cambio más restrictivo

Ejemplos:

- costo 30 → 50;
- duración 30 min → 20 min;
- no restringida → restringida;
- desbloqueo diario → sesión temporal cuando objetivamente reduce acceso.

Debe existir una función de dominio que compare reglas.

---

## RF-081 — Aplicar inmediatamente cambios restrictivos

No requieren reto.

Si existe una sesión activa, la regla nueva no debe invalidarla retroactivamente salvo acción explícita del usuario.

---

## RF-082 — Detectar cambio permisivo

Ejemplos:

- costo 30 → 10;
- duración 20 → 60;
- restringida → no restringida.

---

## RF-083 — Proteger cambios permisivos

Un cambio permisivo requiere:

1. iniciar solicitud;
2. completar reto;
3. confirmar;
4. registrar cambio pendiente;
5. aplicar en el siguiente día lógico.

---

## RF-084 — No permitir múltiples cambios pendientes incompatibles

Debe existir como máximo una solicitud pendiente efectiva por aplicación.

Una nueva solicitud reemplaza o cancela explícitamente la anterior.

---

## RF-085 — Cancelar cambio pendiente

El usuario puede cancelar un cambio que todavía no se haya aplicado.

Cancelar no requiere reto.

---

# 15. Reto de concentración

## RF-090 — Iniciar reto

Debe indicar:

- propósito;
- progreso;
- posibilidad de abandonar.

---

## RF-091 — Generar retos localmente

No requiere servidor.

---

## RF-092 — Tipos iniciales

MVP debe implementar al menos dos:

1. cálculo aritmético;
2. secuencias/patrones.

---

## RF-093 — Duración mínima efectiva

El objetivo de producto es introducir fricción sustancial.

Para desarrollo/MVP debe existir una configuración de modo debug que permita reducir la duración.

En producción personal se recomienda un reto de varios minutos.

---

## RF-094 — Abandonar reto

Si el usuario sale del reto:

- no se aplica el cambio;
- el progreso puede reiniciarse según política.

### Decisión MVP

El reto se reinicia.

---

## RF-095 — Fallar respuestas

Los errores deben extender o dificultar el progreso, pero no bloquear permanentemente al usuario.

---

## RF-096 — Completar reto

Solo tras superar el reto puede confirmarse el cambio permisivo.

---

# 16. Detección y bloqueo de aplicaciones

## RF-100 — Detectar apertura de aplicación restringida

Con el mecanismo de protección habilitado, el sistema debe detectar que una aplicación seleccionada pasa al frente.

---

## RF-101 — Evaluar autorización

Al detectar una aplicación restringida:

```text
¿Existe sesión válida?
Sí → permitir
No → bloquear
```

---

## RF-102 — Mostrar shield de bloqueo

Debe mostrar como mínimo:

- nombre/icono de app;
- estado "Bloqueada";
- costo;
- duración o tipo de acceso;
- saldo actual;
- puntos faltantes si aplica;
- acción para volver;
- acción para desbloquear si es posible.

---

## RF-103 — No permitir interacción normal con la app protegida mientras exista shield

La superficie de bloqueo debe impedir el uso accidental de la aplicación subyacente mientras esté activa.

---

## RF-104 — Salir del bloqueo

Debe existir acción clara:

```text
Volver
```

Puede navegar a Home o cerrar el shield según la estrategia técnica elegida.

---

## RF-105 — Comprar desbloqueo desde el shield

Si el saldo es suficiente, el usuario puede iniciar la compra.

---

## RF-106 — Confirmar gasto

Antes de descontar:

```text
Desbloquear Instagram
30 pts
20 minutos

Saldo: 45 → 15 pts
```

Debe existir confirmación.

Puede existir opción futura para omitir confirmación por aplicación.

---

## RF-107 — Compra atómica

La creación de sesión y el débito de puntos deben ocurrir en una misma operación lógica/transaccional.

Si una parte falla, ninguna debe quedar aplicada.

---

## RF-108 — Saldo insuficiente

Mostrar:

- costo;
- saldo;
- diferencia.

Ejemplo:

```text
Te faltan 15 pts
```

No mostrar botón activo de compra.

---

## RF-109 — Aplicaciones no restringidas

No deben sufrir interferencia.

---

## RF-110 — Protección deshabilitada

Si el servicio necesario se deshabilita:

- actualizar estado global;
- mostrar aviso dentro de la app;
- opcionalmente notificar;
- no fingir que las apps siguen bloqueadas.

---

# 17. Sesiones de desbloqueo

## RF-120 — Crear sesión temporal

Datos:

- app;
- inicio;
- fin;
- costo;
- regla aplicada;
- estado.

---

## RF-121 — Persistir sesión

Cerrar la app de productividad no debe eliminar una sesión.

---

## RF-122 — Validar por tiempo absoluto

El tiempo restante no debe depender de que exista una pantalla abierta.

---

## RF-123 — Expirar sesión

Al superar `endsAt`, la siguiente evaluación debe considerar la sesión expirada.

---

## RF-124 — Mostrar tiempo restante

Debe poder consultarse desde Inicio y/o pantalla de aplicaciones.

---

## RF-125 — Finalizar sesión voluntariamente

El usuario puede finalizar antes.

### Decisión MVP

No hay reembolso.

Debe advertirse:

> "Los puntos utilizados no se devolverán."

---

## RF-126 — Sesión hasta fin del día

Debe persistir hasta `logicalDayEnd`.

---

## RF-127 — Nueva compra durante sesión

Para MVP:

- si ya existe sesión temporal activa, puede comprarse tiempo adicional;
- el nuevo tiempo se suma al final de la sesión actual;
- se genera un nuevo débito.

---

## RF-128 — Reinicio del teléfono

Las sesiones se restauran a partir de datos persistidos.

No se reinician sus tiempos.

---

# 18. Desbloqueo de emergencia

## RF-130 — Acceder al desbloqueo de emergencia

Disponible desde el shield mediante una acción secundaria no destacada.

---

## RF-131 — Mostrar advertencia

Debe explicar:

- se ignorará temporalmente la regla;
- quedará registrado;
- puede existir penalización.

---

## RF-132 — Requerir confirmación deliberada

Para MVP:

- mantener pulsado durante varios segundos o realizar confirmación de dos pasos.

No requiere Internet.

---

## RF-133 — Registrar evento

Datos:

- app;
- hora;
- duración;
- motivo opcional;
- penalización;
- saldo previo/posterior.

---

## RF-134 — Penalización configurable

Valores:

```text
SIN_PENALIZACIÓN
PUNTOS_FIJOS
```

No se permiten saldos negativos.

Si el saldo no cubre la penalización, se descuenta hasta cero o se registra como evento sin deuda.

### Decisión MVP

El saldo mínimo es cero; no existen deudas.

---

## RF-135 — Duración de emergencia

Configurable globalmente.

Valor inicial recomendado:

```text
10 minutos
```

---

# 19. Cierre de día

## RF-140 — Detectar cambio de día lógico

Debe ejecutarse de forma idempotente.

---

## RF-141 — Cerrar plan activo

Registrar resumen del día.

---

## RF-142 — Expirar desbloqueos diarios

Todas las sesiones `HASTA_FIN_DEL_DÍA` pierden validez.

---

## RF-143 — Aplicar cambios pendientes

Los cambios con fecha efectiva alcanzada se aplican una sola vez.

---

## RF-144 — Activar siguiente plan

Si existe.

---

## RF-145 — Mantener puntos acumulados

Por defecto, los puntos **no se reinician** al terminar el día.

---

## RF-146 — Límite de saldo

El sistema debe dejar preparada una política configurable.

### Decisión MVP

No se impone límite de saldo en la primera versión.

Debe modelarse de forma que pueda añadirse posteriormente sin romper el ledger.

---

# 20. Notificaciones

## RF-150 — Recordatorio de planificación

Configurado por usuario.

---

## RF-151 — Notificación de fin de sesión

Opcional.

---

## RF-152 — Resumen diario

Opcional.

---

## RF-153 — Aviso de protección desactivada

Si el sistema detecta una condición que impide bloquear, puede generar aviso.

Debe evitar spam.

---

## RF-154 — Configuración independiente

Cada tipo de notificación puede habilitarse/deshabilitarse.

---

# 21. Inicio / Dashboard

## RF-160 — Mostrar resumen de hoy

Debe priorizar:

- saldo;
- tareas;
- puntos ganados hoy;
- accesos activos.

---

## RF-161 — Acceso rápido a tareas

El usuario puede completar tareas con pocos pasos.

---

## RF-162 — Mostrar sesiones activas

Ejemplo:

```text
Instagram · 12 min restantes
```

---

## RF-163 — Mostrar estado de protección solo cuando sea relevante

No debe ocupar espacio dominante permanentemente si todo funciona correctamente.

Si existe un problema, debe ser visible.

---

# 22. Estadísticas

## RF-170 — Estadísticas del día

- tareas;
- cumplimiento;
- puntos ganados;
- puntos gastados;
- saldo neto;
- minutos desbloqueados;
- emergencias.

---

## RF-171 — Estadísticas semanales

- cumplimiento por día;
- puntos ganados/gastados;
- aplicaciones más desbloqueadas;
- minutos comprados;
- emergencias.

---

## RF-172 — Historial por aplicación

Mostrar compras de acceso y emergencias.

---

## RF-173 — Historial de tareas

Mostrar tareas completadas/omitidas por fecha.

---

## RF-174 — Estadísticas calculadas localmente

No enviar datos a terceros.

---

# 23. Temas y apariencia

## RF-180 — Tema claro

## RF-181 — Tema oscuro

## RF-182 — Seguir sistema

## RF-183 — Seleccionar acento visual

La app debe soportar múltiples paletas/accent colors.

El diseño no puede depender de que un color concreto signifique obligatoriamente una acción.

---

## RF-184 — Persistir tema

La selección debe sobrevivir reinicios.

---

# 24. Configuración

## RF-190 — Configurar hora de planificación

## RF-191 — Configurar notificaciones

## RF-192 — Configurar desbloqueo de emergencia

## RF-193 — Configurar apariencia

## RF-194 — Revisar permisos

Debe existir una pantalla central para:

- accesibilidad;
- uso;
- notificaciones.

---

## RF-195 — Reiniciar datos

Debe existir opción de borrar todos los datos locales.

Requiere confirmación fuerte.

---

# 25. Persistencia

## RF-200 — Utilizar base de datos local

Room sobre SQLite.

---

## RF-201 — Persistir entidades críticas

Mínimo:

```text
UserProgress
DailyPlan
Task
PointTransaction
RestrictedApp
AppRule
UnlockSession
PendingRuleChange
ChallengeAttempt
EmergencyUnlock
DailySummary
```

---

## RF-202 — DataStore para preferencias ligeras

Ejemplos:

- tema;
- hora de recordatorio;
- onboarding completado;
- opciones de notificación.

---

## RF-203 — Integridad transaccional

Operaciones que cambian varias entidades deben utilizar transacciones de base de datos cuando sea necesario.

Casos críticos:

- completar tarea;
- comprar desbloqueo;
- aplicar cambio pendiente;
- cerrar día.

---

# 26. Trabajo en segundo plano

## RF-210 — Programar recordatorios persistentes

Debe utilizar mecanismos apropiados de Android.

---

## RF-211 — Reprogramar después de reinicios/cambios

La app debe reconstruir programación cuando sea necesario.

---

## RF-212 — No depender exclusivamente de ejecución exacta a medianoche

La consistencia debe recuperarse al abrir la app o al ejecutar cualquier proceso relevante.

---

# 27. Requisitos no funcionales

## RNF-001 — Offline

100% de las funcionalidades core deben funcionar sin red.

---

## RNF-002 — Privacidad

No enviar datos de uso/tareas fuera del dispositivo.

---

## RNF-003 — Rendimiento

Las operaciones comunes de UI deben sentirse inmediatas.

Objetivo orientativo:

- consultas locales frecuentes < 100 ms en condiciones normales;
- navegación sin bloqueos del hilo principal.

---

## RNF-004 — Consumo de batería

La detección y monitorización no debe basarse en polling agresivo continuo.

---

## RNF-005 — Resiliencia

El sistema debe tolerar:

- cierre de proceso;
- reinicio;
- retrasos de WorkManager;
- ausencia temporal de permisos.

---

## RNF-006 — Consistencia

No pueden generarse puntos duplicados ni compras sin débito asociado.

---

## RNF-007 — Testabilidad

La lógica de negocio debe poder probarse sin dispositivo Android.

---

## RNF-008 — Arquitectura

Separación recomendada:

```text
presentation
domain
data
platform
```

---

## RNF-009 — UI declarativa

Jetpack Compose.

---

## RNF-010 — Persistencia

Room + DataStore.

---

## RNF-011 — Accesibilidad visual

Soportar:

- escalado de fuente;
- contraste razonable;
- labels;
- targets táctiles adecuados.

---

## RNF-012 — Idioma

Primera versión: español.

La arquitectura de textos debe permitir internacionalización posterior mediante recursos.

---

## RNF-013 — Sin anuncios

---

## RNF-014 — Sin cuenta obligatoria

---

# 28. Restricciones técnicas y de plataforma

## RT-001

El mecanismo de restricción depende de capacidades que el usuario debe habilitar manualmente en Android.

## RT-002

La app no puede garantizar bloqueo si el usuario:

- revoca permisos;
- deshabilita accesibilidad;
- fuerza detención;
- desinstala;
- modifica el sistema.

## RT-003

La primera versión no se diseña para cumplir requisitos de publicación masiva en Google Play; es un proyecto personal/portafolio.

Si se decide publicar, deberá revisarse específicamente la política vigente sobre APIs sensibles y accesibilidad.

## RT-004

No usar APIs privadas/no-SDK como fundamento del producto.

---

# 29. Máquina de estados — tarea

```text
              ┌─────────────┐
              │  PENDIENTE  │
              └──────┬──────┘
         ┌───────────┼───────────┐
         ↓           ↓           ↓
   COMPLETADA     OMITIDA     CANCELADA
```

`COMPLETADA` genera recompensa una sola vez.

---

# 30. Máquina de estados — sesión

```text
CREADA
  ↓
ACTIVA
  ↓
┌───────────────┬────────────────┐
↓               ↓                ↓
EXPIRADA     FINALIZADA       CANCELADA*
```

`CANCELADA` puede reservarse para errores/operaciones no iniciadas.

---

# 31. Máquina de estados — cambio de regla

```text
SOLICITADO
    ↓
RETO_EN_CURSO
    ↓
RETO_SUPERADO
    ↓
PENDIENTE
    ↓
APLICADO

Ramas:
CANCELADO
FALLIDO
EXPIRADO
```

---

# 32. Reglas de concurrencia

## RC-001

Dos pulsaciones rápidas sobre "Completar" no pueden generar dos recompensas.

## RC-002

Dos intentos simultáneos de compra no pueden gastar más puntos que el saldo.

## RC-003

La evaluación de saldo + débito + creación de sesión debe ser atómica.

## RC-004

El cierre diario puede ejecutarse múltiples veces sin duplicar eventos.

---

# 33. Casos límite obligatorios

## CE-001 — Cambio manual de hora del dispositivo

La app debe recalcular sesiones y día lógico a partir de timestamps persistidos.

Para MVP se acepta que cambios extremos de reloj puedan afectar temporizadores; el evento debe manejarse sin corrupción de datos.

## CE-002 — Cambio de zona horaria

Recalcular día lógico con la zona actual y conservar timestamps absolutos.

## CE-003 — Reinicio durante una sesión

La sesión sigue hasta su hora original de expiración.

## CE-004 — App restringida desinstalada

Mantener o archivar configuración sin provocar errores.

## CE-005 — App reinstalada con mismo package name

Puede recuperar su regla previa mediante confirmación.

## CE-006 — Servicio desactivado durante sesión

La sesión permanece en DB; el estado de protección pasa a desactivado.

## CE-007 — Saldo exactamente igual al costo

Compra válida; saldo final cero.

## CE-008 — Falta de puntos para penalización de emergencia

Saldo final cero, nunca negativo.

## CE-009 — Tarea completada justo durante cambio de día

La operación debe pertenecer a un único día lógico de manera determinista.

## CE-010 — Días sin abrir la aplicación

Debe cerrar/reconciliar todos los estados necesarios al siguiente inicio.

---

# 34. Datos que no deben almacenarse

En MVP no es necesario almacenar:

- contenido leído dentro de otras aplicaciones;
- textos de chats;
- contraseñas;
- pulsaciones de teclado;
- capturas;
- contenido de pantalla;
- ubicación;
- contactos.

El servicio usado para restricción debe procesar únicamente los eventos estrictamente necesarios para detectar la aplicación objetivo y aplicar el shield.

---

# 35. Criterios de aceptación del MVP completo

El MVP se considera terminado cuando se demuestra de extremo a extremo:

### Escenario A — Ganar puntos

1. Saldo = 0.
2. Existe tarea de 30 pts.
3. Usuario la completa.
4. Saldo = 30.
5. XP aumenta 30.
6. Existe exactamente una transacción `TASK_REWARD`.

### Escenario B — Bloqueo sin saldo

1. Instagram cuesta 30.
2. Saldo = 20.
3. Usuario abre Instagram.
4. Aparece shield.
5. Informa "faltan 10 pts".
6. No puede comprar sesión.

### Escenario C — Desbloqueo

1. Saldo = 40.
2. Instagram cuesta 30 / 20 min.
3. Usuario confirma.
4. Saldo = 10.
5. Se crea transacción -30.
6. Se crea sesión.
7. Instagram queda accesible.

### Escenario D — Expiración

1. Sesión termina.
2. Usuario intenta volver a usar Instagram.
3. Aparece shield.
4. No se descuentan puntos automáticamente.

### Escenario E — Cambio impulsivo

1. Instagram cuesta 30.
2. Usuario intenta cambiar a 10.
3. Se exige reto.
4. Tras superarlo, el cambio queda pendiente.
5. Durante el resto del día sigue costando 30.
6. El día siguiente cuesta 10.

### Escenario F — Emergencia

1. App bloqueada.
2. Usuario inicia emergencia.
3. Confirma deliberadamente.
4. Se registra evento.
5. Se aplica penalización configurada sin saldo negativo.
6. Se concede acceso temporal.

### Escenario G — Sin Internet

Todos los escenarios anteriores funcionan en modo avión.

---

# 36. Priorización

## P0 — Obligatorio MVP

- onboarding;
- permisos;
- tareas;
- puntos;
- ledger;
- apps restringidas;
- regla temporal;
- regla hasta fin de día;
- bloqueo/shield;
- compra;
- expiración;
- persistencia;
- cierre diario;
- reto básico;
- cambio diferido;
- desbloqueo de emergencia;
- tema claro/oscuro/sistema.

## P1 — Versión 1.0 de portafolio

- estadísticas semanales;
- XP/niveles visuales;
- varios tipos de reto;
- recordatorios completos;
- búsqueda avanzada;
- tema/acento configurable;
- refinamiento de UX;
- tests instrumentados de flujos críticos.

## P2 — Futuro

- límites de saldo;
- tareas recurrentes;
- widgets;
- exportación;
- backups manuales;
- más analítica;
- internacionalización;
- reglas por horario;
- grupos de aplicaciones.

---

# 37. Fuera de alcance

No implementar en el MVP:

- backend;
- cloud;
- login;
- sincronización;
- control remoto;
- bloqueo corporativo;
- bloqueo imposible de evadir;
- IA;
- recomendaciones automáticas;
- pagos;
- anuncios;
- redes sociales internas;
- versión iOS.

---

# 38. Definición funcional del producto terminado

El producto final de la primera versión debe permitir que una persona configure su propia economía de atención y que Android actúe como ejecutor de esas reglas siempre que los permisos necesarios permanezcan habilitados.

La secuencia crítica es:

```text
Definir tareas
      ↓
Completar trabajo
      ↓
Generar puntos
      ↓
Abrir app distractora
      ↓
Shield
      ↓
Evaluar saldo
      ↓
Pagar
      ↓
Crear sesión
      ↓
Usar app
      ↓
Expirar sesión
      ↓
Volver a restringir
```

Cualquier implementación que no complete de forma fiable este ciclo todavía no constituye el producto definido en este documento.
