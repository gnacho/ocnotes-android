# Changelog

All notable changes to ocnotes-android are documented here.
The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/).

## [0.1.10] - 2026-11-08

### Added

- Note interior: in-note search with highlighting, a view/edit toggle, favorite,
  rename and an overflow menu (rename, category, share, move, cancel, delete,
  pin to home). Markdown rendered with an editable styled field.
- Side drawer with all notes / favorites / uncategorized / every category and
  their counts, plus settings and about.
- Settings screen: Wi-Fi-only and background sync, theme, direct editing,
  monospace, font size, keep screen on and prevent screenshots, wired to
  WorkManager constraints, the editor and FLAG_SECURE.
- Pin-to-home shortcut for a note.

## [0.1.8] - 2026-11-08

### Changed

- Redesigned the notes list to match the reference: a pill search bar with the
  user avatar, note cards (star, title + date, preview + category chip), date
  section headers (Hoy / Ayer / Esta semana / La semana pasada / Este mes / El
  mes pasado / <mes> de <año>), favorites pinned first, pull-to-refresh and a
  square blue FAB.
- Dark-first Material 3 palette sampled from the reference, with a coherent
  light variant; the editor, login and settings adopt it too.
- Functional client-side search (title, content, category).

## [0.1.6] - 2026-11-08

### Added

- Live markdown styling in the note editor: headings `#`..`######` at increasing
  sizes and bold, `**bold**`/`*italic*`/`***both***`, `~~strike~~`, inline code
  (monospace + background) and links (accent + underline) are rendered in place
  while writing. Implemented with a pure parser and a VisualTransformation
  (identity offset mapping), no new dependency. Lists and images stay as text.

## [0.1.4] - 2026-11-08

### Fixed

- Reopening a saved note closed the editor immediately: the editor ViewModel was
  reused across opens (fixed key) and kept its `closed` flag. Each editor session
  now gets a fresh ViewModel. This also fixes a second "new note" inheriting the
  previous note's content.
- A failed sync (offline, 4xx/5xx) crashed the app because the exception was
  thrown from a coroutine without a handler. Sync failures now keep the local
  cache and surface a "Sync failed" hint. Sync runs on open, so opening the app
  offline used to crash.

### Added

- Adaptive launcher icon.

## [0.1.2] - 2026-11-08

First functional release of the native Android client for ocnotes.

### Added

- Login with server URL + username + OpenCloud app token (Basic auth), validated
  against `GET /settings`.
- Full Notes API v1.4 sync: initial pull, incremental pull with a `pruneBefore`
  watermark (full notes + `{id}` stubs), server-side deletion reconciliation and
  optimistic edit/delete/favorite push with `If-Match` etags.
- Note list with favorites, categories and relative time; All / Favorites scopes.
- Markdown note editor: create, edit, category, favorite and delete.
- Settings: theme (system/light/dark) and sync interval (WorkManager).
- Offline-first local storage (Room) and periodic background sync.
- 29 unit tests covering the sync engine, repository, title derivation, time
  formatting and ViewModels.
