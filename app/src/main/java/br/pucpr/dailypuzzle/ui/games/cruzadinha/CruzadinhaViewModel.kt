package br.pucpr.dailypuzzle.ui.games.cruzadinha

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.pucpr.dailypuzzle.data.repository.GameRepository
import br.pucpr.dailypuzzle.model.Game
import br.pucpr.dailypuzzle.model.GameStatus
import br.pucpr.dailypuzzle.navigation.Routes
import br.pucpr.dailypuzzle.util.Cruzadinha
import br.pucpr.dailypuzzle.util.CruzadinhaEntry
import br.pucpr.dailypuzzle.util.DailyCruzadinhas
import br.pucpr.dailypuzzle.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CruzadinhaState(
    val puzzle: Cruzadinha,
    val input: List<List<Char>>,          // ' ' = vazio, '#' = casa preta
    val selRow: Int,
    val selCol: Int,
    val horizontal: Boolean,
    val wrong: Set<Pair<Int, Int>> = emptySet(),
    val gameStatus: GameStatus = GameStatus.PLAYING
) {
    val currentEntry: CruzadinhaEntry?
        get() = puzzle.entryAt(selRow, selCol, horizontal)
}

@HiltViewModel
class CruzadinhaViewModel @Inject constructor(
    private val repository: GameRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val date: String = savedStateHandle.get<String>(Routes.ARG_DATE)
        ?.takeIf { it.isNotBlank() }
        ?: DateUtils.today()

    private var resultSaved = false
    private var checks = 0

    // null = verificando; true = esse dia já foi jogado; false = pode jogar
    private val _alreadyPlayed = MutableStateFlow<Boolean?>(null)
    val alreadyPlayed: StateFlow<Boolean?> = _alreadyPlayed.asStateFlow()

    init {
        viewModelScope.launch {
            _alreadyPlayed.value = repository.getProgress(Game.CRUZADINHA_MINI, date) != null
        }
    }

    private val puzzle = DailyCruzadinhas.forDate(date)

    private val _state = MutableStateFlow(
        puzzle.entries.first().let { first ->
            CruzadinhaState(
                puzzle = puzzle,
                input = List(puzzle.height) { r ->
                    List(puzzle.width) { c -> if (puzzle.isBlock(r, c)) '#' else ' ' }
                },
                selRow = first.row,
                selCol = first.col,
                horizontal = first.horizontal
            )
        }
    )
    val state: StateFlow<CruzadinhaState> = _state.asStateFlow()

    fun onCellClick(r: Int, c: Int) {
        val s = _state.value
        if (s.gameStatus != GameStatus.PLAYING || s.puzzle.isBlock(r, c)) return
        val horizontal = when {
            // tocar de novo na mesma casa alterna a direção
            r == s.selRow && c == s.selCol ->
                if (s.puzzle.entryAt(r, c, !s.horizontal) != null) !s.horizontal else s.horizontal
            s.puzzle.entryAt(r, c, s.horizontal) != null -> s.horizontal
            else -> !s.horizontal
        }
        _state.update { it.copy(selRow = r, selCol = c, horizontal = horizontal) }
    }

    fun onToggleDirection() {
        val s = _state.value
        if (s.gameStatus != GameStatus.PLAYING) return
        if (s.puzzle.entryAt(s.selRow, s.selCol, !s.horizontal) != null) {
            _state.update { it.copy(horizontal = !it.horizontal) }
        }
    }

    fun onKeyPress(char: Char) {
        val s = _state.value
        if (s.gameStatus != GameStatus.PLAYING) return
        val newInput = s.input.setCell(s.selRow, s.selCol, char)
        val (nr, nc) = neighborInEntry(s, +1)
        val won = isSolved(s.puzzle, newInput)
        _state.update {
            it.copy(
                input = newInput,
                selRow = nr,
                selCol = nc,
                wrong = it.wrong - (s.selRow to s.selCol),
                gameStatus = if (won) GameStatus.WON else GameStatus.PLAYING
            )
        }
        if (won) saveResult()
    }

    fun onBackspace() {
        val s = _state.value
        if (s.gameStatus != GameStatus.PLAYING) return
        val current = s.selRow to s.selCol
        // se a casa atual está vazia, apaga a anterior
        val target = if (s.input[current.first][current.second] != ' ') current else neighborInEntry(s, -1)
        _state.update {
            it.copy(
                input = it.input.setCell(target.first, target.second, ' '),
                selRow = target.first,
                selCol = target.second,
                wrong = it.wrong - target
            )
        }
    }

    /** "Checar": marca em vermelho as letras erradas. */
    fun onCheck() {
        val s = _state.value
        if (s.gameStatus != GameStatus.PLAYING) return
        checks++
        val wrong = buildSet {
            for (r in 0 until s.puzzle.height) for (c in 0 until s.puzzle.width) {
                val ch = s.input[r][c]
                if (!s.puzzle.isBlock(r, c) && ch != ' ' && ch != s.puzzle.solution(r, c)) add(r to c)
            }
        }
        _state.update { it.copy(wrong = wrong) }
    }

    private fun neighborInEntry(s: CruzadinhaState, delta: Int): Pair<Int, Int> {
        val cur = s.selRow to s.selCol
        val cells = s.currentEntry?.cells() ?: return cur
        return cells.getOrNull(cells.indexOf(cur) + delta) ?: cur
    }

    private fun List<List<Char>>.setCell(r: Int, c: Int, ch: Char): List<List<Char>> =
        mapIndexed { ri, row ->
            if (ri == r) row.mapIndexed { ci, old -> if (ci == c) ch else old } else row
        }

    private fun isSolved(p: Cruzadinha, input: List<List<Char>>): Boolean {
        for (r in 0 until p.height) for (c in 0 until p.width) {
            if (!p.isBlock(r, c) && input[r][c] != p.solution(r, c)) return false
        }
        return true
    }

    private fun saveResult() {
        if (resultSaved) return
        resultSaved = true
        viewModelScope.launch {
            repository.saveResult(
                game = Game.CRUZADINHA_MINI,
                won = true,
                attempts = checks + 1, // nº de vezes que apertou "Checar" + a conclusão
                date = date
            )
        }
    }
}