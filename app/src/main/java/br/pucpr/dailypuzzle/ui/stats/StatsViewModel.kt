package br.pucpr.dailypuzzle.ui.stats

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

data class DayStatus(
    val date: String,
    val letter: String,
    val played: Boolean,
    val won: Boolean,
    val isToday: Boolean
)

data class GameStatsUi(
    val game: Game,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val totalPlayed: Int = 0,
    val totalWon: Int = 0,
    val days: List<DayStatus> = emptyList()
)

data class StatsUiState(
    val games: List<GameStatsUi> = emptyList()
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    repository: GameRepository
) : ViewModel() {

    val uiState: StateFlow<StatsUiState> = combine(
        repository.observeStats(),
        repository.observeProgressSince(DateUtils.daysAgo(DAYS - 1))
    ) { stats, progress ->
        val days = DateUtils.lastDays(DAYS)
        val today = DateUtils.today()

        StatsUiState(
            games = Game.entries.map { game ->
                val gameStats = stats.firstOrNull { it.gameId == game }
                val gameProgress = progress.filter { it.gameId == game }

                GameStatsUi(
                    game = game,
                    currentStreak = gameStats?.currentStreak ?: 0,
                    bestStreak = gameStats?.bestStreak ?: 0,
                    totalPlayed = gameStats?.totalPlayed ?: 0,
                    totalWon = gameStats?.totalWon ?: 0,
                    days = days.map { date ->
                        val entry = gameProgress.firstOrNull { it.date == date }
                        DayStatus(
                            date = date,
                            letter = DateUtils.weekdayLetter(date),
                            played = entry != null,
                            won = entry?.won == true,
                            isToday = date == today
                        )
                    }
                )
            }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = StatsUiState()
    )

    private companion object {
        const val DAYS = 7
    }
}
