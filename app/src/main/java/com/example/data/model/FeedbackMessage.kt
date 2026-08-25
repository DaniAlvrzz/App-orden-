package com.example.data.model

import java.util.UUID

data class FeedbackMessage(
    val id: String = UUID.randomUUID().toString(),
    val message: String,
    val isError: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
