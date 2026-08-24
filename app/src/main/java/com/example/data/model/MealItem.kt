package com.example.data.model

import com.squareup.moshi.JsonClass

enum class MealSlot(val label: String) {
    BREAKFAST("Breakfast (Fuel)"),
    LUNCH("Lunch (Sustained Focus)"),
    DINNER("Dinner (Restorative)"),
    SNACK("Bio-Snack (Cognitive Boost)")
}

enum class BioGlycemicImpact(val label: String) {
    LOW_GLYCEMIC_FOCUS("Low-Glycemic (High Focus)"),
    MODERATE_STEADY("Moderate Steady Release"),
    DEEP_RECOVERY("Restorative & Easy Digestion")
}

@JsonClass(generateAdapter = true)
data class MealItem(
    val id: String,
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
