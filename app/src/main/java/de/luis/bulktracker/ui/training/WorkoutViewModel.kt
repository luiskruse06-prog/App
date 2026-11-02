package de.luis.bulktracker.ui.training

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import de.luis.bulktracker.core.Format
import de.luis.bulktracker.core.Parse
import de.luis.bulktracker.domain.calc.SetPrefill
import de.luis.bulktracker.domain.model.PreviousSession
import de.luis.bulktracker.domain.model.SetValue
import de.luis.bulktracker.domain.model.Workout
import de.luis.bulktracker.domain.model.WorkoutExerciseItem
import de.luis.bulktracker.domain.repo.TrainingRepository
import de.luis.bulktracker.ui.nav.WorkoutRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SetInput(val weight: String = "", val reps: String = "")

data class ExerciseCardState(
    val item: WorkoutExerciseItem,
    val previous: PreviousSession?,
    val input: SetInput,
)

data class WorkoutUiState(
    val loading: Boolean = true,
    val workout: Workout? = null,
    val cards: List<ExerciseCardState> = emptyList(),
)

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: TrainingRepository,
) : ViewModel() {

    val workoutId: Long = savedStateHandle.toRoute<WorkoutRoute>().workoutId

    /**
     * Frühere Einheiten ändern sich während dieses Workouts nicht – einmal je Übung laden reicht.
     * Zugriff nur aus dem (sequentiellen) map-Operator unten.
     */
    private val previousCache = mutableMapOf<Long, PreviousSession?>()

    /** Nur was der Nutzer selbst getippt hat; ohne Eintrag gilt die Vorbefüllung. */
    private val inputs = MutableStateFlow<Map<Long, SetInput>>(emptyMap())

    private val _messages = MutableStateFlow<String?>(null)
    val messages: StateFlow<String?> = _messages.asStateFlow()

    private val workoutWithPrevious = repository.observeWorkout(workoutId).map { workout ->
        if (workout != null) {
            workout.exercises.map { it.exercise.id }.distinct().forEach { exerciseId ->
                if (exerciseId !in previousCache) {
                    previousCache[exerciseId] = repository.previousSession(exerciseId, workout.startedAt)
                }
            }
        }
        workout to previousCache.toMap()
    }

    val state: StateFlow<WorkoutUiState> =
        combine(workoutWithPrevious, inputs) { (workout, previous), typed ->
            WorkoutUiState(
                loading = false,
                workout = workout,
                cards = workout?.exercises.orEmpty().map { item ->
                    val prev = previous[item.exercise.id]
                    ExerciseCardState(
                        item = item,
                        previous = prev,
                        input = typed[item.id] ?: prefill(prev, item),
                    )
                },
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), WorkoutUiState())

    private fun prefill(previous: PreviousSession?, item: WorkoutExerciseItem): SetInput {
        val suggestion = SetPrefill.suggest(previous?.sets.orEmpty(), item.sets.map { it.value })
            ?: return SetInput()
        return SetInput(Format.number(suggestion.weightKg), suggestion.reps.toString())
    }

    private fun currentInput(workoutExerciseId: Long): SetInput =
        state.value.cards.firstOrNull { it.item.id == workoutExerciseId }?.input ?: SetInput()

    fun onWeightChange(workoutExerciseId: Long, text: String) {
        val current = currentInput(workoutExerciseId)
        inputs.update { it + (workoutExerciseId to current.copy(weight = text)) }
    }

    fun onRepsChange(workoutExerciseId: Long, text: String) {
        val current = currentInput(workoutExerciseId)
        inputs.update { it + (workoutExerciseId to current.copy(reps = text)) }
    }

    fun addSet(workoutExerciseId: Long) {
        val input = currentInput(workoutExerciseId)
        val value = parse(input.weight, input.reps) ?: return
        viewModelScope.launch {
            repository.addSet(workoutExerciseId, value)
            // Eingabe zurücksetzen, damit die Vorbefüllung für den NÄCHSTEN Satz greift.
            inputs.update { it - workoutExerciseId }
        }
    }

    fun updateSet(setId: Long, weightText: String, repsText: String): Boolean {
        val value = parse(weightText, repsText) ?: return false
        viewModelScope.launch { repository.updateSet(setId, value) }
        return true
    }

    fun duplicateSet(setId: Long) {
        viewModelScope.launch { repository.duplicateSet(setId) }
    }

    fun deleteSet(setId: Long) {
        viewModelScope.launch { repository.deleteSet(setId) }
    }

    fun removeExercise(workoutExerciseId: Long) {
        viewModelScope.launch {
            repository.removeExerciseFromWorkout(workoutExerciseId)
            inputs.update { it - workoutExerciseId }
        }
    }

    fun finish(onDone: () -> Unit) {
        viewModelScope.launch {
            val saved = repository.finishWorkout(workoutId)
            if (!saved) _messages.value = "Workout ohne Sätze verworfen."
            onDone()
        }
    }

    fun delete(onDone: () -> Unit) {
        viewModelScope.launch {
            repository.deleteWorkout(workoutId)
            onDone()
        }
    }

    fun messageShown() {
        _messages.value = null
    }

    private fun parse(weightText: String, repsText: String): SetValue? {
        val weight = Parse.weightKg(weightText)
        val reps = Parse.reps(repsText)
        if (weight == null) {
            _messages.value = "Gewicht ungültig – z. B. 82,5 (0 = Körpergewicht)."
            return null
        }
        if (reps == null) {
            _messages.value = "Wiederholungen ungültig – ganze Zahl von 1 bis 999."
            return null
        }
        return SetValue(weight, reps)
    }
}
