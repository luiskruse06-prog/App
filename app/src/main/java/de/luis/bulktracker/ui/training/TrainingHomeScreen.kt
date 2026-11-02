package de.luis.bulktracker.ui.training

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.luis.bulktracker.core.Format
import de.luis.bulktracker.domain.model.Workout
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainingHomeScreen(
    onOpenWorkout: (Long) -> Unit,
    viewModel: TrainingHomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = { TopAppBar(title = { Text("Training") }) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                val active = state.active
                if (active != null) {
                    ActiveWorkoutCard(active, onClick = { onOpenWorkout(active.id) })
                } else {
                    Button(
                        onClick = { viewModel.startWorkout(onOpenWorkout) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.loading,
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null)
                        Text("  Workout starten")
                    }
                }
            }
            item {
                Spacer(Modifier.height(4.dp))
                Text("Verlauf", style = MaterialTheme.typography.titleMedium)
            }
            if (!state.loading && state.history.isEmpty()) {
                item {
                    Text(
                        "Noch keine abgeschlossenen Workouts.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            items(state.history, key = { it.id }) { workout ->
                HistoryCard(workout, onClick = { onOpenWorkout(workout.id) })
            }
        }
    }
}

@Composable
private fun ActiveWorkoutCard(workout: Workout, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Workout läuft", style = MaterialTheme.typography.titleMedium)
            Text(
                "seit ${Format.time(workout.startedAt)} · ${workout.exercises.size} Übungen · ${workout.setCount} Sätze",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text("Tippen zum Fortsetzen", style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun HistoryCard(workout: Workout, onClick: () -> Unit) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(Format.weekdayDate(workout.startedAt), style = MaterialTheme.typography.titleSmall)
            Text(
                "${Format.time(workout.startedAt)} · ${Format.duration(workout.duration(Instant.now()))} · ${workout.setCount} Sätze",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (workout.exercises.isNotEmpty()) {
                Text(
                    workout.exercises.joinToString(" · ") { it.exercise.name },
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
