# TASKS: Registro e inicio de sesión con Supabase

SPEC: `ai-docs/features/auth/SPEC.md` (Aprobada) · PLAN: `ai-docs/features/auth/PLAN.md` (Aprobado)

Estados: Pendiente · En curso · Hecha · Bloqueada

| ID | Objetivo | Alcance | Depende de | RF / CA | Validación | Estado |
| --- | --- | --- | --- | --- | --- | --- |
| T1 | Dependencias y configuración | `libs.versions.toml`, `app/build.gradle.kts` (BuildConfig desde `local.properties`), manifiesto (`INTERNET`, `android:name`), `DocuMindApplication`, `di/AppModule.kt` | — | Todos | `assembleDebug` verde | Hecha |
| T2 | Dominio y validador | `domain/{model,repository,validation}`, `CredentialsValidatorTest` | T1 | RF-03 · CA-04 | Tests verdes | Hecha |
| T3 | Repositorio Supabase | `data/repository/SupabaseAuthRepository.kt` | T2 | RF-02, RF-04, RF-05, RF-06 | Compila | Hecha |
| T4 | Componentes compartidos | `DmPrimaryButton(enabled, loading)`, `DmPillField(enabled, errorText)`, `ObserveAsEvents` | T1 | RF-03, RF-07 | Compila; usos existentes sin cambio de firma | Hecha |
| T5 | Login real | `LoginViewModel`, `AuthFormLayout`, `LoginScreen` sin «¿Olvidaste…?», `LoginViewModelTest` | T2, T4 | RF-03, RF-04, RF-07, RF-08 · CA-03, CA-05, CA-06, CA-09, CA-10, CA-11, CA-12 | Tests verdes | Hecha |
| T6 | Registro | `RegisterViewModel`, `RegisterScreen`, diálogo, `RegisterViewModelTest` | T5 | RF-01, RF-02, RF-03, RF-07 · CA-01, CA-02, CA-04, CA-09 | Tests verdes | Hecha |
| T7 | Sesión, navegación y cerrar sesión | `SessionViewModel`, `AppNavigation`, menú del avatar, `SessionViewModelTest` | T3, T5, T6 | RF-05, RF-06 · CA-07, CA-08 | Tests verdes | Hecha |
| T8 | Textos | `strings.xml` en inglés y español | T5, T6, T7 | Todos | Lint sin textos faltantes | Hecha |
| T9 | Validación final | build, unit tests, lint, `DocumentTabBarTest`, grep design system, pruebas manuales | T1–T8 | CA-01…CA-13 | Resultados registrados abajo | En curso (parcial: CA-05, CA-07, CA-08 pendientes) |
| T10 | Repositorio y dominio de recuperación | `AuthRepository` (+3 métodos), `AuthError` (+3), `SupabaseAuthRepository`, PKCE, `signUp` con `redirectUrl = null`, `FakeAuthRepository` | T3 | RF-10…RF-13 | Compila; tests existentes verdes | Hecha |
| T11 | Pantalla «¿Olvidaste tu contraseña?» | `ForgotPasswordViewModel` + `Screen`, enlace en login, test | T10 | RF-09, RF-10 · CA-14, CA-15 | Tests verdes | Hecha |
| T12 | Pantalla «Nueva contraseña» | `ResetPasswordViewModel` + `Screen`, test | T10 | RF-11…RF-13 · CA-17…CA-19 | Tests verdes | Hecha |
| T13 | Enlace a la app y navegación | manifiesto (`singleTask`, intent-filter), `MainActivity`, `AppNavigation` deep link + `onNewIntent` | T11, T12 | RF-11 · CA-16, CA-18 | `assembleDebug`; `am start -d documind://…` abre la pantalla | Hecha |
| T14 | Textos y validación de la ampliación | `strings.xml` en/es; build, tests, lint, pruebas en emulador | T11–T13 | CA-14…CA-19 | Resultados registrados abajo | Hecha (parcial: CA-16, CA-17 pendientes) |

## Registro de validación

Fecha: 2026-11-03. Entorno: Windows, emulador `Medium_Phone` (Android 17, API 37),
proyecto Supabase «DocuMind».

**Automático**
- `./gradlew assembleDebug testDebugUnitTest lintDebug`: BUILD SUCCESSFUL.
- Tests unitarios: 22/22 verdes (`CredentialsValidatorTest` 5, `LoginViewModelTest` 8,
  `RegisterViewModelTest` 4, `SessionViewModelTest` 4, `ExampleUnitTest` 1).
- `connectedDebugAndroidTest` en el emulador: 4/4 verdes (incluye `DocumentTabBarTest`, regresión).
- Lint: 0 errores. Avisos: versiones más nuevas disponibles (fijadas a propósito, ver PLAN),
  recursos sin usar previos y `login_forgot_password` (se reutilizará en la recuperación de contraseña).
- `grep` del design system fuera de `ui/theme` y `ui/components`: sin resultados.

**Manual en emulador**
| Criterio | Resultado |
| --- | --- |
| CA-01 | Superado: «Sign up here» abre el registro con 3 campos; «Atrás» del sistema vuelve al login. |
| CA-02 | Superado: registro de `martajimpac+documindtest@gmail.com` → diálogo «Check your email» → login. En Supabase el usuario existe con `email_confirmed_at = null` y `confirmation_sent_at` rellenado. |
| CA-03 | Superado: login con esa cuenta sin confirmar → «Confirm your email first…». |
| CA-04 | Superado: email `ana@mail`, contraseña de 7 y contraseñas distintas → tres errores junto a sus campos. |
| CA-05 | **Pendiente**: necesita pulsar el enlace de confirmación del correo. Confirmar el usuario por SQL fue denegado por permisos. |
| CA-06 | Superado contra Supabase: «Incorrect email or password», email conservado. |
| CA-07 | **Pendiente** (necesita una cuenta confirmada). Cubierto por `SessionViewModelTest`. |
| CA-08 | **Pendiente** (necesita una cuenta confirmada). Cubierto por `SessionViewModelTest`. |
| CA-09 | Cubierto por tests unitarios (una sola petición); no reproducible a mano por la rapidez de la respuesta. |
| CA-10 | Superado: el login no muestra «¿Olvidaste tu contraseña?». |
| CA-11 | Superado: modo avión → «No connection…»; al quitarlo, el reintento llega a Supabase. |
| CA-12 | Superado: en horizontal se conserva el formulario; tras matar el proceso se conserva el email y la contraseña se vacía. |
| CA-13 | Superado: texto al 200 % con teclado abierto; desplazando se ven todos los campos y el botón (captura). |

**Corregido durante la validación:** «Siguiente» en un campo de contraseña llevaba el foco al
botón del ojo; ahora baja al siguiente campo.

**Pendientes fuera del código**
- Confirmar la cuenta de prueba desde el correo y repetir CA-05, CA-07 y CA-08.
- Borrar el usuario de prueba `martajimpac+documindtest@gmail.com` de Supabase cuando ya no haga falta.
- Dashboard de Supabase: *Minimum password length* = 8 y revisar *Site URL* (ver Riesgos del PLAN).

### Ampliación: recuperación de contraseña (2026-11-03)

Implementada por un subagente (Sonnet) según el PLAN y revisada por el agente
principal, que corrigió:
- Sin red al verificar el enlace se mostraba «enlace caducado»: nuevo estado
  `Offline` con «Reintentar».
- «Atrás» durante la verificación podía dejar la sesión abierta si la
  verificación terminaba después: `cancel()` cancela antes la verificación.
- El enlace a la app no abría la pantalla con `navDeepLink`: se trata a mano
  (ver PLAN).

**Automático:** `assembleDebug testDebugUnitTest lintDebug` en verde. Tests unitarios
39/39 (`ResetPasswordViewModelTest` 12, `ForgotPasswordViewModelTest` 5 y los 22
anteriores). Lint 0 errores. `grep` del design system sin resultados.

**Manual en emulador**
| Criterio | Resultado |
| --- | --- |
| CA-14 | Superado: el email escrito en el login aparece en la recuperación; «Atrás» vuelve al login. |
| CA-15 | Superado contra Supabase: aviso genérico y vuelta al login; `recovery_sent_at` del usuario de prueba rellenado. |
| CA-16 | **Pendiente**: falta añadir `documind://reset-password` a *Redirect URLs* en Supabase y pulsar el enlace del correo. Con `adb` el enlace abre la app en la pantalla correcta, en frío y con la app abierta. |
| CA-17 | **Pendiente** (necesita un enlace real). Cubierto por `ResetPasswordViewModelTest`. |
| CA-18 | Superado: enlace con `error_code=otp_expired` (en frío) y con código falso (app abierta) → «This link is not valid or has expired.»; «Request a new link» abre la recuperación; «Atrás» vuelve al login. |
| CA-19 | Cubierto por tests (cancelación cierra la sesión, también durante la verificación); en dispositivo necesita un enlace real. |


### Revisión tras la conversión a KMP (2026-11-04)

- El código de auth pasó a `shared/src/commonMain` sin cambios de comportamiento (ver `ai-docs/features/kmp-conversion`).
- Cambió la ruta de los archivos citados arriba: `app/src/main/java/...` ahora es `shared/src/commonMain/kotlin/...`.
- El enlace de recuperación ahora llega como `MainActivity.onNewIntent` → `App(incomingLink)` → `ResetLink.parse` (Ktor `Url`).

Se repitió en el emulador con el código compartido:

| CA | Resultado |
| --- | --- |
| CA-03, CA-04 | OK: errores de campos vacíos, contraseña corta (plural) y contraseñas distintas |
| CA-06 | OK: «Incorrect email or password» desde Supabase |
| CA-12 | OK: el registro conserva sus datos al girar y el correo tras matar el proceso |
| CA-18 | OK: `otp_expired` con la app abierta y código falso con la app cerrada; no se reabre al girar |
| CA-19 (parcial) | OK: Atrás desde el aviso de enlace no válido vuelve al inicio de sesión |

Siguen pendientes, por los mismos motivos:

| CA | Qué falta | Quién |
| --- | --- | --- |
| CA-05, CA-07, CA-08 | La cuenta de prueba sigue sin confirmar (consultado en `auth.users` el 2026-11-04). No se volvió a intentar confirmarla por SQL porque los permisos ya lo denegaron | Usuaria: pulsar el correo de confirmación |
| CA-16, CA-17, CA-19 (con sesión) | Enlace real de recuperación | Usuaria: añadir `documind://reset-password` a *Redirect URLs* y pedir un enlace |
