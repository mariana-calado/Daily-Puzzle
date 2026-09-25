package br.pucpr.dailypuzzle.navigation

import br.pucpr.dailypuzzle.model.Game

object Routes {

    const val HOME = "home"
    const val STATS = "stats"

    const val ARG_GAME_ID = "gameId"
    const val ARG_DATE = "date"

    const val RESULT = "result/{$ARG_GAME_ID}?$ARG_DATE={$ARG_DATE}"

    fun pattern(game: Game): String = "${base(game)}?$ARG_DATE={$ARG_DATE}"

    fun forGame(game: Game, date: String? = null): String = base(game) + query(date)

    fun result(game: Game, date: String? = null): String = "result/${game.name}" + query(date)

    private fun query(date: String?): String = if (date.isNullOrBlank()) "" else "?$ARG_DATE=$date"

    private fun base(game: Game): String = when (game) {
        Game.TERMO -> "termo"
        Game.CACA_PALAVRAS -> "caca_palavras"
        Game.HASHTAG -> "hashtag"
        Game.SUDOKU -> "sudoku"
        Game.CRUZADINHA_MINI -> "cruzadinha_mini"
    }
}
