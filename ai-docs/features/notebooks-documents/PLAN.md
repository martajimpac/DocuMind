# PLAN: Cuadernos y documentos reales (crear, subir PDF y listar)

**SPEC de referencia:** `ai-docs/features/notebooks-documents/SPEC.md`
**Versión de la spec revisada:** Aprobada el 2026-11-04 (en el índice de git, sin commit todavía)
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

| Componente o archivo existente | Ruta verificada | Responsabilidad y uso previsto |
| --- | --- | --- |
| Módulo KMP compartido | `shared/build.gradle.kts` | Todo el código de la app (commonMain, androidMain, iosMain). Se añaden aquí las dependencias nuevas |
| Cliente Supabase y DI (Koin) | `shared/src/commonMain/kotlin/com/example/chatbot/di/AppModule.kt` | Hoy solo instala `Auth`. Se añaden `Postgrest` y `Storage`, el repositorio y los ViewModels nuevos |
| Patrón de repositorio | `domain/repository/AuthRepository.kt`, `data/repository/SupabaseAuthRepository.kt` | El dominio oculta Supabase y los errores se traducen a un enum de dominio. Se replica para los cuadernos |
| Patrón de ViewModel | `ui/screens/login/LoginViewModel.kt` | `StateFlow<UiState>` + `Channel` de eventos de una sola vez, más `SavedStateHandle` para lo que debe sobrevivir |
| Eventos de una sola vez | `ui/utils/ObserveAsEvents.kt` | Recoge los eventos solo con la pantalla en STARTED. Se usa para navegar al detalle tras crear un cuaderno |
| Formato según el idioma | `ui/utils/PercentFormat.kt` (+ `.android.kt`, `.ios.kt`) | Patrón `expect`/`actual` para formatear con el locale. Se replica para fechas y tamaños |
| Mis Cuadernos | `ui/screens/notebooks/NotebooksScreen.kt` | Hoy usa `sampleDocuments` fijos. Pasa a leer un ViewModel |
| Detalle | `ui/screens/detail/NotebookDetailScreen.kt`, `DocumentReaderTab.kt` | Recibe solo el título. Pasa a recibir el id del cuaderno; la pestaña Document añade la sección de fuentes |
| Rutas y navegación | `navigation/Routes.kt`, `navigation/AppNavigation.kt` | `NotebookDetailRoute(title)` pasa a `NotebookDetailRoute(notebookId, title)` |
| Sesión | `navigation/SessionViewModel.kt` | Ya devuelve al inicio de sesión cuando la sesión deja de ser válida. Se reutiliza para el caso "sesión caducada" |
| Componentes del diseño | `ui/components/` (`DmSurfaceCard`, `DmPrimaryButton`, `DmTextButton`, `DocumentListItem`, `SectionHeader`, `DmInlineAction`, `SystemBarsProtection`) | Se reutilizan para el estado vacío, el progreso, los errores y "añadir fuente" (sin diseño nuevo en Figma) |
| Tests compartidos | `shared/src/commonTest/...` (`FakeAuthRepository`, `MainDispatcherTest`) | Patrón de fakes y de dispatcher para los tests nuevos de ViewModels |
| Proyecto Supabase "DocuMind" | `zaftthtyrjjqrnwddebm` (eu-north-1) | Verificado con MCP: sin tablas en `public` ni buckets. `pgcrypto` y `uuid-ossp` instalados. No existe la carpeta `supabase/` ni la CLI en este PC |

**Convenciones y patrón de referencia:**
- Arquitectura MVVM + repositorio, sin casos de uso, igual que Auth: UI → ViewModel → Repository (dominio) → implementación Supabase (data).
- Koin para la DI y `@Serializable` para las rutas tipadas.
- Textos en `composeResources` (inglés y español).
- No se escriben colores, tamaños ni tipografías fuera de `ui/theme` y `ui/components`.

## Solución propuesta

<!-- Explica el enfoque y sus motivos. Describe las responsabilidades y el
recorrido de datos y eventos hasta la interfaz. Usa un diagrama si aporta claridad. -->

La app habla directamente con Supabase (restricción de la SPEC): Storage para
los PDFs y Postgres para los cuadernos y las fuentes. Toda la seguridad la
aplica el servidor (RLS, límites del bucket y una función transaccional), de
modo que una petición directa sin la app tiene las mismas restricciones (RF-08,
CA-08, CA-11).

**Subida sin dejar nada a medias (RF-04, CA-04, CA-12).** El orden es "primero el
archivo, después las filas":

```
Selector del sistema ─► PickedPdf (nombre, bytes)
        │
        ▼
PdfValidator (no vacío, ≤ 20 MB, empieza por "%PDF-") ──✗──► error con el motivo (CA-03)
        │ ✓
        ▼
Storage: subir a documents/{user_id}/{notebook_id}/{document_id}.pdf  (progreso)
        │ falla o se cancela ─► borrar el objeto si llegó a crearse ─► error / nada
        ▼
RPC create_notebook_with_document(...)  o  add_document(...)   ← una sola transacción
        │ falla (red, límite de 20) ─► borrar el objeto subido ─► error
        ▼
Filas visibles ─► la lista y el detalle las muestran
```

- La app genera los UUID del cuaderno y del documento antes de subir, así la ruta del archivo se conoce de antemano.
- Las filas solo existen si la función SQL las crea en una única transacción.
- La función comprueba que el objeto existe en Storage y toma su tamaño real de los metadatos del objeto, no de lo que diga la app.
- Por eso nunca se ve un cuaderno vacío ni una fuente sin archivo.
- Si el proceso muere entre la subida y la función, puede quedar un archivo huérfano en Storage, invisible para la persona (ver Riesgos).

**Límite de 20 fuentes (RF-02, CA-11).**
- La app desactiva "añadir fuente" cuando un cuaderno tiene 20 fuentes y explica por qué.
- En el servidor, un trigger `before insert` sobre `documents` bloquea la fila del cuaderno (`for update`) y rechaza la fuente 21. Así se cubren también las peticiones directas y las subidas simultáneas.

**Lista (RF-05, RF-06, CA-06, CA-07).**
- Una consulta PostgREST devuelve los cuadernos del usuario, ordenados por `created_at desc`, con sus fuentes embebidas (`documents(size_bytes)`).
- El número de fuentes y el tamaño total se calculan en la app. Con 20 fuentes como máximo por cuaderno, el coste es despreciable.

**Selección del archivo.** Es un `expect`/`actual` propio, `rememberPdfPicker`:
- **Android**: `ActivityResultContracts.OpenDocument(arrayOf("application/pdf"))`. Lee el nombre y el tamaño con `OpenableColumns` **antes** de cargar los bytes; un archivo de más de 20 MB se rechaza sin leerlo. Los bytes se leen en `Dispatchers.IO`.
- **iOS**: `UIDocumentPickerViewController` con el tipo PDF.
- No requiere permisos de almacenamiento.

**Estado y eventos.**
- `NotebooksViewModel` lleva la lista y la creación de cuadernos.
- `NotebookDetailViewModel` lleva el cuaderno abierto y "añadir fuente".
- La subida se ejecuta en `viewModelScope`: sobrevive al giro y al paso a segundo plano mientras el proceso viva (CA-09, CA-12) y se cancela con el `Job`.
- La navegación al detalle tras crear un cuaderno es un evento de una sola vez (`Channel` + `ObserveAsEvents`).

## Módulos y componentes afectados

<!-- Si el proyecto está modularizado, identifica los módulos afectados, sus
responsabilidades y la dirección de sus dependencias. Respeta los límites
existentes y justifica cualquier módulo o dependencia nueva. Si no está
modularizado, describe las carpetas o componentes afectados sin introducir
modularización fuera del alcance; marca la tabla de módulos como No aplica. -->

| Módulo | Existe / nuevo | Responsabilidad y cambios | Dependencias afectadas |
| --- | --- | --- | --- |
| `:shared` | Existente | Dominio, datos, UI y `expect`/`actual` del selector y de los formatos | Añade `postgrest-kt` y `storage-kt` (mismo BOM de Supabase) |
| `:app` | Existente | Sin cambios de código. `MainActivity` ya hospeda `App()`, y el selector usa el `ActivityResultRegistry` de Compose | Ninguna |
| `supabase/migrations/` | Nuevo (carpeta) | SQL versionado del esquema, RLS, bucket y funciones, aplicado con el MCP de Supabase | Ninguna en Gradle |

<!-- Distingue lo que se reutiliza, modifica o crea. Las rutas nuevas son propuestas.
Señala impacto sobre modelos, contratos o componentes compartidos. -->

Rutas relativas a `shared/src/commonMain/kotlin/com/example/chatbot/` salvo que se indique otra.

| Componente o ruta | Acción | Cambio y responsabilidad | Requisito relacionado |
| --- | --- | --- | --- |
| `supabase/migrations/<timestamp>_notebooks_documents.sql` (raíz del repo) | Crear | Tablas `notebooks` y `documents`, RLS, trigger del límite, funciones `create_notebook_with_document` y `add_document`, bucket `documents` y políticas de `storage.objects` | RF-01, RF-02, RF-03, RF-08 |
| `domain/model/Notebook.kt` | Crear | `Notebook(id, title, createdAt, sourceCount, totalSizeBytes)`, `NotebookError` (Network, LimitReached, InvalidFile, Unknown), `UploadProgress` | RF-05 |
| `domain/model/PickedPdf.kt` | Crear | Nombre, tamaño y bytes del archivo elegido | RF-01, RF-03 |
| `domain/validation/PdfValidator.kt` | Crear | Vacío, > 20 MB, cabecera `%PDF-` → `PdfError`; título = nombre sin ".pdf" | RF-01, RF-03 |
| `domain/repository/NotebookRepository.kt` | Crear | `getNotebooks()`, `getNotebook(id)`, `createNotebook(pdf)` y `addDocument(notebookId, pdf)` como `Flow` de progreso y resultado | RF-01, RF-02, RF-05 |
| `data/repository/SupabaseNotebookRepository.kt` | Crear | Storage + RPC + consulta, limpieza del objeto si algo falla, traducción de errores | RF-01…RF-05, RF-08 |
| `data/remote/NotebookDtos.kt` | Crear | DTO `@Serializable` de la consulta y de los parámetros de las RPC | RF-05 |
| `di/AppModule.kt` | Modificar | `install(Postgrest)`, `install(Storage)`, repositorio, validador y ViewModels nuevos | — |
| `ui/files/PdfPicker.kt` + `androidMain/.../PdfPicker.android.kt` + `iosMain/.../PdfPicker.ios.kt` | Crear | `expect`/`actual` `rememberPdfPicker` | RF-01, RF-02 |
| `ui/utils/DateFormat.kt`, `ui/utils/FileSizeFormat.kt` (+ `actual`) | Crear | Fecha corta y tamaño ("2,4 MB") según el idioma | RF-05, RF-09 |
| `ui/screens/notebooks/NotebooksViewModel.kt` | Crear | Estados Loading / Content / Empty / Error, crear cuaderno, progreso, reintento, cancelar | RF-01, RF-04…RF-06 |
| `ui/screens/notebooks/NotebooksScreen.kt` | Modificar | Quita `sampleDocuments`. `LazyColumn` con `contentPadding` del Scaffold; estado vacío, carga, error y progreso; `BackHandler` con diálogo durante la subida | RF-04…RF-07 |
| `ui/screens/detail/NotebookDetailViewModel.kt` | Crear | Carga el cuaderno por id, añade fuentes, controla el límite de 20 y la cancelación | RF-02, RF-04 |
| `ui/screens/detail/NotebookDetailScreen.kt`, `DocumentReaderTab.kt` | Modificar | Recibe el ViewModel. La pestaña Document muestra "N sources" y el botón "Add source" (o el aviso de límite), el progreso y los errores, sobre el lector de ejemplo; diálogo al volver atrás durante una subida | RF-02, RF-04, RF-07 |
| `ui/components/UploadProgressCard.kt` | Crear | Tarjeta compartida (lista y detalle): nombre del archivo, barra de progreso y error con "Retry". Construida con `DmSurfaceCard` y tokens del tema | RF-04 |
| `navigation/Routes.kt`, `navigation/AppNavigation.kt` | Modificar | `NotebookDetailRoute(notebookId, title)`; navegación tras crear un cuaderno | RF-07 |
| `composeResources/values*/strings.xml` | Modificar | Textos nuevos en inglés y en español; se eliminan los textos de ejemplo de la lista que dejen de usarse | RF-09 |
| `commonTest/.../FakeNotebookRepository.kt` + tests | Crear | Fake y tests de `PdfValidator`, `NotebooksViewModel` y `NotebookDetailViewModel` | CA-01…CA-07, CA-11, CA-12 |

## Datos y contratos

<!-- Completa solo lo aplicable. Si un punto no aplica, indica el motivo. -->

- **Modelos y contratos de entrada y salida:**
  - `notebooks(id uuid pk, user_id uuid not null default auth.uid() references auth.users on delete cascade, title text not null check (char_length between 1 and 200), created_at timestamptz not null default now())`
  - `documents(id uuid pk, notebook_id uuid not null references notebooks on delete cascade, user_id uuid not null default auth.uid() references auth.users on delete cascade, file_name text not null, size_bytes bigint not null check (size_bytes > 0 and size_bytes <= 20971520), storage_path text not null unique, created_at timestamptz not null default now())`
  - RPC `create_notebook_with_document(p_notebook_id uuid, p_title text, p_document_id uuid, p_file_name text) returns notebooks`
  - RPC `add_document(p_notebook_id uuid, p_document_id uuid, p_file_name text) returns documents`
  - *(Ajuste en T1, 2026-11-04: las funciones no reciben la ruta del archivo; la calculan en el servidor con `private.document_path`, así no dependen de lo que envíe la app.)*
  - `file_name` admite de 1 a 255 caracteres y `title` de 1 a 200. La app recorta el nombre a esos límites antes de llamar.
- **Identificadores, relaciones y restricciones:**
  - Los UUID los genera la app (`kotlin.uuid.Uuid`).
  - Ruta del archivo: `{user_id}/{notebook_id}/{document_id}.pdf`. El servidor la construye con el `auth.uid()` de quien llama.
  - Errores propios: `file_not_found` (P0002, el archivo no está en Storage), `source_limit_reached` (P0001) y `not_authenticated` (28000).
  - Índices en `notebooks(user_id, created_at desc)`, `documents(notebook_id)` y `documents(user_id)`.
  - Las dos funciones son `security invoker`, con `search_path` fijo y `execute` solo para `authenticated`.
- **Origen de los datos mostrados y transformaciones:**
  - Consulta `notebooks?select=id,title,created_at,documents(size_bytes)&order=created_at.desc`.
  - `sourceCount` y `totalSizeBytes` se calculan en el repositorio.
  - La fecha y el tamaño se formatean en la UI según el idioma.
- **Persistencia, consultas y actualizaciones:**
  - Solo en Supabase. RLS activado en las dos tablas, con políticas `select` e `insert` `to authenticated` y la condición `(select auth.uid()) = user_id`.
  - En `documents`, la condición de `insert` exige además que el cuaderno sea de quien inserta.
  - No hay políticas de `update` ni `delete` en las tablas: quedan fuera de alcance.
  - Bucket privado `documents` con `file_size_limit = 20971520` y `allowed_mime_types = {application/pdf}`.
  - Políticas en `storage.objects` (`insert`, `select` y `delete`) restringidas a `bucket_id = 'documents'` y a la carpeta `(storage.foldername(name))[1] = auth.uid()::text`. El `delete` solo sirve para retirar el archivo cuando la subida no se completa; la app no ofrece borrar.
- **Convivencia entre datos locales y remotos:** No aplica. No hay caché local (fuera de alcance); la lista se pide al servidor cada vez que se abre Mis Cuadernos o se reintenta.
- **Compatibilidad y migraciones de datos existentes:** No hay datos previos: el esquema `public` está vacío. La migración se guarda en el repositorio y se aplica una vez con el MCP de Supabase (no hay CLI instalada).

## Estado, operaciones y errores

<!-- Cómo se implementan los comportamientos aprobados en la spec.
Referencia RF/CA y aplica las consideraciones relevantes de MOBILE_GUIDELINES.md. -->

- **Gestión del estado de interfaz y navegación:**
  - `NotebooksUiState(list: Loading | Content(notebooks) | Empty | Error(error), upload: UploadState?)`, con `UploadState(fileName, progress, error?)`.
  - El detalle tiene un estado equivalente, con `notebook` y `canAddSource` (falso si tiene 20 fuentes o si hay una subida en curso).
  - Al terminar de crear un cuaderno se emite el evento `OpenNotebook(id, title)`.
  - Al volver del detalle, Mis Cuadernos recarga la lista para reflejar las fuentes añadidas (CA-02).
- **Conservación y restauración del estado:**
  - El ViewModel conserva la lista y la subida al girar la pantalla.
  - La posición de la lista la guarda `rememberLazyListState` (CA-09).
  - Tras la muerte del proceso, la subida no se restaura (SPEC) y la lista se vuelve a pedir.
  - Los bytes del PDF no van al `SavedStateHandle`: son demasiado grandes.
- **Ejecución, concurrencia y cancelación de operaciones:**
  - La lectura del archivo se hace en `Dispatchers.IO`; la red ya la gestiona Ktor sin bloquear.
  - Solo puede haber una subida por ViewModel: `onPick` / `onAddSource` no hacen nada si ya hay un `Job` activo, y además el botón queda desactivado (CA-05).
  - Cancelar desde el diálogo de "Atrás" cancela el `Job`. Un bloque `NonCancellable` borra el objeto si llegó a subirse, y la pantalla sale (CA-12).
  - "No" en el diálogo no interrumpe nada.
- **Errores, reintentos y prevención de duplicados:**
  - `HttpRequestException` → `Network`.
  - Error del trigger (código SQL propio, p. ej. `P0001` con el mensaje `source_limit_reached`) → `LimitReached`.
  - Rechazo del bucket por tipo o tamaño → `InvalidFile`.
  - Otros → `Unknown`.
  - "Retry" vuelve a lanzar la misma subida con los bytes que ya están en memoria y con UUID nuevos, para no chocar con un objeto que se hubiera quedado subido.
  - Si la sesión caduca, `SessionViewModel` ya lleva al inicio de sesión.
- **Otras consideraciones mobile aplicables y su solución:**
  - **Accesibilidad**: el progreso y los errores usan `liveRegion` para que el lector de pantalla los anuncie, y los botones nuevos tienen etiqueta y al menos 48 dp de área táctil (los componentes `Dm*` ya lo cumplen).
  - **Edge-to-edge**: la lista recibe el `innerPadding` del `Scaffold` como `contentPadding` y se mantiene `SystemBarsProtection`.
  - **Textos grandes**: las tarjetas crecen en altura (`heightIn(min)`, ya usado por `DocumentListItem`).
  - **Memoria**: el PDF se rechaza por tamaño antes de leerlo, y solo hay un PDF en memoria a la vez.

## Dependencias y configuración

<!-- Librerías, servicios, permisos o configuración afectados. Verifica compatibilidad
con el proyecto y justifica las incorporaciones. No agregues dependencias por defecto. -->

- `io.github.jan-tennert.supabase:postgrest-kt` y `storage-kt`, sin versión propia: las fija el BOM 3.2.6 que ya se usa. Son los módulos oficiales del mismo SDK, compatibles con Kotlin 2.2.
  - Se ha verificado en la documentación de Supabase para Kotlin que `uploadAsFlow` devuelve `UploadStatus.Progress` / `Success`.
  - Hay que confirmar al compilar que la firma en la 3.2.6 coincide.
- No se añade ninguna librería para elegir archivos ni para fechas: se usan `expect`/`actual` y `kotlin.time` / `kotlin.uuid`, que ya están en la librería estándar de Kotlin 2.2.
- **Supabase**: se crean la migración y el bucket `documents`. No hace falta ninguna clave nueva; se sigue usando la clave publicable.
- **Android**: no hacen falta permisos nuevos (el selector del sistema da acceso al archivo elegido). `INTERNET` ya está declarado.

## Estrategia de validación

<!-- Una fila por criterio de la spec. Selecciona el método capaz de demostrarlo:
test unitario, integración, UI o prueba manual. No todos requieren todos los métodos.
Identifica tests existentes y separa los nuevos propuestos. Incluye regresiones relevantes.
Una captura aislada no demuestra persistencia ni ausencia de peticiones de red. -->

| Criterio | Método y test existente o propuesto | Entorno y datos necesarios | Evidencia prevista |
| --- | --- | --- | --- |
| CA-01 | Test nuevo de `NotebooksViewModel` (progreso → evento `OpenNotebook`) + prueba manual | Emulador, cuenta confirmada, PDF pequeño | Resultado del test; capturas del progreso, del detalle y de la lista con "1 source"; fila visible en Supabase |
| CA-02 | Test de `NotebookDetailViewModel` + prueba manual | Emulador, mismo cuaderno, segundo PDF | Test; captura con "2 sources" y el tamaño sumado |
| CA-03 | Tests de `PdfValidator` (vacío, 20 MB + 1 byte, cabecera falsa, límite exacto) + prueba manual con los 3 archivos | Archivos de prueba copiados al emulador | Tests; capturas de los 3 mensajes; lista sin cambios |
| CA-04 | Test con fake que falla y reintenta + prueba manual con modo avión | Emulador | Test; capturas; Storage y tablas consultados con MCP para comprobar que no queda nada |
| CA-05 | Test: dos llamadas seguidas lanzan una sola subida | — | Resultado del test |
| CA-06 | Test de mapeo y orden + prueba manual con 3 cuadernos | Emulador | Test; captura comparada con la consulta SQL |
| CA-07 | Test del estado `Empty` + prueba manual con una cuenta nueva | Segunda cuenta confirmada | Test; captura |
| CA-08 | Prueba de RLS con SQL (`set role authenticated` + `request.jwt.claims` de B) con MCP `execute_sql` + prueba manual con dos cuentas | Cuentas A y B | Resultado de las consultas: 0 filas y errores al insertar o descargar lo de A |
| CA-09 | Prueba manual: girar con la lista cargada y durante una subida | Emulador | Capturas antes y después |
| CA-10 | Prueba manual con la app en `es-ES` + revisión de los `.cvr` generados | Emulador | Capturas |
| CA-11 | Test del ViewModel (20 fuentes → botón desactivado) + SQL directo: insertar la fuente 21 | Cuaderno con 20 fuentes, creado con SQL para la prueba | Test; error `source_limit_reached` |
| CA-12 | Tests del ViewModel (cancelar limpia y no deja filas; "No" mantiene la subida) + prueba manual (Inicio y volver; Atrás → No / Sí) | Emulador, PDF de varios MB para tener tiempo | Tests; capturas; comprobación con MCP de que no queda nada |

**Comprobaciones de regresión:**
- Los 43 tests actuales de `:shared:testAndroidHostTest`.
- `DocumentTabBarTest` instrumentado.
- Inicio y cierre de sesión, restablecimiento de contraseña, pestañas del detalle.
- Búsqueda de tokens del diseño fuera de `ui/theme` y `ui/components`: debe salir vacía.
- `get_advisors` (seguridad y rendimiento) de Supabase sin avisos nuevos.

**Comandos verificados para compilar y ejecutar tests:**
- `./gradlew :shared:compileCommonMainKotlinMetadata :app:assembleDebug`
- `./gradlew :shared:testAndroidHostTest`
- `./gradlew :app:connectedDebugAndroidTest` (con `ANDROID_SERIAL=emulator-5554`)
- `./gradlew lint`

Todos se usaron en la validación de kmp-conversion.

**Pruebas en dispositivo, emulador o simulador:**
- Emulador `Medium_Phone` (API 37), siempre con `-s emulator-5554` para no instalar nada en el móvil personal.
- Los PDFs de prueba se copian con `adb push` a Descargas.
- Hace falta una segunda cuenta confirmada para CA-07 y CA-08 (la crea la usuaria).

**Limitaciones del entorno:**
- El `actual` de iOS del selector y de los formatos se escribe pero **no se compila** en Windows. Queda pendiente para un Mac.
- La subida con red lenta se simula con el modo avión, no con limitación de ancho de banda.

<!-- Esta sección planifica la validación. Durante la implementación, registra
en TASKS.md o en el informe de validación acordado los resultados y evidencias
reales. Distingue pruebas ejecutadas, fallidas, no ejecutadas y bloqueadas.
Compilar o tener tests en verde no sustituye revisar los criterios de la spec. -->

## Orden de implementación

<!-- Etapas y dependencias principales. El desglose ejecutable se escribe en TASKS.md.
Incluye puntos de comprobación para avanzar con cambios pequeños. -->

1. **Backend en Supabase**: migración SQL, aplicación con MCP, `get_advisors` y pruebas SQL de RLS y del límite (CA-08, CA-11 en el servidor). *Comprobación:* las consultas SQL dan el resultado esperado.
2. **Dominio y datos**: modelos, `PdfValidator` con sus tests, repositorio y DTO, Postgrest y Storage en Koin. *Comprobación:* tests y compilación de commonMain.
3. **Selector y formatos**: `expect`/`actual` de Android (iOS escrito, sin compilar). *Comprobación:* `:app:assembleDebug`.
4. **Mis Cuadernos**: ViewModel con sus tests, pantalla con los estados, creación con progreso y navegación con el id. *Comprobación:* CA-01, CA-03…CA-07 y CA-09 en el emulador.
5. **Detalle**: ViewModel con sus tests, sección de fuentes en Document, límite y diálogo al volver atrás. *Comprobación:* CA-02, CA-11 y CA-12.
6. **Textos y validación final**: inglés y español, regresiones, lint y registro en TASKS.md. *Comprobación:* CA-10 y la lista de regresiones.

## Riesgos y decisiones pendientes

<!-- Riesgos concretos de esta solución y cómo se resolverán, sin listas genéricas.
Escribe Ninguna en las decisiones pendientes cuando estén resueltas. -->

- **Riesgos y medidas acordadas:**
  - **Archivos huérfanos en Storage** si el proceso muere justo entre la subida y la función SQL. Son invisibles para la persona y no ocupan cuadernos ni fuentes, pero sí espacio. *Medida:* se acepta en esta funcionalidad y se anota para la futura de "borrar", que podrá limpiarlos con un job programado. No se añade ahora (fuera de alcance).
  - **Validación del PDF en el servidor**: el bucket comprueba el tamaño y el tipo declarado, pero no la cabecera `%PDF-`, que solo comprueba la app. Una petición directa podría subir otro archivo con tipo `application/pdf` a su propia carpeta privada. *Medida:* no afecta a otros usuarios. Se volverá a validar el contenido cuando el RAG procese los PDFs.
  - **Firma de `uploadAsFlow` en supabase-kt 3.2.6**: está documentada para la versión actual. Si difiere, se usa `upload` sin progreso fino y se muestra un progreso indeterminado, y se avisa antes de cambiar el comportamiento.
  - **Memoria**: un PDF de 20 MB en un `ByteArray` es asumible. La subida reanudable (TUS) queda descartada por ahora: añade complejidad que la SPEC no pide.
  - **iOS sin compilar**: el `actual` de iOS puede tener errores que solo se verán en un Mac.
- **Decisiones pendientes:** Ninguna.

## Ajuste tras la validación (2026-11-05)

Aceptado por la usuaria («continúa con lo que quieras», 2026-11-05) después de presentarle la incidencia registrada en TASKS.md.

- **Problema verificado:**
  - Con la red del emulador a UMTS, una subida de 3 MB se para en el 8 % y falla a los 15 s con `SocketTimeoutException`.
  - `storage-kt` da a las transferencias un tiempo total propio (`transferTimeout`), pero el motor OkHttp mantiene sus 10 s por defecto para cada lectura o escritura, porque `supabase-kt` no fija `socketTimeoutMillis`.
- **Cambio** en `di/AppModule.kt`, al crear el cliente:
  - `httpEngine = createHttpEngine(60 s)`, con `expect`/`actual` en `data/remote/HttpEngine*.kt`: OkHttp con `readTimeout` y `writeTimeout` de 60 s en Android; Darwin con `timeoutIntervalForRequest` de 60 s en iOS, sin compilar. Tolera hasta 60 s sin avanzar en una lectura o escritura. Se descartó `httpConfig { install(HttpTimeout) }` porque `supabase-kt` lo marca como API interna.
  - `install(Storage) { transferTimeout = 5 min }`: tiempo máximo de una subida completa. Con 1 Mbps, un PDF de 20 MB tarda unos 160 s.
  - El resto de peticiones mantiene el tiempo total de 10 s de `supabase-kt`, así que no esperan más de lo que esperaban antes.
- **Validación:**
  - Repetir en el emulador, a UMTS, la subida de 3 MB que fallaba: debe completarse.
  - Probar en el emulador «Cancel upload» de CA-12, que antes estaba bloqueado.
  - Tests y compilación completos.

