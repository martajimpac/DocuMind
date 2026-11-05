# Bugfixes

<!--
Bugfixes and workarounds worth remembering for this project.
Append entries with: mobiai brain save bugfix (coming in Phase 2).
Mark temporary workarounds as status: temporary so the agent does not
treat them as permanent decisions.
-->

## Navigation Compose navDeepLink did not open custom-scheme reset links

- id: navigation-compose-navdeeplink-did-not-open-custom-scheme-re-20261103-195027
- type: bug_fix
- status: active
- platform: android
- area: navigation
- date: 2026-11-03

Problem (2026-11-03): composable(deepLinks = navDeepLink { uriPattern = "documind://reset-password?code={code}&error_code={error_code}" }) never navigated, neither from the launch intent nor via addOnNewIntentListener + handleDeepLink, on navigation-compose 2.8.8 with a NavHost created after an async start-destination decision. Solution: HandleResetLinks in AppNavigation parses intent.data by scheme/host and navigates explicitly; it runs after NavHost, handles activity.intent and new intents, and remembers the last handled link with rememberSaveable so rotation or recreation does not reopen a used code. Root cause not confirmed.

### Files
- app/src/main/java/com/example/chatbot/navigation/AppNavigation.kt

## Compose Multiplatform strings differ from Android strings.xml

- id: compose-multiplatform-strings-differ-from-android-strings-xm-20261104-121639
- type: bug_fix
- status: active
- platform: kmp
- area: resources
- date: 2026-11-04

Problem (2026-11-04): after moving strings.xml to composeResources, the UI showed Don\'t and literal quotes. Compose Multiplatform 1.9 does not unescape \' and keeps Android quote wrapping (" · "), and only substitutes positional args (%1$d, not %d). Solution: plain apostrophes, \u0020 for leading/trailing spaces, %1$d in plurals. Check by decoding the generated .cvr files (base64) under shared/build/generated/assets.

### Files
- shared/src/commonMain/composeResources/values/strings.xml

## Large uploads time out on slow networks (OkHttp 10 s write timeout)

- id: large-uploads-time-out-on-slow-networks-okhttp-10-s-write-ti-20261105-091414
- type: platform_workaround
- status: temporary
- platform: kmp
- area: storage_upload
- date: 2026-11-05

Problem (2026-11-05): uploading a 19 MB PDF with the emulator network at EDGE or UMTS fails with SocketTimeoutException from okhttp Http2Stream write. storage-kt sets requestTimeoutMillis = transferTimeout for storage calls, but the OkHttp engine keeps its default ~10 s write/read timeout. The app shows No connection + Retry and leaves nothing behind. Not fixed yet: raising the socket timeout for storage uploads needs a decision (pending, see docs/features/notebooks-documents/TASKS.md).

### Files
- shared/src/commonMain/kotlin/com/example/chatbot/di/AppModule.kt

## Upload timeouts: own HTTP engine with 60 s socket timeout and 5 min transfer timeout

- id: upload-timeouts-own-http-engine-with-60-s-socket-timeout-and-20261105-102051
- type: bug_fix
- status: active
- platform: kmp
- area: storage_upload
- date: 2026-11-05

Follow-up of 'Large uploads time out on slow networks' (2026-11-05). Fix: SupabaseClientBuilder.httpEngine = createHttpEngine(60.seconds) (expect/actual: OkHttp read/write timeout on Android, Darwin timeoutIntervalForRequest on iOS) plus install(Storage) { transferTimeout = 5.minutes }. httpConfig { install(HttpTimeout) } is @SupabaseInternal in 3.2.6, so it was not used. On the emulator network shaping (UMTS) the upload still stalls at 8% and the server resets the HTTP/2 stream after ~20 s, so the benefit on real slow networks is unverified: test on a phone with mobile data. Also: connectedDebugAndroidTest uninstalls the app, which signs the emulator out. Note: feature docs moved from docs/ to ai-docs/ on 2026-11-05.

### Files
- shared/src/commonMain/kotlin/com/example/chatbot/data/remote/HttpEngine.kt
- shared/src/androidMain/kotlin/com/example/chatbot/data/remote/HttpEngine.android.kt
- shared/src/commonMain/kotlin/com/example/chatbot/di/AppModule.kt
- ai-docs/features/notebooks-documents/TASKS.md
