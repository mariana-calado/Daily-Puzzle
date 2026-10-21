package br.pucpr.dailypuzzle.model

import androidx.compose.ui.graphics.Color

enum class LetterStatus(val color: Color) {
    EMPTY(Color.Transparent),
    TYPED(Color.DarkGray),
    CORRECT(Color(0xFF3A8F5C)),
    PRESENT(Color(0xFFB7892B)),
    ABSENT(Color(0xFF787C7E))
}

enum class GameStatus {
    PLAYING, WON, LOST
}

data class Letter(
    val char: Char = ' ',
    val status: LetterStatus = LetterStatus.EMPTY
)

data class TermoState(
    val grid: List<List<Letter>> = List(6) { List(5) { Letter() } },
    val currentRow: Int = 0,
    val currentCol: Int = 0,
    val targetWord: String = "BOLAS", // Palavra alvo de 5 letras
    val gameStatus: GameStatus = GameStatus.PLAYING
)