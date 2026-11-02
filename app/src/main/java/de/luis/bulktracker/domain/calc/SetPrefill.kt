package de.luis.bulktracker.domain.calc

import de.luis.bulktracker.domain.model.SetValue

/**
 * Welche Werte stehen in den Eingabefeldern für den nächsten Satz?
 *
 * 1. Gab es letztes Mal einen Satz an dieser Position → genau den (Ziel: mindestens gleich viel).
 * 2. Sonst der zuletzt heute geloggte Satz (typisch: „noch einer wie eben“).
 * 3. Sonst der letzte Satz der vorigen Einheit.
 * 4. Sonst nichts – neue Übung, leere Felder.
 */
object SetPrefill {
    fun suggest(previous: List<SetValue>, loggedToday: List<SetValue>): SetValue? =
        previous.getOrNull(loggedToday.size)
            ?: loggedToday.lastOrNull()
            ?: previous.lastOrNull()
}
