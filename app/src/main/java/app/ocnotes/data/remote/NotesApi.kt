package app.ocnotes.data.remote

import app.ocnotes.data.remote.dto.CreateNoteRequest
import app.ocnotes.data.remote.dto.NoteDto
import app.ocnotes.data.remote.dto.SettingsResponse
import app.ocnotes.data.remote.dto.UpdateNoteRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

// Nextcloud Notes REST API v1.4. Base: /index.php/apps/notes/api/v1.
interface NotesApi {
    @GET("settings")
    suspend fun settings(): SettingsResponse

    @PUT("settings")
    suspend fun updateSettings(@Body body: SettingsResponse): SettingsResponse

    @GET("notes")
    suspend fun notes(@Query("pruneBefore") pruneBefore: Long? = null): List<NoteDto>

    @POST("notes")
    suspend fun createNote(@Body body: CreateNoteRequest): NoteDto

    @GET("notes/{id}")
    suspend fun note(@Path("id") id: Long): NoteDto

    @PUT("notes/{id}")
    suspend fun updateNote(
        @Path("id") id: Long,
        @Body body: UpdateNoteRequest,
        @Header("If-Match") etag: String,
    ): NoteDto

    @DELETE("notes/{id}")
    suspend fun deleteNote(
        @Path("id") id: Long,
        @Header("If-Match") etag: String,
    )
}
