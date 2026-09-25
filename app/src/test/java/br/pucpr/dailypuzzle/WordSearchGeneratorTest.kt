package br.pucpr.dailypuzzle

import br.pucpr.dailypuzzle.data.content.WordSearchGenerator
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.random.Random

class WordSearchGeneratorTest {

    private val bancos = listOf(
        "Frutas" to listOf("BANANA", "MANGA", "LIMAO", "GOIABA", "PERA", "CAJU"),
        "Animais" to listOf("GATO", "CAVALO", "TIGRE", "MACACO", "PATO", "ONCA"),
        "Cores" to listOf("VERDE", "AZUL", "ROXO", "PRETO", "BRANCO", "AMARELO"),
        "Escola" to listOf("LIVRO", "CADERNO", "LAPIS", "PROVA", "AULA", "MOCHILA"),
        "Praia" to listOf("AREIA", "ONDA", "BARCO", "CONCHA", "SOL", "MAR"),
        "Comida" to listOf("ARROZ", "FEIJAO", "QUEIJO", "PIZZA", "BOLO", "SALADA"),
        "Tecnologia" to listOf("MOUSE", "TECLADO", "CELULAR", "REDE", "CODIGO", "SENHA"),
        "Natureza" to listOf("ARVORE", "FLOR", "MONTANHA", "CHUVA", "FOLHA", "RIO"),
        "Esportes" to listOf("FUTEBOL", "TENIS", "NATACAO", "VOLEI", "JUDO", "CORRIDA"),
        "Casa" to listOf("SOFA", "MESA", "PORTA", "JANELA", "COZINHA", "CAMA")
    )

    @Test
    fun todasAsPalavrasCabemNaGrade() {
        bancos.forEach { (tema, palavras) ->
            repeat(50) { semente ->
                val puzzle = WordSearchGenerator.generate(tema, palavras, 8, Random(semente))
                assertEquals(
                    "tema=$tema semente=$semente",
                    palavras.size,
                    puzzle.words.size
                )
            }
        }
    }

    @Test
    fun letrasDaGradeFormamAsPalavras() {
        bancos.forEach { (tema, palavras) ->
            val puzzle = WordSearchGenerator.generate(tema, palavras, 8, Random(2026))
            puzzle.words.forEach { colocada ->
                val naGrade = colocada.cells
                    .map { celula -> puzzle.letters[celula.row][celula.col] }
                    .joinToString("")
                assertEquals("tema=$tema", colocada.word, naGrade)
            }
        }
    }

    @Test
    fun gradeFicaTotalmentePreenchida() {
        val puzzle = WordSearchGenerator.generate("Frutas", bancos[0].second, 8, Random(7))
        puzzle.letters.forEach { linha ->
            assertEquals(8, linha.size)
            linha.forEach { letra -> assertEquals(true, letra in 'A'..'Z') }
        }
    }
}
