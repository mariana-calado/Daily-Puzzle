package br.pucpr.dailypuzzle.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateUtils {

    private val LOCALE: Locale = Locale.forLanguageTag("pt-BR")
    private val KEY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val LABEL_FORMATTER = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", LOCALE)
    private val SHORT_FORMATTER = DateTimeFormatter.ofPattern("dd/MM", LOCALE)
    private val WEEKDAY_FORMATTER = DateTimeFormatter.ofPattern("EEEEE", LOCALE)

    fun today(): String = LocalDate.now().format(KEY_FORMATTER)

    fun yesterday(): String = daysAgo(1)

    fun daysAgo(count: Int): String =
        LocalDate.now().minusDays(count.toLong()).format(KEY_FORMATTER)

    fun lastDays(count: Int): List<String> = (count - 1 downTo 0).map { daysAgo(it) }

    fun nextDay(date: String): String =
        LocalDate.parse(date, KEY_FORMATTER).plusDays(1).format(KEY_FORMATTER)

    fun todayLabel(): String = label(today())

    fun label(date: String): String =
        LocalDate.parse(date, KEY_FORMATTER)
            .format(LABEL_FORMATTER)
            .replaceFirstChar { it.titlecase(LOCALE) }

    fun shortLabel(date: String): String =
        LocalDate.parse(date, KEY_FORMATTER).format(SHORT_FORMATTER)

    fun weekdayLetter(date: String): String =
        LocalDate.parse(date, KEY_FORMATTER).format(WEEKDAY_FORMATTER).uppercase(LOCALE)
}
