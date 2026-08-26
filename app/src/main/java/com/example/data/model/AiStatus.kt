package com.example.data.model

enum class AiStatus {
    IDLE,
    LIVE,
    FALLBACK,
    ORCHESTRATING,
    SUCCESS,
    ERROR
}

enum class EngineResultSource {
    REMOTE_GEMINI,
    LOCAL_FALLBACK,
    MOCK_DEFAULT
}

data class AetherEngineResult(
    val plan: AetherDailyPlan,
    val status: AiStatus,
    val source: EngineResultSource = EngineResultSource.LOCAL_FALLBACK,
    val errorMessage: String? = null
)
