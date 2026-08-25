package com.example.data.repository

import android.content.Context
import android.os.Environment
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.local.*
import com.example.data.mapper.*
import com.example.data.model.*
import com.example.data.remote.AetherGeminiEngine
import com.example.data.util.AetherDateUtils
import com.example.service.AetherNotificationScheduler
import com.example.ui.i18n.AppLanguage
import com.example.widget.FrogTaskWidgetProvider
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

private val Context.aetherDataStore: DataStore<Preferences> by preferencesDataStore(name = "aether_prefs")
private val LANGUAGE_KEY = stringPreferencesKey("app_language")

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
    private val completionLogDao = database.completionLogDao()
    private val dailySummaryDao = database.dailySummaryDao()
    private val aiMessageDao = database.aiMessageDao()

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val planAdapter = moshi.adapter(AetherDailyPlan::class.java).indent("  ")
    private val backupAdapter = moshi.adapter(AetherFullBackup::class.java).indent("  ")

    val aiMessages: Flow<List<AiMessage>> = aiMessageDao.getAllMessages().map { list ->
        list.map { it.toModel() }
    }

    val favoriteAiMessages: Flow<List<AiMessage>> = aiMessageDao.getFavoriteMessages().map { list ->
        list.map { it.toModel() }
    }

    val tasks: Flow<List<TaskItem>> = taskDao.getAllTasks().map { list ->
        list.map { it.toModel() }
    }

    val timeBlocks: Flow<List<TimeBlock>> = timeBlockDao.getAllTimeBlocks().map { list ->
        list.map { it.toModel() }
    }

    val pantryItems: Flow<List<PantryItem>> = pantryDao.getAllPantryItems().map { list ->
        list.map { it.toModel() }
    }

    val meals: Flow<List<MealItem>> = combine(
        mealDao.getAllMeals(),
        pantryDao.getAllPantryItems()
    ) { mealEntities, pantryEntities ->
        val inStockNames = pantryEntities
            .filter { it.inStock }
            .map { it.name.trim().lowercase(Locale.ROOT) }
            .toSet()

        mealEntities.map { mealEntity ->
            val hasAllInStock = calculateMealIngredientsInStock(mealEntity.ingredients, inStockNames)
            mealEntity.toModel(inStock = hasAllInStock)
        }
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

    val completionLogs: Flow<List<CompletionLog>> = completionLogDao.getAllLogs().map { list ->
        list.map { it.toModel() }
    }

    val dailySummaries: Flow<List<DailySummary>> = dailySummaryDao.getAllSummaries().map { list ->
        list.map { it.toModel() }
    }

    fun getLogsByDate(dateIso: String): Flow<List<CompletionLog>> {
        return completionLogDao.getLogsByDate(dateIso).map { list ->
            list.map { it.toModel() }
        }
    }

    fun getLogsBetweenDates(startDateIso: String, endDateIso: String): Flow<List<CompletionLog>> {
        return completionLogDao.getLogsBetweenDates(startDateIso, endDateIso).map { list ->
            list.map { it.toModel() }
        }
    }

    fun getSummariesForMonth(yearMonthPrefix: String): Flow<List<DailySummary>> {
        return dailySummaryDao.getSummariesForMonth("$yearMonthPrefix%").map { list ->
            list.map { it.toModel() }
        }
    }

    fun getSummariesBetweenDates(startDateIso: String, endDateIso: String): Flow<List<DailySummary>> {
        return dailySummaryDao.getSummariesBetweenDates(startDateIso, endDateIso).map { list ->
            list.map { it.toModel() }
        }
    }

    // Language persistence with DataStore Preferences
    fun getLanguage(): Flow<AppLanguage> {
        if (appContext == null) return flowOf(AppLanguage.SPANISH)
        return appContext.aetherDataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                val langStr = preferences[LANGUAGE_KEY] ?: AppLanguage.SPANISH.name
                try {
                    AppLanguage.valueOf(langStr)
                } catch (e: Exception) {
                    AppLanguage.SPANISH
                }
            }
    }

    suspend fun saveLanguage(lang: AppLanguage) {
        appContext?.aetherDataStore?.edit { preferences ->
            preferences[LANGUAGE_KEY] = lang.name
        }
    }

    suspend fun resetDataToLanguage(language: AppLanguage) {
        AetherDatabase.clearAllAetherData(database, clearHistory = false)
        AetherDatabase.populateInitialAetherData(database, language)
        appContext?.let { FrogTaskWidgetProvider.updateAllWidgets(it) }
    }

    // --- Automatic Logging & Summary Recalculation ---
    private suspend fun logActionAndRecalculate(
        itemType: CompletionItemType,
        itemId: String,
        title: String,
        status: CompletionStatus,
        dateIso: String = AetherDateUtils.getTodayIso()
    ) {
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

    suspend fun recalculateDailySummary(dateIso: String = AetherDateUtils.getTodayIso()) {
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

    suspend fun updateTask(task: TaskItem) {
        if (task.isFrog || task.priorityType == PriorityType.FROG) {
            taskDao.clearFrogStatus()
        }
        taskDao.updateTask(task.toEntity())
        appContext?.let { FrogTaskWidgetProvider.updateAllWidgets(it) }
    }

    suspend fun restoreTask(task: TaskItem) {
        taskDao.insertTask(task.toEntity())
        appContext?.let { FrogTaskWidgetProvider.updateAllWidgets(it) }
    }

    suspend fun reorderTasks(tasks: List<TaskItem>) {
        val baseTime = System.currentTimeMillis()
        tasks.forEachIndexed { index, task ->
            val updated = task.toEntity().copy(createdAt = baseTime - (index * 1000L))
            taskDao.updateTask(updated)
        }
    }

    suspend fun toggleTaskComplete(task: TaskItem) {
        val newCompleted = !task.isCompleted
        val updated = task.copy(isCompleted = newCompleted)
        taskDao.updateTask(updated.toEntity())
        appContext?.let { FrogTaskWidgetProvider.updateAllWidgets(it) }

        val status = if (newCompleted) CompletionStatus.COMPLETED else CompletionStatus.MISSED
        logActionAndRecalculate(CompletionItemType.TASK, task.id, task.title, status)
    }

    suspend fun setTaskAsFrog(taskId: String) {
        taskDao.clearFrogStatus()
        taskDao.setFrogTask(taskId)
        appContext?.let { FrogTaskWidgetProvider.updateAllWidgets(it) }
    }

    suspend fun deleteTask(taskId: String) {
        val allTasks = taskDao.getAllTasks().first()
        val taskToDelete = allTasks.find { it.id == taskId }
        if (taskToDelete != null && !taskToDelete.isCompleted) {
            logActionAndRecalculate(
                CompletionItemType.TASK,
                taskToDelete.id,
                taskToDelete.title,
                CompletionStatus.MISSED
            )
        }
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
        appContext?.let { ctx ->
            FrogTaskWidgetProvider.updateAllWidgets(ctx)
        }
    }

    suspend fun updateTimeBlock(block: TimeBlock) {
        timeBlockDao.updateTimeBlock(block.toEntity())
        appContext?.let { ctx ->
            FrogTaskWidgetProvider.updateAllWidgets(ctx)
        }
    }

    suspend fun restoreTimeBlock(block: TimeBlock) {
        timeBlockDao.insertTimeBlock(block.toEntity())
        appContext?.let { ctx ->
            FrogTaskWidgetProvider.updateAllWidgets(ctx)
        }
    }

    suspend fun reorderTimeBlocks(blocks: List<TimeBlock>) {
        blocks.forEachIndexed { index, block ->
            timeBlockDao.updateTimeBlock(block.toEntity().copy(sortOrder = index))
        }
        appContext?.let { ctx ->
            FrogTaskWidgetProvider.updateAllWidgets(ctx)
        }
    }

    suspend fun toggleTimeBlockComplete(block: TimeBlock) {
        val newCompleted = !block.isCompleted
        val updated = block.copy(isCompleted = newCompleted)
        timeBlockDao.updateTimeBlock(updated.toEntity())

        val status = if (newCompleted) CompletionStatus.COMPLETED else CompletionStatus.MISSED
        logActionAndRecalculate(CompletionItemType.TIME_BLOCK, block.id, block.title, status)
    }

    suspend fun deleteTimeBlock(id: String) {
        val allBlocks = timeBlockDao.getAllTimeBlocks().first()
        val blockToDelete = allBlocks.find { it.id == id }
        if (blockToDelete != null && !blockToDelete.isCompleted) {
            logActionAndRecalculate(
                CompletionItemType.TIME_BLOCK,
                blockToDelete.id,
                blockToDelete.title,
                CompletionStatus.MISSED
            )
        }
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

    suspend fun updatePantryItem(item: PantryItem) {
        pantryDao.updateItem(item.toEntity())
    }

    suspend fun restorePantryItem(item: PantryItem) {
        pantryDao.insertItem(item.toEntity())
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
        bioImpact: BioGlycemicImpact,
        customSlotName: String? = null,
        proteinGrams: Int = 0,
        carbsGrams: Int = 0,
        fatGrams: Int = 0,
        caloriesKcal: Int = 0,
        dateIso: String = AetherDateUtils.getTodayIso()
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

    suspend fun duplicateMeal(
        meal: MealItem,
        targetDateIso: String = AetherDateUtils.getTodayIso(),
        copySuffix: Boolean = false
    ) {
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

    suspend fun updateMeal(meal: MealItem) {
        val calculatedKcal = if (meal.caloriesKcal > 0) meal.caloriesKcal else (meal.proteinGrams * 4 + meal.carbsGrams * 4 + meal.fatGrams * 9)
        mealDao.updateMeal(meal.copy(caloriesKcal = calculatedKcal).toEntity())
    }

    suspend fun restoreMeal(meal: MealItem) {
        mealDao.insertMeal(meal.toEntity())
    }

    suspend fun toggleMealComplete(meal: MealItem) {
        val newCompleted = !meal.isCompleted
        val updated = meal.copy(isCompleted = newCompleted)
        mealDao.updateMeal(updated.toEntity())

        val status = if (newCompleted) CompletionStatus.COMPLETED else CompletionStatus.MISSED
        logActionAndRecalculate(CompletionItemType.MEAL, meal.id, meal.title, status)
    }

    suspend fun deleteMeal(id: String) {
        val allMeals = mealDao.getAllMeals().first()
        val mealToDelete = allMeals.find { it.id == id }
        if (mealToDelete != null && !mealToDelete.isCompleted) {
            logActionAndRecalculate(
                CompletionItemType.MEAL,
                mealToDelete.id,
                mealToDelete.title,
                CompletionStatus.MISSED
            )
        }
        mealDao.deleteMeal(id)
    }

    // --- Habit & Grace Operations ---
    suspend fun addHabit(
        title: String,
        description: String,
        anchor: CircadianAnchor,
        streakDays: Int = 0,
        reframingTip: String = "Biological consistency is a pattern of return, not perfection."
    ) {
        val id = "habit-" + UUID.randomUUID().toString().take(8)
        val entity = HabitEntity(
            id = id,
            title = title,
            description = description,
            anchor = anchor,
            isCompleted = false,
            streakDays = streakDays,
            graceDaysUsed = 0,
            reframingTip = reframingTip.ifBlank { "Biological consistency is a pattern of return, not perfection." }
        )
        habitDao.insertHabit(entity)
    }

    suspend fun updateHabit(habit: HabitAnchor) {
        habitDao.updateHabit(habit.toEntity())
    }

    suspend fun deleteHabit(id: String) {
        val allHabits = habitDao.getAllHabits().first()
        val habitToDelete = allHabits.find { it.id == id }
        if (habitToDelete != null && !habitToDelete.isCompleted) {
            logActionAndRecalculate(
                CompletionItemType.HABIT,
                habitToDelete.id,
                habitToDelete.title,
                CompletionStatus.MISSED
            )
        }
        habitDao.deleteHabit(id)
    }

    suspend fun restoreHabit(habit: HabitAnchor) {
        habitDao.insertHabit(habit.toEntity())
    }

    suspend fun toggleHabitComplete(habit: HabitAnchor) {
        val newCompleted = !habit.isCompleted
        val newStreak = if (newCompleted) habit.streakDays + 1 else maxOf(0, habit.streakDays - 1)
        habitDao.updateHabit(habit.toEntity().copy(isCompleted = newCompleted, streakDays = newStreak))

        val status = if (newCompleted) CompletionStatus.COMPLETED else CompletionStatus.MISSED
        logActionAndRecalculate(CompletionItemType.HABIT, habit.id, habit.title, status)
    }

    suspend fun applyGraceDay(habit: HabitAnchor) {
        habitDao.updateHabit(habit.toEntity().copy(graceDaysUsed = habit.graceDaysUsed + 1))
        logActionAndRecalculate(CompletionItemType.HABIT, habit.id, habit.title, CompletionStatus.PARTIAL)
    }

    // --- Biometric Updates ---
    suspend fun updateReadiness(score: Int) {
        val isRecovery = score < 60
        val today = AetherDateUtils.getTodayIso()
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
        val today = AetherDateUtils.getTodayIso()
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
        val today = AetherDateUtils.getTodayIso()
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

    suspend fun resetToCleanSlate(language: AppLanguage, wipeHistory: Boolean = false) {
        AetherDatabase.populateCleanSlate(database, language, wipeHistory = wipeHistory)
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

    // --- Phase 3 Module 5: AI Chat Streaming & Message Persistence ---
    fun streamChatResponse(prompt: String, context: com.example.data.remote.AetherAiContext): Flow<String> {
        return geminiEngine.streamChatResponse(prompt, context)
    }

    suspend fun saveAiMessage(message: AiMessage) {
        aiMessageDao.insertMessage(message.toEntity())
    }

    suspend fun updateAiMessage(message: AiMessage) {
        aiMessageDao.updateMessage(message.toEntity())
    }

    suspend fun toggleAiMessageFavorite(id: String, isFavorite: Boolean) {
        aiMessageDao.setFavorite(id, isFavorite)
    }

    suspend fun deleteAiMessage(id: String) {
        aiMessageDao.deleteMessage(id)
    }

    suspend fun clearAllAiMessages() {
        aiMessageDao.clearAllMessages()
    }

    fun exportPlanAsJson(plan: AetherDailyPlan): String {
        return planAdapter.toJson(plan)
    }

    // --- Full Backup & Restore ---
    suspend fun createFullBackupJson(): String = withContext(Dispatchers.IO) {
        val backup = AetherFullBackup(
            version = 3,
            exportedAt = System.currentTimeMillis(),
            exportedDate = AetherDateUtils.getTodayIso(),
            tasks = taskDao.getAllTasks().first(),
            timeBlocks = timeBlockDao.getAllTimeBlocks().first(),
            pantryItems = pantryDao.getAllPantryItems().first(),
            meals = mealDao.getAllMeals().first(),
            habits = habitDao.getAllHabits().first(),
            biometrics = biometricDao.getRecentBiometrics(365).first(),
            completionLogs = completionLogDao.getAllLogs().first(),
            dailySummaries = dailySummaryDao.getAllSummaries().first()
        )
        backupAdapter.toJson(backup)
    }

    suspend fun exportFullBackupToDocuments(context: Context): Result<String> = withContext(Dispatchers.IO) {
        try {
            val jsonContent = createFullBackupJson()
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val fileName = "aether_backup_$timeStamp.json"

            val docsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                ?: File(context.filesDir, "documents").apply { mkdirs() }
            if (!docsDir.exists()) docsDir.mkdirs()

            val file = File(docsDir, fileName)
            file.writeText(jsonContent)
            Result.success(file.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun restoreFromBackupJson(jsonString: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val backup = backupAdapter.fromJson(jsonString)
                ?: return@withContext Result.failure(IllegalArgumentException("Invalid backup JSON payload"))

            if (backup.tasks.isNotEmpty()) {
                taskDao.clearAllTasks()
                taskDao.insertTasks(backup.tasks)
            }
            if (backup.timeBlocks.isNotEmpty()) {
                timeBlockDao.clearAllTimeBlocks()
                timeBlockDao.insertTimeBlocks(backup.timeBlocks)
            }
            if (backup.pantryItems.isNotEmpty()) {
                pantryDao.clearAllPantry()
                pantryDao.insertItems(backup.pantryItems)
            }
            if (backup.meals.isNotEmpty()) {
                mealDao.clearAllMeals()
                mealDao.insertMeals(backup.meals)
            }
            if (backup.habits.isNotEmpty()) {
                habitDao.clearAllHabits()
                habitDao.insertHabits(backup.habits)
            }
            if (backup.biometrics.isNotEmpty()) {
                biometricDao.clearAllBiometrics()
                backup.biometrics.forEach { biometricDao.insertBiometric(it) }
            }
            if (backup.completionLogs.isNotEmpty()) {
                completionLogDao.clearAllLogs()
                completionLogDao.insertLogs(backup.completionLogs)
            }
            if (backup.dailySummaries.isNotEmpty()) {
                dailySummaryDao.clearAllSummaries()
                dailySummaryDao.insertSummaries(backup.dailySummaries)
            }

            appContext?.let { FrogTaskWidgetProvider.updateAllWidgets(it) }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
