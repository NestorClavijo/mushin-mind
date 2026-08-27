# Validación manual de la protección

Esta validación necesita un dispositivo físico. Antes de comenzar, instala el APK debug, configura una aplicación de prueba como restringida, usa **Configuración > Desarrollo > Añadir puntos de prueba** y activa **Mind > Protección de aplicaciones** en los ajustes de accesibilidad de Android.

## Ciclo principal

1. Abre la aplicación restringida y confirma que el shield cubre toda su interfaz.
2. Comprueba nombre, icono, costo, duración y saldo.
3. Pulsa **Volver** y verifica que Android regresa al Home.
4. Con saldo suficiente, vuelve a la app, pulsa **Desbloquear** y cancela la confirmación; no debe existir débito.
5. Confirma la compra; el shield debe desaparecer y permitir el acceso.
6. Cierra Mind y vuelve a la app restringida durante la sesión; debe seguir permitida.
7. Espera hasta `endsAt` y vuelve a abrirla; el shield debe reaparecer.

## Robustez

- Gira el dispositivo con el shield visible.
- Apaga y enciende la pantalla.
- Alterna rápidamente entre dos aplicaciones.
- Abre Recientes y vuelve a la aplicación restringida.
- Abre el launcher y luego Mind.
- Recibe una llamada con el shield visible.
- Desactiva el servicio de accesibilidad y verifica que el shield desaparece y Configuración muestra **Desactivada**.

No debe existir sondeo periódico: la evaluación se activa únicamente con cambios de ventana enviados por Android.
