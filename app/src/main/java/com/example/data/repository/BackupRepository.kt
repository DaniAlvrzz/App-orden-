package com.example.data.repository

import com.example.data.local.*
import com.example.data.model.AetherFullBackup
import com.example.data.util.AetherDateUtils
import com.example.data.util.NoOpWidgetUpdater
import com.example.data.util.WidgetUpdater
import com.example.ui.i18n.AppLanguage
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File

interface BackupRepository {
    suspend fun createFullBackupJson(): String
    suspend fun restoreFromBackupJson(jsonString: String): Result<Unit>
    suspend fun exportFullBackupToFile(destinationFile: File): Result<String>
    suspend fun resetToCleanSlate(language: AppLanguage, wipeHistory: Boolean = false)
    suspend fun populateDemoData(language: AppLanguage)
    suspend fun resetDataToLanguage(language: AppLanguage)
}

class BackupRepositoryImpl(
    private val database: AetherDatabase,
    private val taskDao: TaskDao,
    private val timeBlockDao: TimeBlockDao,
    private val pantryDao: PantryDao,
    private val mealDao: MealDao,
    private val habitDao: HabitDao,
    private val biometricDao: BiometricDao,
    private val completionLogDao: CompletionLogDao,
    private val dailySummaryDao: DailySummaryDao,
    private val widgetUpdater: WidgetUpdater = NoOpWidgetUpdater
) : BackupRepository {

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val backupAdapter = moshi.adapter(AetherFullBackup::class.java).indent("  ")

    override suspend fun createFullBackupJson(): String = withContext(Dispatchers.IO) {
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

    override suspend fun exportFullBackupToFile(destinationFile: File): Result<String> = withContext(Dispatchers.IO) {
        try {
            val jsonContent = createFullBackupJson()
            if (destinationFile.parentFile?.exists() != true) {
                destinationFile.parentFile?.mkdirs()
            }
            destinationFile.writeText(jsonContent)
            Result.success(destinationFile.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun restoreFromBackupJson(jsonString: String): Result<Unit> = withContext(Dispatchers.IO) {
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

            widgetUpdater.updateWidgets()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun resetToCleanSlate(language: AppLanguage, wipeHistory: Boolean) {
        AetherDatabase.populateCleanSlate(database, language, wipeHistory = wipeHistory)
        widgetUpdater.updateWidgets()
    }

    override suspend fun populateDemoData(language: AppLanguage) {
        AetherDatabase.populateInitialAetherData(database, language)
        widgetUpdater.updateWidgets()
    }

    override suspend fun resetDataToLanguage(language: AppLanguage) {
        AetherDatabase.clearAllAetherData(database, clearHistory = false)
        AetherDatabase.populateInitialAetherData(database, language)
        widgetUpdater.updateWidgets()
    }
}
