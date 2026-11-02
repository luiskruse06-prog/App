package de.luis.bulktracker.domain.calc

import com.google.common.truth.Truth.assertThat
import de.luis.bulktracker.domain.model.SetValue
import org.junit.Test

class SetPrefillTest {
    private val previous = listOf(SetValue(80.0, 8), SetValue(80.0, 7), SetValue(75.0, 9))

    @Test
    fun `erster Satz uebernimmt den ersten Satz der letzten Einheit`() {
        assertThat(SetPrefill.suggest(previous, emptyList())).isEqualTo(SetValue(80.0, 8))
    }

    @Test
    fun `n-ter Satz uebernimmt den n-ten Satz der letzten Einheit`() {
        val today = listOf(SetValue(82.5, 8), SetValue(82.5, 7))
        assertThat(SetPrefill.suggest(previous, today)).isEqualTo(SetValue(75.0, 9))
    }

    @Test
    fun `mehr Saetze als letztes Mal - letzter heutiger Satz wird wiederholt`() {
        val today = listOf(SetValue(80.0, 8), SetValue(80.0, 8), SetValue(80.0, 8), SetValue(85.0, 5))
        assertThat(SetPrefill.suggest(previous, today)).isEqualTo(SetValue(85.0, 5))
    }

    @Test
    fun `neue Uebung ohne Historie - erst leer, dann letzter heutiger Satz`() {
        assertThat(SetPrefill.suggest(emptyList(), emptyList())).isNull()
        assertThat(SetPrefill.suggest(emptyList(), listOf(SetValue(20.0, 12)))).isEqualTo(SetValue(20.0, 12))
    }
}
