# SPEC: Cuadernos y documentos reales (crear, subir PDF y listar)

**Estado:** Aprobada <!-- Borrador | En revisión | Aprobada -->

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

La persona con sesión iniciada puede crear sus propios cuadernos, añadirles
documentos PDF como fuentes y ver en "Your library" sus cuadernos reales, en
lugar de los de ejemplo. Los documentos son privados: solo los ve su dueño. Es
el primer paso para poder preguntar después sobre ellos (RAG), que llegará en
una funcionalidad posterior.

## Situación actual

<!-- Comportamiento actual relevante, limitación que queremos resolver y
comportamientos existentes que deben conservarse. No describas la arquitectura. -->

- Mis Cuadernos muestra 3 cuadernos de ejemplo fijos ("Annual report",
  "Brand guidelines", "Research summary"), iguales para cualquier usuario.
- "Start" abre un detalle llamado "New notebook" que no se guarda en ningún sitio.
- No se puede subir ningún archivo. El buscador y "Last opened" no hacen nada.
- El detalle (pestañas Chat, Document y Lab) muestra contenido de ejemplo.
- En Supabase no hay tablas ni almacenamiento de archivos para la app.
- Se conserva: inicio de sesión, cierre de sesión desde el avatar, el aspecto
  del diseño de Figma y la navegación Mis Cuadernos → detalle → atrás.

## Dentro del alcance

<!-- Requisitos concretos, con identificadores estables para vincularlos a
criterios, decisiones del plan y tareas. -->

- **RF-01:** Desde "Create new notebook" la persona crea un cuaderno eligiendo
  un PDF de su dispositivo. El cuaderno se guarda en su cuenta con ese PDF como
  primera fuente. El título del cuaderno es el nombre del archivo sin ".pdf";
  no se pide un nombre al crearlo.
- **RF-02:** Desde el detalle de un cuaderno propio, la persona puede añadir
  más PDFs como fuentes de ese cuaderno, hasta un máximo de 20 fuentes por
  cuaderno. Al llegar a 20 ya no se pueden añadir más y se explica el motivo.
- **RF-03:** Solo se aceptan archivos PDF válidos de 20 MB como máximo. Si el
  archivo no cumple, no se sube nada y se explica el motivo.
- **RF-04:** Mientras se sube un PDF se muestra que la subida está en curso y
  no se puede lanzar la misma acción dos veces. Al terminar se confirma el
  resultado; si falla, se informa y se puede reintentar.
- **RF-05:** Mis Cuadernos lista los cuadernos reales de la persona, del más
  reciente al más antiguo. Cada tarjeta muestra el título, el número de
  fuentes, el tamaño total y la fecha de creación.
- **RF-06:** Si la persona no tiene cuadernos, Mis Cuadernos muestra un estado
  vacío que invita a crear el primero.
- **RF-07:** Al tocar un cuaderno se abre su detalle con su título real.
- **RF-08:** Cada persona solo puede ver y modificar sus propios cuadernos y
  documentos, también si alguien intenta acceder directamente sin la app.
- **RF-09:** Los textos nuevos existen en inglés (por defecto) y en español.

## Fuera de alcance

<!-- Exclusiones acordadas, no deducidas por el agente. Si no hay exclusiones
adicionales, indícalo tras revisarlo con la persona. -->

- Borrar o renombrar cuadernos y documentos (decidido por la persona).
- Procesar el contenido del PDF: extraer el texto, crear embeddings, RAG y chat
  real. El chat, el lector y Lab siguen mostrando contenido de ejemplo.
- Ver el PDF real en la pestaña Document.
- Que funcionen el buscador y la ordenación "Last opened".
- Otros formatos distintos de PDF.
- Un backend FastAPI propio: en esta funcionalidad la app usa Supabase
  directamente (decisión de la persona, 2026-11-04).
- Guardar los cuadernos en el dispositivo para verlos sin conexión.

## Flujo de usuario

<!-- Cómo se inicia, qué hace el usuario y qué resultado obtiene.
Incluye pantallas afectadas, navegación y alternativas relevantes. -->

1. La persona inicia sesión y ve Mis Cuadernos con sus cuadernos o, si no tiene
   ninguno, el estado vacío.
2. Toca "Start" en "Create new notebook" y se abre el selector de archivos del
   sistema, filtrado a PDF.
   - Si cancela el selector, vuelve a Mis Cuadernos sin cambios.
3. Elige un PDF. Se comprueba el archivo y empieza la subida, con un indicador
   de progreso.
4. Al terminar, se abre el detalle del nuevo cuaderno. Al volver atrás, el
   cuaderno aparece el primero de la lista.
5. En la pestaña Document del detalle de un cuaderno, la persona toca "añadir
   fuente" y repite los pasos 2–3. El número de fuentes del cuaderno aumenta.
   El Figma no incluye este botón: se construye con los componentes existentes
   del sistema de diseño, sin diseño nuevo en Figma.
6. Si la persona vuelve atrás durante una subida, la app pregunta si quiere
   cancelarla. Si confirma, la subida se cancela sin dejar nada a medias; si
   no, la subida continúa.

## Datos y reglas de negocio

<!-- Información que necesita el usuario, campos obligatorios, validaciones,
límites y reglas como duplicados u orden de presentación. Describe significado
y comportamiento, sin diseñar tablas, DTO, DAO ni almacenamiento. -->

- **Cuaderno**: título, dueño, fecha de creación y lista de fuentes. Un cuaderno
  siempre tiene al menos una fuente: no existen cuadernos vacíos.
- **Fuente (documento)**: nombre original del archivo, tamaño y fecha de subida.
  Pertenece a un único cuaderno.
- Máximo 20 fuentes por cuaderno (decisión de la persona, 2026-11-04). El
  límite se aplica también en el servidor, no solo en la app.
- Tamaño máximo por PDF: 20 MB. Un archivo vacío o que no sea un PDF se rechaza,
  aunque su nombre termine en ".pdf".
- Se puede subir dos veces el mismo PDF; no se detectan duplicados.
- Una fuente solo aparece cuando su subida termina por completo. Si la subida
  falla, no quedan cuadernos ni fuentes a medias.
- Orden de la lista: por fecha de creación del cuaderno, del más reciente al más antiguo.
- Las fechas y los tamaños se muestran con el formato del idioma de la app.

## Comportamiento mobile y casos alternativos

<!-- Adapta la tabla usando MOBILE_GUIDELINES.md. Añade escenarios relevantes.
Marca No aplica con su motivo cuando corresponda. No presupongas soporte offline
ni conservación de todo el estado. Expresa resultados, no mecanismos técnicos. -->

| Situación | Comportamiento esperado |
| --- | --- |
| Carga o acción en curso | Al cargar la lista se muestra un indicador de carga. Durante una subida se ve el progreso y "Start" / "añadir fuente" quedan desactivados. |
| Sin datos | Estado vacío con un texto que invita a crear el primer cuaderno y la tarjeta "Create new notebook" visible. |
| Entrada inválida | Un archivo que no es PDF, está vacío o supera los 20 MB se rechaza antes de subirlo, con un mensaje que dice el motivo. Un cuaderno con 20 fuentes no admite más y se indica por qué. |
| Error o espera excesiva | Si falla la carga de la lista o la subida, se muestra un mensaje claro con la opción de reintentar. Si la sesión ha caducado y no se puede renovar, la app vuelve al inicio de sesión. |
| Sin conexión o conexión interrumpida | Sin conexión, la lista muestra un error con "Reintentar". Si la conexión se corta durante una subida, la subida falla y se puede reintentar. No queda nada a medias. |
| Cancelar o volver atrás | Cancelar el selector de archivos no cambia nada. Volver atrás durante una subida pide confirmar si se cancela; al confirmar no queda nada a medias. |
| Pasar a segundo plano y regresar | La subida continúa mientras la app siga en memoria y, al volver, se ve su estado actual. |
| Recrear la pantalla | Al girar la pantalla se conservan la lista, la posición de desplazamiento y el estado de una subida en curso. |
| Reabrir después de terminarse el proceso | Una subida que no terminó se pierde sin dejar restos. Al reabrir, la lista muestra los datos guardados en el servidor. |
| Cerrar sesión e iniciar con otra cuenta | La lista solo muestra los cuadernos de la cuenta actual. Nunca aparecen los de la cuenta anterior. |
| Accesibilidad | El progreso, los errores y el estado vacío se anuncian con el lector de pantalla. Los botones nuevos tienen etiqueta y un área táctil adecuada. |
| Tamaños de pantalla y texto | La lista y los mensajes se adaptan a pantallas pequeñas y a textos grandes sin cortarse. |

**Puntos de la guía no aplicables y motivo:** Permisos: el selector de archivos
del sistema no necesita permisos de almacenamiento. Trabajo en segundo plano:
no se pide que la subida continúe si el sistema cierra la app. Persistencia local: fuera de alcance.

## Restricciones del pedido

<!-- Condiciones ya impuestas: compatibilidad, límites de alcance, requisitos
de accesibilidad o rendimiento medibles, o una tecnología expresamente exigida.
Ejemplo: Usar Room puede ser una restricción; el diseño de entidades va en PLAN.md.
No conviertas una preferencia del agente en una restricción. -->

- El backend es Supabase (base de datos y almacenamiento de archivos), en el
  proyecto "DocuMind" ya usado para la autenticación.
- La privacidad entre usuarios se garantiza en el servidor (reglas de acceso de
  Supabase), no solo en la app.
- No se exponen claves secretas en la app: solo la URL y la clave publicable de Supabase.
- Se mantiene la arquitectura KMP actual (código compartido en `:shared`) y el
  sistema de diseño existente.

## Criterios de aceptación

<!-- Resultados observables que permitan decidir si se cumple cada requisito.
Incluye los casos alternativos acordados. No uses Funciona correctamente.
Repite el formato según sea necesario. -->

- **CA-01 · RF-01, RF-04, RF-07:** Dada una persona con sesión iniciada, cuando
  toca "Start" y elige un PDF válido de menos de 20 MB, entonces ve el progreso
  de la subida, se abre el detalle del nuevo cuaderno con su título y, al volver,
  el cuaderno aparece el primero de la lista con "1 source".
- **CA-02 · RF-02:** Dado un cuaderno con 1 fuente, cuando la persona añade otro
  PDF válido desde el detalle, entonces el cuaderno pasa a mostrar "2 sources"
  y su tamaño total suma los dos archivos.
- **CA-11 · RF-02:** Dado un cuaderno con 20 fuentes, cuando la persona intenta
  añadir otra, entonces no se sube nada y se indica que se ha alcanzado el
  máximo de 20 fuentes. Una petición directa a Supabase para añadir la fuente
  21 también se rechaza.
- **CA-12 · Volver atrás y segundo plano:** Durante una subida, cuando la
  persona pasa la app a segundo plano y vuelve, entonces la subida sigue o ya
  ha terminado. Cuando vuelve atrás, entonces se le pregunta si quiere
  cancelarla: "No" mantiene la subida y "Sí" la cancela sin dejar ningún
  cuaderno ni fuente a medias.
- **CA-03 · RF-03:** Cuando la persona elige un archivo de más de 20 MB, un
  archivo que no es PDF (aunque se llame ".pdf") o un archivo vacío, entonces no
  se sube nada, no se crea ningún cuaderno y se muestra el motivo.
- **CA-04 · RF-04:** Cuando la subida falla por falta de conexión, entonces se
  muestra un error con "Reintentar", no aparece ningún cuaderno ni fuente a
  medias y, con la conexión recuperada, reintentar completa la subida.
- **CA-05 · RF-04:** Durante una subida, pulsar varias veces "Start" o "añadir
  fuente" no lanza subidas adicionales.
- **CA-06 · RF-05:** Dada una persona con 3 cuadernos, cuando abre Mis
  Cuadernos, entonces ve exactamente esos 3, del más reciente al más antiguo,
  con su título, número de fuentes, tamaño total y fecha. No aparece ningún
  cuaderno de ejemplo.
- **CA-07 · RF-06:** Dada una cuenta nueva sin cuadernos, cuando abre Mis
  Cuadernos, entonces ve el estado vacío.
- **CA-08 · RF-08:** Dadas dos cuentas A y B, cuando B inicia sesión, entonces no
  ve los cuadernos de A. Una petición directa a Supabase con la sesión de B no
  puede leer, crear ni descargar documentos de A.
- **CA-09 · Recrear:** Durante una subida y con la lista cargada, al girar la
  pantalla se mantienen la lista, la posición y el progreso.
- **CA-10 · RF-09:** Con la app en español, todos los textos nuevos (estado
  vacío, errores, progreso, añadir fuente) aparecen en español.

## Cómo se comprueba el comportamiento

<!-- Una fila por criterio: escenario y resultado que debemos comprobar.
La selección de tests, herramientas, comandos y evidencias se desarrolla en PLAN.md.
No marques los criterios como superados durante la especificación. -->

| Criterio | Condiciones y pasos | Resultado esperado |
| --- | --- | --- |
| CA-01 | Cuenta confirmada → Start → elegir un PDF de unos pocos MB | Progreso visible, detalle con el título, cuaderno primero en la lista con "1 source" |
| CA-02 | En ese detalle → añadir fuente → otro PDF → volver | "2 sources" y tamaño total correcto |
| CA-11 | Cuaderno con 20 fuentes → añadir fuente; además, insertar la fuente 21 directamente en Supabase con la sesión del dueño | Mensaje de límite en la app; el servidor rechaza la inserción |
| CA-12 | Durante una subida: ir a la pantalla de inicio y volver; después pulsar Atrás y elegir "No"; repetir con "Sí" | La subida sigue o termina; "No" la mantiene; "Sí" la cancela y la lista no cambia |
| CA-03 | Elegir un PDF de más de 20 MB, un .txt renombrado a .pdf y un archivo vacío | Tres rechazos con su motivo; la lista no cambia |
| CA-04 | Activar el modo avión durante la subida → reintentar sin conexión → desactivarlo y reintentar | Error con reintento, nada a medias; la subida se completa al final |
| CA-05 | Pulsar Start varias veces seguidas durante una subida | Solo se sube un archivo |
| CA-06 | Cuenta con 3 cuadernos creados en orden conocido | Lista con esos 3, en orden y con los datos correctos |
| CA-07 | Cuenta recién confirmada | Estado vacío |
| CA-08 | Crear cuadernos con A; iniciar sesión con B; probar el acceso directo a Supabase con el token de B | B no ve, no lee y no descarga nada de A |
| CA-09 | Girar el dispositivo con la lista cargada y durante una subida | Se conservan la lista, la posición y el progreso |
| CA-10 | Idioma de la app en es-ES y recorrer los estados nuevos | Todos los textos en español |

## Decisiones pendientes

<!-- Al resolverlas, actualiza las secciones afectadas. Escribe Ninguna cuando
no queden pendientes funcionales ni restricciones por decidir. -->

Ninguna para esta funcionalidad. Queda una nota para una funcionalidad posterior:

- **Backend para la IA (para una funcionalidad posterior, no bloquea esta):**
  `AGENTS.md` dice que el backend es FastAPI y que toda petición de IA pasa por
  él. La persona indica que usa Supabase como backend. Cuando lleguemos al RAG
  hay que decidir si la IA va en FastAPI o en Supabase (Edge Functions) y
  actualizar `AGENTS.md` en consecuencia.
