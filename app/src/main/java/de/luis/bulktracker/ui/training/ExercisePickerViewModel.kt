package de.luis.bulktracker.ui.training

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.luis.bulktracker.core.Search
import de.luis.bulktracker.domain.model.Exercise
import de.luis.bulktracker.domain.model.MuscleGroup
import de.luis.bulktracker.domain.repo.TrainingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExercisePickerState(
    val query: String = "",
    val exercises: List<Exercise> = emptyList(),
    /** true, wenn kein Treffer genau so heißt – dann wird „anlegen“ angeboten. */
    val canCreateFromQuery: Boolean = false,
)

@HiltViewModel
class ExercisePickerViewModel @Inject constructor(
    private val repository: TrainingRepository,
) : ViewModel() {

    private val query = MutableStateFlow("")

    val state: StateFlow<ExercisePickerState> =
        combine(repository.observeExercises(), query) { all, q ->
            val filtered = all.filter { Search.matches(it.name, q) }
            ExercisePickerState(
                query = q,
                exercises = filtered,
                canCreateFromQuery = q.isNotBlank() &&
                    all.none { it.name.equals(q.trim(), ignoreCase = true) },
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ExercisePickerState())

    fun onQueryChange(text: String) {
        query.value = text
    }

    fun pick(workoutId: Long, exerciseId: Long, onDone: () -> Unit) {
        viewModelScope.launch {
            repository.addExerciseToWorkout(workoutId, exerciseId)
            onDone()
        }
    }

    fun createAndPick(workoutId: Long, name: String, group: MuscleGroup, onDone: () -> Unit) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val id = repository.createExercise(name, group)
            repository.addExerciseToWorkout(workoutId, id)
            onDone()
        }
    }
}
