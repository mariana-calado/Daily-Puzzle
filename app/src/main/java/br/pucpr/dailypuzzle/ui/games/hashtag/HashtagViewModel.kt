package br.pucpr.dailypuzzle.ui.games.hashtag

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.pucpr.dailypuzzle.data.repository.GameRepository
import br.pucpr.dailypuzzle.model.Game
import br.pucpr.dailypuzzle.model.GameStatus
import br.pucpr.dailypuzzle.model.LetterStatus
import br.pucpr.dailypuzzle.navigation.Routes
import br.pucpr.dailypuzzle.util.DailyHashtags
import br.pucpr.dailypuzzle.util.DateUtils
import br.pucpr.dailypuzzle.util.HashtagPuzzle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HashtagState(
    val puzzle: HashtagPuzzle,
    val grid: List<List<Char>>,
    val selected: Pair<Int, Int>? = null,
    val movesLeft: Int = DailyHashtags.MAX_MOVES,
    val gameStatus: GameStatus = GameStatus.PLAYING
)

@HiltViewModel
class HashtagViewModel @Inject constructor(
    private val repository: GameRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val date: String = savedStateHandle.get<String>(Routes.ARG_DATE)
        ?.takeIf { it.isNotBlank() }
        ?: DateUtils.today()

    private var resultSaved = false

    private val _alreadyPlayed = MutableStateFlow<Boolean?>(null)
    val alreadyPlayed: StateFlow<Boolean?> = _alreadyPlayed.asStateFlow()

    init {
        viewModelScope.launch {
            _alreadyPlayed.value = repository.getProgress(Game.HASHTAG, date) != null
        }
    }

    private val puzzle = DailyHashtags.forDate(date)

    private val _state = MutableStateFlow(
        HashtagState(puzzle = puzzle, grid = DailyHashtags.scramble(puzzle, date))
    )
    val state: StateFlow<HashtagState> = _state.asStateFlow()

    private fun isMovable(s: HashtagState, cell: Pair<Int, Int>): Boolean =
        s.puzzle.isActive(cell.first, cell.second) &&
                s.puzzle.statuses(s.grid)[cell] != LetterStatus.CORRECT
    fun onCellTap(cell: Pair<Int, Int>) {
        val s = _state.value
        if (s.gameStatus != GameStatus.PLAYING || !isMovable(s, cell)) return
        val selected = s.selected
        when {
            selected == null -> _state.update { it.copy(selected = cell) }
            selected == cell -> _state.update { it.copy(selected = null) }
            else -> swap(selected, cell)
        }
    }

    fun onDragStart(cell: Pair<Int, Int>) {
        val s = _state.value
        if (s.gameStatus != GameStatus.PLAYING || !isMovable(s, cell)) return
        _state.update { it.copy(selected = cell) }
    }

    fun onDrop(from: Pair<Int, Int>?, to: Pair<Int, Int>?) {
        if (from == null || to == null) {
            _state.update { it.copy(selected = null) }
            return
        }
        swap(from, to)
    }

    private fun swap(a: Pair<Int, Int>, b: Pair<Int, Int>) {
        val s = _state.value
        val letterA = s.grid[a.first][a.second]
        val letterB = s.grid[b.first][b.second]
        if (s.gameStatus != GameStatus.PLAYING || a == b ||
            !isMovable(s, a) || !isMovable(s, b) || letterA == letterB
        ) {
            _state.update { it.copy(selected = null) }
            return
        }

        val newGrid = s.grid.mapIndexed { r, row ->
            row.mapIndexed { c, ch ->
                when (r to c) {
                    a -> letterB
                    b -> letterA
                    else -> ch
                }
            }
        }
        val movesLeft = s.movesLeft - 1
        val status = when {
            s.puzzle.isSolved(newGrid) -> GameStatus.WON
            movesLeft == 0 -> GameStatus.LOST
            else -> GameStatus.PLAYING
        }

        _state.update {
            it.copy(grid = newGrid, selected = null, movesLeft = movesLeft, gameStatus = status)
        }

        if (status != GameStatus.PLAYING) {
            saveResult(
                won = status == GameStatus.WON,
                attempts = DailyHashtags.MAX_MOVES - movesLeft
            )
        }
    }

    private fun saveResult(won: Boolean, attempts: Int) {
        if (resultSaved) return
        resultSaved = true
        viewModelScope.launch {
            repository.saveResult(
                game = Game.HASHTAG,
                won = won,
                attempts = attempts,
                date = date
            )
        }
    }
}