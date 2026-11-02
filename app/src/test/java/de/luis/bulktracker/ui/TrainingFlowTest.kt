package de.luis.bulktracker.ui

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextReplacement
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import de.luis.bulktracker.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Rauchtest der echten App (Hilt-Graph, Room-Datei, Navigation, Compose) in Robolectric:
 * Startet die App, loggt ein Workout und prüft, dass das nächste Workout die letzten Werte zeigt
 * und vorbefüllt. Fällt er, startet die App nicht oder der Kern von Phase 1 ist kaputt.
 * Ersetzt keinen Test auf dem Handy (Tastatur, Bedienung, Optik).
 */
@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(application = HiltTestApplication::class)
class TrainingFlowTest {
    @get:Rule(order = 0)
    val hilt = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val compose = createAndroidComposeRule<MainActivity>()

    /** Room arbeitet auf eigenen Threads, die Compose nicht als „beschäftigt“ kennt – deshalb explizit warten. */
    private fun waitFor(matcher: SemanticsMatcher) {
        compose.waitUntil(timeoutMillis = 10_000) {
            compose.onAllNodes(matcher).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun waitForText(text: String, substring: Boolean = false) =
        waitFor(hasText(text, substring = substring))

    /** Klick über die Semantik statt über simulierte Touch-Events – die sind in Robolectric unzuverlässig. */
    private fun SemanticsNodeInteraction.click() = performSemanticsAction(SemanticsActions.OnClick)

    private fun openSquatInNewWorkout() {
        compose.onNodeWithText("Workout starten", substring = true).click()
        waitFor(hasContentDescription("Übung hinzufügen"))
        compose.onNodeWithContentDescription("Übung hinzufügen").click()
        // Die Liste ist lang und der Testbildschirm klein – über die Suche (ohne Umlaut-Genauigkeit) finden.
        waitFor(hasSetTextAction())
        compose.onNode(hasSetTextAction()).performTextReplacement("knie")
        waitForText("Kniebeuge")
        compose.onNodeWithText("Kniebeuge").click()
        waitForText("Letztes Mal", substring = true)
    }

    @Test
    fun workoutLoggenUndLetzteWerteSehen() {
        compose.onNodeWithText("Training").click()
        waitForText("Workout starten", substring = true)
        // Untere Leiste markiert den aktiven Bereich.
        compose.onNode(hasText("Training") and isSelectable()).assertIsSelected()

        // Erstes Workout: noch keine Historie.
        openSquatInNewWorkout()
        compose.onNodeWithText("Letztes Mal: –").assertIsDisplayed()
        compose.onNode(hasSetTextAction() and hasText("kg")).performTextReplacement("102,5")
        compose.onNode(hasSetTextAction() and hasText("Wdh.")).performTextReplacement("5")
        compose.onNodeWithContentDescription("Satz hinzufügen").click()
        waitForText("102,5 kg × 5")
        compose.onNodeWithText("Beenden").click()
        compose.onAllNodesWithText("Beenden")[1].click() // Bestätigungsdialog
        waitForText("Verlauf")
        waitForText("Kniebeuge", substring = true)

        // Zweites Workout: dieselbe Übung zeigt „Letztes Mal“ und füllt die Felder vor.
        openSquatInNewWorkout()
        waitForText("102,5×5", substring = true)
        compose.onNode(hasSetTextAction() and hasText("102,5")).assertExists()
        compose.onNode(hasSetTextAction() and hasText("5")).assertExists()
    }
}
