package com.example.data.usecase

import androidx.room.withTransaction
import com.example.data.local.*
import com.example.data.model.Chronotype
import com.example.data.model.CompletionItemType
import com.example.data.model.CompletionStatus
import com.example.data.model.DailyRolloverResult
import com.example.data.util.AetherDateUtils
import com.example.data.util.NoOpWidgetUpdater
import com.example.data.util.WidgetUpdater
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * UseCase implementing the midnight / new-day biological reset engine.
 * Responsibilities:
 * 1. Preserves completed tasks, habits, and meals into CompletionLogEntity for the previous date.
 * 2. Recalculates and persists DailySummaryEntity for the previous date.
 * 3. Archives completed tasks (isArchived = true) so they don't reappear as pending.
 * 4. Resets isCompleted = false on active tasks, time blocks, and meals for the new day.
 * 5. Evaluates habit streaks: resets to 0 only if missed and not protected by Grace Day.
 * 6. Ensures today's biometric baseline exists.
 * 7. Updates stored last active date and triggers widget sync.
 */
class DailyRolloverUseCase(
    private val database: AetherDatabase,
    private val taskDao: TaskDao,
    private val timeBlockDao: TimeBlockDao,
    private val habitDao: HabitDao,
    private val mealDao: MealDao,
    private val biometricDao: BiometricDao,
    private val completionLogDao: CompletionLogDao,
    private val dailySummaryDao: DailySummaryDao,
    private val preferencesManager: PreferencesManager,
    private val widgetUpdater: WidgetUpdater = NoOpWidgetUpdater
) {
    private val rolloverMutex = Mutex()

    suspend fun execute(): DailyRolloverResult? = rolloverMutex.withLock {
        withContext(Dispatchers.IO) {
            val today = AetherDateUtils.getTodayIso()
            val lastDate = preferencesManager.getLastActiveDate()

            if (lastDate == null) {
                preferencesManager.saveLastActiveDate(today)
                return@withContext null
            }

            if (lastDate == today) {
                return@withContext null
            }

            var completedTasks = 0
            var completedHabits = 0
            var completedMeals = 0
            var preservedStreaks = 0
            var brokenStreaks = 0
            var rolledOverTasks = 0

            database.withTransaction {
                val tasks = taskDao.getAllTasks().first()
                val habits = habitDao.getAllHabits().first()
                // Meals are per-day (dateIso), unlike habits/time-blocks: only the previous
                // day's meals belong to this rollover — using getAllMeals() here would touch
                // (and incorrectly reset) every historical meal ever logged.
                val meals = mealDao.getMealsForDate(lastDate).first()
                val timeBlocks = timeBlockDao.getAllTimeBlocks().first()

                // 1. Process Tasks for previous day
                tasks.forEach { task ->
                    if (task.isCompleted && !task.isArchived) {
                        completedTasks++
                        val existingLogs = completionLogDao.getLogsByDate(lastDate).first()
                        val alreadyLogged = existingLogs.any { it.itemId == task.id && it.status == CompletionStatus.COMPLETED }
                        if (!alreadyLogged) {
                            completionLogDao.insertLog(
                                CompletionLogEntity(
                                    dateIso = lastDate,
                                    itemType = CompletionItemType.TASK,
                                    itemId = task.id,
                                    title = task.title,
                                    status = CompletionStatus.COMPLETED,
                                    timestamp = System.currentTimeMillis()
                                )
                            )
                        }
                        if (task.isPermanent) {
                            // Recurring task: log it like any other completion, but reset for
                            // tomorrow instead of archiving — it must reappear every day.
                            taskDao.updateTask(task.copy(isCompleted = false, completedDate = ""))
                        } else {
                            // One-off task: archive it now that it's done.
                            taskDao.updateTask(task.copy(isArchived = true, completedDate = lastDate))
                        }
                    } else if (!task.isArchived) {
                        // Preserved in backlog as uncompleted
                        rolledOverTasks++
                        taskDao.updateTask(task.copy(isCompleted = false))
                    }
                }

                val hadMondayPassed = AetherDateUtils.hasMondayBetween(lastDate, today)

                // 2. Process Habits for previous day
                habits.forEach { habit ->
                    val isGraceProtected = habit.graceDayLastUsedDate == lastDate
                    val updatedGraceDaysUsed = if (hadMondayPassed) 0 else habit.graceDaysUsed
                    if (habit.isCompleted) {
                        completedHabits++
                        val existingLogs = completionLogDao.getLogsByDate(lastDate).first()
                        val alreadyLogged = existingLogs.any { it.itemId == habit.id && it.status == CompletionStatus.COMPLETED }
                        if (!alreadyLogged) {
                            completionLogDao.insertLog(
                                CompletionLogEntity(
                                    dateIso = lastDate,
                                    itemType = CompletionItemType.HABIT,
                                    itemId = habit.id,
                                    title = habit.title,
                                    status = CompletionStatus.COMPLETED,
                                    timestamp = System.currentTimeMillis()
                                )
                            )
                        }
                        habitDao.updateHabit(
                            habit.copy(
                                isCompleted = false,
                                graceDaysUsed = updatedGraceDaysUsed,
                                pendingStreakBeforeReset = 0
                            )
                        )
                    } else if (isGraceProtected) {
                        // Protected by Grace Day: Keep streak intact
                        preservedStreaks++
                        habitDao.updateHabit(
                            habit.copy(
                                isCompleted = false,
                                graceDaysUsed = updatedGraceDaysUsed,
                                pendingStreakBeforeReset = 0
                            )
                        )
                    } else {
                        // Missed without grace day: store previous streak before resetting to 0.
                        // lastCompletedDate is deliberately cleared here: leaving a stale date
                        // behind desynchronises it from streakDays (which is now 0) and corrupts
                        // the streak arithmetic in toggleHabitComplete on later days.
                        if (habit.streakDays > 0) brokenStreaks++
                        habitDao.updateHabit(
                            habit.copy(
                                isCompleted = false,
                                streakDays = 0,
                                lastCompletedDate = "",
                                graceDaysUsed = updatedGraceDaysUsed,
                                pendingStreakBeforeReset = habit.streakDays
                            )
                        )
                    }
                }

                // 3. Process Meals for previous day
                meals.forEach { meal ->
                    if (meal.isCompleted) {
                        completedMeals++
                        val existingLogs = completionLogDao.getLogsByDate(lastDate).first()
                        val alreadyLogged = existingLogs.any { it.itemId == meal.id && it.status == CompletionStatus.COMPLETED }
                        if (!alreadyLogged) {
                            completionLogDao.insertLog(
                                CompletionLogEntity(
                                    dateIso = lastDate,
                                    itemType = CompletionItemType.MEAL,
                                    itemId = meal.id,
                                    title = meal.title,
                                    status = CompletionStatus.COMPLETED,
                                    timestamp = System.currentTimeMillis()
                                )
                            )
                        }
                        mealDao.updateMeal(meal.copy(isCompleted = false))
                    }
                }

                // 4. Reset completed time blocks for new day
                timeBlocks.forEach { block ->
                    if (block.isCompleted) {
                        timeBlockDao.updateTimeBlock(block.copy(isCompleted = false))
                    }
                }

                // 5. Summarize previous day's metrics
                recalculateDailySummary(lastDate)

                // 5b. Backfill the skipped days between lastDate and today.
                // The rollover only runs when the app is opened, and it processes a single
                // transition (lastDate -> today). If the user didn't open the app for several
                // days, every day in between was never closed out: those dates had no logs and
                // no daily summary at all, so history/heatmap showed silent holes and streak
                // maths behaved as if only ONE day had been missed instead of N. Writing an
                // explicit MISSED entry per habit for each skipped day makes "the user wasn't
                // there" indistinguishable-in-outcome from "the user didn't do it" — which is
                // the correct semantics for a streak — while keeping the history honest.
                // Capped to avoid pathological writes if the app is reopened months later.
                val skippedDates = AetherDateUtils.datesBetweenExclusive(lastDate, today)
                if (skippedDates.isNotEmpty()) {
                    val habitsForBackfill = habitDao.getAllHabits().first()
                    skippedDates.forEach { skippedDate ->
                        val existingLogs = completionLogDao.getLogsByDate(skippedDate).first()
                        val alreadyLoggedIds = existingLogs.map { it.itemId }.toSet()
                        habitsForBackfill.forEach { habit ->
                            // Never overwrite a log the user created themselves for that date
                            // (e.g. a retroactive confirmation) — only fill genuine gaps.
                            if (!alreadyLoggedIds.contains(habit.id)) {
                                completionLogDao.insertLog(
                                    CompletionLogEntity(
                                        dateIso = skippedDate,
                                        itemType = CompletionItemType.HABIT,
                                        itemId = habit.id,
                                        title = habit.title,
                                        status = CompletionStatus.MISSED,
                                        timestamp = System.currentTimeMillis()
                                    )
                                )
                            }
                        }
                        recalculateDailySummary(skippedDate)
                    }
                }

                // 6. Ensure baseline biometrics for today
                val latestBio = biometricDao.getLatestBiometric().first()
                biometricDao.insertBiometric(
                    BiometricEntity(
                        date = today,
                        readinessScore = latestBio?.readinessScore ?: 75,
                        perceivedEnergy = latestBio?.perceivedEnergy ?: 75,
                        sleepHours = 7.5,
                        sleepQuality = 4,
                        chronotype = latestBio?.chronotype ?: Chronotype.BEAR,
                        recoveryModeTriggered = false,
                        graceDayActive = false
                    )
                )
            }

            // 7. Update last active date & widget sync
            preferencesManager.saveLastActiveDate(today)
            widgetUpdater.updateWidgets()

            DailyRolloverResult(
                previousDateIso = lastDate,
                currentDateIso = today,
                daysDiff = AetherDateUtils.daysBetween(lastDate, today),
                preservedHabitStreaksCount = preservedStreaks,
                brokenHabitStreaksCount = brokenStreaks,
                rolledOverTasksCount = rolledOverTasks,
                completedTasksCount = completedTasks,
                completedHabitsCount = completedHabits,
                completedMealsCount = completedMeals
            )
        }
    }

    private suspend fun recalculateDailySummary(dateIso: String) {
        val logs = completionLogDao.getLogsByDate(dateIso).first()
        val allHabits = habitDao.getAllHabits().first()
        val mealsForDate = mealDao.getMealsForDate(dateIso).first()

        val nonTaskLogs = logs.filter { it.itemType != CompletionItemType.TASK }
        val activeCount = (allHabits.size + mealsForDate.size).coerceAtLeast(nonTaskLogs.size)
        val totalCount = activeCount.coerceAtLeast(1)
        val completedCount = nonTaskLogs.count { it.status == CompletionStatus.COMPLETED }
        val partialCount = nonTaskLogs.count { it.status == CompletionStatus.PARTIAL }
        val ratio = if (allHabits.isNotEmpty() || mealsForDate.isNotEmpty() || nonTaskLogs.isNotEmpty()) {
            ((completedCount.toFloat() + partialCount.toFloat() * 0.5f) / totalCount.toFloat()).coerceIn(0f, 1f)
        } else 0f

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
