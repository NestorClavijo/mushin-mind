# CONTEXT.md

## 1. Propósito del documento

Este documento define el contexto, la motivación, la filosofía de producto y las decisiones conceptuales de una aplicación móvil Android orientada a productividad y autocontrol del uso de aplicaciones distractoras.

Su objetivo es que cualquier persona que participe en el desarrollo pueda entender **por qué existe la aplicación**, **qué problema intenta resolver**, **qué principios deben respetarse** y **qué enfoque se seguirá para convertir la productividad en acceso consciente al ocio digital**.

Este documento no define todos los requisitos funcionales ni el detalle de implementación. Para eso existen:

- `REQUIREMENTS.md`: comportamiento funcional y restricciones del producto.
- `PLAN.md`: fases de implementación, entregables y pruebas.
- `UX-UI.md`: arquitectura de información, navegación, estados e identidad visual.

---

# 2. Problema que se quiere resolver

El problema principal no es que las aplicaciones de entretenimiento existan ni que deban prohibirse permanentemente.

El problema es el uso **automático, impulsivo y poco consciente** de aplicaciones que compiten por la atención del usuario.

En muchos casos, al desbloquear el teléfono se abre casi de forma automática una red social, un videojuego, una plataforma de video u otra aplicación de entretenimiento sin que exista una decisión deliberada de hacerlo.

El comportamiento que se quiere modificar es:

```text
Desbloquear teléfono
        ↓
Abrir aplicación distractora por hábito
        ↓
Consumir tiempo sin decisión consciente
        ↓
Posponer tareas importantes
```

La aplicación propuesta introduce una barrera intencional entre el impulso y la acción.

```text
Intentar abrir aplicación distractora
        ↓
Comprobar reglas de acceso
        ↓
¿He generado suficientes puntos?
        ↓
Sí → pagar el costo → acceder
No → mantener el bloqueo
```

El objetivo no es impedir el ocio, sino hacer que el usuario tenga que **ganarse y elegir conscientemente ese tiempo**.

---

# 3. Idea central: economía de atención

El producto se basa en una **economía personal de atención**.

El usuario crea tareas productivas. Completar esas tareas genera una moneda virtual interna denominada inicialmente **puntos**.

Los puntos se pueden gastar para desbloquear aplicaciones que el propio usuario haya marcado como distractoras.

La relación fundamental es:

```text
Acciones productivas
        ↓
Generan puntos
        ↓
Saldo disponible
        ↓
Compra acceso a ocio
        ↓
Los puntos se consumen
```

El sistema convierte una decisión abstracta como:

> "Debería estudiar antes de entrar a Instagram"

en una regla concreta:

> "Instagram cuesta 30 puntos y todavía tengo 20."

La aplicación no decide qué es productivo ni qué es distractor. **El usuario configura sus propias reglas.**

---

# 4. Principios del producto

## 4.1 El ocio no está prohibido

La aplicación no trata el entretenimiento como algo negativo.

El usuario puede utilizar las aplicaciones que quiera siempre que cumpla las condiciones que él mismo configuró.

El sistema debe comunicar:

> "Puedes usar esta aplicación, pero primero cumple el compromiso que definiste."

y no:

> "No deberías estar usando esta aplicación."

La experiencia debe evitar un tono moralista o de castigo.

---

## 4.2 Los puntos son una moneda, no una simple condición

Los puntos deben gastarse.

Ejemplo:

```text
Saldo actual: 40 pts
Instagram: 30 pts / 20 min

Desbloqueo:
40 - 30 = 10 pts restantes
```

No debe bastar con haber alcanzado alguna vez el saldo necesario.

Esto obliga al usuario a tomar decisiones entre diferentes formas de ocio.

Ejemplo:

```text
Saldo: 100 pts

Opción A
TFT hasta terminar el día → 100 pts

Opción B
Instagram 20 min → 30 pts
Instagram 20 min → 30 pts
YouTube 20 min → 25 pts

Saldo restante → 15 pts
```

La aplicación introduce así un **costo de oportunidad**.

---

# 5. Puntos disponibles y experiencia histórica

Se deben separar dos conceptos.

## 5.1 Puntos disponibles

Son la moneda que puede gastarse.

```text
+20 pts por completar una tarea
-30 pts por desbloquear Instagram
```

El saldo puede disminuir.

## 5.2 Experiencia o XP

Representa el progreso histórico del usuario.

Completar una tarea puede producir simultáneamente:

```text
+20 puntos disponibles
+20 XP
```

Al gastar puntos:

```text
-30 puntos disponibles
0 XP perdidos
```

Esto permite introducir niveles, estadísticas y progresión sin asociar el ocio con una pérdida de progreso histórico.

---

# 6. Planificación diaria

La aplicación debe fomentar que las tareas sean definidas **antes** del momento de tentación.

El flujo esperado es:

```text
Noche anterior
      ↓
Recordatorio de planificación
      ↓
Crear tareas del día siguiente
      ↓
Asignar dificultad/recompensa
      ↓
Confirmar Plan del Día
      ↓
Día siguiente
      ↓
Completar tareas y generar puntos
```

El Plan del Día actúa como un compromiso previo.

La razón es evitar una trampa evidente:

> "Quiero jugar, así que voy a crear ahora una tarea de 100 puntos muy fácil, marcarla como terminada y desbloquear el juego."

Por ello, una vez iniciado el día, el plan debe tener restricciones de edición.

---

# 7. Modelo inicial de tareas

Cada tarea diaria tendrá como mínimo:

- Nombre.
- Descripción opcional.
- Puntaje/recompensa.
- Estado.
- Fecha planificada.
- Momento de creación.
- Momento de finalización.
- Origen:
  - planificada previamente;
  - añadida durante el día.
- Indicador de si genera puntos.

Estados mínimos:

```text
PENDIENTE
COMPLETADA
OMITIDA
CANCELADA
```

Una tarea debe acreditar puntos una sola vez.

---

# 8. Dificultad y recompensa

En la primera versión, la dificultad se representa mediante puntos.

El usuario puede trabajar con valores simples como:

```text
5 pts   → tarea muy pequeña
10 pts  → tarea sencilla
20 pts  → tarea media
30 pts  → tarea exigente
50 pts  → tarea importante
```

La aplicación puede sugerir escalas, pero no debe imponerlas.

El usuario mantiene control sobre su economía personal.

---

# 9. Tipos de desbloqueo de aplicaciones

El producto debe soportar al menos tres modelos de acceso.

## 9.1 Sesión temporal

Ejemplo:

```text
Instagram
Costo: 30 pts
Duración: 20 min
```

Al pagar:

- se descuentan 30 puntos;
- Instagram queda accesible durante 20 minutos;
- al finalizar el tiempo vuelve a bloquearse.

---

## 9.2 Desbloqueo hasta finalizar el día

Ejemplo:

```text
TFT
Costo: 100 pts
Duración: hasta finalizar el día
```

Al pagar:

- se descuentan 100 puntos;
- TFT queda habilitado hasta el cierre lógico del día;
- al comenzar el siguiente día se vuelve a bloquear.

---

## 9.3 Compra de tiempo

Ejemplo:

```text
YouTube
Costo: 10 pts
Tiempo adquirido: 10 min
```

El usuario puede comprar varias unidades de tiempo.

La primera versión puede implementar este modelo internamente como sesiones temporales acumulables.

---

# 10. Configuración de aplicaciones restringidas

El usuario seleccionará qué aplicaciones instaladas desea controlar.

Para cada aplicación podrá configurar:

- Aplicación.
- Estado: controlada/no controlada.
- Tipo de regla.
- Costo en puntos.
- Duración cuando corresponda.
- Disponibilidad actual.
- Excepciones.
- Momento de última modificación.

No todas las aplicaciones deben bloquearse.

La app debe advertir al usuario cuando intenta restringir herramientas potencialmente críticas, por ejemplo:

- teléfono;
- autenticadores;
- aplicaciones bancarias;
- mapas;
- mensajería necesaria;
- configuración del sistema.

La decisión final puede seguir siendo del usuario, pero debe estar informada.

---

# 11. Fricción para modificar reglas

Una regla importante del producto es que **reducir una restricción no debe ser instantáneo**.

Ejemplo:

```text
Instagram
Actual: 30 pts / 20 min
Nuevo: 10 pts / 20 min
```

Este cambio facilita el acceso y, por tanto, debe considerarse una reducción de restricción.

El flujo será:

```text
Solicitar cambio
      ↓
Reto de concentración
      ↓
Superar reto
      ↓
Cambio queda pendiente
      ↓
Se aplica en el siguiente día lógico
```

Por el contrario, hacer una regla más estricta puede aplicarse inmediatamente.

Ejemplo:

```text
30 pts → 50 pts
```

La finalidad no es impedir los cambios, sino evitar modificaciones impulsivas.

---

# 12. Reto de concentración

El reto no debe depender de Internet.

Debe funcionar completamente en local.

Posibles categorías:

- operaciones aritméticas;
- secuencias;
- memoria visual;
- patrones;
- comparación;
- lógica sencilla;
- atención sostenida;
- series de pequeños problemas consecutivos.

El objetivo no es medir inteligencia ni generar frustración.

Su propósito es introducir una barrera de aproximadamente varios minutos de atención sostenida suficiente para romper una decisión impulsiva.

En versiones iniciales puede implementarse con retos más cortos mientras se valida la mecánica.

---

# 13. Cambios diferidos

Los cambios que reduzcan restricciones deben almacenarse como **cambios pendientes**.

Ejemplo:

```text
Regla actual:
Instagram → 30 pts / 20 min

Cambio pendiente:
Instagram → 20 pts / 20 min

Fecha de aplicación:
mañana
```

Hasta que llegue el siguiente día lógico debe seguir aplicándose la regla anterior.

---

# 14. Salida de emergencia

El producto debe evitar crear una situación en la que el usuario quede inutilmente atrapado.

Debe existir un mecanismo de **desbloqueo de emergencia**.

Características:

- debe ser accesible;
- debe advertir claramente que se está omitiendo una regla;
- debe requerir una confirmación deliberada;
- debe quedar registrado;
- puede aplicar una penalización configurable;
- debe aparecer en las estadísticas.

Ejemplo:

```text
Desbloqueo de emergencia
Instagram
10:42 p. m.

Motivo opcional: "Necesito revisar un mensaje"
Penalización: -20 pts
```

La aplicación debe promover responsabilidad, no bloqueo irreversible.

---

# 15. Alcance real del bloqueo en Android

La aplicación será desarrollada inicialmente para Android utilizando Kotlin y será un proyecto personal/de portafolio.

La intención es detectar la apertura de una aplicación restringida y colocar una interfaz de bloqueo propia mientras no exista una autorización activa.

La implementación inicial puede apoyarse en un `AccessibilityService` autorizado expresamente por el usuario para detectar cambios relevantes de ventana/aplicación y presentar un shield o pantalla de restricción.

También se puede utilizar información de uso del sistema para estadísticas y comprobaciones complementarias.

La aplicación **no se considera un sistema MDM, control parental corporativo ni un mecanismo de seguridad imposible de desactivar**.

El usuario propietario del dispositivo siempre podrá, en último término:

- deshabilitar permisos;
- deshabilitar el servicio;
- forzar detención;
- desinstalar la aplicación;
- alterar configuraciones del sistema.

El objetivo es crear fricción útil para el autocontrol, no protección contra un atacante.

---

# 16. Funcionamiento offline-first

La aplicación funcionará sin conexión a Internet.

Todos los datos se almacenarán localmente.

No habrá como requisito inicial:

- cuenta;
- inicio de sesión;
- servidor;
- API;
- sincronización;
- almacenamiento cloud;
- analítica externa;
- publicidad.

La persistencia principal se implementará con Room sobre SQLite.

Configuraciones ligeras podrán mantenerse mediante DataStore.

---

# 17. Privacidad

La privacidad es una característica fundamental.

La aplicación puede conocer información sensible sobre hábitos personales:

- aplicaciones utilizadas;
- tiempo de uso;
- tareas;
- productividad;
- horarios;
- desbloqueos de emergencia.

En el alcance inicial:

> Estos datos nunca salen del dispositivo.

No debe existir telemetría oculta.

Si en el futuro se añade exportación o sincronización, deberá ser una funcionalidad explícita y separada.

---

# 18. Notificaciones

Las notificaciones se utilizarán principalmente para:

- recordar preparar el día siguiente;
- recordar que existe un plan sin completar;
- avisar de la finalización de una sesión;
- mostrar un resumen diario opcional;
- alertar si el mecanismo de protección necesario está desactivado.

Las notificaciones deben ser configurables.

El producto no debe convertirse en otra fuente de interrupciones.

---

# 19. Cierre lógico del día

El concepto de "día" debe pertenecer al dominio de la aplicación y no depender únicamente de ejecutar una tarea exacta a medianoche.

Por defecto:

```text
Inicio: 00:00
Fin: 23:59
```

Pero la arquitectura debe permitir posteriormente configurar una hora de cierre diferente.

El estado diario debe calcularse de forma consistente incluso si:

- el teléfono estaba apagado;
- la aplicación no se abrió en varios días;
- Android retrasó un trabajo de fondo.

Al abrir la aplicación se debe reconciliar siempre el día actual con el último estado persistido.

---

# 20. Estadísticas

La aplicación debe mostrar información que ayude al usuario a entender su comportamiento.

Ejemplos:

- tareas planificadas;
- tareas completadas;
- porcentaje de cumplimiento;
- puntos generados;
- puntos gastados;
- saldo neto;
- XP;
- aplicaciones desbloqueadas;
- minutos de ocio comprados;
- desbloqueos de emergencia;
- días consecutivos de planificación;
- evolución semanal.

El objetivo no es maximizar métricas sino facilitar reflexión.

---

# 21. Ledger de puntos

La economía de puntos debe estar respaldada por un historial transaccional.

No se debe depender únicamente de un campo mutable como:

```text
user.points = 120
```

Debe existir un ledger:

```text
POINT_TRANSACTION
------------------------------------------------
TASK_REWARD        +20
TASK_REWARD        +10
APP_UNLOCK         -30
EMERGENCY_PENALTY  -20
MANUAL_ADJUSTMENT   +0 / según política
```

Esto permite:

- auditar el saldo;
- evitar dobles recompensas;
- reconstruir estadísticas;
- detectar inconsistencias;
- probar la lógica de negocio.

---

# 22. Arquitectura conceptual

La aplicación se plantea inicialmente con:

```text
Kotlin
Jetpack Compose
Navigation Compose
ViewModel
Coroutines / Flow
Room
DataStore
WorkManager
AccessibilityService
UsageStatsManager (cuando corresponda)
Notification APIs
```

Se recomienda una arquitectura por capas:

```text
UI
 ↓
Presentation
 ↓
Domain
 ↓
Data
 ↓
Room / Android Services
```

La lógica de negocio crítica debe permanecer fuera de Activities, Composables y servicios del sistema.

Ejemplos:

- calcular si un usuario puede desbloquear;
- cobrar puntos;
- completar una tarea;
- determinar si un cambio reduce una restricción;
- aplicar cambios pendientes;
- cerrar un día;
- crear una sesión.

Todo ello debe poder probarse con pruebas unitarias sin depender de una interfaz Android.

---

# 23. Filosofía de UX

La aplicación debe sentirse:

- minimalista;
- deliberada;
- rápida;
- limpia;
- calmada;
- elegante;
- no infantil;
- no moralista;
- no sobrecargada de gamificación.

Las pantallas deben concentrarse en la siguiente acción importante.

Ejemplo de inicio:

```text
Buenos días

Saldo
75 pts

HOY
3 / 5 tareas

[ Tarea 1 ]    +20
[ Tarea 2 ]    +10
[ Tarea 3 ]    +30

Acceso disponible
Instagram · 30 pts
TFT · 100 pts
```

La aplicación debe ofrecer varios temas visuales sin acoplar su identidad a un solo color.

---

# 24. Qué hará que el proyecto sea valioso para portafolio

El valor técnico no estará en añadir infraestructura innecesaria.

El proyecto demostrará:

- diseño de producto;
- modelado de dominio;
- arquitectura Android;
- persistencia local;
- Jetpack Compose;
- navegación;
- estados complejos;
- servicios Android;
- permisos especiales;
- lógica transaccional;
- trabajo en segundo plano;
- notificaciones;
- pruebas unitarias;
- pruebas de integración;
- estadísticas locales;
- diseño UX;
- manejo de casos límite.

La decisión de no utilizar backend es intencional y coherente con el problema.

---

# 25. Fuera de alcance inicial

No forman parte de la primera versión:

- versión iOS;
- cuentas multiusuario;
- autenticación;
- backend remoto;
- sincronización cloud;
- versión web;
- panel administrativo;
- control parental remoto;
- integración con terceros;
- pagos;
- publicidad;
- IA;
- colaboración social;
- rankings entre usuarios.

Estas funcionalidades solo deben añadirse si en el futuro existe una necesidad real.

---

# 26. Definición de éxito del producto

Una primera versión se considerará conceptualmente exitosa cuando un usuario pueda:

1. configurar qué aplicaciones quiere controlar;
2. definir cuánto cuesta acceder a ellas;
3. preparar sus tareas;
4. completar tareas y ganar puntos;
5. intentar abrir una aplicación restringida;
6. recibir un bloqueo cuando no cumple las condiciones;
7. gastar puntos y desbloquearla cuando sí cumple;
8. volver a bloquearla al terminar el tiempo;
9. revisar qué hizo y cómo gastó sus puntos;
10. no poder abaratar impulsivamente sus propias reglas sin atravesar la fricción definida.

Ese ciclo representa el corazón del producto:

```text
PLANIFICAR
    ↓
EJECUTAR
    ↓
GANAR
    ↓
ELEGIR
    ↓
GASTAR
    ↓
ANALIZAR
    ↓
PLANIFICAR
```

---

# 27. Principio rector

Ante una decisión de producto o implementación, se debe preguntar:

> ¿Esta funcionalidad ayuda a transformar un impulso automático en una decisión consciente?

Si la respuesta es no, probablemente no sea una prioridad para el producto.

La aplicación no existe para controlar al usuario.

Existe para ayudarle a cumplir reglas que **él mismo decidió cuando estaba pensando con claridad**.
