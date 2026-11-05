# PLAN: Conversión a Kotlin Multiplatform

**SPEC de referencia:** `ai-docs/features/kmp-conversion/SPEC.md`
**Versión de la spec revisada:** 2026-11-04 (sobre el commit `d9ccabf`)
**Estado:** Aprobado <!-- Borrador | En revisión | Aprobado -->

> Aprobado por delegación el 2026-11-04. La usuaria pidió «no me preguntes nada, haz lo que creas». Las versiones y la estructura las comprobó el agente compilando un prototipo antes de cerrar el plan.

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
| Módulo único Android | `app/` (AGP 9.2.1 con Kotlin integrado; Kotlin 2.2.10; compileSdk 37; minSdk 24) | Pasa a ser solo el punto de entrada Android |
| Dominio y datos de auth | `app/src/main/java/.../domain`, `.../data` | Kotlin puro y supabase-kt (KMP): se mueven sin cambios |
| Pantallas, tema y componentes | `.../ui` | Compose: se mueven y cambian `R` por `Res` de Compose Multiplatform |
| Navegación | `.../navigation/AppNavigation.kt` | Usaba `android.net.Uri`, `Intent` y rutas de texto: se reescribe |
| DI | `.../di/AppModule.kt` | Leía `BuildConfig`: recibe la configuración como parámetro |
| Recursos | `res/values{,-es}/strings.xml`, `res/drawable/ic_*.xml`, `res/font/inter_*.ttf` | Pasan a `composeResources` |
| Tests | `app/src/test` (38 tests y `ExampleUnitTest`), `app/src/androidTest` | Los unitarios pasan a `commonTest`; `DocumentTabBarTest` se queda en `app` |

**Convenciones y patrón de referencia:**
- Se mantienen las decisiones de Brain: tokens del diseño en `ui/theme`, componentes `Dm*` y auth con supabase-kt 3.2.6 y Koin 4.1.1 (fijadas por Kotlin 2.2).
- Se mantiene el paquete `com.example.chatbot` y la carpeta `ui/` en lugar de `presentation/`. Así no se mezcla un cambio de nombres con la migración.

## Solución propuesta

<!-- Explica el enfoque y sus motivos. Describe las responsabilidades y el
recorrido de datos y eventos hasta la interfaz. Usa un diagrama si aporta claridad. -->

AGP 9 no admite el plugin `kotlin-multiplatform` en un módulo `com.android.application`. Por eso, en vez de convertir `:app` como proponía el borrador anterior, se crea un módulo nuevo `:shared` con `com.android.kotlin.multiplatform.library`. Contiene:

- **Destinos:** Android, `iosArm64` e `iosSimulatorArm64`, con un framework estático `Shared`.
- **`commonMain`:** dominio, datos, DI, navegación, UI y recursos.
- **`androidMain` e `iosMain`:** solo el motor Ktor y el formato de porcentajes de cada plataforma.

`:app` depende de `:shared` y solo conserva lo propio de Android:

- `DocuMindApplication`, que llama a `initKoin(SupabaseConfig(BuildConfig…)) { androidContext(…) }`;
- `MainActivity`;
- el manifiesto, el icono, `themes.xml` y `app_name`.

```
MainActivity ──intent.dataString──▶ App(incomingLink) ─▶ ChatbotTheme ─▶ AppNavigation
   onNewIntent ─────────────────────▲                                       │
                                     HandleResetLinks: ResetLink.parse ─▶ navigate(ResetPasswordRoute)
iOS (pendiente): MainViewController(config) ─▶ initKoin + ComposeUIViewController { App() }
```

## Módulos y componentes afectados

<!-- Si el proyecto está modularizado, identifica los módulos afectados, sus
responsabilidades y la dirección de sus dependencias. Respeta los límites
existentes y justifica cualquier módulo o dependencia nueva. Si no está
modularizado, describe las carpetas o componentes afectados sin introducir
modularización fuera del alcance; marca la tabla de módulos como No aplica. -->

| Módulo | Existe / nuevo | Responsabilidad y cambios | Dependencias afectadas |
| --- | --- | --- | --- |
| `:shared` | Nuevo | Todo el código de la app y los recursos | Compose Multiplatform, lifecycle y navigation de JetBrains, Koin, supabase-kt, Ktor |
| `:app` | Existente | Punto de entrada Android, configuración de Supabase y recepción de enlaces | Pasa a depender de `:shared`; deja de depender de navigation, Material3, iconos y lifecycle-compose |

<!-- Distingue lo que se reutiliza, modifica o crea. Las rutas nuevas son propuestas.
Señala impacto sobre modelos, contratos o componentes compartidos. -->

| Componente o ruta | Acción | Cambio y responsabilidad | Requisito relacionado |
| --- | --- | --- | --- |
| `domain/`, `data/`, `ui/`, `navigation/SessionViewModel.kt` | Mover a `shared/src/commonMain/kotlin` con `git mv` | Se cambia `R.*` por `Res.*` y se actualizan los imports de recursos, `Preview`, `koinViewModel` y `BackHandler` | RF-02 |
| `ui/theme/Type.kt`, `Theme.kt` | Modificar | Las fuentes de recursos solo se cargan en composición: `interFontFamily()` + `materialTypography(inter)` + `DocuMindTypography(inter)` | RF-01, RF-02 |
| `navigation/Routes.kt` | Crear | Rutas `@Serializable`. Los nombres de los argumentos son las claves de `SavedStateHandle` que ya usaban los ViewModel (`email`, `code`, `error_code`) | RF-02 |
| `navigation/ResetLink.kt` | Crear | Lee `documind://reset-password` con `io.ktor.http.Url` en lugar de `android.net.Uri` | RF-01, RF-03 |
| `navigation/AppNavigation.kt` | Reescribir | Rutas con tipos. `HandleResetLinks` recibe `incomingLink` y recuerda el último enlace con `rememberSaveable`, como antes | RF-01, RF-02 |
| `App.kt`, `di/AppModule.kt` | Crear / modificar | `App(incomingLink)`, `SupabaseConfig`, `initKoin`, `appModule(config)` | RF-02, RF-03 |
| `ui/utils/PercentFormat*.kt` | Crear (`expect`/`actual`) | Sustituye `java.text.NumberFormat`. En iOS usa `NSNumberFormatter` | RF-01 |
| `ui/screens/detail/ChatTab.kt` | Modificar | Cambia `System.currentTimeMillis()` por `kotlin.time.Clock` | RF-02 |
| `iosMain/MainViewController.kt` | Crear | Punto de entrada iOS | RF-04 |
| `MainActivity.kt`, `DocuMindApplication.kt` | Modificar | `App(incomingLink)`; `onNewIntent` actualiza el enlace; Koin con la configuración de `BuildConfig` | RF-03 |
| `composeResources/values{,-es}/strings.xml` | Mover y adaptar | Compose Multiplatform solo sustituye argumentos con posición (`%d` → `%1$d`) y no procesa `\'` ni las comillas de Android: quitar `\'` y usar ` ` para los espacios iniciales | RF-01 |
| Tests unitarios | Mover a `commonTest` | Pasan de JUnit a `kotlin.test`; `MainDispatcherRule` se convierte en la clase base `MainDispatcherTest`; se añade `ResetLinkTest` | RF-05 |
| `DocumentTabBarTest` | Modificar | Lee los textos con `getString(Res.string…)` | RF-01 |

## Datos y contratos

<!-- Completa solo lo aplicable. Si un punto no aplica, indica el motivo. -->

- **Modelos y contratos de entrada y salida:** sin cambios en `AuthRepository`, `AuthResult` ni `AuthError`. Nuevos: `SupabaseConfig(url, publishableKey)`, `ResetLink(code, errorCode)` y las rutas `@Serializable`.
- **Identificadores, relaciones y restricciones:** se mantiene `applicationId` `com.example.chatbot`. El namespace del módulo compartido es `com.example.chatbot.shared` y la clase de recursos `com.example.chatbot.resources.Res` es pública para que la use `androidTest`.
- **Origen de los datos mostrados y transformaciones:** los mismos que antes.
- **Persistencia, consultas y actualizaciones:** supabase-kt guarda la sesión con su propio almacenamiento, ligado a la app y no al módulo. Al actualizar se lee la sesión existente (CA-05).
- **Convivencia entre datos locales y remotos:** no aplica; no cambia.
- **Compatibilidad y migraciones de datos existentes:** no hay migración. Solo cambia dónde vive el código.

## Estado, operaciones y errores

<!-- Cómo se implementan los comportamientos aprobados en la spec.
Referencia RF/CA y aplica las consideraciones relevantes de MOBILE_GUIDELINES.md. -->

- **Gestión del estado de interfaz y navegación:** los mismos ViewModel con `StateFlow` y los mismos eventos con `Channel`. Las rutas con tipos sustituyen a las de texto sin cambiar la pila de navegación.
- **Conservación y restauración del estado:** `SavedStateHandle` (KMP) y `rememberSaveable`, igual que antes (CA-03).
- **Ejecución, concurrencia y cancelación de operaciones:** sin cambios.
- **Errores, reintentos y prevención de duplicados:** sin cambios. Un enlace de recuperación ya usado no se reabre al recrear la actividad (CA-02).
- **Otras consideraciones mobile aplicables y su solución:** se mantienen edge-to-edge (`enableEdgeToEdge` en `MainActivity`, `adjustResize` y `Scaffold` con `safeDrawing` en las pantallas). Atrás en «Nueva contraseña» usa `BackHandler` de `ui-backhandler`, que en Compose Multiplatform 1.9 es experimental.

## Dependencias y configuración

<!-- Librerías, servicios, permisos o configuración afectados. Verifica compatibilidad
con el proyecto y justifica las incorporaciones. No agregues dependencias por defecto. -->

Las versiones se comprobaron en Maven Central y compilando:

| Dependencia | Versión | Motivo |
| --- | --- | --- |
| Plugins `org.jetbrains.kotlin.multiplatform` y `plugin.serialization` | 2.2.10 | La misma versión de Kotlin que el proyecto |
| Plugin `com.android.kotlin.multiplatform.library` | 9.2.1 | La misma versión que AGP |
| Compose Multiplatform | 1.9.3 | La última estable compatible con Kotlin 2.2.10. 1.10 solo tiene Material3 en alpha |
| Material3 multiplataforma | 1.9.0 | Compilado con Kotlin 2.1.21 |
| `material-icons-extended` | 1.7.3 | La última publicada; ya se usaban esos iconos |
| `org.jetbrains.androidx.lifecycle` | 2.9.4 | Versión de lifecycle KMP alineada con Compose Multiplatform 1.9 |
| `org.jetbrains.androidx.navigation` | 2.9.0 | Versión de navigation KMP alineada con Compose Multiplatform 1.9 |
| Koin `koin-core`, `koin-compose`, `koin-compose-viewmodel` | 4.1.1 (BOM existente) | Sin cambio de versión |
| `ktor-client-darwin` | 3.3.1 | Motor Ktor para iOS; Android sigue con OkHttp |

- **Se eliminan del catálogo:** navigation, Material3, iconos y lifecycle-compose de androidx, `lifecycle-runtime-ktx` y `koin-androidx-compose`.
- **`:app` conserva:** el BOM de Compose para `activity-compose`, `ui-tooling` y los tests instrumentados.
- **`gradle.properties`:** `kotlin.native.ignoreDisabledTargets=true`. En Windows los destinos iOS no se pueden compilar y se omiten sin avisos.

## Estrategia de validación

<!-- Una fila por criterio de la spec. Selecciona el método capaz de demostrarlo:
test unitario, integración, UI o prueba manual. No todos requieren todos los métodos.
Identifica tests existentes y separa los nuevos propuestos. Incluye regresiones relevantes.
Una captura aislada no demuestra persistencia ni ausencia de peticiones de red. -->

| Criterio | Método y test existente o propuesto | Entorno y datos necesarios | Evidencia prevista |
| --- | --- | --- | --- |
| CA-01 | Recorrido manual con ADB en inglés y en español; comparación de textos decodificando los `.cvr` generados | Emulador `emulator-5554` | Volcado de UI y capturas |
| CA-02 | `adb shell am start -d` con `error_code=otp_expired` y con un código falso, con la app abierta y cerrada; girar | Emulador | Volcado de UI |
| CA-03 | Girar y `am kill` con datos escritos | Emulador | Volcado de UI |
| CA-04 | `:shared:compileCommonMainKotlinMetadata`, `:shared:compileAndroidMain`, `:app:assembleDebug`; revisión de los archivos de `:app` | Windows | Salida de Gradle |
| CA-05 | Instalar con `install -r` encima de la app con sesión; y con `pm clear` | Emulador | Pantalla inicial |
| CA-06 | `:shared:testAndroidHostTest` | Windows | 43 tests verdes |

**Comprobaciones de regresión:**
- `DocumentTabBarTest` (`connectedDebugAndroidTest`) y lint.
- Búsqueda de tokens del diseño fuera de `ui/theme` y `ui/components` (ahora en `shared/src/commonMain`).

**Comandos verificados para compilar y ejecutar tests:**
- `./gradlew :shared:testAndroidHostTest :shared:compileCommonMainKotlinMetadata :app:assembleDebug lint`
- `./gradlew :app:connectedDebugAndroidTest`, solo con el emulador conectado.
- Instalar solo en el emulador: `adb -s emulator-5554 install -r app/build/outputs/apk/debug/app-debug.apk`. Lección de auth: `installDebug` instala en todos los dispositivos conectados.

**Pruebas en dispositivo, emulador o simulador:** emulador Android para CA-01, CA-02, CA-03 y CA-05.

**Limitaciones del entorno:** el código de iOS (`iosMain` y el enlace del framework) no se puede compilar en Windows. El código común se comprueba con los metadatos comunes. La compilación iOS queda pendiente para un Mac.

<!-- Esta sección planifica la validación. Durante la implementación, registra
en TASKS.md o en el informe de validación acordado los resultados y evidencias
reales. Distingue pruebas ejecutadas, fallidas, no ejecutadas y bloqueadas.
Compilar o tener tests en verde no sustituye revisar los criterios de la spec. -->

## Orden de implementación

<!-- Etapas y dependencias principales. El desglose ejecutable se escribe en TASKS.md.
Incluye puntos de comprobación para avanzar con cambios pequeños. -->

1. Configurar Gradle con `:shared` vacío. Comprobación: la configuración de Gradle carga sin errores.
2. Mover el código y los recursos con `git mv` y adaptarlos (R → Res, tipografía, porcentajes, reloj). Comprobación: compilan los metadatos comunes y Android.
3. Reescribir navegación y DI, y adelgazar `:app`. Comprobación: `assembleDebug`.
4. Migrar los tests y añadir `ResetLinkTest`. Comprobación: tests verdes.
5. Adaptar los textos que Compose Multiplatform interpreta distinto. Comprobación: decodificar los `.cvr`.
6. Lint, tests instrumentados y validación manual en el emulador.

## Riesgos y decisiones pendientes

<!-- Riesgos concretos de esta solución y cómo se resolverán, sin listas genéricas.
Escribe Ninguna en las decisiones pendientes cuando estén resueltas. -->

- **Riesgos y medidas acordadas:**
  - **Textos de Android con escapes que Compose Multiplatform no interpreta.** Se decodifican todos los textos generados y se buscan comillas, barras invertidas y espacios.
  - **El código iOS no se ha compilado.** Queda pendiente, documentado.
  - **Compose para Android:** `:app` sigue resolviendo Compose desde el BOM 2026.02.01, una versión más nueva que la de Compose Multiplatform 1.9. Así se mantiene el mismo runtime de Compose en Android que antes, y Gradle elige la más alta.
  - **Rutas con tipos y enlaces del correo:** pueden cambiar cómo se reciben los argumentos. Mitigación: se mantienen los nombres de los argumentos y se prueba en el emulador (CA-02).
- **Decisiones pendientes:** Ninguna.

<!-- ANTES DE SOLICITAR APROBACIÓN
Comprueba que el plan cubre los requisitos, respeta las exclusiones, reutiliza
componentes verificados y permite demostrar todos los criterios de aceptación.
Resuelve dudas y marcadores pendientes. Si la spec cambió, revisa su impacto.
Tras aprobar el plan, deriva TASKS.md con IDs, dependencias, referencias a RF/CA
y comprobaciones. No marques una tarea terminada sin realizar su validación;
si está bloqueada, registra el motivo.
-->
