package br.pucpr.dailypuzzle.ui.games.sudoku

import androidx.lifecycle.ViewModel
import br.pucpr.dailypuzzle.model.SudokuCell
import br.pucpr.dailypuzzle.model.SudokuState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SudokuViewModel : ViewModel() {

    private val _state = MutableStateFlow(newGame())
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
        _state.value = newGame()
    }

    private companion object {
        // Um dígito por célula, 0 = vazia
        const val PUZZLE =
            "530070000" +
                    "600195000" +
                    "098000060" +
                    "800060003" +
                    "400803001" +
                    "700020006" +
                    "060000280" +
                    "000419005" +
                    "000080079"

        const val SOLUTION =
            "534678912" +
                    "672195348" +
                    "198342567" +
                    "859761423" +
                    "426853791" +
                    "713924856" +
                    "961537284" +
                    "287419635" +
                    "345286179"

        fun newGame(): SudokuState {
            val grid = List(9) { r ->
                List(9) { c ->
                    val v = PUZZLE[r * 9 + c].digitToInt()
                    SudokuCell(value = v, isGiven = v != 0)
                }
            }
            val solution = List(9) { r -> List(9) { c -> SOLUTION[r * 9 + c].digitToInt() } }
            return SudokuState(grid = grid, solution = solution)
        }
    }
}