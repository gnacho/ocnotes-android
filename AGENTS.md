# AGENTS.md — ocnotes-android

Cliente Android nativo de [ocnotes](https://github.com/gnacho/ocnotes) (notas para OpenCloud). Kotlin + Jetpack Compose + MVVM, sin fork del cliente Nextcloud: implementación propia contra la Notes REST API v1.4.

## Qué es
- App Android que consume la Notes API v1.4 que sirve el backend de ocnotes.
- Referencia funcional: `nextcloud/notes-android` (AGPL-3.0, Java). NO se copia código ni estructura: se usa solo como referencia de funcionalidad y UX.

## Estructura
- `docs/audit.md` — paridad de funcionalidades + contrato de la Notes API v1.4.
- `docs/mvp-spec.md` — especificación del MVP (alcance, modelo Room, motor de sync).
- `app/` — módulo Android (`app.ocnotes`).

## Cómo se trabaja
- Kotlin idiomático, Compose, MVVM (UI + ViewModel + Repository).
- Room + DataStore, Retrofit/OkHttp + kotlinx.serialization, Coroutines/Flow, WorkManager.
- Commits conventional (`feat:`, `fix:`, `refactor:`, `docs:`), ramas `<tipo>/<desc>`.
- Español en la comunicación, inglés en código y docs del repo.
