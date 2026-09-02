# ADR-0005: cambios permisivos protegidos y diferidos

- Estado: aceptada
- Fecha: 2026-09-01

## Contexto

Reducir costo, ampliar duración o desactivar una regla durante la tentación anula el compromiso. Prohibir cambios elimina autonomía.

## Decisión

`CompareRuleStrictness` permite aplicar cambios estrictos/equivalentes. Los permisivos exigen reto local, crean `PendingRuleChange` y se aplican en el siguiente día lógico. Abandonar no modifica la regla activa.

## Consecuencias

Conserva autonomía con fricción y aplicación predecible. Añade estados, dependencia temporal y reconciliación idempotente. La emergencia sigue siendo una vía separada.

## Alternativas descartadas

PIN no rompe el impulso; prohibir cambios es inseguro; aplicar al terminar el reto todavía altera el compromiso actual.
