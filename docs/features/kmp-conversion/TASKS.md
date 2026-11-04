# TASKS: Conversión a Kotlin Multiplatform

SPEC: `docs/features/kmp-conversion/SPEC.md` (Aprobada) · PLAN: `docs/features/kmp-conversion/PLAN.md` (Aprobado)

Estados: Pendiente · En curso · Hecha · Bloqueada

| ID | Objetivo | Alcance | Depende de | RF / CA | Validación | Estado |
| --- | --- | --- | --- | --- | --- | --- |
| T1 | Configurar Gradle | `settings.gradle.kts` (`:shared`), `build.gradle.kts`, `libs.versions.toml`, `shared/build.gradle.kts`, `gradle.properties` | — | RF-02, RF-04 | Gradle configura `:shared` y lista las tareas de Android e iOS | Hecha |
| T2 | Mover código y recursos | `git mv` de `data`, `di`, `domain`, `navigation`, `ui` y de los recursos a `shared/src/commonMain`; R → Res; `Type.kt`/`Theme.kt`; `formatPercent`; `Clock` | T1 | RF-02 | `:shared:compileCommonMainKotlinMetadata` y `:shared:compileAndroidMain` verdes | Hecha |
| T3 | Navegación, DI y entrada Android | `Routes.kt`, `ResetLink.kt`, `AppNavigation.kt`, `App.kt`, `AppModule.kt` (`initKoin`), `MainActivity`, `DocuMindApplication`, `MainViewController` (iOS) | T2 | RF-01, RF-03, RF-04 | `:app:assembleDebug` verde | Hecha |
| T4 | Tests compartidos | Tests a `commonTest` con `kotlin.test`, `MainDispatcherTest`, `ResetLinkTest`; `DocumentTabBarTest` con `getString` | T2, T3 | RF-05 · CA-06 | `:shared:testAndroidHostTest` | Hecha |
| T5 | Textos equivalentes | `%d` → `%1$d`; quitar `\'`; comillas → ` ` | T2 | RF-01 · CA-01 | Decodificar los `.cvr` generados | Hecha |
| T6 | Validación | Build, tests, lint, tests instrumentados, búsqueda de tokens del diseño, emulador | T1–T5 | CA-01…CA-06 | Resultados abajo | Hecha (parcial: CA-01 sin Mis Cuadernos en el emulador, CA-05 sin sesión real) |

## Registro de validación (2026-11-04)

Entorno: Windows 11, emulador `Medium_Phone` (API 37, `emulator-5554`). El móvil de la usuaria estaba conectado por ADB: todos los comandos usaron `-s emulator-5554` o `ANDROID_SERIAL=emulator-5554`, así que no se instaló nada en el móvil.

### Comprobaciones automáticas

| Comprobación | Resultado |
| --- | --- |
| `:shared:compileCommonMainKotlinMetadata` | Verde: el código común no usa APIs solo de Android |
| `:shared:compileAndroidMain`, `:app:assembleDebug`, `:app:compileDebugAndroidTestKotlin` | Verde |
| `:shared:testAndroidHostTest` (CA-06) | 43 tests, 0 fallos: los 38 que había y 5 nuevos de `ResetLinkTest` |
| `lint` | 0 errores y 30 avisos: versiones nuevas disponibles, `targetSdk` y 7 colores de la plantilla sin usar en `app/res/values/colors.xml`, que ya estaban. El código principal de `:shared` no lo analiza la tarea `lint` de Gradle (solo `lintAnalyzeAndroidHostTest`) |
| `:app:connectedDebugAndroidTest` | 4/4 en el emulador (`DocumentTabBarTest` con textos de `Res`) |
| Búsqueda de tokens del diseño fuera de `ui/theme` y `ui/components` | Sin resultados |
| CA-04: archivos Kotlin en `:app` | Solo `MainActivity`, `DocuMindApplication` y los tests de ejemplo e instrumentados |
| Destinos iOS | Declarados (`iosArm64`, `iosSimulatorArm64`, framework `Shared`). **No compilados: hace falta macOS** |

### Textos (CA-01)

- Se decodificaron los `.cvr` generados y se buscaron comillas, barras invertidas y espacios en los extremos.
- Había tres diferencias con Android y se corrigieron:
  - `Don\'t` y `don\'t` mostraban la barra;
  - `" · "` mostraba las comillas;
  - `" / %1$d"` mostraba las comillas.
- En el emulador se comprobó que ahora se lee «Don't have an account? Sign up here».

### Emulador

| CA | Escenario | Resultado |
| --- | --- | --- |
| CA-01 | Login vacío → errores en los campos; captura con Inter, colores e iconos correctos | OK |
| CA-01 | Login con un correo inexistente → «Incorrect email or password» desde Supabase (red con Ktor OkHttp en `:shared`) | OK |
| CA-01 | Registro: contraseña corta → «Use at least 8 characters» (plural) y «Passwords don't match» | OK |
| CA-01 | Idioma de la app `es-ES`: registro («Crea tu cuenta», «Repite la contraseña») y «¿Olvidaste tu contraseña?» («Enviar enlace») | OK |
| CA-01 | Mis Cuadernos y detalle en el emulador | **No probado**: hace falta una cuenta confirmada. El detalle está cubierto por `DocumentTabBarTest` |
| CA-02 | `error_code=otp_expired` con la app abierta (`onNewIntent`) → «This link is not valid or has expired.» + «Request a new link»; al girar no se reabre | OK |
| CA-02 | `code=fake-code-123` con la app cerrada → mismo aviso; al girar se mantiene; Atrás → inicio de sesión | OK |
| CA-03 | Registro con correo y contraseña; girar → todo se mantiene; `am kill` en segundo plano y reabrir → vuelve al registro con el correo. La contraseña no se guarda, igual que antes | OK |
| CA-05 | Instalación limpia y `pm clear` → inicio de sesión | OK |
| CA-05 | Actualizar encima de una sesión iniciada | **Bloqueado**: la cuenta de prueba no está confirmada, así que no se puede iniciar sesión. supabase-kt (misma versión y mismo almacenamiento) es el que guarda la sesión, pero no está comprobado en el dispositivo |

**Incidencia no reproducible:** en la primera instalación tras arrancar el emulador desde su snapshot, un volcado de UI mostró Mis Cuadernos. Ese volcado dio el error `null root node`. Con instalación limpia, `pm clear` y arranque en frío se vio siempre el inicio de sesión. El log de Supabase dice «No session found in storage».

**Emulador inestable:** se cerró dos veces porque el PC tenía 1,6 GB libres de 14 GB. Se volvió a arrancar con `-memory 2048`.
