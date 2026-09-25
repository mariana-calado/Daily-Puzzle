package br.pucpr.dailypuzzle.ui.result

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.pucpr.dailypuzzle.data.repository.GameRepository
import br.pucpr.dailypuzzle.model.Game
import br.pucpr.dailypuzzle.navigation.Routes
import br.pucpr.dailypuzzle.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ResultUiState(
    val loading: Boolean = true,
    val game: Game = Game.CACA_PALAVRAS,
    val dateLabel: String = "",
    val played: Boolean = false,
    val won: Boolean = false,
    val attempts: Int = 0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val totalPlayed: Int = 0,
    val totalWon: Int = 0
)

@HiltViewModel
class ResultViewModel @Inject constructor(
    private val repository: GameRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val game: Game = Game.entries
        .firstOrNull { it.name == savedStateHandle.get<String>(Routes.ARG_GAME_ID) }
        ?: Game.CACA_PALAVRAS

    private val date: String = savedStateHandle.get<String>(Routes.ARG_DATE)
        ?.takeIf { it.isNotBlank() }
        ?: DateUtils.today()

    private val _uiState = MutableStateFlow(ResultUiState())
    val uiState: StateFlow<ResultUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val progress = repository.getProgress(game, date)
            val stats = repository.getStats(game)

            _uiState.value = ResultUiState(
                loading = false,
                game = game,
                dateLabel = DateUtils.label(date),
                played = progress != null,
                won = progress?.won == true,
                attempts = progress?.attempts ?: 0,
                currentStreak = stats?.currentStreak ?: 0,
                bestStreak = stats?.bestStreak ?: 0,
                totalPlayed = stats?.totalPlayed ?: 0,
                totalWon = stats?.totalWon ?: 0
            )
        }
    }
}
