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

### Running the instrumented tests

`JAVA_HOME` comes from `env` in `.claude/settings.json`, so `./gradlew` needs no prefix. The Android
SDK is not on `PATH`, so the emulator and adb are addressed by full path:

```bash
~/Library/Android/sdk/emulator/emulator -avd Medium_Phone_API_36.1 -no-boot-anim
```

- `tools/push-test-audio.sh` puts audio on the device. Without it the MediaStore-backed tests skip
  themselves. `tools/push-compilation-audio.sh` builds the artist-with-no-album state by hand
- A freshly booted emulator can report `Starting 0 tests` → `Process crashed`. That means the APKs
  are not installed (`adb logcat` shows `Unable to find instrumentation info`), not a flaky
  emulator. `adb install -r -t` both `app-debug.apk` and `app-debug-androidTest.apk`, then re-run
- Shut the emulator down when finished (`adb emu kill`). A leftover instance blocks the next launch
  of the same AVD
- Anything that depends on the audio route changing cannot be tested on the emulator:
  `ACTION_HEADSET_PLUG` and `ACTION_AUDIO_BECOMING_NOISY` are protected broadcasts, and with no
  headset hardware `HEADSET_PLUG` never reaches the sticky list. Ask for a check on a real device
  rather than trying to fake it

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
- **Database**: Room (favorites, playlists, shortcuts), AppDatabase singleton via `MainApplication.appContext`. Schemas exported to `app/schemas/`. The app is unreleased, so schema changes edit version 1 in place and the exported schema is regenerated — no `Migration` is written. Once it ships, that stops being safe: there is no `fallbackToDestructiveMigration()`, so from then on every schema change needs a version bump and a `Migration` in `AppDatabase`
- **Preferences**: DataStore Preferences (repeat, shuffle, menu visibility)
- **Async**: Kotlin Coroutines. All Room and MediaStore I/O is off the main thread

### Looking up library APIs

The Context7 MCP server (`.mcp.json`) serves version-pinned documentation for the libraries above.
Use it **without being asked** — `resolve-library-id`, then `query-docs` — before writing or
changing code that calls a third-party API, and before answering a question about one. Model
training data lags behind Media3, Compose and Room, and a plausible-looking call that no longer
exists costs more than the lookup does. Pass the version this project actually pins, not the
latest.

Do not use it for anything answerable from this repository: the project's own classes, Gradle
tasks, git, the emulator workflow, or a library this codebase already calls in a way you can read.
Reading the existing call site is faster and it is the version that is really in use.

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
- A surrogate `id` only exists where something reads it. The favourite tables are keyed by the
  MediaStore id itself (`favorite_songs(song_id)`, `favorite_artists(artist_id)`), and
  `playlist_songs` by the pair (`playlist_id`, `song_id`), which is what forbids the duplicate rows
  (the ones `toggle` was written to avoid; the same song registered to one playlist twice) and
  indexes the columns the queries filter on at the same time.
  `shortcuts` keeps its `id` because `ShortcutService` carries it into `ShortcutModel`
- Every non-primary-key column a DAO query filters on carries an `@Index`, and nothing more:
  composite only where every query filters on the whole pair (`shortcuts(item_id, type)`), and
  nothing at all where the primary key's own index already leads with the column the query filters
  on (`playlist_songs(playlist_id, song_id)` serves the queries that name only `playlist_id`)
- The order of a playlist is the order its rows were inserted in. `findByPlaylistId` therefore says
  `ORDER BY rowid` rather than trusting whichever index SQLite picks, and a write that has to keep
  the order (`updatePlaylist`) deletes the playlist's rows and re-inserts them in the new order
- Column names are `snake_case`, set with `@ColumnInfo(name = ...)` where the Kotlin property is
  camelCase. Without it Room names the column after the property, which is how the schema ended up
  mixing snake_case tables with camelCase columns. SQLite identifiers are case-insensitive, so
  camelCase never distinguished anything there in the first place
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
- **Every `ConfigStore` accessor is `suspend` too**, and for the same reason must not be wrapped
  in `withContext(Dispatchers.IO)` — DataStore already does its file access on its own
  dispatcher. `ConfigStore` used to hide `runBlocking` inside every getter and setter, which is
  how blocking reads ended up in `MusicService.onCreate()` and a blocking fsync ended up behind
  the repeat and shuffle buttons
- `withContext(Dispatchers.IO)` belongs in exactly two places: `MediaStoreRepository.get()` /
  `getList()` (plain blocking `ContentResolver.query()`), and `MusicService.decodeAlbumArt()`
  (`ImageDecoder`)
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
  share one key. They pass only the id to the write — never the state they displayed, which may
  have gone stale while the menu was open
- `MusicService` owns a `serviceScope` (cancelled in `onDestroy`) for its `ConfigStore` access and
  the album art decode. It is a `Service`, not a `ViewModel`, so there is no `viewModelScope` to
  lean on
- Services that reconcile Room ids against MediaStore (`FavoriteSongsService`, `ShortcutService`,
  `PlaylistDetailService`, …) delete the ids that resolved to nothing and **return the list they
  already mapped**. Do not re-enter the function after the cleanup: a duplicated id makes an id
  count and a MediaStore row count disagree forever, and the old re-read spun instead of
  converging

## Working agreements

- **Pull requests are written in Japanese** — the title, the commit message and the body alike.
  The body opens with `## 概要`, then `## 理由`, then topic-specific sections, and closes with
  `## 動作確認`. PRs #4 through #12 predate this and stay as they are
- **A title says what the change makes true**, in terms visible from outside the code —
  `編集画面で、存在しない id があっても並び替えを保存できるようにする`, not the name of the
  function that changed and not a `〜を修正` suffix, which carries no information because every
  commit is a change. Never name a symptom that was not actually verified: a title claiming a crash
  the body admits was never reproduced contradicts its own body. What was broken goes in `## 概要`
- Merges are **squash**, and this repository's `squash_merge_commit_title` is `COMMIT_OR_PR_TITLE`,
  so a single-commit PR lands on main under the *commit's* subject rather than the PR title — write
  the subject as if it were the title. Branches are kept after merging, so no `--delete-branch`
- **Never force-push**, in any form, on any branch. `--amend` is only for commits that have not been
  pushed; once a commit is on the remote a correction goes in a follow-up commit, and wording that
  no longer matches gets fixed in the PR body instead
- The bar for `## 動作確認` is unit tests, the instrumented suite with its count, and a **negative
  control** — revert the fix, show the new tests fail, restore it. State plainly what was not
  verified rather than leaving it implied
- **A PR describes its own change and nothing else.** Other bugs noticed on the way, and any request
  for the reviewer to go and check something on a device, belong in the report after the work, not
  in the body — they make the PR bigger than the change it is asking to merge. "This was not
  verified on Android 13" is part of `## 動作確認` and stays; "please confirm it on your device"
  does not
- When reporting that a command passed, keep the result off the command line itself. `./gradlew test
  — pass` gets copy-pasted verbatim and Gradle then fails on `—` as a task name
