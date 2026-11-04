

# AGENTS.md — DocuMind

## General Rules

* Always read `SPEC.md` before implementing features.
* Follow the provided Figma design as the source of truth for UI.
* Do not invent features without checking the specification.
* Prioritize clean, maintainable and scalable code.
* Never implement everything in one step.
* Ask before making major architectural changes.

## Architecture

Clean Architecture with unidirectional data flow. Read `SPEC.md` for more details.

* **UI:** Jetpack Compose with Material 3.
* **State Management:** `StateFlow` and `collectAsStateWithLifecycle()`.
* **Dependency Injection:** Koin.
* **Navigation:** Type-safe navigation using `@Serializable` classes.
* **Persistence:** Room.
* **Backend:** Supabase.
* **Asynchronous Programming:** Kotlin Coroutines and Flow.

### Frontend

* Kotlin Multiplatform.
* Compose Multiplatform.
* Clean Architecture.
* MVVM.
* Repository pattern.
* Koin for dependency injection.
* Ktor for networking.

Keep UI, domain and data layers separated.

### Backend

* Python + FastAPI.
* Modular architecture.
* Separate routers, services, repositories and models.
* Use async operations where appropriate.
* Validate all incoming requests with Pydantic.

## AI & RAG

* Gemini API calls must always go through FastAPI.
* Never expose API keys in the frontend.
* Keep the RAG pipeline modular.
* Make chunking, embeddings and retrieval configurable.
* LAB parameters must affect actual backend execution.
* Never fabricate document citations.
* AI responses must prioritize retrieved document context.

## Security

* Validate Supabase JWT tokens in protected endpoints.
* Enforce user ownership on every resource.
* Use Supabase RLS.
* Never expose service-role keys.
* Store secrets in environment variables.
* Validate uploaded files and their sizes.

---

## UI Component Reuse and Consistency

* Before creating a new Compose component, search the codebase for an existing equivalent.
* UI elements with the same purpose and visual behavior must reuse the same shared component.
* Do not duplicate implementations of buttons, text fields, cards, top bars, or other recurring UI patterns.
* Centralize colors, typography, shapes, spacing, and shared styles through the app's design system.
* When a variation is needed, prefer adding parameters or an explicit variant to the existing component rather than duplicating it.
* Allow justified exceptions when specific screens have different functional or design requirements.
* When modifying a shared component, check all screens that use it and validate that no regressions have been introduced.
* At the end of each UI task, report which components were reused, which new components were created, and which duplications were identified.
