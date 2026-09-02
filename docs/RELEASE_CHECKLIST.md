# Checklist de release

Este documento separa las verificaciones automatizadas de las que requieren Android real. Una release no debe marcarse como validada en dispositivo hasta completar la matriz manual.

## Puerta automatizada

- [ ] `gradlew testDebugUnitTest`
- [ ] `gradlew compileDebugAndroidTestSources`
- [ ] `gradlew assembleDebug assembleRelease`
- [ ] `gradlew lintDebug`
- [ ] pruebas instrumentadas ejecutadas en al menos un dispositivo o emulador

Las pruebas críticas cubren ledger y finalización idempotente de tareas, compras y concurrencia, expiración de sesiones, comparación de reglas, cierre diario, cambios pendientes, emergencia, Room, DataStore, solicitudes de WorkManager y flujos principales de Compose.

## Matriz Android

| Entorno | Versión | Resultado | Evidencia |
|---|---:|---|---|
| Dispositivo personal actual | Por registrar | Pendiente | — |
| Emulador API anterior | Por registrar | Pendiente | — |
| Emulador API objetivo | API 37 | Pendiente | — |

## Regresión obligatoria

Ejecutar el flujo completo sin conexión para comprobar que el producto sigue siendo local y tolera reinicios.

- [ ] Crear el plan, añadir una tarea y confirmarlo.
- [ ] Completar la tarea y comprobar un único crédito en saldo e historial.
- [ ] Abrir una aplicación restringida y comprobar el shield.
- [ ] Comprar acceso y comprobar un único débito y una sesión activa.
- [ ] Esperar la expiración y comprobar que vuelve el bloqueo.
- [ ] Reiniciar el teléfono y comprobar la reconciliación y las sesiones por timestamp.
- [ ] Superar el reto de un cambio permisivo y comprobar que queda diferido.
- [ ] Usar emergencia y comprobar sesión, penalización limitada y auditoría.
- [ ] Repetir los pasos relevantes en modo avión.

## Severidad de salida

Un defecto P0 bloquea la release si provoca pérdida o duplicación de puntos, permite saltar una restricción sin una sesión válida, impide abrir la aplicación, corrompe datos o elimina la vía de emergencia. Los defectos encontrados deben registrar entorno, pasos, resultado esperado y evidencia.

## Evidencia de la fase 10

El 1 de septiembre de 2026 pasaron las 42 pruebas JVM, la compilación de las 31 pruebas instrumentadas, los APK debug/release y `lintDebug` sin errores. La ejecución instrumentada y la matriz manual quedan pendientes hasta conectar los dispositivos indicados arriba.
