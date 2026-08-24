package com.example.data.model

enum class AiStatus {
    IDLE,
    LIVE,
    FALLBACK,
    ERROR
}

data class AetherEngineResult(
    val plan: AetherDailyPlan,
    val status: AiStatus,
    val errorMessage: String? = null
)
