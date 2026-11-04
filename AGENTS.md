# AGENTS.md — DocuMind

## General Rules

* Read `SPEC.md` before implementing features.
* Follow Figma designs as the source of truth for UI.
* Do not invent functionality outside the specification.
* Prioritize clean, reusable, maintainable code.
* Implement incrementally, never everything at once.
* Ask before making major architectural changes.

## Architecture

* Language: Kotlin
* UI: Jetpack Compose
* Backend: FastAPI
* Authentication and database: Supabase
* AI integration: See `SPEC.md` for the configured provider and implementation details.

## AI & RAG

* All AI requests must go through FastAPI.
* Never expose API keys or secrets in the frontend.
* Keep the RAG pipeline modular and configurable.
* Ensure LAB parameters affect actual backend execution.
* Prioritize retrieved document context in AI responses.
* Never fabricate document citations.

## Security

* Validate Supabase JWTs on protected endpoints.
* Enforce resource ownership and Supabase RLS.
* Never expose service-role keys or secrets.
* Store credentials in environment variables.
* Validate uploaded files, types, and sizes.

## UI Consistency

* Search for existing Compose components before creating new ones.
* Reuse shared components for recurring UI patterns.
* Centralize styles through the design system.
* Prefer configurable variants over duplicated components.
* Check all affected screens when modifying shared components.
* Validate UI changes for regressions.