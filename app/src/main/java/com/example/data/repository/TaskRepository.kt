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

    suspend fun addTask(
        title: String,
        description: String,
        energyLevel: EnergyLevel,
        priorityType: PriorityType,
        estimatedMinutes: Int,
        category: String,
        makeFrog: Boolean = false
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

    override suspend fun addTask(
        title: String,
        description: String,
        energyLevel: EnergyLevel,
        priorityType: PriorityType,
        estimatedMinutes: Int,
        category: String,
        makeFrog: Boolean
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
            category = category.ifBlank { "General" }
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
        val newCompleted = !task.isCompleted
        val updated = task.copy(isCompleted = newCompleted)
        taskDao.updateTask(updated.toEntity())
        widgetUpdater.updateWidgets()

        if (newCompleted) {
            logActionAndRecalculate(CompletionItemType.TASK, task.id, task.title, CompletionStatus.COMPLETED)
        }
    }

    override suspend fun setTaskAsFrog(taskId: String) {
        taskDao.clearFrogStatus()
        taskDao.setFrogTask(taskId)
        widgetUpdater.updateWidgets()
    }

    override suspend fun deleteTask(taskId: String) {
        taskDao.deleteTask(taskId)
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
        val newCompleted = !block.isCompleted
        val updated = block.copy(isCompleted = newCompleted)
        timeBlockDao.updateTimeBlock(updated.toEntity())

        if (newCompleted) {
            logActionAndRecalculate(CompletionItemType.TIME_BLOCK, block.id, block.title, CompletionStatus.COMPLETED)
        }
    }

    override suspend fun deleteTimeBlock(id: String) {
        timeBlockDao.deleteTimeBlock(id)
    }

    override suspend fun breakDownTask(taskTitle: String, minutes: Int, language: AppLanguage): List<String> {
        return geminiEngine.breakDownTask(taskTitle, minutes, language)
    }

    override suspend fun recalculateDailySummary(dateIso: String) {
        val logs = completionLogDao.getLogsByDate(dateIso).first()
        val allTasks = taskDao.getAllTasks().first()
        val allHabits = habitDao.getAllHabits().first()
        val allMeals = mealDao.getAllMeals().first()

        val activeCount = (allTasks.size + allHabits.size + allMeals.size).coerceAtLeast(logs.size)
        val totalCount = activeCount.coerceAtLeast(1)
        val completedCount = logs.count { it.status == CompletionStatus.COMPLETED }
        val partialCount = logs.count { it.status == CompletionStatus.PARTIAL }
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

    private suspend fun logActionAndRecalculate(
        itemType: CompletionItemType,
        itemId: String,
        title: String,
        status: CompletionStatus,
        dateIso: String = AetherDateUtils.getTodayIso()
    ) {
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
