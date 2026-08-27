package com.example.data.util

import com.example.ui.i18n.AppLanguage
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object AetherDateUtils {
    fun getTodayIso(): String {
        return LocalDate.now().toString()
    }

    fun getFormattedToday(language: AppLanguage): String {
        val locale = if (language == AppLanguage.SPANISH) Locale.forLanguageTag("es-ES") else Locale.US
        val pattern = if (language == AppLanguage.SPANISH) "EEEE, d 'de' MMMM" else "EEEE, MMMM d"
        val formatter = DateTimeFormatter.ofPattern(pattern, locale)
        val formatted = LocalDate.now().format(formatter)
        return formatted.replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
    }

    fun daysBetween(fromIso: String, toIso: String): Long {
        return try {
            val fromDate = LocalDate.parse(fromIso)
            val toDate = LocalDate.parse(toIso)
            java.time.temporal.ChronoUnit.DAYS.between(fromDate, toDate)
        } catch (e: Exception) {
            1L
        }
    }

    /**
     * Checks if at least one Monday has occurred in the interval (fromIso, toIso],
     * i.e., from fromDate (exclusive) to toDate (inclusive).
     */
    fun hasMondayBetween(fromIso: String, toIso: String): Boolean {
        return try {
            val fromDate = LocalDate.parse(fromIso)
            val toDate = LocalDate.parse(toIso)
            if (!toDate.isAfter(fromDate)) return false
            val firstMondayAfterFrom = fromDate.plusDays(1).with(java.time.temporal.TemporalAdjusters.nextOrSame(java.time.DayOfWeek.MONDAY))
            !firstMondayAfterFrom.isAfter(toDate)
        } catch (e: Exception) {
            false
        }
    }
}
