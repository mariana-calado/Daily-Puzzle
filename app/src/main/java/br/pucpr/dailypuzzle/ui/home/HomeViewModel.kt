package br.pucpr.dailypuzzle.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.pucpr.dailypuzzle.data.repository.GameRepository
import br.pucpr.dailypuzzle.model.Game
import br.pucpr.dailypuzzle.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class HomeUiState(
    val dateLabel: String = "",
    val games: List<Game> = Game.entries,
    val results: Map<Game, Boolean> = emptyMap(),
    val streak: Int = 0
) {
    val completedCount: Int get() = results.size
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    repository: GameRepository
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        repository.observeTodayProgress(),
        repository.observeStats()
    ) { progress, stats ->
        HomeUiState(
            dateLabel = DateUtils.todayLabel(),
            results = progress.filter { it.completed }.associate { it.gameId to it.won },
            streak = stats.maxOfOrNull { it.currentStreak } ?: 0
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(dateLabel = DateUtils.todayLabel())
    )
}
