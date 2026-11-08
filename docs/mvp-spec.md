# ocnotes-android — MVP spec

Native Android client for ocnotes. Kotlin + Jetpack Compose + MVVM, talking to
the Nextcloud Notes REST API v1.4 (the contract ocnotes implements, a drop-in of
the upstream Nextcloud Notes app).

## Scope

MVP:

- Login with server URL + username + app token.
- Sync: initial pull, incremental pull, optimistic edit/delete/favorite push.
- Note list (All / Favorites), categories, relative time.
- Note editor: create, edit (markdown), category, favorite, delete.
- Settings: theme (system/light/dark) and sync interval.

Out of scope for the MVP (future work):

- Attachments (API v1.4 `/attachment/{noteId}`).
- Markdown preview / rich rendering.
- Search, per-category scopes, trash.
- Encryption / E2EE vault.

## Notes API v1.4 contract (verified against the ocnotes backend)

Base: `/index.php/apps/notes/api/v1/`.

Note JSON:

```json
{
  "id": 1,
  "etag": "hex",
  "readonly": false,
  "modified": 1700000000,
  "title": "First line of content",
  "category": "",
  "content": "markdown body",
  "favorite": false
}
```

- `modified` is **Unix seconds** (not milliseconds). All time formatting multiplies
  by 1000.

Endpoints:

| Method | Path | Notes |
|---|---|---|
| GET | `/settings` | Authenticated ping used at login. Returns `{notesPath, fileSuffix}`. |
| PUT | `/settings` | Updates settings (modeled, not surfaced in the MVP UI). |
| GET | `/notes` | List all notes. Optional `pruneBefore=<seconds>`. |
| POST | `/notes` | Body `{title, content, category}`. `favorite` is not settable on create. |
| GET | `/notes/{id}` | Single note, `ETag` header. |
| PUT | `/notes/{id}` | Body `{title?, content?, category?, favorite?}`, `If-Match` etag → 412 on mismatch. |
| DELETE | `/notes/{id}` | 200 `{}`. |

`pruneBefore` semantics (incremental sync): the server returns **every** note id,
either as a full object (`modified >= pruneBefore`) or as a `{id}` stub
(`modified < pruneBefore`). Notes deleted server-side are simply absent, so the
client reconciles: local ids not present in the response were deleted remotely.

## Room model

`note` table (`NoteEntity`): `id` (PK), `etag`, `readonly`, `modified`, `title`,
`category`, `content`, `favorite`, plus two outbound-queue flags:

- `dirty`: the local edit differs from the server (needs `PUT`).
- `deleted`: tombstone (needs `DELETE`), hidden from the list.

## Sync engine (`SyncEngine`)

`sync()` = `pushPending()` then pull.

1. `pushPending()`:
   - Pending deletes → `DELETE /notes/{id}` (404 tolerated); remove the tombstone.
   - Pending updates → `PUT /notes/{id}` with `If-Match`; store the server copy and
     clear `dirty`.
2. Pull:
   - Empty DB → `initialSync()` (plain `GET /notes`).
   - Otherwise `incrementalSync()`: watermark = `MAX(modified)`; `GET
     /notes?pruneBefore=watermark`; upsert full notes; delete local ids missing
     from the response.

The list title is derived from the first non-empty line of the content (like the
server does), so the server-side title stays meaningful.

## Testing

- JVM unit tests with fakes (`FakeNoteDao`, `FakeNotesApi`, `FakeSessionStore`).
- E2E against a local mock of the Notes API and against a real ocnotes/Nextcloud
  instance (`adb reverse tcp:PORT tcp:PORT` + `http://127.0.0.1:PORT`).
