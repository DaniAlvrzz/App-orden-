package com.example.data.model

import com.example.data.local.*

data class AetherFullBackup(
    val version: Int = 3,
    val exportedAt: Long = System.currentTimeMillis(),
    val exportedDate: String = "",
    val tasks: List<TaskEntity> = emptyList(),
    val timeBlocks: List<TimeBlockEntity> = emptyList(),
    val pantryItems: List<PantryEntity> = emptyList(),
    val meals: List<MealEntity> = emptyList(),
    val habits: List<HabitEntity> = emptyList(),
    val biometrics: List<BiometricEntity> = emptyList(),
    val completionLogs: List<CompletionLogEntity> = emptyList(),
    val dailySummaries: List<DailySummaryEntity> = emptyList()
)
