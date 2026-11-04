# Estructura del proyecto

El proyecto sigue una **Clean Architecture** organizada por capas y por funcionalidades.

## Estructura principal

```text
app/
├── core/
├── data/
├── domain/
├── presentation/
└── di/
```

## core

Contiene código compartido e infraestructura que no pertenece exclusivamente a ninguna capa.

Ejemplos:

* Utilidades.
* Extensiones.
* `DefaultDispatchersProvider`.
* Constantes.
* Clases base.
* Servicios, Workers y Receivers compartidos.

No colocar en `core` clases relacionadas con acceso a datos, lógica de negocio o UI.

---

## data

Contiene únicamente la implementación del acceso a datos y las dependencias externas.

Ejemplos:

* Implementaciones de repositorios.
* DataSources.
* Room.
* DataStore.
* Supabase.
* APIs.
* DTOs.
* Entidades de base de datos.
* Mappers entre modelos de datos y dominio.

### Organización

```text
data/
├── local/
│   ├── room/
│   └── datastore/
├── remote/
├── repository/
└── mapper/
```

### DataStore

Todo el acceso a `DataStore` debe encapsularse dentro de `data/local/datastore`.

No acceder directamente a `DataStore` desde un `ViewModel`, caso de uso o repositorio.

Crear uno o varios `DataSource` responsables de leer y escribir preferencias.

---

## domain

Contiene únicamente la lógica de negocio.

Ejemplos:

* Entidades.
* Casos de uso.
* Interfaces de repositorios.
* Interfaces compartidas como `DispatchersProvider`.

El dominio no debe depender de `data` ni de `presentation`.

---

## presentation

La capa de presentación está organizada por funcionalidades (feature-first).

Cada pantalla debe contener todo lo relacionado con ella:

* `Screen`
* `ViewModel`
* Componentes exclusivos de esa pantalla

Los componentes reutilizables deben ubicarse en:

```text
presentation/
├── components/
├── navigation/
├── theme/
└── utils/
```

---

## di

Toda la configuración de Hilt debe estar centralizada en la carpeta raíz `di`.

No crear carpetas `di` dentro de `data`, `domain`, `presentation` ni de ninguna otra carpeta.

---
