package de.luis.bulktracker.domain.model

import java.time.Duration
import java.time.Instant

data class Exercise(
    val id: Long,
    val name: String,
    val muscleGroup: MuscleGroup,
    val lastUsedAt: Instant?,
)

/** Ein Satz als reiner Wert: Gewicht in kg (0 = nur Körpergewicht) × Wiederholungen. */
data class SetValue(val weightKg: Double, val reps: Int)

data class LoggedSet(val id: Long, val value: SetValue)

data class WorkoutExerciseItem(
    val id: Long,
    val exercise: Exercise,
    val sets: List<LoggedSet>,
)

data class Workout(
    val id: Long,
    val startedAt: Instant,
    val endedAt: Instant?,
    val exercises: List<WorkoutExerciseItem>,
) {
    val isActive: Boolean get() = endedAt == null
    val setCount: Int get() = exercises.sumOf { it.sets.size }
    fun duration(now: Instant): Duration = Duration.between(startedAt, endedAt ?: now)
}

/** Die Sätze der letzten Einheit einer Übung – für die Anzeige „Letztes Mal“ und die Vorbefüllung. */
data class PreviousSession(val date: Instant, val sets: List<SetValue>)
