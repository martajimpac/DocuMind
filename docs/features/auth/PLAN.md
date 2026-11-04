# PLAN: Registro e inicio de sesión con Supabase

**SPEC de referencia:** `docs/features/auth/SPEC.md`
**Versión de la spec revisada:** Aprobada el 2026-11-03 (staged, sin commit)
**Estado:** Aprobado <!-- Borrador | En revisión | Aprobado -->

> Aprobación: la persona aprobó la spec e indicó «no me preguntes más hasta que
> esté implementado» (2026-11-03). Se toma como aprobación del plan y
> autorización explícita para implementar sin más paradas. La recuperación de
> contraseña sigue fuera de este plan; se hará después en su propia iteración.

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
| Navegación | `app/src/main/java/com/example/chatbot/navigation/AppNavigation.kt` | NavHost con rutas string (`login`, `notebooks`, `notebook_detail/{title}`). Se añade `register`, el destino inicial según sesión y la salida al cerrar sesión. |
| Login | `…/ui/screens/login/LoginScreen.kt` | Formulario sin estado real. Pasa a ViewModel; se oculta «¿Olvidaste…?». |
| Mis Cuadernos | `…/ui/screens/notebooks/NotebooksScreen.kt` | `DmAvatar` sin acción (`LibraryHeader`). Abrirá el menú «Cerrar sesión». |
| Componentes | `…/ui/components/Controls.kt` | `DmPrimaryButton`, `DmTextButton`, `DmPillField`, `DmAvatar`, `BrandLockup`. Se amplían con parámetros opcionales. |
| Activity | `…/MainActivity.kt` | Ya llama a `enableEdgeToEdge`; manifiesto con `adjustResize`. Sin cambios. |
| Strings | `app/src/main/res/values{,-es}/strings.xml` | Textos `login_*`. Se añaden textos nuevos en ambos idiomas. |
| Build | `app/build.gradle.kts`, `gradle/libs.versions.toml` | AGP 9.2.1 (Kotlin integrado), Kotlin 2.2.10, lifecycle 2.11.0. Sin DI, sin red. |
| Supabase | Proyecto `DocuMind` (`zaftthtyrjjqrnwddebm`, eu-north-1) | `/auth/v1/settings`: email activo, `disable_signup=false`, `mailer_autoconfirm=false` (confirmación obligatoria). |

**Convenciones y patrón de referencia:** `AGENTS.md` (Clean Architecture + MVVM,
Koin, `StateFlow` + `collectAsStateWithLifecycle`, eventos one-shot con
`Channel(BUFFERED)` + `receiveAsFlow()` y `ObserveAsEvents`). Decisión activa en
Brain: colores/tipos/tamaños solo en `ui/theme`, pantallas compuestas con `Dm*`.
`gradle/SPEC.md` (estructura del proyecto) fija `core/data/domain/presentation/di`;
las clases nuevas siguen sus subcarpetas (`data/repository`, `domain/...`,
`ui/utils`). Diferencias: el código existente usa `ui/` en lugar de
`presentation/` (no se renombra, fuera de alcance) y el documento cita Hilt,
mientras `AGENTS.md` exige Koin; se usa Koin. Se mantienen las rutas string existentes; migrar la navegación a rutas
`@Serializable` sería un refactor fuera de alcance.

## Solución propuesta

<!-- Explica el enfoque y sus motivos. Describe las responsabilidades y el
recorrido de datos y eventos hasta la interfaz. Usa un diagrama si aporta claridad. -->

```
Screen ──acciones──▶ ViewModel ──▶ AuthRepository (domain, interfaz)
  ▲  StateFlow<UiState>     │              ▲
  └── ObserveAsEvents ◀─ Channel           │ implementa
                                 SupabaseAuthRepository (data) ──▶ supabase-kt Auth
```

- **Dominio** (`domain/model`, `domain/repository`, `domain/validation`): interfaz `AuthRepository` con
  `sessionState: Flow<SessionState>` y `signIn`, `signUp`, `signOut`, que
  devuelven `AuthResult` (éxito o `AuthError`). `CredentialsValidator` en Kotlin
  puro (RF-03), testeable en JVM.
- **Datos** (`data/repository`): `SupabaseAuthRepository` traduce `SessionStatus`
  (`Initializing`→`Loading`; `Authenticated`/`RefreshFailure`→`SignedIn`;
  `NotAuthenticated`→`SignedOut`) y excepciones (`AuthRestException.errorCode`
  `InvalidCredentials`/`EmailNotConfirmed`/`WeakPassword`,
  `HttpRequestException`→`Network`). Relanza `CancellationException`.
  supabase-kt guarda la sesión cifrada en el dispositivo y la renueva (RF-05).
- **Presentación**: `LoginViewModel` y `RegisterViewModel` con `UiState`
  inmutable y `Channel` para eventos. `SessionViewModel` decide el destino
  inicial una sola vez y emite `SignedOut` cuando la sesión pasa de iniciada a
  cerrada (logout o sesión revocada, RF-06).
- **UI**: `AuthFormLayout` compartido entre login y registro (scaffold,
  marca, tarjeta, errores) para no duplicar el formulario.

## Módulos y componentes afectados

<!-- Si el proyecto está modularizado, identifica los módulos afectados, sus
responsabilidades y la dirección de sus dependencias. Respeta los límites
existentes y justifica cualquier módulo o dependencia nueva. Si no está
modularizado, describe las carpetas o componentes afectados sin introducir
modularización fuera del alcance; marca la tabla de módulos como No aplica. -->

| Módulo | Existe / nuevo | Responsabilidad y cambios | Dependencias afectadas |
| --- | --- | --- | --- |
| No aplica | — | Proyecto de un solo módulo `:app`; se separan capas por paquetes. | — |

<!-- Distingue lo que se reutiliza, modifica o crea. Las rutas nuevas son propuestas.
Señala impacto sobre modelos, contratos o componentes compartidos. -->

Rutas relativas a `app/src/main/java/com/example/chatbot/`.

| Componente o ruta | Acción | Cambio y responsabilidad | Requisito relacionado |
| --- | --- | --- | --- |
| `DocuMindApplication.kt` + manifiesto | Crear / modificar | `startKoin`; permiso `INTERNET`; `android:name`. | RF-02, RF-04 |
| `di/AppModule.kt` | Crear | Cliente Supabase (`install(Auth)`), repositorio, validador, ViewModels. | Todos |
| `domain/model/Auth.kt`, `domain/repository/AuthRepository.kt`, `domain/validation/CredentialsValidator.kt` | Crear | `AuthRepository`, `SessionState`, `AuthResult`, `AuthError`, `CredentialsValidator`. | RF-02…RF-06 |
| `data/repository/SupabaseAuthRepository.kt` | Crear | Implementación con supabase-kt y mapeo de errores. | RF-02, RF-04, RF-05, RF-06 |
| `ui/utils/ObserveAsEvents.kt` | Crear | `repeatOnLifecycle(STARTED)` + `Dispatchers.Main.immediate`. | RF-02, RF-04, RF-06 |
| `ui/components/Controls.kt` | Modificar | `DmPrimaryButton(enabled, loading)`; `DmPillField(enabled, errorText)`; `DmAvatar` sin cambios. Parámetros opcionales con el comportamiento actual por defecto. | RF-03, RF-07 |
| `ui/theme/Color.kt`/`Theme.kt` | Modificar si hace falta | Token de color de error en el esquema si no está definido. | RF-03 |
| `ui/screens/login/AuthFormLayout.kt` | Crear | Layout compartido (Scaffold `safeDrawing`, scroll, marca, tarjeta, error general con `liveRegion`). | RF-01, RF-03, RF-04 |
| `ui/screens/login/LoginScreen.kt` + `LoginViewModel.kt` | Modificar / crear | Formulario real; sin «¿Olvidaste…?». | RF-03, RF-04, RF-07, RF-08 |
| `ui/screens/register/RegisterScreen.kt` + `RegisterViewModel.kt` | Crear | Tres campos, diálogo «Revisa tu correo», volver al login. | RF-01, RF-02, RF-03, RF-07 |
| `ui/screens/notebooks/NotebooksScreen.kt` | Modificar | `onSignOut`; menú en el avatar con «Cerrar sesión». | RF-06 |
| `navigation/AppNavigation.kt` + `SessionViewModel.kt` | Modificar / crear | Ruta `register`; inicio según sesión; vuelta al login al cerrar sesión. | RF-01, RF-05, RF-06 |
| `res/values{,-es}/strings.xml` | Modificar | Textos nuevos en inglés y español. | Todos |

## Datos y contratos

<!-- Completa solo lo aplicable. Si un punto no aplica, indica el motivo. -->

- **Modelos y contratos de entrada y salida:** `signIn(email, password)` y
  `signUp(email, password)` → `AuthResult`; `AuthError` = `InvalidCredentials`,
  `EmailNotConfirmed`, `WeakPassword`, `Network`, `Unknown`. El email se pasa sin
  espacios al principio ni al final.
- **Identificadores, relaciones y restricciones:** usuario de Supabase Auth.
  Mínimo de contraseña 8 en el cliente; en el servidor, ver Riesgos.
- **Origen de los datos mostrados y transformaciones:** mensajes de error de
  `strings.xml` según `AuthError`; nunca el texto del servidor.
- **Persistencia, consultas y actualizaciones:** solo la sesión, gestionada por
  supabase-kt en el almacenamiento de la app. La contraseña no se persiste.
- **Convivencia entre datos locales y remotos:** sesión guardada + renovación
  automática. Sin red se considera sesión iniciada (`RefreshFailure`).
- **Compatibilidad y migraciones de datos existentes:** no aplica; no hay datos
  previos.

## Estado, operaciones y errores

<!-- Cómo se implementan los comportamientos aprobados en la spec.
Referencia RF/CA y aplica las consideraciones relevantes de MOBILE_GUIDELINES.md. -->

- **Gestión del estado de interfaz y navegación:** `UiState` con campos, errores
  por campo, error general e `isLoading`. Eventos one-shot por `Channel`:
  `LoginEvent.SignedIn` (navega a Mis Cuadernos quitando el login),
  `RegisterEvent.ConfirmationSent` (abre el diálogo; al aceptarlo, vuelve al
  login), `SessionEvent.SignedOut` (navega al login vaciando la pila). Mientras
  la sesión está en `Loading` se pinta solo el fondo, sin mostrar el login.
- **Conservación y restauración del estado:** el ViewModel conserva todo en
  rotación (CA-12). El email se guarda en `SavedStateHandle` para sobrevivir a la
  muerte del proceso; la contraseña no. El diálogo usa `rememberSaveable`.
- **Ejecución, concurrencia y cancelación de operaciones:** llamadas en
  `viewModelScope`; si se destruye el ViewModel la operación se cancela.
- **Errores, reintentos y prevención de duplicados:** se ignora el envío si
  `isLoading`; el botón queda desactivado (CA-09). Tras un error se puede
  reintentar con los datos escritos (CA-11). Errores de campo se limpian al
  editar el campo.
- **Otras consideraciones mobile aplicables y su solución:** teclado con
  `Scaffold(contentWindowInsets = safeDrawing)` + scroll (patrón ya usado en el
  login, CA-13); `ImeAction.Next`/`Done` (Done envía); `KeyboardType.Email`;
  errores con `semantics { error() }` y error general con `liveRegion`; cerrar
  sesión borra la sesión local aunque falle la red (`clearSession`).

## Dependencias y configuración

<!-- Librerías, servicios, permisos o configuración afectados. Verifica compatibilidad
con el proyecto y justifica las incorporaciones. No agregues dependencias por defecto. -->

- `io.github.jan-tennert.supabase:bom:3.2.6` + `auth-kt`: cliente oficial de
  Supabase en Kotlin. Es la última versión compilada con Kotlin 2.2 (stdlib
  2.2.21); 3.7+ exige Kotlin 2.4 y obligaría a actualizar el compilador.
- `io.ktor:ktor-client-okhttp:3.3.1`: motor HTTP, misma versión de Ktor que usa
  supabase-kt 3.2.6. `AGENTS.md` fija Ktor para red.
- `io.insert-koin:koin-bom:4.1.1` + `koin-android` + `koin-androidx-compose`:
  DI exigida por `AGENTS.md`; 4.1.1 está compilada con Kotlin 2.1 (4.2 usa 2.3).
- `androidx.lifecycle:lifecycle-viewmodel-compose` y `lifecycle-runtime-compose`
  2.11.0 (misma versión que `lifecycle-runtime-ktx` del proyecto).
- Test: `org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2` (versión de
  coroutines que arrastra supabase-kt).
- `buildFeatures.buildConfig = true`; `SUPABASE_URL` y
  `SUPABASE_PUBLISHABLE_KEY` se leen de `local.properties` (ignorado por git). La
  clave es la publishable (`sb_publishable_…`); nunca la service role. Si faltan,
  Gradle falla con un mensaje claro.
- Manifiesto: permiso `android.permission.INTERNET`.

## Estrategia de validación

<!-- Una fila por criterio de la spec. Selecciona el método capaz de demostrarlo:
test unitario, integración, UI o prueba manual. No todos requieren todos los métodos.
Identifica tests existentes y separa los nuevos propuestos. Incluye regresiones relevantes.
Una captura aislada no demuestra persistencia ni ausencia de peticiones de red. -->

| Criterio | Método y test existente o propuesto | Entorno y datos necesarios | Evidencia prevista |
| --- | --- | --- | --- |
| CA-01 | Manual: login → registro → atrás | Emulador | Pasos y resultado |
| CA-02 | Unit `RegisterViewModelTest` (evento tras éxito) + manual con email real | JVM; emulador + correo de la persona | Test verde; usuario sin confirmar en Supabase |
| CA-03 | Unit `LoginViewModelTest` (EmailNotConfirmed) + manual | JVM; cuenta sin confirmar | Test verde; mensaje |
| CA-04 | Unit `CredentialsValidatorTest` + `RegisterViewModelTest` (sin llamada al repo) | JVM | Test verde (contador de llamadas = 0) |
| CA-05 | Unit (evento SignedIn) + manual | JVM; cuenta confirmada | Test verde; navegación |
| CA-06 | Unit (InvalidCredentials, email conservado) + manual | JVM | Test verde |
| CA-07 | Unit `SessionViewModelTest` (SignedIn → destino Mis Cuadernos) + manual forzar cierre | JVM; emulador | Test verde; pasos |
| CA-08 | Unit `SessionViewModelTest` (evento SignedOut) + manual | JVM; emulador | Test verde; pasos |
| CA-09 | Unit (doble envío → 1 llamada) | JVM | Test verde |
| CA-10 | Revisión de código + manual | — | Ausencia del botón |
| CA-11 | Unit (Network → mensaje, datos conservados, reintento OK) + manual modo avión | JVM; emulador | Test verde |
| CA-12 | Unit (`SavedStateHandle` conserva email) + manual rotación | JVM; emulador | Test verde |
| CA-13 | Manual texto 200 % + teclado | Emulador | Captura |

**Comprobaciones de regresión:** `DocumentTabBarTest` existente; navegación Mis
Cuadernos → detalle → atrás; buscador y compositor del chat (usan `DmPillField`);
botones primarios existentes (usan `DmPrimaryButton`); `grep` de la decisión del
design system fuera de `ui/theme` y `ui/components`.

**Comandos verificados para compilar y ejecutar tests:** `./gradlew assembleDebug
testDebugUnitTest` (línea base verde el 2026-11-03); además `lintDebug`.

**Pruebas en dispositivo, emulador o simulador:** las manuales de la tabla, si
hay un emulador disponible (`adb devices`).

**Limitaciones del entorno:** recibir y pulsar el correo de confirmación (CA-02,
CA-03, CA-05 completos) requiere el buzón de la persona; queda pendiente si no
se puede hacer aquí.

<!-- Esta sección planifica la validación. Durante la implementación, registra
en TASKS.md o en el informe de validación acordado los resultados y evidencias
reales. Distingue pruebas ejecutadas, fallidas, no ejecutadas y bloqueadas.
Compilar o tener tests en verde no sustituye revisar los criterios de la spec. -->

## Orden de implementación

<!-- Etapas y dependencias principales. El desglose ejecutable se escribe en TASKS.md.
Incluye puntos de comprobación para avanzar con cambios pequeños. -->

1. Dependencias, configuración, Application y Koin → compila.
2. Dominio + validador + tests → tests verdes.
3. Repositorio Supabase → compila.
4. Componentes compartidos ampliados → compila; pantallas existentes sin cambios visuales.
5. Login + Register + ViewModels + tests → tests verdes.
6. Sesión, navegación y cerrar sesión + tests → tests verdes.
7. Validación completa (build, tests, lint, manual) y registro en TASKS.md.

## Riesgos y decisiones pendientes

<!-- Riesgos concretos de esta solución y cómo se resolverán, sin listas genéricas.
Escribe Ninguna en las decisiones pendientes cuando estén resueltas. -->

- **Riesgos y medidas acordadas:**
  - *Mínimo de 8 caracteres en el servidor:* no se puede leer ni cambiar con las
    herramientas disponibles (MCP sin acceso a la configuración de Auth). El
    cliente lo exige siempre; la persona debe fijarlo en Dashboard → Authentication
    → Providers → Email → *Minimum password length* = 8. Queda como pendiente.
  - *Página tras confirmar el email:* Supabase redirige a la *Site URL* del
    proyecto (por defecto `http://localhost:3000`), que en un móvil muestra una
    página de error aunque la cuenta quede confirmada. La persona debe revisar
    Authentication → URL Configuration → Site URL. Queda como pendiente.
  - *Correos de Supabase sin SMTP propio:* el servicio por defecto limita el envío
    (solo a miembros del equipo y pocos por hora). Afecta a las pruebas manuales.
  - *Rutas string en vez de `@Serializable`* (diferencia con `AGENTS.md`): se
    mantiene el patrón existente para no refactorizar fuera de alcance.
- **Decisiones pendientes:** Ninguna.

## Ampliación: recuperación de contraseña (RF-09…RF-13)

> Aprobación: la persona pidió implementarla «sin preguntas, tomando las
> decisiones necesarias» y usando un modelo más barato para las tareas
> (2026-11-03). Se toma como aprobación de esta ampliación del plan y
> autorización para implementarla. La implementación se delega en un subagente
> (Sonnet) con estas instrucciones; el agente principal revisa y valida.

**Solución.** Flujo PKCE de Supabase con enlace propio a la app:

```
Forgot screen ─resetPasswordForEmail(email, "documind://reset-password")─▶ Supabase ─correo─▶
enlace ─▶ Supabase verifica ─redirige─▶ documind://reset-password?code=… (o ?error_code=…)
─▶ MainActivity (singleTask) ─▶ NavHost deep link ─▶ ResetPasswordViewModel
   exchangeCodeForSession(code) ─ok─▶ formulario ─updateUser{password}─▶ Mis Cuadernos
                                 └─error─▶ «enlace no válido» + «Pedir otro enlace»
```

- `install(Auth) { flowType = FlowType.PKCE }`. No se configuran `scheme`/`host`
  (no se usa `handleDeeplinks`) y `signUp` pasa `redirectUrl = null`, para que el
  correo de confirmación siga yendo a la web (D2).
- `AuthRepository` añade `sendPasswordReset(email)`, `exchangeRecoveryCode(code)`
  y `updatePassword(password)`, todos con `AuthResult`. `AuthError` añade
  `InvalidLink` (`OtpExpired`, `FlowStateNotFound`, `FlowStateExpired`,
  `BadCodeVerifier`, sin verificador guardado), `SamePassword` y `RateLimited`
  (`OverEmailSendRateLimit`).
- Navegación: rutas `forgot_password?email={email}` y
  `reset_password?code={code}&error_code={error_code}` (argumentos opcionales).
  *Cambio durante la validación:* `navDeepLink` no abría la pantalla en el
  emulador (ni en frío ni con la app abierta), así que `AppNavigation` lee el
  enlace a mano (`HandleResetLinks`): mismo código para el intent de arranque y
  para `addOnNewIntentListener`, y recuerda con `rememberSaveable` el último
  enlace tratado para no reabrirlo al girar o recrear. `MainActivity` pasa a `launchMode="singleTask"` con un
  `intent-filter` VIEW/BROWSABLE para `documind://reset-password`; con la app
  abierta, `addOnNewIntentListener` llama a `navController.handleDeepLink`.
- `ForgotPasswordViewModel`: email (prellenado y en `SavedStateHandle`),
  validación, `isLoading`, evento `LinkSent`. La pantalla muestra el aviso en un
  diálogo y vuelve al login (mismo patrón que el registro).
- `ResetPasswordViewModel`: estados `Verifying` → `Ready` (formulario) o
  `InvalidLink`; al guardar, evento `PasswordUpdated` → Mis Cuadernos vaciando la
  pila. «Atrás» llama a `cancel()` → `signOut()` y vuelve al login (RF-13). El
  código se canjea una sola vez aunque se recree la pantalla (estado en el
  ViewModel).
- `SessionViewModel`: el inicio de sesión temporal del enlace no debe disparar
  navegación; solo `SignedIn → SignedOut` emite evento (ya es así). Al cancelar,
  ese evento lleva al login, que es el destino esperado.
- UI: reutiliza `AuthFormLayout`, `AuthEmailField`, `AuthPasswordField`,
  `AuthErrorMessage`, `DmPrimaryButton`, `DmTextButton`. Vuelve a mostrarse
  «¿Olvidaste tu contraseña?» (`login_forgot_password`).

| Componente o ruta | Acción | Requisito |
| --- | --- | --- |
| `domain/model/Auth.kt`, `domain/repository/AuthRepository.kt` | Modificar | RF-10…RF-13 |
| `data/repository/SupabaseAuthRepository.kt`, `di/AppModule.kt` | Modificar | RF-10…RF-13 |
| `ui/screens/forgotpassword/*` | Crear | RF-09, RF-10 |
| `ui/screens/resetpassword/*` | Crear | RF-11…RF-13 |
| `ui/screens/login/LoginScreen.kt`, `AuthFormLayout.kt` | Modificar | RF-09 |
| `navigation/AppNavigation.kt`, `MainActivity.kt`, manifiesto | Modificar | RF-11 |
| `strings.xml` (en, es) | Modificar | Todos |
| Tests unitarios de ambos ViewModels | Crear | CA-15, CA-17…CA-19 |

**Validación.** Unit tests de los ViewModels con `FakeAuthRepository`
ampliado. En emulador: CA-14, CA-15 (petición real), CA-18 y CA-19 con
`adb shell am start -a android.intent.action.VIEW -d "documind://reset-password?…"`.
CA-16/CA-17 de punta a punta requieren el correo y la configuración del
Dashboard (abajo).

**Riesgos.**
- *Redirect URL no permitida:* Supabase solo redirige a URLs de la lista
  *Authentication → URL Configuration → Redirect URLs*. Hay que añadir
  `documind://reset-password`; si no, el enlace acaba en la *Site URL* y la app no
  se abre. No se puede configurar con las herramientas disponibles: queda
  pendiente para la persona.
- *Un solo verificador PKCE guardado:* si tras pedir el enlace se pide otro (o se
  hace un registro), el primer enlace deja de valer y se muestra «enlace no
  válido» con la opción de pedir otro.
- *Límite de correos del SMTP por defecto de Supabase* (pocos por hora): se
  muestra `RateLimited`.

<!-- ANTES DE SOLICITAR APROBACIÓN
Comprueba que el plan cubre los requisitos, respeta las exclusiones, reutiliza
componentes verificados y permite demostrar todos los criterios de aceptación.
Resuelve dudas y marcadores pendientes. Si la spec cambió, revisa su impacto.
Tras aprobar el plan, deriva TASKS.md con IDs, dependencias, referencias a RF/CA
y comprobaciones. No marques una tarea terminada sin realizar su validación;
si está bloqueada, registra el motivo.
-->
