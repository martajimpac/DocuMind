# SPEC: Conversión a Kotlin Multiplatform

**Estado:** Aprobada <!-- Borrador | En revisión | Aprobada -->

> Aprobada por delegación el 2026-11-04. La usuaria pidió «migra a kmp […] sigue sdd pero no me preguntes nada, haz lo que creas». Las decisiones que tomó el agente están marcadas con *(decisión del agente)*. Sustituye al borrador del 2026-10-29 (commit `b4e2a20`), que describía la app anterior al login.

<!-- PARA LA PERSONA
Copia esta plantilla como SPEC.md en una carpeta de la funcionalidad.
Pide al agente que la complete contigo usando MOBILE_GUIDELINES.md.
SPEC.md define qué debe cumplirse; PLAN.md desarrolla cómo implementarlo;
TASKS.md organiza los pasos de ejecución.
-->

<!-- PARA EL AGENTE
- Lee las instrucciones del proyecto y MOBILE_GUIDELINES.md. Inspecciona el
  repositorio para comprobar el comportamiento actual. Si falta la guía, pide su ubicación.
- Completa esta spec con la persona: investiga lo comprobable y consulta las
  decisiones pendientes. Haz pocas preguntas por vez y actualiza las respuestas.
- No inventes requisitos ni exclusiones. Distingue propuestas de decisiones
  confirmadas y marca como PENDIENTE lo que aún no esté resuelto.
- Aplica las consideraciones mobile relevantes sin ampliar el alcance automáticamente.
- No incluyas diseño de clases, tablas, componentes, archivos o algoritmos:
  esos detalles pertenecen a PLAN.md. Sí registra restricciones explícitas del pedido.
- Mantén el documento breve y proporcional a la funcionalidad. Conserva los comentarios.
- Un documento completo no está aprobado automáticamente. Solicita aprobación
  antes de marcarlo como Aprobada. No implementes durante esta etapa.
-->

## Qué construimos y para quién

<!-- Qué necesidad resolvemos, quién tiene esa necesidad y qué podrá hacer.
Describe el objetivo en lenguaje de producto. -->

Para quien usa DocuMind en Android, la app no cambia. Por dentro, las pantallas, la lógica de sesión y el acceso a Supabase pasan a ser Kotlin compartido. Así una app iOS posterior podrá reutilizarlos sin reescribirlos, como pide `AGENTS.md` (Kotlin Multiplatform y Compose Multiplatform).

## Situación actual

<!-- Comportamiento actual relevante, limitación que queremos resolver y
comportamientos existentes que deben conservarse. No describas la arquitectura. -->

Hoy la app es solo Android, con un único módulo. Hay que conservar todo lo que ya hace:

- **Cuenta:** registro con confirmación por correo, inicio de sesión real con Supabase y sesión que se mantiene al reabrir la app.
- **Cierre de sesión:** se hace desde el avatar.
- **Recuperación de contraseña:**
  - el enlace `documind://reset-password` abre la app en «Nueva contraseña»;
  - si el enlace no vale, la app lo indica y ofrece pedir otro;
  - si no hay red, ofrece reintentar.
- **Cuadernos:** lista con cuatro cuadernos de ejemplo y búsqueda; detalle con las pestañas Chat, Documento y Lab.
- **Aspecto:** tema claro fijo con la fuente Inter.
- **Idiomas:** inglés y español.

## Dentro del alcance

<!-- Requisitos concretos, con identificadores estables para vincularlos a
criterios, decisiones del plan y tareas. -->

- **RF-01:** En Android, todos los flujos de «Situación actual» se comportan igual que antes: mismos textos en inglés y español, mismo aspecto y mismos errores.
- **RF-02:** Las pantallas, el tema, los textos, los iconos, la navegación, la lógica de sesión y el acceso a Supabase son código Kotlin Multiplatform compartido.
- **RF-03:** La app Android conserva el identificador `com.example.chatbot`. La sesión ya guardada sigue siendo válida tras actualizar la app. El punto de entrada, el manifiesto y la recepción del enlace de recuperación siguen siendo propios de Android.
- **RF-04:** El código compartido declara destinos iOS y ofrece un punto de entrada iOS para la interfaz compartida.
- **RF-05:** Los tests unitarios existentes siguen pasando como tests compartidos. *(decisión del agente)*

## Fuera de alcance

<!-- Exclusiones acordadas, no deducidas por el agente. Si no hay exclusiones
adicionales, indícalo tras revisarlo con la persona. -->

- Crear el proyecto Xcode, o compilar, instalar o ejecutar la app iOS. No se puede hacer en este PC con Windows. *(decisión del agente)*
- Funcionalidades nuevas, un rediseño o cambios de comportamiento.
- Un destino de escritorio o web.
- Room, almacenamiento local nuevo o backend FastAPI.

## Flujo de usuario

<!-- Cómo se inicia, qué hace el usuario y qué resultado obtiene.
Incluye pantallas afectadas, navegación y alternativas relevantes. -->

1. La persona abre la app. Con una sesión guardada ve Mis Cuadernos; si no, ve el inicio de sesión. Es el mismo comportamiento que antes.
2. Inicia sesión, se registra o recupera la contraseña como hasta ahora. Abre un enlace de recuperación desde el correo y llega a «Nueva contraseña».
3. Navega por los cuadernos y cierra sesión desde el avatar.

## Datos y reglas de negocio

<!-- Información que necesita el usuario, campos obligatorios, validaciones,
límites y reglas como duplicados u orden de presentación. Describe significado
y comportamiento, sin diseñar tablas, DTO, DAO ni almacenamiento. -->

- No cambian las reglas de auth (SPEC de `docs/features/auth`): correo válido, contraseña de al menos 8 caracteres y repetición de contraseña.
- La sesión guardada por versiones anteriores se sigue leyendo, así que la persona no tiene que volver a iniciar sesión.

## Comportamiento mobile y casos alternativos

<!-- Adapta la tabla usando MOBILE_GUIDELINES.md. Añade escenarios relevantes.
Marca No aplica con su motivo cuando corresponda. No presupongas soporte offline
ni conservación de todo el estado. Expresa resultados, no mecanismos técnicos. -->

| Situación | Comportamiento esperado |
| --- | --- |
| Carga o acción en curso | Igual que antes: el botón muestra la carga y no se puede pulsar dos veces. |
| Sin datos | Igual que antes. |
| Entrada inválida | Igual que antes: mismos mensajes de validación. |
| Error o espera excesiva | Igual que antes: mismos mensajes de error. |
| Sin conexión o conexión interrumpida | Igual que antes: aviso de sin conexión y reintento en «Nueva contraseña». |
| Cancelar o volver atrás | Igual que antes. Atrás en «Nueva contraseña» cierra la sesión temporal y vuelve al inicio de sesión. |
| Pasar a segundo plano y regresar | Igual que antes. |
| Recrear la pantalla | Se conservan los campos escritos y la pantalla actual. Un enlace de recuperación ya usado no se vuelve a abrir. |
| Reabrir después de terminarse el proceso | Igual que antes: se conservan la sesión y la pantalla. |
| Abrir un enlace de recuperación con la app abierta o cerrada | Abre «Nueva contraseña» en los dos casos. |
| Tamaño de texto grande y teclado abierto | Igual que antes: nada queda tapado ni cortado. |

**Puntos de la guía no aplicables y motivo:**
- Permisos, trabajo en segundo plano y persistencia nueva: la conversión no los añade.
- Formatos: se conservan los porcentajes según el idioma del dispositivo.

## Restricciones del pedido

<!-- Condiciones ya impuestas: compatibilidad, límites de alcance, requisitos
de accesibilidad o rendimiento medibles, o una tecnología expresamente exigida.
Ejemplo: Usar Room puede ser una restricción; el diseño de entidades va en PLAN.md.
No conviertas una preferencia del agente en una restricción. -->

- Kotlin Multiplatform y Compose Multiplatform, con Koin y Supabase (`AGENTS.md`).
- Navegación con tipos usando clases `@Serializable` (`AGENTS.md`).
- No hacer commit (pedido de la usuaria).

## Criterios de aceptación

<!-- Resultados observables que permitan decidir si se cumple cada requisito.
Incluye los casos alternativos acordados. No uses Funciona correctamente.
Repite el formato según sea necesario. -->

- **CA-01 · RF-01:** Dado el APK nuevo en el emulador, cuando la persona recorre login con error, registro con validaciones, «¿Olvidaste tu contraseña?» con envío, lista de cuadernos con búsqueda y detalle con las tres pestañas, entonces textos, iconos, fuente y colores coinciden con la versión anterior en inglés y en español.
- **CA-02 · RF-01, RF-03:** Dada la app abierta o cerrada, cuando se abre `documind://reset-password?error_code=otp_expired` o un enlace con un código falso, entonces se muestra «Nueva contraseña» con el aviso de enlace no válido y «Pedir otro enlace». Al girar la pantalla, no se vuelve a abrir.
- **CA-03 · RF-01:** Dado un formulario a medio escribir, cuando se gira la pantalla o el sistema termina el proceso, entonces se conservan los campos y la pantalla.
- **CA-04 · RF-02, RF-04:** Dado el proyecto, cuando se compila, entonces no queda en el módulo Android código de pantallas, navegación, dominio ni datos. El código compartido compila para los metadatos comunes y para Android, y declara los destinos iOS.
- **CA-05 · RF-03:** Dada la app anterior instalada con una sesión iniciada, cuando se instala encima el APK nuevo, entonces la app abre Mis Cuadernos sin volver a pedir el inicio de sesión. Si no hay sesión guardada, abre el inicio de sesión.
- **CA-06 · RF-05:** Dados los tests unitarios, cuando se ejecutan como tests compartidos, entonces pasan los mismos escenarios que antes, más los tests nuevos del lector de enlaces.

## Cómo se comprueba el comportamiento

<!-- Una fila por criterio: escenario y resultado que debemos comprobar.
La selección de tests, herramientas, comandos y evidencias se desarrolla en PLAN.md.
No marques los criterios como superados durante la especificación. -->

| Criterio | Condiciones y pasos | Resultado esperado |
| --- | --- | --- |
| CA-01 | Emulador, en inglés y en español: recorrer las pantallas | Mismos textos y aspecto que antes |
| CA-02 | `adb am start` con los enlaces, con la app abierta y cerrada, y girar | «Nueva contraseña» con el aviso, una sola vez |
| CA-03 | Escribir, girar y matar el proceso | Datos conservados |
| CA-04 | Compilación y revisión de archivos | Sin lógica en `:app`; destinos iOS declarados |
| CA-05 | Instalar encima con y sin sesión | Pantalla inicial correcta |
| CA-06 | Tests compartidos | Verdes |

## Decisiones pendientes

<!-- Al resolverlas, actualiza las secciones afectadas. Escribe Ninguna cuando
no queden pendientes funcionales ni restricciones por decidir. -->

- Ninguna.

<!-- ANTES DE SOLICITAR APROBACIÓN
Comprueba que el alcance está acordado, los flujos son coherentes, los puntos
mobile relevantes están cubiertos y cada requisito tiene criterios comprobables.
Resuelve las dudas y los marcadores pendientes. Mantén el diseño técnico en PLAN.md.
-->
