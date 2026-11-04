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
