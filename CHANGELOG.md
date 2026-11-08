# Changelog

All notable changes to ocnotes-android are documented here.
The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/).

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
