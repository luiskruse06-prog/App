package de.luis.bulktracker.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import de.luis.bulktracker.data.db.entity.ExerciseEntity
import de.luis.bulktracker.data.db.entity.PreviousSetRow
import de.luis.bulktracker.data.db.entity.WorkoutEntity
import de.luis.bulktracker.data.db.entity.WorkoutExerciseEntity
import de.luis.bulktracker.data.db.entity.WorkoutSetEntity
import de.luis.bulktracker.data.db.entity.WorkoutWithExercises
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
interface TrainingDao {
    // --- Übungen ---------------------------------------------------------------------------

    @Query("SELECT * FROM exercise WHERE archived = 0 ORDER BY lastUsedAt IS NULL, lastUsedAt DESC, name")
    fun observeExercises(): Flow<List<ExerciseEntity>>

    /** Die Spalte hat COLLATE NOCASE – „bankdrücken“ findet also auch „Bankdrücken“. */
    @Query("SELECT * FROM exercise WHERE name = :name LIMIT 1")
    suspend fun findExerciseByName(name: String): ExerciseEntity?

    @Insert
    suspend fun insertExercise(exercise: ExerciseEntity): Long

    @Query("UPDATE exercise SET archived = 0 WHERE id = :id")
    suspend fun unarchiveExercise(id: Long)

    @Query("UPDATE exercise SET lastUsedAt = :at WHERE id = :id")
    suspend fun touchExercise(id: Long, at: Instant)

    // --- Workouts --------------------------------------------------------------------------

    @Insert
    suspend fun insertWorkout(workout: WorkoutEntity): Long

    @Query("SELECT * FROM workout WHERE endedAt IS NULL ORDER BY startedAt DESC LIMIT 1")
    suspend fun getActiveWorkout(): WorkoutEntity?

    @Transaction
    @Query("SELECT * FROM workout WHERE endedAt IS NULL ORDER BY startedAt DESC LIMIT 1")
    fun observeActiveWorkout(): Flow<WorkoutWithExercises?>

    @Transaction
    @Query("SELECT * FROM workout WHERE id = :id")
    fun observeWorkout(id: Long): Flow<WorkoutWithExercises?>

    @Transaction
    @Query("SELECT * FROM workout WHERE endedAt IS NOT NULL ORDER BY startedAt DESC")
    fun observeHistory(): Flow<List<WorkoutWithExercises>>

    @Query("UPDATE workout SET endedAt = :endedAt WHERE id = :id")
    suspend fun finishWorkout(id: Long, endedAt: Instant)

    @Query("DELETE FROM workout WHERE id = :id")
    suspend fun deleteWorkout(id: Long)

    @Query(
        """SELECT COUNT(*) FROM workout_set ws
           JOIN workout_exercise we ON we.id = ws.workoutExerciseId
           WHERE we.workoutId = :workoutId""",
    )
    suspend fun countSets(workoutId: Long): Int

    // --- Übungen im Workout ----------------------------------------------------------------

    @Query("SELECT COALESCE(MAX(position), -1) + 1 FROM workout_exercise WHERE workoutId = :workoutId")
    suspend fun nextExercisePosition(workoutId: Long): Int

    @Insert
    suspend fun insertWorkoutExercise(entry: WorkoutExerciseEntity): Long

    @Query("DELETE FROM workout_exercise WHERE id = :id")
    suspend fun deleteWorkoutExercise(id: Long)

    // --- Sätze -----------------------------------------------------------------------------

    @Query("SELECT COALESCE(MAX(position), -1) + 1 FROM workout_set WHERE workoutExerciseId = :workoutExerciseId")
    suspend fun nextSetPosition(workoutExerciseId: Long): Int

    @Insert
    suspend fun insertSet(set: WorkoutSetEntity): Long

    @Query("SELECT * FROM workout_set WHERE id = :id")
    suspend fun getSet(id: Long): WorkoutSetEntity?

    @Query("UPDATE workout_set SET weightKg = :weightKg, reps = :reps WHERE id = :id")
    suspend fun updateSet(id: Long, weightKg: Double, reps: Int)

    @Query("DELETE FROM workout_set WHERE id = :id")
    suspend fun deleteSet(id: Long)

    /**
     * Sätze der jüngsten Einheit dieser Übung, die vor [before] begann. Einheiten, in denen die
     * Übung nur hinzugefügt, aber kein Satz geloggt wurde, werden übersprungen – sonst stünde
     * „Letztes Mal: –“ da, obwohl es eine echte Einheit davor gibt.
     */
    @Query(
        """SELECT w.startedAt AS startedAt, ws.weightKg AS weightKg, ws.reps AS reps, ws.position AS position
           FROM workout_set ws
           JOIN workout_exercise we ON we.id = ws.workoutExerciseId
           JOIN workout w ON w.id = we.workoutId
           WHERE we.id = (
               SELECT we2.id FROM workout_exercise we2
               JOIN workout w2 ON w2.id = we2.workoutId
               WHERE we2.exerciseId = :exerciseId
                 AND w2.startedAt < :before
                 AND EXISTS (SELECT 1 FROM workout_set s WHERE s.workoutExerciseId = we2.id)
               ORDER BY w2.startedAt DESC, we2.id DESC
               LIMIT 1
           )
           ORDER BY ws.position""",
    )
    suspend fun previousSets(exerciseId: Long, before: Instant): List<PreviousSetRow>
}
