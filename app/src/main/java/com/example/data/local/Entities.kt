package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.*

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String = "",
    val energyLevel: EnergyLevel = EnergyLevel.MEDIUM,
    val priorityType: PriorityType = PriorityType.QUICK,
    val estimatedMinutes: Int = 30,
    val isCompleted: Boolean = false,
    val isFrog: Boolean = false,
    val scheduledTime: String? = null,
    val category: String = "General",
    val isArchived: Boolean = false,
    val completedDate: String = "",
    val isPermanent: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "time_blocks")
data class TimeBlockEntity(
    @PrimaryKey val id: String,
    val startTime: String,
    val endTime: String,
    val blockType: BlockType,
    val title: String,
    val isCompleted: Boolean = false,
    val linkedTaskId: String? = null,
    val notes: String = "",
    val sortOrder: Int = 0
)

@Entity(tableName = "pantry_items")
data class PantryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val category: PantryCategory,
    val inStock: Boolean = true,
    val isBatchBase: Boolean = false,
    val quantityDesc: String = "Sufficient"
)

@Entity(tableName = "meals")
data class MealEntity(
    @PrimaryKey val id: String,
    val slot: MealSlot,
    val title: String,
    val description: String,
    val prepTimeMinutes: Int,
    val ingredients: List<String>,
    val usesBatchCookedBase: Boolean = false,
    val allIngredientsInStock: Boolean = true,
    val bioImpact: BioGlycemicImpact = BioGlycemicImpact.LOW_GLYCEMIC_FOCUS,
    val isCompleted: Boolean = false,
    val customSlotName: String? = null,
    val proteinGrams: Int = 0,
    val carbsGrams: Int = 0,
    val fatGrams: Int = 0,
    val caloriesKcal: Int = 0,
    val dateIso: String = ""
)

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey val id: String,
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
    val pendingStreakBeforeReset: Int = 0
)

@Entity(
    tableName = "biometrics",
    indices = [Index(value = ["date"], unique = true)]
)
data class BiometricEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String,
    val readinessScore: Int = 75,
    val computedReadinessScore: Int = 75,
    val perceivedEnergy: Int = 75,
    val sleepStartTime: String = "23:00",
    val sleepEndTime: String = "07:30",
    val sleepHours: Double = 7.5,
    val sleepInterruptionsCount: Int = 0,
    val sleepQuality: Int = 4,
    val wakeUpFeeling: WakeUpFeeling = WakeUpFeeling.RESTED,
    val currentEnergyLevel: Int = 7,
    val stressLevel: Int = 3,
    val motivationLevel: Int = 7,
    val caffeineIntake: CaffeineIntake = CaffeineIntake.MODERATE,
    val exerciseDone: Boolean = false,
    val mealRegularity: MealRegularity = MealRegularity.REGULAR,
    val mentalOverload: Boolean = false,
    val emotionalConcern: String = "",
    val chronotype: Chronotype = Chronotype.BEAR,
    val dynamicCognitiveCeilingMinutes: Int = 180,
    val cognitiveCeilingReason: String = "Línea base estándar ajustada por biometría.",
    val recoveryModeTriggered: Boolean = false,
    val graceDayActive: Boolean = false
)

@Entity(
    tableName = "completion_logs",
    indices = [Index(value = ["dateIso"])]
)
data class CompletionLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dateIso: String,
    val itemType: CompletionItemType,
    val itemId: String,
    val title: String,
    val status: CompletionStatus,
    val timestamp: Long
)

@Entity(tableName = "daily_summaries")
data class DailySummaryEntity(
    @PrimaryKey val dateIso: String,
    val totalCount: Int,
    val completedCount: Int,
    val partialCount: Int,
    val ratio: Float
)

@Entity(
    tableName = "ai_messages",
    indices = [Index(value = ["timestamp"]), Index(value = ["isFavorite"])]
)
data class AiMessageEntity(
    @PrimaryKey val id: String,
    val role: String,
    val content: String,
    val timestamp: Long,
    val isFavorite: Boolean = false
)

@Entity(tableName = "quick_notes")
data class QuickNoteEntity(
    @PrimaryKey val id: String,
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
    val isProcessed: Boolean = false,
    val convertedToTaskId: String? = null
)

@Entity(
    tableName = "focus_sessions",
    indices = [Index(value = ["timestamp"])]
)
data class FocusSessionEntity(
    @PrimaryKey val id: String,
    val taskTitle: String,
    val durationMinutes: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = true,
    val linkedTaskId: String? = null,
    val roundNumber: Int = 1
)
