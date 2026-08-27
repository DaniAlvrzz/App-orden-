package com.example.data.model

enum class CompletionItemType {
    TASK,
    HABIT,
    MEAL,
    TIME_BLOCK
}

enum class CompletionStatus {
    COMPLETED,
    PARTIAL,
    MISSED
}

data class CompletionLog(
    val id: Int = 0,
    val dateIso: String,
    val itemType: CompletionItemType,
    val itemId: String,
    val title: String,
    val status: CompletionStatus,
    val timestamp: Long
)

data class IndividualHistoryTarget(
    val id: String,
    val title: String,
    val itemType: CompletionItemType,
    val subtitle: String = "",
    val streakDays: Int = 0,
    val isPermanent: Boolean = false,
    val energyLevel: EnergyLevel? = null,
    val isFrog: Boolean = false,
    val anchor: CircadianAnchor? = null,
    val graceDaysUsed: Int = 0
)
