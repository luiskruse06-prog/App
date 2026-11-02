package de.luis.bulktracker.ui.training

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.luis.bulktracker.core.Format
import de.luis.bulktracker.domain.model.LoggedSet
import de.luis.bulktracker.domain.model.Workout
import kotlinx.coroutines.delay
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(
    onBack: () -> Unit,
    onAddExercise: (Long) -> Unit,
    viewModel: WorkoutViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val message by viewModel.messages.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var confirmFinish by rememberSaveable { mutableStateOf(false) }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    var menuOpen by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<LoggedSet?>(null) }

    LaunchedEffect(message) {
        message?.let {
            snackbar.showSnackbar(it)
            viewModel.messageShown()
        }
    }

    val workout = state.workout

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(if (workout?.isActive != false) "Workout" else "Workout (abgeschlossen)")
                        if (workout != null) WorkoutSubtitle(workout)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                },
                actions = {
                    if (workout?.isActive == true) {
                        TextButton(onClick = { confirmFinish = true }) { Text("Beenden") }
                    }
                    Box {
                        IconButton(onClick = { menuOpen = true }) {
                            Icon(Icons.Filled.MoreVert, contentDescription = "Mehr")
                        }
                        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                            DropdownMenuItem(
                                text = { Text("Workout löschen") },
                                leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null) },
                                onClick = {
                                    menuOpen = false
                                    confirmDelete = true
                                },
                            )
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            if (workout != null) {
                ExtendedFloatingActionButton(
                    onClick = { onAddExercise(workout.id) },
                    // Die Material-3-FAB versteckt ihren Text vor TalkBack – deshalb die Beschreibung am Icon.
                    icon = { Icon(Icons.Filled.Add, contentDescription = "Übung hinzufügen") },
                    text = { Text("Übung") },
                )
            }
        },
    ) { padding ->
        when {
            state.loading -> Unit
            workout == null -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Workout nicht gefunden.")
            }
            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (state.cards.isEmpty()) {
                    item {
                        Text(
                            "Noch keine Übung. Tippe unten auf „Übung“.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 24.dp),
                        )
                    }
                }
                items(state.cards, key = { it.item.id }) { card ->
                    ExerciseCard(
                        card = card,
                        onWeightChange = { viewModel.onWeightChange(card.item.id, it) },
                        onRepsChange = { viewModel.onRepsChange(card.item.id, it) },
                        onAddSet = { viewModel.addSet(card.item.id) },
                        onEditSet = { editing = it },
                        onDuplicateSet = viewModel::duplicateSet,
                        onDeleteSet = viewModel::deleteSet,
                        onRemoveExercise = { viewModel.removeExercise(card.item.id) },
                    )
                }
            }
        }
    }

    if (confirmFinish && workout != null) {
        AlertDialog(
            onDismissRequest = { confirmFinish = false },
            title = { Text("Workout beenden?") },
            text = {
                Text(
                    if (workout.setCount == 0) "Es ist noch kein Satz eingetragen – das Workout wird verworfen."
                    else "${workout.exercises.size} Übungen, ${workout.setCount} Sätze, " +
                        "Dauer ${Format.duration(workout.duration(Instant.now()))}.",
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmFinish = false
                    viewModel.finish(onBack)
                }) { Text("Beenden") }
            },
            dismissButton = { TextButton(onClick = { confirmFinish = false }) { Text("Weiter trainieren") } },
        )
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Workout löschen?") },
            text = { Text("Alle Sätze dieses Workouts werden endgültig gelöscht.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    viewModel.delete(onBack)
                }) { Text("Löschen") }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Abbrechen") } },
        )
    }

    editing?.let { set ->
        EditSetDialog(
            set = set,
            onDismiss = { editing = null },
            onSave = { weight, reps -> if (viewModel.updateSet(set.id, weight, reps)) editing = null },
        )
    }
}

@Composable
private fun WorkoutSubtitle(workout: Workout) {
    // Laufende Uhr nur für aktive Workouts; Minutenauflösung genügt.
    val now by produceState(Instant.now(), workout.isActive) {
        while (workout.isActive) {
            value = Instant.now()
            delay(30_000)
        }
    }
    Text(
        "${Format.weekdayDate(workout.startedAt)} · ${Format.time(workout.startedAt)} · " +
            Format.duration(workout.duration(now)),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun ExerciseCard(
    card: ExerciseCardState,
    onWeightChange: (String) -> Unit,
    onRepsChange: (String) -> Unit,
    onAddSet: () -> Unit,
    onEditSet: (LoggedSet) -> Unit,
    onDuplicateSet: (Long) -> Unit,
    onDeleteSet: (Long) -> Unit,
    onRemoveExercise: () -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }
    var confirmRemove by remember { mutableStateOf(false) }

    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(start = 16.dp, end = 4.dp, top = 8.dp, bottom = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(card.item.exercise.name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        card.item.exercise.muscleGroup.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Box {
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Übungsmenü")
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        DropdownMenuItem(
                            text = { Text("Aus Workout entfernen") },
                            onClick = {
                                menuOpen = false
                                if (card.item.sets.isEmpty()) onRemoveExercise() else confirmRemove = true
                            },
                        )
                    }
                }
            }

            val previous = card.previous
            Text(
                if (previous == null) "Letztes Mal: –"
                else "Letztes Mal (${Format.shortDate(previous.date)}): " +
                    previous.sets.joinToString(" · ") { "${Format.number(it.weightKg)}×${it.reps}" },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, end = 12.dp),
            )

            card.item.sets.forEachIndexed { index, set ->
                SetRow(
                    number = index + 1,
                    set = set,
                    onClick = { onEditSet(set) },
                    onDuplicate = { onDuplicateSet(set.id) },
                    onDelete = { onDeleteSet(set.id) },
                )
            }

            Row(
                modifier = Modifier.padding(top = 8.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = card.input.weight,
                    onValueChange = onWeightChange,
                    label = { Text("kg") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                    modifier = Modifier.weight(1f),
                )
                Text("×")
                OutlinedTextField(
                    value = card.input.reps,
                    onValueChange = onRepsChange,
                    label = { Text("Wdh.") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    modifier = Modifier.weight(1f),
                )
                FilledTonalIconButton(onClick = onAddSet) {
                    Icon(Icons.Filled.Add, contentDescription = "Satz hinzufügen")
                }
            }
        }
    }

    if (confirmRemove) {
        AlertDialog(
            onDismissRequest = { confirmRemove = false },
            title = { Text("Übung entfernen?") },
            text = { Text("${card.item.sets.size} eingetragene Sätze werden mit gelöscht.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmRemove = false
                    onRemoveExercise()
                }) { Text("Entfernen") }
            },
            dismissButton = { TextButton(onClick = { confirmRemove = false }) { Text("Abbrechen") } },
        )
    }
}

@Composable
private fun SetRow(
    number: Int,
    set: LoggedSet,
    onClick: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "$number",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(28.dp),
        )
        Text(
            Format.set(set.value.weightKg, set.value.reps),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onDuplicate) {
            Icon(Icons.Filled.ContentCopy, contentDescription = "Satz duplizieren")
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Filled.Delete, contentDescription = "Satz löschen")
        }
    }
}

@Composable
private fun EditSetDialog(
    set: LoggedSet,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit,
) {
    var weight by rememberSaveable(set.id) { mutableStateOf(Format.number(set.value.weightKg)) }
    var reps by rememberSaveable(set.id) { mutableStateOf(set.value.reps.toString()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Satz bearbeiten") },
        text = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text("kg") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                )
                Text("×")
                OutlinedTextField(
                    value = reps,
                    onValueChange = { reps = it },
                    label = { Text("Wdh.") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                )
            }
        },
        confirmButton = { TextButton(onClick = { onSave(weight, reps) }) { Text("Speichern") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Abbrechen") } },
    )
}
