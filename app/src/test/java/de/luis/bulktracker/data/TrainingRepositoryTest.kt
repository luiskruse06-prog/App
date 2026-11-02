package de.luis.bulktracker.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import de.luis.bulktracker.data.db.AppDatabase
import de.luis.bulktracker.data.db.SeedData
import de.luis.bulktracker.data.repo.TrainingRepositoryImpl
import de.luis.bulktracker.domain.model.MuscleGroup
import de.luis.bulktracker.domain.model.SetValue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId

/** Stellbare Uhr, damit Workouts in definierter Reihenfolge entstehen. */
private class TestClock(var now: Instant) : Clock() {
    override fun getZone(): ZoneId = ZoneId.of("Europe/Berlin")
    override fun withZone(zone: ZoneId?): Clock = this
    override fun instant(): Instant = now
    fun advance(duration: Duration) {
        now = now.plus(duration)
    }
}

@RunWith(AndroidJUnit4::class)
class TrainingRepositoryTest {
    private lateinit var db: AppDatabase
    private lateinit var clock: TestClock
    private lateinit var repo: TrainingRepositoryImpl

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AppDatabase::class.java)
            .addCallback(SeedData.callback)
            .allowMainThreadQueries()
            .build()
        clock = TestClock(Instant.parse("2026-10-01T16:00:00Z"))
        repo = TrainingRepositoryImpl(db, clock)
    }

    @After
    fun tearDown() = db.close()

    private suspend fun exerciseId(name: String) =
        repo.observeExercises().first().first { it.name == name }.id

    /** Ein komplettes Workout mit einer Übung und den angegebenen Sätzen, danach beendet. */
    private suspend fun logWorkout(exerciseId: Long, vararg sets: SetValue): Long {
        val workoutId = repo.startOrResumeWorkout()
        val we = repo.addExerciseToWorkout(workoutId, exerciseId)
        sets.forEach { repo.addSet(we, it) }
        clock.advance(Duration.ofMinutes(60))
        repo.finishWorkout(workoutId)
        clock.advance(Duration.ofDays(2))
        return workoutId
    }

    @Test
    fun seedLegtUebungenAn() = runBlocking<Unit> {
        assertThat(repo.observeExercises().first().map { it.name })
            .containsAtLeast("Bankdrücken (Langhantel)", "Kniebeuge", "Klimmzüge")
    }

    @Test
    fun zuletztGenutzteUebungStehtOben() = runBlocking<Unit> {
        val squat = exerciseId("Kniebeuge")
        logWorkout(squat, SetValue(100.0, 5))
        assertThat(repo.observeExercises().first().first().name).isEqualTo("Kniebeuge")
    }

    @Test
    fun uebungAnlegenIstUnabhaengigVonGrossKleinschreibung() = runBlocking<Unit> {
        val a = repo.createExercise("  Kabelzug   Crossover ", MuscleGroup.BRUST)
        val b = repo.createExercise("kabelzug crossover", MuscleGroup.SONSTIGES)
        assertThat(b).isEqualTo(a)
        assertThat(repo.observeExercises().first().count { it.id == a }).isEqualTo(1)
        assertThat(repo.observeExercises().first().first { it.id == a }.name).isEqualTo("Kabelzug Crossover")
    }

    @Test
    fun esGibtNieZweiLaufendeWorkouts() = runBlocking<Unit> {
        val first = repo.startOrResumeWorkout()
        clock.advance(Duration.ofMinutes(5))
        assertThat(repo.startOrResumeWorkout()).isEqualTo(first)
    }

    @Test
    fun workoutOhneSaetzeWirdBeimBeendenVerworfen() = runBlocking<Unit> {
        val id = repo.startOrResumeWorkout()
        repo.addExerciseToWorkout(id, exerciseId("Kniebeuge"))
        assertThat(repo.finishWorkout(id)).isFalse()
        assertThat(repo.observeWorkout(id).first()).isNull()
        assertThat(repo.observeActiveWorkout().first()).isNull()
    }

    @Test
    fun letzteWerteKommenAusDerJuengstenEinheitMitSaetzen() = runBlocking<Unit> {
        val bench = exerciseId("Bankdrücken (Langhantel)")
        logWorkout(bench, SetValue(80.0, 8), SetValue(80.0, 7))
        logWorkout(bench, SetValue(82.5, 8), SetValue(82.5, 6), SetValue(80.0, 8))

        // Dazwischen ein Workout, in dem Bankdrücken nur hinzugefügt, aber nie geloggt wurde.
        val empty = repo.startOrResumeWorkout()
        repo.addExerciseToWorkout(empty, bench)
        repo.addSet(repo.addExerciseToWorkout(empty, exerciseId("Kniebeuge")), SetValue(100.0, 5))
        clock.advance(Duration.ofMinutes(60))
        repo.finishWorkout(empty)
        clock.advance(Duration.ofDays(2))

        val current = repo.startOrResumeWorkout()
        val startedAt = repo.observeWorkout(current).first()!!.startedAt
        val previous = repo.previousSession(bench, startedAt)!!

        assertThat(previous.sets)
            .containsExactly(SetValue(82.5, 8), SetValue(82.5, 6), SetValue(80.0, 8))
            .inOrder()
    }

    @Test
    fun letzteWerteIgnorierenDasAktuelleUndSpaetereWorkouts() = runBlocking<Unit> {
        val bench = exerciseId("Bankdrücken (Langhantel)")
        val old = logWorkout(bench, SetValue(70.0, 10))
        logWorkout(bench, SetValue(75.0, 10))

        // Beim nachträglichen Ansehen des ALTEN Workouts darf nicht das neuere als „letztes Mal“ erscheinen.
        val oldStart = repo.observeWorkout(old).first()!!.startedAt
        assertThat(repo.previousSession(bench, oldStart)).isNull()
    }

    @Test
    fun satzDuplizierenHaengtKopieAmEndeAn() = runBlocking<Unit> {
        val id = repo.startOrResumeWorkout()
        val we = repo.addExerciseToWorkout(id, exerciseId("Kniebeuge"))
        repo.addSet(we, SetValue(100.0, 5))
        repo.addSet(we, SetValue(110.0, 3))
        val firstSetId = repo.observeWorkout(id).first()!!.exercises.single().sets.first().id

        repo.duplicateSet(firstSetId)

        val sets = repo.observeWorkout(id).first()!!.exercises.single().sets.map { it.value }
        assertThat(sets).containsExactly(SetValue(100.0, 5), SetValue(110.0, 3), SetValue(100.0, 5)).inOrder()
    }

    @Test
    fun uebungenBehaltenIhreReihenfolgeImWorkout() = runBlocking<Unit> {
        val id = repo.startOrResumeWorkout()
        listOf("Kniebeuge", "Beinpresse", "Wadenheben").forEach { repo.addExerciseToWorkout(id, exerciseId(it)) }
        assertThat(repo.observeWorkout(id).first()!!.exercises.map { it.exercise.name })
            .containsExactly("Kniebeuge", "Beinpresse", "Wadenheben").inOrder()
    }

    @Test
    fun verlaufEnthaeltNurAbgeschlosseneWorkoutsNeuesteZuerst() = runBlocking<Unit> {
        val squat = exerciseId("Kniebeuge")
        val a = logWorkout(squat, SetValue(100.0, 5))
        val b = logWorkout(squat, SetValue(105.0, 5))
        repo.startOrResumeWorkout() // läuft noch

        val history = repo.observeHistory().first()
        assertThat(history.map { it.id }).containsExactly(b, a).inOrder()
        assertThat(history.first().duration(clock.instant())).isEqualTo(Duration.ofMinutes(60))
    }
}
