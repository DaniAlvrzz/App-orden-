package com.example.ui.viewmodel

import com.example.data.model.AchievementId
import com.example.data.model.CircadianAnchor
import com.example.data.model.HabitAnchor
import com.example.data.repository.HabitRepository
import com.example.ui.i18n.AppLanguage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/**
 * Modular Delegate / Sub-ViewModel handling Habit anchors, Grace Days,
 * Streak management, and Cognitive Reframing.
 */
class HabitsDelegate(
    private val habitRepository: HabitRepository,
    private val uiState: MutableStateFlow<AetherUiState>,
    private val scope: CoroutineScope,
    private val showFeedback: (String) -> Unit,
    private val unlockAchievement: (AchievementId) -> Unit
) {
    fun openAddHabitDialog() {
        uiState.value = uiState.value.copy(showAddHabitDialog = true)
    }

    fun closeAddHabitDialog() {
        uiState.value = uiState.value.copy(showAddHabitDialog = false)
    }

    fun openHabitEditor(habit: HabitAnchor) {
        uiState.value = uiState.value.copy(editingHabit = habit)
    }

    fun closeHabitEditor() {
        uiState.value = uiState.value.copy(editingHabit = null)
    }

    fun openReframeDialog() {
        uiState.value = uiState.value.copy(showReframeDialog = true)
    }

    fun closeReframeDialog() {
        uiState.value = uiState.value.copy(
            showReframeDialog = false,
            reframeResponse = null,
            isReframing = false
        )
    }

    fun addHabit(
        title: String,
        description: String,
        anchor: CircadianAnchor,
        streakDays: Int = 0,
        reframingTip: String = ""
    ) {
        scope.launch {
            habitRepository.addHabit(
                title = title,
                description = description,
                anchor = anchor,
                streakDays = streakDays,
                reframingTip = reframingTip
            )
            val isSpanish = uiState.value.currentLanguage == AppLanguage.SPANISH
            showFeedback(if (isSpanish) "Hábito ancla creado." else "Habit anchor created.")
            closeAddHabitDialog()
        }
    }

    fun updateHabit(habit: HabitAnchor) {
        scope.launch {
            habitRepository.updateHabit(habit)
            closeHabitEditor()
            val isSpanish = uiState.value.currentLanguage == AppLanguage.SPANISH
            showFeedback(if (isSpanish) "Hábito actualizado." else "Habit updated.")
        }
    }

    fun toggleHabitComplete(habit: HabitAnchor) {
        scope.launch {
            val willBeCompleted = !habit.isCompleted
            habitRepository.toggleHabitComplete(habit)

            val isSpanish = uiState.value.currentLanguage == AppLanguage.SPANISH
            if (willBeCompleted) {
                uiState.value = uiState.value.copy(habitConfettiKey = System.currentTimeMillis())
                unlockAchievement(AchievementId.FIRST_HABIT)
                showFeedback(if (isSpanish) "🌱 Hábito completado: ${habit.title}" else "🌱 Habit completed: ${habit.title}")
            }
        }
    }

    fun applyGraceDay(habit: HabitAnchor) {
        scope.launch {
            val isSpanish = uiState.value.currentLanguage == AppLanguage.SPANISH
            val result = habitRepository.applyGraceDay(habit)
            if (result.isSuccess) {
                showFeedback(
                    if (isSpanish) "🛡️ Grace Day activado para ${habit.title}. Tu racha biológica queda protegida."
                    else "🛡️ Grace Day activated for ${habit.title}. Streak protected."
                )
            } else {
                val err = result.exceptionOrNull()?.message ?: ""
                when {
                    err.contains("GRACE_LIMIT_REACHED") -> {
                        showFeedback(
                            if (isSpanish) "⚠️ Has alcanzado el límite de Grace Days para este ciclo."
                            else "⚠️ Grace Day limit reached for this cycle."
                        )
                    }
                    err.contains("GRACE_ALREADY_USED_TODAY") -> {
                        showFeedback(
                            if (isSpanish) "⚠️ Ya utilizaste un Grace Day hoy en este hábito."
                            else "⚠️ Grace Day already applied today for this habit."
                        )
                    }
                    err.contains("HABIT_ALREADY_COMPLETED") -> {
                        showFeedback(
                            if (isSpanish) "ℹ️ El hábito ya está completado hoy."
                            else "ℹ️ Habit is already completed today."
                        )
                    }
                    else -> {
                        showFeedback(
                            if (isSpanish) "No se pudo activar el Grace Day."
                            else "Could not activate Grace Day."
                        )
                    }
                }
            }
        }
    }

    fun deleteHabit(habit: HabitAnchor) {
        scope.launch {
            habitRepository.deleteHabit(habit.id)
            uiState.value = uiState.value.copy(
                lastDeletedHabit = habit,
                undoMessage = if (uiState.value.currentLanguage == AppLanguage.SPANISH)
                    "Hábito eliminado: ${habit.title}" else "Habit deleted: ${habit.title}"
            )
        }
    }

    fun undoDeleteHabit() {
        val habit = uiState.value.lastDeletedHabit ?: return
        scope.launch {
            habitRepository.restoreHabit(habit)
            uiState.value = uiState.value.copy(lastDeletedHabit = null, undoMessage = null)
            val isSpanish = uiState.value.currentLanguage == AppLanguage.SPANISH
            showFeedback(if (isSpanish) "Hábito restaurado." else "Habit restored.")
        }
    }

    fun requestCognitiveReframe(userFeeling: String) {
        if (userFeeling.isBlank()) return
        uiState.value = uiState.value.copy(isReframing = true, reframeResponse = null)
        scope.launch {
            val response = habitRepository.getCognitiveReframe(userFeeling, uiState.value.biometric.readinessScore)
            uiState.value = uiState.value.copy(
                isReframing = false,
                reframeResponse = response
            )
        }
    }
}
