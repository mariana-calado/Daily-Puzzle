package br.pucpr.dailypuzzle.ui.games.termo

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.pucpr.dailypuzzle.data.local.entity.GameProgressEntity
import br.pucpr.dailypuzzle.data.repository.GameRepository
import br.pucpr.dailypuzzle.model.Game
import br.pucpr.dailypuzzle.model.GameStatus
import br.pucpr.dailypuzzle.model.Letter
import br.pucpr.dailypuzzle.model.LetterStatus
import br.pucpr.dailypuzzle.model.TermoState
import br.pucpr.dailypuzzle.util.DailyWords
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
class TermoViewModel @Inject constructor(
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
            val progress = repository.getProgress(Game.TERMO, date)
            _previousResult.value = progress
            _alreadyPlayed.value = progress != null
        }
    }

    private val _state = MutableStateFlow(TermoState(targetWord = DailyWords.forDate(date)))
    val state: StateFlow<TermoState> = _state.asStateFlow()

    fun onKeyPress(char: Char) {
        val currentState = _state.value
        if (currentState.gameStatus != GameStatus.PLAYING) return
        if (currentState.currentCol < 5) {
            val newGrid = currentState.grid.mapIndexed { rowIndex, row ->
                if (rowIndex == currentState.currentRow) {
                    row.mapIndexed { colIndex, letter ->
                        if (colIndex == currentState.currentCol) Letter(char, LetterStatus.TYPED) else letter
                    }
                } else row
            }
            _state.update { it.copy(grid = newGrid, currentCol = it.currentCol + 1) }
        }
    }

    fun onBackspace() {
        val currentState = _state.value
        if (currentState.gameStatus != GameStatus.PLAYING) return
        if (currentState.currentCol > 0) {
            val newCol = currentState.currentCol - 1
            val newGrid = currentState.grid.mapIndexed { rowIndex, row ->
                if (rowIndex == currentState.currentRow) {
                    row.mapIndexed { colIndex, letter ->
                        if (colIndex == newCol) Letter(' ', LetterStatus.EMPTY) else letter
                    }
                } else row
            }
            _state.update { it.copy(grid = newGrid, currentCol = newCol) }
        }
    }

    fun onEnter() {
        val currentState = _state.value
        if (currentState.gameStatus != GameStatus.PLAYING) return
        if (currentState.currentCol == 5) {
            validateRow()
        }
    }

    private fun validateRow() {
        val currentState = _state.value
        val targetWord = currentState.targetWord
        val currentRow = currentState.currentRow
        val guessedChars = currentState.grid[currentRow].map { it.char }
        val guessedWord = guessedChars.joinToString("")


        val newRow = currentState.grid[currentRow].mapIndexed { index, letter ->
            val status = when {
                letter.char == targetWord[index] -> LetterStatus.CORRECT
                targetWord.contains(letter.char) -> LetterStatus.PRESENT
                else -> LetterStatus.ABSENT
            }
            letter.copy(status = status)
        }

        val newGrid = currentState.grid.toMutableList()
        newGrid[currentRow] = newRow

        val newGameStatus = when {
            guessedWord == targetWord -> GameStatus.WON
            currentRow == 5 -> GameStatus.LOST
            else -> GameStatus.PLAYING
        }

        _state.update {
            it.copy(
                grid = newGrid,
                currentRow = if (newGameStatus == GameStatus.PLAYING) currentRow + 1 else currentRow,
                currentCol = 0,
                gameStatus = newGameStatus
            )
        }


        if (newGameStatus != GameStatus.PLAYING) {
            saveResult(
                won = newGameStatus == GameStatus.WON,
                attempts = currentRow + 1
            )
        }
    }

    fun onPlayAgain() {
        resultSaved = true
        _practice.value = true
        _state.value = TermoState(targetWord = DailyWords.forDate(date))
    }

    private fun saveResult(won: Boolean, attempts: Int) {
        if (resultSaved) return
        resultSaved = true
        viewModelScope.launch {
            repository.saveResult(
                game = Game.TERMO,
                won = won,
                attempts = attempts,
                date = date
            )
        }
    }
}