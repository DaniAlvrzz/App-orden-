package com.example.data.repository

import android.content.Context
import com.example.data.local.*
import com.example.data.mapper.*
import com.example.data.model.*
import com.example.data.remote.AetherGeminiEngine
import com.example.service.AetherNotificationScheduler
import com.example.ui.i18n.AppLanguage
import com.example.widget.FrogTaskWidgetProvider
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.*
import java.util.UUID

class AetherRepository(
    private val database: AetherDatabase,
    private val geminiEngine: AetherGeminiEngine,
    private val appContext: Context? = null
) {
    private val taskDao = database.taskDao()
    private val timeBlockDao = database.timeBlockDao()
    private val pantryDao = database.pantryDao()
    private val mealDao = database.mealDao()
    private val habitDao = database.habitDao()
    private val biometricDao = database.biometricDao()

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val planAdapter = moshi.adapter(AetherDailyPlan::class.java).indent("  ")

    val tasks: Flow<List<TaskItem>> = taskDao.getAllTasks().map { list ->
        list.map { it.toModel() }
    }

    val timeBlocks: Flow<List<TimeBlock>> = timeBlockDao.getAllTimeBlocks().map { list ->
        list.map { it.toModel() }
    }

    val pantryItems: Flow<List<PantryItem>> = pantryDao.getAllPantryItems().map { list ->
        list.map { it.toModel() }
    }

    val meals: Flow<List<MealItem>> = mealDao.getAllMeals().map { list ->
        list.map { it.toModel() }
    }

    val habits: Flow<List<HabitAnchor>> = habitDao.getAllHabits().map { list ->
        list.map { it.toModel() }
    }

    val biometric: Flow<BiometricBaseline> = biometricDao.getLatestBiometric().map { entity ->
        entity?.toModel() ?: BiometricBaseline(readinessScore = 75, chronotype = Chronotype.BEAR)
    }

    val recentBiometrics: Flow<List<BiometricBaseline>> = biometricDao.getRecentBiometrics(30).map { list ->
        list.map { it.toModel() }
    }

    suspend fun resetDataToLanguage(language: AppLanguage) {
        AetherDatabase.clearAllAetherData(database)
        AetherDatabase.populateInitialAetherData(database, language)
        appContext?.let { FrogTaskWidgetProvider.updateAllWidgets(it) }
    }

    // --- Task Operations ---
    suspend fun addTask(
        title: String,
        description: String,
        energyLevel: EnergyLevel,
        priorityType: PriorityType,
        estimatedMinutes: Int,
        category: String,
        makeFrog: Boolean = false
    ) {
        val id = "task-" + UUID.randomUUID().toString().take(8)
        if (makeFrog || priorityType == PriorityType.FROG) {
            taskDao.clearFrogStatus()
        }
        val entity = TaskEntity(
            id = id,
            title = title,
            description = description,
            energyLevel = energyLevel,
            priorityType = if (makeFrog) PriorityType.FROG else priorityType,
            estimatedMinutes = estimatedMinutes,
            isCompleted = false,
            isFrog = makeFrog || priorityType == PriorityType.FROG,
            scheduledTime = null,
            category = category.ifBlank { "General" }
        )
        taskDao.insertTask(entity)
        appContext?.let { FrogTaskWidgetProvider.updateAllWidgets(it) }
    }

    suspend fun toggleTaskComplete(task: TaskItem) {
        val updated = task.copy(isCompleted = !task.isCompleted)
        taskDao.updateTask(updated.toEntity())
        appContext?.let { FrogTaskWidgetProvider.updateAllWidgets(it) }
    }

    suspend fun setTaskAsFrog(taskId: String) {
        taskDao.clearFrogStatus()
        taskDao.setFrogTask(taskId)
        appContext?.let { FrogTaskWidgetProvider.updateAllWidgets(it) }
    }

    suspend fun deleteTask(taskId: String) {
        taskDao.deleteTask(taskId)
        appContext?.let { FrogTaskWidgetProvider.updateAllWidgets(it) }
    }

    // --- TimeBlock Operations ---
    suspend fun addTimeBlock(
        startTime: String,
        endTime: String,
        blockType: BlockType,
        title: String,
        notes: String = ""
    ) {
        val id = "block-" + UUID.randomUUID().toString().take(8)
        val entity = TimeBlockEntity(
            id = id,
            startTime = startTime,
            endTime = endTime,
            blockType = blockType,
            title = title,
            isCompleted = false,
            notes = notes,
            sortOrder = 99
        )
        timeBlockDao.insertTimeBlock(entity)
    }

    suspend fun toggleTimeBlockComplete(block: TimeBlock) {
        val updated = block.copy(isCompleted = !block.isCompleted)
        timeBlockDao.updateTimeBlock(updated.toEntity())
    }

    suspend fun deleteTimeBlock(id: String) {
        timeBlockDao.deleteTimeBlock(id)
    }

    // --- Pantry Operations ---
    suspend fun addPantryItem(
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

    suspend fun togglePantryStock(id: String, inStock: Boolean) {
        pantryDao.setStockStatus(id, inStock)
    }

    suspend fun deletePantryItem(id: String) {
        pantryDao.deleteItem(id)
    }

    // --- Meals Operations ---
    suspend fun addMeal(
        slot: MealSlot,
        title: String,
        description: String,
        prepTimeMinutes: Int,
        ingredients: List<String>,
        usesBatchCookedBase: Boolean,
        allIngredientsInStock: Boolean,
        bioImpact: BioGlycemicImpact
    ) {
        val id = "meal-" + UUID.randomUUID().toString().take(8)
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
            isCompleted = false
        )
        mealDao.insertMeal(entity)
    }

    suspend fun toggleMealComplete(meal: MealItem) {
        val updated = meal.copy(isCompleted = !meal.isCompleted)
        mealDao.updateMeal(updated.toEntity())
    }

    suspend fun deleteMeal(id: String) {
        mealDao.deleteMeal(id)
    }

    // --- Habit & Grace Operations ---
    suspend fun toggleHabitComplete(habit: HabitAnchor) {
        val newCompleted = !habit.isCompleted
        val newStreak = if (newCompleted) habit.streakDays + 1 else maxOf(0, habit.streakDays - 1)
        habitDao.updateHabit(habit.toEntity().copy(isCompleted = newCompleted, streakDays = newStreak))
    }

    suspend fun applyGraceDay(habit: HabitAnchor) {
        habitDao.updateHabit(habit.toEntity().copy(graceDaysUsed = habit.graceDaysUsed + 1))
    }

    // --- Biometric Updates ---
    suspend fun updateReadiness(score: Int) {
        val isRecovery = score < 60
        val today = com.example.data.util.AetherDateUtils.getTodayIso()
        biometricDao.insertBiometric(
            BiometricEntity(
                date = today,
                readinessScore = score,
                perceivedEnergy = score,
                sleepHours = 7.5,
                sleepQuality = if (score > 70) 4 else 2,
                chronotype = Chronotype.BEAR,
                recoveryModeTriggered = isRecovery,
                graceDayActive = isRecovery
            )
        )
    }

    suspend fun updateChronotype(chronotype: Chronotype, currentReadiness: Int) {
        val today = com.example.data.util.AetherDateUtils.getTodayIso()
        biometricDao.insertBiometric(
            BiometricEntity(
                date = today,
                readinessScore = currentReadiness,
                perceivedEnergy = currentReadiness,
                chronotype = chronotype,
                recoveryModeTriggered = currentReadiness < 60,
                graceDayActive = currentReadiness < 60
            )
        )
    }

    suspend fun setRecoveryMode(enabled: Boolean, currentScore: Int) {
        val today = com.example.data.util.AetherDateUtils.getTodayIso()
        biometricDao.insertBiometric(
            BiometricEntity(
                date = today,
                readinessScore = if (enabled) 45 else maxOf(65, currentScore),
                perceivedEnergy = if (enabled) 40 else maxOf(65, currentScore),
                chronotype = Chronotype.BEAR,
                recoveryModeTriggered = enabled,
                graceDayActive = enabled
            )
        )
    }

    suspend fun resetToCleanSlate(language: AppLanguage) {
        AetherDatabase.populateCleanSlate(database, language)
        appContext?.let { FrogTaskWidgetProvider.updateAllWidgets(it) }
    }

    suspend fun populateDemoData(language: AppLanguage) {
        AetherDatabase.populateInitialAetherData(database, language)
        appContext?.let { FrogTaskWidgetProvider.updateAllWidgets(it) }
    }

    // --- AI Orchestration & Plan Synthesis ---
    suspend fun orchestrateDailyPlan(
        readiness: Int,
        chronotype: Chronotype,
        currentTasks: List<TaskItem>,
        currentPantry: List<PantryItem>
    ): AetherEngineResult {
        val result = geminiEngine.orchestratePlan(
            readinessScore = readiness,
            chronotype = chronotype,
            existingTasks = currentTasks,
            pantryItems = currentPantry
        )

        // Sync TimeBlocks into Database
        timeBlockDao.clearAllTimeBlocks()
        timeBlockDao.insertTimeBlocks(result.plan.time_blocks.mapIndexed { index, b ->
            b.toEntity().copy(sortOrder = index)
        })

        // Schedule local alarms for time blocks if context available
        appContext?.let { ctx ->
            AetherNotificationScheduler.scheduleTimeBlockAlerts(ctx, result.plan.time_blocks)
            FrogTaskWidgetProvider.updateAllWidgets(ctx)
        }

        return result
    }

    suspend fun getCognitiveReframe(userFeeling: String, readinessScore: Int): String {
        return geminiEngine.generateCognitiveReframe(userFeeling, readinessScore)
    }

    fun exportPlanAsJson(plan: AetherDailyPlan): String {
        return planAdapter.toJson(plan)
    }
}
