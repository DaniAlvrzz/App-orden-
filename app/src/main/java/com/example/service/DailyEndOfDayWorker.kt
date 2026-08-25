package com.example.service

import android.content.Context
import android.util.Log
import androidx.work.*
import com.example.data.local.AetherDatabase
import com.example.data.local.CompletionLogEntity
import com.example.data.local.DailySummaryEntity
import com.example.data.model.CompletionItemType
import com.example.data.model.CompletionStatus
import com.example.data.util.AetherDateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

class DailyEndOfDayWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val database = AetherDatabase.getDatabase(applicationContext)
            val todayIso = AetherDateUtils.getTodayIso()

            val taskDao = database.taskDao()
            val habitDao = database.habitDao()
            val mealDao = database.mealDao()
            val timeBlockDao = database.timeBlockDao()
            val completionLogDao = database.completionLogDao()
            val dailySummaryDao = database.dailySummaryDao()

            val tasks = taskDao.getAllTasks().first()
            val habits = habitDao.getAllHabits().first()
            val meals = mealDao.getAllMeals().first()
            val blocks = timeBlockDao.getAllTimeBlocks().first()

            val existingLogs = completionLogDao.getLogsByDate(todayIso).first()
            val loggedItemIds = existingLogs.map { it.itemId }.toSet()

            val nowMillis = System.currentTimeMillis()
            val newLogs = mutableListOf<CompletionLogEntity>()

            // Mark uncompleted tasks as MISSED if not already logged
            tasks.forEach { task ->
                if (!task.isCompleted && !loggedItemIds.contains(task.id)) {
                    newLogs.add(
                        CompletionLogEntity(
                            dateIso = todayIso,
                            itemType = CompletionItemType.TASK,
                            itemId = task.id,
                            title = task.title,
                            status = CompletionStatus.MISSED,
                            timestamp = nowMillis
                        )
                    )
                }
            }

            // Mark uncompleted habits as MISSED
            habits.forEach { habit ->
                if (!habit.isCompleted && !loggedItemIds.contains(habit.id)) {
                    newLogs.add(
                        CompletionLogEntity(
                            dateIso = todayIso,
                            itemType = CompletionItemType.HABIT,
                            itemId = habit.id,
                            title = habit.title,
                            status = if (habit.graceDaysUsed > 0) CompletionStatus.PARTIAL else CompletionStatus.MISSED,
                            timestamp = nowMillis
                        )
                    )
                }
            }

            // Mark uncompleted meals as MISSED
            meals.forEach { meal ->
                if (!meal.isCompleted && !loggedItemIds.contains(meal.id)) {
                    newLogs.add(
                        CompletionLogEntity(
                            dateIso = todayIso,
                            itemType = CompletionItemType.MEAL,
                            itemId = meal.id,
                            title = meal.title,
                            status = CompletionStatus.MISSED,
                            timestamp = nowMillis
                        )
                    )
                }
            }

            // Mark uncompleted time blocks as MISSED
            blocks.forEach { block ->
                if (!block.isCompleted && !loggedItemIds.contains(block.id)) {
                    newLogs.add(
                        CompletionLogEntity(
                            dateIso = todayIso,
                            itemType = CompletionItemType.TIME_BLOCK,
                            itemId = block.id,
                            title = block.title,
                            status = CompletionStatus.MISSED,
                            timestamp = nowMillis
                        )
                    )
                }
            }

            if (newLogs.isNotEmpty()) {
                completionLogDao.insertLogs(newLogs)
            }

            // Recalculate daily summary
            val allTodayLogs = completionLogDao.getLogsByDate(todayIso).first()
            val totalCount = allTodayLogs.size.coerceAtLeast(tasks.size + habits.size + meals.size)
            val completedCount = allTodayLogs.count { it.status == CompletionStatus.COMPLETED }
            val partialCount = allTodayLogs.count { it.status == CompletionStatus.PARTIAL }
            val ratio = if (totalCount > 0) {
                ((completedCount.toFloat() + partialCount.toFloat() * 0.5f) / totalCount.toFloat()).coerceIn(0f, 1f)
            } else 0f

            dailySummaryDao.insertOrUpdateSummary(
                DailySummaryEntity(
                    dateIso = todayIso,
                    totalCount = totalCount,
                    completedCount = completedCount,
                    partialCount = partialCount,
                    ratio = ratio
                )
            )

            // Schedule the next 23:55 trigger
            scheduleDailyEndOfDayWork(applicationContext)

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error in DailyEndOfDayWorker", e)
            Result.retry()
        }
    }

    companion object {
        const val TAG = "DailyEndOfDayWorker"
        const val WORK_NAME = "aether_daily_end_of_day_work"

        fun scheduleDailyEndOfDayWork(context: Context) {
            val now = LocalDateTime.now()
            var target = now.with(LocalTime.of(23, 55, 0))
            if (now.isAfter(target)) {
                target = target.plusDays(1)
            }

            val delayMinutes = Duration.between(now, target).toMinutes().coerceAtLeast(1)

            val workRequest = OneTimeWorkRequestBuilder<DailyEndOfDayWorker>()
                .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresBatteryNotLow(false)
                        .build()
                )
                .addTag(TAG)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
            Log.d(TAG, "Scheduled DailyEndOfDayWorker in $delayMinutes minutes (at 23:55).")
        }
    }
}
