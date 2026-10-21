package br.pucpr.dailypuzzle.ui.games.sudoku

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.pucpr.dailypuzzle.data.local.entity.GameProgressEntity
import br.pucpr.dailypuzzle.data.repository.GameRepository
import br.pucpr.dailypuzzle.model.Game
import br.pucpr.dailypuzzle.model.SudokuCell
import br.pucpr.dailypuzzle.model.SudokuState
import br.pucpr.dailypuzzle.navigation.Routes
import br.pucpr.dailypuzzle.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SudokuViewModel @Inject constructor(
    private val repository: GameRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val date: String = savedStateHandle.get<String>(Routes.ARG_DATE)
        ?.takeIf { it.isNotBlank() }
        ?: DateUtils.today()

    private var resultSaved = false

    private val _previousResult = MutableStateFlow<GameProgressEntity?>(null)
    val previousResult: StateFlow<GameProgressEntity?> = _previousResult.asStateFlow()

    private val _practice = MutableStateFlow(false)
    val practice: StateFlow<Boolean> = _practice.asStateFlow()

    val isToday: Boolean = date == DateUtils.today()
    val dateLabel: String = DateUtils.label(date)

    private val _alreadyPlayed = MutableStateFlow<Boolean?>(null)
    val alreadyPlayed: StateFlow<Boolean?> = _alreadyPlayed.asStateFlow()

    init {
        viewModelScope.launch {
            val progress = repository.getProgress(Game.SUDOKU, date)
            _previousResult.value = progress
            _alreadyPlayed.value = progress != null
        }
    }

    private val _state = MutableStateFlow(newGame(date))
    val state: StateFlow<SudokuState> = _state.asStateFlow()

    fun onCellSelected(row: Int, col: Int) {
        _state.update { it.copy(selectedRow = row, selectedCol = col) }
    }

    fun onNumberInput(number: Int) {
        val s = _state.value
        if (s.isCompleted) return
        val r = s.selectedRow ?: return
        val c = s.selectedCol ?: return
        val cell = s.grid[r][c]
        if (cell.isGiven) return

        val wrong = number != s.solution[r][c]
        val newGrid = s.grid.mapIndexed { ri, row ->
            if (ri == r) {
                row.mapIndexed { ci, old ->
                    if (ci == c) old.copy(value = number, isError = wrong) else old
                }
            } else row
        }
        val done = newGrid.indices.all { i ->
            newGrid[i].indices.all { j -> newGrid[i][j].value == s.solution[i][j] }
        }
        val countsAsMistake = wrong && cell.value != number

        _state.update {
            it.copy(
                grid = newGrid,
                mistakes = if (countsAsMistake) it.mistakes + 1 else it.mistakes,
                isCompleted = done
            )
        }

        if (done) saveResult()
    }

    fun onPlayAgain() {
        resultSaved = true
        _practice.value = true
        _state.value = newGame(date)
    }

    private fun saveResult() {
        if (resultSaved) return
        resultSaved = true
        val mistakes = _state.value.mistakes
        viewModelScope.launch {
            repository.saveResult(
                game = Game.SUDOKU,
                won = true,
                attempts = mistakes,
                date = date
            )
        }
    }

    fun onErase() {
        val s = _state.value
        if (s.isCompleted) return
        val r = s.selectedRow ?: return
        val c = s.selectedCol ?: return
        if (s.grid[r][c].isGiven) return

        val newGrid = s.grid.mapIndexed { ri, row ->
            if (ri == r) {
                row.mapIndexed { ci, old ->
                    if (ci == c) old.copy(value = 0, isError = false) else old
                }
            } else row
        }
        _state.update { it.copy(grid = newGrid) }
    }

    fun onReset() {
        _state.value = newGame(date)
    }

    private companion object {
        fun newGame(date: String): SudokuState {
            val board = SudokuGenerator.forDate(date)
            val grid = List(9) { r ->
                List(9) { c ->
                    val v = board.puzzle[r][c]
                    SudokuCell(value = v, isGiven = v != 0)
                }
            }
            return SudokuState(grid = grid, solution = board.solution)
        }
    }
}