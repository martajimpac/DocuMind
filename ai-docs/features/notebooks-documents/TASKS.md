# TASKS: Cuadernos y documentos reales (crear, subir PDF y listar)

SPEC: `ai-docs/features/notebooks-documents/SPEC.md` (Aprobada) · PLAN: `ai-docs/features/notebooks-documents/PLAN.md` (Aprobado)

Estados: Pendiente · En curso · Hecha · Bloqueada

| ID | Objetivo | Alcance | Depende de | RF / CA | Validación | Estado |
| --- | --- | --- | --- | --- | --- | --- |
| T1 | Esquema y seguridad en Supabase | `supabase/migrations/<timestamp>_notebooks_documents.sql`: tablas `notebooks` y `documents`, índices, RLS, trigger del límite de 20, funciones `create_notebook_with_document` y `add_document`, bucket `documents` (privado, 20 MB, `application/pdf`) y políticas de `storage.objects`. Aplicar con MCP `apply_migration` | — | RF-01, RF-02, RF-03, RF-08 · CA-08, CA-11 | `get_advisors` (seguridad y rendimiento) sin avisos nuevos; SQL con los JWT de dos usuarios: B no ve ni inserta en A; la fuente 21 se rechaza; una ruta de otra carpeta se rechaza | Hecha |
| T2 | Dominio y validación | `domain/model/Notebook.kt`, `PickedPdf.kt`, `domain/validation/PdfValidator.kt`, `domain/repository/NotebookRepository.kt` | — | RF-01, RF-03, RF-05 · CA-03 | `PdfValidatorTest` (vacío, 20 MB exactos, 20 MB + 1 byte, cabecera falsa, título sin ".pdf") en `:shared:testAndroidHostTest` | Hecha |
| T3 | Repositorio Supabase y DI | `postgrest-kt` y `storage-kt` en `libs.versions.toml` y `shared/build.gradle.kts`; `data/remote/NotebookDtos.kt`; `data/repository/SupabaseNotebookRepository.kt` (subida con progreso, RPC, limpieza del objeto, errores); `di/AppModule.kt` | T1, T2 | RF-01…RF-05, RF-08 | `:shared:compileCommonMainKotlinMetadata` y `:app:assembleDebug` en verde; firma de `uploadAsFlow` confirmada en 3.2.6 | Hecha |
| T4 | Selector de PDF y formatos | `ui/files/PdfPicker.kt` + `actual` de Android (`OpenDocument`, `OpenableColumns`, lectura en IO) y de iOS (`UIDocumentPickerViewController`, sin compilar); `ui/utils/DateFormat.kt` y `FileSizeFormat.kt` + `actual` | T2 | RF-01, RF-02, RF-05 | `:app:assembleDebug` en verde; el selector se abre filtrado a PDF en el emulador | Hecha |
| T5 | Componente de progreso de subida | `ui/components/UploadProgressCard.kt` (nombre, barra, error con "Retry", `liveRegion`) con vista previa | — | RF-04 | Búsqueda de tokens del diseño fuera de `ui/theme` y `ui/components` sin resultados; la vista previa compila | Hecha |
| T6 | ViewModel de Mis Cuadernos | `ui/screens/notebooks/NotebooksViewModel.kt` + `commonTest/.../FakeNotebookRepository.kt` + `NotebooksViewModelTest` | T2, T3 | RF-01, RF-04, RF-05, RF-06 · CA-01, CA-04…CA-07, CA-12 | Tests: carga → Content / Empty / Error; crear → progreso → `OpenNotebook`; un solo `Job`; fallo → Retry; cancelar no deja nada | Hecha |
| T7 | Pantalla de Mis Cuadernos y navegación | `NotebooksScreen.kt` (`LazyColumn` con `contentPadding`, estados, progreso, `BackHandler` con diálogo, recarga al volver), `Routes.kt` (`NotebookDetailRoute(notebookId, title)`), `AppNavigation.kt`, textos | T4, T5, T6 | RF-01, RF-04…RF-07 · CA-01, CA-03…CA-07, CA-09, CA-12 | Emulador: CA-01, CA-03…CA-07, CA-09, CA-12 con capturas; Supabase consultado con MCP | Hecha |
| T8 | ViewModel del detalle | `ui/screens/detail/NotebookDetailViewModel.kt` + `NotebookDetailViewModelTest` | T2, T3 | RF-02, RF-04 · CA-02, CA-05, CA-11, CA-12 | Tests: carga por id; añadir fuente → recuento; 20 fuentes → `canAddSource = false`; `LimitReached` del servidor; cancelar | Hecha |
| T9 | Detalle: sección de fuentes | `NotebookDetailScreen.kt` y `DocumentReaderTab.kt` ("N sources", "Add source" o aviso de límite, progreso, error, diálogo al volver atrás), textos | T4, T5, T8, T7 | RF-02, RF-04, RF-07 · CA-02, CA-11, CA-12 | Emulador: CA-02, CA-11 (cuaderno de 20 fuentes preparado con SQL), CA-12 | Hecha |
| T10 | Validación final | Textos en inglés y español, limpieza de textos de ejemplo sin uso, regresiones, lint, CA-08 con dos cuentas, registro de resultados | T1–T9 | RF-08, RF-09 · CA-08, CA-10 y todas las regresiones del PLAN | `:shared:testAndroidHostTest`, `:app:connectedDebugAndroidTest`, `lint`, `get_advisors`, búsqueda de tokens; emulador en `es-ES`; prueba con las cuentas A y B | Hecha (iOS sin compilar) |
| T11 | Tiempos de espera de las subidas (ajuste del PLAN tras la validación) | `data/remote/HttpEngine.kt` (+ `actual` de Android y de iOS) y `di/AppModule.kt`: `httpEngine` con 60 s por lectura o escritura y `transferTimeout` de 5 min | T3 | RF-04 · CA-04, CA-12 | Subida de 3 MB a UMTS: antes fallaba en rojo; repetir después; tests, compilación y lint | Hecha (validación en red lenta no concluyente, ver registro) |

## Registro de validación

### T1 · Supabase (2026-11-04)

- Migración `supabase/migrations/20261104180000_notebooks_documents.sql` aplicada con MCP (`notebooks_documents`).
- `get_advisors` de seguridad: ningún aviso nuevo. Siguen los que ya había: la función `public.rls_auto_enable()` (SECURITY DEFINER) se puede ejecutar como `anon` y `authenticated`, y la protección contra contraseñas filtradas está desactivada. Quedan fuera de esta funcionalidad.
- `get_advisors` de rendimiento: solo un aviso informativo de índices sin usar (las tablas están vacías).
- Prueba de seguridad con SQL. Se simularon las dos cuentas existentes como `authenticated` y después `anon`, dentro de un bloque que se deshace al final; luego se comprobó que quedan 0 cuadernos, 0 documentos y 0 objetos:

| # | Escenario | Resultado |
| --- | --- | --- |
| 1 | A sube un objeto y llama a `create_notebook_with_document` | OK, `size_bytes` = 1234, tomado del objeto |
| 2 | A sube a la carpeta de B | Rechazado por RLS de `storage.objects` |
| 3 | Crear un cuaderno sin archivo | `file_not_found` |
| 4–5 | Añadir 19 fuentes (20 en total) y después la 21 | 20 aceptadas; la 21 da `source_limit_reached` |
| 6 | Insertar la fuente 21 directamente en `documents` | `source_limit_reached` |
| 7–9 | B lista cuadernos, documentos y objetos | 0, 0 y 0 |
| 10 | B añade una fuente al cuaderno de A | Rechazado por RLS de `documents` |
| 11 | B inserta un cuaderno con `user_id` de A | Rechazado por RLS de `notebooks` |
| 12 | B intenta `update` | `permission denied` |
| 13 | `anon` lee `notebooks` | `permission denied` |

### T2–T9 · Código y tests (2026-11-04 / 05)

- TDD: cada test se ejecutó primero en rojo (con un esqueleto vacío) y después en verde:
  - `PdfValidatorTest`: 6 tests, rojo 6/6 y después verde.
  - `NotebookDtosTest`: 2 tests.
  - `FileSizeFormatTest`: 2 tests.
  - `NotebooksViewModelTest`: 12 tests, rojo 11/12. El que pasaba en rojo comprueba que «Atrás» no se intercepta sin subida, comportamiento que ya cumplía el esqueleto.
  - `NotebookDetailViewModelTest`: 8 tests, rojo 8/8.
- `:shared:testAndroidHostTest`: 73 tests, 0 fallos (los 43 que había y 30 nuevos).
- `:shared:compileCommonMainKotlinMetadata`, `:app:assembleDebug` y `:app:compileDebugAndroidTestKotlin`: en verde.
- `:app:connectedDebugAndroidTest`: 4/4 en `emulator-5554`. `DocumentTabBarTest` usa ahora `NotebookDetailContent` (la parte sin estado de la pantalla), porque `NotebookDetailScreen` necesita su ViewModel.
- `lint`: 0 errores y 30 avisos, los mismos que antes.
- Búsqueda de tokens del diseño (`Color(0x`, `fontSize`, `RoundedCornerShape(`, `.sp`, `FontWeight.`, `N.dp`) fuera de `ui/theme` y `ui/components`: sin resultados.
- Firma de `uploadAsFlow(path, ByteArray)` y de `UploadStatus.Progress` comprobada con `javap` en storage-kt 3.2.6.
- iOS: `PdfPicker.ios.kt` y `LocaleFormat.ios.kt` están escritos. `compileIosMainKotlinMetadata` aparece como SKIPPED en Windows: **sin compilar**, hace falta un Mac.
- Textos de ejemplo de la biblioteca que dejaron de usarse: eliminados de `values` y `values-es`.

### Emulador (2026-11-05, `emulator-5554`, Medium_Phone API 37, cuenta de la usuaria)

Capturas en el scratchpad de la sesión (`evidence/`), sin incluir en el repositorio.

| CA | Escenario | Resultado |
| --- | --- | --- |
| CA-07 | Cuenta sin cuadernos | OK: «No notebooks yet» |
| CA-01 | Start → «Informe anual 2024.pdf» (3 MB) | OK: «Uploading… 100%», detalle «Informe anual 2024», lista «1 source · 3 MB · Added Nov 5, 2026». En Supabase: `size_bytes` 3 145 931 tomado del objeto, ruta correcta, 1 objeto |
| CA-02 | Document → Add source → «Anexo tecnico.pdf» | OK: «2 sources · 4 MB» en el detalle y en la lista |
| CA-03 | 21 MB, texto renombrado a `.pdf`, archivo vacío | OK: los tres mensajes; la lista no cambia |
| CA-04 | Modo avión → subida → «No connection…» + Retry; sin modo avión → Retry | OK: sin filas ni objetos mientras falla; al reintentar se crea el cuaderno |
| CA-05 | Cinco pulsaciones en Start durante una subida lenta | OK: no se abre el selector ni se lanza otra subida |
| CA-06 | 3 cuadernos y después 4 | OK: del más reciente al más antiguo |
| CA-09 | Giro durante una subida (UMTS) | OK: la tarjeta de la subida, la lista y el progreso se conservan al girar |
| CA-11 | Cuaderno con 20 fuentes añadidas desde la app | OK: «This notebook has reached the maximum of 20 sources.», sin botón. El rechazo de la fuente 21 en el servidor quedó probado en T1 |
| CA-12 | Atrás durante una subida → «Cancel the upload?» → «Keep uploading» | OK: diálogo y subida mantenida |
| CA-12 | «Cancel upload» durante una subida | OK (2026-11-05, tras T11). Probado en la biblioteca y en el detalle (flecha de la barra superior); ver el registro de T11 |
| CA-10 | App en `es-ES` | OK: «20 fuentes», «Este cuaderno ha alcanzado el máximo de 20 fuentes.», «Añadido el 5 nov 2026», «Este archivo supera los 20 MB.» |
| CA-08 | Cuenta B (segunda cuenta confirmada de la usuaria) en la app | OK: B ve «No notebooks yet» y no aparece ninguno de los 4 cuadernos de A; después crea uno y solo ve ese |

Consistencia en Supabase tras 4 subidas fallidas (sin red o por tiempo de espera): 0 objetos huérfanos, 0 fuentes sin archivo y 0 cuadernos vacíos.

**Incidencia: tiempo de espera en redes lentas.**
- Con la red del emulador limitada a EDGE y a UMTS, las subidas grandes (19 MB) fallan con `SocketTimeoutException`, porque OkHttp corta una escritura que pasa unos 10 s parada.
- La app hace lo que pide la SPEC (mensaje y «Retry», nada a medias), pero en redes móviles lentas reales un PDF grande puede no llegar a subirse nunca.
- Ampliar ese tiempo (configuración del cliente HTTP) no está en el PLAN: queda pendiente de decisión.

### CA-08 · Peticiones directas con la sesión real de B (2026-11-05)

Token de B obtenido con `auth/v1/token`. Peticiones con `curl` a la API de Supabase, sin pasar por la app:

| # | Petición | Resultado |
| --- | --- | --- |
| 1 | `GET /rest/v1/notebooks` | 1 fila, solo de B |
| 2 | `GET /rest/v1/notebooks?user_id=eq.<A>` | `[]` |
| 3 | Listar la carpeta de A en el bucket `documents` | `[]` |
| 4 | `POST /rest/v1/notebooks` con `user_id` de A | 403 |
| 5 | Descargar un PDF de A (`object/authenticated`) | 400 (objeto no visible para B) |
| 6 | Subir un PDF a la carpeta de A | 400 (rechazado por RLS); no se creó ningún objeto |
| 7 | Sin sesión (`anon`), `GET /rest/v1/notebooks` | 401 |

Después: 0 objetos ajenos en la carpeta de A, 0 cuadernos «spoof» y 0 objetos huérfanos.

Nota: `:app:connectedDebugAndroidTest` desinstala la app al terminar, así que se pierde la sesión del emulador. Hay que reinstalarla e iniciar sesión otra vez antes de seguir con pruebas manuales.

### T11 · Tiempos de espera de las subidas (2026-11-05)

Ajuste añadido al PLAN («Ajuste tras la validación») después de que la usuaria lo aceptara.

| Paso | Resultado |
| --- | --- |
| Rojo (versión anterior). Cuenta B, emulador sin Wi-Fi y red a UMTS, «Informe anual 2024.pdf» (3 MB) | Se para en el 8 % y a los 15 s da «No connection». Log: `SocketTimeoutException` de OkHttp (`Http2Stream` write) |
| Primer intento: `httpConfig { install(HttpTimeout) }` | Descartado: `supabase-kt` 3.2.6 lo marca como API interna (error de compilación por opt-in) |
| Cambio: `httpEngine = createHttpEngine(60 s)` y `transferTimeout = 5 min` | Compila en Android. iOS escrito, sin compilar |
| Repetición con la misma red y el mismo archivo | **No concluyente.** El cliente ya no corta a los 10 s, pero la red simulada del emulador deja de enviar datos en el 8 % y unos 20 s después el servidor cierra la conexión (`stream was reset: PROTOCOL_ERROR`). Con HSDPA las subidas de 19 MB se completan. Para saber si mejora en una red móvil lenta real hace falta probarlo en un dispositivo con datos móviles |
| CA-12 en la biblioteca: subida parada → Atrás → «Cancel upload» | OK: la tarjeta desaparece y la lista sigue igual. En Supabase la cuenta B mantiene 1 cuaderno, 1 fuente, 1 objeto y 0 huérfanos |
| CA-12 en el detalle: subida → flecha Atrás de la barra → «Cancel the upload?» → «Cancel upload» | OK: vuelve a Mis Cuadernos. Se canceló con la subida al 100 % (registro en curso) y no quedó nada suelto: B tiene 2 fuentes y 2 objetos, 0 huérfanos y 0 fuentes sin archivo |
| Lista al volver con red lenta | Muestra los datos anteriores («1 source») hasta que termina la recarga; con la red restaurada pasa a «2 sources · 4 MB», como está previsto (la recarga mantiene la lista visible) |
| Regresiones | `:shared:testAndroidHostTest` 73/73, `:app:assembleDebug` y `:app:compileDebugAndroidTestKotlin` en verde, `lint` con 0 errores y 30 avisos, búsqueda de tokens del diseño sin resultados. `connectedDebugAndroidTest` no se repitió: no cambió la UI de `DocumentTabBarTest` y la tarea desinstala la app |

