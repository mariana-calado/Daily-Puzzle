package br.pucpr.dailypuzzle.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.pucpr.dailypuzzle.data.repository.GameRepository
import br.pucpr.dailypuzzle.model.Game
import br.pucpr.dailypuzzle.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class HomeUiState(
    val dateLabel: String = "",
    val games: List<Game> = Game.entries,
    val completedToday: Set<Game> = emptySet()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    repository: GameRepository
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = repository.observeTodayProgress()
        .map { progress ->
            HomeUiState(
                dateLabel = DateUtils.todayLabel(),
                completedToday = progress.filter { it.completed }.map { it.gameId }.toSet()
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState(dateLabel = DateUtils.todayLabel())
        )
}
