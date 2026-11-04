# Project Structure and Architecture

The project follows **Clean Architecture** principles, organizing code by layers and features (*feature-first*).

The architecture must keep presentation, business logic, and data access responsibilities separate, promoting code reuse, maintainability, and scalability.

# 1. Frontend — Kotlin Multiplatform

The frontend uses:

* Kotlin Multiplatform.
* Compose Multiplatform with Material 3.
* Clean Architecture.
* MVVM.
* Repository Pattern.
* Koin for dependency injection.
* Kotlin Coroutines and Flow.
* Ktor for networking.
* Room for local persistence, where compatible with the configured targets.
* Supabase for authentication and backend services.

## 1.1. General Structure

Shared code must be placed in `commonMain`. Platform-specific implementations must remain in their respective source sets.

```text
shared/
└── src/
    ├── commonMain/
    │   └── kotlin/
    │       └── app/
    │           ├── core/
    │           ├── data/
    │           ├── domain/
    │           ├── presentation/
    │           └── di/
    │
    ├── androidMain/
    │   └── kotlin/
    │       └── app/
    │
    ├── iosMain/
    │   └── kotlin/
    │       └── app/
    │
    └── commonTest/
```

The structure may evolve according to platform requirements, but architectural responsibilities must remain consistent.

## 1.2. core

Contains cross-cutting infrastructure and shared code that can be used by different layers without belonging exclusively to a specific feature or business responsibility.

Examples:

* General-purpose utilities.
* Kotlin extensions.
* Global constants.
* Shared base classes.
* Common technical abstractions.
* Interfaces for cross-cutting services.
* Dispatcher abstractions.

This directory must not become a generic container for code without a clearly defined responsibility.

**Restrictions:**

* Do not place repository implementations here.
* Do not place business logic here.
* Do not place UI components here.
* Do not place feature-specific code here.
* Do not introduce dependencies on `data` or `presentation`.

Platform-specific implementations must remain in their respective source sets.

## 1.3. data

Contains implementations related to data access, storage, and transformation.

It is responsible for communicating with local and external data sources and providing data to the domain layer through the corresponding interfaces.

Examples:

* Repository implementations.
* DataSources.
* APIs and network clients.
* Ktor.
* Supabase.
* Room.
* DataStore.
* DTOs.
* Persistence entities.
* Mappers between data and domain models.

### Structure

```text
data/
├── local/
│   ├── room/
│   └── datastore/
├── remote/
│   ├── api/
│   └── dto/
├── repository/
└── mapper/
```

The structure may grow according to project requirements, but duplication must be avoided and responsibilities must remain clearly separated.

### DataSources

DataSources encapsulate communication with a specific data source.

* Local DataSources handle access to databases and preferences.
* Remote DataSources handle communication with APIs and external services.
* Repositories coordinate data sources and expose models defined by the domain layer.

### DataStore

All DataStore access must be encapsulated within `data/local/datastore`.

* Do not access DataStore directly from a ViewModel.
* Do not access DataStore directly from a use case.
* Do not access DataStore directly from the presentation layer.
* Create one or more DataSources responsible for reading and writing preferences.

### Room

Room access must remain encapsulated within the data layer.

Database queries and persistence operations must be exposed through DataSources and repositories. Database entities must never be passed directly to the presentation layer.

## 1.4. domain

Contains exclusively the application's business rules and business logic.

It is the central layer of the architecture and must remain independent of implementation details.

Examples:

* Domain entities.
* Use cases.
* Repository interfaces.
* Business rules.
* Domain-specific validations.
* Shared abstractions such as `DispatchersProvider`, when needed.

**Restrictions:**

* Must not depend on `data`.
* Must not depend on `presentation`.
* Must not depend on persistence frameworks.
* Must not depend on HTTP clients.
* Must not contain DTOs or database entities.
* Must not make direct calls to APIs or external services.

Repository interfaces are defined in `domain`, while their implementations belong in `data`.

## 1.5. presentation

Contains the user interface and presentation logic.

It is organized by features (*feature-first*), grouping all elements related to each feature.

### Structure

```text
presentation/
├── features/
│   ├── home/
│   │   ├── HomeScreen.kt
│   │   ├── HomeViewModel.kt
│   │   ├── HomeUiState.kt
│   │   └── components/
│   │
│   └── profile/
│       ├── ProfileScreen.kt
│       ├── ProfileViewModel.kt
│       ├── ProfileUiState.kt
│       └── components/
│
├── components/
├── navigation/
├── theme/
└── utils/
```

Each feature must group its exclusive components:

* Screen.
* ViewModel.
* UiState.
* Feature-specific components.
* Presentation models, when needed.

### Reusable Components

Components shared across multiple features must be placed in `presentation/components`.

Before creating a new component:

1. Search for an existing equivalent.
2. Check whether it can be reused.
3. If variations are needed, prefer parameters or explicit variants.
4. Avoid duplicating components with the same purpose and visual behavior.

Visual components must follow the centralized design system in `presentation/theme`.

### State Management

* Use `StateFlow` to expose UI state.
* Use `collectAsStateWithLifecycle()` when available for the corresponding platform.
* Maintain a `UiState` for each screen or feature when appropriate.
* ViewModels must coordinate use cases and transform their results into presentation states.
* Do not implement business logic directly inside screens.

### Navigation

* Use type-safe navigation.
* Define routes using `@Serializable` classes or objects.
* Keep navigation configuration centralized.
* Avoid coupling ViewModels to concrete navigation implementations.

## 1.6. di

Contains the dependency injection configuration using Koin.

All dependency injection configuration must be centralized in this directory. Avoid scattering DI modules across different layers.

Example:

```text
di/
├── AppModule.kt
├── DataModule.kt
├── DomainModule.kt
└── PresentationModule.kt
```

Modules must register dependencies according to their responsibilities.

**Restrictions:**

* Use Koin as the only dependency injection solution.
* Do not introduce Hilt or Dagger.
* Do not create additional `di` directories inside individual layers.
* Keep dependencies explicit and properly organized.
* Platform-specific dependencies must be configured in their respective source sets when necessary.

---

# 2. Backend — Python + FastAPI

The backend uses a modular architecture that separates request handling, business logic, and data access responsibilities.

### General Structure

```text
backend/
└── app/
    ├── api/
    │   └── routers/
    ├── core/
    ├── models/
    ├── schemas/
    ├── services/
    ├── repositories/
    ├── dependencies/
    └── main.py
```

### Responsibilities

**api/routers/**

* Define API endpoints.
* Handle incoming requests.
* Validate input data using the appropriate schemas.
* Delegate processing to services.

**core/**

* General configuration.
* Environment variables.
* Security.
* Constants.
* Cross-cutting configuration.

**models/**

* Persistence models and entities used by the backend.

**schemas/**

* Pydantic schemas.
* Request and response validation.
* API communication contracts.

**services/**

* Implement backend business logic.
* Coordinate repositories and external services.
* Handle asynchronous operations when appropriate.

**repositories/**

* Encapsulate access to databases and external data sources.
* Prevent services from depending directly on persistence implementation details.

**dependencies/**

* Define shared FastAPI dependencies.
* Handle authentication, authorization, and shared resources.

### Backend Rules

* Keep routers, services, and repositories separated.
* Use asynchronous operations when supported by the underlying dependencies.
* Validate inputs and outputs using Pydantic where appropriate.
* Do not place complex business logic inside endpoints.
* Handle errors consistently.
* Store credentials and secrets in environment variables.
* Validate authentication and authorization on protected endpoints.
* Verify resource ownership before allowing access or modifications.

---

# 3. General Architecture Rules

The following rules apply throughout the project:

1. Keep presentation, domain, and data responsibilities separated.
2. The domain layer must not depend on external implementations.
3. Dependencies must point toward the domain, never in the opposite direction.
4. Avoid circular dependencies between modules and layers.
5. Avoid unnecessary abstractions and overengineering.
6. Reuse existing code before creating new implementations.
7. Keep platform-specific implementations outside shared code when appropriate.
8. Do not introduce new technologies or dependencies without justifying their necessity.
9. Follow the project's established patterns and conventions.
10. Prioritize readable, maintainable, testable, and scalable code.

**Fundamental principle:** Each layer must only know about the abstractions it needs to fulfill its responsibilities. Implementation details must remain encapsulated within the appropriate layer.
