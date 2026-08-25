package com.example.data.repository

import android.content.Context
import com.example.data.local.AiMessageDao
import com.example.data.local.TimeBlockDao
import com.example.data.mapper.toEntity
import com.example.data.mapper.toModel
import com.example.data.model.*
import com.example.data.remote.AetherAiContext
import com.example.data.remote.AetherGeminiEngine
import com.example.data.util.NoOpWidgetUpdater
import com.example.data.util.WidgetUpdater
import com.example.service.AetherNotificationScheduler
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface AiRepository {
    val aiMessages: Flow<List<AiMessage>>
    val favoriteAiMessages: Flow<List<AiMessage>>

    suspend fun orchestrateDailyPlan(
        readiness: Int,
        chronotype: Chronotype,
        currentTasks: List<TaskItem>,
        currentPantry: List<PantryItem>,
        context: Context? = null
    ): AetherEngineResult

    fun streamChatResponse(prompt: String, context: AetherAiContext): Flow<String>
    suspend fun saveAiMessage(message: AiMessage)
    suspend fun updateAiMessage(message: AiMessage)
    suspend fun toggleAiMessageFavorite(id: String, isFavorite: Boolean)
    suspend fun deleteAiMessage(id: String)
    suspend fun clearAllAiMessages()
    fun exportPlanAsJson(plan: AetherDailyPlan): String
}

class AiRepositoryImpl(
    private val aiMessageDao: AiMessageDao,
    private val timeBlockDao: TimeBlockDao,
    private val geminiEngine: AetherGeminiEngine,
    private val widgetUpdater: WidgetUpdater = NoOpWidgetUpdater
) : AiRepository {

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val planAdapter = moshi.adapter(AetherDailyPlan::class.java).indent("  ")

    override val aiMessages: Flow<List<AiMessage>> = aiMessageDao.getAllMessages().map { list ->
        list.map { it.toModel() }
    }

    override val favoriteAiMessages: Flow<List<AiMessage>> = aiMessageDao.getFavoriteMessages().map { list ->
        list.map { it.toModel() }
    }

    override suspend fun orchestrateDailyPlan(
        readiness: Int,
        chronotype: Chronotype,
        currentTasks: List<TaskItem>,
        currentPantry: List<PantryItem>,
        context: Context?
    ): AetherEngineResult {
        val result = geminiEngine.orchestratePlan(
            readinessScore = readiness,
            chronotype = chronotype,
            existingTasks = currentTasks,
            pantryItems = currentPantry
        )

        timeBlockDao.clearAllTimeBlocks()
        timeBlockDao.insertTimeBlocks(result.plan.time_blocks.mapIndexed { index, b ->
            b.toEntity().copy(sortOrder = index)
        })

        context?.let { ctx ->
            AetherNotificationScheduler.scheduleTimeBlockAlerts(ctx, result.plan.time_blocks)
        }
        widgetUpdater.updateWidgets()

        return result
    }

    override fun streamChatResponse(prompt: String, context: AetherAiContext): Flow<String> {
        return geminiEngine.streamChatResponse(prompt, context)
    }

    override suspend fun saveAiMessage(message: AiMessage) {
        aiMessageDao.insertMessage(message.toEntity())
    }

    override suspend fun updateAiMessage(message: AiMessage) {
        aiMessageDao.updateMessage(message.toEntity())
    }

    override suspend fun toggleAiMessageFavorite(id: String, isFavorite: Boolean) {
        aiMessageDao.setFavorite(id, isFavorite)
    }

    override suspend fun deleteAiMessage(id: String) {
        aiMessageDao.deleteMessage(id)
    }

    override suspend fun clearAllAiMessages() {
        aiMessageDao.clearAllMessages()
    }

    override fun exportPlanAsJson(plan: AetherDailyPlan): String {
        return planAdapter.toJson(plan)
    }
}
