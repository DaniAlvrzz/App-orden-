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
import com.example.data.remote.AetherAiContext
import com.example.data.remote.AetherGeminiEngine
import com.example.data.util.AetherDateUtils
import com.example.service.AetherNotificationScheduler
import com.example.ui.i18n.AppLanguage
import com.example.widget.FrogTaskWidgetProvider
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Date
import java.util.Locale
import java.util.UUID

private val Context.aetherDataStore: DataStore<Preferences> by preferencesDataStore(name = "aether_prefs")
private val LANGUAGE_KEY = stringPreferencesKey("app_language")
private val LAST_ACTIVE_DATE_KEY = stringPreferencesKey("last_active_date")

data class DailyRolloverResult(
    val previousDateIso: String,
    val currentDateIso: String,
    val completedTasksCount: Int,
    val completedHabitsCount: Int,
    val completedMealsCount: Int
)

/**
 * Clean Architecture Repository for Aether OS.
 * Manages reactive data streams, database CRUD operations, undo/restore flows,
 * circadian time blocks, biological biometrics, meal planning with macros,
 * anchor habits with grace days, and AI chat streaming with Gemini.
 */
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

    // --- Reactive Data Streams ---

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

    // --- History Queries ---

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

    // --- Localization & Preferences ---

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

    // --- Daily Rollover & History Persistence Engine ---

    /**
     * Checks if today is a new calendar day. If so:
     * 1. Preserves completed tasks/habits into CompletionLogEntity for the previous date.
     * 2. Recalculates and persists DailySummaryEntity for the previous date.
     * 3. Resets isCompleted = false on active tasks, time blocks, and habits for the new day.
     * 4. Ensures a biometric entry exists for today.
     * 5. Updates the stored last active date.
     */
    suspend fun checkAndPerformDailyRollover(): DailyRolloverResult? = withContext(Dispatchers.IO) {
        val today = AetherDateUtils.getTodayIso()
        val lastDate = getLastActiveDate()

        if (lastDate == null) {
            saveLastActiveDate(today)
            return@withContext null
        }

        if (lastDate == today) {
            return@withContext null
        }

        // New day detected! Process rollover from lastDate -> today
        val tasks = taskDao.getAllTasks().first()
        val habits = habitDao.getAllHabits().first()
        val meals = mealDao.getAllMeals().first()
        val timeBlocks = timeBlockDao.getAllTimeBlocks().first()

        val existingLogsForLastDate = completionLogDao.getLogsByDate(lastDate).first()
        val loggedItemIds = existingLogsForLastDate.map { it.itemId }.toSet()

        var completedTasks = 0
        var completedHabits = 0
        var completedMeals = 0

        // 1. Log completed tasks from previous day
        tasks.filter { it.isCompleted }.forEach { task ->
            completedTasks++
            if (task.id !in loggedItemIds) {
                completionLogDao.insertLog(
                    CompletionLogEntity(
                        dateIso = lastDate,
                        itemType = CompletionItemType.TASK,
                        itemId = task.id,
                        title = task.title,
                        status = CompletionStatus.COMPLETED,
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
        }

        // 2. Log completed habits from previous day
        habits.filter { it.isCompleted }.forEach { habit ->
            completedHabits++
            if (habit.id !in loggedItemIds) {
                completionLogDao.insertLog(
                    CompletionLogEntity(
                        dateIso = lastDate,
                        itemType = CompletionItemType.HABIT,
                        itemId = habit.id,
                        title = habit.title,
                        status = CompletionStatus.COMPLETED,
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
        }

        // 3. Log completed meals from previous day
        meals.filter { it.isCompleted }.forEach { meal ->
            completedMeals++
            if (meal.id !in loggedItemIds) {
                completionLogDao.insertLog(
                    CompletionLogEntity(
                        dateIso = lastDate,
                        itemType = CompletionItemType.MEAL,
                        itemId = meal.id,
                        title = meal.title,
                        status = CompletionStatus.COMPLETED,
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
        }

        // 4. Summarize previous day's metrics
        recalculateDailySummary(lastDate)

        // 5. Reset completion flags for the new day (keep existing backlog items intact)
        tasks.forEach { task ->
            if (task.isCompleted) {
                taskDao.updateTask(task.copy(isCompleted = false))
            }
        }
        habits.forEach { habit ->
            if (habit.isCompleted) {
                habitDao.updateHabit(habit.copy(isCompleted = false))
            }
        }
        meals.forEach { meal ->
            if (meal.isCompleted) {
                mealDao.updateMeal(meal.copy(isCompleted = false))
            }
        }
        timeBlocks.forEach { block ->
            if (block.isCompleted) {
                timeBlockDao.updateTimeBlock(block.copy(isCompleted = false))
            }
        }

        // 6. Ensure baseline biometrics for today
        val latestBio = biometricDao.getLatestBiometric().first()
        biometricDao.insertBiometric(
            BiometricEntity(
                date = today,
                readinessScore = latestBio?.readinessScore ?: 75,
                perceivedEnergy = latestBio?.perceivedEnergy ?: 75,
                sleepHours = 7.5,
                sleepQuality = 4,
                chronotype = latestBio?.chronotype ?: Chronotype.BEAR,
                recoveryModeTriggered = false,
                graceDayActive = false
            )
        )

        // 7. Update last active date
        saveLastActiveDate(today)

        appContext?.let { FrogTaskWidgetProvider.updateAllWidgets(it) }

        DailyRolloverResult(
            previousDateIso = lastDate,
            currentDateIso = today,
            completedTasksCount = completedTasks,
            completedHabitsCount = completedHabits,
            completedMealsCount = completedMeals
        )
    }

    private suspend fun getLastActiveDate(): String? {
        if (appContext == null) return null
        return try {
            val prefs = appContext.aetherDataStore.data.first()
            prefs[LAST_ACTIVE_DATE_KEY]
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun saveLastActiveDate(dateIso: String) {
        appContext?.aetherDataStore?.edit { prefs ->
            prefs[LAST_ACTIVE_DATE_KEY] = dateIso
        }
    }

    // --- Logging & Daily Summary Engine ---

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

    // --- 1. Tareas (Tasks) ---

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

        if (newCompleted) {
            logActionAndRecalculate(CompletionItemType.TASK, task.id, task.title, CompletionStatus.COMPLETED)
        }
    }

    suspend fun setTaskAsFrog(taskId: String) {
        taskDao.clearFrogStatus()
        taskDao.setFrogTask(taskId)
        appContext?.let { FrogTaskWidgetProvider.updateAllWidgets(it) }
    }

    suspend fun deleteTask(taskId: String) {
        // Safe delete: Do NOT emit MISSED logs when merely cleaning backlog,
        // and preserve all existing completed history intact.
        taskDao.deleteTask(taskId)
        appContext?.let { FrogTaskWidgetProvider.updateAllWidgets(it) }
    }

    // --- 2. Bloques de Tiempo (TimeBlocks) ---

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

        if (newCompleted) {
            logActionAndRecalculate(CompletionItemType.TIME_BLOCK, block.id, block.title, CompletionStatus.COMPLETED)
        }
    }

    suspend fun deleteTimeBlock(id: String) {
        timeBlockDao.deleteTimeBlock(id)
    }

    // --- 3. Despensa (Pantry) ---

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

    // --- 4. Comidas (Meals & Macros) ---

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

        if (newCompleted) {
            logActionAndRecalculate(CompletionItemType.MEAL, meal.id, meal.title, CompletionStatus.COMPLETED)
        }
    }

    suspend fun deleteMeal(id: String) {
        mealDao.deleteMeal(id)
    }

    /**
     * Fault-Tolerant External AI Diet Importer.
     * Parses JSON or natural language text from ChatGPT, Claude, Gemini, etc.,
     * extracts meal slots, ingredients, and macronutrients, and inserts them into the DB.
     */
    suspend fun importMealsFromExternalAI(rawTextOrJson: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val clean = rawTextOrJson.trim()
            if (clean.isBlank()) {
                return@withContext Result.failure(IllegalArgumentException("Empty input text"))
            }

            val today = AetherDateUtils.getTodayIso()
            val importedMeals = mutableListOf<MealEntity>()

            // 1. Try Parsing as JSON Array or JSON Object
            if (clean.startsWith("[") || clean.startsWith("{")) {
                try {
                    if (clean.startsWith("[")) {
                        val jsonArray = JSONArray(clean)
                        for (i in 0 until jsonArray.length()) {
                            val obj = jsonArray.optJSONObject(i) ?: continue
                            val entity = parseMealJsonObject(obj, today)
                            if (entity != null) importedMeals.add(entity)
                        }
                    } else {
                        val jsonObject = JSONObject(clean)
                        if (jsonObject.has("meals")) {
                            val array = jsonObject.optJSONArray("meals")
                            if (array != null) {
                                for (i in 0 until array.length()) {
                                    val obj = array.optJSONObject(i) ?: continue
                                    val entity = parseMealJsonObject(obj, today)
                                    if (entity != null) importedMeals.add(entity)
                                }
                            }
                        } else {
                            // Check slot keys: breakfast, lunch, dinner, snack
                            val slots = listOf("breakfast", "lunch", "dinner", "snack", "desayuno", "almuerzo", "comida", "cena", "merienda")
                            for (key in slots) {
                                if (jsonObject.has(key)) {
                                    val obj = jsonObject.optJSONObject(key)
                                    if (obj != null) {
                                        val entity = parseMealJsonObject(obj, today, fallbackSlot = mapKeyToSlot(key))
                                        if (entity != null) importedMeals.add(entity)
                                    }
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Fallback to text parsing if JSON parse failed
                }
            }

            // 2. If JSON parsing didn't produce meals, use Natural Language Regex Line Parser
            if (importedMeals.isEmpty()) {
                val lines = clean.lines()
                var currentSlot: MealSlot = MealSlot.BREAKFAST
                var currentTitle = ""
                var currentDesc = ""
                var currentIngredients = mutableListOf<String>()
                var currentProtein = 0
                var currentCarbs = 0
                var currentFat = 0
                var currentKcal = 0

                fun commitCurrentMeal() {
                    if (currentTitle.isNotBlank()) {
                        val id = "meal-" + UUID.randomUUID().toString().take(8)
                        val kcal = if (currentKcal > 0) currentKcal else (currentProtein * 4 + currentCarbs * 4 + currentFat * 9)
                        importedMeals.add(
                            MealEntity(
                                id = id,
                                slot = currentSlot,
                                title = currentTitle.trim(),
                                description = currentDesc.ifBlank { "Comida importada desde IA externa" },
                                prepTimeMinutes = 15,
                                ingredients = if (currentIngredients.isNotEmpty()) currentIngredients else listOf(currentTitle),
                                usesBatchCookedBase = false,
                                allIngredientsInStock = true,
                                bioImpact = BioGlycemicImpact.MODERATE_STEADY,
                                isCompleted = false,
                                proteinGrams = currentProtein,
                                carbsGrams = currentCarbs,
                                fatGrams = currentFat,
                                caloriesKcal = kcal,
                                dateIso = today
                            )
                        )
                        currentTitle = ""
                        currentDesc = ""
                        currentIngredients = mutableListOf()
                        currentProtein = 0
                        currentCarbs = 0
                        currentFat = 0
                        currentKcal = 0
                    }
                }

                for (line in lines) {
                    val trimmed = line.trim().removePrefix("-").removePrefix("*").trim()
                    if (trimmed.isBlank()) continue

                    val lower = trimmed.lowercase(Locale.ROOT)
                    val detectedSlot = when {
                        lower.contains("desayuno") || lower.contains("breakfast") -> MealSlot.BREAKFAST
                        lower.contains("almuerzo") || lower.contains("comida") || lower.contains("lunch") -> MealSlot.LUNCH
                        lower.contains("cena") || lower.contains("dinner") -> MealSlot.DINNER
                        lower.contains("snack") || lower.contains("merienda") || lower.contains("tentempi") -> MealSlot.SNACK
                        else -> null
                    }

                    if (detectedSlot != null) {
                        commitCurrentMeal()
                        currentSlot = detectedSlot
                        val afterColon = if (trimmed.contains(":")) trimmed.substringAfter(":").trim() else ""
                        if (afterColon.isNotBlank()) {
                            currentTitle = extractTitleWithoutMacros(afterColon)
                            currentDesc = afterColon
                            extractMacrosFromText(afterColon).let { (p, c, f, k) ->
                                currentProtein = p
                                currentCarbs = c
                                currentFat = f
                                currentKcal = k
                            }
                        }
                    } else if (currentTitle.isBlank()) {
                        currentTitle = extractTitleWithoutMacros(trimmed)
                        currentDesc = trimmed
                        extractMacrosFromText(trimmed).let { (p, c, f, k) ->
                            currentProtein = p
                            currentCarbs = c
                            currentFat = f
                            currentKcal = k
                        }
                    } else {
                        // Additional line details / ingredients
                        val items = trimmed.split(",", ";", "•", "+").map { it.trim() }.filter { it.length > 2 }
                        currentIngredients.addAll(items)
                        extractMacrosFromText(trimmed).let { (p, c, f, k) ->
                            if (p > 0) currentProtein = p
                            if (c > 0) currentCarbs = c
                            if (f > 0) currentFat = f
                            if (k > 0) currentKcal = k
                        }
                    }
                }
                commitCurrentMeal()
            }

            if (importedMeals.isEmpty()) {
                return@withContext Result.failure(IllegalArgumentException("No valid meals could be detected in the provided text."))
            }

            mealDao.insertMeals(importedMeals)
            Result.success(importedMeals.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun mapKeyToSlot(key: String): MealSlot {
        val lower = key.lowercase(Locale.ROOT)
        return when {
            lower.contains("break") || lower.contains("desay") -> MealSlot.BREAKFAST
            lower.contains("lunch") || lower.contains("almu") || lower.contains("comida") -> MealSlot.LUNCH
            lower.contains("din") || lower.contains("cena") -> MealSlot.DINNER
            else -> MealSlot.SNACK
        }
    }

    private fun parseMealJsonObject(obj: JSONObject, today: String, fallbackSlot: MealSlot = MealSlot.BREAKFAST): MealEntity? {
        val title = obj.optString("title").ifBlank { obj.optString("name") }
        if (title.isBlank()) return null

        val slotStr = obj.optString("slot").ifBlank { fallbackSlot.name }
        val slot = try {
            MealSlot.valueOf(slotStr.uppercase(Locale.ROOT))
        } catch (e: Exception) {
            fallbackSlot
        }

        val desc = obj.optString("description").ifBlank { obj.optString("notes", "Comida importada de IA") }
        val prepTime = obj.optInt("prepTimeMinutes", obj.optInt("prep_time", 15))
        val p = obj.optInt("proteinGrams", obj.optInt("protein", 0))
        val c = obj.optInt("carbsGrams", obj.optInt("carbs", 0))
        val f = obj.optInt("fatGrams", obj.optInt("fat", 0))
        val kcal = obj.optInt("caloriesKcal", obj.optInt("calories", (p * 4 + c * 4 + f * 9)))

        val ingredientsList = mutableListOf<String>()
        val ingArray = obj.optJSONArray("ingredients")
        if (ingArray != null) {
            for (j in 0 until ingArray.length()) {
                val ing = ingArray.optString(j)
                if (ing.isNotBlank()) ingredientsList.add(ing)
            }
        }

        val id = "meal-" + UUID.randomUUID().toString().take(8)
        return MealEntity(
            id = id,
            slot = slot,
            title = title,
            description = desc,
            prepTimeMinutes = prepTime,
            ingredients = if (ingredientsList.isNotEmpty()) ingredientsList else listOf(title),
            usesBatchCookedBase = obj.optBoolean("usesBatchCookedBase", false),
            allIngredientsInStock = true,
            bioImpact = BioGlycemicImpact.MODERATE_STEADY,
            isCompleted = false,
            customSlotName = obj.optString("customSlotName", null),
            proteinGrams = p,
            carbsGrams = c,
            fatGrams = f,
            caloriesKcal = kcal,
            dateIso = today
        )
    }

    private fun extractTitleWithoutMacros(text: String): String {
        return text.replace(Regex("\\(.*\\)"), "")
            .replace(Regex("\\[.*\\]"), "")
            .replace(Regex("\\|.*"), "")
            .trim()
    }

    private fun extractMacrosFromText(text: String): Quadruple<Int, Int, Int, Int> {
        var protein = 0
        var carbs = 0
        var fat = 0
        var kcal = 0

        val pMatch = Regex("(\\d+)\\s*(?:g|gr|gramos)?\\s*(?:de\\s*)?(?:prote[ií]na|protein|prot|p\\b)", RegexOption.IGNORE_CASE).find(text)
        if (pMatch != null) protein = pMatch.groupValues[1].toIntOrNull() ?: 0

        val cMatch = Regex("(\\d+)\\s*(?:g|gr|gramos)?\\s*(?:de\\s*)?(?:carbos|carbohidratos|carbs|hc|c\\b)", RegexOption.IGNORE_CASE).find(text)
        if (cMatch != null) carbs = cMatch.groupValues[1].toIntOrNull() ?: 0

        val fMatch = Regex("(\\d+)\\s*(?:g|gr|gramos)?\\s*(?:de\\s*)?(?:grasas|grasa|fat|g\\b)", RegexOption.IGNORE_CASE).find(text)
        if (fMatch != null) fat = fMatch.groupValues[1].toIntOrNull() ?: 0

        val kMatch = Regex("(\\d+)\\s*(?:kcal|calor[ií]as|calories|cal\\b)", RegexOption.IGNORE_CASE).find(text)
        if (kMatch != null) kcal = kMatch.groupValues[1].toIntOrNull() ?: 0

        return Quadruple(protein, carbs, fat, kcal)
    }

    private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

    // --- 5. Hábitos Ancla (Habits & Grace Days) ---

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
        habitDao.deleteHabit(id)
    }

    suspend fun restoreHabit(habit: HabitAnchor) {
        habitDao.insertHabit(habit.toEntity())
    }

    suspend fun toggleHabitComplete(habit: HabitAnchor) {
        val newCompleted = !habit.isCompleted
        val newStreak = if (newCompleted) habit.streakDays + 1 else maxOf(0, habit.streakDays - 1)
        habitDao.updateHabit(habit.toEntity().copy(isCompleted = newCompleted, streakDays = newStreak))

        if (newCompleted) {
            logActionAndRecalculate(CompletionItemType.HABIT, habit.id, habit.title, CompletionStatus.COMPLETED)
        }
    }

    suspend fun applyGraceDay(habit: HabitAnchor) {
        habitDao.updateHabit(habit.toEntity().copy(graceDaysUsed = habit.graceDaysUsed + 1))
        logActionAndRecalculate(CompletionItemType.HABIT, habit.id, habit.title, CompletionStatus.PARTIAL)
    }

    // --- 6. Biometría & Modos Biológicos ---

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

    suspend fun saveBiometricBaseline(biometric: BiometricBaseline) {
        val today = AetherDateUtils.getTodayIso()
        val entity = biometric.toEntity().copy(date = today)
        biometricDao.insertBiometric(entity)
    }

    suspend fun updateChronotype(chronotype: Chronotype, currentReadiness: Int) {
        val today = AetherDateUtils.getTodayIso()
        val current = biometricDao.getBiometric(today).first()
        if (current != null) {
            biometricDao.insertBiometric(current.copy(chronotype = chronotype))
        } else {
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
    }

    suspend fun setRecoveryMode(enabled: Boolean, currentScore: Int) {
        val today = AetherDateUtils.getTodayIso()
        val current = biometricDao.getBiometric(today).first()
        val targetScore = if (enabled) 45 else maxOf(65, currentScore)
        if (current != null) {
            biometricDao.insertBiometric(
                current.copy(
                    readinessScore = targetScore,
                    recoveryModeTriggered = enabled,
                    graceDayActive = enabled
                )
            )
        } else {
            biometricDao.insertBiometric(
                BiometricEntity(
                    date = today,
                    readinessScore = targetScore,
                    perceivedEnergy = if (enabled) 40 else maxOf(65, currentScore),
                    chronotype = Chronotype.BEAR,
                    recoveryModeTriggered = enabled,
                    graceDayActive = enabled
                )
            )
        }
    }

    suspend fun breakDownTask(taskTitle: String, minutes: Int, language: AppLanguage): List<String> {
        return geminiEngine.breakDownTask(taskTitle, minutes, language)
    }

    suspend fun resetToCleanSlate(language: AppLanguage, wipeHistory: Boolean = false) {
        AetherDatabase.populateCleanSlate(database, language, wipeHistory = wipeHistory)
        appContext?.let { FrogTaskWidgetProvider.updateAllWidgets(it) }
    }

    suspend fun populateDemoData(language: AppLanguage) {
        AetherDatabase.populateInitialAetherData(database, language)
        appContext?.let { FrogTaskWidgetProvider.updateAllWidgets(it) }
    }

    // --- 7. Orquestación IA, Streaming & Mensajes Persistentes ---

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

        // Schedule local alarms for time blocks if context is available
        appContext?.let { ctx ->
            AetherNotificationScheduler.scheduleTimeBlockAlerts(ctx, result.plan.time_blocks)
            FrogTaskWidgetProvider.updateAllWidgets(ctx)
        }

        return result
    }

    suspend fun getCognitiveReframe(userFeeling: String, readinessScore: Int): String {
        return geminiEngine.generateCognitiveReframe(userFeeling, readinessScore)
    }

    fun streamChatResponse(prompt: String, context: AetherAiContext): Flow<String> {
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

    // --- 8. Copias de Seguridad Completas (Full Backup & Restore) ---

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
