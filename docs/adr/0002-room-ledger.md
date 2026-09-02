# ADR-0002: Room y ledger transaccional

- Estado: aceptada
- Fecha: 2026-09-01

## Contexto

Un campo mutable de saldo no explica cambios y queda inconsistente si una tarea o compra falla a mitad.

## Decisión

Room es fuente de verdad. Cada crédito/débito crea una transacción inmutable; saldo y XP materializados cambian atómicamente con la tarea, sesión o emergencia. Cambios condicionales e IDs únicos aportan idempotencia.

## Consecuencias

El saldo es auditable, los fallos revierten todo y las compras concurrentes no duplican gasto. Requiere migraciones, pruebas Room y que toda escritura financiera pase por repositorios.

## Alternativas descartadas

DataStore no ofrece el modelo relacional requerido; saldo sin ledger pierde trazabilidad; sumar siempre el ledger encarece cada decisión del shield.
