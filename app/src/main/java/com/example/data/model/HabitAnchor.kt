package com.example.data.model

import com.squareup.moshi.JsonClass

enum class CircadianAnchor(val label: String, val idealWindow: String) {
    MORNING("Morning", "Start of the day"),
    AFTERNOON("Afternoon", "Midday"),
    EVENING("Evening", "End of the day"),
    ALL_DAY("Throughout the Day / Flexible", "Any time during the day")
}

@JsonClass(generateAdapter = true)
data class HabitAnchor(
    val id: String,
    val title: String,
    val description: String,
    val anchor: CircadianAnchor,
    val isCompleted: Boolean = false,
    val streakDays: Int = 0,
    val graceDaysUsed: Int = 0,
    val reframingTip: String = "Biological consistency is a pattern of return, not perfection.",
    val maxGraceDaysPerPeriod: Int = 2,
    val graceDayLastUsedDate: String = "",
    val lastCompletedDate: String = "",
    val pendingStreakBeforeReset: Int = 0,
    /** Highest streak ever reached for this habit; survives a streak reset. */
    val bestStreakDays: Int = 0
)

