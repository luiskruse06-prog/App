package de.luis.bulktracker.core

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.Duration
import java.time.Instant
import java.time.ZoneId

class FormatTest {
    @Test
    fun `Zahlen mit Dezimalkomma und ohne ueberfluessige Nullen`() {
        assertThat(Format.number(80.0)).isEqualTo("80")
        assertThat(Format.number(82.5)).isEqualTo("82,5")
        assertThat(Format.number(22.25)).isEqualTo("22,25")
        assertThat(Format.number(1.005)).isEqualTo("1,01")
        assertThat(Format.set(82.5, 8)).isEqualTo("82,5 kg × 8")
    }

    @Test
    fun `Dauer in Minuten bzw Stunden und Minuten`() {
        assertThat(Format.duration(Duration.ofMinutes(45))).isEqualTo("45 min")
        assertThat(Format.duration(Duration.ofMinutes(65))).isEqualTo("1 h 05 min")
        assertThat(Format.duration(Duration.ofMinutes(-3))).isEqualTo("0 min")
    }

    @Test
    fun `Uhrzeit im 24-Stunden-Format`() {
        val instant = Instant.parse("2026-11-02T21:05:00Z")
        assertThat(Format.time(instant, ZoneId.of("Europe/Berlin"))).isEqualTo("22:05")
        assertThat(Format.date(instant, ZoneId.of("Europe/Berlin"))).isEqualTo("02.11.2026")
    }

    @Test
    fun `Gewicht akzeptiert Komma und Punkt, lehnt Unsinn ab`() {
        assertThat(Parse.weightKg("82,5")).isEqualTo(82.5)
        assertThat(Parse.weightKg(" 82.5 ")).isEqualTo(82.5)
        assertThat(Parse.weightKg("0")).isEqualTo(0.0)
        assertThat(Parse.weightKg("-5")).isNull()
        assertThat(Parse.weightKg("abc")).isNull()
        assertThat(Parse.weightKg("")).isNull()
        assertThat(Parse.weightKg("NaN")).isNull()
    }

    @Test
    fun `Wiederholungen nur als positive ganze Zahl`() {
        assertThat(Parse.reps("8")).isEqualTo(8)
        assertThat(Parse.reps("0")).isNull()
        assertThat(Parse.reps("7,5")).isNull()
    }

    @Test
    fun `Suche ignoriert Umlaute, Gross- und Kleinschreibung und Wortreihenfolge`() {
        assertThat(Search.matches("Rücken", "ruecken")).isTrue()
        assertThat(Search.matches("Rücken", "RUCKEN")).isTrue()
        assertThat(Search.matches("Bankdrücken (Langhantel)", "lang bank")).isTrue()
        assertThat(Search.matches("Kniebeuge", "bank")).isFalse()
        assertThat(Search.matches("Kniebeuge", "  ")).isTrue()
    }
}
