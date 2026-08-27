package com.example.data.repository

import com.example.data.local.CompletionLogDao
import com.example.data.local.CompletionLogEntity
import com.example.data.local.DailySummaryDao
import com.example.data.local.DailySummaryEntity
import com.example.data.local.HabitDao
import com.example.data.local.MealDao
import com.example.data.local.TaskDao
import com.example.data.local.TaskEntity
import com.example.data.local.TimeBlockDao
import com.example.data.local.TimeBlockEntity
import com.example.data.mapper.toEntity
import com.example.data.mapper.toModel
import com.example.data.model.*
import com.example.data.remote.AetherGeminiEngine
import com.example.data.util.AetherDateUtils
import com.example.data.util.NoOpWidgetUpdater
import com.example.data.util.WidgetUpdater
import com.example.ui.i18n.AppLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID

interface TaskRepository {
    val tasks: Flow<List<TaskItem>>
    val archivedTasks: Flow<List<TaskItem>>
    val timeBlocks: Flow<List<TimeBlock>>
    val quickNotes: Flow<List<QuickNoteItem>>
    val focusSessions: Flow<List<FocusSession>>
    val totalFocusMinutes: Flow<Int>

    suspend fun addTask(
        title: String,
        description: String,
        energyLevel: EnergyLevel,
        priorityType: PriorityType,
        estimatedMinutes: Int,
        category: String,
        makeFrog: Boolean = false,
        isPermanent: Boolean = false
    )
    suspend fun updateTask(task: TaskItem)
    suspend fun restoreTask(task: TaskItem)
    suspend fun reorderTasks(tasks: List<TaskItem>)
    suspend fun toggleTaskComplete(task: TaskItem)
    suspend fun setTaskAsFrog(taskId: String)
    suspend fun deleteTask(taskId: String)

    suspend fun addTimeBlock(
        startTime: String,
        endTime: String,
        blockType: BlockType,
        title: String,
        notes: String = ""
    )
    suspend fun updateTimeBlock(block: TimeBlock)
    suspend fun restoreTimeBlock(block: TimeBlock)
    suspend fun reorderTimeBlocks(blocks: List<TimeBlock>)
    suspend fun toggleTimeBlockComplete(block: TimeBlock)
    suspend fun deleteTimeBlock(id: String)

    suspend fun addQuickNote(content: String)
    suspend fun deleteQuickNote(id: String)
    suspend fun convertQuickNoteToTask(note: QuickNoteItem): TaskItem
    suspend fun recordFocusSession(session: FocusSession)

    suspend fun getYesterdayUnfinishedItems(targetDateIso: String? = null): Pair<List<HabitAnchor>, List<TaskItem>>
    suspend fun logRetroactiveCompletion(itemType: CompletionItemType, itemId: String, title: String)
    suspend fun clearPendingHabitStreaks()

    suspend fun breakDownTask(taskTitle: String, minutes: Int, language: AppLanguage): List<String>
    suspend fun recalculateDailySummary(dateIso: String = AetherDateUtils.getTodayIso())
}

class TaskRepositoryImpl(
    private val taskDao: TaskDao,
    private val timeBlockDao: TimeBlockDao,
    private val completionLogDao: CompletionLogDao,
    private val dailySummaryDao: DailySummaryDao,
    private val habitDao: HabitDao,
    private val mealDao: MealDao,
    private val quickNoteDao: com.example.data.local.QuickNoteDao? = null,
    private val focusSessionDao: com.example.data.local.FocusSessionDao? = null,
    private val geminiEngine: AetherGeminiEngine,
    private val widgetUpdater: WidgetUpdater = NoOpWidgetUpdater
) : TaskRepository {

    override val tasks: Flow<List<TaskItem>> = taskDao.getAllTasks().map { list ->
        list.filter { !it.isArchived }.map { it.toModel() }
    }

    override val archivedTasks: Flow<List<TaskItem>> = taskDao.getAllTasks().map { list ->
        list.filter { it.isArchived }.map { it.toModel() }
    }

    override val timeBlocks: Flow<List<TimeBlock>> = timeBlockDao.getAllTimeBlocks().map { list ->
        list.map { it.toModel() }
    }

    override val quickNotes: Flow<List<QuickNoteItem>> = quickNoteDao?.getActiveNotes()?.map { list ->
        list.map { it.toModel() }
    } ?: kotlinx.coroutines.flow.flowOf(emptyList())

    override val focusSessions: Flow<List<FocusSession>> = focusSessionDao?.getAllSessions()?.map { list ->
        list.map { it.toModel() }
    } ?: kotlinx.coroutines.flow.flowOf(emptyList())

    override val totalFocusMinutes: Flow<Int> = focusSessionDao?.getTotalFocusMinutes()?.map {
        it ?: 0
    } ?: kotlinx.coroutines.flow.flowOf(0)

    override suspend fun addTask(
        title: String,
        description: String,
        energyLevel: EnergyLevel,
        priorityType: PriorityType,
        estimatedMinutes: Int,
        category: String,
        makeFrog: Boolean,
        isPermanent: Boolean
    ) {
        val id = "task-" + UUID.randomUUID().toString().take(8)
        if (makeFrog || priorityType == PriorityType.FROG) {
            taskDao.clearFrogStatus()
        }
        val entity = TaskEntity(
            id = id,
            title = title,
            description = description,
            energyLevel = energyLevel,
            priorityType = if (makeFrog) PriorityType.FROG else priorityType,
            estimatedMinutes = estimatedMinutes,
            isCompleted = false,
            isFrog = makeFrog || priorityType == PriorityType.FROG,
            scheduledTime = null,
            category = category.ifBlank { "General" },
            isPermanent = isPermanent
        )
        taskDao.insertTask(entity)
        widgetUpdater.updateWidgets()
    }

    override suspend fun updateTask(task: TaskItem) {
        if (task.isFrog || task.priorityType == PriorityType.FROG) {
            taskDao.clearFrogStatus()
        }
        taskDao.updateTask(task.toEntity())
        widgetUpdater.updateWidgets()
    }

    override suspend fun restoreTask(task: TaskItem) {
        taskDao.insertTask(task.toEntity())
        widgetUpdater.updateWidgets()
    }

    override suspend fun reorderTasks(tasks: List<TaskItem>) {
        val baseTime = System.currentTimeMillis()
        tasks.forEachIndexed { index, task ->
            val updated = task.toEntity().copy(createdAt = baseTime - (index * 1000L))
            taskDao.updateTask(updated)
        }
    }

    override suspend fun toggleTaskComplete(task: TaskItem) {
        val today = AetherDateUtils.getTodayIso()
        if (task.isPermanent) {
            // Persistent / Recurring task:
            // Toggles completion for TODAY only, exactly like an ephemeral task, but never
            // archives — the daily rollover resets it fresh for tomorrow instead of archiving it.
            val newCompleted = !task.isCompleted
            val updated = task.copy(
                isCompleted = newCompleted,
                completedDate = if (newCompleted) today else ""
            )
            taskDao.updateTask(updated.toEntity())

            if (newCompleted) {
                logActionAndRecalculate(CompletionItemType.TASK, task.id, task.title, CompletionStatus.COMPLETED, today)
            } else {
                completionLogDao.deleteLogForItemAndDate(task.id, today)
                recalculateDailySummary(today)
            }
        } else {
            // Ephemeral task:
            // When marked as completed, archives from pending tasks and saves into history log.
            val newCompleted = !task.isCompleted
            val updated = task.copy(
                isCompleted = newCompleted,
                isArchived = newCompleted,
                completedDate = if (newCompleted) today else ""
            )
            taskDao.updateTask(updated.toEntity())

            if (newCompleted) {
                logActionAndRecalculate(CompletionItemType.TASK, task.id, task.title, CompletionStatus.COMPLETED, today)
            } else {
                completionLogDao.deleteLogForItemAndDate(task.id, today)
                recalculateDailySummary(today)
            }
        }
        widgetUpdater.updateWidgets()
    }

    override suspend fun setTaskAsFrog(taskId: String) {
        taskDao.clearFrogStatus()
        taskDao.setFrogTask(taskId)
        widgetUpdater.updateWidgets()
    }

    override suspend fun deleteTask(taskId: String) {
        val today = AetherDateUtils.getTodayIso()
        taskDao.deleteTask(taskId)
        completionLogDao.deleteLogForItemAndDate(taskId, today)
        recalculateDailySummary(today)
        widgetUpdater.updateWidgets()
    }

    override suspend fun addTimeBlock(
        startTime: String,
        endTime: String,
        blockType: BlockType,
        title: String,
        notes: String
    ) {
        val id = "block-" + UUID.randomUUID().toString().take(8)
        val entity = TimeBlockEntity(
            id = id,
            startTime = startTime,
            endTime = endTime,
            blockType = blockType,
            title = title,
            isCompleted = false,
            notes = notes,
            sortOrder = 99
        )
        timeBlockDao.insertTimeBlock(entity)
        widgetUpdater.updateWidgets()
    }

    override suspend fun updateTimeBlock(block: TimeBlock) {
        timeBlockDao.updateTimeBlock(block.toEntity())
        widgetUpdater.updateWidgets()
    }

    override suspend fun restoreTimeBlock(block: TimeBlock) {
        timeBlockDao.insertTimeBlock(block.toEntity())
        widgetUpdater.updateWidgets()
    }

    override suspend fun reorderTimeBlocks(blocks: List<TimeBlock>) {
        blocks.forEachIndexed { index, block ->
            timeBlockDao.updateTimeBlock(block.toEntity().copy(sortOrder = index))
        }
        widgetUpdater.updateWidgets()
    }

    override suspend fun toggleTimeBlockComplete(block: TimeBlock) {
        val today = AetherDateUtils.getTodayIso()
        val newCompleted = !block.isCompleted
        val updated = block.copy(isCompleted = newCompleted)
        timeBlockDao.updateTimeBlock(updated.toEntity())

        if (newCompleted) {
            logActionAndRecalculate(CompletionItemType.TIME_BLOCK, block.id, block.title, CompletionStatus.COMPLETED, today)
        } else {
            completionLogDao.deleteLogForItemAndDate(block.id, today)
            recalculateDailySummary(today)
        }
    }

    override suspend fun deleteTimeBlock(id: String) {
        val today = AetherDateUtils.getTodayIso()
        timeBlockDao.deleteTimeBlock(id)
        completionLogDao.deleteLogForItemAndDate(id, today)
        recalculateDailySummary(today)
    }

    override suspend fun breakDownTask(taskTitle: String, minutes: Int, language: AppLanguage): List<String> {
        return geminiEngine.breakDownTask(taskTitle, minutes, language)
    }

    override suspend fun recalculateDailySummary(dateIso: String) {
        val logs = completionLogDao.getLogsByDate(dateIso).first()
        val allHabits = habitDao.getAllHabits().first()
        val todaysMeals = mealDao.getMealsForDate(dateIso).first()

        val nonTaskLogs = logs.filter { it.itemType != CompletionItemType.TASK }
        val activeCount = (allHabits.size + todaysMeals.size).coerceAtLeast(nonTaskLogs.size)
        val totalCount = activeCount.coerceAtLeast(1)
        val completedCount = nonTaskLogs.count { it.status == CompletionStatus.COMPLETED }
        val partialCount = nonTaskLogs.count { it.status == CompletionStatus.PARTIAL }
        val ratio = if (allHabits.isNotEmpty() || todaysMeals.isNotEmpty() || nonTaskLogs.isNotEmpty()) {
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

    override suspend fun addQuickNote(content: String) {
        if (content.isBlank()) return
        val id = "note-" + UUID.randomUUID().toString().take(8)
        quickNoteDao?.insertNote(
            com.example.data.local.QuickNoteEntity(
                id = id,
                content = content.trim(),
                createdAt = System.currentTimeMillis()
            )
        )
    }

    override suspend fun deleteQuickNote(id: String) {
        quickNoteDao?.deleteNote(id)
    }

    override suspend fun convertQuickNoteToTask(note: QuickNoteItem): TaskItem {
        val taskId = "task-" + UUID.randomUUID().toString().take(8)
        val task = TaskEntity(
            id = taskId,
            title = note.content,
            description = "Creado desde Notas Rápidas",
            energyLevel = EnergyLevel.MEDIUM,
            priorityType = PriorityType.QUICK,
            estimatedMinutes = 15,
            category = "Inbox",
            createdAt = System.currentTimeMillis()
        )
        taskDao.insertTask(task)
        quickNoteDao?.updateNote(
            note.toEntity().copy(isProcessed = true, convertedToTaskId = taskId)
        )
        return task.toModel()
    }

    override suspend fun recordFocusSession(session: FocusSession) {
        focusSessionDao?.insertSession(session.toEntity())
    }

    override suspend fun getYesterdayUnfinishedItems(targetDateIso: String?): Pair<List<HabitAnchor>, List<TaskItem>> {
        val yesterdayIso = targetDateIso ?: java.time.LocalDate.now().minusDays(1).toString()
        val yesterdayLogs = completionLogDao.getLogsByDate(yesterdayIso).first()
        val completedItemIds = yesterdayLogs.filter { it.status == CompletionStatus.COMPLETED }.map { it.itemId }.toSet()

        val startOfTodayMillis = try {
            java.time.LocalDate.now().atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        } catch (e: Exception) {
            System.currentTimeMillis() - 86400000L
        }

        val allHabits = habitDao.getAllHabits().first().map { it.toModel() }
        val allTaskEntities = taskDao.getAllTasks().first()

        // Only real user habits that were not completed yesterday
        val unfinishedHabits = allHabits.filter { habit ->
            !completedItemIds.contains(habit.id) && habit.lastCompletedDate != yesterdayIso
        }

        // Only real user tasks created on or before yesterday that were not completed yesterday
        val unfinishedTasks = allTaskEntities.filter { task ->
            !task.isArchived &&
            !completedItemIds.contains(task.id) &&
            task.completedDate != yesterdayIso &&
            task.createdAt < startOfTodayMillis
        }.map { it.toModel() }

        return Pair(unfinishedHabits, unfinishedTasks)
    }

    override suspend fun logRetroactiveCompletion(itemType: CompletionItemType, itemId: String, title: String) {
        val yesterdayIso = java.time.LocalDate.now().minusDays(1).toString()
        val log = CompletionLogEntity(
            dateIso = yesterdayIso,
            itemType = itemType,
            itemId = itemId,
            title = title,
            status = CompletionStatus.COMPLETED,
            timestamp = System.currentTimeMillis()
        )
        completionLogDao.insertLog(log)
        recalculateDailySummary(yesterdayIso)

        if (itemType == CompletionItemType.HABIT) {
            val habit = habitDao.getAllHabits().first().find { it.id == itemId }
            if (habit != null) {
                val restoredStreak = if (habit.pendingStreakBeforeReset > 0) {
                    habit.pendingStreakBeforeReset + 1
                } else {
                    habit.streakDays + 1
                }
                habitDao.updateHabit(
                    habit.copy(
                        streakDays = restoredStreak,
                        pendingStreakBeforeReset = 0,
                        lastCompletedDate = yesterdayIso,
                        isCompleted = false
                    )
                )
            }
        } else if (itemType == CompletionItemType.TASK) {
            val task = taskDao.getAllTasks().first().find { it.id == itemId }
            if (task != null) {
                // Retroactively completed: mark completed for yesterday and archive so it does not clutter today
                taskDao.updateTask(task.copy(isCompleted = true, completedDate = yesterdayIso, isArchived = true))
            }
        }
        widgetUpdater.updateWidgets()
    }

    override suspend fun clearPendingHabitStreaks() {
        habitDao.clearPendingStreakResets()
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
}
