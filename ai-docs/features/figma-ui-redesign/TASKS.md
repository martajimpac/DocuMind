# TASKS: Rediseño visual según Figma (DocuMind · Soft Intelligence)

**SPEC:** docs/features/figma-ui-redesign/SPEC.md (Aprobada)
**PLAN:** docs/features/figma-ui-redesign/PLAN.md (Aprobado)
**Implementación:** autorizada por la persona el 2026-10-29 ("authorize implementation").

Estados: `Pendiente` · `En curso` · `Hecha` · `Bloqueada`.
Rutas: `…/` = `app/src/main/java/com/example/chatbot/`.

Pautas transversales (skills `android-jetpack-compose`, `edge-to-edge`, AGENTS.md):
`Modifier` como primer parámetro opcional; componentes sin estado (estado elevado a
la pantalla); claves estables en `Lazy*`; ningún color, radio, `fontSize` ni `.sp`
fuera de `ui/theme` y `ui/components`; antes de crear un componente, buscar uno
equivalente; variantes por parámetro, no por copia.

## Etapa 1 · Fundamentos

| ID | Objetivo | Alcance | Depende de | Cubre | Validación | Estado |
| --- | --- | --- | --- | --- | --- | --- |
| T01 | Tokens de color del Figma | `ui/theme/Color.kt`: paleta; `Theme.kt`: `lightColorScheme` fijo (sin color dinámico ni oscuro) + `DocuMindTheme.colors` (éxito #4D9275, asa #D7D5CF) vía `CompositionLocal` | — | RF-01, CA-01, CA-12 | `assembleDebug` OK | Hecha |
| T02 | Formas, espaciado y elevación | Crear `ui/theme/Shape.kt` (8/12/16/20/28 dp, píldora) y `Dimens.kt` | — | RF-07 | `assembleDebug` OK | Hecha |
| T03 | Fuente Inter y tipografía | Descargar Inter 400/500/600/700 (rsms/inter, OFL) a `res/font/inter_*.ttf`; `Type.kt` con la escala 28…10 sp e interlineados 1.5–1.55 | — | RF-01 | `assembleDebug` OK; tamaño APK anotado | Hecha |
| T04 | Barras del sistema y fondo de arranque | `MainActivity`: `enableEdgeToEdge(SystemBarStyle.light(...))` en ambas barras, `isNavigationBarContrastEnforced = false`; `themes.xml`: `windowBackground` crema | T01 | CA-12, CA-10 | Captura en modo claro y oscuro (`cmd uimode night yes`) | Hecha |

## Etapa 2 · Recursos

| ID | Objetivo | Alcance | Depende de | Cubre | Validación | Estado |
| --- | --- | --- | --- | --- | --- | --- |
| T05 | Iconos del Figma como vectores | Descargar SVG de los frames 3:1489, 3:1568, 3:1632, 19:360; convertir a `res/drawable/ic_*.xml` (script en el scratchpad, no en el repo); un drawable por icono distinto | — | RF-09, CA-09 | `assembleDebug` OK; cada icono revisado visualmente en T11–T17 | Hecha |
| T06 | Textos EN/ES y nombre DocuMind | `res/values/strings.xml` (EN, copia del Figma, `app_name` = "DocuMind" no traducible, `plurals`); crear `res/values-es/strings.xml`; título por defecto de `AppNavigation.kt` desde recurso | — | RF-10, RF-11, CA-11, CA-13 | `lintDebug` sin `MissingTranslation`; `grep -ri chatbot app/src/main/res` sin textos visibles | Hecha |

## Etapa 3 · Componentes compartidos (`…/ui/components/`, todos con `@Preview`)

| ID | Objetivo | Alcance | Depende de | Cubre | Validación | Estado |
| --- | --- | --- | --- | --- | --- | --- |
| T07 | Básicos | `DmSurfaceCard`, `DmPrimaryButton` (52 dp/15 sp, icono opcional), `DmPillField`, `DmIconButton` (plano / con borde, 48 dp), `DmIconTile` (acento / suave / con borde), `DmAvatar` | T01–T03 | RF-07 | `assembleDebug` OK; previews compilan | Hecha |
| T08 | Cabecera y pestañas de documento | `DocumentTopBar` (insets `statusBars`), `DocumentTabBar` + `DocumentTab` sin estado, `selectable(role = Role.Tab)`, insets `navigationBars` | T05, T07 | RF-05, CA-05 | `assembleDebug` OK | Hecha |
| T09 | Contenido de documento | `HighlightedPassage`, `DocumentCover`, `DocumentListItem`, `CitationCard`, `SectionHeader` | T05, T07 | RF-02, RF-03, RF-04, RF-07 | `assembleDebug` OK | Hecha |
| T10 | Hoja "Original source" | `CitationSourceSheetContent` solo con `@Preview`, sin conectarla a ninguna acción | T07, T09 | RF-06, CA-06 | Preview renderizado (o comprobación pendiente en Android Studio registrada) | Hecha |

## Etapa 4 · Pantallas con diseño

| ID | Objetivo | Alcance | Depende de | Cubre | Validación | Estado |
| --- | --- | --- | --- | --- | --- | --- |
| T11 | Detalle del cuaderno | `NotebookDetailScreen.kt`: `Scaffold` con `DocumentTopBar` + `DocumentTabBar`; `enum` Chat/Document/Lab, inicial Chat, `rememberSaveable` | T08 | RF-05, CA-05 | Captura; rotación conserva la pestaña | Hecha |
| T12 | Chat | `ChatTab.kt` según 3:1568: burbuja de usuario, asistente sin burbuja, marcadores [n] violeta, `CitationCard`, copiar, compositor con `imePadding`, aviso; se quitan chips y horas; envío local se conserva | T09, T11 | RF-03, CA-03 | Captura vs Figma; enviar mensaje añade burbuja y vacía el campo; teclado no tapa el compositor | Hecha |
| T13 | Document | Renombrar `FuentesTab.kt` → `DocumentReaderTab.kt`; página PDF de ejemplo + controles del lector (sin funcionalidad) | T09, T11 | RF-04, CA-04 | Captura vs 3:1632 | Hecha |
| T14 | Biblioteca | `NotebooksScreen.kt` según 3:1489: cabecera de marca + avatar, "Your library", tarjeta "Create new notebook", buscador, "Recent notebooks", 3 `DocumentListItem` (claves estables), aviso de privacidad; se quitan FAB, carrusel y lista duplicada; "Start" y tarjetas navegan al detalle | T07, T09 | RF-02, CA-02 | Captura vs Figma; navegación al detalle y vuelta | Hecha |

## Etapa 5 · Pantallas sin diseño

| ID | Objetivo | Alcance | Depende de | Cubre | Validación | Estado |
| --- | --- | --- | --- | --- | --- | --- |
| T15 | Login | Restilizar con `DmSurfaceCard`, `DmPillField`, `DmPrimaryButton`, marca `DmIconTile` + "DocuMind"; mismos campos y acciones; `imePadding` | T07 | RF-08, RF-11, CA-08, CA-13 | Captura antes/después; ver/ocultar contraseña; login navega | Hecha |
| T16 | Lab | Restilizar `LabTab.kt` con componentes compartidos e `Icons.Outlined.*`; mismo contenido y acciones | T07, T11 | RF-08, CA-08, CA-09 | Captura antes/después | Hecha |

## Etapa 6 · Test y validación

| ID | Objetivo | Alcance | Depende de | Cubre | Validación | Estado |
| --- | --- | --- | --- | --- | --- | --- |
| T17 | Test de pestañas | Crear `app/src/androidTest/…/DocumentTabBarTest.kt`: al pulsar cada pestaña solo ella está `isSelected()` | T08 | CA-05 | `connectedDebugAndroidTest` OK | Hecha |
| T18 | Revisión de estilos únicos | `grep` de `Color(0x`, `fontSize`, `RoundedCornerShape(`, `.sp` fuera de `ui/theme` y `ui/components` = 0 | T11–T16 | RF-07, CA-07 | Salida del `grep` | Hecha |
| T19 | Validación completa en emulador | Skill `mobiai-mobile-verification`; `Medium_Phone`: capturas vs Figma, EN/ES, modo oscuro, `font_scale 2.0`, gestos y 3 botones, regresiones (login → biblioteca → detalle → atrás); `lintDebug`; informe de componentes reutilizados/creados/duplicaciones (AGENTS.md); staging de los archivos de la tarea | T01–T18 | CA-01…CA-13 | Resultados registrados abajo | Hecha |

## Registro de validación

Entorno: emulador `Medium_Phone` (API 37, 1080×2400, 420 dpi). Capturas guardadas en
el directorio temporal de la sesión (no se versionan). Fecha: 2026-10-30.

| Criterio | Resultado | Evidencia |
| --- | --- | --- |
| CA-01 | Superado | Capturas de las 5 pantallas: paleta del Figma e Inter. El emulador usa color dinámico (Android 12+) y la app no lo aplica. |
| CA-02 | Superado con desviaciones | Captura de la biblioteca vs 3:1489. Desviaciones en la tabla siguiente (D-1, D-2). |
| CA-03 | Superado con desviación | Captura del chat vs 3:1568. Burbuja ajustada al texto (D-3). |
| CA-04 | Superado | Captura de Document vs 3:1632. |
| CA-05 | Superado | `DocumentTabBarTest` (3 tests) en `connectedDebugAndroidTest`: 0 fallos. Captura de cada pestaña. |
| CA-06 | Superado | Render en el emulador de `CitationSourceSheetContent` (test temporal, no versionado) vs 19:360; en la app "View highlighted passage" no abre nada. |
| CA-07 | Superado | `grep -rnE "Color\(0x\|fontSize\|RoundedCornerShape\(\|\.sp\|FontWeight\."` fuera de `ui/theme` y `ui/components`: 0 resultados. |
| CA-08 | Superado | Capturas de Login y Lab: mismos campos, textos y acciones; componentes compartidos. |
| CA-09 | Superado | Iconos del Figma (17 vectores) visibles en las capturas con forma y color correctos; Login/Lab con `Icons.Outlined.*`. |
| CA-10 | Superado con limitación | `font_scale 2.0` + navegación de 3 botones: Login, biblioteca, chat, Document y Lab sin contenido tapado. Se corrigieron 3 problemas encontrados (acción "Last opened", portada, protección de barras). El teclado del emulador es flotante (teclado físico), así que no se pudo comprobar el desplazamiento del compositor con un teclado en pantalla completo (L-1). |
| CA-11 | Superado | Recorrido en inglés y en español (`cmd locale set-app-locales … es-ES`): todo traducido. `lintDebug` sin `MissingTranslation` ni `HardcodedText`. |
| CA-12 | Superado | `cmd uimode night yes`: paleta clara e iconos de barra oscuros. |
| CA-13 | Superado | `grep -rni chatbot app/src/main/res` sin textos visibles; Login y lanzador muestran "DocuMind". |
| Build y tests | Superado | Ejecución final del 2026-10-30: `assembleDebug` correcto; `testDebugUnitTest` 1/1; `connectedDebugAndroidTest` 4/4 (3 de `DocumentTabBarTest` + ejemplo); `lintDebug` 0 errores (13 avisos previos: recursos de plantilla sin usar y versiones de dependencias). |
| Regresiones | Superado | Login → biblioteca → detalle → atrás; rotación conserva la pestaña; enviar mensaje añade la burbuja y vacía el campo; ver/ocultar contraseña presente. |

### Desviaciones respecto al Figma (decididas durante la implementación)

| ID | Pantalla | Desviación | Motivo |
| --- | --- | --- | --- |
| D-1 | Biblioteca | El encabezado "Recent notebooks" ocupa 48 dp en lugar de 24 dp, lo que añade unos 12 dp de separación arriba y abajo. | La acción "Last opened" mantiene el área táctil mínima de 48 dp. |
| D-2 | Biblioteca | El botón "Start" mide 52 dp/15 sp (Figma: 48 dp/14 sp). | Botón principal único acordado en el PLAN. |
| D-3 | Chat | La burbuja del usuario se ajusta al texto (máx. 86 % del ancho) en vez de tener 312 dp fijos. | Mensajes cortos no deben ocupar una burbuja ancha. |
| D-4 | Global | Títulos 21 sp (marca) y 20 sp (hoja) unificados a 20 sp; radios 9/10 dp unificados a 10 dp; subtítulo de cabecera 12/13 sp unificado a 13 sp. | Única fuente de verdad (PLAN). |
| D-5 | Biblioteca y Login | Fondo crema translúcido detrás de las barras del sistema. | Legibilidad de los iconos del sistema al desplazar el contenido (skill `edge-to-edge`). |

### Limitaciones

- **L-1:** comprobación del teclado en pantalla completo pendiente en un dispositivo o emulador sin teclado físico. El chat usa `imePadding()` y el Login `contentWindowInsets = safeDrawing`, según la skill `edge-to-edge`.

### Informe de componentes (AGENTS.md)

- **Reutilizados (existentes):** ninguno. El proyecto no tenía componentes compartidos.
- **Creados (`ui/components/`):** `DmSurfaceCard`, `HighlightedPassage`, `UserMessageBubble`, `SystemBarsProtection`, `Modifier.softShadow`, `Modifier.hairlineEdge`, `DmPrimaryButton`, `DmTextButton`, `DmIconButton` (Plain/Outlined/Filled), `DmIconTile` (Accent/Soft/Neutral/Outlined), `DmAvatar`, `BrandLockup`, `DmPillField` (Surface/Recessed), `DocumentTopBar`, `DocumentTabBar`, `DocumentCover`, `DocumentListItem`, `CitationCard`, `CitedParagraph`, `SectionHeader`, `DmInlineAction`, `CitationSourceSheetContent`.
- **Duplicaciones eliminadas:** 4 estilos de botón, 3 de campo de texto, 6 tarjetas `Card` con colores propios, 2 barras superiores y los colores fijos del Lab pasan a los componentes y tokens anteriores. `NotebookCard`, `NotebookRowItem`, `CreateNotebookCard`, `QuickAddSourceCard`, `ChatMessageItem` y `LabToolCard` (versión anterior) se eliminan o se sustituyen.
- **Duplicaciones pendientes:** ninguna detectada.
