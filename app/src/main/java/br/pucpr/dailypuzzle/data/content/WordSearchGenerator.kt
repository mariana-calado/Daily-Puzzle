package br.pucpr.dailypuzzle.data.content

import br.pucpr.dailypuzzle.model.PlacedWord
import br.pucpr.dailypuzzle.model.WordCell
import br.pucpr.dailypuzzle.model.WordSearchPuzzle
import kotlin.random.Random

object WordSearchGenerator {

    private data class Placement(
        val row: Int,
        val col: Int,
        val stepRow: Int,
        val stepCol: Int
    )

    private val DIRECTIONS = listOf(
        0 to 1,
        1 to 0,
        1 to 1,
        -1 to 1
    )

    private const val ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private const val EMPTY = ' '
    private const val GRID_ATTEMPTS = 12

    fun generate(
        theme: String,
        words: List<String>,
        size: Int,
        random: Random
    ): WordSearchPuzzle {
        val ordered = words.sortedByDescending { it.length }
        var bestGrid = emptyGrid(size)
        var bestPlaced = emptyList<PlacedWord>()

        for (attempt in 0 until GRID_ATTEMPTS) {
            val grid = emptyGrid(size)
            val placed = mutableListOf<PlacedWord>()

            ordered.forEach { word ->
                val cells = place(word, grid, size, random)
                if (cells != null) {
                    placed += PlacedWord(word = word, cells = cells)
                }
            }

            if (attempt == 0 || placed.size > bestPlaced.size) {
                bestGrid = grid
                bestPlaced = placed.toList()
            }

            if (bestPlaced.size == ordered.size) break
        }

        for (row in 0 until size) {
            for (col in 0 until size) {
                if (bestGrid[row][col] == EMPTY) {
                    bestGrid[row][col] = ALPHABET[random.nextInt(ALPHABET.length)]
                }
            }
        }

        return WordSearchPuzzle(
            theme = theme,
            size = size,
            letters = bestGrid.map { it.toList() },
            words = bestPlaced.sortedBy { it.word }
        )
    }

    private fun emptyGrid(size: Int): Array<CharArray> = Array(size) { CharArray(size) { EMPTY } }

    private fun place(
        word: String,
        grid: Array<CharArray>,
        size: Int,
        random: Random
    ): List<WordCell>? {
        val options = mutableListOf<Placement>()

        DIRECTIONS.forEach { direction ->
            for (row in 0 until size) {
                for (col in 0 until size) {
                    val endRow = row + direction.first * (word.length - 1)
                    val endCol = col + direction.second * (word.length - 1)
                    if (endRow in 0 until size && endCol in 0 until size) {
                        options += Placement(
                            row = row,
                            col = col,
                            stepRow = direction.first,
                            stepCol = direction.second
                        )
                    }
                }
            }
        }

        options.shuffle(random)

        options.forEach { option ->
            val cells = word.indices.map { step ->
                WordCell(
                    row = option.row + option.stepRow * step,
                    col = option.col + option.stepCol * step
                )
            }

            val fits = cells.withIndex().all { (step, cell) ->
                val current = grid[cell.row][cell.col]
                current == EMPTY || current == word[step]
            }

            if (fits) {
                cells.forEachIndexed { step, cell -> grid[cell.row][cell.col] = word[step] }
                return cells
            }
        }

        return null
    }
}
