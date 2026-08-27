package com.example.data.repository

import com.example.data.local.CompletionLogDao
import com.example.data.local.DailySummaryDao
import com.example.data.local.PreferencesManager
import com.example.data.mapper.toModel
import com.example.data.model.*
import com.example.data.remote.AetherAiContext
import com.example.data.usecase.DailyRolloverUseCase
import com.example.data.usecase.MealImportUseCase
import com.example.data.util.AetherDateUtils
import com.example.ui.i18n.AppLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File

/**
 * High-Level Coordinator / Facade Repository for Aether OS.
 * Decomposed into specialized Domain Repositories and Use Cases for Clean Architecture:
 * - TaskRepository
 * - HabitRepository
 * - MealRepository
 * - BiometricRepository
 * - BackupRepository
 * - AiRepository
 * - DailyRolloverUseCase
 * - MealImportUseCase
 * Completely decoupled from Android Context for pure JVM unit testability.
 */
class AetherRepository(
    private val taskRepo: TaskRepository,
    private val habitRepo: HabitRepository,
    private val mealRepo: MealRepository,
    private val biometricRepo: BiometricRepository,
    private val backupRepo: BackupRepository,
    private val aiRepo: AiRepository,
    private val dailyRolloverUseCase: DailyRolloverUseCase,
    private val mealImportUseCase: MealImportUseCase,
    private val preferencesManager: PreferencesManager,
    private val completionLogDao: CompletionLogDao,
    private val dailySummaryDao: DailySummaryDao
) {
    // --- Reactive Data Streams ---
    val tasks: Flow<List<TaskItem>> = taskRepo.tasks
    val archivedTasks: Flow<List<TaskItem>> = taskRepo.archivedTasks
    val timeBlocks: Flow<List<TimeBlock>> = taskRepo.timeBlocks
    val habits: Flow<List<HabitAnchor>> = habitRepo.habits
    val meals: Flow<List<MealItem>> = mealRepo.meals
    val pantryItems: Flow<List<PantryItem>> = mealRepo.pantryItems
    val biometric: Flow<BiometricBaseline> = biometricRepo.biometric
    val recentBiometrics: Flow<List<BiometricBaseline>> = biometricRepo.recentBiometrics
    val aiMessages: Flow<List<AiMessage>> = aiRepo.aiMessages
    val favoriteAiMessages: Flow<List<AiMessage>> = aiRepo.favoriteAiMessages

    val dailySummaries: Flow<List<DailySummary>> = dailySummaryDao.getAllSummaries().map { list ->
        list.map { it.toModel() }
    }

    // --- Preferences / Localization ---
    fun getLanguage(): Flow<AppLanguage> = preferencesManager.languageFlow

    suspend fun saveLanguage(lang: AppLanguage) {
        preferencesManager.saveLanguage(lang)
    }

    // --- Daily Rollover & History Logs ---
    suspend fun checkAndPerformDailyRollover(): DailyRolloverResult? {
        return dailyRolloverUseCase.execute()
    }

    fun getLogsByDate(dateIso: String): Flow<List<CompletionLog>> {
        return completionLogDao.getLogsByDate(dateIso).map { list ->
            list.map { it.toModel() }
        }
    }

    fun getLogsByItemId(itemId: String): Flow<List<CompletionLog>> {
        return completionLogDao.getLogsByItemId(itemId).map { list ->
            list.map { it.toModel() }
        }
    }

    suspend fun recalculateDailySummary(dateIso: String = AetherDateUtils.getTodayIso()) {
        taskRepo.recalculateDailySummary(dateIso)
    }

    // --- Task Domain Operations ---
    suspend fun addTask(
        title: String,
        description: String,
        energyLevel: EnergyLevel,
        priorityType: PriorityType,
        estimatedMinutes: Int,
        category: String,
        makeFrog: Boolean = false
    ) = taskRepo.addTask(title, description, energyLevel, priorityType, estimatedMinutes, category, makeFrog)

    suspend fun updateTask(task: TaskItem) = taskRepo.updateTask(task)
    suspend fun restoreTask(task: TaskItem) = taskRepo.restoreTask(task)
    suspend fun reorderTasks(tasks: List<TaskItem>) = taskRepo.reorderTasks(tasks)
    suspend fun toggleTaskComplete(task: TaskItem) = taskRepo.toggleTaskComplete(task)
    suspend fun setTaskAsFrog(taskId: String) = taskRepo.setTaskAsFrog(taskId)
    suspend fun deleteTask(taskId: String) = taskRepo.deleteTask(taskId)
    suspend fun breakDownTask(taskTitle: String, minutes: Int, language: AppLanguage) =
        taskRepo.breakDownTask(taskTitle, minutes, language)

    // --- TimeBlock Domain Operations ---
    suspend fun addTimeBlock(
        startTime: String,
        endTime: String,
        blockType: BlockType,
        title: String,
        notes: String = ""
    ) = taskRepo.addTimeBlock(startTime, endTime, blockType, title, notes)

    suspend fun updateTimeBlock(block: TimeBlock) = taskRepo.updateTimeBlock(block)
    suspend fun restoreTimeBlock(block: TimeBlock) = taskRepo.restoreTimeBlock(block)
    suspend fun reorderTimeBlocks(blocks: List<TimeBlock>) = taskRepo.reorderTimeBlocks(blocks)
    suspend fun toggleTimeBlockComplete(block: TimeBlock) = taskRepo.toggleTimeBlockComplete(block)
    suspend fun deleteTimeBlock(id: String) = taskRepo.deleteTimeBlock(id)

    // --- Habit Domain Operations ---
    suspend fun addHabit(
        title: String,
        description: String,
        anchor: CircadianAnchor,
        streakDays: Int = 0,
        reframingTip: String = "Biological consistency is a pattern of return, not perfection."
    ) = habitRepo.addHabit(title, description, anchor, streakDays, reframingTip)

    suspend fun updateHabit(habit: HabitAnchor) = habitRepo.updateHabit(habit)
    suspend fun deleteHabit(id: String) = habitRepo.deleteHabit(id)
    suspend fun restoreHabit(habit: HabitAnchor) = habitRepo.restoreHabit(habit)
    suspend fun toggleHabitComplete(habit: HabitAnchor) = habitRepo.toggleHabitComplete(habit)
    suspend fun applyGraceDay(habit: HabitAnchor): Result<Unit> = habitRepo.applyGraceDay(habit)
    suspend fun clearPendingHabitStreaks() = taskRepo.clearPendingHabitStreaks()
    suspend fun getCognitiveReframe(userFeeling: String, readinessScore: Int): String =
        habitRepo.getCognitiveReframe(userFeeling, readinessScore)

    // --- Meal & Pantry Domain Operations ---
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
    ) = mealRepo.addMeal(
        slot, title, description, prepTimeMinutes, ingredients,
        usesBatchCookedBase, allIngredientsInStock, bioImpact, customSlotName,
        proteinGrams, carbsGrams, fatGrams, caloriesKcal, dateIso
    )

    suspend fun duplicateMeal(meal: MealItem, targetDateIso: String = AetherDateUtils.getTodayIso(), copySuffix: Boolean = false) =
        mealRepo.duplicateMeal(meal, targetDateIso, copySuffix)

    suspend fun updateMeal(meal: MealItem) = mealRepo.updateMeal(meal)
    suspend fun restoreMeal(meal: MealItem) = mealRepo.restoreMeal(meal)
    suspend fun toggleMealComplete(meal: MealItem) = mealRepo.toggleMealComplete(meal)
    suspend fun deleteMeal(id: String) = mealRepo.deleteMeal(id)
    suspend fun importMealsFromExternalAI(rawTextOrJson: String): Result<Int> = mealImportUseCase.execute(rawTextOrJson)

    suspend fun addPantryItem(
        name: String,
        category: PantryCategory,
        inStock: Boolean,
        isBatchBase: Boolean,
        quantityDescription: String
    ) = mealRepo.addPantryItem(name, category, inStock, isBatchBase, quantityDescription)

    suspend fun updatePantryItem(item: PantryItem) = mealRepo.updatePantryItem(item)
    suspend fun restorePantryItem(item: PantryItem) = mealRepo.restorePantryItem(item)
    suspend fun togglePantryStock(id: String, inStock: Boolean) = mealRepo.togglePantryStock(id, inStock)
    suspend fun deletePantryItem(id: String) = mealRepo.deletePantryItem(id)

    // --- Biometric Domain Operations ---
    suspend fun updateReadiness(score: Int) = biometricRepo.updateReadiness(score)
    suspend fun saveBiometricBaseline(biometric: BiometricBaseline) = biometricRepo.saveBiometricBaseline(biometric)
    suspend fun updateChronotype(chronotype: Chronotype, currentReadiness: Int) =
        biometricRepo.updateChronotype(chronotype, currentReadiness)
    suspend fun setRecoveryMode(enabled: Boolean, currentScore: Int) =
        biometricRepo.setRecoveryMode(enabled, currentScore)

    // --- AI Domain Operations ---
    suspend fun orchestrateDailyPlan(
        readiness: Int,
        chronotype: Chronotype,
        currentTasks: List<TaskItem>,
        currentPantry: List<PantryItem>,
        context: android.content.Context? = null
    ): AetherEngineResult = aiRepo.orchestrateDailyPlan(readiness, chronotype, currentTasks, currentPantry, context)

    fun streamChatResponse(prompt: String, context: AetherAiContext): Flow<String> =
        aiRepo.streamChatResponse(prompt, context)

    suspend fun saveAiMessage(message: AiMessage) = aiRepo.saveAiMessage(message)
    suspend fun updateAiMessage(message: AiMessage) = aiRepo.updateAiMessage(message)
    suspend fun toggleAiMessageFavorite(id: String, isFavorite: Boolean) =
        aiRepo.toggleAiMessageFavorite(id, isFavorite)
    suspend fun deleteAiMessage(id: String) = aiRepo.deleteAiMessage(id)
    suspend fun clearAllAiMessages() = aiRepo.clearAllAiMessages()
    fun exportPlanAsJson(plan: AetherDailyPlan): String = aiRepo.exportPlanAsJson(plan)

    // --- Backup & Restore Domain Operations ---
    suspend fun createFullBackupJson(): String = backupRepo.createFullBackupJson()
    suspend fun exportFullBackupToFile(destinationFile: File): Result<String> =
        backupRepo.exportFullBackupToFile(destinationFile)
    suspend fun restoreFromBackupJson(jsonString: String): Result<Unit> =
        backupRepo.restoreFromBackupJson(jsonString)
    suspend fun resetToCleanSlate(language: AppLanguage, wipeHistory: Boolean = false) =
        backupRepo.resetToCleanSlate(language, wipeHistory)
    suspend fun populateDemoData(language: AppLanguage) = backupRepo.populateDemoData(language)
    suspend fun resetDataToLanguage(language: AppLanguage) = backupRepo.resetDataToLanguage(language)
}
