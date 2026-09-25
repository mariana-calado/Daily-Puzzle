package br.pucpr.dailypuzzle.model

data class WordCell(val row: Int, val col: Int)

data class PlacedWord(
    val word: String,
    val cells: List<WordCell>
)

data class WordSearchPuzzle(
    val theme: String,
    val size: Int,
    val letters: List<List<Char>>,
    val words: List<PlacedWord>
)
