package br.pucpr.dailypuzzle.util

import java.time.LocalDate
import kotlin.random.Random

/**
 * Escolhe a palavra do Termo a partir da data (formato "yyyy-MM-dd").
 *
 * A mesma data sempre gera a mesma palavra, em qualquer aparelho, e dias consecutivos
 * nunca repetem palavra até a lista inteira ser percorrida. Todas as palavras estão em
 * maiúsculas e sem acento, para combinar com o teclado A–Z.
 */
object DailyWords {

    // Embaralhada com semente fixa para a sequência não ficar em ordem alfabética.
    // Não mude a semente depois de lançar, senão as palavras de todos os dias mudam.
    private val words: List<String> = listOf(
        "AMIGO", "AMIGA", "ANTES", "ARROZ", "BANCO", "BEIJO", "BRISA", "CAMPO", "CANTO", "CARTA",
        "CERCA", "CHAVE", "CHUVA", "CINCO", "CLARO", "COISA", "CORPO", "CRIME", "CURVA", "DENTE",
        "DEUSA", "DOCES", "DUELO", "ELITE", "ENTRE", "FALAR", "FAROL", "FESTA", "FILHO", "FIRME",
        "FLORA", "FOLHA", "FORMA", "FRASE", "FRUTA", "GANHO", "GATOS", "GENTE", "GOSTO", "GRAMA",
        "GRAVE", "HOTEL", "IDEIA", "JOGOS", "JOVEM", "LEITE", "LETRA", "LIVRE", "LIVRO", "LUGAR",
        "MACIO", "MAGIA", "MANGA", "MARCA", "MESMO", "METRO", "MOTOR", "MUITO", "MUNDO", "NAVIO",
        "NOITE", "NOVOS", "NUVEM", "OBRAS", "OLHAR", "ONTEM", "ORDEM", "OUTRO", "PAPEL", "PEDRA",
        "PEIXE", "PERTO", "PIZZA", "PLANO", "POEMA", "PONTE", "PORTA", "POUCO", "PRAIA", "PRAZO",
        "PRETO", "PROVA", "QUASE", "QUEDA", "REGRA", "RISCO", "ROUPA", "SABOR", "SALTO", "SENHA",
        "SINAL", "SOBRE", "SONHO", "SORTE", "TARDE", "TEMPO", "TERMO", "TERRA", "TIGRE", "TINTA",
        "TORRE", "TROCA", "VALOR", "VENTO", "VIDRO", "VISTA", "VOZES", "ZEBRA"
    ).shuffled(Random(2026))

    fun forDate(date: String): String {
        val day = LocalDate.parse(date).toEpochDay()
        val index = Math.floorMod(day, words.size.toLong()).toInt()
        return words[index]
    }
}