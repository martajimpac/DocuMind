# Decisions

<!--
Architecture decisions specific to this project.
Append entries with: mobiai brain save decision (coming in Phase 2).
Each entry should record: title, status (active|deprecated), platform,
area, date, decision, reason, files.
-->

## Design system: ui/theme tokens + ui/components shared composables

- id: design-system-ui-theme-tokens-ui-components-shared-composabl-20261030-082236
- type: architecture_decision
- status: active
- platform: android
- area: ui_design_system
- date: 2026-10-30

Decision (figma-ui-redesign, 2026-10-30): all colours, type, shapes and sizes live in ui/theme (Material slots + DocuMindTheme.colors/typography/shapes for values without a slot). Screens compose ui/components (Dm* prefix) and never declare Color(0x..), fontSize, .sp, FontWeight or RoundedCornerShape. Fixed light palette (no dynamic colour, no dark theme); SystemBarStyle.light in MainActivity. Icons are Figma SVGs converted to res/drawable/ic_*.xml, tinted at call site. Inter bundled in res/font (OFL licence in docs/licenses). Check with: grep -rnE 'Color\(0x|fontSize|RoundedCornerShape\(|\.sp\b|FontWeight\.' outside ui/theme and ui/components must return nothing.

### Files
- app/src/main/java/com/example/chatbot/ui/theme
- app/src/main/java/com/example/chatbot/ui/components

## Auth: Supabase Auth via supabase-kt 3.2.6 + Koin, session-driven start screen

- id: auth-supabase-auth-via-supabase-kt-3-2-6-koin-session-driven-20261103-192901
- type: architecture_decision
- status: active
- platform: android
- area: auth
- date: 2026-11-03

Decision (auth, 2026-11-03): email/password auth with Supabase Auth through supabase-kt 3.2.6 (last release built with Kotlin 2.2; 3.7+ needs Kotlin 2.4), Ktor OkHttp 3.3.1, Koin 4.1.1 (4.2 needs Kotlin 2.3). Domain AuthRepository hides Supabase; SupabaseAuthRepository maps SessionStatus (RefreshFailure counts as signed in so the app opens offline) and AuthRestException codes. SessionViewModel picks the start screen once and emits SignedOut through a Channel. Supabase URL and publishable key come from local.properties into BuildConfig (never the service role key). Email confirmation is required on the server (mailer_autoconfirm=false). Keyboard Next must use moveFocus(Down): default traversal stops on the password visibility button.

### Files
- app/src/main/java/com/example/chatbot/data/repository/SupabaseAuthRepository.kt
- app/src/main/java/com/example/chatbot/navigation/SessionViewModel.kt
- app/src/main/java/com/example/chatbot/di/AppModule.kt
- docs/features/auth/PLAN.md

## Kotlin Multiplatform: :shared (KMP library) + thin :app

- id: kotlin-multiplatform-shared-kmp-library-thin-app-20261104-121639
- type: architecture_decision
- status: active
- platform: kmp
- area: architecture
- date: 2026-11-04

Decision (kmp-conversion, 2026-11-04): AGP 9 does not allow kotlin-multiplatform in a com.android.application module, so all app code (domain, data, di, navigation, ui, resources) lives in :shared (com.android.kotlin.multiplatform.library + iosArm64/iosSimulatorArm64, framework Shared) and :app only keeps MainActivity, DocuMindApplication (initKoin with SupabaseConfig from BuildConfig), manifest and launcher assets. Versions pinned by Kotlin 2.2.10: Compose Multiplatform 1.9.3, material3 1.9.0, JetBrains lifecycle 2.9.4 and navigation 2.9.0, ktor-client-darwin 3.3.1. Type-safe @Serializable routes; argument names equal the SavedStateHandle keys. MainActivity passes intent.dataString (also onNewIntent) to App(incomingLink); ResetLink.parse uses io.ktor.http.Url. Tests run with :shared:testAndroidHostTest. iOS is not compiled on Windows.

### Files
- shared/build.gradle.kts
- app/src/main/java/com/example/chatbot/MainActivity.kt
- shared/src/commonMain/kotlin/com/example/chatbot/navigation/AppNavigation.kt
- docs/features/kmp-conversion/PLAN.md

## Notebooks: upload-first to Storage, rows via one SQL function, RLS everywhere

- id: notebooks-upload-first-to-storage-rows-via-one-sql-function-20261105-091414
- type: architecture_decision
- status: active
- platform: kmp
- area: notebooks_documents
- date: 2026-11-05

Decision (notebooks-documents, 2026-11-05): the app talks to Supabase directly (no FastAPI for this feature). PDFs go to the private bucket documents at {user_id}/{notebook_id}/{document_id}.pdf (20 MB, application/pdf); then create_notebook_with_document / add_document (SECURITY INVOKER) create the rows in one transaction, computing the path and taking size_bytes from storage.objects metadata. A before-insert trigger with pg_advisory_xact_lock caps 20 sources per notebook. Tables have RLS select/insert own only (no update/delete yet). The repository deletes the file on upload failure or cancellation, but not after the RPC was sent (unknown outcome): a stray file is preferred to a source without file. Migrations live in supabase/migrations and are applied with the Supabase MCP (no CLI on this PC). Test RLS with a DO block that switches role/jwt claims and ends with RAISE EXCEPTION so everything rolls back.

### Files
- supabase/migrations/20261104180000_notebooks_documents.sql
- shared/src/commonMain/kotlin/com/example/chatbot/data/repository/SupabaseNotebookRepository.kt
- docs/features/notebooks-documents/PLAN.md
