package de.luis.bulktracker.domain.calc

import de.luis.bulktracker.domain.model.SetValue

/**
 * Geschätztes Ein-Wiederholungs-Maximum nach Epley: 1RM = Gewicht × (1 + Wdh. / 30).
 *
 * Über [MAX_RELIABLE_REPS] Wiederholungen wird die Formel so ungenau, dass sie mehr
 * verwirrt als hilft – dann gibt es bewusst keinen Wert (null) statt einer Fantasiezahl.
 */
object OneRepMax {
    const val MAX_RELIABLE_REPS = 12

    fun epley(weightKg: Double, reps: Int): Double? = when {
        weightKg <= 0.0 || reps <= 0 -> null
        reps == 1 -> weightKg
        reps > MAX_RELIABLE_REPS -> null
        else -> weightKg * (1.0 + reps / 30.0)
    }

    fun epley(set: SetValue): Double? = epley(set.weightKg, set.reps)

    /** Bester geschätzter 1RM einer Einheit, null wenn kein Satz schätzbar ist. */
    fun best(sets: List<SetValue>): Double? = sets.mapNotNull(::epley).maxOrNull()
}
