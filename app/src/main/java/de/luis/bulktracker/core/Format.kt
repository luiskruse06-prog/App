package de.luis.bulktracker.core

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.Normalizer
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Deutsche Anzeige: Dezimalkomma, 24-h-Uhr, dd.MM.yyyy. */
object Format {
    private val locale: Locale = Locale.GERMANY

    val time: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm", locale)
    val date: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy", locale)
    val shortDate: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.", locale)
    val weekdayDate: DateTimeFormatter = DateTimeFormatter.ofPattern("EE, dd.MM.yyyy", locale)

    fun time(instant: Instant, zone: ZoneId = ZoneId.systemDefault()): String = time.format(instant.atZone(zone))
    fun date(instant: Instant, zone: ZoneId = ZoneId.systemDefault()): String = date.format(instant.atZone(zone))
    fun shortDate(instant: Instant, zone: ZoneId = ZoneId.systemDefault()): String = shortDate.format(instant.atZone(zone))
    fun weekdayDate(instant: Instant, zone: ZoneId = ZoneId.systemDefault()): String = weekdayDate.format(instant.atZone(zone))

    /** 80.0 → „80“, 82.5 → „82,5“, 22.25 → „22,25“ – höchstens zwei Nachkommastellen. */
    fun number(value: Double): String =
        BigDecimal.valueOf(value)
            .setScale(2, RoundingMode.HALF_UP)
            .stripTrailingZeros()
            .toPlainString()
            .replace('.', ',')

    fun set(weightKg: Double, reps: Int): String = "${number(weightKg)} kg × $reps"

    /** „45 min“ bzw. „1 h 05 min“. Negative Dauern (Uhr verstellt) werden als 0 angezeigt. */
    fun duration(duration: Duration): String {
        val totalMinutes = duration.toMinutes().coerceAtLeast(0)
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return if (hours == 0L) "$minutes min" else "$hours h ${minutes.toString().padStart(2, '0')} min"
    }
}

/** Eingaben aus Textfeldern – tolerant gegenüber Komma/Punkt und Leerzeichen. */
object Parse {
    /** Gewicht in kg: 0 (Körpergewicht) bis 1000, Komma oder Punkt. */
    fun weightKg(text: String): Double? {
        val value = text.trim().replace(',', '.').toDoubleOrNull() ?: return null
        return value.takeIf { it.isFinite() && it >= 0.0 && it <= 1000.0 }
    }

    fun reps(text: String): Int? = text.trim().toIntOrNull()?.takeIf { it in 1..999 }
}

/** Suche, die Umlaute und Groß-/Kleinschreibung ignoriert: „ruecken“, „Rücken“ und „rucken“ finden dasselbe. */
object Search {
    fun normalize(text: String): String {
        val lower = text.lowercase(Locale.GERMANY)
            .replace("ä", "a").replace("ö", "o").replace("ü", "u")
            .replace("ae", "a").replace("oe", "o").replace("ue", "u")
            .replace("ß", "ss")
        return Normalizer.normalize(lower, Normalizer.Form.NFD)
            .replace(Regex("\\p{M}"), "")
            .trim()
    }

    fun matches(candidate: String, query: String): Boolean {
        val q = normalize(query)
        if (q.isEmpty()) return true
        val c = normalize(candidate)
        return q.split(' ').filter { it.isNotEmpty() }.all { c.contains(it) }
    }
}
