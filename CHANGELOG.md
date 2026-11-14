# Changelog

All notable changes to ocnotes-android are documented here.
The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/).

## [Unreleased]

### Added

- The app token can be replaced from Settings when it expires or is revoked, keeping the notes that only exist on the device. The screen is prefilled with the account and only asks for the new token.

### Changed

- A 401 during sync or a pending push is now a "session expired" state with a sign-in action, instead of a raw `Sync failed: HTTP 401` banner.
- Signing out with unsynced edits asks for confirmation and states how many notes will be lost.

## [0.1.34] - 2026-11-10

### Changed

- New icon: flat #20434F background with the notepad glyph in #E2BAFF, matching ocnews and ocphotos.

## [0.1.32] - 2026-11-10

### Changed

- The side drawer is now half the screen wide on a phone, narrower on wide screens and never wider than 320 dp, instead of a fixed 80% on every device.

## [0.1.30] - 2026-11-09

### Changed

- Settings now slides in and out like the rest of the screens, instead of appearing at once.
- The navigation animation is a plain 300 ms slide, matching ocnews: no more cross fade.

## [0.1.28] - 2026-11-09

### Changed

- The editor defaults to live markdown styling (bold, italics and headings render while editing) instead of plain text.
- Swipe transition between the note editor and the list.

## [0.1.26] - 2026-11-09

### Changed

- New launcher glyph: lucide "notepad-text" (notepad with three rings and three lines) in white, replacing the rounded page with orange lines. It matches the icon family used by ocnews and ocphotos.

## [0.1.24] - 2026-11-09

### Changed

- New app icon background: the OpenCloud radial gradient (lilac centre to dark teal edge) replaces the flat orange, matching ocnews. The gradient radius is tuned so the dark rim stays inside the launcher mask.

## [0.1.22] - 2026-11-09

### Fixed

- The side drawer now animates its collapse (scrim fade + sheet slide) when
  closed by tapping outside, pressing back, or tapping the drawer's own title.

## [0.1.20] - 2026-11-09

### Fixed

- A multi-line YouTube/HTML embed showed the raw `<a>`/`</a>` tags as text; the
  note view now strips them and renders only the thumbnail (image + clickable
  link).
- Tapping outside the side drawer did not close it (the built-in scrim is
  disabled when gestures are off). The drawer is now a custom sheet with its own
  scrim, so an outside tap closes it.

## [0.1.18] - 2026-11-09

### Fixed

- Note edits (category, title, content) were not saved: the redesigned editor
  had no save action. They now persist when confirming the category/rename
  dialogs, when leaving with Back, and via an explicit "Guardar" item.
- The side drawer opens at ~80% width, and Back closes the open drawer instead of
  leaving the app.

### Changed

- The list preview shows plain text (markdown and HTML stripped).
- The note view renders raw-HTML `<img>` as a real image (Coil + OkHttp) and
  makes a wrapping `<a href>` clickable, so a pasted YouTube embed previews with
  its thumbnail.

## [0.1.16] - 2026-11-09

### Fixed

- The back gesture (and button) from a note or Settings closed the app instead
  of returning to the list on Android 16 predictive back. The manifest now opts
  in with `android:enableOnBackInvokedCallback="true"`.

## [0.1.14] - 2026-11-08

### Fixed

- Off switches in Settings were hard to read in dark mode (dark thumb on a dark
  track). They now use a visible thumb and border.

## [0.1.12] - 2026-11-08

### Added

- Grid view (2 columns) toggle for the notes list.
- Swipe actions: swipe a card right to favorite and left to delete.

### Changed

- The "note opening mode" setting (plain text vs styled editor) is now wired to
  the editor rendering.

### Fixed

- The system Back button closed the app from Settings or the editor; it now
  returns to the list.

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
