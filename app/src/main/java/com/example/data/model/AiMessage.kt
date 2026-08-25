package com.example.data.model

import java.util.UUID

data class AiMessage(
    val id: String = "msg-" + UUID.randomUUID().toString().take(8),
    val role: String, // "user" or "model"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val isStreaming: Boolean = false
) {
    val isUser: Boolean get() = role == "user"
    val isModel: Boolean get() = role == "model" || role == "assistant"
}

enum class AiQuickAction {
    PLAN_DAY,
    LOW_ENERGY,
    WEEKLY_REVIEW,
    THIRTY_MIN_TASK
}
