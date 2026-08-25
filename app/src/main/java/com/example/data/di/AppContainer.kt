package com.example.data.di

import android.content.Context
import com.example.data.local.AetherDatabase
import com.example.data.local.DataStorePreferencesManager
import com.example.data.local.PreferencesManager
import com.example.data.remote.AetherGeminiEngine
import com.example.data.repository.*
import com.example.data.usecase.DailyRolloverUseCase
import com.example.data.usecase.MealImportUseCase
import com.example.data.util.AndroidWidgetUpdater
import com.example.data.util.WidgetUpdater

/**
 * Dependency Injection Container for providing singletons and decoupled dependencies
 * across the application.
 */
interface AppContainer {
    val database: AetherDatabase
    val geminiEngine: AetherGeminiEngine
    val widgetUpdater: WidgetUpdater
    val preferencesManager: PreferencesManager
    val taskRepository: TaskRepository
    val habitRepository: HabitRepository
    val mealRepository: MealRepository
    val biometricRepository: BiometricRepository
    val backupRepository: BackupRepository
    val aiRepository: AiRepository
    val dailyRolloverUseCase: DailyRolloverUseCase
    val mealImportUseCase: MealImportUseCase
    val repository: AetherRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    override val database: AetherDatabase by lazy {
        AetherDatabase.getDatabase(context)
    }

    override val geminiEngine: AetherGeminiEngine by lazy {
        AetherGeminiEngine()
    }

    override val widgetUpdater: WidgetUpdater by lazy {
        AndroidWidgetUpdater(context)
    }

    override val preferencesManager: PreferencesManager by lazy {
        DataStorePreferencesManager(context)
    }

    override val taskRepository: TaskRepository by lazy {
        TaskRepositoryImpl(
            taskDao = database.taskDao(),
            timeBlockDao = database.timeBlockDao(),
            completionLogDao = database.completionLogDao(),
            dailySummaryDao = database.dailySummaryDao(),
            habitDao = database.habitDao(),
            mealDao = database.mealDao(),
            quickNoteDao = database.quickNoteDao(),
            focusSessionDao = database.focusSessionDao(),
            geminiEngine = geminiEngine,
            widgetUpdater = widgetUpdater
        )
    }

    override val habitRepository: HabitRepository by lazy {
        HabitRepositoryImpl(
            habitDao = database.habitDao(),
            taskDao = database.taskDao(),
            mealDao = database.mealDao(),
            completionLogDao = database.completionLogDao(),
            dailySummaryDao = database.dailySummaryDao(),
            geminiEngine = geminiEngine
        )
    }

    override val mealRepository: MealRepository by lazy {
        MealRepositoryImpl(
            mealDao = database.mealDao(),
            pantryDao = database.pantryDao(),
            taskDao = database.taskDao(),
            habitDao = database.habitDao(),
            completionLogDao = database.completionLogDao(),
            dailySummaryDao = database.dailySummaryDao()
        )
    }

    override val biometricRepository: BiometricRepository by lazy {
        BiometricRepositoryImpl(
            biometricDao = database.biometricDao()
        )
    }

    override val backupRepository: BackupRepository by lazy {
        BackupRepositoryImpl(
            database = database,
            taskDao = database.taskDao(),
            timeBlockDao = database.timeBlockDao(),
            pantryDao = database.pantryDao(),
            mealDao = database.mealDao(),
            habitDao = database.habitDao(),
            biometricDao = database.biometricDao(),
            completionLogDao = database.completionLogDao(),
            dailySummaryDao = database.dailySummaryDao(),
            widgetUpdater = widgetUpdater
        )
    }

    override val aiRepository: AiRepository by lazy {
        AiRepositoryImpl(
            aiMessageDao = database.aiMessageDao(),
            timeBlockDao = database.timeBlockDao(),
            geminiEngine = geminiEngine,
            widgetUpdater = widgetUpdater
        )
    }

    override val dailyRolloverUseCase: DailyRolloverUseCase by lazy {
        DailyRolloverUseCase(
            database = database,
            taskDao = database.taskDao(),
            timeBlockDao = database.timeBlockDao(),
            habitDao = database.habitDao(),
            mealDao = database.mealDao(),
            biometricDao = database.biometricDao(),
            completionLogDao = database.completionLogDao(),
            dailySummaryDao = database.dailySummaryDao(),
            preferencesManager = preferencesManager,
            widgetUpdater = widgetUpdater
        )
    }

    override val mealImportUseCase: MealImportUseCase by lazy {
        MealImportUseCase(
            mealDao = database.mealDao()
        )
    }

    override val repository: AetherRepository by lazy {
        AetherRepository(
            taskRepo = taskRepository,
            habitRepo = habitRepository,
            mealRepo = mealRepository,
            biometricRepo = biometricRepository,
            backupRepo = backupRepository,
            aiRepo = aiRepository,
            dailyRolloverUseCase = dailyRolloverUseCase,
            mealImportUseCase = mealImportUseCase,
            preferencesManager = preferencesManager,
            completionLogDao = database.completionLogDao(),
            dailySummaryDao = database.dailySummaryDao()
        )
    }
}
