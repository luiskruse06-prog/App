package de.luis.bulktracker.data.repo

import androidx.room.withTransaction
import de.luis.bulktracker.data.db.AppDatabase
import de.luis.bulktracker.data.db.entity.ExerciseEntity
import de.luis.bulktracker.data.db.entity.WorkoutEntity
import de.luis.bulktracker.data.db.entity.WorkoutExerciseEntity
import de.luis.bulktracker.data.db.entity.WorkoutSetEntity
import de.luis.bulktracker.data.db.entity.WorkoutWithExercises
import de.luis.bulktracker.domain.model.Exercise
import de.luis.bulktracker.domain.model.LoggedSet
import de.luis.bulktracker.domain.model.MuscleGroup
import de.luis.bulktracker.domain.model.PreviousSession
import de.luis.bulktracker.domain.model.SetValue
import de.luis.bulktracker.domain.model.Workout
import de.luis.bulktracker.domain.model.WorkoutExerciseItem
import de.luis.bulktracker.domain.repo.TrainingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Clock
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrainingRepositoryImpl @Inject constructor(
    private val db: AppDatabase,
    private val clock: Clock,
) : TrainingRepository {

    private val dao = db.trainingDao()

    override fun observeExercises(): Flow<List<Exercise>> =
        dao.observeExercises().map { list -> list.map { it.toDomain() } }

    override suspend fun createExercise(name: String, muscleGroup: MuscleGroup): Long {
        val clean = name.trim().replace(Regex("\\s+"), " ")
        require(clean.isNotEmpty()) { "Name darf nicht leer sein" }
        return db.withTransaction {
            val existing = dao.findExerciseByName(clean)
            if (existing != null) {
                if (existing.archived) dao.unarchiveExercise(existing.id)
                existing.id
            } else {
                dao.insertExercise(ExerciseEntity(name = clean, muscleGroup = muscleGroup))
            }
        }
    }

    override fun observeActiveWorkout(): Flow<Workout?> =
        dao.observeActiveWorkout().map { it?.toDomain() }

    override fun observeWorkout(id: Long): Flow<Workout?> =
        dao.observeWorkout(id).map { it?.toDomain() }

    override fun observeHistory(): Flow<List<Workout>> =
        dao.observeHistory().map { list -> list.map { it.toDomain() } }

    override suspend fun startOrResumeWorkout(): Long = db.withTransaction {
        dao.getActiveWorkout()?.id ?: dao.insertWorkout(WorkoutEntity(startedAt = clock.instant()))
    }

    override suspend fun finishWorkout(id: Long): Boolean = db.withTransaction {
        if (dao.countSets(id) == 0) {
            dao.deleteWorkout(id)
            false
        } else {
            dao.finishWorkout(id, clock.instant())
            true
        }
    }

    override suspend fun deleteWorkout(id: Long) = dao.deleteWorkout(id)

    override suspend fun addExerciseToWorkout(workoutId: Long, exerciseId: Long): Long = db.withTransaction {
        dao.touchExercise(exerciseId, clock.instant())
        dao.insertWorkoutExercise(
            WorkoutExerciseEntity(
                workoutId = workoutId,
                exerciseId = exerciseId,
                position = dao.nextExercisePosition(workoutId),
            ),
        )
    }

    override suspend fun removeExerciseFromWorkout(workoutExerciseId: Long) =
        dao.deleteWorkoutExercise(workoutExerciseId)

    override suspend fun addSet(workoutExerciseId: Long, value: SetValue) {
        db.withTransaction {
            dao.insertSet(
                WorkoutSetEntity(
                    workoutExerciseId = workoutExerciseId,
                    position = dao.nextSetPosition(workoutExerciseId),
                    weightKg = value.weightKg,
                    reps = value.reps,
                ),
            )
        }
    }

    override suspend fun updateSet(setId: Long, value: SetValue) =
        dao.updateSet(setId, value.weightKg, value.reps)

    override suspend fun duplicateSet(setId: Long) {
        val original = dao.getSet(setId) ?: return
        addSet(original.workoutExerciseId, SetValue(original.weightKg, original.reps))
    }

    override suspend fun deleteSet(setId: Long) = dao.deleteSet(setId)

    override suspend fun previousSession(exerciseId: Long, before: Instant): PreviousSession? {
        val rows = dao.previousSets(exerciseId, before)
        if (rows.isEmpty()) return null
        return PreviousSession(
            date = rows.first().startedAt,
            sets = rows.map { SetValue(it.weightKg, it.reps) },
        )
    }
}

private fun ExerciseEntity.toDomain() = Exercise(
    id = id,
    name = name,
    muscleGroup = muscleGroup,
    lastUsedAt = lastUsedAt,
)

/** @Relation liefert Listen ohne garantierte Reihenfolge – deshalb hier nach Position sortieren. */
private fun WorkoutWithExercises.toDomain() = Workout(
    id = workout.id,
    startedAt = workout.startedAt,
    endedAt = workout.endedAt,
    exercises = exercises
        .sortedBy { it.entry.position }
        .map { we ->
            WorkoutExerciseItem(
                id = we.entry.id,
                exercise = we.exercise.toDomain(),
                sets = we.sets
                    .sortedWith(compareBy({ it.position }, { it.id }))
                    .map { LoggedSet(it.id, SetValue(it.weightKg, it.reps)) },
            )
        },
)
