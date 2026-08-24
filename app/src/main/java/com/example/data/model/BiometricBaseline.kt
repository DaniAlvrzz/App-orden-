package com.example.data.model

import com.squareup.moshi.JsonClass

enum class Chronotype(val title: String, val peakHours: String, val description: String) {
    LION("Lion (Early Bird)", "07:00 - 11:00", "Early morning focus peak, early crash"),
    BEAR("Bear (Circadian Standard)", "09:30 - 13:30", "Follows solar flow, mid-morning energy zenith"),
    WOLF("Wolf (Night Owl)", "16:00 - 21:00", "Late starter, peak creative burst late afternoon & night"),
    DOLPHIN("Dolphin (Variable/Light)", "14:00 - 18:00", "Sensitive nervous system, best in afternoon micro-bursts")
}

enum class SystemMode(val title: String, val badgeColorHex: Long, val description: String) {
    HIGH_PERFORMANCE("High Performance Mode", 0xFF00E5FF, "Optimal bio-energetics: 1 Frog + Deep focus slots enabled."),
    BALANCED("Balanced Circadian Mode", 0xFF2979FF, "Steady cadence: moderate demand with protective recovery buffers."),
    RECOVERY("Recovery Protocol Mode", 0xFF00E676, "Readiness < 60: Type A tasks suspended, restorative logistics only.")
}

@JsonClass(generateAdapter = true)
data class EnergyCurvePoint(
    val hour: String, // "06:00", "09:00", etc.
    val energyLevel: Int // 0 - 100
)

@JsonClass(generateAdapter = true)
data class BiometricBaseline(
    val readinessScore: Int = 75, // 0 - 100
    val perceivedEnergy: Int = 75, // 0 - 100
    val sleepHours: Double = 7.5,
    val sleepQuality: Int = 4, // 1 - 5
    val chronotype: Chronotype = Chronotype.BEAR,
    val estimatedEnergyCurve: List<EnergyCurvePoint> = emptyList(),
    val recoveryModeTriggered: Boolean = false,
    val graceDayActive: Boolean = false
) {
    val systemMode: SystemMode
        get() = when {
            readinessScore < 60 || recoveryModeTriggered -> SystemMode.RECOVERY
            readinessScore >= 80 -> SystemMode.HIGH_PERFORMANCE
            else -> SystemMode.BALANCED
        }
}
