package com.example.data.model

import com.squareup.moshi.JsonClass

enum class BlockType(val displayName: String, val iconName: String) {
    DEEP_WORK("Deep Work", "psychology"),
    MEETING("Meeting / Sync", "groups"),
    ADMIN_SLOT("Admin Slot", "task_alt"),
    MEAL("Relational Meal", "restaurant"),
    HABIT_ANCHOR("Habit Anchor", "wb_sunny"),
    COGNITIVE_RECOVERY_BUFFER("Recovery Buffer", "spa"),
    SLEEP("Biological Sleep", "bedtime")
}

@JsonClass(generateAdapter = true)
data class TimeBlock(
    val id: String,
    val startTime: String, // e.g. "08:30"
    val endTime: String,   // e.g. "10:30"
    val blockType: BlockType,
    val title: String,
    val isCompleted: Boolean = false,
    val linkedTaskId: String? = null,
    val notes: String = ""
)
