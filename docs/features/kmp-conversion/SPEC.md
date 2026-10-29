# SPEC: Conversión a Kotlin Multiplatform

**Estado:** Borrador <!-- Borrador | En revisión | Aprobada -->

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

DocuMind sigue siendo la misma aplicación de cuadernos para quien la usa en Android. El cambio hace que las pantallas actuales sean Kotlin compartido, de modo que una aplicación iOS posterior pueda usarlas. En Android, la persona sigue empezando en el inicio de sesión, continúa a Mis Cuadernos, abre un cuaderno y cambia entre Fuentes, Chat y Lab. Este cambio no añade cuentas, subida de documentos ni búsqueda RAG.

## Situación actual

<!-- Comportamiento actual relevante, limitación que queremos resolver y
comportamientos existentes que deben conservarse. No describas la arquitectura. -->

Hoy la aplicación es solo Android. El inicio de sesión no comprueba el correo ni la contraseña: la acción de iniciar sesión y la acción de registrarse abren la lista de cuadernos y quitan el inicio de sesión de la pila de retroceso. La lista muestra cuatro cuadernos de ejemplo, un campo de búsqueda, una acción de perfil que no hace nada y una acción de crear que abre un cuaderno titulado «Nuevo Cuaderno». Un cuaderno muestra su título y las tres pestañas. Volver regresa a la lista. El contenido de chat, fuentes y lab es interfaz local de ejemplo. No se guarda nada y no se usa la red. Android 12 y superior puede usar color dinámico; las versiones anteriores usan el tema púrpura fijo. La aplicación sigue el tema oscuro del sistema.

## Dentro del alcance

<!-- Requisitos concretos, con identificadores estables para vincularlos a
criterios, decisiones del plan y tareas. -->

- **RF-01:** En Android, la persona puede iniciar sesión sin que se compruebe el correo ni la contraseña, abrir Mis Cuadernos, escribir en la búsqueda de los cuatro cuadernos de ejemplo, abrir uno de ellos o crear «Nuevo Cuaderno», cambiar entre Fuentes, Chat y Lab, y volver a la lista. «¿Olvidaste tu contraseña?» y la acción de perfil siguen sin hacer nada. El contenido de ejemplo se mantiene.
- **RF-02:** Las pantallas, el tema y la navegación de ese flujo son código Kotlin Multiplatform compartido dentro del módulo `:app` existente.
- **RF-03:** La aplicación Android sigue abriéndose con el identificador `com.example.chatbot`. El punto de entrada Android y el manifiesto siguen siendo específicos de Android.
- **RF-04:** El proyecto declara un destino iOS para esa interfaz compartida.

## Fuera de alcance

<!-- Exclusiones acordadas, no deducidas por el agente. Si no hay exclusiones
adicionales, indícalo tras revisarlo con la persona. -->

- Una capa de dominio, almacenamiento, red, subida de documentos o RAG.
- Un destino de escritorio.
- Compilar, instalar o ejecutar una aplicación iOS en este PC con Windows.
- Dejar las pantallas solo en código Android con un módulo compartido vacío.
- Un rediseño visual o pantallas nuevas.

## Flujo de usuario

<!-- Cómo se inicia, qué hace el usuario y qué resultado obtiene.
Incluye pantallas afectadas, navegación y alternativas relevantes. -->

1. [PENDIENTE]
2. [PENDIENTE]
3. [PENDIENTE]

## Datos y reglas de negocio

<!-- Información que necesita el usuario, campos obligatorios, validaciones,
límites y reglas como duplicados u orden de presentación. Describe significado
y comportamiento, sin diseñar tablas, DTO, DAO ni almacenamiento. -->

- [PENDIENTE]

## Comportamiento mobile y casos alternativos

<!-- Adapta la tabla usando MOBILE_GUIDELINES.md. Añade escenarios relevantes.
Marca No aplica con su motivo cuando corresponda. No presupongas soporte offline
ni conservación de todo el estado. Expresa resultados, no mecanismos técnicos. -->

| Situación | Comportamiento esperado |
| --- | --- |
| Carga o acción en curso | [PENDIENTE] |
| Sin datos | [PENDIENTE] |
| Entrada inválida | [PENDIENTE] |
| Error o espera excesiva | [PENDIENTE] |
| Sin conexión o conexión interrumpida | [PENDIENTE] |
| Cancelar o volver atrás | [PENDIENTE] |
| Pasar a segundo plano y regresar | [PENDIENTE] |
| Recrear la pantalla | [PENDIENTE] |
| Reabrir después de terminarse el proceso | [PENDIENTE] |
| Otros puntos aplicables de la guía | [PENDIENTE] |

**Puntos de la guía no aplicables y motivo:** [PENDIENTE]

## Restricciones del pedido

<!-- Condiciones ya impuestas: compatibilidad, límites de alcance, requisitos
de accesibilidad o rendimiento medibles, o una tecnología expresamente exigida.
Ejemplo: Usar Room puede ser una restricción; el diseño de entidades va en PLAN.md.
No conviertas una preferencia del agente en una restricción. -->

- [PENDIENTE]

## Criterios de aceptación

<!-- Resultados observables que permitan decidir si se cumple cada requisito.
Incluye los casos alternativos acordados. No uses Funciona correctamente.
Repite el formato según sea necesario. -->

- **CA-01 · RF-01:** Dado [contexto], cuando [acción], entonces [resultado observable].
- **CA-02 · RF-02:** Dado [contexto], cuando [acción], entonces [resultado observable].

## Cómo se comprueba el comportamiento

<!-- Una fila por criterio: escenario y resultado que debemos comprobar.
La selección de tests, herramientas, comandos y evidencias se desarrolla en PLAN.md.
No marques los criterios como superados durante la especificación. -->

| Criterio | Condiciones y pasos | Resultado esperado |
| --- | --- | --- |
| CA-01 | [PENDIENTE] | [PENDIENTE] |
| CA-02 | [PENDIENTE] | [PENDIENTE] |

## Decisiones pendientes

<!-- Al resolverlas, actualiza las secciones afectadas. Escribe Ninguna cuando
no queden pendientes funcionales ni restricciones por decidir. -->

- [PENDIENTE]

<!-- ANTES DE SOLICITAR APROBACIÓN
Comprueba que el alcance está acordado, los flujos son coherentes, los puntos
mobile relevantes están cubiertos y cada requisito tiene criterios comprobables.
Resuelve las dudas y los marcadores pendientes. Mantén el diseño técnico en PLAN.md.
-->
