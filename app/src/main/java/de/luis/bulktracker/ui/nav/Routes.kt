package de.luis.bulktracker.ui.nav

import kotlinx.serialization.Serializable

@Serializable data object TodayRoute
@Serializable data object NutritionRoute
@Serializable data object RecipesRoute
@Serializable data object TrainingRoute
@Serializable data object MoreRoute

@Serializable data class WorkoutRoute(val workoutId: Long)
@Serializable data class ExercisePickerRoute(val workoutId: Long)
