package de.luis.bulktracker.ui.nav

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import de.luis.bulktracker.ui.common.PlaceholderScreen
import de.luis.bulktracker.ui.more.MoreScreen
import de.luis.bulktracker.ui.training.ExercisePickerScreen
import de.luis.bulktracker.ui.training.TrainingHomeScreen
import de.luis.bulktracker.ui.training.WorkoutScreen
import kotlin.reflect.KClass

private enum class TopLevel(val route: Any, val routeClass: KClass<*>, val label: String, val icon: ImageVector) {
    TODAY(TodayRoute, TodayRoute::class, "Heute", Icons.Filled.Home),
    NUTRITION(NutritionRoute, NutritionRoute::class, "Ernährung", Icons.Filled.Restaurant),
    RECIPES(RecipesRoute, RecipesRoute::class, "Rezepte", Icons.AutoMirrored.Filled.MenuBook),
    TRAINING(TrainingRoute, TrainingRoute::class, "Training", Icons.Filled.FitnessCenter),
    MORE(MoreRoute, MoreRoute::class, "Mehr", Icons.Filled.MoreHoriz),
}

/** Gerüst der App: untere Navigation + NavHost. */
@Composable
fun AppRoot() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    // Die einzelnen Screens bringen eigene TopAppBars (inkl. Statusleisten-Abstand) mit;
    // das äußere Gerüst kümmert sich nur um die untere Leiste.
    Scaffold(
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            NavigationBar {
                TopLevel.entries.forEach { item ->
                    val selected = currentDestination?.hierarchy?.any { it.hasRoute(item.routeClass) } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = null) },
                        label = { Text(item.label) },
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = TodayRoute,
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .imePadding(),
        ) {
            composable<TodayRoute> {
                PlaceholderScreen("Heute", "Das Dashboard kommt in Phase 6.")
            }
            composable<NutritionRoute> {
                PlaceholderScreen("Ernährung", "Portionen-Log, Vorlagen und Tages-/Wochenansicht kommen in Phase 2.")
            }
            composable<RecipesRoute> {
                PlaceholderScreen("Rezepte", "Rezeptsammlung und Startrezepte kommen in Phase 2, Import in Phase 5.")
            }
            composable<TrainingRoute> {
                TrainingHomeScreen(
                    onOpenWorkout = { id -> navController.navigate(WorkoutRoute(id)) },
                )
            }
            composable<MoreRoute> {
                MoreScreen()
            }
            composable<WorkoutRoute> {
                WorkoutScreen(
                    onBack = { navController.popBackStack() },
                    onAddExercise = { workoutId -> navController.navigate(ExercisePickerRoute(workoutId)) },
                )
            }
            composable<ExercisePickerRoute> { entry ->
                val route = entry.toRoute<ExercisePickerRoute>()
                ExercisePickerScreen(
                    workoutId = route.workoutId,
                    onDone = { navController.popBackStack() },
                )
            }
        }
    }
}
