package de.luis.bulktracker.data.db

import androidx.room.TypeConverter
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

/**
 * Zeitpunkte als Epoch-Millis (UTC), Kalendertage als Epoch-Day, Uhrzeiten als Sekunde des Tages.
 * Alle drei sind sortierbar, sodass ORDER BY und Bereichsabfragen direkt in SQL funktionieren.
 */
class Converters {
    @TypeConverter fun instantToLong(value: Instant?): Long? = value?.toEpochMilli()
    @TypeConverter fun longToInstant(value: Long?): Instant? = value?.let(Instant::ofEpochMilli)

    @TypeConverter fun dateToLong(value: LocalDate?): Long? = value?.toEpochDay()
    @TypeConverter fun longToDate(value: Long?): LocalDate? = value?.let(LocalDate::ofEpochDay)

    @TypeConverter fun timeToInt(value: LocalTime?): Int? = value?.toSecondOfDay()
    @TypeConverter fun intToTime(value: Int?): LocalTime? = value?.let { LocalTime.ofSecondOfDay(it.toLong()) }
}
