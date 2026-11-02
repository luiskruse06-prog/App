package de.luis.bulktracker.domain.repo

import de.luis.bulktracker.domain.model.Exercise
import de.luis.bulktracker.domain.model.MuscleGroup
import de.luis.bulktracker.domain.model.PreviousSession
import de.luis.bulktracker.domain.model.SetValue
import de.luis.bulktracker.domain.model.Workout
import kotlinx.coroutines.flow.Flow
import java.time.Instant

interface TrainingRepository {
    /** Alle aktiven Übungen, zuletzt genutzte oben, danach alphabetisch. */
    fun observeExercises(): Flow<List<Exercise>>

    /** Legt eine Übung an. Existiert der Name schon (Groß-/Kleinschreibung egal), kommt deren ID zurück. */
    suspend fun createExercise(name: String, muscleGroup: MuscleGroup): Long

    fun observeActiveWorkout(): Flow<Workout?>
    fun observeWorkout(id: Long): Flow<Workout?>
    fun observeHistory(): Flow<List<Workout>>

    /** Startet ein Workout – oder liefert das bereits laufende, damit es nie zwei gleichzeitig gibt. */
    suspend fun startOrResumeWorkout(): Long

    /** Beendet das Workout. Ohne einen einzigen Satz wird es verworfen; Rückgabe: true = gespeichert. */
    suspend fun finishWorkout(id: Long): Boolean
    suspend fun deleteWorkout(id: Long)

    suspend fun addExerciseToWorkout(workoutId: Long, exerciseId: Long): Long
    suspend fun removeExerciseFromWorkout(workoutExerciseId: Long)

    suspend fun addSet(workoutExerciseId: Long, value: SetValue)
    suspend fun updateSet(setId: Long, value: SetValue)
    suspend fun duplicateSet(setId: Long)
    suspend fun deleteSet(setId: Long)

    /** Letzte Einheit dieser Übung, die VOR [before] begonnen hat und mindestens einen Satz enthält. */
    suspend fun previousSession(exerciseId: Long, before: Instant): PreviousSession?
}
