# ocnotes-android

![version](https://img.shields.io/badge/version-0.1.6-blue)

Native Android client for [ocnotes](https://github.com/gnacho/ocnotes), the notes app for OpenCloud.

Kotlin + Jetpack Compose + MVVM. It talks to the [Nextcloud Notes REST API v1.4](https://nextcloud.github.io/notes/) that ocnotes implements, so it works against ocnotes and any Nextcloud Notes server.

## Features

- Log in with your server URL, username and app token.
- Offline-first sync of notes with incremental updates (`pruneBefore` watermark)
  and a local store, plus optimistic edits pushed back to the server.
- Note list with favorites, categories and relative time; All / Favorites scopes.
- Markdown note editor: create, edit, favorite and delete notes.
- Settings: theme (system/light/dark) and sync interval.
- Background periodic sync via WorkManager.

## Build

Requires JDK 17 and the Android SDK (compileSdk 37).

```
./gradlew :app:assembleDebug
./gradlew :app:assembleRelease
```

## License

AGPL-3.0. See [LICENSE](LICENSE).
