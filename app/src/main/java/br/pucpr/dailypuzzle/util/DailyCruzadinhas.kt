package br.pucpr.dailypuzzle.util

import java.time.LocalDate

data class CruzadinhaEntry(
    val number: Int,
    val horizontal: Boolean,
    val row: Int,
    val col: Int,
    val answer: String,
    val clue: String
) {
    val length: Int get() = answer.length

    fun cells(): List<Pair<Int, Int>> =
        List(length) { i -> if (horizontal) row to (col + i) else (row + i) to col }
}

/**
 * rows: grid com '#' nas casas pretas.
 * across/down: dicas na ordem de numeração (esquerda→direita, cima→baixo).
 */
data class Cruzadinha(
    val rows: List<String>,
    val across: List<String>,
    val down: List<String>
) {
    val height: Int = rows.size
    val width: Int = rows.first().length

    fun isBlock(r: Int, c: Int): Boolean = rows[r][c] == '#'
    fun solution(r: Int, c: Int): Char = rows[r][c]

    val entries: List<CruzadinhaEntry> by lazy { buildEntries() }

    val numbers: Map<Pair<Int, Int>, Int> by lazy {
        entries.associate { (it.row to it.col) to it.number }
    }

    fun entryAt(r: Int, c: Int, horizontal: Boolean): CruzadinhaEntry? =
        entries.firstOrNull { it.horizontal == horizontal && (r to c) in it.cells() }

    private fun buildEntries(): List<CruzadinhaEntry> {
        val result = mutableListOf<CruzadinhaEntry>()
        var number = 0
        var a = 0
        var d = 0
        for (r in 0 until height) {
            for (c in 0 until width) {
                if (isBlock(r, c)) continue
                val startsAcross = (c == 0 || isBlock(r, c - 1)) && c + 1 < width && !isBlock(r, c + 1)
                val startsDown = (r == 0 || isBlock(r - 1, c)) && r + 1 < height && !isBlock(r + 1, c)
                if (!startsAcross && !startsDown) continue
                number++
                if (startsAcross) {
                    var end = c
                    while (end + 1 < width && !isBlock(r, end + 1)) end++
                    result += CruzadinhaEntry(number, true, r, c, rows[r].substring(c, end + 1), across[a++])
                }
                if (startsDown) {
                    val sb = StringBuilder()
                    var rr = r
                    while (rr < height && !isBlock(rr, c)) { sb.append(rows[rr][c]); rr++ }
                    result += CruzadinhaEntry(number, false, r, c, sb.toString(), down[d++])
                }
            }
        }
        return result
    }
}

object DailyCruzadinhas {

    private val all = listOf(
        Cruzadinha(
            rows = listOf("CARTA", "A#O#R", "CAUSA", "A#P#R", "USADA"),
            across = listOf("Mensagem escrita enviada pelo correio", "Motivo ou origem de algo", "Que já foi utilizada"),
            down = listOf("Fruto de onde vem o chocolate", "Peça que vestimos", "Ave colorida de bico forte")
        ),
        Cruzadinha(
            rows = listOf("TERRA", "E#O#L", "CACAU", "L#H#N", "AMADA"),
            across = listOf("Planeta onde vivemos", "Semente usada para fazer chocolate", "Querida"),
            down = listOf("Peça do teclado", "Pedra de grande tamanho", "Estudante de escola")
        ),
        Cruzadinha(
            rows = listOf("FALTA", "A#O#L", "CACAU", "A#A#N", "SALSA"),
            across = listOf("Ausência de algo", "Semente usada para fazer chocolate", "Erva usada como tempero"),
            down = listOf("Talheres de corte", "Lugar", "Estudante de escola")
        )
    )

    /** Uma cruzadinha por dia: o mesmo dia sempre dá a mesma cruzadinha. */
    fun forDate(date: String): Cruzadinha {
        val day = runCatching { LocalDate.parse(date).toEpochDay() }
            .getOrElse { date.hashCode().toLong() }
        return all[Math.floorMod(day, all.size.toLong()).toInt()]
    }
}