package de.luis.bulktracker.data.db

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import de.luis.bulktracker.domain.model.MuscleGroup

/** Startbestand beim allerersten Öffnen der Datenbank. Läuft genau einmal (onCreate). */
object SeedData {
    val exercises: List<Pair<String, MuscleGroup>> = listOf(
        "Bankdrücken (Langhantel)" to MuscleGroup.BRUST,
        "Schrägbankdrücken (Kurzhantel)" to MuscleGroup.BRUST,
        "Butterfly (Maschine)" to MuscleGroup.BRUST,
        "Dips" to MuscleGroup.BRUST,
        "Klimmzüge" to MuscleGroup.RUECKEN,
        "Latziehen" to MuscleGroup.RUECKEN,
        "Langhantelrudern" to MuscleGroup.RUECKEN,
        "Rudern am Kabel (sitzend)" to MuscleGroup.RUECKEN,
        "Kreuzheben" to MuscleGroup.RUECKEN,
        "Schulterdrücken (Kurzhantel)" to MuscleGroup.SCHULTERN,
        "Seitheben" to MuscleGroup.SCHULTERN,
        "Face Pulls" to MuscleGroup.SCHULTERN,
        "Kniebeuge" to MuscleGroup.BEINE,
        "Beinpresse" to MuscleGroup.BEINE,
        "Rumänisches Kreuzheben" to MuscleGroup.BEINE,
        "Beinstrecker" to MuscleGroup.BEINE,
        "Beinbeuger" to MuscleGroup.BEINE,
        "Ausfallschritte" to MuscleGroup.BEINE,
        "Hip Thrust" to MuscleGroup.PO,
        "Wadenheben" to MuscleGroup.WADEN,
        "Bizepscurls (Kurzhantel)" to MuscleGroup.BIZEPS,
        "Hammercurls" to MuscleGroup.BIZEPS,
        "Trizepsdrücken am Kabel" to MuscleGroup.TRIZEPS,
        "French Press" to MuscleGroup.TRIZEPS,
        "Crunches am Kabel" to MuscleGroup.BAUCH,
        "Beinheben (hängend)" to MuscleGroup.BAUCH,
    )

    val callback = object : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            exercises.forEach { (name, group) ->
                db.execSQL(
                    "INSERT INTO exercise (name, muscleGroup, lastUsedAt, archived) VALUES (?, ?, NULL, 0)",
                    arrayOf<Any>(name, group.name),
                )
            }
        }
    }
}
