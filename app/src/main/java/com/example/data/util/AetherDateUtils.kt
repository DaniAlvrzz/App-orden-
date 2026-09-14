package com.example.data.util

import com.example.ui.i18n.AppLanguage
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object AetherDateUtils {
    fun getTodayIso(): String {
        return LocalDate.now().toString()
    }

    /** Returns the ISO date for the day immediately before [dateIso]. */
    fun previousDay(dateIso: String): String {
        return try {
            LocalDate.parse(dateIso).minusDays(1).toString()
        } catch (e: Exception) {
            ""
        }
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

    /**
     * Returns every date strictly between fromIso and toIso — both excluded.
     * These are the days the app was never opened on, so they were never closed out by the
     * daily rollover: no completion logs, no daily summary, silent gaps in history.
     *
     * Capped at [maxDays] so that reopening the app after a very long absence can't trigger an
     * unbounded write storm (habits × days) inside a single transaction. Beyond the cap the
     * most recent [maxDays] days are returned, which is what history and streaks actually show.
     */
    fun datesBetweenExclusive(fromIso: String, toIso: String, maxDays: Int = 90): List<String> {
        return try {
            val fromDate = LocalDate.parse(fromIso)
            val toDate = LocalDate.parse(toIso)
            if (!toDate.isAfter(fromDate.plusDays(1))) return emptyList()

            val gap = mutableListOf<String>()
            var cursor = toDate.minusDays(1)
            while (cursor.isAfter(fromDate) && gap.size < maxDays) {
                gap.add(cursor.toString())
                cursor = cursor.minusDays(1)
            }
            gap.reversed()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
