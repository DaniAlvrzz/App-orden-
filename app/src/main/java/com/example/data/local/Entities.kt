package com.example.data.local

import androidx.room.Entity
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
    val isCompleted: Boolean = false
)

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val anchor: CircadianAnchor,
    val isCompleted: Boolean = false,
    val streakDays: Int = 5,
    val graceDaysUsed: Int = 1,
    val reframingTip: String = "Biological consistency is a pattern of return, not perfection."
)

@Entity(
    tableName = "biometrics",
    indices = [androidx.room.Index(value = ["date"], unique = true)]
)
data class BiometricEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String,
    val readinessScore: Int = 75,
    val perceivedEnergy: Int = 75,
    val sleepHours: Double = 7.5,
    val sleepQuality: Int = 4,
    val chronotype: Chronotype = Chronotype.BEAR,
    val recoveryModeTriggered: Boolean = false,
    val graceDayActive: Boolean = false
)
