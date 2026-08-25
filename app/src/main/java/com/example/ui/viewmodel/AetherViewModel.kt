package com.example.ui.viewmodel

import android.app.Application
import android.os.Environment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.AetherApplication
import com.example.data.model.*
import com.example.data.repository.AchievementRepository
import com.example.data.repository.AetherRepository
import com.example.data.util.AetherDateUtils
import com.example.service.FocusTimerWorker
import com.example.ui.i18n.AppLanguage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import java.io.File
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
    val themeMode: AppThemeMode = AppThemeMode.DARK,
    val activeFeedback: FeedbackMessage? = null,
    val statusMessage: String? = null,
    // Active editing items
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
    // Persistent History State
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
    // Dopamine Visuals & Gamification System
    val userLevelInfo: UserLevelInfo = UserLevelInfo(),
    val levelUpCelebrationLevel: Int? = null,
    val habitConfettiKey: Long? = null,
    val newlyUnlockedAchievementModal: AchievementItem? = null,
    // AI Chat & Persistent History
    val aiMessages: List<AiMessage> = emptyList(),
    val favoriteAiMessages: List<AiMessage> = emptyList(),
    val selectedAiTab: Int = 0,
    val isAiStreaming: Boolean = false,
    val isAiThinking: Boolean = false,
    val activeStreamingMessageId: String? = null,
    val activeStreamingContent: String = "",
    // External AI Diet Importer
    val showImportDietDialog: Boolean = false,
    val isImportingDiet: Boolean = false,
    // Archived Tasks
    val archivedTasks: List<TaskItem> = emptyList(),
    // Daily Rollover Notice
    val dailyRolloverNotice: DailyRolloverResult? = null,
    // Section 4-6 Features
    val yesterdayUnfinishedHabits: List<HabitAnchor> = emptyList(),
    val yesterdayUnfinishedTasks: List<TaskItem> = emptyList(),
    val showMorningCheckInDialog: Boolean = false,
    val compassionModeState: CompassionModeState = CompassionModeState(),
    val showBreathworkDialog: Boolean = false,
    val quickNotes: List<QuickNoteItem> = emptyList(),
    val pomodoroPhase: FocusPhase = FocusPhase.WORK,
    val currentPomodoroRound: Int = 1,
    val totalFocusMinutes: Int = 0
) {
    val deepWorkMinutesAllocated: Int
        get() = timeBlocks
            .filter { it.blockType == BlockType.DEEP_WORK }
            .sumOf { block -> calculateMinutesBetween(block.startTime, block.endTime) }

    val maxCognitiveCeilingMinutes: Int = 210

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

/**
 * Coordinator ViewModel for Aether OS.
 * Orchestrates sub-domain delegates (`tasksDelegate`, `habitsDelegate`, `nutritionDelegate`,
 * `biometricDelegate`, `aiChatDelegate`), reactive state combine, and history navigation.
 */
class AetherViewModel(
    application: Application,
    private val repository: AetherRepository,
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

    private val feedbackMessageQueue = ArrayDeque<FeedbackMessage>()
    private val feedbackMutex = Mutex()
    private var historyLogsJob: Job? = null

    // Domain Delegates
    val tasksDelegate: TasksDelegate = TasksDelegate(
        taskRepository = (application as? AetherApplication)?.container?.taskRepository
            ?: error("Container missing"),
        uiState = _uiState,
        scope = viewModelScope,
        showFeedback = { msg -> showFeedback(msg) },
        unlockAchievement = { id -> unlockAchievement(id) }
    )

    val habitsDelegate: HabitsDelegate = HabitsDelegate(
        habitRepository = (application as? AetherApplication)?.container?.habitRepository
            ?: error("Container missing"),
        uiState = _uiState,
        scope = viewModelScope,
        showFeedback = { msg -> showFeedback(msg) },
        unlockAchievement = { id -> unlockAchievement(id) }
    )

    val nutritionDelegate: NutritionDelegate = NutritionDelegate(
        mealRepository = (application as? AetherApplication)?.container?.mealRepository
            ?: error("Container missing"),
        mealImportUseCase = (application as? AetherApplication)?.container?.mealImportUseCase
            ?: error("Container missing"),
        uiState = _uiState,
        scope = viewModelScope,
        showFeedback = { msg -> showFeedback(msg) },
        unlockAchievement = { id -> unlockAchievement(id) }
    )

    val biometricDelegate: BiometricDelegate = BiometricDelegate(
        biometricRepository = (application as? AetherApplication)?.container?.biometricRepository
            ?: error("Container missing"),
        uiState = _uiState,
        scope = viewModelScope,
        showFeedback = { msg -> showFeedback(msg) },
        unlockAchievement = { id -> unlockAchievement(id) }
    )

    val aiChatDelegate: AiChatDelegate = AiChatDelegate(
        aiRepository = (application as? AetherApplication)?.container?.aiRepository
            ?: error("Container missing"),
        uiState = _uiState,
        scope = viewModelScope,
        showFeedback = { msg -> showFeedback(msg) },
        unlockAchievement = { id -> unlockAchievement(id) },
        contextProvider = { getApplication() }
    )

    init {
        performDailyRolloverCheck()
        checkMorningRetroactiveCheckIn()
        observeData()
    }

    private fun performDailyRolloverCheck() {
        viewModelScope.launch {
            val rolloverResult = repository.checkAndPerformDailyRollover()
            if (rolloverResult != null) {
                _uiState.value = _uiState.value.copy(dailyRolloverNotice = rolloverResult)
                val isSpanish = _uiState.value.currentLanguage == AppLanguage.SPANISH
                showFeedback(
                    if (isSpanish) "🌅 ¡Nuevo día iniciado! Tareas y hábitos reiniciados para hoy."
                    else "🌅 New day started! Tasks & habits reset for today."
                )
            }
        }
    }

    private fun checkMorningRetroactiveCheckIn() {
        val taskRepo = (getApplication() as? AetherApplication)?.container?.taskRepository ?: return
        viewModelScope.launch {
            val (habits, tasks) = taskRepo.getYesterdayUnfinishedItems()
            if (habits.isNotEmpty() || tasks.isNotEmpty()) {
                _uiState.value = _uiState.value.copy(
                    yesterdayUnfinishedHabits = habits,
                    yesterdayUnfinishedTasks = tasks,
                    showMorningCheckInDialog = true
                )
            }
        }
    }

    fun dismissDailyRolloverNotice() {
        _uiState.value = _uiState.value.copy(dailyRolloverNotice = null)
    }

    private fun observeData() {
        val taskRepo = (getApplication() as? AetherApplication)?.container?.taskRepository
        if (taskRepo != null) {
            viewModelScope.launch {
                taskRepo.quickNotes.collect { notes ->
                    _uiState.value = _uiState.value.copy(quickNotes = notes)
                }
            }
            viewModelScope.launch {
                taskRepo.totalFocusMinutes.collect { mins ->
                    _uiState.value = _uiState.value.copy(totalFocusMinutes = mins)
                }
            }
        }

        viewModelScope.launch {
            repository.getLanguage().distinctUntilChanged().collect { savedLang ->
                _uiState.value = _uiState.value.copy(currentLanguage = savedLang)
            }
        }

        viewModelScope.launch {
            achievementRepository.achievements.collect { list ->
                _uiState.value = _uiState.value.copy(achievements = list)
            }
        }

        var previousLevel: Int? = null
        viewModelScope.launch {
            achievementRepository.userLevelInfo.collect { info ->
                val prev = previousLevel
                previousLevel = info.currentLevel
                if (prev != null && info.currentLevel > prev) {
                    _uiState.value = _uiState.value.copy(
                        userLevelInfo = info,
                        levelUpCelebrationLevel = info.currentLevel
                    )
                } else {
                    _uiState.value = _uiState.value.copy(userLevelInfo = info)
                }
            }
        }

        viewModelScope.launch {
            repository.dailySummaries.collect { summaries ->
                _uiState.value = _uiState.value.copy(historySummaries = summaries)
            }
        }

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

        viewModelScope.launch {
            repository.archivedTasks.collect { archived ->
                _uiState.value = _uiState.value.copy(archivedTasks = archived)
            }
        }

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
                if (habits.any { it.streakDays >= 7 }) unlockAchievement(AchievementId.STREAK_7_DAYS)
                if (habits.any { it.streakDays >= 30 }) unlockAchievement(AchievementId.STREAK_30_DAYS)
                if (habits.isNotEmpty() && habits.all { it.isCompleted }) unlockAchievement(AchievementId.PERFECT_DAY)
                if (pantry.count { it.inStock } >= 5) unlockAchievement(AchievementId.PANTRY_5_ITEMS)
                val completedCount = tasks.count { it.isCompleted }
                val completedFrogs = tasks.count { it.isCompleted && it.isFrog }
                if (completedFrogs >= 10) unlockAchievement(AchievementId.FROGS_10)
                if (completedCount >= 10) unlockAchievement(AchievementId.TASKS_10)
                if (completedCount >= 100) unlockAchievement(AchievementId.TASKS_100)

                val frog = tasks.firstOrNull { it.isFrog } ?: tasks.firstOrNull { it.energyLevel == EnergyLevel.HIGH }
                val mediums = tasks.filter { !it.isFrog && it.energyLevel == EnergyLevel.MEDIUM }
                val quicks = tasks.filter { !it.isFrog && it.energyLevel == EnergyLevel.LOW }

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
                        medium_tasks = mediums.take(3),
                        quick_wins = quicks.take(5)
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
                    deep_work_minutes_allocated = timeBlocks.filter { it.blockType == BlockType.DEEP_WORK }.sumOf { 60 },
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

    private data class Tuple5<A, B, C, D, E>(val a: A, val b: B, val c: C, val d: D, val e: E)
    private data class Tuple6<A, B, C, D, E, F>(val a: A, val b: B, val c: C, val d: D, val e: E, val f: F)

    fun selectTab(tabIndex: Int) {
        _uiState.value = _uiState.value.copy(activeTab = tabIndex)
    }

    // --- History Navigation ---
    fun openHistory() {
        val today = LocalDate.now()
        val todayIso = today.toString()
        _uiState.value = _uiState.value.copy(
            showHistoryDialog = true,
            selectedHistoryYear = today.year,
            selectedHistoryMonth = today.monthValue,
            selectedHistoryDateIso = todayIso,
            historyViewMode = HistoryViewMode.MONTH
        )
        loadLogsForSelectedDate(todayIso)
    }

    fun closeHistory() { _uiState.value = _uiState.value.copy(showHistoryDialog = false) }
    fun setHistoryViewMode(mode: HistoryViewMode) { _uiState.value = _uiState.value.copy(historyViewMode = mode) }

    fun selectHistoryYear(year: Int) {
        _uiState.value = _uiState.value.copy(selectedHistoryYear = year, historyViewMode = HistoryViewMode.YEAR)
    }

    fun selectHistoryMonth(year: Int, month: Int) {
        val today = LocalDate.now()
        val dateIso = if (year == today.year && month == today.monthValue) {
            today.toString()
        } else {
            String.format(Locale.US, "%04d-%02d-01", year, month)
        }
        _uiState.value = _uiState.value.copy(
            selectedHistoryYear = year,
            selectedHistoryMonth = month,
            selectedHistoryDateIso = dateIso,
            historyViewMode = HistoryViewMode.MONTH
        )
        loadLogsForSelectedDate(dateIso)
    }

    fun selectHistoryDate(dateIso: String) {
        _uiState.value = _uiState.value.copy(selectedHistoryDateIso = dateIso, historyViewMode = HistoryViewMode.DAY)
        loadLogsForSelectedDate(dateIso)
    }

    // --- Quick Notes & Retroactive Logging ---
    fun addQuickNote(content: String) = tasksDelegate.addQuickNote(content)
    fun deleteQuickNote(id: String) = tasksDelegate.deleteQuickNote(id)
    fun convertQuickNoteToTask(note: QuickNoteItem) = tasksDelegate.convertQuickNoteToTask(note)
    fun confirmRetroactiveHabit(habit: HabitAnchor) = tasksDelegate.confirmRetroactiveHabit(habit)
    fun confirmRetroactiveTask(task: TaskItem) = tasksDelegate.confirmRetroactiveTask(task)
    fun dismissMorningCheckIn() = tasksDelegate.dismissMorningCheckIn()

    // --- Compassion Mode & Breathwork ---
    fun activateCompassionMode() {
        _uiState.value = _uiState.value.copy(compassionModeState = CompassionModeState(isActive = true))
    }
    fun dismissCompassionMode() {
        _uiState.value = _uiState.value.copy(compassionModeState = CompassionModeState(isActive = false))
    }
    fun setShowBreathwork(show: Boolean) {
        _uiState.value = _uiState.value.copy(showBreathworkDialog = show)
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
        _uiState.value = _uiState.value.copy(wipeHistoryWithCleanSlate = !_uiState.value.wipeHistoryWithCleanSlate)
    }

    fun exportFullBackup() {
        viewModelScope.launch {
            val docsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            val file = File(docsDir, "AetherOS_Backup_${System.currentTimeMillis()}.json")
            val result = repository.exportFullBackupToFile(file)
            val isSpanish = _uiState.value.currentLanguage == AppLanguage.SPANISH
            if (result.isSuccess) {
                showFeedback(
                    if (isSpanish) "✅ Copia exportada a Documents/${file.name}"
                    else "✅ Full backup exported to Documents/${file.name}"
                )
            } else {
                showFeedback(if (isSpanish) "❌ Error al exportar copia." else "❌ Failed to export backup.")
            }
        }
    }

    fun openRestoreBackupDialog() { _uiState.value = _uiState.value.copy(showRestoreBackupDialog = true) }
    fun closeRestoreBackupDialog() { _uiState.value = _uiState.value.copy(showRestoreBackupDialog = false) }

    fun restoreFullBackupFromJson(jsonString: String) {
        viewModelScope.launch {
            val result = repository.restoreFromBackupJson(jsonString)
            val isSpanish = _uiState.value.currentLanguage == AppLanguage.SPANISH
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(showRestoreBackupDialog = false)
                showFeedback(if (isSpanish) "✅ ¡Copia de seguridad restaurada!" else "✅ Backup restored!")
            } else {
                showFeedback(if (isSpanish) "❌ Error al restaurar: JSON inválido." else "❌ Failed to restore: Invalid JSON.")
            }
        }
    }

    fun resetToCleanSlate() {
        viewModelScope.launch {
            val lang = _uiState.value.currentLanguage
            val wipeHistory = _uiState.value.wipeHistoryWithCleanSlate
            repository.resetToCleanSlate(lang, wipeHistory = wipeHistory)
            if (wipeHistory) {
                achievementRepository.resetAchievements()
            }
            closeSettings()
            showFeedback(
                if (lang == AppLanguage.SPANISH) "🌿 Base de datos vaciada." else "🌿 Clean slate initialized."
            )
        }
    }

    fun loadDemoData() {
        viewModelScope.launch {
            val lang = _uiState.value.currentLanguage
            repository.populateDemoData(lang)
            closeSettings()
            showFeedback(
                if (lang == AppLanguage.SPANISH) "🚀 Datos de demostración cargados." else "🚀 Demo data loaded."
            )
        }
    }

    fun resetDemoData() {
        viewModelScope.launch {
            val lang = _uiState.value.currentLanguage
            repository.resetDataToLanguage(lang)
            closeSettings()
            showFeedback(
                if (lang == AppLanguage.SPANISH) "Datos reiniciados en Español." else "Demo data reset."
            )
        }
    }

    // --- Settings & UI Controls ---
    fun setLanguage(language: AppLanguage) {
        _uiState.value = _uiState.value.copy(currentLanguage = language)
        viewModelScope.launch { repository.saveLanguage(language) }
        val isSpanish = language == AppLanguage.SPANISH
        showFeedback(if (isSpanish) "Idioma: Español 🇪🇸" else "Language: English 🇬🇧")
    }

    fun setThemeMode(mode: AppThemeMode) {
        _uiState.value = _uiState.value.copy(themeMode = mode)
        val isSpanish = _uiState.value.currentLanguage == AppLanguage.SPANISH
        val modeName = if (isSpanish) mode.titleEs else mode.titleEn
        showFeedback(if (isSpanish) "Tema: $modeName ${mode.icon}" else "Theme: $modeName ${mode.icon}")
    }

    fun openSettings() { _uiState.value = _uiState.value.copy(showSettingsDialog = true) }
    fun closeSettings() { _uiState.value = _uiState.value.copy(showSettingsDialog = false) }
    fun setShowAchievementsDialog(show: Boolean) { _uiState.value = _uiState.value.copy(showAchievementsDialog = show) }
    fun openTutorial(stepIndex: Int = 0) { _uiState.value = _uiState.value.copy(showTutorialDialog = true, tutorialStepIndex = stepIndex) }
    fun closeTutorial() { _uiState.value = _uiState.value.copy(showTutorialDialog = false) }
    fun setTutorialStep(stepIndex: Int) { _uiState.value = _uiState.value.copy(tutorialStepIndex = stepIndex) }

    // --- Feedback Queue ---
    fun showFeedback(msg: String, isError: Boolean = false) {
        if (msg.isBlank()) return
        val item = FeedbackMessage(message = msg, isError = isError)
        viewModelScope.launch {
            feedbackMutex.lock()
            try {
                if (isError) feedbackMessageQueue.addFirst(item) else feedbackMessageQueue.addLast(item)
                processNextFeedbackLocked()
            } finally {
                feedbackMutex.unlock()
            }
        }
    }

    private fun processNextFeedbackLocked() {
        if (_uiState.value.activeFeedback == null && feedbackMessageQueue.isNotEmpty()) {
            val next = feedbackMessageQueue.removeFirst()
            _uiState.value = _uiState.value.copy(activeFeedback = next, statusMessage = next.message)
        }
    }

    fun dismissActiveFeedback(id: String? = null) {
        viewModelScope.launch {
            feedbackMutex.lock()
            try {
                val current = _uiState.value.activeFeedback
                if (id == null || current?.id == id) {
                    _uiState.value = _uiState.value.copy(activeFeedback = null, statusMessage = null)
                    processNextFeedbackLocked()
                }
            } finally {
                feedbackMutex.unlock()
            }
        }
    }

    fun clearStatusMessage() = dismissActiveFeedback()

    // --- Gamification ---
    fun unlockAchievement(id: AchievementId) {
        viewModelScope.launch {
            val newlyUnlocked = achievementRepository.unlockAchievement(id)
            if (newlyUnlocked) {
                val item = AchievementItem(id = id, isUnlocked = true)
                _uiState.value = _uiState.value.copy(
                    newlyUnlockedAchievement = item,
                    newlyUnlockedAchievementModal = item
                )
                val isSpanish = _uiState.value.currentLanguage == AppLanguage.SPANISH
                showFeedback(
                    if (isSpanish) "🏆 ¡Logro desbloqueado: ${id.titleEs} (+${id.xpReward} XP)!"
                    else "🏆 Achievement Unlocked: ${id.titleEn} (+${id.xpReward} XP)!"
                )
                viewModelScope.launch {
                    delay(4000)
                    _uiState.value = _uiState.value.copy(newlyUnlockedAchievement = null)
                }
            }
        }
    }

    fun dismissAchievementModal() { _uiState.value = _uiState.value.copy(newlyUnlockedAchievementModal = null) }
    fun dismissAchievementBanner() { _uiState.value = _uiState.value.copy(newlyUnlockedAchievement = null) }
    fun dismissLevelUpToast() { _uiState.value = _uiState.value.copy(levelUpCelebrationLevel = null) }

    // --- Forwarding Actions to Delegates ---
    fun setSearchQuery(query: String) = tasksDelegate.setSearchQuery(query)
    fun setEnergyFilter(filter: EnergyLevel?) = tasksDelegate.setFilterEnergy(filter)
    fun setShowQuickAdd(show: Boolean) { if (show) tasksDelegate.openQuickAdd() else tasksDelegate.closeQuickAdd() }
    fun quickAddTask(
        title: String,
        description: String,
        energyLevel: EnergyLevel,
        priorityType: PriorityType,
        estimatedMinutes: Int,
        category: String,
        makeFrog: Boolean = false
    ) = tasksDelegate.addTask(title, description, energyLevel, priorityType, estimatedMinutes, category, makeFrog)
    fun updateTask(task: TaskItem) = tasksDelegate.updateTask(task)
    fun setEditingTask(task: TaskItem?) { if (task != null) tasksDelegate.openTaskEditor(task) else tasksDelegate.closeTaskEditor() }
    fun toggleTask(task: TaskItem) = tasksDelegate.toggleTaskComplete(task)
    fun promoteToFrog(taskId: String) = tasksDelegate.setTaskAsFrog(taskId)
    fun deleteTask(taskId: String) {
        val task = _uiState.value.tasks.firstOrNull { it.id == taskId } ?: return
        tasksDelegate.deleteTask(task)
    }
    fun deleteTaskWithUndo(task: TaskItem) = tasksDelegate.deleteTask(task)
    fun reorderTasks(orderedTasks: List<TaskItem>) = tasksDelegate.reorderTasks(orderedTasks)
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
    fun dismissFrogCelebration() = tasksDelegate.dismissFrogCelebration()

    // TimeBlock Forwarders
    fun setShowAddTimeBlock(show: Boolean) { if (show) tasksDelegate.openAddTimeBlockDialog() else tasksDelegate.closeAddTimeBlockDialog() }
    fun addTimeBlock(startTime: String, endTime: String, blockType: BlockType, title: String, notes: String = "") =
        tasksDelegate.addTimeBlock(startTime, endTime, blockType, title, notes)
    fun updateTimeBlock(block: TimeBlock) = tasksDelegate.updateTimeBlock(block)
    fun setEditingTimeBlock(block: TimeBlock?) { if (block != null) tasksDelegate.openTimeBlockEditor(block) else tasksDelegate.closeTimeBlockEditor() }
    fun toggleTimeBlock(block: TimeBlock) = tasksDelegate.toggleTimeBlockComplete(block)
    fun deleteTimeBlock(id: String) {
        val block = _uiState.value.timeBlocks.firstOrNull { it.id == id } ?: return
        tasksDelegate.deleteTimeBlock(block)
    }
    fun deleteTimeBlockWithUndo(block: TimeBlock) = tasksDelegate.deleteTimeBlock(block)
    fun reorderTimeBlocks(orderedBlocks: List<TimeBlock>) = tasksDelegate.reorderTimeBlocks(orderedBlocks)
    fun moveTimeBlock(fromIndex: Int, toIndex: Int) {
        val list = _uiState.value.timeBlocks.toMutableList()
        if (fromIndex in list.indices && toIndex in list.indices) {
            val item = list.removeAt(fromIndex)
            list.add(toIndex, item)
            reorderTimeBlocks(list)
        }
    }

    // Focus Timer
    fun startFocusTimer(task: TaskItem? = null) {
        val currentTask = task ?: _uiState.value.frogTask ?: _uiState.value.tasks.firstOrNull { !it.isCompleted }
        val durationMinutes = currentTask?.estimatedMinutes ?: 25
        val durationSeconds = durationMinutes * 60
        val isSpanish = _uiState.value.currentLanguage == AppLanguage.SPANISH

        FocusTimerWorker.scheduleFocusTimer(
            context = getApplication(),
            durationSeconds = durationSeconds.toLong(),
            taskTitle = currentTask?.title ?: (if (isSpanish) "Enfoque Profundo" else "Deep Focus Block"),
            isFrog = currentTask?.isFrog ?: false,
            isSpanish = isSpanish
        )
        tasksDelegate.startFocusTimer(currentTask)
    }

    fun pauseFocusTimer() {
        FocusTimerWorker.cancelFocusTimer(getApplication())
        tasksDelegate.pauseFocusTimer()
    }

    fun resetFocusTimer() {
        FocusTimerWorker.cancelFocusTimer(getApplication())
        tasksDelegate.resetFocusTimer(_uiState.value.activeFocusTask?.estimatedMinutes ?: 25)
    }

    // Habit Forwarders
    fun setShowAddHabit(show: Boolean) { if (show) habitsDelegate.openAddHabitDialog() else habitsDelegate.closeAddHabitDialog() }
    fun addHabit(title: String, description: String, anchor: CircadianAnchor, streakDays: Int = 0, reframingTip: String = "") =
        habitsDelegate.addHabit(title, description, anchor, streakDays, reframingTip)
    fun updateHabit(habit: HabitAnchor) = habitsDelegate.updateHabit(habit)
    fun setEditingHabit(habit: HabitAnchor?) { if (habit != null) habitsDelegate.openHabitEditor(habit) else habitsDelegate.closeHabitEditor() }
    fun toggleHabit(habit: HabitAnchor) = habitsDelegate.toggleHabitComplete(habit)
    fun applyGraceDay(habit: HabitAnchor) = habitsDelegate.applyGraceDay(habit)
    fun deleteHabit(id: String) {
        val habit = _uiState.value.habits.firstOrNull { it.id == id } ?: return
        habitsDelegate.deleteHabit(habit)
    }
    fun deleteHabitWithUndo(habit: HabitAnchor) = habitsDelegate.deleteHabit(habit)
    fun setShowReframe(show: Boolean) { if (show) habitsDelegate.openReframeDialog() else habitsDelegate.closeReframeDialog() }
    fun requestCognitiveReframe(statement: String) = habitsDelegate.requestCognitiveReframe(statement)

    // Nutrition Forwarders
    fun setShowAddMeal(show: Boolean) { if (show) nutritionDelegate.openAddMealDialog() else nutritionDelegate.closeAddMealDialog() }
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
    ) = nutritionDelegate.addMeal(
        slot, title, description, prepTimeMinutes, ingredients,
        usesBatchCookedBase, allIngredientsInStock, bioImpact, customSlotName,
        proteinGrams, carbsGrams, fatGrams, caloriesKcal
    )
    fun duplicateMeal(meal: MealItem, targetOffsetDays: Int = 1) {
        val targetDate = LocalDate.now().plusDays(targetOffsetDays.toLong()).toString()
        nutritionDelegate.duplicateMeal(meal, targetDate, copySuffix = (targetOffsetDays == 0))
    }
    fun updateMeal(meal: MealItem) = nutritionDelegate.updateMeal(meal)
    fun setEditingMeal(meal: MealItem?) { if (meal != null) nutritionDelegate.openMealEditor(meal) else nutritionDelegate.closeMealEditor() }
    fun toggleMeal(meal: MealItem) = nutritionDelegate.toggleMealComplete(meal)
    fun deleteMeal(id: String) {
        val meal = _uiState.value.meals.firstOrNull { it.id == id } ?: return
        nutritionDelegate.deleteMeal(meal)
    }
    fun deleteMealWithUndo(meal: MealItem) = nutritionDelegate.deleteMeal(meal)
    fun setShowImportDiet(show: Boolean) { if (show) nutritionDelegate.openImportDietDialog() else nutritionDelegate.closeImportDietDialog() }
    fun importMealsFromExternalAI(rawTextOrJson: String) = nutritionDelegate.importMealsFromExternalAI(rawTextOrJson)

    // Pantry Forwarders
    fun setShowPantryAdd(show: Boolean) { if (show) nutritionDelegate.openPantryAddDialog() else nutritionDelegate.closePantryAddDialog() }
    fun setShowBatchBaseDialog(show: Boolean) { if (show) nutritionDelegate.openBatchBaseDialog() else nutritionDelegate.closeBatchBaseDialog() }
    fun addPantryItem(name: String, category: PantryCategory, inStock: Boolean, isBatchBase: Boolean, quantity: String) =
        nutritionDelegate.addPantryItem(name, category, inStock, isBatchBase, quantity)
    fun updatePantryItem(item: PantryItem) = nutritionDelegate.updatePantryItem(item)
    fun setEditingPantryItem(item: PantryItem?) { if (item != null) nutritionDelegate.openPantryEditor(item) else nutritionDelegate.closePantryEditor() }
    fun togglePantryStock(id: String, inStock: Boolean) = nutritionDelegate.togglePantryStock(id, inStock)
    fun deletePantryItem(id: String) {
        val item = _uiState.value.pantryItems.firstOrNull { it.id == id } ?: return
        nutritionDelegate.deletePantryItem(item)
    }
    fun deletePantryItemWithUndo(item: PantryItem) = nutritionDelegate.deletePantryItem(item)

    // Biometric Forwarders
    fun updateReadiness(score: Int) = biometricDelegate.updateReadiness(score)
    fun saveBiometricBaseline(biometric: BiometricBaseline) = biometricDelegate.saveBiometricBaseline(biometric)
    fun updateChronotype(chronotype: Chronotype) = biometricDelegate.updateChronotype(chronotype)
    fun toggleRecoveryMode() {
        val currentRecovery = _uiState.value.biometric.systemMode == SystemMode.RECOVERY
        biometricDelegate.toggleRecoveryMode(!currentRecovery)
    }

    // AI Forwarders
    fun setAiTab(tab: Int) = aiChatDelegate.selectAiTab(tab)
    fun setShowJsonInspector(show: Boolean) { if (show) aiChatDelegate.openJsonInspector() else aiChatDelegate.closeJsonInspector() }
    fun sendChatMessage(prompt: String) = aiChatDelegate.sendAiPrompt(prompt)
    fun toggleAiMessageFavorite(message: AiMessage) = aiChatDelegate.toggleFavoriteMessage(message)
    fun deleteAiMessage(id: String) = aiChatDelegate.deleteAiMessage(id)
    fun clearAiChatHistory() = aiChatDelegate.clearChatHistory()
    fun triggerOrchestration() = aiChatDelegate.orchestrateDailyPlan()
    fun getExportJson(): String = aiChatDelegate.exportCurrentPlanJson()

    fun sendQuickAction(action: AiQuickAction) {
        val isSpanish = _uiState.value.currentLanguage == AppLanguage.SPANISH
        val prompt = when (action) {
            AiQuickAction.PLAN_DAY -> if (isSpanish) "Planifica mi día con base en mi readiness y tareas" else "Plan my day based on readiness and tasks"
            AiQuickAction.LOW_ENERGY -> if (isSpanish) "Tengo poca energía hoy, ¿qué debo hacer?" else "I have low energy today, what should I do?"
            AiQuickAction.WEEKLY_REVIEW -> if (isSpanish) "Haz una revisión semanal de mis hábitos y tareas" else "Do a weekly review of my habits and tasks"
            AiQuickAction.THIRTY_MIN_TASK -> if (isSpanish) "¿Qué puedo hacer ahora con 30 minutos disponibles?" else "What can I do now with 30 minutes available?"
            AiQuickAction.BREAK_DOWN_TASK -> if (isSpanish) "Desglosa mi tarea principal (Frog) en micro-pasos de 5 a 15 min para empezar sin fricción" else "Break down my Frog task into 5-15 min micro-steps to start without friction"
            AiQuickAction.NO_MOTIVATION -> if (isSpanish) "No tengo motivación ni ganas de hacer nada. Dame un micro-paso de 5 minutos." else "I have zero motivation today. Give me a 5-minute micro-step."
            AiQuickAction.OVERWHELMED -> if (isSpanish) "Estoy muy saturado y abrumado con tantas cosas. Ayúdame a vaciar la mente y regularme." else "I feel overwhelmed with too many things. Help me regulate and downscale."
            AiQuickAction.MICRO_STEP -> if (isSpanish) "¿Cuál es el mínimo micro-paso que puedo dar ahora mismo para vencer la inercia?" else "What is the smallest possible micro-step to break inertia right now?"
            AiQuickAction.EMOTIONAL_SUPPORT -> if (isSpanish) "Necesito apoyo y desahogo personal para recuperar la calma y el enfoque." else "I need emotional support and a moment of calm grounding."
            AiQuickAction.GENTLE_PLAN -> if (isSpanish) "Genera un plan suave y compasivo para el resto del día." else "Generate a gentle, compassionate plan for the rest of today."
        }
        sendChatMessage(prompt)
    }

    // Undo Global Handler
    fun restoreLastDeletedItem() {
        when {
            _uiState.value.lastDeletedTask != null -> tasksDelegate.undoDeleteTask()
            _uiState.value.lastDeletedTimeBlock != null -> tasksDelegate.undoDeleteTimeBlock()
            _uiState.value.lastDeletedHabit != null -> habitsDelegate.undoDeleteHabit()
            _uiState.value.lastDeletedMeal != null -> nutritionDelegate.undoDeleteMeal()
            _uiState.value.lastDeletedPantryItem != null -> nutritionDelegate.undoDeletePantryItem()
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
}
