# SPEC: Rediseño visual según Figma (DocuMind · Soft Intelligence)

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

Que la app tenga el aspecto definido en el Figma "DocuMind · Soft Intelligence"
(https://www.figma.com/design/pYHxch462JeAupjcKe3NtO): base cálida, superficies
blancas con bordes suaves, acento violeta y tipografía Inter. El objetivo es una
interfaz coherente y mantenible, donde los elementos con el mismo propósito o el
mismo aspecto se vean idénticos en todas las pantallas y tengan un único origen
de estilo. Solo cambia la apariencia; no se añade funcionalidad.

## Situación actual

<!-- Comportamiento actual relevante, limitación que queremos resolver y
comportamientos existentes que deben conservarse. No describas la arquitectura. -->

- La app usa el tema Material 3 por defecto (violeta genérico y color dinámico
  en Android 12+), por lo que su aspecto no coincide con el diseño y varía según el dispositivo.
- Pantallas: Login, Mis Cuadernos (carrusel + lista), detalle de cuaderno con
  pestañas Fuentes / Chat / Lab.
- Cada pantalla dibuja sus propias tarjetas, botones y cabeceras con estilos
  distintos (radios, colores y tamaños repetidos a mano).
- Se conserva: la navegación Login → Cuadernos → Detalle, volver atrás, cambio
  de pestaña y el envío local de mensajes del chat de prueba.

## Dentro del alcance

<!-- Requisitos concretos, con identificadores estables para vincularlos a
criterios, decisiones del plan y tareas. -->

- **RF-01:** La app usa la paleta del Figma (crema #F7F6F2 de fondo, blanco
  para superficies, #202124 para contenido, violeta #7771E8 para acción y énfasis,
  verde #4D9275 para estados correctos, gris cálido #E8E6E0 para bordes y
  separadores, #6B6B66 para texto secundario) y la tipografía Inter, con un
  aspecto idéntico en todos los dispositivos.
- **RF-02:** La pantalla de cuadernos reproduce el diseño "Document library":
  cabecera con marca DocuMind y avatar con iniciales, título "Your library",
  tarjeta "Create new notebook" con botón principal, buscador, lista "Recent
  notebooks" con tarjetas de documento y nota de privacidad.
- **RF-03:** La pestaña Chat reproduce el diseño "Document chat": burbuja de
  pregunta del usuario, respuesta con identidad del asistente, referencias de
  cita [n] en violeta, tarjeta de cita del documento, botón de copiar y
  compositor de mensaje con botón de envío circular y aviso de precisión.
- **RF-04:** La pestaña de documento (actual "Fuentes") reproduce el diseño
  "Cited PDF passage": página de documento con pasaje resaltado y controles del
  lector (página anterior/siguiente, selector de página, zoom).
- **RF-05:** El detalle del cuaderno usa la cabecera de documento (volver, título,
  subtítulo, más opciones) y la barra inferior de 3 pestañas del diseño
  (Chat, Document, Lab), con la pestaña seleccionada resaltada en violeta.
- **RF-06:** Existe la hoja inferior "Original source" del diseño como elemento
  visual reutilizable, terminado y con vista previa de diseño, pero sin abrirse
  desde ninguna pantalla todavía (abrirla es funcionalidad futura).
- **RF-07:** Los elementos con el mismo propósito o el mismo aspecto (cabeceras,
  tarjetas, botón principal, campos de entrada tipo píldora, pestañas, bloque de
  pasaje resaltado, marcas e iconos en recuadro, avatar) se ven idénticos en
  todas las pantallas donde aparecen y su estilo se define en un único lugar.
- **RF-08:** Las pantallas sin diseño en Figma (Login, Lab) adoptan la paleta, la
  tipografía y los mismos elementos compartidos (tarjetas, botón principal,
  campos tipo píldora, iconos en recuadro). Se conservan su estructura y su
  contenido actuales; solo cambia la apariencia.
- **RF-09:** Los iconos coinciden con los del Figma (estilo de contorno Lucide),
  usando los recursos exportados del propio Figma, sin añadir librerías de iconos.
- **RF-11:** El nombre visible de la app es siempre "DocuMind" (lanzador, título
  de la app, Login y cualquier texto que hoy dice "Chatbot" o "Chatbot AI").
  Añadido a petición de la persona el 2026-10-29, tras la aprobación inicial.
- **RF-10:** Todos los textos visibles existen en inglés (por defecto, igual que el Figma) y en español.

## Fuera de alcance

<!-- Exclusiones acordadas, no deducidas por el agente. Si no hay exclusiones
adicionales, indícalo tras revisarlo con la persona. -->

- Nueva funcionalidad (pedido explícito): búsqueda real, ordenación, subida de
  PDF, lector de PDF real, paginación/zoom, copiar al portapapeles, apertura de
  la hoja de cita desde el chat, menú de más opciones, cuenta de usuario.
- Datos reales: el contenido mostrado sigue siendo de ejemplo.
- Barra de estado y barra de gestos dibujadas en el Figma: se usan las del sistema.

## Flujo de usuario

<!-- Cómo se inicia, qué hace el usuario y qué resultado obtiene.
Incluye pantallas afectadas, navegación y alternativas relevantes. -->

1. El usuario inicia sesión (pantalla Login, restilizada según D2) y llega a "Your library".
2. Pulsa una tarjeta de documento o "Start" y entra en el detalle con la pestaña Chat.
3. Cambia entre Chat, Document y Lab desde la barra inferior; vuelve con la flecha atrás o el gesto del sistema.

## Datos y reglas de negocio

<!-- Información que necesita el usuario, campos obligatorios, validaciones,
límites y reglas como duplicados u orden de presentación. Describe significado
y comportamiento, sin diseñar tablas, DTO, DAO ni almacenamiento. -->

- Contenido de ejemplo tomado del Figma (Annual Report 2024, Brand Guidelines,
  Research Summary, conversación y pasaje citados).
- Idioma (D1, decidido): todos los textos visibles en inglés por defecto (copia
  literal del Figma) y traducción al español; el dispositivo en español ve la app
  en español, cualquier otro idioma la ve en inglés.
- Los textos visibles se definen como recursos de texto, no fijos en el código.

## Comportamiento mobile y casos alternativos

<!-- Adapta la tabla usando MOBILE_GUIDELINES.md. Añade escenarios relevantes.
Marca No aplica con su motivo cuando corresponda. No presupongas soporte offline
ni conservación de todo el estado. Expresa resultados, no mecanismos técnicos. -->

| Situación | Comportamiento esperado |
| --- | --- |
| Carga o acción en curso | No aplica: no hay operaciones nuevas. |
| Sin datos | No aplica: datos de ejemplo fijos; no se añaden estados vacíos nuevos. |
| Entrada inválida | Igual que hoy (el envío de un mensaje vacío no hace nada). |
| Error o espera excesiva | No aplica: sin red ni operaciones. |
| Sin conexión o conexión interrumpida | No aplica: sin red. |
| Cancelar o volver atrás | Igual que hoy: atrás desde el detalle vuelve a la biblioteca. |
| Pasar a segundo plano y regresar | Se conserva la pestaña seleccionada (comportamiento actual). |
| Recrear la pantalla | Se conserva la pestaña seleccionada (comportamiento actual). |
| Reabrir después de terminarse el proceso | Igual que hoy: arranca en Login. |
| Áreas del sistema | El contenido no queda tapado por la barra de estado, la de navegación ni el teclado; el fondo crema llega hasta los bordes. |
| Tamaño de texto | Con escala de fuente grande (200 %) no hay texto cortado ni controles inaccesibles. |
| Accesibilidad | Botones solo con icono tienen descripción; áreas táctiles ≥ 48 dp; la pestaña seleccionada no se indica solo por color (también el estado seleccionado para el lector de pantalla). |
| Tema oscuro del sistema activado | La app se sigue viendo con la paleta clara del Figma; barra de estado y de navegación con iconos oscuros legibles sobre el fondo crema. |

**Puntos de la guía no aplicables y motivo:** conectividad, persistencia,
permisos, trabajo en segundo plano y privacidad de datos: el cambio es solo visual
y no toca datos, red ni capacidades del dispositivo.

## Restricciones del pedido

<!-- Condiciones ya impuestas: compatibilidad, límites de alcance, requisitos
de accesibilidad o rendimiento medibles, o una tecnología expresamente exigida.
Ejemplo: Usar Room puede ser una restricción; el diseño de entidades va en PLAN.md.
No conviertas una preferencia del agente en una restricción. -->

- No añadir funcionalidad (pedido explícito).
- Elementos con el mismo propósito o aspecto deben usar el mismo componente
  reutilizable en todas las pantallas; única fuente de verdad para componentes
  compartidos y estilos (pedido explícito).
- Fuente del diseño: Figma pYHxch462JeAupjcKe3NtO (nodos 16:159, 3:1489, 3:1568, 3:1632, 19:360).

## Criterios de aceptación

<!-- Resultados observables que permitan decidir si se cumple cada requisito.
Incluye los casos alternativos acordados. No uses Funciona correctamente.
Repite el formato según sea necesario. -->

- **CA-01 · RF-01:** Dado un dispositivo Android 12+ con color dinámico, cuando se abre cualquier pantalla, entonces fondo, superficies, texto y acentos usan los colores del Figma (no los del fondo de pantalla del usuario) y el texto se ve en Inter.
- **CA-02 · RF-02:** Dado que el usuario inicia sesión, cuando aparece la biblioteca, entonces su captura coincide con el frame "Document library" en estructura, textos, colores, radios y espaciado (tolerancia ±2 dp).
- **CA-03 · RF-03:** Dado el detalle abierto en la pestaña Chat, cuando se observa, entonces coincide con el frame "Document chat" (burbuja, asistente, citas [n] violeta, tarjeta de cita, botón copiar, compositor y aviso).
- **CA-04 · RF-04:** Dado el detalle, cuando se selecciona Document, entonces coincide con el frame "Cited PDF passage".
- **CA-05 · RF-05:** Dado el detalle, cuando se cambia de pestaña, entonces solo la pestaña activa aparece con fondo crema, icono en círculo violeta y texto violeta, y la cabecera muestra volver, título, subtítulo y más opciones.
- **CA-06 · RF-06:** Dada la vista previa de la hoja "Original source", cuando se renderiza, entonces coincide con el frame 19:360 (marcador [1], título, cerrar, identidad del documento, pasaje resaltado y botón "View original page"); y en la app ninguna acción la abre.
- **CA-07 · RF-07:** Dado el código, cuando se revisan las pantallas, entonces cada elemento compartido listado en RF-07 está definido una sola vez y ninguna pantalla define colores, radios o estilos de texto propios para ellos.
- **CA-08 · RF-08:** Dado Login y la pestaña Lab, cuando se observan, entonces usan fondo crema, superficies blancas con borde #E8E6E0, botón principal violeta tipo píldora e Inter, con los mismos componentes compartidos que el resto, y conservan los mismos campos, textos y acciones que antes.
- **CA-09 · RF-09:** Dado cualquier icono visible en las pantallas con diseño, cuando se compara con el Figma, entonces tiene la misma forma, grosor de trazo, tamaño y color; en Login y Lab se usan iconos del mismo estilo de contorno.
- **CA-11 · RF-10:** Dado el idioma del dispositivo en inglés, cuando se recorren las pantallas, entonces los textos coinciden con el Figma; dado el idioma en español, entonces todos los textos aparecen en español y ninguno queda en inglés.
- **CA-13 · RF-11:** Dado el lanzador del dispositivo, la pantalla de Login y el resto de pantallas, cuando se observan en inglés y en español, entonces el nombre de la app aparece como "DocuMind" y no aparece "Chatbot" en ningún texto visible.
- **CA-12 · RF-01:** Dado el sistema en modo oscuro, cuando se abre cualquier pantalla, entonces se ve con la paleta clara del Figma y los iconos de las barras del sistema son legibles.
- **CA-10 · Mobile:** Dado escala de fuente 200 % y navegación por gestos o por 3 botones, cuando se recorren todas las pantallas, entonces no hay contenido tapado ni cortado y todos los controles siguen siendo pulsables.

## Cómo se comprueba el comportamiento

<!-- Una fila por criterio: escenario y resultado que debemos comprobar.
La selección de tests, herramientas, comandos y evidencias se desarrolla en PLAN.md.
No marques los criterios como superados durante la especificación. -->

| Criterio | Condiciones y pasos | Resultado esperado |
| --- | --- | --- |
| CA-01 | Emulador API 34 con fondo de pantalla de color fuerte; abrir cada pantalla | Colores del Figma, fuente Inter |
| CA-02 | Login → biblioteca; captura y comparación con el frame 3:1489 | Coincidencia visual |
| CA-03 | Abrir un documento (pestaña Chat); captura vs 3:1568 | Coincidencia visual |
| CA-04 | Pestaña Document; captura vs 3:1632 | Coincidencia visual |
| CA-05 | Pulsar cada pestaña | Solo la activa resaltada; cabecera correcta |
| CA-06 | Renderizar la vista previa; pulsar "View highlighted passage" en la app | Coincide con 19:360; en la app no se abre nada |
| CA-07 | Revisión de código | Sin estilos duplicados para elementos compartidos |
| CA-08 | Abrir Login y la pestaña Lab; capturas | Estilo del sistema de diseño; mismos elementos y acciones que antes |
| CA-09 | Comparar los iconos de cada captura con el frame correspondiente | Misma forma, trazo, tamaño y color |
| CA-11 | Recorrer pantallas con el dispositivo en inglés y luego en español | Textos del Figma en inglés; todo en español en español |
| CA-13 | Ver el icono en el lanzador y recorrer pantallas en EN y ES | Siempre "DocuMind"; nunca "Chatbot" |
| CA-12 | Activar modo oscuro del sistema; recorrer pantallas | Paleta clara; barras del sistema legibles |
| CA-10 | Escala de fuente 200 %, gestos y 3 botones; recorrer pantallas | Nada tapado ni cortado |

## Decisiones pendientes

<!-- Al resolverlas, actualiza las secciones afectadas. Escribe Ninguna cuando
no queden pendientes funcionales ni restricciones por decidir. -->

- ~~D1~~: resuelto — inglés por defecto + traducción al español.
- ~~D2~~: resuelto — Login y Lab se restilizan con los componentes compartidos, sin cambiar estructura ni contenido.
- ~~D3~~: resuelto — la hoja se crea como componente con vista previa, sin conectarla.
- ~~D4~~: resuelto — SVG exportados del Figma como recursos vectoriales, sin librería nueva.
- ~~D5~~: resuelto — solo tema claro; con el sistema en modo oscuro la app mantiene la paleta del Figma.

Ninguna decisión pendiente.

<!-- ANTES DE SOLICITAR APROBACIÓN
Comprueba que el alcance está acordado, los flujos son coherentes, los puntos
mobile relevantes están cubiertos y cada requisito tiene criterios comprobables.
Resuelve las dudas y los marcadores pendientes. Mantén el diseño técnico en PLAN.md.
-->
