package de.luis.bulktracker.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import de.luis.bulktracker.domain.model.SleepSource
import java.time.Instant
import java.time.LocalDate

/**
 * Eine Schlafsession. [nightOf] ist das Datum des AUFWACHENS – so gehört die Nacht
 * von Montag auf Dienstag eindeutig zum Dienstag, auch wenn man vor Mitternacht ins Bett geht.
 *
 * Manuelle und automatische Einträge liegen nebeneinander; beim Lesen gewinnt MANUAL
 * für denselben [nightOf]. So wird nie etwas gelöscht, und ein erneuter Health-Connect-
 * Abgleich überschreibt keine Korrektur.
 */
@Entity(
    tableName = "sleep_entry",
    indices = [Index("nightOf"), Index(value = ["hcRecordId"], unique = true)],
)
data class SleepEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nightOf: LocalDate,
    val bedtime: Instant,
    val wakeTime: Instant,
    val source: SleepSource,
    val quality: Int? = null,
    val note: String? = null,
    val hcRecordId: String? = null,
)

/** Ein Wert pro Tag (morgens gewogen); ein zweiter Eintrag am selben Tag ersetzt den ersten. */
@Entity(tableName = "body_weight")
data class BodyWeightEntity(
    @PrimaryKey val date: LocalDate,
    val kg: Double,
)
