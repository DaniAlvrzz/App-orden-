package com.example.ui.viewmodel

import com.example.data.model.*
import com.example.data.repository.TaskRepository
import com.example.ui.i18n.AppLanguage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/**
 * Modular Delegate / Sub-ViewModel handling Task management, Priority Matrix 1-3-5,
 * Focus Timer, TimeBlocks, and Task AI Breakdown.
 */
class TasksDelegate(
    private val taskRepository: TaskRepository,
    private val uiState: MutableStateFlow<AetherUiState>,
    private val scope: CoroutineScope,
    private val showFeedback: (String) -> Unit,
    private val unlockAchievement: (AchievementId) -> Unit
) {
    private var timerJob: Job? = null
    private var celebrationJob: Job? = null

    fun setSearchQuery(query: String) {
        uiState.value = uiState.value.copy(searchQuery = query)
    }

    fun setFilterEnergy(level: EnergyLevel?) {
        uiState.value = uiState.value.copy(filterEnergyLevel = level)
    }

    fun openQuickAdd(defaultEnergy: EnergyLevel? = null) {
        uiState.value = uiState.value.copy(
            showQuickAddDialog = true,
            filterEnergyLevel = defaultEnergy
        )
    }

    fun closeQuickAdd() {
        uiState.value = uiState.value.copy(showQuickAddDialog = false)
    }

    fun openTaskEditor(task: TaskItem) {
        uiState.value = uiState.value.copy(editingTask = task)
    }

    fun closeTaskEditor() {
        uiState.value = uiState.value.copy(editingTask = null)
    }

    fun openTimeBlockEditor(block: TimeBlock) {
        uiState.value = uiState.value.copy(editingTimeBlock = block)
    }

    fun closeTimeBlockEditor() {
        uiState.value = uiState.value.copy(editingTimeBlock = null)
    }

    fun openAddTimeBlockDialog() {
        uiState.value = uiState.value.copy(showAddTimeBlockDialog = true)
    }

    fun closeAddTimeBlockDialog() {
        uiState.value = uiState.value.copy(showAddTimeBlockDialog = false)
    }

    fun addTask(
        title: String,
        description: String,
        energyLevel: EnergyLevel,
        priorityType: PriorityType,
        estimatedMinutes: Int,
        category: String,
        makeFrog: Boolean = false
    ) {
        scope.launch {
            taskRepository.addTask(
                title = title,
                description = description,
                energyLevel = energyLevel,
                priorityType = priorityType,
                estimatedMinutes = estimatedMinutes,
                category = category,
                makeFrog = makeFrog
            )
            val isSpanish = uiState.value.currentLanguage == AppLanguage.SPANISH
            val msg = if (makeFrog || priorityType == PriorityType.FROG) {
                if (isSpanish) "🐸 Tarea Frog fijada como Máxima Prioridad." else "🐸 Frog task set as Highest Priority."
            } else {
                if (isSpanish) "Tarea añadida correctamente." else "Task added successfully."
            }
            showFeedback(msg)
            closeQuickAdd()
        }
    }

    fun updateTask(task: TaskItem) {
        scope.launch {
            taskRepository.updateTask(task)
            closeTaskEditor()
            val isSpanish = uiState.value.currentLanguage == AppLanguage.SPANISH
            showFeedback(if (isSpanish) "Tarea actualizada." else "Task updated.")
        }
    }

    fun toggleTaskComplete(task: TaskItem) {
        scope.launch {
            val willBeCompleted = !task.isCompleted
            taskRepository.toggleTaskComplete(task)

            val isSpanish = uiState.value.currentLanguage == AppLanguage.SPANISH
            if (willBeCompleted) {
                if (task.isFrog) {
                    celebrateFrogCompletion(task.title)
                    unlockAchievement(AchievementId.FIRST_FROG)
                    showFeedback(if (isSpanish) "🐸 ¡ENHORABUENA! Te has comido tu sapo del día." else "🐸 CONGRATULATIONS! You ate your frog.")
                } else {
                    showFeedback(if (isSpanish) "✨ Tarea completada: ${task.title}" else "✨ Task completed: ${task.title}")
                }
            }
        }
    }

    fun setTaskAsFrog(taskId: String) {
        scope.launch {
            taskRepository.setTaskAsFrog(taskId)
            val isSpanish = uiState.value.currentLanguage == AppLanguage.SPANISH
            showFeedback(if (isSpanish) "🐸 Asignada como Tarea Frog del día." else "🐸 Set as Frog task of the day.")
        }
    }

    fun reorderTasks(reordered: List<TaskItem>) {
        scope.launch {
            taskRepository.reorderTasks(reordered)
        }
    }

    fun deleteTask(task: TaskItem) {
        scope.launch {
            taskRepository.deleteTask(task.id)
            uiState.value = uiState.value.copy(
                lastDeletedTask = task,
                undoMessage = if (uiState.value.currentLanguage == AppLanguage.SPANISH)
                    "Tarea eliminada: ${task.title}" else "Task deleted: ${task.title}"
            )
        }
    }

    fun undoDeleteTask() {
        val task = uiState.value.lastDeletedTask ?: return
        scope.launch {
            taskRepository.restoreTask(task)
            uiState.value = uiState.value.copy(lastDeletedTask = null, undoMessage = null)
            val isSpanish = uiState.value.currentLanguage == AppLanguage.SPANISH
            showFeedback(if (isSpanish) "Tarea restaurada." else "Task restored.")
        }
    }

    fun breakDownTaskWithAi(taskTitle: String, minutes: Int, onResult: (List<String>) -> Unit) {
        scope.launch {
            val subtasks = taskRepository.breakDownTask(taskTitle, minutes, uiState.value.currentLanguage)
            onResult(subtasks)
        }
    }

    fun addTimeBlock(
        startTime: String,
        endTime: String,
        blockType: BlockType,
        title: String,
        notes: String = ""
    ) {
        scope.launch {
            taskRepository.addTimeBlock(startTime, endTime, blockType, title, notes)
            val isSpanish = uiState.value.currentLanguage == AppLanguage.SPANISH
            showFeedback(if (isSpanish) "Bloque de tiempo añadido." else "Time block added.")
            closeAddTimeBlockDialog()
        }
    }

    fun updateTimeBlock(block: TimeBlock) {
        scope.launch {
            taskRepository.updateTimeBlock(block)
            closeTimeBlockEditor()
            val isSpanish = uiState.value.currentLanguage == AppLanguage.SPANISH
            showFeedback(if (isSpanish) "Bloque de tiempo actualizado." else "Time block updated.")
        }
    }

    fun reorderTimeBlocks(reordered: List<TimeBlock>) {
        scope.launch {
            taskRepository.reorderTimeBlocks(reordered)
        }
    }

    fun toggleTimeBlockComplete(block: TimeBlock) {
        scope.launch {
            taskRepository.toggleTimeBlockComplete(block)
        }
    }

    fun deleteTimeBlock(block: TimeBlock) {
        scope.launch {
            taskRepository.deleteTimeBlock(block.id)
            uiState.value = uiState.value.copy(
                lastDeletedTimeBlock = block,
                undoMessage = if (uiState.value.currentLanguage == AppLanguage.SPANISH)
                    "Bloque eliminado: ${block.title}" else "Block deleted: ${block.title}"
            )
        }
    }

    fun undoDeleteTimeBlock() {
        val block = uiState.value.lastDeletedTimeBlock ?: return
        scope.launch {
            taskRepository.restoreTimeBlock(block)
            uiState.value = uiState.value.copy(lastDeletedTimeBlock = null, undoMessage = null)
            val isSpanish = uiState.value.currentLanguage == AppLanguage.SPANISH
            showFeedback(if (isSpanish) "Bloque restaurado." else "Block restored.")
        }
    }

    // --- Focus Timer Engine ---
    fun startFocusTimer(task: TaskItem? = null) {
        val currentTask = task ?: uiState.value.frogTask ?: uiState.value.tasks.firstOrNull { !it.isCompleted }
        uiState.value = uiState.value.copy(
            isFocusTimerRunning = true,
            activeFocusTask = currentTask
        )
        timerJob?.cancel()
        timerJob = scope.launch {
            while (uiState.value.isFocusTimerRunning && uiState.value.focusSecondsRemaining > 0) {
                delay(1000L)
                val remaining = uiState.value.focusSecondsRemaining - 1
                uiState.value = uiState.value.copy(focusSecondsRemaining = remaining)
                if (remaining <= 0) {
                    uiState.value = uiState.value.copy(isFocusTimerRunning = false)
                    unlockAchievement(AchievementId.FOCUS_25)
                    val isSpanish = uiState.value.currentLanguage == AppLanguage.SPANISH
                    showFeedback(if (isSpanish) "⏳ ¡Sesión de Enfoque Profundo finalizada!" else "⏳ Deep Focus session completed!")
                    break
                }
            }
        }
    }

    fun pauseFocusTimer() {
        uiState.value = uiState.value.copy(isFocusTimerRunning = false)
        timerJob?.cancel()
    }

    fun resetFocusTimer(minutes: Int = 25) {
        timerJob?.cancel()
        uiState.value = uiState.value.copy(
            isFocusTimerRunning = false,
            focusSecondsRemaining = minutes * 60
        )
    }

    private fun celebrateFrogCompletion(title: String) {
        celebrationJob?.cancel()
        celebrationJob = scope.launch {
            uiState.value = uiState.value.copy(
                showFrogCelebration = true,
                celebratingFrogTaskTitle = title
            )
            delay(5000L)
            uiState.value = uiState.value.copy(
                showFrogCelebration = false,
                celebratingFrogTaskTitle = ""
            )
        }
    }

    fun dismissFrogCelebration() {
        celebrationJob?.cancel()
        uiState.value = uiState.value.copy(
            showFrogCelebration = false,
            celebratingFrogTaskTitle = ""
        )
    }
}
