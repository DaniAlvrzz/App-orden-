package com.example.data.model

import com.squareup.moshi.JsonClass

enum class CircadianAnchor(val label: String, val idealWindow: String) {
    MORNING_LIGHT("Morning Photonic Anchor (Sunlight)", "Within 30m of waking"),
    HYDRATION_ELECTROLYTES("Hydration & Mineral Charge", "Upon waking + Post-lunch"),
    ZONE_2_MOVEMENT("Zone 2 Aerobic Movement", "Midday or Post-Deep Work"),
    CAFFEINE_CUTOFF("Adenosine/Caffeine Cutoff", "8-10 hours before sleep"),
    DIGITAL_SUNSET("Digital Sunset & Melatonin Prep", "60m before bed"),
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
    val lastCompletedDate: String = ""
)

