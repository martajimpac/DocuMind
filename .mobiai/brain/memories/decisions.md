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
