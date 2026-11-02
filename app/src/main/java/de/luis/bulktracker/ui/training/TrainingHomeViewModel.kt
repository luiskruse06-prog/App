package de.luis.bulktracker.ui.training

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.luis.bulktracker.domain.model.Workout
import de.luis.bulktracker.domain.repo.TrainingRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TrainingHomeState(
    val loading: Boolean = true,
    val active: Workout? = null,
    val history: List<Workout> = emptyList(),
)

@HiltViewModel
class TrainingHomeViewModel @Inject constructor(
    private val repository: TrainingRepository,
) : ViewModel() {

    val state: StateFlow<TrainingHomeState> =
        combine(repository.observeActiveWorkout(), repository.observeHistory()) { active, history ->
            TrainingHomeState(loading = false, active = active, history = history)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TrainingHomeState())

    fun startWorkout(onReady: (Long) -> Unit) {
        viewModelScope.launch { onReady(repository.startOrResumeWorkout()) }
    }
}
