package com.example.data.model

import com.squareup.moshi.JsonClass

enum class PantryCategory(val displayName: String) {
    PROTEIN("Proteins & Eggs"),
    CARB_BASE("Complex Carbs & Grains"),
    PRODUCE("Veggies & Greens"),
    HEALTHY_FAT("Fats & Oils"),
    SEASONING("Spices & Sauces")
}

@JsonClass(generateAdapter = true)
data class PantryItem(
    val id: String,
    val name: String,
    val category: PantryCategory,
    val inStock: Boolean = true,
    val isBatchBase: Boolean = false, // E.g., batch-cooked brown rice or shredded chicken
    val quantityDesc: String = "Sufficient"
)
