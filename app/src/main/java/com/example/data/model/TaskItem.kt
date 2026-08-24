package com.example.data.model

import com.squareup.moshi.JsonClass

enum class EnergyLevel {
    HIGH,    // High demand, strategic, creative, deep problem solving
    MEDIUM,  // Execution, writing, communications
    LOW      // Admin, sorting, logistics, micro-tasks
}

enum class PriorityType {
    FROG,    // Type A - Maximum 1 per day strictly!
    MEDIUM,  // Type B - 3 per day
    QUICK    // Type C - 5 quick wins per day
}

@JsonClass(generateAdapter = true)
data class TaskItem(
    val id: String,
    val title: String,
    val description: String = "",
    val energyLevel: EnergyLevel = EnergyLevel.MEDIUM,
    val priorityType: PriorityType = PriorityType.QUICK,
    val estimatedMinutes: Int = 30,
    val isCompleted: Boolean = false,
    val isFrog: Boolean = false,
    val scheduledTime: String? = null,
    val category: String = "General"
)
