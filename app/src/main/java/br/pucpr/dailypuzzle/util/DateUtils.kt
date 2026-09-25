package br.pucpr.dailypuzzle.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateUtils {

    private val LOCALE: Locale = Locale.forLanguageTag("pt-BR")
    private val KEY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val LABEL_FORMATTER = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", LOCALE)

    fun today(): String = LocalDate.now().format(KEY_FORMATTER)

    fun yesterday(): String = LocalDate.now().minusDays(1).format(KEY_FORMATTER)

    fun todayLabel(): String =
        LocalDate.now().format(LABEL_FORMATTER).replaceFirstChar { it.titlecase(LOCALE) }
}
