package app.ocnotes.ui.list

import app.ocnotes.data.local.NoteEntity
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.WeekFields

// A flat, ready-to-render list of section headers and note cards.
sealed interface NoteListEntry {
    data class Header(val label: String) : NoteListEntry
    data class Card(val note: NoteEntity) : NoteListEntry
}

private val MONTHS_SHORT = listOf(
    "ene", "feb", "mar", "abr", "may", "jun",
    "jul", "ago", "sep", "oct", "nov", "dic",
)
private val MONTHS_LONG = listOf(
    "enero", "febrero", "marzo", "abril", "mayo", "junio",
    "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre",
)

// "20 jun", as shown on the card's right edge.
fun formatCardDate(epochSeconds: Long, zone: ZoneId = ZoneId.systemDefault()): String {
    val d = Instant.ofEpochSecond(epochSeconds).atZone(zone).toLocalDate()
    return "${d.dayOfMonth} ${MONTHS_SHORT[d.monthValue - 1]}"
}

private fun monthYear(year: Int, month: Int) = "${MONTHS_LONG[month - 1]} de $year"

// (order, label) for one note's date. Lower order = more recent section.
private fun classify(date: LocalDate, today: LocalDate, wf: WeekFields): Pair<Int, String> {
    if (date == today) return 0 to "Hoy"
    if (date == today.minusDays(1)) return 1 to "Ayer"

    val thisWeekYear = today.get(wf.weekBasedYear())
    val thisWeek = today.get(wf.weekOfWeekBasedYear())
    val dYear = date.get(wf.weekBasedYear())
    val dWeek = date.get(wf.weekOfWeekBasedYear())
    if (dYear == thisWeekYear && dWeek == thisWeek) return 2 to "Esta semana"

    val lastWeek = today.minusWeeks(1)
    if (dYear == lastWeek.get(wf.weekBasedYear()) && dWeek == lastWeek.get(wf.weekOfWeekBasedYear())) {
        return 3 to "La semana pasada"
    }
    if (date.year == today.year && date.month == today.month) return 4 to "Este mes"
    val lastMonth = today.minusMonths(1)
    if (date.year == lastMonth.year && date.month == lastMonth.month) return 5 to "El mes pasado"

    val monthsBetween = (today.year * 12 + today.monthValue) - (date.year * 12 + date.monthValue)
    return (5 + monthsBetween) to monthYear(date.year, date.monthValue)
}

// Favorites first (no header), then the rest grouped chronologically. Groups are
// emitted in order so a section never repeats even when buckets interleave.
fun buildNoteList(
    notes: List<NoteEntity>,
    nowMillis: Long = System.currentTimeMillis(),
    zone: ZoneId = ZoneId.systemDefault(),
): List<NoteListEntry> {
    if (notes.isEmpty()) return emptyList()
    val today = Instant.ofEpochMilli(nowMillis).atZone(zone).toLocalDate()
    val wf = WeekFields.ISO

    val favorites = notes.filter { it.favorite }.sortedByDescending { it.modified }
    val others = notes.filter { !it.favorite }

    val grouped = others.groupBy { note ->
        val date = Instant.ofEpochSecond(note.modified).atZone(zone).toLocalDate()
        classify(date, today, wf)
    }

    val out = mutableListOf<NoteListEntry>()
    favorites.forEach { out.add(NoteListEntry.Card(it)) }
    grouped.entries.sortedBy { it.key.first }.forEach { (key, list) ->
        out.add(NoteListEntry.Header(key.second))
        list.sortedByDescending { it.modified }.forEach { out.add(NoteListEntry.Card(it)) }
    }
    return out
}
