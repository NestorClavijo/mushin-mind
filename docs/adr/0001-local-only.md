# ADR-0001: producto local y sin backend

- Estado: aceptada
- Fecha: 2026-09-01

## Contexto

Tareas, hábitos, apps y emergencias forman un perfil sensible. El ciclo central debe funcionar en modo avión y no necesita cuentas.

## Decisión

Guardar todo con Room/DataStore, sin permiso de Internet, autenticación, analítica, publicidad o backend. Excluir DB y preferencias de backups/transferencias.

## Consecuencias

Mejora privacidad, simplicidad y operación offline. A cambio, no hay sincronización ni recuperación tras borrar datos. Una futura nube/exportación requerirá consentimiento y un ADR nuevo.

## Alternativas descartadas

Backend o backup automático desde el MVP: agregan exposición y complejidad sin resolver el riesgo principal.
