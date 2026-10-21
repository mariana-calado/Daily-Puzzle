package br.pucpr.dailypuzzle.util

import br.pucpr.dailypuzzle.model.LetterStatus
import java.time.LocalDate
import java.util.Random

/**
 * Tabuleiro 5x5 em forma de #: linhas 1 e 3, colunas 1 e 3 (16 casas).
 *
 * words = [linha de cima, linha de baixo, coluna da esquerda, coluna da direita]
 * Cruzamentos: cima[1]=esq[1], cima[3]=dir[1], baixo[1]=esq[3], baixo[3]=dir[3].
 */
data class HashtagPuzzle(val words: List<String>) {

    companion object {
        const val SIZE = 5
    }

    private val lines: List<List<Pair<Int, Int>>> = listOf(
        List(SIZE) { 1 to it },
        List(SIZE) { 3 to it },
        List(SIZE) { it to 1 },
        List(SIZE) { it to 3 }
    )

    fun isActive(r: Int, c: Int): Boolean = r == 1 || r == 3 || c == 1 || c == 3

    fun cells(): List<Pair<Int, Int>> =
        (0 until SIZE).flatMap { r -> (0 until SIZE).filter { c -> isActive(r, c) }.map { c -> r to c } }

    fun solution(r: Int, c: Int): Char = when {
        r == 1 -> words[0][c]
        r == 3 -> words[1][c]
        c == 1 -> words[2][r]
        else -> words[3][r]
    }

    fun solvedGrid(): List<List<Char>> =
        List(SIZE) { r -> List(SIZE) { c -> if (isActive(r, c)) solution(r, c) else ' ' } }

    fun isSolved(grid: List<List<Char>>): Boolean =
        cells().all { (r, c) -> grid[r][c] == solution(r, c) }

    /** Cor de cada casa (regra do Wordle, aplicada em cada uma das 4 palavras). */
    fun statuses(grid: List<List<Char>>): Map<Pair<Int, Int>, LetterStatus> {
        val result = mutableMapOf<Pair<Int, Int>, LetterStatus>()
        fun rank(s: LetterStatus) = when (s) {
            LetterStatus.CORRECT -> 2
            LetterStatus.PRESENT -> 1
            else -> 0
        }
        lines.forEachIndexed { i, cells ->
            val answer = words[i]
            // letras da resposta que ainda não foram acertadas (disponíveis para amarelo)
            val remaining = mutableMapOf<Char, Int>()
            cells.forEachIndexed { k, (r, c) ->
                if (grid[r][c] != answer[k]) remaining[answer[k]] = (remaining[answer[k]] ?: 0) + 1
            }
            cells.forEachIndexed { k, cell ->
                val ch = grid[cell.first][cell.second]
                val status = when {
                    ch == answer[k] -> LetterStatus.CORRECT
                    (remaining[ch] ?: 0) > 0 -> {
                        remaining[ch] = remaining[ch]!! - 1
                        LetterStatus.PRESENT
                    }
                    else -> LetterStatus.ABSENT
                }
                val previous = result[cell]
                if (previous == null || rank(status) > rank(previous)) result[cell] = status
            }
        }
        return result
    }
}

object DailyHashtags {

    const val MAX_MOVES = 12
    private const val SCRAMBLE_SWAPS = 9 // sempre resolvível em até 9 movimentos

    private val all = listOf(
        HashtagPuzzle(listOf("CASAS", "BOLAS", "FAROL", "VAGAS")),
        HashtagPuzzle(listOf("PATOS", "MESAS", "PAPEL", "COPAS")),
        HashtagPuzzle(listOf("TELAS", "DADOS", "MEDAL", "GATOS")),
        HashtagPuzzle(listOf("SOLAR", "MOTOR", "BOLOS", "LAGOS")),
        HashtagPuzzle(listOf("FILAS", "NOVAS", "PISOS", "SALAS")),
        HashtagPuzzle(listOf("RISOS", "CEDER", "VIDEO", "COMER")),
        HashtagPuzzle(listOf("VOTOS", "DEDOS", "POTES", "POLOS"))
    )

    private fun dayOf(date: String): Long =
        runCatching { LocalDate.parse(date).toEpochDay() }.getOrElse { date.hashCode().toLong() }

    /** Um tabuleiro por dia: o mesmo dia sempre dá o mesmo jogo. */
    fun forDate(date: String): HashtagPuzzle =
        all[Math.floorMod(dayOf(date), all.size.toLong()).toInt()]

    /** Embaralha a partir da solução, sempre igual para a mesma data. */
    fun scramble(puzzle: HashtagPuzzle, date: String): List<List<Char>> {
        val random = Random(dayOf(date) * 31L + 17L)
        val cells = puzzle.cells()
        while (true) {
            val grid = puzzle.solvedGrid().map { it.toMutableList() }
            var swaps = 0
            while (swaps < SCRAMBLE_SWAPS) {
                val a = cells[random.nextInt(cells.size)]
                val b = cells[random.nextInt(cells.size)]
                if (grid[a.first][a.second] == grid[b.first][b.second]) continue
                val tmp = grid[a.first][a.second]
                grid[a.first][a.second] = grid[b.first][b.second]
                grid[b.first][b.second] = tmp
                swaps++
            }
            if (!puzzle.isSolved(grid)) return grid
        }
    }
}