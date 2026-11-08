# Changelog

All notable changes to ocnotes-android are documented here.
The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/).

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
