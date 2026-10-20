package br.pucpr.dailypuzzle.ui.games.sudoku

import java.time.LocalDate
import kotlin.random.Random

/**
 * Gera o tabuleiro do dia a partir da data (formato "yyyy-MM-dd").
 *
 * A mesma data sempre produz o mesmo tabuleiro. O processo é:
 * 1. parte de uma solução válida conhecida e a embaralha (bandas, linhas, colunas e dígitos),
 *    o que sempre resulta em outra solução válida;
 * 2. remove células em ordem aleatória, mas só mantém a remoção se o puzzle continuar
 *    com exatamente uma solução.
 */
object SudokuGenerator {

    class Board(
        val puzzle: List<List<Int>>,   // 0 = vazia
        val solution: List<List<Int>>
    )

    private const val CELLS_TO_REMOVE = 46   // ~35 dicas; aumente para ficar mais difícil

    fun forDate(date: String): Board {
        val day = LocalDate.parse(date).toEpochDay()
        val rnd = Random(day * 1_000_003L + 17L)

        val solution = shuffledSolution(rnd)
        val grid = IntArray(81) { solution[it / 9][it % 9] }

        var removed = 0
        for (index in (0 until 81).shuffled(rnd)) {
            if (removed >= CELLS_TO_REMOVE) break
            val backup = grid[index]
            grid[index] = 0
            if (countSolutions(grid, limit = 2) == 1) {
                removed++
            } else {
                grid[index] = backup
            }
        }

        return Board(
            puzzle = List(9) { r -> List(9) { c -> grid[r * 9 + c] } },
            solution = solution
        )
    }

    private fun shuffledSolution(rnd: Random): List<List<Int>> {
        val rows = shuffledAxis(rnd)
        val cols = shuffledAxis(rnd)
        val digits = (1..9).shuffled(rnd)
        return List(9) { r ->
            List(9) { c ->
                val base = (3 * (rows[r] % 3) + rows[r] / 3 + cols[c]) % 9
                digits[base]
            }
        }
    }

    // Embaralha as 3 bandas e as 3 linhas dentro de cada banda (vale também para colunas)
    private fun shuffledAxis(rnd: Random): List<Int> =
        (0..2).shuffled(rnd).flatMap { band ->
            (0..2).shuffled(rnd).map { band * 3 + it }
        }

    // Conta soluções até o limite (basta saber se é 0, 1 ou "mais de 1")
    private fun countSolutions(grid: IntArray, limit: Int): Int {
        var bestIndex = -1
        var bestMask = 0
        var bestCount = 10

        for (i in 0 until 81) {
            if (grid[i] != 0) continue
            val mask = candidates(grid, i)
            val n = Integer.bitCount(mask)
            if (n == 0) return 0
            if (n < bestCount) {
                bestIndex = i
                bestMask = mask
                bestCount = n
            }
        }
        if (bestIndex == -1) return 1 // sem células vazias: uma solução

        var total = 0
        for (d in 1..9) {
            if (bestMask and (1 shl d) == 0) continue
            grid[bestIndex] = d
            total += countSolutions(grid, limit - total)
            grid[bestIndex] = 0
            if (total >= limit) return total
        }
        return total
    }

    // Máscara de bits (1..9) dos dígitos ainda possíveis na célula
    private fun candidates(grid: IntArray, index: Int): Int {
        val r = index / 9
        val c = index % 9
        var used = 0
        for (k in 0 until 9) {
            used = used or (1 shl grid[r * 9 + k]) or (1 shl grid[k * 9 + c])
        }
        val br = r / 3 * 3
        val bc = c / 3 * 3
        for (dr in 0 until 3) {
            for (dc in 0 until 3) {
                used = used or (1 shl grid[(br + dr) * 9 + bc + dc])
            }
        }
        return 0x3FE and used.inv()
    }
}