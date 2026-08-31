package com.example.data.repository

import com.example.data.local.CompletionLogDao
import com.example.data.local.CompletionLogEntity
import com.example.data.local.DailySummaryDao
import com.example.data.local.DailySummaryEntity
import com.example.data.local.HabitDao
import com.example.data.local.HabitEntity
import com.example.data.local.MealDao
import com.example.data.local.TaskDao
import com.example.data.mapper.toEntity
import com.example.data.mapper.toModel
import com.example.data.model.*
import com.example.data.remote.AetherGeminiEngine
import com.example.data.util.AetherDateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID

interface HabitRepository {
    val habits: Flow<List<HabitAnchor>>

    suspend fun addHabit(
        title: String,
        description: String,
        anchor: CircadianAnchor,
        streakDays: Int = 0,
        reframingTip: String = "Biological consistency is a pattern of return, not perfection."
    )
    suspend fun updateHabit(habit: HabitAnchor)
    suspend fun deleteHabit(id: String)
    suspend fun restoreHabit(habit: HabitAnchor)
    suspend fun toggleHabitComplete(habit: HabitAnchor): Result<Unit>
    suspend fun applyGraceDay(habit: HabitAnchor): Result<Unit>
    suspend fun getCognitiveReframe(userFeeling: String, readinessScore: Int): String
}

class HabitRepositoryImpl(
    private val habitDao: HabitDao,
    private val taskDao: TaskDao,
    private val mealDao: MealDao,
    private val completionLogDao: CompletionLogDao,
    private val dailySummaryDao: DailySummaryDao,
    private val geminiEngine: AetherGeminiEngine
) : HabitRepository {

    override val habits: Flow<List<HabitAnchor>> = habitDao.getAllHabits().map { list ->
        list.map { it.toModel() }
    }

    override suspend fun addHabit(
        title: String,
        description: String,
        anchor: CircadianAnchor,
        streakDays: Int,
        reframingTip: String
    ) {
        val id = "habit-" + UUID.randomUUID().toString().take(8)
        val entity = HabitEntity(
            id = id,
            title = title,
            description = description,
            anchor = anchor,
            isCompleted = false,
            streakDays = streakDays,
            graceDaysUsed = 0,
            reframingTip = reframingTip.ifBlank { "Biological consistency is a pattern of return, not perfection." }
        )
        habitDao.insertHabit(entity)
    }

    override suspend fun updateHabit(habit: HabitAnchor) {
        habitDao.updateHabit(habit.toEntity())
    }

    override suspend fun deleteHabit(id: String) {
        val today = AetherDateUtils.getTodayIso()
        habitDao.deleteHabit(id)
        completionLogDao.deleteLogForItemAndDate(id, today)
        recalculateDailySummary(today)
    }

    override suspend fun restoreHabit(habit: HabitAnchor) {
        habitDao.insertHabit(habit.toEntity())
    }

    override suspend fun toggleHabitComplete(habit: HabitAnchor): Result<Unit> {
        val today = AetherDateUtils.getTodayIso()
        val newCompleted = !habit.isCompleted

        // Mutual exclusivity: If Grace Day was used today, cannot mark as completed
        if (newCompleted && habit.graceDayLastUsedDate == today) {
            return Result.failure(IllegalStateException("GRACE_ALREADY_USED_TODAY"))
        }

        val (newStreak, newLastCompletedDate) = if (newCompleted) {
            if (habit.lastCompletedDate == today) {
                Pair(habit.streakDays, habit.lastCompletedDate)
            } else {
                Pair(habit.streakDays + 1, today)
            }
        } else {
            if (habit.lastCompletedDate == today) {
                // Undoing today's completion. Clearing lastCompletedDate outright would erase
                // the record of the previous genuine completion, so derive it from the streak
                // that remains: if a streak survives the undo, the last real completion was
                // yesterday; if the streak drops to 0 there is no prior completion to point at.
                val revertedStreak = maxOf(0, habit.streakDays - 1)
                val previousCompletion = if (revertedStreak > 0) {
                    AetherDateUtils.previousDay(today)
                } else {
                    ""
                }
                Pair(revertedStreak, previousCompletion)
            } else {
                Pair(habit.streakDays, habit.lastCompletedDate)
            }
        }

        habitDao.updateHabit(
            habit.toEntity().copy(
                isCompleted = newCompleted,
                streakDays = newStreak,
                lastCompletedDate = newLastCompletedDate,
                // Personal best only ever grows; it must survive future streak resets.
                bestStreakDays = maxOf(habit.bestStreakDays, newStreak)
            )
        )

        if (newCompleted) {
            logActionAndRecalculate(CompletionItemType.HABIT, habit.id, habit.title, CompletionStatus.COMPLETED, today)
        } else {
            completionLogDao.deleteLogForItemAndDate(habit.id, today)
            recalculateDailySummary(today)
        }
        return Result.success(Unit)
    }

    override suspend fun applyGraceDay(habit: HabitAnchor): Result<Unit> {
        val today = AetherDateUtils.getTodayIso()

        if (habit.isCompleted) {
            return Result.failure(IllegalStateException("HABIT_ALREADY_COMPLETED"))
        }
        if (habit.graceDayLastUsedDate == today) {
            return Result.failure(IllegalStateException("GRACE_ALREADY_USED_TODAY"))
        }
        if (habit.graceDaysUsed >= habit.maxGraceDaysPerPeriod) {
            return Result.failure(IllegalStateException("GRACE_LIMIT_REACHED"))
        }

        val updatedHabit = habit.toEntity().copy(
            graceDaysUsed = habit.graceDaysUsed + 1,
            graceDayLastUsedDate = today
        )
        habitDao.updateHabit(updatedHabit)
        logActionAndRecalculate(CompletionItemType.HABIT, habit.id, habit.title, CompletionStatus.PARTIAL, today)
        return Result.success(Unit)
    }

    override suspend fun getCognitiveReframe(userFeeling: String, readinessScore: Int): String {
        return geminiEngine.generateCognitiveReframe(userFeeling, readinessScore)
    }

    private suspend fun logActionAndRecalculate(
        itemType: CompletionItemType,
        itemId: String,
        title: String,
        status: CompletionStatus,
        dateIso: String = AetherDateUtils.getTodayIso()
    ) {
        completionLogDao.deleteLogForItemAndDate(itemId, dateIso)
        val log = CompletionLogEntity(
            dateIso = dateIso,
            itemType = itemType,
            itemId = itemId,
            title = title,
            status = status,
            timestamp = System.currentTimeMillis()
        )
        completionLogDao.insertLog(log)
        recalculateDailySummary(dateIso)
    }

    private suspend fun recalculateDailySummary(dateIso: String) {
        val logs = completionLogDao.getLogsByDate(dateIso).first()
        val allHabits = habitDao.getAllHabits().first()
        val todaysMeals = mealDao.getMealsForDate(dateIso).first()

        // Tasks count for history logs, but not towards the completion percentage
        val relevantLogs = logs.filter { it.itemType != CompletionItemType.TASK }
        val activeCount = (allHabits.size + todaysMeals.size).coerceAtLeast(relevantLogs.size)
        val totalCount = activeCount.coerceAtLeast(1)
        val completedCount = relevantLogs.count { it.status == CompletionStatus.COMPLETED }
        val partialCount = relevantLogs.count { it.status == CompletionStatus.PARTIAL }
        val ratio = ((completedCount.toFloat() + partialCount.toFloat() * 0.5f) / totalCount.toFloat()).coerceIn(0f, 1f)

        dailySummaryDao.insertOrUpdateSummary(
            DailySummaryEntity(
                dateIso = dateIso,
                totalCount = totalCount,
                completedCount = completedCount,
                partialCount = partialCount,
                ratio = ratio
            )
        )
    }
}
