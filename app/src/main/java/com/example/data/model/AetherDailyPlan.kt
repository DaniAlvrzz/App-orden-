package com.example.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Top3Priorities(
    val frog_task: TaskItem?,
    val medium_tasks: List<TaskItem> = emptyList(),
    val quick_wins: List<TaskItem> = emptyList()
)

@JsonClass(generateAdapter = true)
data class SuggestedEnergyMenu(
    val high_energy_backlog: List<TaskItem> = emptyList(),
    val medium_energy_backlog: List<TaskItem> = emptyList(),
    val low_energy_backlog: List<TaskItem> = emptyList()
)

@JsonClass(generateAdapter = true)
data class DailyMealsPlan(
    val breakfast: MealItem? = null,
    val lunch: MealItem? = null,
    val dinner: MealItem? = null,
    val snack: MealItem? = null
)

@JsonClass(generateAdapter = true)
data class AetherDailyPlan(
    val date: String, // e.g. "2026-08-22"
    val biometric_baseline: BiometricBaseline,
    val top_3_priorities_1_3_5: Top3Priorities,
    val time_blocks: List<TimeBlock>,
    val suggested_tasks_by_energy_menu: SuggestedEnergyMenu,
    val daily_meals: DailyMealsPlan,
    val deep_work_minutes_allocated: Int = 120, // Cognitive ceiling max 210m (3.5h)
    val max_cognitive_ceiling_minutes: Int = 210,
    val active_mode_label: String = "Balanced Circadian Mode",
    val cognitive_reframing_message: String = "Remember: Action follows physiological readiness. Protect your energy envelope."
)
