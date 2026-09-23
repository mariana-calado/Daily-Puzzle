package br.pucpr.dailypuzzle.navigation

import br.pucpr.dailypuzzle.model.Game

/**
 * Nomes de todas as rotas do app, inclusive das telas que ainda não existem.
 * Faz parte do "contrato" da Fase 0: o NavGraph e as telas usam só estas constantes,
 * nunca strings soltas.
 */
object Routes {
    const val HOME = "home"
    const val STATS = "stats"

    // Uma rota por jogo
    const val TERMO = "termo"
    const val CACA_PALAVRAS = "caca_palavras"
    const val HASHTAG = "hashtag"
    const val SUDOKU = "sudoku"
    const val CRUZADINHA_MINI = "cruzadinha_mini"

    // Resultado recebe qual jogo terminou; o resto (venceu, tentativas) é lido do Room
    const val ARG_GAME_ID = "gameId"
    const val RESULT = "result/{$ARG_GAME_ID}" // vira "result/{gameId}"

    fun result(game: Game): String = "result/${game.name}"

    // O "when" cobre todos os valores do enum: se um dia entrar um 6º jogo,
    // o código não compila até alguém dar uma rota para ele.
    fun forGame(game: Game): String = when (game) {
        Game.TERMO -> TERMO
        Game.CACA_PALAVRAS -> CACA_PALAVRAS
        Game.HASHTAG -> HASHTAG
        Game.SUDOKU -> SUDOKU
        Game.CRUZADINHA_MINI -> CRUZADINHA_MINI
    }
}
