# Guion de demostración

## Objetivo

Grabar en 2–3 minutos el ciclo real trabajo → puntos → compra → sesión → expiración → historial. No usar una maqueta.

## Preparación

- dispositivo API 26+ conectado;
- debug instalado y servicio de Mind habilitado;
- aplicación no crítica con regla de 30 puntos y duración corta;
- plan activo con una tarea pendiente de 30 puntos;
- datos personales y notificaciones ocultos.

## Tomas

| Tiempo | Acción | Idea demostrada |
|---:|---|---|
| 0:00 | Home con saldo 0 y tarea | Compromiso previo |
| 0:15 | Completar tarea | Crédito único y XP |
| 0:30 | Historial | Ledger auditable |
| 0:45 | Mostrar regla | Costo/duración configurables |
| 1:05 | Abrir app restringida | Shield orientado a eventos |
| 1:20 | Confirmar compra | Débito y sesión atómicos |
| 1:40 | Acceder durante sesión | Persistencia por timestamp |
| 2:00 | Abrir tras expirar | Bloqueo restablecido |
| 2:20 | Historial final | Métricas consistentes |

Como tomas secundarias: reto con cambio diferido, emergencia y selector de tema.

## Grabar con ADB

```powershell
$adbPath = Join-Path $env:LOCALAPPDATA 'Android\Sdk\platform-tools\adb.exe'
& $adbPath devices
& $adbPath shell screenrecord --bit-rate 8000000 /sdcard/mind-demo.mp4
```

Detén con `Ctrl+C` y copia:

```powershell
& $adbPath pull /sdcard/mind-demo.mp4 docs/media/mind-demo.mp4
```

Revisa que no aparezcan nombres, tareas privadas, notificaciones ni apps recientes. Capturas recomendadas: Home, editor de regla, shield, reto, semana y temas.
