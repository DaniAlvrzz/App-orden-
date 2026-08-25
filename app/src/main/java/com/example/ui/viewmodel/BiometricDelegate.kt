package com.example.ui.viewmodel

import com.example.data.model.AchievementId
import com.example.data.model.BiometricBaseline
import com.example.data.model.Chronotype
import com.example.data.repository.BiometricRepository
import com.example.ui.i18n.AppLanguage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/**
 * Modular Delegate / Sub-ViewModel handling Biometrics, Readiness updates,
 * Chronotype customization, and Biological Recovery Mode.
 */
class BiometricDelegate(
    private val biometricRepository: BiometricRepository,
    private val uiState: MutableStateFlow<AetherUiState>,
    private val scope: CoroutineScope,
    private val showFeedback: (String) -> Unit,
    private val unlockAchievement: (AchievementId) -> Unit
) {
    fun updateReadiness(score: Int) {
        scope.launch {
            biometricRepository.updateReadiness(score)
            unlockAchievement(AchievementId.FIRST_BIO_SYNC)
            val isSpanish = uiState.value.currentLanguage == AppLanguage.SPANISH
            val msg = if (score < 60) {
                if (isSpanish) "🛡️ Modo Recuperación activado: Carga cognitiva reducida."
                else "🛡️ Recovery Mode activated: Cognitive load reduced."
            } else {
                if (isSpanish) "🔋 Readiness sincronizado: $score/100"
                else "🔋 Readiness synced: $score/100"
            }
            showFeedback(msg)
        }
    }

    fun saveBiometricBaseline(biometric: BiometricBaseline) {
        scope.launch {
            biometricRepository.saveBiometricBaseline(biometric)
            unlockAchievement(AchievementId.FIRST_BIO_SYNC)
            val isSpanish = uiState.value.currentLanguage == AppLanguage.SPANISH
            showFeedback(
                if (isSpanish) "🧬 Biometría y cronotipo sincronizados."
                else "🧬 Biometrics and chronotype synced."
            )
        }
    }

    fun updateChronotype(chronotype: Chronotype) {
        scope.launch {
            biometricRepository.updateChronotype(chronotype, uiState.value.biometric.readinessScore)
            val isSpanish = uiState.value.currentLanguage == AppLanguage.SPANISH
            showFeedback(
                if (isSpanish) "Cronotipo cambiado a ${chronotype.title}."
                else "Chronotype changed to ${chronotype.title}."
            )
        }
    }

    fun toggleRecoveryMode(enabled: Boolean) {
        scope.launch {
            biometricRepository.setRecoveryMode(enabled, uiState.value.biometric.readinessScore)
            val isSpanish = uiState.value.currentLanguage == AppLanguage.SPANISH
            showFeedback(
                if (enabled) {
                    if (isSpanish) "🛡️ Modo Recuperación activado: Tareas de alta demanda pausadas."
                    else "🛡️ Recovery Mode activated: High demand tasks paused."
                } else {
                    if (isSpanish) "⚡ Modo Normal restaurado."
                    else "⚡ Normal Mode restored."
                }
            )
        }
    }
}
