package br.pucpr.dailypuzzle.ui.theme

import androidx.compose.ui.graphics.Color
import br.pucpr.dailypuzzle.model.Game

fun gameAccent(game: Game): Color = when (game) {
    Game.TERMO -> AccentTermo
    Game.CACA_PALAVRAS -> AccentCacaPalavras
    Game.HASHTAG -> AccentHashtag
    Game.SUDOKU -> AccentSudoku
    Game.CRUZADINHA_MINI -> AccentCruzadinha
}

fun gameEmoji(game: Game): String = when (game) {
    Game.TERMO -> "🟩"
    Game.CACA_PALAVRAS -> "🔎"
    Game.HASHTAG -> "🔤"
    Game.SUDOKU -> "🔢"
    Game.CRUZADINHA_MINI -> "✏️"
}
