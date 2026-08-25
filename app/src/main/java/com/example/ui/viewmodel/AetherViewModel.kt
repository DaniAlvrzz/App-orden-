package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.AetherApplication
import com.example.data.local.AetherDatabase
import com.example.data.model.*
import com.example.data.remote.AetherGeminiEngine
import com.example.data.repository.AchievementRepository
import com.example.data.repository.AetherRepository
import com.example.data.util.AetherDateUtils
import com.example.service.FocusTimerWorker
import com.example.ui.i18n.AppLanguage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.util.Locale

data class AetherUiState(
    val tasks: List<TaskItem> = emptyList(),
    val timeBlocks: List<TimeBlock> = emptyList(),
    val pantryItems: List<PantryItem> = emptyList(),
    val meals: List<MealItem> = emptyList(),
    val habits: List<HabitAnchor> = emptyList(),
    val biometric: BiometricBaseline = BiometricBaseline(),
    val recentBiometrics: List<BiometricBaseline> = emptyList(),
    val dailyPlan: AetherDailyPlan? = null,
    val isOrchestrating: Boolean = false,
    val aiEngineStatus: AiStatus = AiStatus.IDLE,
    val reframeResponse: String? = null,
    val isReframing: Boolean = false,
    val filterEnergyLevel: EnergyLevel? = null,
    val searchQuery: String = "",
    val activeTab: Int = 0,
    val showQuickAddDialog: Boolean = false,
    val showReframeDialog: Boolean = false,
    val showJsonInspector: Boolean = false,
    val showBatchBaseDialog: Boolean = false,
    val showPantryAddDialog: Boolean = false,
    val showSettingsDialog: Boolean = false,
    val showTutorialDialog: Boolean = false,
    val showAddTimeBlockDialog: Boolean = false,
    val showAddMealDialog: Boolean = false,
    val showAddHabitDialog: Boolean = false,
    val showAchievementsDialog: Boolean = false,
    val showFrogCelebration: Boolean = false,
    val celebratingFrogTaskTitle: String = "",
    val achievements: List<AchievementItem> = emptyList(),
    val newlyUnlockedAchievement: AchievementItem? = null,
    val tutorialStepIndex: Int = 0,
    val currentLanguage: AppLanguage = AppLanguage.SPANISH,
    val statusMessage: String? = null,
    // Active editing items (dialogs open when non-null)
    val editingTask: TaskItem? = null,
    val editingTimeBlock: TimeBlock? = null,
    val editingPantryItem: PantryItem? = null,
    val editingMeal: MealItem? = null,
    val editingHabit: HabitAnchor? = null,
    // Undo mechanism
    val lastDeletedTask: TaskItem? = null,
    val lastDeletedTimeBlock: TimeBlock? = null,
    val lastDeletedPantryItem: PantryItem? = null,
    val lastDeletedMeal: MealItem? = null,
    val lastDeletedHabit: HabitAnchor? = null,
    val undoMessage: String? = null,
    // Focus Timer
    val isFocusTimerRunning: Boolean = false,
    val focusSecondsRemaining: Int = 25 * 60,
    val activeFocusTask: TaskItem? = null,
    // Module 2: Persistent History State
    val showHistoryDialog: Boolean = false,
    val historyViewMode: HistoryViewMode = HistoryViewMode.MONTH,
    val selectedHistoryYear: Int = LocalDate.now().year,
    val selectedHistoryMonth: Int = LocalDate.now().monthValue,
    val selectedHistoryDateIso: String = LocalDate.now().toString(),
    val historySummaries: List<DailySummary> = emptyList(),
    val historyLogsForSelectedDay: List<CompletionLog> = emptyList(),
    // Clean Slate & Backup Options
    val wipeHistoryWithCleanSlate: Boolean = false,
    val showRestoreBackupDialog: Boolean = false,
    // Fase 3 Módulo 4: Dopamine Visuals & Gamification System
    val userLevelInfo: UserLevelInfo = UserLevelInfo(),
    val levelUpCelebrationLevel: Int? = null,
    val habitConfettiKey: Long? = null,
    val newlyUnlockedAchievementModal: AchievementItem? = null,
    // Fase 3 Módulo 5: Núcleo IA Mejorado & Chat Persistente
    val aiMessages: List<AiMessage> = emptyList(),
    val favoriteAiMessages: List<AiMessage> = emptyList(),
    val selectedAiTab: Int = 0, // 0: Chat, 1: Favorites, 2: JSON Schema
    val isAiStreaming: Boolean = false,
    val isAiThinking: Boolean = false,
    val activeStreamingMessageId: String? = null,
    val activeStreamingContent: String = ""
) {
    // Cognitive ceiling computation: Deep work sum
    val deepWorkMinutesAllocated: Int
        get() = timeBlocks
            .filter { it.blockType == BlockType.DEEP_WORK }
            .sumOf { block ->
                calculateMinutesBetween(block.startTime, block.endTime)
            }

    val maxCognitiveCeilingMinutes: Int = 210 // 3.5h Law

    val isCeilingExceeded: Boolean
        get() = deepWorkMinutesAllocated > maxCognitiveCeilingMinutes

    val frogTask: TaskItem?
        get() = tasks.firstOrNull { it.isFrog } ?: tasks.firstOrNull { it.energyLevel == EnergyLevel.HIGH }

    val mediumTasks: List<TaskItem>
        get() = tasks.filter { !it.isFrog && it.energyLevel == EnergyLevel.MEDIUM }

    val quickTasks: List<TaskItem>
        get() = tasks.filter { !it.isFrog && it.energyLevel == EnergyLevel.LOW }

    private fun calculateMinutesBetween(start: String, end: String): Int {
        return try {
            val sTime = parseFlexibleLocalTime(start)
            val eTime = parseFlexibleLocalTime(end)
            val minutes = Duration.between(sTime, eTime).toMinutes().toInt()
            if (minutes > 0) minutes else 60
        } catch (e: Exception) {
            60
        }
    }

    private fun parseFlexibleLocalTime(timeStr: String): LocalTime {
        val clean = timeStr.trim().uppercase(Locale.US)
        val timeOnly = clean.replace(Regex("[^0-9:]"), "")
        val parts = timeOnly.split(":")
        var hour = parts.getOrNull(0)?.toIntOrNull() ?: 0
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
        if (clean.contains("PM") && hour < 12) hour += 12
        if (clean.contains("AM") && hour == 12) hour = 0
        return LocalTime.of(hour.coerceIn(0, 23), minute.coerceIn(0, 59))
    }
}

class AetherViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: AetherRepository = (application as? AetherApplication)?.container?.repository
        ?: AetherRepository(
            AetherDatabase.getDatabase(application),
            AetherGeminiEngine(),
            application
        ),
    private val achievementRepository: AchievementRepository = AchievementRepository(application)
) : AndroidViewModel(application) {

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as AetherApplication)
                AetherViewModel(
                    application = app,
                    repository = app.container.repository,
                    achievementRepository = AchievementRepository(app)
                )
            }
        }
    }

    private val _uiState = MutableStateFlow(AetherUiState())
    val uiState: StateFlow<AetherUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var celebrationJob: Job? = null
    private var historyLogsJob: Job? = null

    init {
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            repository.getLanguage().distinctUntilChanged().collect { savedLang ->
                _uiState.value = _uiState.value.copy(currentLanguage = savedLang)
            }
        }

        viewModelScope.launch {
            achievementRepository.achievements.collect { achievementsList ->
                _uiState.value = _uiState.value.copy(achievements = achievementsList)
            }
        }

        viewModelScope.launch {
            achievementRepository.userLevelInfo.collect { levelInfo ->
                _uiState.value = _uiState.value.copy(userLevelInfo = levelInfo)
            }
        }

        // Collect daily summaries for persistent history
        viewModelScope.launch {
            repository.dailySummaries.collect { summaries ->
                _uiState.value = _uiState.value.copy(historySummaries = summaries)
            }
        }

        // Collect persistent AI conversation messages & favorites
        viewModelScope.launch {
            repository.aiMessages.collect { messages ->
                _uiState.value = _uiState.value.copy(aiMessages = messages)
            }
        }

        viewModelScope.launch {
            repository.favoriteAiMessages.collect { favs ->
                _uiState.value = _uiState.value.copy(favoriteAiMessages = favs)
            }
        }

        // Observe selected day logs
        loadLogsForSelectedDate(_uiState.value.selectedHistoryDateIso)

        viewModelScope.launch {
            combine(
                repository.tasks,
                repository.timeBlocks,
                repository.pantryItems,
                repository.meals,
                repository.habits
            ) { tasks, timeBlocks, pantry, meals, habits ->
                Tuple5(tasks, timeBlocks, pantry, meals, habits)
            }.combine(repository.biometric) { (tasks, timeBlocks, pantry, meals, habits), bio ->
                Tuple6(tasks, timeBlocks, pantry, meals, habits, bio)
            }.combine(repository.recentBiometrics) { (tasks, timeBlocks, pantry, meals, habits, bio), recents ->
                // Check automated achievements
                if (habits.any { it.streakDays >= 7 }) {
                    unlockAchievement(AchievementId.STREAK_7_DAYS)
                }
                if (habits.any { it.streakDays >= 30 }) {
                    unlockAchievement(AchievementId.STREAK_30_DAYS)
                }
                if (habits.isNotEmpty() && habits.all { it.isCompleted }) {
                    unlockAchievement(AchievementId.PERFECT_DAY)
                }
                if (pantry.count { it.inStock } >= 5) {
                    unlockAchievement(AchievementId.PANTRY_5_ITEMS)
                }
                val completedCount = tasks.count { it.isCompleted }
                val completedFrogs = tasks.count { it.isCompleted && it.isFrog }
                if (completedFrogs >= 10) {
                    unlockAchievement(AchievementId.FROGS_10)
                }
                if (completedCount >= 10) {
                    unlockAchievement(AchievementId.TASKS_10)
                }
                if (completedCount >= 100) {
                    unlockAchievement(AchievementId.TASKS_100)
                }

                val totalDeepWork = timeBlocks
                    .filter { it.blockType == BlockType.DEEP_WORK }
                    .sumOf { 60 } // Default estimation

                val frog = tasks.firstOrNull { it.isFrog } ?: tasks.firstOrNull { it.energyLevel == EnergyLevel.HIGH }
                val mediums = tasks.filter { !it.isFrog && it.energyLevel == EnergyLevel.MEDIUM }.take(3)
                val quicks = tasks.filter { !it.isFrog && it.energyLevel == EnergyLevel.LOW }.take(5)

                val bFast = meals.firstOrNull { it.slot == MealSlot.BREAKFAST }
                val lUnch = meals.firstOrNull { it.slot == MealSlot.LUNCH }
                val dInner = meals.firstOrNull { it.slot == MealSlot.DINNER }
                val sNack = meals.firstOrNull { it.slot == MealSlot.SNACK }

                val isSpanish = _uiState.value.currentLanguage == AppLanguage.SPANISH

                val synthesizedPlan = AetherDailyPlan(
                    date = AetherDateUtils.getTodayIso(),
                    biometric_baseline = bio,
                    top_3_priorities_1_3_5 = Top3Priorities(
                        frog_task = if (bio.systemMode == SystemMode.RECOVERY) null else frog,
                        medium_tasks = mediums,
                        quick_wins = quicks
                    ),
                    time_blocks = timeBlocks,
                    suggested_tasks_by_energy_menu = SuggestedEnergyMenu(
                        high_energy_backlog = tasks.filter { it.energyLevel == EnergyLevel.HIGH && !it.isFrog },
                        medium_energy_backlog = tasks.filter { it.energyLevel == EnergyLevel.MEDIUM },
                        low_energy_backlog = tasks.filter { it.energyLevel == EnergyLevel.LOW }
                    ),
                    daily_meals = DailyMealsPlan(
                        breakfast = bFast,
                        lunch = lUnch,
                        dinner = dInner,
                        snack = sNack
                    ),
                    deep_work_minutes_allocated = totalDeepWork,
                    max_cognitive_ceiling_minutes = 210,
                    active_mode_label = bio.systemMode.title,
                    cognitive_reframing_message = if (bio.systemMode == SystemMode.RECOVERY) {
                        if (isSpanish) "Modo Recuperación activo: Tareas Tipo A eliminadas. Concéntrate en la línea base de descanso."
                        else "Recovery mode active: Type A tasks stripped. Focus on restorative baseline."
                    } else {
                        if (isSpanish) "Línea base bioenergética establecida. 1 Tarea Frog fijada."
                        else "Bioenergetic baseline established. 1 Frog task locked in."
                    }
                )

                _uiState.value.copy(
                    tasks = tasks,
                    timeBlocks = timeBlocks,
                    pantryItems = pantry,
                    meals = meals,
                    habits = habits,
                    biometric = bio,
                    recentBiometrics = recents,
                    dailyPlan = synthesizedPlan
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    private data class Tuple5<A, B, C, D, E>(
        val a: A, val b: B, val c: C, val d: D, val e: E
    )

    private data class Tuple6<A, B, C, D, E, F>(
        val a: A, val b: B, val c: C, val d: D, val e: E, val f: F
    )

    fun selectTab(tabIndex: Int) {
        _uiState.value = _uiState.value.copy(activeTab = tabIndex)
    }

    // --- Module 2: History Navigation & State ---
    fun openHistory() {
        _uiState.value = _uiState.value.copy(showHistoryDialog = true)
    }

    fun closeHistory() {
        _uiState.value = _uiState.value.copy(showHistoryDialog = false)
    }

    fun setHistoryViewMode(mode: HistoryViewMode) {
        _uiState.value = _uiState.value.copy(historyViewMode = mode)
    }

    fun selectHistoryYear(year: Int) {
        _uiState.value = _uiState.value.copy(
            selectedHistoryYear = year,
            historyViewMode = HistoryViewMode.YEAR
        )
    }

    fun selectHistoryMonth(year: Int, month: Int) {
        val dateIso = String.format(Locale.US, "%04d-%02d-01", year, month)
        _uiState.value = _uiState.value.copy(
            selectedHistoryYear = year,
            selectedHistoryMonth = month,
            selectedHistoryDateIso = dateIso,
            historyViewMode = HistoryViewMode.MONTH
        )
        loadLogsForSelectedDate(dateIso)
    }

    fun selectHistoryDate(dateIso: String) {
        _uiState.value = _uiState.value.copy(
            selectedHistoryDateIso = dateIso,
            historyViewMode = HistoryViewMode.DAY
        )
        loadLogsForSelectedDate(dateIso)
    }

    private fun loadLogsForSelectedDate(dateIso: String) {
        historyLogsJob?.cancel()
        historyLogsJob = viewModelScope.launch {
            repository.getLogsByDate(dateIso).collect { logs ->
                _uiState.value = _uiState.value.copy(historyLogsForSelectedDay = logs)
            }
        }
    }

    // --- Full Backup & Restore ---
    fun toggleWipeHistoryWithCleanSlate() {
        _uiState.value = _uiState.value.copy(
            wipeHistoryWithCleanSlate = !_uiState.value.wipeHistoryWithCleanSlate
        )
    }

    fun exportFullBackup() {
        viewModelScope.launch {
            val result = repository.exportFullBackupToDocuments(getApplication())
            val isSpanish = _uiState.value.currentLanguage == AppLanguage.SPANISH
            if (result.isSuccess) {
                showFeedback(
                    if (isSpanish) "✅ Copia exportada a Documents/${result.getOrNull()?.substringAfterLast("/")}"
                    else "✅ Full backup exported to Documents/${result.getOrNull()?.substringAfterLast("/")}"
                )
            } else {
                showFeedback(
                    if (isSpanish) "❌ Error al exportar copia de seguridad."
                    else "❌ Failed to export full backup."
                )
            }
        }
    }

    fun openRestoreBackupDialog() {
        _uiState.value = _uiState.value.copy(showRestoreBackupDialog = true)
    }

    fun closeRestoreBackupDialog() {
        _uiState.value = _uiState.value.copy(showRestoreBackupDialog = false)
    }

    fun restoreFullBackupFromJson(jsonString: String) {
        viewModelScope.launch {
            val isSpanish = _uiState.value.currentLanguage == AppLanguage.SPANISH
            val result = repository.restoreFromBackupJson(jsonString)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(showRestoreBackupDialog = false)
                showFeedback(
                    if (isSpanish) "✅ ¡Copia de seguridad restaurada correctamente!"
                    else "✅ Full backup restored successfully!"
                )
            } else {
                showFeedback(
                    if (isSpanish) "❌ Error al restaurar copia: Formato JSON no válido."
                    else "❌ Failed to restore: Invalid JSON payload."
                )
            }
        }
    }

    // --- Localization & Settings ---
    fun setLanguage(language: AppLanguage) {
        _uiState.value = _uiState.value.copy(currentLanguage = language)
        viewModelScope.launch {
            repository.saveLanguage(language)
        }
        val isSpanish = language == AppLanguage.SPANISH
        showFeedback(if (isSpanish) "Idioma guardado: Español Castellano 🇪🇸" else "Language saved: English 🇬🇧")
    }

    fun openSettings() {
        _uiState.value = _uiState.value.copy(showSettingsDialog = true)
    }

    fun closeSettings() {
        _uiState.value = _uiState.value.copy(showSettingsDialog = false)
    }

    fun setShowAchievementsDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showAchievementsDialog = show)
    }

    fun openTutorial(stepIndex: Int = 0) {
        _uiState.value = _uiState.value.copy(
            showTutorialDialog = true,
            tutorialStepIndex = stepIndex
        )
    }

    fun closeTutorial() {
        _uiState.value = _uiState.value.copy(showTutorialDialog = false)
    }

    fun setTutorialStep(stepIndex: Int) {
        _uiState.value = _uiState.value.copy(tutorialStepIndex = stepIndex)
    }

    fun resetDemoData() {
        viewModelScope.launch {
            val lang = _uiState.value.currentLanguage
            repository.resetDataToLanguage(lang)
            closeSettings()
            showFeedback(
                if (lang == AppLanguage.SPANISH) "Datos de demostración reiniciados en Español."
                else "Demo data reset in English."
            )
        }
    }

    // --- Biometric & Mode Management ---
    fun updateReadiness(score: Int) {
        viewModelScope.launch {
            repository.updateReadiness(score)
            unlockAchievement(AchievementId.CIRCADIAN_SYNC)
            val isSpanish = _uiState.value.currentLanguage == AppLanguage.SPANISH
            showFeedback(if (isSpanish) "Preparación biológica calibrada a $score/100" else "Readiness calibrated to $score/100")
        }
    }

    fun updateChronotype(chronotype: Chronotype) {
        viewModelScope.launch {
            repository.updateChronotype(chronotype, _uiState.value.biometric.readinessScore)
            unlockAchievement(AchievementId.CIRCADIAN_SYNC)
            val isSpanish = _uiState.value.currentLanguage == AppLanguage.SPANISH
            showFeedback(if (isSpanish) "Curva circadiana ajustada a ${chronotype.title}" else "Circadian curve tuned to ${chronotype.title}")
        }
    }

    fun toggleRecoveryMode() {
        val currentRecovery = _uiState.value.biometric.systemMode == SystemMode.RECOVERY
        viewModelScope.launch {
            repository.setRecoveryMode(!currentRecovery, _uiState.value.biometric.readinessScore)
            val isSpanish = _uiState.value.currentLanguage == AppLanguage.SPANISH
            if (!currentRecovery) {
                showFeedback(if (isSpanish) "⚠️ Modo Recuperación Activado: Tareas de alta demanda suspendidas." else "⚠️ Recovery Mode Engaged: High-demand tasks suspended.")
            } else {
                showFeedback(if (isSpanish) "⚡ Restaurado al flujo circadiano equilibrado." else "⚡ Restored to standard circadian flow.")
            }
        }
    }

    fun triggerOrchestration() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isOrchestrating = true)
            val isSpanish = _uiState.value.currentLanguage == AppLanguage.SPANISH
            try {
                val state = _uiState.value
                val engineResult = repository.orchestrateDailyPlan(
                    readiness = state.biometric.readinessScore,
                    chronotype = state.biometric.chronotype,
                    currentTasks = state.tasks,
                    currentPantry = state.pantryItems
                )
                _uiState.value = _uiState.value.copy(
                    dailyPlan = engineResult.plan,
                    aiEngineStatus = engineResult.status,
                    isOrchestrating = false
                )
                unlockAchievement(AchievementId.AI_ORCHESTRATION)
                val statusText = when (engineResult.status) {
                    AiStatus.LIVE -> if (isSpanish) "✨ ¡Plan circadiano sintetizado en vivo con Gemini AI!" else "✨ Circadian plan synthesized live with Gemini AI!"
                    AiStatus.FALLBACK -> if (isSpanish) "⚡ Plan circadiano generado con Motor de Respaldo Determinista." else "⚡ Circadian plan generated with Deterministic Engine."
                    AiStatus.ERROR -> if (isSpanish) "⚠️ Error en servicio IA: Usando motor circadiano de respaldo." else "⚠️ AI service error: Deterministic fallback engaged."
                    AiStatus.IDLE -> ""
                }
                showFeedback(statusText)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isOrchestrating = false,
                    aiEngineStatus = AiStatus.FALLBACK
                )
                showFeedback(if (isSpanish) "Orquestación actualizada con reglas biológicas." else "Orchestration refreshed with bio-rules.")
            }
        }
    }

    fun requestCognitiveReframe(statement: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isReframing = true)
            val reframe = repository.getCognitiveReframe(statement, _uiState.value.biometric.readinessScore)
            _uiState.value = _uiState.value.copy(
                reframeResponse = reframe,
                isReframing = false,
                showReframeDialog = true
            )
            unlockAchievement(AchievementId.COGNITIVE_REFRAME)
        }
    }

    fun quickAddTask(
        title: String,
        description: String,
        energyLevel: EnergyLevel,
        priorityType: PriorityType,
        estimatedMinutes: Int,
        category: String,
        makeFrog: Boolean = false
    ) {
        viewModelScope.launch {
            repository.addTask(
                title = title,
                description = description,
                energyLevel = energyLevel,
                priorityType = priorityType,
                estimatedMinutes = estimatedMinutes,
                category = category,
                makeFrog = makeFrog
            )
            val isSpanish = _uiState.value.currentLanguage == AppLanguage.SPANISH
            _uiState.value = _uiState.value.copy(showQuickAddDialog = false)
            showFeedback(
                if (isSpanish) "Tarea capturada en el menú de energía ${energyLevel.name}"
                else "Task captured to ${energyLevel.name} energy menu"
            )
        }
    }

    fun updateTask(task: TaskItem) {
        viewModelScope.launch {
            repository.updateTask(task)
            val isSpanish = _uiState.value.currentLanguage == AppLanguage.SPANISH
            _uiState.value = _uiState.value.copy(editingTask = null)
            showFeedback(if (isSpanish) "Tarea '${task.title}' actualizada." else "Task '${task.title}' updated.")
        }
    }

    fun setEditingTask(task: TaskItem?) {
        _uiState.value = _uiState.value.copy(editingTask = task)
    }

    fun toggleTask(task: TaskItem) {
        val willBeCompleted = !task.isCompleted
        viewModelScope.launch {
            repository.toggleTaskComplete(task)

            if (willBeCompleted) {
                val xpToAdd = if (task.isFrog) 30 else 10
                val result = achievementRepository.addXp(xpToAdd)
                if (result.didLevelUp) {
                    triggerLevelUpToast(result.newLevel)
                }

                // Trigger FROG celebration and achievement
                if (task.isFrog) {
                    unlockAchievement(AchievementId.FIRST_FROG)
                    triggerFrogCelebration(task.title)
                }
            }
        }
    }

    private fun triggerFrogCelebration(title: String) {
        celebrationJob?.cancel()
        _uiState.value = _uiState.value.copy(
            showFrogCelebration = true,
            celebratingFrogTaskTitle = title
        )
        celebrationJob = viewModelScope.launch {
            delay(3000)
            _uiState.value = _uiState.value.copy(showFrogCelebration = false)
        }
    }

    fun dismissFrogCelebration() {
        celebrationJob?.cancel()
        _uiState.value = _uiState.value.copy(showFrogCelebration = false)
    }

    fun promoteToFrog(taskId: String) {
        viewModelScope.launch {
            repository.setTaskAsFrog(taskId)
            val isSpanish = _uiState.value.currentLanguage == AppLanguage.SPANISH
            showFeedback(if (isSpanish) "🔥 ¡Fijada como la 1 Tarea FROG de hoy (Tipo A)!" else "🔥 Designated as today's 1 FROG Task (Type A)!")
        }
    }

    fun deleteTask(taskId: String) {
        val taskToDelete = _uiState.value.tasks.firstOrNull { it.id == taskId }
        if (taskToDelete != null) {
            deleteTaskWithUndo(taskToDelete)
        } else {
            viewModelScope.launch {
                repository.deleteTask(taskId)
            }
        }
    }

    fun deleteTaskWithUndo(task: TaskItem) {
        viewModelScope.launch {
            repository.deleteTask(task.id)
            val isSpanish = _uiState.value.currentLanguage == AppLanguage.SPANISH
            _uiState.value = _uiState.value.copy(
                lastDeletedTask = task,
                lastDeletedTimeBlock = null,
                lastDeletedPantryItem = null,
                lastDeletedMeal = null,
                lastDeletedHabit = null,
                undoMessage = if (isSpanish) "Tarea '${task.title}' eliminada" else "Task '${task.title}' deleted"
            )
        }
    }

    fun reorderTasks(orderedTasks: List<TaskItem>) {
        viewModelScope.launch {
            repository.reorderTasks(orderedTasks)
        }
    }

    fun moveTask(fromIndex: Int, toIndex: Int) {
        val list = _uiState.value.tasks.toMutableList()
        if (fromIndex in list.indices && toIndex in list.indices) {
            val item = list.removeAt(fromIndex)
            list.add(toIndex, item)
            reorderTasks(list)
        }
    }

    fun moveMediumTask(fromIndex: Int, toIndex: Int) {
        val mediums = _uiState.value.mediumTasks.toMutableList()
        if (fromIndex in mediums.indices && toIndex in mediums.indices) {
            val item = mediums.removeAt(fromIndex)
            mediums.add(toIndex, item)
            val frogs = _uiState.value.tasks.filter { it.isFrog }
            val quicks = _uiState.value.quickTasks
            val other = _uiState.value.tasks.filter { !it.isFrog && it.energyLevel != EnergyLevel.MEDIUM && it.energyLevel != EnergyLevel.LOW }
            reorderTasks(frogs + mediums + quicks + other)
        }
    }

    fun moveQuickTask(fromIndex: Int, toIndex: Int) {
        val quicks = _uiState.value.quickTasks.toMutableList()
        if (fromIndex in quicks.indices && toIndex in quicks.indices) {
            val item = quicks.removeAt(fromIndex)
            quicks.add(toIndex, item)
            val frogs = _uiState.value.tasks.filter { it.isFrog }
            val mediums = _uiState.value.mediumTasks
            val other = _uiState.value.tasks.filter { !it.isFrog && it.energyLevel != EnergyLevel.MEDIUM && it.energyLevel != EnergyLevel.LOW }
            reorderTasks(frogs + mediums + quicks + other)
        }
    }

    // --- TimeBlock Actions ---
    fun addTimeBlock(
        startTime: String,
        endTime: String,
        blockType: BlockType,
        title: String,
        notes: String = ""
    ) {
        viewModelScope.launch {
            repository.addTimeBlock(startTime, endTime, blockType, title, notes)
            val isSpanish = _uiState.value.currentLanguage == AppLanguage.SPANISH
            _uiState.value = _uiState.value.copy(showAddTimeBlockDialog = false)
            showFeedback(if (isSpanish) "Bloque circadiano agregado al timeline." else "Circadian block added to timeline.")
        }
    }

    fun updateTimeBlock(block: TimeBlock) {
        viewModelScope.launch {
            repository.updateTimeBlock(block)
            val isSpanish = _uiState.value.currentLanguage == AppLanguage.SPANISH
            _uiState.value = _uiState.value.copy(editingTimeBlock = null)
            showFeedback(if (isSpanish) "Bloque '${block.title}' actualizado." else "Time block '${block.title}' updated.")
        }
    }

    fun setEditingTimeBlock(block: TimeBlock?) {
        _uiState.value = _uiState.value.copy(editingTimeBlock = block)
    }

    fun toggleTimeBlock(block: TimeBlock) {
        viewModelScope.launch {
            repository.toggleTimeBlockComplete(block)
        }
    }

    fun deleteTimeBlock(id: String) {
        val blockToDelete = _uiState.value.timeBlocks.firstOrNull { it.id == id }
        if (blockToDelete != null) {
            deleteTimeBlockWithUndo(blockToDelete)
        } else {
            viewModelScope.launch {
                repository.deleteTimeBlock(id)
            }
        }
    }

    fun deleteTimeBlockWithUndo(block: TimeBlock) {
        viewModelScope.launch {
            repository.deleteTimeBlock(block.id)
            val isSpanish = _uiState.value.currentLanguage == AppLanguage.SPANISH
            _uiState.value = _uiState.value.copy(
                lastDeletedTimeBlock = block,
                lastDeletedTask = null,
                lastDeletedPantryItem = null,
                lastDeletedMeal = null,
                lastDeletedHabit = null,
                undoMessage = if (isSpanish) "Bloque '${block.title}' eliminado" else "Time block '${block.title}' deleted"
            )
        }
    }

    fun reorderTimeBlocks(orderedBlocks: List<TimeBlock>) {
        viewModelScope.launch {
            repository.reorderTimeBlocks(orderedBlocks)
        }
    }

    fun moveTimeBlock(fromIndex: Int, toIndex: Int) {
        val list = _uiState.value.timeBlocks.toMutableList()
        if (fromIndex in list.indices && toIndex in list.indices) {
            val item = list.removeAt(fromIndex)
            list.add(toIndex, item)
            reorderTimeBlocks(list)
        }
    }

    fun setShowAddTimeBlock(show: Boolean) {
        _uiState.value = _uiState.value.copy(showAddTimeBlockDialog = show)
    }

    // --- Pantry Actions ---
    fun addPantryItem(
        name: String,
        category: PantryCategory,
        inStock: Boolean,
        isBatchBase: Boolean,
        quantity: String
    ) {
        viewModelScope.launch {
            repository.addPantryItem(name, category, inStock, isBatchBase, quantity)
            val isSpanish = _uiState.value.currentLanguage == AppLanguage.SPANISH
            _uiState.value = _uiState.value.copy(showPantryAddDialog = false)
            showFeedback(if (isSpanish) "Ingrediente $name añadido a la despensa." else "Pantry item $name added.")
        }
    }

    fun updatePantryItem(item: PantryItem) {
        viewModelScope.launch {
            repository.updatePantryItem(item)
            val isSpanish = _uiState.value.currentLanguage == AppLanguage.SPANISH
            _uiState.value = _uiState.value.copy(editingPantryItem = null)
            showFeedback(if (isSpanish) "Ingrediente '${item.name}' actualizado." else "Pantry item '${item.name}' updated.")
        }
    }

    fun setEditingPantryItem(item: PantryItem?) {
        _uiState.value = _uiState.value.copy(editingPantryItem = item)
    }

    fun togglePantryStock(id: String, inStock: Boolean) {
        viewModelScope.launch {
            repository.togglePantryStock(id, inStock)
        }
    }

    fun deletePantryItem(id: String) {
        val itemToDelete = _uiState.value.pantryItems.firstOrNull { it.id == id }
        if (itemToDelete != null) {
            deletePantryItemWithUndo(itemToDelete)
        } else {
            viewModelScope.launch {
                repository.deletePantryItem(id)
            }
        }
    }

    fun deletePantryItemWithUndo(item: PantryItem) {
        viewModelScope.launch {
            repository.deletePantryItem(item.id)
            val isSpanish = _uiState.value.currentLanguage == AppLanguage.SPANISH
            _uiState.value = _uiState.value.copy(
                lastDeletedPantryItem = item,
                lastDeletedTask = null,
                lastDeletedTimeBlock = null,
                lastDeletedMeal = null,
                lastDeletedHabit = null,
                undoMessage = if (isSpanish) "Ingrediente '${item.name}' eliminado" else "Item '${item.name}' deleted"
            )
        }
    }

    // --- Meals Actions ---
    fun addCustomMeal(
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
        caloriesKcal: Int = 0
    ) {
        viewModelScope.launch {
            repository.addMeal(
                slot = slot,
                title = title,
                description = description,
                prepTimeMinutes = prepTimeMinutes,
                ingredients = ingredients,
                usesBatchCookedBase = usesBatchCookedBase,
                allIngredientsInStock = allIngredientsInStock,
                bioImpact = bioImpact,
                customSlotName = customSlotName,
                proteinGrams = proteinGrams,
                carbsGrams = carbsGrams,
                fatGrams = fatGrams,
                caloriesKcal = caloriesKcal
            )
            val isSpanish = _uiState.value.currentLanguage == AppLanguage.SPANISH
            _uiState.value = _uiState.value.copy(showAddMealDialog = false)
            showFeedback(if (isSpanish) "Comida '$title' programada con éxito." else "Meal '$title' added.")
        }
    }

    fun duplicateMeal(meal: MealItem, targetOffsetDays: Int = 1) {
        viewModelScope.launch {
            val targetDate = LocalDate.now().plusDays(targetOffsetDays.toLong()).toString()
            val isSpanish = _uiState.value.currentLanguage == AppLanguage.SPANISH
            val isToday = targetOffsetDays == 0
            repository.duplicateMeal(meal, targetDateIso = targetDate, copySuffix = isToday)
            
            val dayDesc = when (targetOffsetDays) {
                0 -> if (isSpanish) "para hoy" else "for today"
                1 -> if (isSpanish) "para mañana" else "for tomorrow"
                else -> if (isSpanish) "para dentro de $targetOffsetDays días" else "for in $targetOffsetDays days"
            }
            showFeedback(if (isSpanish) "Comida '${meal.title}' duplicada $dayDesc." else "Meal '${meal.title}' duplicated $dayDesc.")
        }
    }

    fun updateMeal(meal: MealItem) {
        viewModelScope.launch {
            repository.updateMeal(meal)
            val isSpanish = _uiState.value.currentLanguage == AppLanguage.SPANISH
            _uiState.value = _uiState.value.copy(editingMeal = null)
            showFeedback(if (isSpanish) "Comida '${meal.title}' actualizada." else "Meal '${meal.title}' updated.")
        }
    }

    fun setEditingMeal(meal: MealItem?) {
        _uiState.value = _uiState.value.copy(editingMeal = meal)
    }

    fun toggleMeal(meal: MealItem) {
        viewModelScope.launch {
            repository.toggleMealComplete(meal)
        }
    }

    fun deleteMeal(id: String) {
        val mealToDelete = _uiState.value.meals.firstOrNull { it.id == id }
        if (mealToDelete != null) {
            deleteMealWithUndo(mealToDelete)
        } else {
            viewModelScope.launch {
                repository.deleteMeal(id)
            }
        }
    }

    fun deleteMealWithUndo(meal: MealItem) {
        viewModelScope.launch {
            repository.deleteMeal(meal.id)
            val isSpanish = _uiState.value.currentLanguage == AppLanguage.SPANISH
            _uiState.value = _uiState.value.copy(
                lastDeletedMeal = meal,
                lastDeletedTask = null,
                lastDeletedTimeBlock = null,
                lastDeletedPantryItem = null,
                lastDeletedHabit = null,
                undoMessage = if (isSpanish) "Comida '${meal.title}' eliminada" else "Meal '${meal.title}' deleted"
            )
        }
    }

    fun setShowAddMeal(show: Boolean) {
        _uiState.value = _uiState.value.copy(showAddMealDialog = show)
    }

    // --- Habit Actions ---
    fun addHabit(
        title: String,
        description: String,
        anchor: CircadianAnchor,
        streakDays: Int = 0,
        reframingTip: String = ""
    ) {
        viewModelScope.launch {
            repository.addHabit(title, description, anchor, streakDays, reframingTip)
            val isSpanish = _uiState.value.currentLanguage == AppLanguage.SPANISH
            _uiState.value = _uiState.value.copy(showAddHabitDialog = false)
            showFeedback(if (isSpanish) "Hábito '$title' añadido con éxito." else "Habit '$title' added successfully.")
        }
    }

    fun updateHabit(habit: HabitAnchor) {
        viewModelScope.launch {
            repository.updateHabit(habit)
            val isSpanish = _uiState.value.currentLanguage == AppLanguage.SPANISH
            _uiState.value = _uiState.value.copy(editingHabit = null)
            showFeedback(if (isSpanish) "Hábito '${habit.title}' actualizado." else "Habit '${habit.title}' updated.")
        }
    }

    fun setEditingHabit(habit: HabitAnchor?) {
        _uiState.value = _uiState.value.copy(editingHabit = habit)
    }

    fun setShowAddHabit(show: Boolean) {
        _uiState.value = _uiState.value.copy(showAddHabitDialog = show)
    }

    fun deleteHabit(id: String) {
        val habitToDelete = _uiState.value.habits.firstOrNull { it.id == id }
        if (habitToDelete != null) {
            deleteHabitWithUndo(habitToDelete)
        } else {
            viewModelScope.launch {
                repository.deleteHabit(id)
            }
        }
    }

    fun deleteHabitWithUndo(habit: HabitAnchor) {
        viewModelScope.launch {
            repository.deleteHabit(habit.id)
            val isSpanish = _uiState.value.currentLanguage == AppLanguage.SPANISH
            _uiState.value = _uiState.value.copy(
                lastDeletedHabit = habit,
                lastDeletedTask = null,
                lastDeletedTimeBlock = null,
                lastDeletedPantryItem = null,
                lastDeletedMeal = null,
                undoMessage = if (isSpanish) "Hábito '${habit.title}' eliminado" else "Habit '${habit.title}' deleted"
            )
        }
    }

    fun toggleHabit(habit: HabitAnchor) {
        val willBeCompleted = !habit.isCompleted
        viewModelScope.launch {
            repository.toggleHabitComplete(habit)

            if (willBeCompleted) {
                // Trigger confetti animation key
                _uiState.value = _uiState.value.copy(habitConfettiKey = System.currentTimeMillis())

                unlockAchievement(AchievementId.FIRST_HABIT)

                // 4.4 XP: +10 XP for completing habit
                val xpResult = achievementRepository.addXp(10)
                if (xpResult.didLevelUp) {
                    triggerLevelUpToast(xpResult.newLevel)
                }

                // Check if all habits are now completed -> Perfect Day Bonus (+50 XP)
                val currentHabits = _uiState.value.habits
                val otherHabitsCompleted = currentHabits.filter { it.id != habit.id }.all { it.isCompleted }
                if (currentHabits.isNotEmpty() && otherHabitsCompleted) {
                    unlockAchievement(AchievementId.PERFECT_DAY)
                    val bonusResult = achievementRepository.addXp(50)
                    val isSpanish = _uiState.value.currentLanguage == AppLanguage.SPANISH
                    showFeedback(
                        if (isSpanish) "🌟 ¡DÍA PERFECTO! +50 XP Bonus por completar todos tus hábitos."
                        else "🌟 PERFECT DAY! +50 XP Bonus for completing all your habits."
                    )
                    if (bonusResult.didLevelUp) {
                        triggerLevelUpToast(bonusResult.newLevel)
                    }
                }
            }
        }
    }

    fun applyGraceDay(habit: HabitAnchor) {
        viewModelScope.launch {
            repository.applyGraceDay(habit)
            unlockAchievement(AchievementId.GRACE_DAY_ACTIVATED)
            val isSpanish = _uiState.value.currentLanguage == AppLanguage.SPANISH
            showFeedback(
                if (isSpanish) "🛡️ Día de Gracia activado para ${habit.title}. ¡Racha protegida!"
                else "🛡️ Grace Day activated for ${habit.title}. Streak protected!"
            )
        }
    }

    // --- Undo Restore Action ---
    fun restoreLastDeletedItem() {
        val state = _uiState.value
        val isSpanish = state.currentLanguage == AppLanguage.SPANISH
        viewModelScope.launch {
            when {
                state.lastDeletedTask != null -> {
                    repository.restoreTask(state.lastDeletedTask)
                    _uiState.value = _uiState.value.copy(lastDeletedTask = null, undoMessage = null)
                    showFeedback(if (isSpanish) "Tarea restaurada" else "Task restored")
                }
                state.lastDeletedTimeBlock != null -> {
                    repository.restoreTimeBlock(state.lastDeletedTimeBlock)
                    _uiState.value = _uiState.value.copy(lastDeletedTimeBlock = null, undoMessage = null)
                    showFeedback(if (isSpanish) "Bloque restaurado" else "Time block restored")
                }
                state.lastDeletedPantryItem != null -> {
                    repository.restorePantryItem(state.lastDeletedPantryItem)
                    _uiState.value = _uiState.value.copy(lastDeletedPantryItem = null, undoMessage = null)
                    showFeedback(if (isSpanish) "Ingrediente restaurado" else "Pantry item restored")
                }
                state.lastDeletedMeal != null -> {
                    repository.restoreMeal(state.lastDeletedMeal)
                    _uiState.value = _uiState.value.copy(lastDeletedMeal = null, undoMessage = null)
                    showFeedback(if (isSpanish) "Comida restaurada" else "Meal restored")
                }
                state.lastDeletedHabit != null -> {
                    repository.restoreHabit(state.lastDeletedHabit)
                    _uiState.value = _uiState.value.copy(lastDeletedHabit = null, undoMessage = null)
                    showFeedback(if (isSpanish) "Hábito restaurado" else "Habit restored")
                }
            }
        }
    }

    fun dismissUndo() {
        _uiState.value = _uiState.value.copy(
            lastDeletedTask = null,
            lastDeletedTimeBlock = null,
            lastDeletedPantryItem = null,
            lastDeletedMeal = null,
            lastDeletedHabit = null,
            undoMessage = null
        )
    }

    // --- Clean Slate & Demo Data Handlers ---
    fun resetToCleanSlate() {
        viewModelScope.launch {
            val lang = _uiState.value.currentLanguage
            val wipeHistory = _uiState.value.wipeHistoryWithCleanSlate
            repository.resetToCleanSlate(lang, wipeHistory = wipeHistory)
            closeSettings()
            showFeedback(
                if (lang == AppLanguage.SPANISH) {
                    if (wipeHistory) "🌿 Base de datos e historial vaciados por completo."
                    else "🌿 Base de datos vaciada. Historial persistente conservado."
                } else {
                    if (wipeHistory) "🌿 Database and persistent history completely wiped."
                    else "🌿 Clean slate initialized. Persistent history preserved."
                }
            )
        }
    }

    fun loadDemoData() {
        viewModelScope.launch {
            val lang = _uiState.value.currentLanguage
            repository.populateDemoData(lang)
            closeSettings()
            showFeedback(
                if (lang == AppLanguage.SPANISH) "🚀 Datos de demostración bioenergéticos cargados."
                else "🚀 Bioenergetic demo data populated."
            )
        }
    }

    fun setEnergyFilter(filter: EnergyLevel?) {
        _uiState.value = _uiState.value.copy(filterEnergyLevel = filter)
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun setShowQuickAdd(show: Boolean) {
        _uiState.value = _uiState.value.copy(showQuickAddDialog = show)
    }

    fun setShowReframe(show: Boolean) {
        _uiState.value = _uiState.value.copy(showReframeDialog = show)
    }

    fun setShowJsonInspector(show: Boolean) {
        _uiState.value = _uiState.value.copy(showJsonInspector = show)
    }

    fun setShowPantryAdd(show: Boolean) {
        _uiState.value = _uiState.value.copy(showPantryAddDialog = show)
    }

    fun getExportJson(): String {
        val plan = _uiState.value.dailyPlan ?: return "{}"
        return repository.exportPlanAsJson(plan)
    }

    // Focus Pomodoro Timer with WorkManager Background Alarm
    fun startFocusTimer(task: TaskItem? = null) {
        val durationMinutes = task?.estimatedMinutes ?: 25
        val durationSeconds = durationMinutes * 60
        val isSpanish = _uiState.value.currentLanguage == AppLanguage.SPANISH

        _uiState.value = _uiState.value.copy(
            isFocusTimerRunning = true,
            activeFocusTask = task,
            focusSecondsRemaining = durationSeconds
        )

        FocusTimerWorker.scheduleFocusTimer(
            context = getApplication(),
            durationSeconds = durationSeconds.toLong(),
            taskTitle = task?.title ?: (if (isSpanish) "Enfoque Profundo" else "Deep Focus Block"),
            isFrog = task?.isFrog ?: false,
            isSpanish = isSpanish
        )

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.focusSecondsRemaining > 0 && _uiState.value.isFocusTimerRunning) {
                delay(1000)
                _uiState.value = _uiState.value.copy(
                    focusSecondsRemaining = _uiState.value.focusSecondsRemaining - 1
                )
            }
            if (_uiState.value.focusSecondsRemaining <= 0 && _uiState.value.isFocusTimerRunning) {
                _uiState.value = _uiState.value.copy(isFocusTimerRunning = false)
                unlockAchievement(AchievementId.FOCUS_BLOCK_DONE)
                showFeedback(
                    if (isSpanish) "🎯 ¡Bloque de enfoque completado! Tómate una pausa de recuperación cognitiva."
                    else "🎯 Focus Block Complete! Step away for cognitive recovery."
                )
            }
        }
    }

    fun stopFocusTimer() {
        timerJob?.cancel()
        FocusTimerWorker.cancelFocusTimer(getApplication())
        _uiState.value = _uiState.value.copy(isFocusTimerRunning = false)
    }

    fun resetFocusTimer(minutes: Int = 25) {
        timerJob?.cancel()
        FocusTimerWorker.cancelFocusTimer(getApplication())
        _uiState.value = _uiState.value.copy(
            isFocusTimerRunning = false,
            focusSecondsRemaining = minutes * 60
        )
    }

    private fun triggerLevelUpToast(level: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(levelUpCelebrationLevel = level)
            delay(3500)
            if (_uiState.value.levelUpCelebrationLevel == level) {
                _uiState.value = _uiState.value.copy(levelUpCelebrationLevel = null)
            }
        }
    }

    private fun unlockAchievement(id: AchievementId) {
        viewModelScope.launch {
            val newlyUnlocked = achievementRepository.unlockAchievement(id)
            if (newlyUnlocked) {
                val item = AchievementItem(id = id, isUnlocked = true)
                _uiState.value = _uiState.value.copy(
                    newlyUnlockedAchievement = item,
                    newlyUnlockedAchievementModal = item
                )
                delay(3500)
                if (_uiState.value.newlyUnlockedAchievement?.id == id) {
                    _uiState.value = _uiState.value.copy(newlyUnlockedAchievement = null)
                }
            }
        }
    }

    fun dismissAchievementModal() {
        _uiState.value = _uiState.value.copy(newlyUnlockedAchievementModal = null)
    }

    fun dismissLevelUpToast() {
        _uiState.value = _uiState.value.copy(levelUpCelebrationLevel = null)
    }

    private fun showFeedback(msg: String) {
        _uiState.value = _uiState.value.copy(statusMessage = msg)
    }

    fun clearStatusMessage() {
        _uiState.value = _uiState.value.copy(statusMessage = null)
    }

    // --- Phase 3 Module 5: Núcleo IA Chat, Quick Actions & Saved Favorites ---

    fun setAiTab(tab: Int) {
        _uiState.value = _uiState.value.copy(selectedAiTab = tab)
    }

    fun sendChatMessage(prompt: String) {
        val cleanPrompt = prompt.trim()
        if (cleanPrompt.isBlank() || _uiState.value.isAiStreaming) return

        viewModelScope.launch {
            val userMsg = AiMessage(
                role = "user",
                content = cleanPrompt,
                timestamp = System.currentTimeMillis()
            )
            repository.saveAiMessage(userMsg)

            val modelMsgId = "msg-" + java.util.UUID.randomUUID().toString().take(8)
            _uiState.value = _uiState.value.copy(
                isAiStreaming = true,
                isAiThinking = true,
                activeStreamingMessageId = modelMsgId,
                activeStreamingContent = ""
            )

            val currentState = _uiState.value
            val context = com.example.data.remote.AetherAiContext(
                dateIso = AetherDateUtils.getTodayIso(),
                language = currentState.currentLanguage,
                readinessScore = currentState.biometric.readinessScore,
                perceivedEnergy = currentState.biometric.perceivedEnergy,
                sleepHours = currentState.biometric.sleepHours,
                sleepQuality = currentState.biometric.sleepQuality,
                chronotype = currentState.biometric.chronotype,
                isRecoveryMode = currentState.biometric.recoveryModeTriggered,
                isGraceDayActive = currentState.biometric.graceDayActive,
                pendingTasks = currentState.tasks.filter { !it.isCompleted },
                habits = currentState.habits,
                timeBlocks = currentState.timeBlocks,
                inStockPantry = currentState.pantryItems.filter { it.inStock },
                meals = currentState.meals,
                deepWorkMinutesAllocated = currentState.deepWorkMinutesAllocated,
                maxCognitiveCeilingMinutes = currentState.maxCognitiveCeilingMinutes,
                recentSummaries = currentState.historySummaries
            )

            var accumulatedContent = ""
            try {
                repository.streamChatResponse(cleanPrompt, context).collect { partialText ->
                    accumulatedContent = partialText
                    _uiState.value = _uiState.value.copy(
                        isAiThinking = false,
                        activeStreamingContent = partialText
                    )
                }
            } catch (e: Exception) {
                val isSpanish = currentState.currentLanguage == AppLanguage.SPANISH
                accumulatedContent = if (isSpanish) "Error al conectar con el asistente IA. Por favor intenta de nuevo." else "Error connecting to AI assistant. Please try again."
            } finally {
                val finalMsg = AiMessage(
                    id = modelMsgId,
                    role = "model",
                    content = accumulatedContent.ifBlank { "..." },
                    timestamp = System.currentTimeMillis(),
                    isStreaming = false
                )
                repository.saveAiMessage(finalMsg)
                _uiState.value = _uiState.value.copy(
                    isAiStreaming = false,
                    isAiThinking = false,
                    activeStreamingMessageId = null,
                    activeStreamingContent = ""
                )
            }
        }
    }

    fun sendQuickAction(action: AiQuickAction) {
        val isSpanish = _uiState.value.currentLanguage == AppLanguage.SPANISH
        val prompt = when (action) {
            AiQuickAction.PLAN_DAY -> if (isSpanish) "Planifica mi día con base en mi readiness y tareas" else "Plan my day based on readiness and tasks"
            AiQuickAction.LOW_ENERGY -> if (isSpanish) "Tengo poca energía hoy, ¿qué debo hacer?" else "I have low energy today, what should I do?"
            AiQuickAction.WEEKLY_REVIEW -> if (isSpanish) "Haz una revisión semanal de mis hábitos y tareas" else "Do a weekly review of my habits and tasks"
            AiQuickAction.THIRTY_MIN_TASK -> if (isSpanish) "¿Qué puedo hacer ahora con 30 minutos disponibles?" else "What can I do now with 30 minutes available?"
        }
        sendChatMessage(prompt)
    }

    fun toggleAiMessageFavorite(message: AiMessage) {
        viewModelScope.launch {
            val newFav = !message.isFavorite
            repository.toggleAiMessageFavorite(message.id, newFav)
            val isSpanish = _uiState.value.currentLanguage == AppLanguage.SPANISH
            showFeedback(
                if (newFav) {
                    if (isSpanish) "⭐ Respuesta guardada en Notas Favoritas" else "⭐ Response saved to Favorite Notes"
                } else {
                    if (isSpanish) "Nota eliminada de favoritas" else "Note removed from favorites"
                }
            )
        }
    }

    fun deleteAiMessage(id: String) {
        viewModelScope.launch {
            repository.deleteAiMessage(id)
        }
    }

    fun clearAiChatHistory() {
        viewModelScope.launch {
            repository.clearAllAiMessages()
            val isSpanish = _uiState.value.currentLanguage == AppLanguage.SPANISH
            showFeedback(if (isSpanish) "Historial de conversación limpiado" else "Chat history cleared")
        }
    }
}

