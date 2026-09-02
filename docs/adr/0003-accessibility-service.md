# ADR-0003: AccessibilityService para el shield

- Estado: aceptada
- Fecha: 2026-09-01

## Contexto

Android no tiene una API convencional para bloquear otras apps. UsageStats implica retraso/polling; MDM no corresponde a autocontrol personal.

## Decisión

Usar un servicio habilitado explícitamente que escucha cambios de ventana y presenta un accessibility overlay si el paquete está restringido y no tiene sesión. No recupera contenido y delega el negocio al dominio.

## Consecuencias

Reacciona sin polling y funciona sobre terceros, pero requiere permiso sensible, explicación transparente, pruebas reales y revisión de políticas. El usuario puede deshabilitarlo: no es seguridad adversarial.

## Alternativas descartadas

UsageStats continuo afecta batería/inmediatez; VPN no impide abrir apps; Device Owner cambia el producto y la distribución.
