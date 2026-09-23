package br.pucpr.dailypuzzle.model

enum class Game(
    val title: String,
    val description: String
) {
    TERMO("Termo", "Descubra a palavra de 5 letras em 6 tentativas"),
    CACA_PALAVRAS("Caça-palavras", "Encontre as palavras escondidas na grade"),
    HASHTAG("Hashtag", "Reordene as letras para formar a palavra"),
    SUDOKU("Sudoku", "Preencha a grade 9x9 sem repetir números"),
    CRUZADINHA_MINI("Cruzadinha Mini", "Uma cruzadinha rápida para resolver no dia")
}
