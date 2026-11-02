package de.luis.bulktracker.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import de.luis.bulktracker.data.db.dao.TrainingDao
import de.luis.bulktracker.data.db.entity.BodyWeightEntity
import de.luis.bulktracker.data.db.entity.ExerciseEntity
import de.luis.bulktracker.data.db.entity.MealEntryEntity
import de.luis.bulktracker.data.db.entity.MealTemplateEntity
import de.luis.bulktracker.data.db.entity.RecipeApplianceEntity
import de.luis.bulktracker.data.db.entity.RecipeEntity
import de.luis.bulktracker.data.db.entity.RecipeIngredientEntity
import de.luis.bulktracker.data.db.entity.RecipeStepEntity
import de.luis.bulktracker.data.db.entity.RecipeTagEntity
import de.luis.bulktracker.data.db.entity.SleepEntryEntity
import de.luis.bulktracker.data.db.entity.WorkoutEntity
import de.luis.bulktracker.data.db.entity.WorkoutExerciseEntity
import de.luis.bulktracker.data.db.entity.WorkoutSetEntity

/**
 * Das komplette Datenmodell steht ab Version 1, auch für Bereiche, deren Oberfläche erst in
 * späteren Phasen kommt. So muss für die geplanten Features keine Migration geschrieben werden.
 */
@Database(
    version = AppDatabase.VERSION,
    exportSchema = true,
    entities = [
        ExerciseEntity::class,
        WorkoutEntity::class,
        WorkoutExerciseEntity::class,
        WorkoutSetEntity::class,
        MealEntryEntity::class,
        MealTemplateEntity::class,
        RecipeEntity::class,
        RecipeIngredientEntity::class,
        RecipeStepEntity::class,
        RecipeApplianceEntity::class,
        RecipeTagEntity::class,
        SleepEntryEntity::class,
        BodyWeightEntity::class,
    ],
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trainingDao(): TrainingDao

    companion object {
        const val VERSION = 1
        const val NAME = "bulktracker.db"

        fun build(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, NAME)
                .addMigrations(*Migrations.ALL)
                .addCallback(SeedData.callback)
                .build()
    }
}
