package br.pucpr.dailypuzzle.navigation

import br.pucpr.dailypuzzle.model.Game

object Routes {
    const val HOME = "home"
    const val STATS = "stats"

    const val TERMO = "termo"
    const val CACA_PALAVRAS = "caca_palavras"
    const val HASHTAG = "hashtag"
    const val SUDOKU = "sudoku"
    const val CRUZADINHA_MINI = "cruzadinha_mini"

    const val ARG_GAME_ID = "gameId"
    const val RESULT = "result/{$ARG_GAME_ID}"

    fun result(game: Game): String = "result/${game.name}"

    fun forGame(game: Game): String = when (game) {
        Game.TERMO -> TERMO
        Game.CACA_PALAVRAS -> CACA_PALAVRAS
        Game.HASHTAG -> HASHTAG
        Game.SUDOKU -> SUDOKU
        Game.CRUZADINHA_MINI -> CRUZADINHA_MINI
    }
}
