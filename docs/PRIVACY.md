# Privacidad técnica

Mind es local y offline-first. No incluye cuenta, servidor, analítica, publicidad, telemetría ni permiso de Internet.

## Datos locales

Room guarda planes, tareas, saldo, XP, ledger, aplicaciones elegidas, reglas, sesiones, retos, cambios pendientes, emergencias y resúmenes. DataStore guarda tema, acento, recordatorio y política de emergencia.

Mind no almacena contenido de otras aplicaciones, mensajes, contraseñas, pulsaciones, capturas, audio, cámara, contactos, ubicación, archivos personales ni identificadores publicitarios.

## AccessibilityService

El usuario lo habilita manualmente. Solo recibe cambios de ventana y declara `canRetrieveWindowContent=false`. Usa el paquete en primer plano para comprobar la regla/sesión y presentar el shield; no inspecciona contenido. Puede deshabilitarse desde Android.

## Notificaciones

`POST_NOTIFICATIONS` permite el recordatorio opcional en Android 13+. Rechazarlo no impide usar tareas, puntos o protección.

## Backup y eliminación

Room y preferencias están excluidos de cloud backup y device transfer. Borrar datos o desinstalar elimina toda la información sin recuperación remota.

Antes de publicar deben revisarse las políticas vigentes sobre Accessibility API y declaraciones de datos. Una futura nube o exportación requerirá consentimiento y una decisión de arquitectura nueva.
