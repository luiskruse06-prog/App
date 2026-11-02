package de.luis.bulktracker.domain.calc

import com.google.common.truth.Truth.assertThat
import de.luis.bulktracker.domain.model.SetValue
import org.junit.Test

class OneRepMaxTest {
    @Test
    fun `Epley-Formel - 100 kg x 10 ergibt 133,3 kg`() {
        assertThat(OneRepMax.epley(100.0, 10)).isWithin(0.01).of(133.33)
    }

    @Test
    fun `ein einzelnes Wiederholung ist das 1RM selbst`() {
        // Epley würde 1 Wdh. zu Gewicht × 1,033 aufblasen – ein echtes Single ist aber exakt das Maximum.
        assertThat(OneRepMax.epley(140.0, 1)).isEqualTo(140.0)
    }

    @Test
    fun `ueber 12 Wiederholungen gibt es keine Schaetzung`() {
        assertThat(OneRepMax.epley(60.0, 12)).isNotNull()
        assertThat(OneRepMax.epley(60.0, 13)).isNull()
    }

    @Test
    fun `Koerpergewicht-Saetze und ungueltige Werte ergeben null`() {
        assertThat(OneRepMax.epley(0.0, 8)).isNull()
        assertThat(OneRepMax.epley(80.0, 0)).isNull()
    }

    @Test
    fun `best nimmt den hoechsten geschaetzten Wert und ignoriert nicht schaetzbare Saetze`() {
        val sets = listOf(SetValue(100.0, 5), SetValue(90.0, 10), SetValue(50.0, 20))
        // 100×5 → 116,67; 90×10 → 120; 50×20 → nicht schätzbar
        assertThat(OneRepMax.best(sets)).isWithin(0.01).of(120.0)
        assertThat(OneRepMax.best(listOf(SetValue(0.0, 10)))).isNull()
    }
}
