package br.pucpr.dailypuzzle.model

data class SudokuCell(
    val value: Int = 0,
    val isGiven: Boolean = false,
    val isError: Boolean = false
)

data class SudokuState(
    val grid: List<List<SudokuCell>>,
    val solution: List<List<Int>>,
    val selectedRow: Int? = null,
    val selectedCol: Int? = null,
    val mistakes: Int = 0,
    val isCompleted: Boolean = false
)