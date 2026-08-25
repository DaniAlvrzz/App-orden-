package com.example.ui.viewmodel

import com.example.data.model.*
import com.example.data.repository.MealRepository
import com.example.data.usecase.MealImportUseCase
import com.example.data.util.AetherDateUtils
import com.example.ui.i18n.AppLanguage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/**
 * Modular Delegate / Sub-ViewModel handling Meals, Macros, Pantry stock,
 * Batch Cooking Bases, and External AI Diet Importers.
 */
class NutritionDelegate(
    private val mealRepository: MealRepository,
    private val mealImportUseCase: MealImportUseCase,
    private val uiState: MutableStateFlow<AetherUiState>,
    private val scope: CoroutineScope,
    private val showFeedback: (String) -> Unit,
    private val unlockAchievement: (AchievementId) -> Unit
) {
    fun openAddMealDialog() {
        uiState.value = uiState.value.copy(showAddMealDialog = true)
    }

    fun closeAddMealDialog() {
        uiState.value = uiState.value.copy(showAddMealDialog = false)
    }

    fun openMealEditor(meal: MealItem) {
        uiState.value = uiState.value.copy(editingMeal = meal)
    }

    fun closeMealEditor() {
        uiState.value = uiState.value.copy(editingMeal = null)
    }

    fun openPantryAddDialog() {
        uiState.value = uiState.value.copy(showPantryAddDialog = true)
    }

    fun closePantryAddDialog() {
        uiState.value = uiState.value.copy(showPantryAddDialog = false)
    }

    fun openBatchBaseDialog() {
        uiState.value = uiState.value.copy(showBatchBaseDialog = true)
    }

    fun closeBatchBaseDialog() {
        uiState.value = uiState.value.copy(showBatchBaseDialog = false)
    }

    fun openPantryEditor(item: PantryItem) {
        uiState.value = uiState.value.copy(editingPantryItem = item)
    }

    fun closePantryEditor() {
        uiState.value = uiState.value.copy(editingPantryItem = null)
    }

    fun openImportDietDialog() {
        uiState.value = uiState.value.copy(showImportDietDialog = true)
    }

    fun closeImportDietDialog() {
        uiState.value = uiState.value.copy(showImportDietDialog = false)
    }

    fun addMeal(
        slot: MealSlot,
        title: String,
        description: String,
        prepTimeMinutes: Int,
        ingredients: List<String>,
        usesBatchCookedBase: Boolean,
        allIngredientsInStock: Boolean,
        bioImpact: BioGlycemicImpact,
        customSlotName: String? = null,
        proteinGrams: Int = 0,
        carbsGrams: Int = 0,
        fatGrams: Int = 0,
        caloriesKcal: Int = 0,
        dateIso: String = AetherDateUtils.getTodayIso()
    ) {
        scope.launch {
            mealRepository.addMeal(
                slot, title, description, prepTimeMinutes, ingredients,
                usesBatchCookedBase, allIngredientsInStock, bioImpact, customSlotName,
                proteinGrams, carbsGrams, fatGrams, caloriesKcal, dateIso
            )
            val isSpanish = uiState.value.currentLanguage == AppLanguage.SPANISH
            showFeedback(if (isSpanish) "Comida planificada correctamente." else "Meal planned successfully.")
            closeAddMealDialog()
        }
    }

    fun duplicateMeal(meal: MealItem, targetDateIso: String = AetherDateUtils.getTodayIso(), copySuffix: Boolean = false) {
        scope.launch {
            mealRepository.duplicateMeal(meal, targetDateIso, copySuffix)
            val isSpanish = uiState.value.currentLanguage == AppLanguage.SPANISH
            showFeedback(if (isSpanish) "Comida duplicada correctamente." else "Meal duplicated successfully.")
        }
    }

    fun updateMeal(meal: MealItem) {
        scope.launch {
            mealRepository.updateMeal(meal)
            closeMealEditor()
            val isSpanish = uiState.value.currentLanguage == AppLanguage.SPANISH
            showFeedback(if (isSpanish) "Comida actualizada." else "Meal updated.")
        }
    }

    fun toggleMealComplete(meal: MealItem) {
        scope.launch {
            mealRepository.toggleMealComplete(meal)
        }
    }

    fun deleteMeal(meal: MealItem) {
        scope.launch {
            mealRepository.deleteMeal(meal.id)
            uiState.value = uiState.value.copy(
                lastDeletedMeal = meal,
                undoMessage = if (uiState.value.currentLanguage == AppLanguage.SPANISH)
                    "Comida eliminada: ${meal.title}" else "Meal deleted: ${meal.title}"
            )
        }
    }

    fun undoDeleteMeal() {
        val meal = uiState.value.lastDeletedMeal ?: return
        scope.launch {
            mealRepository.restoreMeal(meal)
            uiState.value = uiState.value.copy(lastDeletedMeal = null, undoMessage = null)
            val isSpanish = uiState.value.currentLanguage == AppLanguage.SPANISH
            showFeedback(if (isSpanish) "Comida restaurada." else "Meal restored.")
        }
    }

    fun addPantryItem(
        name: String,
        category: PantryCategory,
        inStock: Boolean,
        isBatchBase: Boolean,
        quantityDescription: String
    ) {
        scope.launch {
            mealRepository.addPantryItem(name, category, inStock, isBatchBase, quantityDescription)
            val isSpanish = uiState.value.currentLanguage == AppLanguage.SPANISH
            showFeedback(if (isSpanish) "Alimento registrado en despensa." else "Item added to pantry.")
            closePantryAddDialog()
            closeBatchBaseDialog()
        }
    }

    fun updatePantryItem(item: PantryItem) {
        scope.launch {
            mealRepository.updatePantryItem(item)
            closePantryEditor()
            val isSpanish = uiState.value.currentLanguage == AppLanguage.SPANISH
            showFeedback(if (isSpanish) "Despensa actualizada." else "Pantry item updated.")
        }
    }

    fun togglePantryStock(id: String, inStock: Boolean) {
        scope.launch {
            mealRepository.togglePantryStock(id, inStock)
        }
    }

    fun deletePantryItem(item: PantryItem) {
        scope.launch {
            mealRepository.deletePantryItem(item.id)
            uiState.value = uiState.value.copy(
                lastDeletedPantryItem = item,
                undoMessage = if (uiState.value.currentLanguage == AppLanguage.SPANISH)
                    "Eliminado de despensa: ${item.name}" else "Removed from pantry: ${item.name}"
            )
        }
    }

    fun undoDeletePantryItem() {
        val item = uiState.value.lastDeletedPantryItem ?: return
        scope.launch {
            mealRepository.restorePantryItem(item)
            uiState.value = uiState.value.copy(lastDeletedPantryItem = null, undoMessage = null)
            val isSpanish = uiState.value.currentLanguage == AppLanguage.SPANISH
            showFeedback(if (isSpanish) "Alimento restaurado en despensa." else "Pantry item restored.")
        }
    }

    fun importMealsFromExternalAI(rawTextOrJson: String) {
        uiState.value = uiState.value.copy(isImportingDiet = true)
        scope.launch {
            val result = mealImportUseCase.execute(rawTextOrJson)
            uiState.value = uiState.value.copy(isImportingDiet = false)
            val isSpanish = uiState.value.currentLanguage == AppLanguage.SPANISH
            if (result.isSuccess) {
                val count = result.getOrNull() ?: 0
                closeImportDietDialog()
                showFeedback(
                    if (isSpanish) "🥗 ¡$count comidas importadas correctamente con macronutrientes!"
                    else "🥗 $count meals imported successfully with macros!"
                )
            } else {
                showFeedback(
                    if (isSpanish) "❌ Error al interpretar la dieta: Formato de texto o JSON no reconocido."
                    else "❌ Failed to parse diet: Unrecognized format."
                )
            }
        }
    }
}
