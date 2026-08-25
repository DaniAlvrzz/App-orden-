package com.example.data.repository

import com.example.data.local.BiometricDao
import com.example.data.local.BiometricEntity
import com.example.data.mapper.toEntity
import com.example.data.mapper.toModel
import com.example.data.model.BiometricBaseline
import com.example.data.model.Chronotype
import com.example.data.util.AetherDateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

interface BiometricRepository {
    val biometric: Flow<BiometricBaseline>
    val recentBiometrics: Flow<List<BiometricBaseline>>

    suspend fun updateReadiness(score: Int)
    suspend fun saveBiometricBaseline(biometric: BiometricBaseline)
    suspend fun updateChronotype(chronotype: Chronotype, currentReadiness: Int)
    suspend fun setRecoveryMode(enabled: Boolean, currentScore: Int)
}

class BiometricRepositoryImpl(
    private val biometricDao: BiometricDao
) : BiometricRepository {

    override val biometric: Flow<BiometricBaseline> = biometricDao.getLatestBiometric().map { entity ->
        entity?.toModel() ?: BiometricBaseline()
    }

    override val recentBiometrics: Flow<List<BiometricBaseline>> = biometricDao.getRecentBiometrics(14).map { list ->
        list.map { it.toModel() }
    }

    override suspend fun updateReadiness(score: Int) {
        val isRecovery = score < 60
        val today = AetherDateUtils.getTodayIso()
        biometricDao.insertBiometric(
            BiometricEntity(
                date = today,
                readinessScore = score,
                perceivedEnergy = score,
                sleepHours = 7.5,
                sleepQuality = if (score > 70) 4 else 2,
                chronotype = Chronotype.BEAR,
                recoveryModeTriggered = isRecovery,
                graceDayActive = isRecovery
            )
        )
    }

    override suspend fun saveBiometricBaseline(biometric: BiometricBaseline) {
        val today = AetherDateUtils.getTodayIso()
        val entity = biometric.toEntity().copy(date = today)
        biometricDao.insertBiometric(entity)
    }

    override suspend fun updateChronotype(chronotype: Chronotype, currentReadiness: Int) {
        val today = AetherDateUtils.getTodayIso()
        val current = biometricDao.getBiometric(today).first()
        if (current != null) {
            biometricDao.insertBiometric(current.copy(chronotype = chronotype))
        } else {
            biometricDao.insertBiometric(
                BiometricEntity(
                    date = today,
                    readinessScore = currentReadiness,
                    perceivedEnergy = currentReadiness,
                    chronotype = chronotype,
                    recoveryModeTriggered = currentReadiness < 60,
                    graceDayActive = currentReadiness < 60
                )
            )
        }
    }

    override suspend fun setRecoveryMode(enabled: Boolean, currentScore: Int) {
        val today = AetherDateUtils.getTodayIso()
        val current = biometricDao.getBiometric(today).first()
        val targetScore = if (enabled) 45 else maxOf(65, currentScore)
        if (current != null) {
            biometricDao.insertBiometric(
                current.copy(
                    readinessScore = targetScore,
                    recoveryModeTriggered = enabled,
                    graceDayActive = enabled
                )
            )
        } else {
            biometricDao.insertBiometric(
                BiometricEntity(
                    date = today,
                    readinessScore = targetScore,
                    perceivedEnergy = if (enabled) 40 else maxOf(65, currentScore),
                    chronotype = Chronotype.BEAR,
                    recoveryModeTriggered = enabled,
                    graceDayActive = enabled
                )
            )
        }
    }
}
