# UX-UI.md

## 1. Objetivo

Este documento define la experiencia de usuario, navegación, estructura de pantallas, estados visuales y sistema de diseño de la aplicación.

La dirección visual será:

> **Minimalista, calmada, elegante, funcional y adaptable a distintos temas.**

La aplicación no debe parecer:

- un videojuego;
- una app infantil de hábitos;
- una herramienta corporativa;
- un dashboard lleno de KPIs;
- un sistema castigador.

Debe sentirse como una herramienta personal de autocontrol.

---

# 2. Principios de UX

## 2.1 Una acción principal por pantalla

Cada pantalla debe responder con claridad:

> ¿Qué debería hacer aquí?

## 2.2 Información antes que decoración

Los puntos, tareas y restricciones son protagonistas.

## 2.3 Fricción donde aporta valor

La app debe ser rápida para:

- completar tareas;
- revisar saldo;
- consultar sesiones.

Debe ser deliberadamente lenta para:

- reducir restricciones;
- usar emergencia;
- borrar datos.

## 2.4 Sin culpa

Evitar textos como:

> "Fallaste otra vez."

Preferir:

> "Usaste un desbloqueo de emergencia."

## 2.5 Gamificación discreta

XP, niveles y rachas no dominan la pantalla.

## 2.6 Diseño neutral a la paleta

No codificar semántica exclusivamente en azul, verde, rojo, etc.

Los temas pueden cambiar.

---

# 3. Arquitectura de navegación

Navegación principal propuesta con 4 destinos:

```text
┌─────────┬──────────┬─────────────┬─────────────┐
│  Inicio │   Apps   │ Estadísticas│ Configuración│
└─────────┴──────────┴─────────────┴─────────────┘
```

Las tareas pertenecen principalmente a Inicio/Planificación y no necesitan un tab separado en MVP.

---

# 4. Mapa general

```text
Onboarding
    ↓
Home
├── Plan de hoy
│   ├── Crear tarea
│   ├── Editar tarea
│   └── Planificar mañana
│
├── Apps
│   ├── Lista
│   ├── Seleccionar app
│   └── Regla
│       └── Challenge si reduce restricción
│
├── Estadísticas
│   ├── Hoy
│   ├── Semana
│   └── Historial
│
└── Configuración
    ├── Apariencia
    ├── Notificaciones
    ├── Protección
    ├── Emergencia
    └── Datos
```

El `Shield` existe fuera de la navegación normal porque puede aparecer sobre otra aplicación.

---

# 5. Onboarding

Máximo recomendado: 4 pantallas.

## Pantalla 1 — Idea

```text
Recupera el control de tu atención

Completa lo que quieres hacer.
Gana puntos.
Decide cómo gastarlos.
```

CTA:

```text
Continuar
```

---

## Pantalla 2 — Economía

Visual simple:

```text
Tarea             +30 pts
                   ↓
Saldo              60 pts
                   ↓
Instagram          -30 pts
                   ↓
20 min de acceso
```

---

## Pantalla 3 — Protección

Explicar:

```text
Para detectar cuándo intentas abrir una aplicación
restringida, es necesario habilitar la protección
en Android.
```

CTA:

```text
Configurar protección
```

Mostrar estado:

```text
○ Pendiente
✓ Activada
```

---

## Pantalla 4 — Primer plan

Permitir:

```text
Crear mi primera tarea
Configurar después
```

No forzar al usuario a configurar diez cosas.

---

# 6. Home

Home debe ser la pantalla con mayor uso.

Jerarquía:

```text
[Saludo / fecha]

Saldo
75 pts                          Nivel 4 · 480 XP

Plan de hoy
3 de 5 completadas

[ ] Revisar módulo            +20
[✓] Estudiar Kotlin           +30
[ ] Entrenar                  +20

[ + Añadir tarea ]

Sesiones
Instagram       12 min

[ Planificar mañana ]
```

---

# 7. Home — prioridad visual

Orden recomendado:

1. saldo;
2. progreso de tareas;
3. lista;
4. sesiones activas;
5. acción de mañana.

No colocar gráficas complejas.

---

# 8. Tarjeta de saldo

Visualmente limpia:

```text
75
puntos disponibles
```

Secundario:

```text
+50 hoy · -30 gastados
```

No convertirla en una tarjeta gigante con gradientes obligatorios.

Debe adaptarse al tema.

---

# 9. Tarea

Componente:

```text
┌────────────────────────────────────┐
│ ○  Estudiar Kotlin           +30   │
│    45 min · Planificada             │
└────────────────────────────────────┘
```

Al completar:

```text
┌────────────────────────────────────┐
│ ✓  Estudiar Kotlin           +30   │
│    Completada · 10:42               │
└────────────────────────────────────┘
```

La recompensa puede representarse con un `PointBadge`.

---

# 10. Crear tarea

Pantalla o bottom sheet:

```text
Nueva tarea

Nombre
[________________________]

Descripción
[________________________]

Recompensa
5  10  20  30  50
[ Personalizar ]

Fecha
Mañana

[ Crear tarea ]
```

No pedir demasiados campos.

---

# 11. Planificar mañana

Debe sentirse como un ritual corto.

```text
Mañana · Jueves 20

3 tareas
80 puntos posibles

Revisar documentación       +20
Gimnasio                    +30
Implementar pantalla        +30

[ + Añadir tarea ]

─────────────
[ Confirmar plan ]
```

Después:

```text
Plan listo para mañana
80 puntos posibles
```

---

# 12. Estado del plan activo

Una vez iniciado el día:

```text
Plan de hoy
Confirmado ayer
```

Las tareas planeadas mantienen recompensa.

Una tarea nueva muestra:

```text
Añadida hoy
Sin recompensa
```

para que la regla antitrampa sea transparente.

---

# 13. Pantalla Apps

Debe parecer un gestor de reglas, no una lista técnica de packages.

```text
Apps

[ Buscar aplicaciones... ]

Restringidas
Instagram
30 pts · 20 min

TFT
100 pts · Hasta fin del día

────────────

Otras aplicaciones
YouTube
Libre

Reddit
Libre
```

Filtro opcional:

```text
Todas | Restringidas
```

---

# 14. Configurar aplicación

```text
Instagram

[icono]

Restricción
[ Activa ]

Tipo de acceso
○ Sesión temporal
○ Hasta fin del día
○ Comprar tiempo

Costo
30 pts

Duración
20 min

Resumen
30 pts → 20 min

[ Guardar ]
```

---

# 15. Cambio más restrictivo

Ejemplo 30 → 50.

Al guardar:

```text
Regla actualizada

Instagram ahora requiere
50 pts por 20 min.
```

Sin fricción adicional.

---

# 16. Cambio más permisivo

Ejemplo 30 → 10.

En vez de guardar:

```text
Este cambio facilita el acceso

Para evitar modificar una regla por impulso,
los cambios que reducen una restricción requieren
un reto de concentración y se aplican mañana.

Actual
30 pts · 20 min

Nuevo
10 pts · 20 min

[ Comenzar reto ]
[ Cancelar ]
```

---

# 17. Challenge

Debe ser extremadamente limpio.

No mostrar navegación normal.

```text
Reto de concentración

3 / 12

17 + 28 = ?

[ 43 ]
[ 44 ]
[ 45 ]
[ 46 ]

────────────
Progreso
██████░░░░
```

No usar animaciones celebratorias excesivas.

Al terminar:

```text
Reto completado

El cambio quedará programado para mañana.

Instagram
30 pts → 10 pts

[ Confirmar cambio ]
```

---

# 18. Shield de aplicación bloqueada

Es una pantalla crítica.

Debe ser calmada y directa.

```text
                  [ Instagram ]

                 Acceso bloqueado

        30 pts por 20 minutos

Saldo disponible
45 pts

[ Desbloquear por 30 pts ]

Te quedarán 15 pts

[ Volver ]

Desbloqueo de emergencia
```

---

# 19. Shield — saldo insuficiente

```text
Instagram

Acceso bloqueado

30 pts por 20 minutos

Saldo
20 pts

Te faltan 10 pts

[ Volver ]

Desbloqueo de emergencia
```

No mostrar CTA de compra habilitado.

---

# 20. Confirmación de compra

Bottom sheet/modal:

```text
¿Desbloquear Instagram?

Duración
20 min

Costo
30 pts

Saldo
45 → 15 pts

[ Desbloquear ]
[ Cancelar ]
```

Esta confirmación hace visible el costo de oportunidad.

---

# 21. Sesión activa

En Home:

```text
Sesiones activas

Instagram
12:34 restantes
[ Finalizar ]
```

En Apps:

```text
Instagram
Acceso activo · 12 min
```

Si el usuario vuelve a Instagram, no debe ver shield mientras la sesión sea válida.

---

# 22. Finalización de sesión

Notificación opcional:

```text
Instagram vuelve a estar restringido
Tu sesión de 20 min terminó.
```

Sin tono de regaño.

---

# 23. Desbloqueo de emergencia

No debe estar a un toque accidental.

Primer estado:

```text
Desbloqueo de emergencia

Esta acción omitirá temporalmente tu regla
y quedará registrada.

Duración
10 min

Penalización
20 pts

[ Continuar ]
[ Cancelar ]
```

Segundo estado:

```text
Mantén pulsado para desbloquear

[████████████████      ]
```

Resultado:

```text
Acceso de emergencia activo
10 min
```

---

# 24. Estadísticas

## Vista Hoy

```text
Hoy

Tareas
4 / 5            80%

Puntos
+70 ganados
-40 gastados
+30 netos

Ocio comprado
Instagram        20 min
YouTube          10 min

Emergencias
0
```

---

# 25. Estadísticas semanales

Evitar dashboard empresarial.

Una visualización principal por bloque.

```text
Esta semana

Cumplimiento
L  80%
M 100%
X  60%
J  90%
V  ---

Puntos
Ganados     320
Gastados    210

Más desbloqueada
Instagram   1 h 40 min
```

---

# 26. Historial

Timeline simple:

```text
Hoy

+30
Estudiar Kotlin
10:42

-30
Instagram · 20 min
11:15

+20
Entrenamiento
17:30
```

Filtros:

```text
Todo | Tareas | Apps | Emergencias
```

---

# 27. Configuración

Agrupar por intención.

```text
Configuración

Protección
  Estado                  Activa
  Permisos                Revisar

Rutina
  Recordatorio nocturno   9:30 p. m.
  Cierre del día           12:00 a. m.

Emergencia
  Duración                 10 min
  Penalización             20 pts

Apariencia
  Tema                     Sistema
  Acento                   Elegir

Notificaciones
  Planificación            Sí
  Fin de sesiones          Sí
  Resumen diario           No

Datos
  Borrar todos los datos
```

---

# 28. Estado de permisos

Pantalla:

```text
Protección

✓ Servicio de protección
  Activo

✓ Acceso de uso
  Activo

○ Notificaciones
  Desactivadas

[ Abrir ajustes ]
```

Cada permiso debe explicar impacto.

---

# 29. Sistema de temas

La app no debe diseñarse alrededor de una sola paleta.

Separar:

```text
Surface
SurfaceVariant
TextPrimary
TextSecondary
Border
Accent
OnAccent
Success
Warning
Danger
```

Los componentes consumen roles semánticos, no colores específicos.

---

# 30. Opciones de tema

## Modo

```text
Sistema
Claro
Oscuro
```

## Acentos

Puede ofrecer presets:

```text
Neutral
Azul
Verde
Violeta
Ámbar
Rojo suave
```

Los nombres son configurables; no son identidad obligatoria.

Si se utiliza Material You/dynamic color en el futuro, debe ser opcional.

---

# 31. Estética

## Forma

- esquinas moderadamente redondeadas;
- superficies simples;
- pocos contenedores anidados;
- líneas/divisores sutiles;
- sombras muy discretas.

## Espaciado

Sistema recomendado:

```text
4
8
12
16
24
32
48
```

## Tipografía

Preferir sans serif del sistema/Material.

Jerarquía:

```text
Display pequeño → saldo
Title → encabezados
Body → contenido
Label → metadata
```

No usar cinco familias tipográficas.

---

# 32. Iconografía

Material Symbols/Icons o set consistente.

Evitar:

- emojis como iconos estructurales;
- iconos 3D;
- ilustraciones excesivas.

Los iconos de aplicaciones instaladas sí utilizan sus iconos originales.

---

# 33. Animaciones

Deben reforzar estado.

Permitidas:

- completar tarea;
- cambio de saldo;
- aparición de shield;
- progreso de challenge;
- cambio de tab.

Duraciones cortas.

Evitar:

- confeti constante;
- rebotes;
- animaciones que retrasen acciones.

---

# 34. Empty states

## Sin tareas

```text
No tienes tareas para hoy

Puedes añadir una tarea, aunque las tareas creadas
durante el día no generan puntos.

[ Añadir tarea ]
```

## Sin apps restringidas

```text
Todavía no controlas ninguna aplicación

Elige las apps en las que quieres introducir
una pausa antes de entrar.

[ Elegir aplicaciones ]
```

## Sin historial

```text
Tu historial aparecerá aquí cuando empieces
a completar tareas y utilizar puntos.
```

---

# 35. Errores

No utilizar mensajes técnicos.

Malo:

```text
SQLiteConstraintException
```

Bueno:

```text
No pudimos guardar el cambio.
Tus puntos no fueron descontados.
```

Cuando una transacción falla, tranquilizar mediante información concreta.

---

# 36. Estados de carga

Como toda la información es local, las cargas deberían ser mínimas.

Preferir:

- contenido skeleton solo si es necesario;
- evitar spinners globales por operaciones triviales;
- operaciones optimistas únicamente cuando no comprometan el ledger.

---

# 37. Accesibilidad

- targets táctiles adecuados;
- soporte font scaling;
- contentDescription;
- no depender únicamente del color;
- contraste;
- textos claros;
- navegación con lector de pantalla cuando sea viable.

El uso de un AccessibilityService por razones de control de apps no reemplaza la obligación de que nuestra propia UI sea accesible.

---

# 38. Estados globales

## 38.1 Normal

No banner.

## 38.2 Protección parcial

Banner discreto:

```text
Protección limitada
Falta un permiso para aplicar todas las reglas.
[ Revisar ]
```

## 38.3 Protección desactivada

Banner destacado:

```text
La protección está desactivada
Tus aplicaciones restringidas pueden abrirse.
[ Activar ]
```

---

# 39. Navegación de atrás

Reglas:

- formularios con cambios no guardados → confirmar salida;
- challenge → advertir que se reiniciará;
- compra → cancelar sin débito;
- shield → volver a Home/launcher;
- emergency flow → cancelar sin evento.

---

# 40. Flujo principal completo

```text
NOCHE
Home
 ↓
Planificar mañana
 ↓
Crear tareas
 ↓
Confirmar

DÍA
Home
 ↓
Completar tarea
 ↓
+ puntos
 ↓
Abrir Instagram
 ↓
Shield
 ↓
Comprar acceso
 ↓
- puntos
 ↓
Instagram disponible
 ↓
Tiempo expira
 ↓
Bloqueada
```

---

# 41. Flujo de modificación

```text
Apps
 ↓
Instagram
 ↓
Editar regla
 ↓
30 pts → 10 pts
 ↓
Detectar que es más permisiva
 ↓
Reto
 ↓
Completado
 ↓
Cambio pendiente
 ↓
Día siguiente
 ↓
Aplicado
```

---

# 42. Flujo de emergencia

```text
Shield
 ↓
Emergencia
 ↓
Advertencia
 ↓
Confirmación deliberada
 ↓
Registrar
 ↓
Penalizar
 ↓
Sesión de emergencia
```

---

# 43. Pantallas MVP

Obligatorias:

```text
01 Splash / bootstrap
02 Onboarding
03 Home
04 Crear tarea
05 Planificar mañana
06 Apps
07 Seleccionar aplicaciones
08 Configurar regla
09 Challenge
10 Shield
11 Confirmación de compra
12 Emergencia
13 Historial
14 Estadísticas
15 Configuración
16 Protección/permisos
17 Apariencia
```

Varias pueden ser sheets/dialogs y no destinos independientes.

---

# 44. Componentes reutilizables

```text
AppTopBar
BottomNavigation
BalanceHeader
PointBadge
TaskRow
AppRow
RuleSummary
ActiveSessionCard
ProtectionBanner
StatBlock
TimelineItem
PrimaryButton
SecondaryButton
DangerButton
ConfirmSheet
EmptyState
SectionHeader
ThemePreview
```

---

# 45. Diseño adaptativo

El foco es móvil portrait.

Debe tolerar:

- dispositivos compactos;
- landscape sin romper;
- font scale alto.

Tablet no es prioridad, pero Compose no debe utilizar tamaños rígidos que lo impidan.

---

# 46. Microcopy

Tono:

- directo;
- neutral;
- breve;
- humano.

Preferir:

```text
Te faltan 10 pts
```

sobre:

```text
No cuentas con la cantidad necesaria de puntos
para realizar esta operación.
```

Preferir:

```text
Aplicar mañana
```

sobre:

```text
La configuración seleccionada será aplicada
al iniciar el siguiente periodo diario.
```

---

# 47. Filosofía visual del shield

El shield es el momento más importante del producto.

Debe provocar una pausa.

No debe:

- parecer error;
- provocar ansiedad;
- usar rojo agresivo de forma permanente;
- mostrar estadísticas innecesarias.

Debe responder inmediatamente:

1. ¿Qué app intenté abrir?
2. ¿Cuánto cuesta?
3. ¿Cuánto tengo?
4. ¿Puedo entrar?
5. ¿Qué pasa si entro?

---

# 48. Filosofía del Home

Home debe responder:

1. ¿Qué tengo que hacer?
2. ¿Cuántos puntos tengo?
3. ¿Qué ya completé?
4. ¿Tengo alguna sesión activa?

Nada más es prioritario.

---

# 49. Criterios visuales de aceptación

Una pantalla se considera aceptable cuando:

- tiene una jerarquía clara a primera vista;
- existe como máximo un CTA principal dominante;
- funciona claro/oscuro;
- funciona con al menos tres acentos;
- no depende de color para entender estados;
- no tiene bloques de texto innecesarios;
- las acciones peligrosas están separadas;
- los datos más importantes pueden leerse sin scroll excesivo.

---

# 50. Dirección final

La estética debe comunicar:

> "Tengo control sobre mis decisiones."

No:

> "Estoy siendo castigado por mi teléfono."

El diseño será visualmente sobrio, pero la interacción debe transmitir progreso.

La personalización por temas permitirá que el usuario haga suya la aplicación sin alterar la jerarquía ni la lógica central.
