package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AetherDatabase
import com.example.data.model.*
import com.example.data.repository.AetherRepository
import com.example.ui.i18n.AppLanguage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AetherUiState(
    val tasks: List<TaskItem> = emptyList(),
    val timeBlocks: List<TimeBlock> = emptyList(),
    val pantryItems: List<PantryItem> = emptyList(),
    val meals: List<MealItem> = emptyList(),
    val habits: List<HabitAnchor> = emptyList(),
    val biometric: BiometricBaseline = BiometricBaseline(),
    val dailyPlan: AetherDailyPlan? = null,
    val isOrchestrating: Boolean = false,
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
    val tutorialStepIndex: Int = 0,
    val currentLanguage: AppLanguage = AppLanguage.SPANISH,
    val statusMessage: String? = null,
    // Focus Timer
    val isFocusTimerRunning: Boolean = false,
    val focusSecondsRemaining: Int = 25 * 60,
    val activeFocusTask: TaskItem? = null
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
            val sParts = start.split(":").map { it.toInt() }
            val eParts = end.split(":").map { it.toInt() }
            val sMin = sParts[0] * 60 + sParts[1]
            val eMin = eParts[0] * 60 + eParts[1]
            if (eMin >= sMin) eMin - sMin else 60
        } catch (e: Exception) {
            60
        }
    }
}

class AetherViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AetherRepository

    init {
        val database = AetherDatabase.getDatabase(application)
        repository = AetherRepository(database)
    }

    private val _uiState = MutableStateFlow(AetherUiState())
    val uiState: StateFlow<AetherUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        observeData()
    }

    private fun observeData() {
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
                    date = "2026-08-22",
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
                    dailyPlan = synthesizedPlan
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    private data class Tuple5<A, B, C, D, E>(
        val a: A,
        val b: B,
        val c: C,
        val d: D,
        val e: E
    )

    fun selectTab(tabIndex: Int) {
        _uiState.value = _uiState.value.copy(activeTab = tabIndex)
    }

    // --- Localization & Settings ---
    fun setLanguage(language: AppLanguage) {
        _uiState.value = _uiState.value.copy(currentLanguage = language)
        val isSpanish = language == AppLanguage.SPANISH
        showFeedback(if (isSpanish) "Idioma cambiado a Español Castellano 🇪🇸" else "Language changed to English 🇬🇧")
    }

    fun openSettings() {
        _uiState.value = _uiState.value.copy(showSettingsDialog = true)
    }

    fun closeSettings() {
        _uiState.value = _uiState.value.copy(showSettingsDialog = false)
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
            val isSpanish = _uiState.value.currentLanguage == AppLanguage.SPANISH
            showFeedback(if (isSpanish) "Preparación biológica calibrada a $score/100" else "Readiness calibrated to $score/100")
        }
    }

    fun updateChronotype(chronotype: Chronotype) {
        viewModelScope.launch {
            repository.updateChronotype(chronotype, _uiState.value.biometric.readinessScore)
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
                val newPlan = repository.orchestrateDailyPlan(
                    readiness = state.biometric.readinessScore,
                    chronotype = state.biometric.chronotype,
                    currentTasks = state.tasks,
                    currentPantry = state.pantryItems
                )
                _uiState.value = _uiState.value.copy(
                    dailyPlan = newPlan,
                    isOrchestrating = false
                )
                showFeedback(if (isSpanish) "✨ ¡Aether OS orquestó tu jornada con éxito!" else "✨ Aether OS orchestrated your day successfully!")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isOrchestrating = false)
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

    fun toggleTask(task: TaskItem) {
        viewModelScope.launch {
            repository.toggleTaskComplete(task)
        }
    }

    fun promoteToFrog(taskId: String) {
        viewModelScope.launch {
            repository.setTaskAsFrog(taskId)
            val isSpanish = _uiState.value.currentLanguage == AppLanguage.SPANISH
            showFeedback(if (isSpanish) "🔥 ¡Fijada como la 1 Tarea FROG de hoy (Tipo A)!" else "🔥 Designated as today's 1 FROG Task (Type A)!")
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            repository.deleteTask(taskId)
        }
    }

    fun addTimeBlock(startTime: String, endTime: String, blockType: BlockType, title: String) {
        viewModelScope.launch {
            repository.addTimeBlock(startTime, endTime, blockType, title)
            val isSpanish = _uiState.value.currentLanguage == AppLanguage.SPANISH
            showFeedback(if (isSpanish) "Bloque temporal anclado: $title" else "Time block anchored: $title")
        }
    }

    fun toggleTimeBlock(block: TimeBlock) {
        viewModelScope.launch {
            repository.toggleTimeBlockComplete(block)
        }
    }

    fun deleteTimeBlock(id: String) {
        viewModelScope.launch {
            repository.deleteTimeBlock(id)
        }
    }

    fun togglePantryStock(id: String, inStock: Boolean) {
        viewModelScope.launch {
            repository.togglePantryStock(id, inStock)
        }
    }

    fun addPantryItem(name: String, category: PantryCategory, inStock: Boolean, isBatchBase: Boolean, qty: String) {
        viewModelScope.launch {
            repository.addPantryItem(name, category, inStock, isBatchBase, qty)
            _uiState.value = _uiState.value.copy(showPantryAddDialog = false)
            val isSpanish = _uiState.value.currentLanguage == AppLanguage.SPANISH
            showFeedback(if (isSpanish) "Inventario de despensa actualizado: $name" else "Pantry inventory updated: $name")
        }
    }

    fun deletePantryItem(id: String) {
        viewModelScope.launch {
            repository.deletePantryItem(id)
        }
    }

    fun toggleMeal(meal: MealItem) {
        viewModelScope.launch {
            repository.toggleMealComplete(meal)
        }
    }

    fun toggleHabit(habit: HabitAnchor) {
        viewModelScope.launch {
            repository.toggleHabitComplete(habit)
        }
    }

    fun applyGraceDay(habit: HabitAnchor) {
        viewModelScope.launch {
            repository.applyGraceDay(habit)
            val isSpanish = _uiState.value.currentLanguage == AppLanguage.SPANISH
            showFeedback(
                if (isSpanish) "🛡️ Día de Gracia activado para ${habit.title}. ¡Racha protegida!"
                else "🛡️ Grace Day activated for ${habit.title}. Streak protected!"
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

    // Focus Pomodoro Timer
    fun startFocusTimer(task: TaskItem? = null) {
        _uiState.value = _uiState.value.copy(
            isFocusTimerRunning = true,
            activeFocusTask = task,
            focusSecondsRemaining = (task?.estimatedMinutes ?: 25) * 60
        )
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.focusSecondsRemaining > 0 && _uiState.value.isFocusTimerRunning) {
                delay(1000)
                _uiState.value = _uiState.value.copy(
                    focusSecondsRemaining = _uiState.value.focusSecondsRemaining - 1
                )
            }
            if (_uiState.value.focusSecondsRemaining <= 0) {
                _uiState.value = _uiState.value.copy(isFocusTimerRunning = false)
                val isSpanish = _uiState.value.currentLanguage == AppLanguage.SPANISH
                showFeedback(
                    if (isSpanish) "🎯 ¡Bloque de enfoque completado! Tómate una pausa de recuperación cognitiva."
                    else "🎯 Focus Block Complete! Step away for cognitive recovery."
                )
            }
        }
    }

    fun stopFocusTimer() {
        timerJob?.cancel()
        _uiState.value = _uiState.value.copy(isFocusTimerRunning = false)
    }

    fun resetFocusTimer(minutes: Int = 25) {
        timerJob?.cancel()
        _uiState.value = _uiState.value.copy(
            isFocusTimerRunning = false,
            focusSecondsRemaining = minutes * 60
        )
    }

    private fun showFeedback(msg: String) {
        _uiState.value = _uiState.value.copy(statusMessage = msg)
    }

    fun clearStatusMessage() {
        _uiState.value = _uiState.value.copy(statusMessage = null)
    }
}
