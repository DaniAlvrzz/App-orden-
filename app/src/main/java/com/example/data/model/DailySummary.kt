package com.example.data.model

data class DailySummary(
    val dateIso: String,
    val totalCount: Int,
    val completedCount: Int,
    val partialCount: Int,
    val ratio: Float
)
