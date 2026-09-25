package br.pucpr.dailypuzzle.ui.games.cacapalavras

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.pucpr.dailypuzzle.data.content.WordSearchGenerator
import br.pucpr.dailypuzzle.data.content.WordSearchLoader
import br.pucpr.dailypuzzle.data.repository.GameRepository
import br.pucpr.dailypuzzle.model.DailySeed
import br.pucpr.dailypuzzle.model.Game
import br.pucpr.dailypuzzle.model.WordCell
import br.pucpr.dailypuzzle.model.WordSearchPuzzle
import br.pucpr.dailypuzzle.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class CacaPalavrasUiState(
    val loading: Boolean = true,
    val alreadyPlayed: Boolean = false,
    val puzzle: WordSearchPuzzle? = null,
    val foundWords: Set<String> = emptySet(),
    val foundCells: Set<WordCell> = emptySet(),
    val selection: List<WordCell> = emptyList(),
    val attempts: Int = 0,
    val finished: Boolean = false
)

@HiltViewModel
class CacaPalavrasViewModel @Inject constructor(
    private val repository: GameRepository,
    private val loader: WordSearchLoader
) : ViewModel() {

    private val _uiState = MutableStateFlow(CacaPalavrasUiState())
    val uiState: StateFlow<CacaPalavrasUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            val date = DateUtils.today()
            val alreadyPlayed = repository.playedToday(Game.CACA_PALAVRAS)

            val puzzle = withContext(Dispatchers.IO) {
                val banks = loader.loadBanks()
                val bank = banks[DailySeed.indexFor(date, banks.size)]
                WordSearchGenerator.generate(
                    theme = bank.theme,
                    words = bank.words.take(WORDS_PER_PUZZLE),
                    size = GRID_SIZE,
                    random = DailySeed.randomFor(date)
                )
            }

            _uiState.update { state ->
                state.copy(
                    loading = false,
                    alreadyPlayed = alreadyPlayed,
                    puzzle = puzzle
                )
            }
        }
    }

    fun onSelectionChanged(cells: List<WordCell>) {
        if (_uiState.value.finished) return
        _uiState.update { state -> state.copy(selection = cells) }
    }

    fun onSelectionFinished() {
        val state = _uiState.value
        val puzzle = state.puzzle

        if (puzzle == null || state.finished || state.selection.size < 2) {
            _uiState.update { it.copy(selection = emptyList()) }
            return
        }

        val match = puzzle.words.firstOrNull { placed ->
            placed.word !in state.foundWords &&
                (placed.cells == state.selection || placed.cells == state.selection.reversed())
        }

        val foundWords = if (match == null) state.foundWords else state.foundWords + match.word
        val foundCells = if (match == null) state.foundCells else state.foundCells + match.cells
        val attempts = state.attempts + 1
        val finished = foundWords.size == puzzle.words.size

        _uiState.update {
            it.copy(
                selection = emptyList(),
                foundWords = foundWords,
                foundCells = foundCells,
                attempts = attempts,
                finished = finished
            )
        }

        if (finished) {
            viewModelScope.launch {
                repository.saveResult(Game.CACA_PALAVRAS, won = true, attempts = attempts)
            }
        }
    }

    private companion object {
        const val GRID_SIZE = 8
        const val WORDS_PER_PUZZLE = 6
    }
}
