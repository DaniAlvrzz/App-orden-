package com.example.data.model

data class DailySummary(
    val dateIso: String,
    val totalCount: Int,
    val completedCount: Int,
    val partialCount: Int,
    val ratio: Float
)

data class DailyRolloverResult(
    val previousDateIso: String,
    val currentDateIso: String,
    val daysDiff: Long = 1,
    val preservedHabitStreaksCount: Int = 0,
    val brokenHabitStreaksCount: Int = 0,
    val rolledOverTasksCount: Int = 0,
    val completedTasksCount: Int = 0,
    val completedHabitsCount: Int = 0,
    val completedMealsCount: Int = 0
)
