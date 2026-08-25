package com.example.data.repository

import com.example.data.local.CompletionLogDao
import com.example.data.local.CompletionLogEntity
import com.example.data.local.DailySummaryDao
import com.example.data.local.DailySummaryEntity
import com.example.data.local.HabitDao
import com.example.data.local.MealDao
import com.example.data.local.MealEntity
import com.example.data.local.PantryDao
import com.example.data.local.PantryEntity
import com.example.data.local.TaskDao
import com.example.data.mapper.calculateMealIngredientsInStock
import com.example.data.mapper.toEntity
import com.example.data.mapper.toModel
import com.example.data.model.*
import com.example.data.util.AetherDateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID

interface MealRepository {
    val meals: Flow<List<MealItem>>
    val pantryItems: Flow<List<PantryItem>>

    suspend fun addMeal(
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
    )
    suspend fun duplicateMeal(
        meal: MealItem,
        targetDateIso: String = AetherDateUtils.getTodayIso(),
        copySuffix: Boolean = false
    )
    suspend fun updateMeal(meal: MealItem)
    suspend fun restoreMeal(meal: MealItem)
    suspend fun toggleMealComplete(meal: MealItem)
    suspend fun deleteMeal(id: String)

    suspend fun addPantryItem(
        name: String,
        category: PantryCategory,
        inStock: Boolean,
        isBatchBase: Boolean,
        quantityDescription: String
    )
    suspend fun updatePantryItem(item: PantryItem)
    suspend fun restorePantryItem(item: PantryItem)
    suspend fun togglePantryStock(id: String, inStock: Boolean)
    suspend fun deletePantryItem(id: String)
}

class MealRepositoryImpl(
    private val mealDao: MealDao,
    private val pantryDao: PantryDao,
    private val taskDao: TaskDao,
    private val habitDao: HabitDao,
    private val completionLogDao: CompletionLogDao,
    private val dailySummaryDao: DailySummaryDao
) : MealRepository {

    override val pantryItems: Flow<List<PantryItem>> = pantryDao.getAllPantryItems().map { list ->
        list.map { it.toModel() }
    }

    override val meals: Flow<List<MealItem>> = combine(
        mealDao.getAllMeals(),
        pantryDao.getAllPantryItems()
    ) { mealEntities, pantryEntities ->
        val inStockNames = pantryEntities.filter { it.inStock }.map { it.name.trim().lowercase() }.toSet()
        mealEntities.map { entity ->
            val hasAllInStock = calculateMealIngredientsInStock(entity.ingredients, inStockNames)
            entity.toModel(inStock = hasAllInStock)
        }
    }

    override suspend fun addMeal(
        slot: MealSlot,
        title: String,
        description: String,
        prepTimeMinutes: Int,
        ingredients: List<String>,
        usesBatchCookedBase: Boolean,
        allIngredientsInStock: Boolean,
        bioImpact: BioGlycemicImpact,
        customSlotName: String?,
        proteinGrams: Int,
        carbsGrams: Int,
        fatGrams: Int,
        caloriesKcal: Int,
        dateIso: String
    ) {
        val id = "meal-" + UUID.randomUUID().toString().take(8)
        val calculatedKcal = if (caloriesKcal > 0) caloriesKcal else (proteinGrams * 4 + carbsGrams * 4 + fatGrams * 9)
        val entity = MealEntity(
            id = id,
            slot = slot,
            title = title,
            description = description,
            prepTimeMinutes = prepTimeMinutes,
            ingredients = ingredients,
            usesBatchCookedBase = usesBatchCookedBase,
            allIngredientsInStock = allIngredientsInStock,
            bioImpact = bioImpact,
            isCompleted = false,
            customSlotName = customSlotName,
            proteinGrams = proteinGrams,
            carbsGrams = carbsGrams,
            fatGrams = fatGrams,
            caloriesKcal = calculatedKcal,
            dateIso = dateIso
        )
        mealDao.insertMeal(entity)
    }

    override suspend fun duplicateMeal(meal: MealItem, targetDateIso: String, copySuffix: Boolean) {
        val newId = "meal-" + UUID.randomUUID().toString().take(8)
        val newTitle = if (copySuffix) "${meal.title} (Copia)" else meal.title
        val duplicated = meal.copy(
            id = newId,
            title = newTitle,
            isCompleted = false,
            dateIso = targetDateIso
        )
        mealDao.insertMeal(duplicated.toEntity())
    }

    override suspend fun updateMeal(meal: MealItem) {
        val calculatedKcal = if (meal.caloriesKcal > 0) meal.caloriesKcal else (meal.proteinGrams * 4 + meal.carbsGrams * 4 + meal.fatGrams * 9)
        mealDao.updateMeal(meal.copy(caloriesKcal = calculatedKcal).toEntity())
    }

    override suspend fun restoreMeal(meal: MealItem) {
        mealDao.insertMeal(meal.toEntity())
    }

    override suspend fun toggleMealComplete(meal: MealItem) {
        val today = AetherDateUtils.getTodayIso()
        val newCompleted = !meal.isCompleted
        val updated = meal.copy(isCompleted = newCompleted)
        mealDao.updateMeal(updated.toEntity())

        if (newCompleted) {
            logActionAndRecalculate(CompletionItemType.MEAL, meal.id, meal.title, CompletionStatus.COMPLETED, today)
        } else {
            completionLogDao.deleteLogForItemAndDate(meal.id, today)
            recalculateDailySummary(today)
        }
    }

    override suspend fun deleteMeal(id: String) {
        val today = AetherDateUtils.getTodayIso()
        mealDao.deleteMeal(id)
        completionLogDao.deleteLogForItemAndDate(id, today)
        recalculateDailySummary(today)
    }

    override suspend fun addPantryItem(
        name: String,
        category: PantryCategory,
        inStock: Boolean,
        isBatchBase: Boolean,
        quantityDescription: String
    ) {
        val id = "pantry-" + UUID.randomUUID().toString().take(8)
        val entity = PantryEntity(
            id = id,
            name = name,
            category = category,
            inStock = inStock,
            isBatchBase = isBatchBase,
            quantityDesc = quantityDescription
        )
        pantryDao.insertItem(entity)
    }

    override suspend fun updatePantryItem(item: PantryItem) {
        pantryDao.updateItem(item.toEntity())
    }

    override suspend fun restorePantryItem(item: PantryItem) {
        pantryDao.insertItem(item.toEntity())
    }

    override suspend fun togglePantryStock(id: String, inStock: Boolean) {
        pantryDao.setStockStatus(id, inStock)
    }

    override suspend fun deletePantryItem(id: String) {
        pantryDao.deleteItem(id)
    }

    private suspend fun logActionAndRecalculate(
        itemType: CompletionItemType,
        itemId: String,
        title: String,
        status: CompletionStatus,
        dateIso: String = AetherDateUtils.getTodayIso()
    ) {
        completionLogDao.deleteLogForItemAndDate(itemId, dateIso)
        val log = CompletionLogEntity(
            dateIso = dateIso,
            itemType = itemType,
            itemId = itemId,
            title = title,
            status = status,
            timestamp = System.currentTimeMillis()
        )
        completionLogDao.insertLog(log)
        recalculateDailySummary(dateIso)
    }

    private suspend fun recalculateDailySummary(dateIso: String) {
        val logs = completionLogDao.getLogsByDate(dateIso).first()
        val allTasks = taskDao.getAllTasks().first()
        val allHabits = habitDao.getAllHabits().first()
        val allMeals = mealDao.getAllMeals().first()

        val activeCount = (allTasks.size + allHabits.size + allMeals.size).coerceAtLeast(logs.size)
        val totalCount = activeCount.coerceAtLeast(1)
        val completedCount = logs.count { it.status == CompletionStatus.COMPLETED }
        val partialCount = logs.count { it.status == CompletionStatus.PARTIAL }
        val ratio = ((completedCount.toFloat() + partialCount.toFloat() * 0.5f) / totalCount.toFloat()).coerceIn(0f, 1f)

        dailySummaryDao.insertOrUpdateSummary(
            DailySummaryEntity(
                dateIso = dateIso,
                totalCount = totalCount,
                completedCount = completedCount,
                partialCount = partialCount,
                ratio = ratio
            )
        )
    }
}
