package app.ocnotes.ui.list

import app.ocnotes.testutil.note
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NoteListModelTest {

    private val zone = ZoneId.of("UTC")

    @Test
    fun `plainPreview strips markdown and html`() {
        assertEquals("Exterior Herrero: Marquesina", plainPreview("## Exterior\nHerrero:\n- Marquesina"))
        assertEquals("negrita cursiva", plainPreview("**negrita** *cursiva*"))
        assertEquals("link code", plainPreview("[link](http://u) `code`"))
        assertEquals("", plainPreview("<a href=\"x\"><img src=\"y\"></a>"))
    }

    private fun secs(y: Int, m: Int, d: Int): Long =
        LocalDate.of(y, m, d).atTime(12, 0).atZone(zone).toEpochSecond()

    @Test
    fun `card date is day and short month`() {
        assertEquals("20 jun", formatCardDate(secs(2026, 6, 20), zone))
        assertEquals("24 abr", formatCardDate(secs(2026, 4, 24), zone))
        assertEquals("14 dic", formatCardDate(secs(2026, 12, 14), zone))
    }

    @Test
    fun `empty list yields no entries`() {
        assertTrue(buildNoteList(emptyList(), nowMillis = secs(2026, 11, 8) * 1000, zone = zone).isEmpty())
    }

    @Test
    fun `favorites first then chronological sections`() {
        val now = secs(2026, 11, 8) // Sunday
        val fav = note(1, modified = secs(2026, 6, 20), favorite = true)
        val hoy = note(2, modified = secs(2026, 11, 8))
        val ayer = note(3, modified = secs(2026, 11, 7))
        val estaSemana = note(4, modified = secs(2026, 11, 4))
        val semanaPasada = note(5, modified = secs(2026, 10, 30))
        val mesPasado = note(6, modified = secs(2026, 10, 14))
        val septiembre = note(7, modified = secs(2026, 9, 20))

        val entries = buildNoteList(
            listOf(hoy, ayer, estaSemana, semanaPasada, mesPasado, septiembre, fav),
            nowMillis = now * 1000,
            zone = zone,
        )

        assertEquals(
            listOf(
                NoteListEntry.Card(fav),
                NoteListEntry.Header("Hoy"), NoteListEntry.Card(hoy),
                NoteListEntry.Header("Ayer"), NoteListEntry.Card(ayer),
                NoteListEntry.Header("Esta semana"), NoteListEntry.Card(estaSemana),
                NoteListEntry.Header("La semana pasada"), NoteListEntry.Card(semanaPasada),
                NoteListEntry.Header("El mes pasado"), NoteListEntry.Card(mesPasado),
                NoteListEntry.Header("septiembre de 2026"), NoteListEntry.Card(septiembre),
            ),
            entries,
        )
    }

    @Test
    fun `favorites are not repeated in date sections`() {
        val now = secs(2026, 11, 8)
        val favToday = note(1, modified = secs(2026, 11, 8), favorite = true)
        val entries = buildNoteList(listOf(favToday), nowMillis = now * 1000, zone = zone)
        assertEquals(listOf(NoteListEntry.Card(favToday)), entries)
    }
}
