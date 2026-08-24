package com.example.data.repository

import com.example.data.local.*
import com.example.data.model.*
import com.example.data.remote.AetherGeminiEngine
import com.example.ui.i18n.AppLanguage
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.*
import java.util.UUID

class AetherRepository(
    private val database: AetherDatabase,
    private val geminiEngine: AetherGeminiEngine = AetherGeminiEngine()
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

    val biometric: Flow<BiometricBaseline> = biometricDao.getBiometric("2026-08-22").map { entity ->
        entity?.toModel() ?: BiometricBaseline(readinessScore = 75, chronotype = Chronotype.BEAR)
    }

    suspend fun resetDataToLanguage(language: AppLanguage) {
        AetherDatabase.clearAllAetherData(database)
        AetherDatabase.populateInitialAetherData(database, language)
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
            category = category
        )
        taskDao.insertTask(entity)
    }

    suspend fun toggleTaskComplete(task: TaskItem) {
        taskDao.updateTask(task.toEntity().copy(isCompleted = !task.isCompleted))
    }

    suspend fun setTaskAsFrog(taskId: String) {
        taskDao.clearFrogStatus()
        taskDao.setFrogTask(taskId)
    }

    suspend fun deleteTask(taskId: String) {
        taskDao.deleteTask(taskId)
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
            notes = notes,
            sortOrder = 10
        )
        timeBlockDao.insertTimeBlock(entity)
    }

    suspend fun toggleTimeBlockComplete(block: TimeBlock) {
        timeBlockDao.updateTimeBlock(block.toEntity().copy(isCompleted = !block.isCompleted))
    }

    suspend fun deleteTimeBlock(id: String) {
        timeBlockDao.deleteTimeBlock(id)
    }

    // --- Pantry Operations ---
    suspend fun addPantryItem(name: String, category: PantryCategory, inStock: Boolean, isBatchBase: Boolean, qty: String) {
        val id = "p-" + UUID.randomUUID().toString().take(6)
        pantryDao.insertItem(PantryEntity(id, name, category, inStock, isBatchBase, qty))
    }

    suspend fun togglePantryStock(id: String, currentInStock: Boolean) {
        pantryDao.setStockStatus(id, !currentInStock)
    }

    suspend fun deletePantryItem(id: String) {
        pantryDao.deleteItem(id)
    }

    // --- Meal Operations ---
    suspend fun toggleMealComplete(meal: MealItem) {
        mealDao.updateMeal(meal.toEntity().copy(isCompleted = !meal.isCompleted))
    }

    // --- Habit Operations ---
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
        biometricDao.insertBiometric(
            BiometricEntity(
                date = "2026-08-22",
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
        biometricDao.insertBiometric(
            BiometricEntity(
                date = "2026-08-22",
                readinessScore = currentReadiness,
                perceivedEnergy = currentReadiness,
                chronotype = chronotype,
                recoveryModeTriggered = currentReadiness < 60,
                graceDayActive = currentReadiness < 60
            )
        )
    }

    suspend fun setRecoveryMode(enabled: Boolean, currentScore: Int) {
        biometricDao.insertBiometric(
            BiometricEntity(
                date = "2026-08-22",
                readinessScore = if (enabled) 45 else maxOf(65, currentScore),
                perceivedEnergy = if (enabled) 40 else maxOf(65, currentScore),
                chronotype = Chronotype.BEAR,
                recoveryModeTriggered = enabled,
                graceDayActive = enabled
            )
        )
    }

    // --- AI Orchestration & Plan Synthesis ---
    suspend fun orchestrateDailyPlan(
        readiness: Int,
        chronotype: Chronotype,
        currentTasks: List<TaskItem>,
        currentPantry: List<PantryItem>
    ): AetherDailyPlan {
        val plan = geminiEngine.orchestratePlan(
            readinessScore = readiness,
            chronotype = chronotype,
            existingTasks = currentTasks,
            pantryItems = currentPantry
        )

        // Sync TimeBlocks into Database
        timeBlockDao.clearAllTimeBlocks()
        timeBlockDao.insertTimeBlocks(plan.time_blocks.mapIndexed { index, b ->
            b.toEntity().copy(sortOrder = index)
        })

        return plan
    }

    suspend fun getCognitiveReframe(userFeeling: String, readinessScore: Int): String {
        return geminiEngine.generateCognitiveReframe(userFeeling, readinessScore)
    }

    fun exportPlanAsJson(plan: AetherDailyPlan): String {
        return planAdapter.toJson(plan)
    }

    // Extension Mappers
    private fun TaskEntity.toModel() = TaskItem(
        id = id,
        title = title,
        description = description,
        energyLevel = energyLevel,
        priorityType = priorityType,
        estimatedMinutes = estimatedMinutes,
        isCompleted = isCompleted,
        isFrog = isFrog,
        scheduledTime = scheduledTime,
        category = category
    )

    private fun TaskItem.toEntity() = TaskEntity(
        id = id,
        title = title,
        description = description,
        energyLevel = energyLevel,
        priorityType = priorityType,
        estimatedMinutes = estimatedMinutes,
        isCompleted = isCompleted,
        isFrog = isFrog,
        scheduledTime = scheduledTime,
        category = category
    )

    private fun TimeBlockEntity.toModel() = TimeBlock(
        id = id,
        startTime = startTime,
        endTime = endTime,
        blockType = blockType,
        title = title,
        isCompleted = isCompleted,
        linkedTaskId = linkedTaskId,
        notes = notes
    )

    private fun TimeBlock.toEntity() = TimeBlockEntity(
        id = id,
        startTime = startTime,
        endTime = endTime,
        blockType = blockType,
        title = title,
        isCompleted = isCompleted,
        linkedTaskId = linkedTaskId,
        notes = notes
    )

    private fun PantryEntity.toModel() = PantryItem(
        id = id,
        name = name,
        category = category,
        inStock = inStock,
        isBatchBase = isBatchBase,
        quantityDesc = quantityDesc
    )

    private fun MealEntity.toModel() = MealItem(
        id = id,
        slot = slot,
        title = title,
        description = description,
        prepTimeMinutes = prepTimeMinutes,
        ingredients = ingredients,
        usesBatchCookedBase = usesBatchCookedBase,
        allIngredientsInStock = allIngredientsInStock,
        bioImpact = bioImpact,
        isCompleted = isCompleted
    )

    private fun MealItem.toEntity() = MealEntity(
        id = id,
        slot = slot,
        title = title,
        description = description,
        prepTimeMinutes = prepTimeMinutes,
        ingredients = ingredients,
        usesBatchCookedBase = usesBatchCookedBase,
        allIngredientsInStock = allIngredientsInStock,
        bioImpact = bioImpact,
        isCompleted = isCompleted
    )

    private fun HabitEntity.toModel() = HabitAnchor(
        id = id,
        title = title,
        description = description,
        anchor = anchor,
        isCompleted = isCompleted,
        streakDays = streakDays,
        graceDaysUsed = graceDaysUsed,
        reframingTip = reframingTip
    )

    private fun HabitAnchor.toEntity() = HabitEntity(
        id = id,
        title = title,
        description = description,
        anchor = anchor,
        isCompleted = isCompleted,
        streakDays = streakDays,
        graceDaysUsed = graceDaysUsed,
        reframingTip = reframingTip
    )

    private fun BiometricEntity.toModel() = BiometricBaseline(
        readinessScore = readinessScore,
        perceivedEnergy = perceivedEnergy,
        sleepHours = sleepHours,
        sleepQuality = sleepQuality,
        chronotype = chronotype,
        recoveryModeTriggered = recoveryModeTriggered,
        graceDayActive = graceDayActive
    )
}
