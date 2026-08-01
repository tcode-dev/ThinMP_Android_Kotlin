# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

ThinMP is a lightweight Android music player built in Kotlin. It plays audio from device storage, supports favorites, playlists, shortcuts, and background playback.

## Build & Run

```bash
./gradlew assembleDebug        # Build debug APK
./gradlew test                 # Run unit tests
./gradlew connectedAndroidTest # Run instrumented tests (Room repository tests live here)
```

- Compile/Target SDK: 36, Min SDK: 33 (Android 13+)
- Java target: JDK 17
- Kotlin with KAPT (for Hilt and Room)

## Architecture

Layered architecture with MVVM:

```
View (Jetpack Compose) → ViewModel (StateFlow) → Service → Repository → Model
View → ViewModel → Register → Repository  (for domain operations like favorites/playlists)
```

- **View**: Jetpack Compose screens in `view/screen/`, UI components in `view/cell/`, `view/layout/`, `view/button/`
- **ViewModel**: One per screen, uses `StateFlow` for state
- **Service**: Business logic layer (e.g., `MainService`, `ArtistDetailService`)
- **Register**: Domain interfaces for favorites/playlists, mixed into ViewModels
- **Repository**: All repositories in `repository/` (MediaStore queries and Room DB access), DAOs in `repository/dao/`
- **Model**: `model/media/` for data models, `model/media/valueObject/` for type-safe IDs (`SongId`, `AlbumId`, `ArtistId`), `model/room/` for Room entities

## Key Technologies

- **UI**: Jetpack Compose + Material 3, Coil for images, Accompanist for insets/permissions
- **Playback**: MediaPlayer3 (ExoPlayer) 1.4.0, MusicService (foreground service), MediaSession
- **DI**: Hilt
- **Database**: Room (favorites, playlists, shortcuts), AppDatabase singleton via `MainApplication.appContext`. Schemas exported to `app/schemas/`
- **Preferences**: DataStore Preferences (repeat, shuffle, menu visibility)
- **Async**: Kotlin Coroutines. All Room and MediaStore I/O is off the main thread

## Key Directories

```
app/src/main/java/dev/tcode/thinmp/
├── activity/          # MainActivity (single activity)
├── application/       # MainApplication (Hilt entry point, provides appContext)
├── config/            # ConfigStore (DataStore preferences)
├── constant/          # Navigation routes, style, notification constants
├── model/             # Data models, value objects, Room entities
├── notification/      # Playback notification helper
├── player/            # MusicPlayer, MusicService
├── receiver/          # HeadsetEventReceiver
├── register/          # Domain logic interfaces (favorites, playlists)
├── repository/        # MediaStore and Room data access, dao/ subdirectory for Room DAOs
├── service/           # Business logic services
├── view/              # All Compose UI (screens, cells, layouts, nav)
└── ui/theme/          # Compose theme
```

## Conventions

- Single-activity app with Compose navigation
- No XML layouts — entirely Jetpack Compose
- Type-safe value objects for IDs (never raw strings/longs for entity IDs)
- Each screen has a dedicated ViewModel
- Room for user-created data; MediaStore for device audio
- Room repositories default to `MainApplication.appContext` for DB access but take the DAO or `AppDatabase` as a constructor argument, so tests can supply an in-memory database
- Register interfaces create repository instances on-demand in each method
- No ProGuard/R8 minification enabled

### Threading

- **Every function in `repository/dao/` is `suspend`.** That is what makes Room's
  `assertNotMainThread()` unreachable and lets `AppDatabase` drop
  `.allowMainThreadQueries()`. Adding a non-suspend DAO function reintroduces main-thread I/O
  and breaks `MainThreadAccessTest`
- **Do not wrap suspend Room calls in `withContext(Dispatchers.IO)`.** Room already moves them
  to its own query executor, and switching dispatchers inside `withTransaction { }` leaves the
  transaction's thread and breaks it
- `withContext(Dispatchers.IO)` belongs in exactly two places: `MediaStoreRepository.get()` /
  `getList()` (plain blocking `ContentResolver.query()`), and the `ConfigStore` reads in
  `MainService` / `MainEditViewModel`, which are marked TODO until `ConfigStore` stops using
  `runBlocking`
- Transactions: work expressible in one DAO gets `@Transaction` on a DAO method; work spanning
  DAOs or interleaved with Kotlin logic gets `db.withTransaction { }` in the repository
- **Never read a row and then write it from two separate calls.** Every suspend DAO call is a
  suspension point, so `if (exists(id)) delete(id) else insert(id)` lets two concurrent callers
  both see "absent" and insert twice — and no table here has a unique index to stop them. Such
  read-modify-write belongs in one `@Transaction` DAO method (`toggle`, `insertAtEnd`,
  `replaceAll`). This is not hypothetical: it is why `FavoriteSongDao.toggle` exists
- ViewModel `load()` runs in `viewModelScope` and cancels the previous job first
  (`loadJob?.cancel()`); `onResume` and `MusicPlayerListener.onError()` can both trigger it
- Edit screens save via `OneShotEvent` / `OnEvent` and navigate away only after the write
  completes. Writes triggered by something that closes immediately (dropdown menus, the playlist
  popup) run in `viewModelScope`, never `rememberCoroutineScope()`
- Dropdown menu items read their state with `produceState(initialValue, id)` so effect and state
  share one key
