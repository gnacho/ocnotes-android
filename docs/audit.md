# ocnotes-android — parity audit

Reference: `nextcloud/notes-android` (Java, AGPL-3.0). It is used only as a
functional/UX reference. This client is an independent Kotlin implementation
against the Notes API v1.4, with no copied code or structure.

## Feature parity

| Feature | nextcloud/notes-android | ocnotes-android (MVP) |
|---|---|---|
| Login (server + user + app password) | yes | yes (Basic auth, validated via `GET /settings`) |
| Note list, favorites first | yes | yes (favorites first, then modified) |
| Favorites scope | yes | yes (All / Favorites drawer) |
| Categories | yes (sidebar) | category shown per note; editable in the editor |
| Markdown editor | yes | yes (plain Markdown text field) |
| Markdown preview | yes | no (future) |
| Create / edit / delete note | yes | yes (create online, edit/delete optimistic) |
| Favorite toggle | yes | yes (optimistic, pushed on sync) |
| Attachments (v1.4) | yes | no (future) |
| Search | yes | no (future) |
| Trash / soft delete | yes | no (server delete) |
| Offline cache | yes (Room) | yes (Room) |
| Incremental sync | yes (ETag/Last-Modified) | yes (`pruneBefore` watermark) |
| Theme (system/light/dark) | yes | yes |
| Background sync | Android sync adapter | WorkManager periodic |

## Deliberate differences

- **Own sync protocol** instead of the Android sync adapter: `pruneBefore` +
  full/stub reconciliation, with an explicit outbound queue (`dirty` / `deleted`
  flags). Simpler to test in the JVM and independent of the platform account
  plumbing.
- **Plain Markdown editing** in the MVP: no rich preview, no WYSIWYG. The stored
  content is the user's Markdown verbatim, preserving the API contract with
  ocnotes / Nextcloud / Iotas.
- **No attachments yet.** The v1.4 `/attachment/{noteId}` multipart endpoints are
  documented in `docs/mvp-spec.md` but not wired in the MVP.

## Notes API gotchas captured

- `modified` is in **seconds**; format with `epochSeconds * 1000`.
- `pruneBefore` responses mix full notes and `{id}` stubs: never treat a stub as
  "content empty", and never delete a local note just because it came back as a
  stub.
- `If-Match` etag: send it quoted (`"etag"`). The ocnotes backend strips quotes;
  upstream Nextcloud expects them.
- Creating a note does not accept `favorite`; favorite is only sent on `PUT`.
- The list title is derived from the first content line; the client mirrors that
  so titles stay consistent with other clients.
