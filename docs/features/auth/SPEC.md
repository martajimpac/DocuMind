# SPEC: Registro e inicio de sesión con Supabase

**Estado:** Aprobada <!-- Borrador | En revisión | Aprobada -->

> **Cambio de alcance (2026-11-03):** la persona pidió después implementar la
> recuperación de contraseña «sin preguntas, tomando las decisiones
> necesarias», tras terminar el login. RF-08 queda sustituido por RF-09…RF-13 y
> CA-10 por CA-14…CA-19. Las decisiones de esta ampliación las tomó el agente y
> están marcadas como *Decisión del agente*. Primera versión aprobada: RF-01…RF-08.

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

Quien use DocuMind podrá crear una cuenta con su email y una contraseña,
confirmarla desde su correo e iniciar sesión. Solo las personas con una cuenta
confirmada acceden a Mis Cuadernos. La sesión se mantiene al reabrir la app
hasta que la persona la cierre. Es la base para que, más adelante, cada
cuaderno pertenezca a su usuario.

## Situación actual

<!-- Comportamiento actual relevante, limitación que queremos resolver y
comportamientos existentes que deben conservarse. No describas la arquitectura. -->

La pantalla de inicio de sesión no comprueba nada: «Iniciar sesión» y
«¿No tienes cuenta? Regístrate aquí» abren Mis Cuadernos directamente y quitan
el login de la pila de retroceso. «¿Olvidaste tu contraseña?» no hace nada. No
existe pantalla de registro. Al reabrir la app siempre aparece el login. En Mis
Cuadernos, la acción de perfil no hace nada. No se usa la red.

Se conserva: el aspecto actual del login (design system DocuMind), la
navegación Mis Cuadernos → detalle → volver, el contenido de ejemplo de los
cuadernos y los textos en español e inglés.

## Dentro del alcance

<!-- Requisitos concretos, con identificadores estables para vincularlos a
criterios, decisiones del plan y tareas. -->

- **RF-01:** Desde el login, «Regístrate aquí» abre una pantalla de registro
  con el mismo estilo, con campos de email, contraseña y repetir contraseña, y
  una forma de volver al login.
- **RF-02:** Al registrarse con datos válidos se crea la cuenta en Supabase y se
  envía un correo de confirmación. La app informa de que hay que revisar el
  correo y vuelve al login. La cuenta no da acceso hasta confirmarse.
- **RF-03:** Antes de enviar, la app valida: email con formato válido,
  contraseña de al menos 8 caracteres y (solo en registro) que ambas
  contraseñas coincidan. Los errores se muestran junto al campo afectado.
- **RF-04:** Iniciar sesión con email y contraseña de una cuenta confirmada abre
  Mis Cuadernos. Credenciales incorrectas o cuenta sin confirmar muestran un
  mensaje claro y la persona sigue en el login.
- **RF-05:** La sesión se mantiene al cerrar y reabrir la app: con sesión
  válida la app abre directamente en Mis Cuadernos, sin mostrar el login.
- **RF-06:** La acción de perfil de Mis Cuadernos ofrece «Cerrar sesión». Al
  usarla se cierra la sesión y se vuelve al login sin poder regresar con «Atrás».
- **RF-07:** Mientras una operación de registro o inicio de sesión está en
  curso, se muestra que está cargando y no se puede enviar de nuevo.
- **RF-08:** ~~Se oculta «¿Olvidaste tu contraseña?».~~ Sustituido por RF-09.
- **RF-09:** «¿Olvidaste tu contraseña?» del login abre una pantalla con el
  campo email (relleno con el email escrito en el login) y «Enviar enlace».
- **RF-10:** Con un email válido se pide a Supabase el correo de recuperación y
  la app muestra «Si existe una cuenta con ese email, te hemos enviado un
  enlace» y vuelve al login. *Decisión del agente:* el mismo mensaje exista o no
  la cuenta.
- **RF-11:** El enlace del correo abre la app en una pantalla «Nueva
  contraseña» con contraseña y repetición (mínimo 8, deben coincidir).
  *Decisión del agente:* el enlace solo funciona en el dispositivo y la app
  donde se pidió (flujo PKCE).
- **RF-12:** Al guardar una contraseña válida se actualiza en Supabase y la
  persona entra en Mis Cuadernos con la sesión iniciada.
- **RF-13:** Si el enlace no es válido, ha caducado o ya se usó, la app lo dice
  y ofrece pedir otro enlace. Volver atrás desde «Nueva contraseña» cancela:
  cierra la sesión temporal del enlace y vuelve al login.

## Fuera de alcance

<!-- Exclusiones acordadas, no deducidas por el agente. Si no hay exclusiones
adicionales, indícalo tras revisarlo con la persona. -->

- ~~Recuperar o cambiar la contraseña (decisión D3).~~ Incluido después (RF-09…RF-13).
- Cambiar la contraseña desde dentro de la app con sesión iniciada.
- Que el enlace de confirmación abra la app (decisión D2); se confirma en la
  página web de Supabase.
- Pedir nombre u otros datos de perfil en el registro (decisión D1).
- *Propuesta:* inicio de sesión con Google u otros proveedores, borrar la cuenta
  y editar el perfil.
- *Propuesta:* asociar cuadernos al usuario; siguen siendo los de ejemplo.
- *Propuesta:* backend FastAPI y validación del JWT en servidor (aún no existe
  backend) y la conversión a Kotlin Multiplatform.

## Flujo de usuario

<!-- Cómo se inicia, qué hace el usuario y qué resultado obtiene.
Incluye pantallas afectadas, navegación y alternativas relevantes. -->

1. **Registro:** Login → «Regístrate aquí» → Registro. Rellena email,
   contraseña y repetición → «Crear cuenta». Ve el aviso «Revisa tu correo para
   confirmar la cuenta» y vuelve al login. «Atrás» desde Registro vuelve al login.
2. **Confirmación:** abre el correo, pulsa el enlace y ve la página web de
   Supabase con la cuenta confirmada. Vuelve a la app manualmente.
3. **Inicio de sesión:** Login → email y contraseña → «Iniciar sesión» → Mis
   Cuadernos. El login sale de la pila de retroceso.
4. **Reapertura:** con sesión guardada, la app abre en Mis Cuadernos.
5. **Cierre de sesión:** Mis Cuadernos → perfil → «Cerrar sesión» → Login.
6. **Recuperación:** Login → «¿Olvidaste tu contraseña?» → email → «Enviar
   enlace» → aviso → Login. En el correo, pulsa el enlace → la app abre «Nueva
   contraseña» → guarda → Mis Cuadernos. Con un enlace no válido: mensaje y
   «Pedir otro enlace» → pantalla de recuperación.

## Datos y reglas de negocio

<!-- Información que necesita el usuario, campos obligatorios, validaciones,
límites y reglas como duplicados u orden de presentación. Describe significado
y comportamiento, sin diseñar tablas, DTO, DAO ni almacenamiento. -->

- Email: obligatorio, formato válido. *Propuesta:* se ignoran los espacios al
  principio y al final.
- Contraseña: obligatoria, mínimo 8 caracteres, sin más reglas (decisión D5).
  El mismo mínimo se aplica en Supabase, no solo en la app.
- Repetir contraseña (solo registro): debe coincidir con la contraseña.
- La contraseña nunca se guarda en el dispositivo ni aparece en logs.
- *Propuesta:* registrarse con un email ya registrado muestra el mismo aviso
  «Revisa tu correo» que un registro nuevo. Así no se revela qué emails tienen
  cuenta (es el comportamiento de Supabase con confirmación activa).
- *Propuesta:* en el login, credenciales incorrectas muestran un único mensaje
  genérico («Email o contraseña incorrectos»), sin indicar cuál falla.

## Comportamiento mobile y casos alternativos

<!-- Adapta la tabla usando MOBILE_GUIDELINES.md. Añade escenarios relevantes.
Marca No aplica con su motivo cuando corresponda. No presupongas soporte offline
ni conservación de todo el estado. Expresa resultados, no mecanismos técnicos. -->

| Situación | Comportamiento esperado |
| --- | --- |
| Carga o acción en curso | El botón principal muestra que está cargando y queda desactivado; los campos no se pueden editar. Pulsar varias veces no envía dos veces. |
| Sin datos | Botón principal activo; al pulsarlo con campos vacíos se muestran los errores de validación. *Propuesta.* |
| Entrada inválida | Mensaje junto al campo afectado (email inválido, contraseña corta, contraseñas distintas). No se contacta con el servidor. El error desaparece al corregir el campo. |
| Error o espera excesiva | Mensaje de error comprensible (sin detalles técnicos) y se puede reintentar. Los datos escritos se conservan. |
| Sin conexión o conexión interrumpida | Mensaje «Sin conexión. Revisa tu red e inténtalo de nuevo». Los datos escritos se conservan. Con sesión guardada, la app abre en Mis Cuadernos aunque no haya red. *Propuesta.* |
| Cancelar o volver atrás | Desde Registro, «Atrás» vuelve al login. Desde el login, «Atrás» sale de la app. Desde Mis Cuadernos con sesión, «Atrás» no vuelve al login. |
| Pasar a segundo plano y regresar | Los datos escritos se conservan. Si una operación terminó mientras tanto, su resultado (navegar o mostrar error) se aplica al volver, sin perderse ni repetirse. |
| Recrear la pantalla | Rotación o cambio de tema conserva campos, errores y operación en curso. |
| Reabrir después de terminarse el proceso | Con sesión: abre en Mis Cuadernos. Sin sesión: abre el login. *Propuesta:* se conserva el email escrito; la contraseña se vacía. |
| Sesión caducada o revocada | *Propuesta:* si la sesión guardada ya no es válida, la app vuelve al login. |
| Teclado | El teclado no tapa campos ni botones. Tipo de teclado de email en el campo email. *Propuesta:* «Siguiente» pasa al campo siguiente y «Hecho» en el último campo envía el formulario. |
| Accesibilidad | Campos con etiqueta; los errores los anuncia el lector de pantalla; mostrar/ocultar contraseña tiene descripción. Los errores no se comunican solo por color. |
| Tamaño de texto y pantallas | Con texto al 200 % o pantalla pequeña el formulario se desplaza sin cortar contenido. |
| Idiomas | Todos los textos nuevos en español e inglés. |

**Puntos de la guía no aplicables y motivo:** Permisos (no se usan
capacidades del dispositivo). Trabajo en segundo plano (no hay tareas
diferidas). Persistencia de datos de usuario más allá de la sesión (no se
guardan otros datos).

## Restricciones del pedido

<!-- Condiciones ya impuestas: compatibilidad, límites de alcance, requisitos
de accesibilidad o rendimiento medibles, o una tecnología expresamente exigida.
Ejemplo: Usar Room puede ser una restricción; el diseño de entidades va en PLAN.md.
No conviertas una preferencia del agente en una restricción. -->

- Autenticación con Supabase Auth (email y contraseña), en el proyecto
  Supabase «DocuMind» ya existente.
- Seguir `AGENTS.md`: Clean Architecture con MVVM, Koin, StateFlow y eventos
  one-shot con Channel + `ObserveAsEvents`.
- Reutilizar el design system existente (`ui/theme`, componentes `Dm*`).
- No incluir claves secretas (service role) en la app; solo la clave pública.
- La app sigue siendo Android (módulo `:app`, `com.example.chatbot`). Nota:
  `AGENTS.md` describe un proyecto KMP, pero el repo actual es solo Android.

## Criterios de aceptación

<!-- Resultados observables que permitan decidir si se cumple cada requisito.
Incluye los casos alternativos acordados. No uses Funciona correctamente.
Repite el formato según sea necesario. -->

- **CA-01 · RF-01:** Dado el login, cuando pulso «Regístrate aquí», entonces veo
  la pantalla de registro con email, contraseña y repetir contraseña; y al pulsar
  «Atrás» vuelvo al login.
- **CA-02 · RF-02:** Dado un email nuevo y datos válidos, cuando pulso «Crear
  cuenta», entonces veo el aviso de revisar el correo, vuelvo al login, el
  usuario aparece en Supabase sin confirmar y llega el correo de confirmación.
- **CA-03 · RF-02:** Dada una cuenta registrada sin confirmar, cuando intento
  iniciar sesión, entonces veo un mensaje que indica que falta confirmar el email
  y sigo en el login.
- **CA-04 · RF-03:** Dado el registro, cuando envío un email sin formato válido,
  una contraseña de 7 caracteres o dos contraseñas distintas, entonces veo el
  error junto al campo correspondiente y no se hace ninguna petición.
- **CA-05 · RF-04:** Dada una cuenta confirmada, cuando inicio sesión con sus
  datos, entonces se abre Mis Cuadernos y «Atrás» no vuelve al login.
- **CA-06 · RF-04:** Dada una contraseña incorrecta, cuando inicio sesión,
  entonces veo «Email o contraseña incorrectos» y sigo en el login con el email
  escrito.
- **CA-07 · RF-05:** Dada una sesión iniciada, cuando cierro la app por completo
  y la reabro, entonces se abre Mis Cuadernos sin ver el login.
- **CA-08 · RF-06:** Dada una sesión iniciada, cuando uso perfil → «Cerrar
  sesión», entonces veo el login, «Atrás» no vuelve a Mis Cuadernos y al reabrir
  la app aparece el login.
- **CA-09 · RF-07:** Dada una petición en curso, cuando pulso el botón principal
  repetidamente, entonces se ve el estado de carga y solo se envía una petición.
- ~~**CA-10 · RF-08:** Dado el login, entonces no aparece «¿Olvidaste tu
  contraseña?».~~ Sustituido por CA-14.
- **CA-11 · RF-04, RF-02:** Dado el modo avión, cuando inicio sesión o me
  registro, entonces veo el mensaje de sin conexión y conservo lo escrito; al
  recuperar la red puedo reintentar con éxito.
- **CA-12 · RF-01, RF-04:** Dado un formulario a medio rellenar, cuando giro la
  pantalla o paso a segundo plano y vuelvo, entonces conservo campos y errores.
- **CA-13 · RF-03:** Dado el registro con texto al 200 % y el teclado abierto,
  entonces todos los campos y el botón son visibles desplazando y ninguno queda
  tapado.
- **CA-14 · RF-09:** Dado el login con un email escrito, cuando pulso
  «¿Olvidaste tu contraseña?», entonces veo la pantalla de recuperación con ese
  email; «Atrás» vuelve al login.
- **CA-15 · RF-10:** Dado un email con formato válido, cuando pulso «Enviar
  enlace», entonces veo el aviso genérico y vuelvo al login; si la cuenta existe,
  Supabase envía el correo. Un email inválido muestra el error sin petición.
- **CA-16 · RF-11:** Dado un enlace de recuperación válido, cuando lo abro en el
  dispositivo, entonces se abre la app en «Nueva contraseña».
- **CA-17 · RF-12:** Dada la pantalla «Nueva contraseña», cuando guardo una
  contraseña válida, entonces entro en Mis Cuadernos y puedo iniciar sesión
  después con la nueva contraseña. Contraseña corta o distinta muestra el error
  sin petición.
- **CA-18 · RF-13:** Dado un enlace caducado, inválido o ya usado, cuando lo abro,
  entonces veo «El enlace no es válido o ha caducado» y un botón para pedir otro.
- **CA-19 · RF-13:** Dada la pantalla «Nueva contraseña», cuando pulso «Atrás»,
  entonces vuelvo al login y la app no queda con la sesión iniciada.

## Cómo se comprueba el comportamiento

<!-- Una fila por criterio: escenario y resultado que debemos comprobar.
La selección de tests, herramientas, comandos y evidencias se desarrolla en PLAN.md.
No marques los criterios como superados durante la especificación. -->

| Criterio | Condiciones y pasos | Resultado esperado |
| --- | --- | --- |
| CA-01 | Login → «Regístrate aquí» → «Atrás» | Pantalla de registro con 3 campos; vuelve al login |
| CA-02 | Registro con email nuevo real | Aviso, vuelta al login, usuario sin confirmar en Supabase, correo recibido |
| CA-03 | Login con la cuenta de CA-02 sin confirmar | Mensaje de email sin confirmar |
| CA-04 | Registro con cada dato inválido | Error en el campo; ninguna petición |
| CA-05 | Confirmar cuenta y hacer login | Mis Cuadernos; «Atrás» sale de la app |
| CA-06 | Login con contraseña errónea | Mensaje genérico; email conservado |
| CA-07 | Login, forzar cierre, reabrir | Mis Cuadernos directamente |
| CA-08 | Cerrar sesión, «Atrás», reabrir | Login en todos los casos |
| CA-09 | Pulsar varias veces durante la carga | Carga visible; una sola petición |
| CA-10 | Sustituido por CA-14 | — |
| CA-11 | Modo avión → enviar → quitar modo avión → reintentar | Mensaje sin conexión; reintento correcto |
| CA-12 | Rellenar, rotar, segundo plano y volver | Campos y errores conservados |
| CA-13 | Texto 200 %, teclado abierto en registro | Todo visible desplazando |
| CA-14 | Escribir email en login → «¿Olvidaste…?» → «Atrás» | Email prellenado; vuelve al login |
| CA-15 | Enviar con email válido y con email inválido | Aviso genérico y correo / error sin petición |
| CA-16 | Pedir enlace y abrirlo en el mismo dispositivo | App en «Nueva contraseña» |
| CA-17 | Guardar contraseña válida; cerrar sesión y entrar con ella | Mis Cuadernos; login correcto |
| CA-18 | Abrir un enlace usado o con error | Mensaje de enlace inválido y botón para pedir otro |
| CA-19 | «Atrás» en «Nueva contraseña», reabrir app | Login |

## Decisiones pendientes

<!-- Al resolverlas, actualiza las secciones afectadas. Escribe Ninguna cuando
no queden pendientes funcionales ni restricciones por decidir. -->

- Confirmar o ajustar las líneas marcadas como *Propuesta*.
