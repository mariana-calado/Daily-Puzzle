package br.pucpr.dailypuzzle.ui.theme

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import br.pucpr.dailypuzzle.R
import br.pucpr.dailypuzzle.model.Game

fun gameAccent(game: Game): Color = when (game) {
    Game.TERMO -> AccentTermo
    Game.CACA_PALAVRAS -> AccentCacaPalavras
    Game.HASHTAG -> AccentHashtag
    Game.SUDOKU -> AccentSudoku
    Game.CRUZADINHA_MINI -> AccentCruzadinha
}

@DrawableRes
fun gameIcon(game: Game): Int = when (game) {
    Game.TERMO -> R.drawable.ic_spellcheck
    Game.CACA_PALAVRAS -> R.drawable.ic_search
    Game.HASHTAG -> R.drawable.ic_tag
    Game.SUDOKU -> R.drawable.ic_grid_on
    Game.CRUZADINHA_MINI -> R.drawable.ic_edit
}
