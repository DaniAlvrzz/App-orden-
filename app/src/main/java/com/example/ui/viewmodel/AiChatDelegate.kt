package com.example.ui.viewmodel

import android.content.Context
import com.example.data.model.*
import com.example.data.remote.AetherAiContext
import com.example.data.repository.AiRepository
import com.example.ui.i18n.AppLanguage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Modular Delegate / Sub-ViewModel handling AI Chat persistence,
 * Gemini real-time streaming, daily plan orchestration, and prompt favorites.
 */
class AiChatDelegate(
    private val aiRepository: AiRepository,
    private val uiState: MutableStateFlow<AetherUiState>,
    private val scope: CoroutineScope,
    private val showFeedback: (String) -> Unit,
    private val unlockAchievement: (AchievementId) -> Unit,
    private val contextProvider: () -> Context?
) {
    fun selectAiTab(tabIndex: Int) {
        uiState.value = uiState.value.copy(selectedAiTab = tabIndex)
    }

    fun openJsonInspector() {
        uiState.value = uiState.value.copy(showJsonInspector = true)
    }

    fun closeJsonInspector() {
        uiState.value = uiState.value.copy(showJsonInspector = false)
    }

    fun sendAiPrompt(prompt: String) {
        if (prompt.isBlank()) return

        val userMessage = AiMessage(
            id = "msg-" + UUID.randomUUID().toString().take(8),
            role = "user",
            content = prompt.trim(),
            timestamp = System.currentTimeMillis()
        )

        val assistantMessageId = "msg-" + UUID.randomUUID().toString().take(8)

        scope.launch {
            aiRepository.saveAiMessage(userMessage)
            unlockAchievement(AchievementId.FIRST_AI_ORCHESTRATION)

            uiState.value = uiState.value.copy(
                isAiStreaming = true,
                isAiThinking = true,
                activeStreamingMessageId = assistantMessageId,
                activeStreamingContent = ""
            )

            val currentState = uiState.value
            val aiContext = AetherAiContext(
                readinessScore = currentState.biometric.readinessScore,
                systemMode = currentState.biometric.systemMode.title,
                tasks = currentState.tasks,
                meals = currentState.meals,
                pantry = currentState.pantryItems,
                habits = currentState.habits,
                language = currentState.currentLanguage
            )

            val accumulatedText = StringBuilder()

            aiRepository.streamChatResponse(prompt.trim(), aiContext)
                .catch { error ->
                    uiState.value = uiState.value.copy(
                        isAiStreaming = false,
                        isAiThinking = false,
                        activeStreamingMessageId = null,
                        activeStreamingContent = ""
                    )
                    val errorMsg = AiMessage(
                        id = assistantMessageId,
                        role = "assistant",
                        content = "⚠️ ${error.message ?: "Connection error with Gemini."}",
                        timestamp = System.currentTimeMillis()
                    )
                    aiRepository.saveAiMessage(errorMsg)
                }
                .collect { chunk ->
                    accumulatedText.append(chunk)
                    uiState.value = uiState.value.copy(
                        isAiThinking = false,
                        activeStreamingContent = accumulatedText.toString()
                    )
                }

            val fullResponse = accumulatedText.toString()
            if (fullResponse.isNotBlank()) {
                val assistantMessage = AiMessage(
                    id = assistantMessageId,
                    role = "assistant",
                    content = fullResponse,
                    timestamp = System.currentTimeMillis()
                )
                aiRepository.saveAiMessage(assistantMessage)
            }

            uiState.value = uiState.value.copy(
                isAiStreaming = false,
                isAiThinking = false,
                activeStreamingMessageId = null,
                activeStreamingContent = ""
            )
        }
    }

    fun toggleFavoriteMessage(message: AiMessage) {
        scope.launch {
            val newFav = !message.isFavorite
            aiRepository.toggleAiMessageFavorite(message.id, newFav)
            val isSpanish = uiState.value.currentLanguage == AppLanguage.SPANISH
            showFeedback(
                if (newFav) {
                    if (isSpanish) "⭐ Mensaje guardado en Favoritos." else "⭐ Saved to Favorites."
                } else {
                    if (isSpanish) "Eliminado de Favoritos." else "Removed from Favorites."
                }
            )
        }
    }

    fun deleteAiMessage(id: String) {
        scope.launch {
            aiRepository.deleteAiMessage(id)
        }
    }

    fun clearChatHistory() {
        scope.launch {
            aiRepository.clearAllAiMessages()
            val isSpanish = uiState.value.currentLanguage == AppLanguage.SPANISH
            showFeedback(if (isSpanish) "Historial de conversación limpiado." else "Chat history cleared.")
        }
    }

    fun orchestrateDailyPlan() {
        uiState.value = uiState.value.copy(
            isOrchestrating = true,
            aiEngineStatus = AiStatus.ORCHESTRATING
        )

        scope.launch {
            try {
                val currentState = uiState.value
                val result = aiRepository.orchestrateDailyPlan(
                    readiness = currentState.biometric.readinessScore,
                    chronotype = currentState.biometric.chronotype,
                    currentTasks = currentState.tasks,
                    currentPantry = currentState.pantryItems,
                    context = contextProvider()
                )

                uiState.value = uiState.value.copy(
                    dailyPlan = result.plan,
                    isOrchestrating = false,
                    aiEngineStatus = AiStatus.SUCCESS
                )
                unlockAchievement(AchievementId.FIRST_AI_ORCHESTRATION)

                val isSpanish = currentState.currentLanguage == AppLanguage.SPANISH
                val feedback = if (result.source == EngineResultSource.REMOTE_GEMINI) {
                    if (isSpanish) "✨ Plan Circadiano orquestado con éxito vía Gemini 2.5."
                    else "✨ Circadian Plan orchestrated successfully via Gemini 2.5."
                } else {
                    if (isSpanish) "⚡ Plan Circadiano sintetizado localmente (Modo Autónomo)."
                    else "⚡ Circadian Plan synthesized locally (Autonomous Mode)."
                }
                showFeedback(feedback)
            } catch (e: Exception) {
                uiState.value = uiState.value.copy(
                    isOrchestrating = false,
                    aiEngineStatus = AiStatus.ERROR
                )
                val isSpanish = uiState.value.currentLanguage == AppLanguage.SPANISH
                showFeedback(
                    if (isSpanish) "Error al orquestar plan: ${e.localizedMessage}"
                    else "Error orchestrating plan: ${e.localizedMessage}"
                )
            }
        }
    }

    fun exportCurrentPlanJson(): String {
        val plan = uiState.value.dailyPlan ?: return "{}"
        return aiRepository.exportPlanAsJson(plan)
    }
}
