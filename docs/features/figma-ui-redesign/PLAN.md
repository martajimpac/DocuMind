# PLAN: Rediseño visual según Figma (DocuMind · Soft Intelligence)

**SPEC de referencia:** docs/features/figma-ui-redesign/SPEC.md
**Versión de la spec revisada:** aprobada por la persona el 2026-10-29 (D1–D5 resueltas)
**Estado:** Aprobado <!-- Borrador | En revisión | Aprobado -->

<!-- PARA LA PERSONA
Copia esta plantilla como PLAN.md junto a la SPEC.md aprobada.
Este documento define la solución técnica. Una vez revisado, el agente puede
derivar TASKS.md con tareas, dependencias y comprobaciones.
-->

<!-- PARA EL AGENTE
- Lee la SPEC.md aprobada, las instrucciones del proyecto y MOBILE_GUIDELINES.md.
  Si falta un documento necesario o la spec no está aprobada, indícalo antes de avanzar.
- Inspecciona el repositorio. Referencia rutas verificadas y distingue las nuevas propuestas.
- Propón una solución proporcional al alcance y coherente con el proyecto.
  Reutiliza lo existente y justifica nuevas dependencias o cambios de arquitectura.
- Distingue hechos, decisiones confirmadas y propuestas. Consulta las decisiones
  no resueltas; haz pocas preguntas por vez y actualiza el plan con las respuestas.
- Referencia los requisitos y criterios por su ID, sin copiar toda la spec.
- Si una decisión cambia el comportamiento o alcance, vuelve a la spec y solicita
  confirmación. No resuelvas una duda de producto mediante una suposición técnica.
- Conserva estos comentarios. No implementes durante la planificación.
- Solicita aprobación antes de marcar el plan como Aprobado. La autorización
  para implementar debe ser explícita; no se deduce del estado de los documentos.
-->

## Contexto técnico verificado

<!-- Qué existe hoy y cómo participa en la funcionalidad. -->

Raíz de código: `app/src/main/java/com/example/chatbot/` (en adelante `…/`).

| Componente o archivo existente | Ruta verificada | Responsabilidad y uso previsto |
| --- | --- | --- |
| Activity única | `…/MainActivity.kt` | Ya llama a `enableEdgeToEdge()` sin argumentos; se ajustará el estilo de barras (CA-12). |
| Navegación | `…/navigation/AppNavigation.kt` | Login → Notebooks → NotebookDetail(title). Se conserva; solo cambia el título por defecto a recurso de texto. |
| Tema | `…/ui/theme/Theme.kt`, `Color.kt`, `Type.kt` | Plantilla por defecto con color dinámico y tema oscuro. Se reemplaza por los tokens del Figma. |
| Login | `…/ui/screens/login/LoginScreen.kt` | Restilizar (RF-08). |
| Biblioteca | `…/ui/screens/notebooks/NotebooksScreen.kt` | Rehacer según frame 3:1489 (RF-02). Tiene un cambio local sin commitear del usuario (quita el título "Todos los Cuadernos"); se sustituye por el diseño nuevo. |
| Detalle | `…/ui/screens/detail/NotebookDetailScreen.kt` | Cabecera + barra de pestañas (RF-05). Pestaña seleccionada con `rememberSaveable` (se conserva). |
| Pestañas | `…/detail/ChatTab.kt`, `FuentesTab.kt`, `LabTab.kt` | Chat (RF-03), Fuentes → Document (RF-04), Lab (RF-08). |
| Recursos | `app/src/main/res/values/strings.xml`, `themes.xml`, `AndroidManifest.xml` | Solo `app_name`; tema XML `android:Theme.Material.Light.NoActionBar`; `adjustResize` ya configurado. |
| Tests | `app/src/test/…/ExampleUnitTest.kt`, `app/src/androidTest/…/ExampleInstrumentedTest.kt` | Solo ejemplos; Compose UI test (`ui-test-junit4`) ya está en dependencias. |
| Build | `app/build.gradle.kts`, `gradle/libs.versions.toml` | Compose BOM 2026.02.01, Material3, material-icons-extended, compileSdk 37, targetSdk 36, minSdk 24. |

**Convenciones y patrón de referencia:** proyecto de un solo módulo, UI 100 %
Compose con pantallas `@Composable` sin ViewModel, datos de ejemplo locales,
`@Preview` por pantalla envuelto en `ChatbotTheme`. Se mantiene ese patrón (no se
introducen ViewModels: no hay funcionalidad nueva).

## Solución propuesta

<!-- Explica el enfoque y sus motivos. Describe las responsabilidades y el
recorrido de datos y eventos hasta la interfaz. Usa un diagrama si aporta claridad. -->

Tres capas, cada una con una única responsabilidad (RF-07):

```
ui/theme       ← tokens: colores, tipografía, formas, espaciado (única fuente de estilo)
   ▲
ui/components  ← componentes compartidos; solo leen tokens del tema
   ▲
ui/screens     ← componen pantallas con componentes; no definen colores, radios ni tamaños de texto
```

1. **Tokens (RF-01, D5).** `ChatbotTheme` pasa a usar siempre un `lightColorScheme`
   fijo con la paleta del Figma (sin color dinámico ni tema oscuro). Mapeo Material 3:
   `background` #F7F6F2, `surface` #FFFFFF, `onBackground/onSurface` #202124,
   `primary` #7771E8, `onPrimary` #FFFFFF, `primaryContainer` #F2F0F8,
   `onPrimaryContainer` #7771E8, `onSurfaceVariant` #6B6B66,
   `outline/outlineVariant` #E8E6E0. Los valores sin hueco en Material 3 (verde de
   éxito #4D9275, asa de hoja #D7D5CF) van en un objeto de colores extendidos
   expuesto con `CompositionLocal` y accesible como `DocuMindTheme.colors`.
   Formas (`Shapes`: 8/12/16/20/28 dp y píldora), espaciado y elevación suave
   (sombras del Figma: 2–4 dp, alfa 3–4 %) también como tokens.
2. **Tipografía (RF-01).** Inter empaquetada en `res/font` (Regular 400, Medium 500,
   SemiBold 600, Bold 700) y una `Typography` de Material 3 cuyas ranuras cubren la
   escala del Figma (28/21/20/16/15/14/13/12/11/10 sp; interlineado 1.5–1.55 en
   textos de lectura). Ninguna pantalla fija `fontSize` ni `fontWeight` a mano.
3. **Iconos (RF-09, D4).** Los SVG del Figma se descargan y se convierten a vector
   drawables (`res/drawable/ic_*.xml`), uno por icono distinto, y se pintan con
   `tint` desde el tema. Para Login/Lab, que usan iconos ausentes en el Figma
   (correo, candado, ojo, herramientas del Lab), se usan los iconos
   `Icons.Outlined.*` de material-icons-extended (ya en dependencias), que son de
   contorno y no añaden librerías.
4. **Componentes compartidos** (`ui/components/`, todos con `@Preview`):

   | Componente | Dónde se usa | Figma |
   | --- | --- | --- |
   | `DmSurfaceCard` (blanca, borde #E8E6E0, radio configurable por token, sombra suave, clic opcional) | tarjeta "Create notebook", tarjetas de documento, burbuja de usuario, tarjeta de cita, página PDF, Login, Lab | 3:1506, 3:1522, 3:1593, 3:1605, 3:1659 |
   | `DmPrimaryButton` (píldora violeta, texto + icono opcional al final o al inicio) | Start, View original page, Iniciar sesión, Generar (Lab) | 3:1510, 19:382 |
   | `DmPillField` (campo píldora con borde, placeholder, icono inicial y acción final opcionales) | buscador, compositor del chat, campos de Login | 3:1513, 3:1624 |
   | `DmIconButton` (48 dp, variante plana y con borde/tarjeta) | atrás, más opciones, copiar, cerrar, página anterior/siguiente | 3:1576, 3:1614, 19:367 |
   | `DmIconTile` (recuadro redondeado con icono o texto; variantes acento / suave / con borde) | marca DocuMind, marca del asistente, marcador [1], símbolo de documento, iconos de pestaña, iconos del Lab | 3:1497, 3:1597, 19:364, 19:371 |
   | `DmAvatar` (círculo #F2F0F8 con iniciales violeta) | cuenta en biblioteca | 3:1500 |
   | `DocumentTopBar` (atrás, título, subtítulo, más opciones; blanco con borde inferior) | detalle (todas las pestañas) | 3:1575 |
   | `DocumentTabBar` + `DocumentTab` (3 pestañas tipo tarjeta, seleccionada en crema + violeta) | detalle | 3:1583 |
   | `HighlightedPassage` (fondo #F2F0F8, borde izquierdo violeta 3 dp, radio 12) | página PDF, hoja de cita | 3:1670, 19:379 |
   | `DocumentCover` (miniatura "PDF" con reglas) | tarjeta de documento | 3:1523 |
   | `DocumentListItem` (cubierta + título + metadatos + actividad opcional en verde + chevron) | biblioteca | 3:1522 |
   | `CitationCard` | chat | 3:1605 |
   | `SectionHeader` (título + contador + acción secundaria) | biblioteca ("Recent notebooks 3 · Last opened") , Lab | 3:1517 |
   | `CitationSourceSheetContent` | solo `@Preview` (D3) | 19:360 |

   Las variantes de un mismo componente (p. ej. icono de pestaña seleccionado o no)
   se expresan con parámetros, no con copias.
5. **Pantallas.** Cada pantalla se reescribe como composición de los componentes
   anteriores con el contenido de ejemplo del Figma. Las interacciones que ya existen
   se mantienen (navegar al detalle, volver, cambiar de pestaña, escribir y enviar en
   el chat local, mostrar/ocultar contraseña); los controles nuevos del diseño sin
   funcionalidad (buscar, ordenar, copiar, cita, zoom, paginación, más opciones,
   cerrar hoja) quedan con `onClick` vacío y sin efecto (fuera de alcance de la spec).
   - Biblioteca: "Start" y las tarjetas navegan al detalle (igual que hoy el FAB y las tarjetas). Desaparecen el FAB, el carrusel y la lista duplicada, que no están en el diseño.
   - Chat: desaparecen los chips de sugerencias y las horas, que no están en el diseño. El asistente se muestra como texto sobre fondo (sin burbuja) y el usuario en burbuja blanca a la derecha, como en el Figma.
   - Document: la pestaña "Fuentes" pasa a "Document" con la página PDF de ejemplo y los controles del lector. La lista de fuentes actual desaparece (no figura en el diseño).
   - Orden de pestañas: Chat, Document, Lab; pestaña inicial Chat (como en el Figma).
6. **Textos (RF-10, D1).** Todos los textos visibles y `contentDescription` pasan a
   `res/values/strings.xml` (inglés, copia del Figma) y `res/values-es/strings.xml`
   (español). Los recuentos usan `plurals`.

## Módulos y componentes afectados

<!-- Si el proyecto está modularizado, identifica los módulos afectados, sus
responsabilidades y la dirección de sus dependencias. Respeta los límites
existentes y justifica cualquier módulo o dependencia nueva. Si no está
modularizado, describe las carpetas o componentes afectados sin introducir
modularización fuera del alcance; marca la tabla de módulos como No aplica. -->

| Módulo | Existe / nuevo | Responsabilidad y cambios | Dependencias afectadas |
| --- | --- | --- | --- |
| No aplica: proyecto de un solo módulo `:app`; no se introduce modularización. | — | — | — |

<!-- Distingue lo que se reutiliza, modifica o crea. Las rutas nuevas son propuestas.
Señala impacto sobre modelos, contratos o componentes compartidos. -->

| Componente o ruta | Acción | Cambio y responsabilidad | Requisito relacionado |
| --- | --- | --- | --- |
| `…/ui/theme/Color.kt` | Modificar | Paleta del Figma; elimina Purple/Pink | RF-01 |
| `…/ui/theme/Theme.kt` | Modificar | Esquema claro fijo + `DocuMindTheme.colors` extendidos | RF-01, CA-12 |
| `…/ui/theme/Type.kt` | Modificar | Familia Inter + escala tipográfica | RF-01 |
| `…/ui/theme/Shape.kt`, `Dimens.kt` (propuestos) | Crear | Radios, espaciado, elevación | RF-07 |
| `app/src/main/res/font/inter_*.ttf` (propuestos) | Crear | Inter 400/500/600/700 | RF-01 |
| `app/src/main/res/drawable/ic_*.xml` (propuestos) | Crear | Iconos del Figma como vectores | RF-09 |
| `…/ui/components/*.kt` (propuestos) | Crear | Componentes de la tabla anterior | RF-06, RF-07 |
| `…/ui/screens/notebooks/NotebooksScreen.kt` | Modificar | Biblioteca | RF-02 |
| `…/ui/screens/detail/NotebookDetailScreen.kt` | Modificar | Cabecera + barra de pestañas, orden de pestañas | RF-05 |
| `…/ui/screens/detail/ChatTab.kt` | Modificar | Chat | RF-03 |
| `…/ui/screens/detail/FuentesTab.kt` → renombrar a `DocumentReaderTab.kt` | Modificar | Lector de documento | RF-04 |
| `…/ui/screens/detail/LabTab.kt` | Modificar | Restilizado | RF-08 |
| `…/ui/screens/login/LoginScreen.kt` | Modificar | Restilizado | RF-08 |
| `…/MainActivity.kt` | Modificar | `enableEdgeToEdge(SystemBarStyle.light(...))` en ambas barras | CA-12, CA-10 |
| `…/navigation/AppNavigation.kt` | Modificar | Título por defecto desde recurso | RF-10 |
| `res/values/strings.xml`, `res/values-es/strings.xml` (nuevo) | Modificar / crear | Textos EN + ES; `app_name` = "DocuMind" (no se traduce) | RF-10, RF-11 |
| `…/ui/screens/login/LoginScreen.kt` (marca) | Modificar | "Chatbot AI" → marca DocuMind (mismo `DmIconTile` de marca que la biblioteca) | RF-11 |
| `res/values/themes.xml` | Modificar | `android:windowBackground` crema para evitar destello blanco/negro al arrancar | RF-01 |
| `app/src/androidTest/…/DocumentTabBarTest.kt` (propuesto) | Crear | Test de UI de pestañas | CA-05 |

## Datos y contratos

<!-- Completa solo lo aplicable. Si un punto no aplica, indica el motivo. -->

- **Modelos y contratos de entrada y salida:** solo modelos de UI de ejemplo
  (p. ej. documento de biblioteca con título, metadatos y actividad opcional), locales a cada pantalla como hoy.
- **Identificadores, relaciones y restricciones:** No aplica: no hay datos persistentes.
- **Origen de los datos mostrados y transformaciones:** contenido de ejemplo fijo del Figma, desde recursos de texto.
- **Persistencia, consultas y actualizaciones:** No aplica.
- **Convivencia entre datos locales y remotos:** No aplica.
- **Compatibilidad y migraciones de datos existentes:** No aplica.

## Estado, operaciones y errores

<!-- Cómo se implementan los comportamientos aprobados en la spec.
Referencia RF/CA y aplica las consideraciones relevantes de MOBILE_GUIDELINES.md. -->

- **Gestión del estado de interfaz y navegación:** sin cambios de rutas. La pestaña
  activa sigue en `rememberSaveable`, ahora sobre un `enum` ordenado Chat/Document/Lab.
- **Conservación y restauración del estado:** igual que hoy (pestaña con
  `rememberSaveable`; texto de campos con `remember`, sin cambios de alcance).
- **Ejecución, concurrencia y cancelación de operaciones:** No aplica.
- **Errores, reintentos y prevención de duplicados:** No aplica; el envío vacío en el chat sigue ignorándose.
- **Otras consideraciones mobile aplicables y su solución:**
  - *Edge-to-edge (skill `edge-to-edge`):* `Scaffold` en cada pantalla con
    `containerColor` crema; `innerPadding` a `contentPadding` de las listas;
    `DocumentTopBar` y `DocumentTabBar` aplican sus propios insets
    (`statusBars` / `navigationBars`) para que su fondo blanco llegue al borde;
    compositor del chat y Login con `imePadding`/`contentWindowInsets` sin doble
    relleno; `isNavigationBarContrastEnforced = false` porque hay barra inferior.
  - *Iconos de barras del sistema (CA-12):* `ComponentActivity.enableEdgeToEdge`
    por defecto elige iconos claros si el sistema está en modo oscuro, lo que sería
    ilegible sobre crema; se fuerza `SystemBarStyle.light(...)` en ambas barras.
  - *Texto grande (CA-10):* sin alturas fijas en contenedores con texto (se usan
    mínimos, `heightIn`), textos con `maxLines` + elipsis donde el Figma es de una
    línea, contenido desplazable.
  - *Accesibilidad:* `contentDescription` en botones solo-icono; áreas táctiles
    ≥ 48 dp; pestañas con `Modifier.selectable(role = Role.Tab)` para exponer el
    estado seleccionado; contraste: #6B6B66 sobre #F7F6F2 ≈ 5,0:1 y blanco sobre
    #7771E8 ≈ 4,1:1 (texto 14 sp SemiBold). Este último no llega a 4,5:1 para texto
    normal; se registra como riesgo (ver abajo) sin cambiar el color aprobado.

## Dependencias y configuración

<!-- Librerías, servicios, permisos o configuración afectados. Verifica compatibilidad
con el proyecto y justifica las incorporaciones. No agregues dependencias por defecto. -->

- **Sin dependencias Gradle nuevas.** Se usan Material3, material-icons-extended y
  ui-test-junit4, ya declarados.
- **Fuente Inter** (licencia SIL OFL 1.1) empaquetada como recurso en
  `res/font`, descargada del repositorio oficial (github.com/rsms/inter, release
  estable) en archivos estáticos 400/500/600/700. Se descarta la fuente
  descargable de Google Fonts porque añade una dependencia y depende de Google Play
  Services y de red en el primer arranque. Impacto ≈ 1,2 MB en el APK.
- **SVG del Figma**: URLs temporales (caducan en 7 días) descargadas una vez y
  convertidas a vector drawables con un script local en el directorio temporal de
  la sesión (no se añade al repositorio).

## Estrategia de validación

<!-- Una fila por criterio de la spec. Selecciona el método capaz de demostrarlo:
test unitario, integración, UI o prueba manual. No todos requieren todos los métodos.
Identifica tests existentes y separa los nuevos propuestos. Incluye regresiones relevantes.
Una captura aislada no demuestra persistencia ni ausencia de peticiones de red. -->

| Criterio | Método y test existente o propuesto | Entorno y datos necesarios | Evidencia prevista |
| --- | --- | --- | --- |
| CA-01 | Manual: capturas de cada pantalla | Emulador `Medium_Phone` (API ≥ 31, color dinámico) con fondo de pantalla de color fuerte | Capturas con paleta del Figma e Inter |
| CA-02 | Manual: captura vs frame 3:1489 | Emulador, idioma EN | Captura lado a lado con el Figma |
| CA-03 | Manual: captura vs 3:1568 | Ídem | Captura lado a lado |
| CA-04 | Manual: captura vs 3:1632 | Ídem | Captura lado a lado |
| CA-05 | Nuevo test Compose UI `DocumentTabBarTest`: al pulsar cada pestaña solo ella queda `isSelected()`; + captura | Emulador | Resultado de `connectedDebugAndroidTest` + captura |
| CA-06 | `@Preview` de `CitationSourceSheetContent` vs 19:360 (render en emulador con una actividad de preview de Android Studio o captura del preview); en la app pulsar "View highlighted passage" no abre nada | Emulador | Captura del preview + captura tras pulsar |
| CA-07 | Revisión de código: `grep` de `Color(0x`, `fontSize`, `RoundedCornerShape(` y `.sp` fuera de `ui/theme` y `ui/components` = 0 resultados | Repositorio | Salida del `grep` |
| CA-08 | Manual: capturas de Login y Lab antes/después | Emulador | Capturas comparadas |
| CA-09 | Manual: iconos en capturas vs frames | Emulador | Capturas |
| CA-10 | Manual: escala de fuente 200 % (`adb shell settings put system font_scale 2.0`) y navegación de 3 botones; recorrer pantallas y abrir teclado en chat y login | Emulador | Capturas |
| CA-11 | Manual: recorrer con idioma EN y ES (`adb shell` cambio de locale o ajustes) + lint `MissingTranslation` | Emulador | Capturas en ambos idiomas + informe lint sin `MissingTranslation`/`HardcodedText` |
| CA-13 | Manual: captura del lanzador + recorrido EN/ES; `grep -ri chatbot app/src/main/res` sin textos visibles | Emulador + repositorio | Capturas + salida del `grep` |
| CA-12 | Manual: modo oscuro del sistema (`adb shell cmd uimode night yes`) | Emulador | Capturas con paleta clara e iconos de barras oscuros |

**Comprobaciones de regresión:** login → biblioteca → detalle → atrás; cambio de
pestaña y rotación conservan la pestaña; envío de mensaje en el chat añade la
burbuja y vacía el campo; ver/ocultar contraseña.

**Comandos verificados para compilar y ejecutar tests:**
- `./gradlew :app:assembleDebug` (verificado el 2026-10-29: compila sin errores en el estado actual).
- `./gradlew :app:lintDebug` y `./gradlew :app:connectedDebugAndroidTest` (tareas estándar de AGP; se ejecutarán durante la validación).
- adb/emulador: `%LOCALAPPDATA%/Android/Sdk/platform-tools/adb.exe`, `…/emulator/emulator.exe -avd Medium_Phone` (verificada su existencia).

**Pruebas en dispositivo, emulador o simulador:** todas en el emulador
`Medium_Phone`; no se necesita hardware real.

**Limitaciones del entorno:** la comparación con el Figma es visual (no hay test
de capturas automatizado en el proyecto y la spec no lo pide). El render del
`@Preview` de la hoja se comprobará en el emulador; si no puede lanzarse desde la
línea de comandos, quedará registrado como comprobación pendiente en Android Studio.

## Orden de implementación

<!-- Etapas y dependencias principales. El desglose ejecutable se escribe en TASKS.md.
Incluye puntos de comprobación para avanzar con cambios pequeños. -->

1. **Fundamentos:** tokens de color, formas, espaciado, fuente Inter, tipografía,
   tema claro fijo, `themes.xml` y barras del sistema. *Comprobación:* compila; la app actual ya se ve en crema/violeta/Inter.
2. **Recursos:** iconos vectoriales del Figma y `strings.xml` EN/ES. *Comprobación:* compila; lint sin traducciones faltantes.
3. **Componentes compartidos** con `@Preview` (incluida la hoja). *Comprobación:* compila; previews revisados.
4. **Pantallas con diseño:** detalle (cabecera + pestañas) → Chat → Document → Biblioteca. *Comprobación:* captura de cada una vs Figma.
5. **Pantallas sin diseño:** Login y Lab. *Comprobación:* capturas.
6. **Test de pestañas y validación completa** (CA-01…CA-12, regresiones, `grep` de CA-07, lint). Registro en TASKS.md.

## Riesgos y decisiones pendientes

<!-- Riesgos concretos de esta solución y cómo se resolverán, sin listas genéricas.
Escribe Ninguna en las decisiones pendientes cuando estén resueltas. -->

- **Riesgos y medidas acordadas:**
  - *Pequeñas variaciones del Figma unificadas* para tener una única fuente de
    verdad: #F3F0F8 y #F2F0F8 (avatar/cubierta vs pasaje) → un solo token #F2F0F8;
    botón principal de 48 dp/14 sp (Start) y 52 dp/15 sp (View original page) → un
    único tamaño 52 dp/15 sp; títulos de 21 sp (marca) y 20 sp (hoja) se mantienen
    como estilos distintos solo si encajan en ranuras distintas de la tipografía; si
    no, se unifican a 20 sp. Diferencias ≤ 4 dp, dentro de la tolerancia razonable
    pero fuera de ±2 dp en el botón de "Start"; se señalarán en la validación.
  - *Contraste del botón violeta* (blanco sobre #7771E8 ≈ 4,1:1): cumple AA para
    texto grande/negrita ≥ 14 sp SemiBold según WCAG solo de forma ajustada; se
    mantiene el color aprobado y se informa.
  - *Conversión SVG → vector:* algunos elementos SVG (círculos, rectángulos con
    `rx`) requieren traducirse a `pathData`; se verificará cada icono visualmente
    en las capturas (CA-09).
  - *Cambio local sin commitear en `NotebooksScreen.kt`:* el archivo se reescribe
    por completo con el diseño nuevo; el cambio del usuario (quitar "Todos los
    Cuadernos") queda absorbido porque esa sección desaparece.
  - *Skill `android-jetpack-compose`:* cargada el 2026-10-29 tras reiniciar la
    sesión. Se aplican sus pautas: `Modifier` como primer parámetro opcional,
    componentes sin estado con el estado elevado a la pantalla, API de slots donde
    aporte flexibilidad, claves estables en listas `Lazy*` y sin efectos ni cálculos
    pesados durante la composición.
- *Nombre interno:* el paquete `com.example.chatbot`, el `applicationId` y
  `Theme.Chatbot` no son visibles para el usuario y no se renombran (cambiar el
  `applicationId` crearía otra app instalada); RF-11 se limita a textos visibles.
- **Decisiones pendientes:** ninguna.
