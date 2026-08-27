package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE isArchived = 0 ORDER BY createdAt DESC")
    fun getActiveTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE isArchived = 1 ORDER BY createdAt DESC")
    fun getArchivedTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE isFrog = 1 AND isArchived = 0 LIMIT 1")
    fun getFrogTask(): Flow<TaskEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskEntity>)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTask(id: String)

    @Query("UPDATE tasks SET isFrog = 0")
    suspend fun clearFrogStatus()

    @Query("UPDATE tasks SET isFrog = 1, priorityType = 'FROG' WHERE id = :id")
    suspend fun setFrogTask(id: String)

    @Query("DELETE FROM tasks")
    suspend fun clearAllTasks()
}

@Dao
interface TimeBlockDao {
    @Query("SELECT * FROM time_blocks ORDER BY sortOrder ASC, startTime ASC")
    fun getAllTimeBlocks(): Flow<List<TimeBlockEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimeBlock(block: TimeBlockEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimeBlocks(blocks: List<TimeBlockEntity>)

    @Update
    suspend fun updateTimeBlock(block: TimeBlockEntity)

    @Query("UPDATE time_blocks SET isCompleted = 0")
    suspend fun resetAllCompletion()

    @Query("DELETE FROM time_blocks WHERE id = :id")
    suspend fun deleteTimeBlock(id: String)

    @Query("DELETE FROM time_blocks")
    suspend fun clearAllTimeBlocks()
}

@Dao
interface PantryDao {
    @Query("SELECT * FROM pantry_items ORDER BY category ASC, name ASC")
    fun getAllPantryItems(): Flow<List<PantryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: PantryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<PantryEntity>)

    @Update
    suspend fun updateItem(item: PantryEntity)

    @Query("DELETE FROM pantry_items WHERE id = :id")
    suspend fun deleteItem(id: String)

    @Query("UPDATE pantry_items SET inStock = :inStock WHERE id = :id")
    suspend fun setStockStatus(id: String, inStock: Boolean)

    @Query("DELETE FROM pantry_items")
    suspend fun clearAllPantry()
}

@Dao
interface MealDao {
    @Query("SELECT * FROM meals")
    fun getAllMeals(): Flow<List<MealEntity>>

    @Query("SELECT * FROM meals WHERE dateIso = :dateIso")
    fun getMealsForDate(dateIso: String): Flow<List<MealEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(meal: MealEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeals(meals: List<MealEntity>)

    @Update
    suspend fun updateMeal(meal: MealEntity)

    @Query("DELETE FROM meals WHERE id = :id")
    suspend fun deleteMeal(id: String)

    @Query("DELETE FROM meals")
    suspend fun clearAllMeals()
}

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits")
    fun getAllHabits(): Flow<List<HabitEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabits(habits: List<HabitEntity>)

    @Update
    suspend fun updateHabit(habit: HabitEntity)

    @Query("UPDATE habits SET streakDays = 0 WHERE isCompleted = 0 AND graceDaysUsed = 0")
    suspend fun breakStreaksForIncompleteHabits()

    @Query("UPDATE habits SET isCompleted = 0, graceDaysUsed = 0")
    suspend fun resetForNewDay()

    @Query("UPDATE habits SET pendingStreakBeforeReset = 0")
    suspend fun clearPendingStreakResets()

    @Query("DELETE FROM habits WHERE id = :id")
    suspend fun deleteHabit(id: String)

    @Query("DELETE FROM habits")
    suspend fun clearAllHabits()
}

@Dao
interface BiometricDao {
    @Query("SELECT * FROM biometrics WHERE date = :date LIMIT 1")
    fun getBiometric(date: String): Flow<BiometricEntity?>

    @Query("SELECT * FROM biometrics ORDER BY date DESC, id DESC LIMIT 1")
    fun getLatestBiometric(): Flow<BiometricEntity?>

    @Query("SELECT * FROM biometrics ORDER BY date DESC LIMIT :limit")
    fun getRecentBiometrics(limit: Int = 30): Flow<List<BiometricEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBiometric(biometric: BiometricEntity)

    @Query("DELETE FROM biometrics")
    suspend fun clearAllBiometrics()
}

@Dao
interface CompletionLogDao {
    @Query("SELECT * FROM completion_logs WHERE dateIso = :dateIso ORDER BY timestamp DESC")
    fun getLogsByDate(dateIso: String): Flow<List<CompletionLogEntity>>

    @Query("SELECT * FROM completion_logs WHERE dateIso BETWEEN :startDateIso AND :endDateIso ORDER BY timestamp DESC")
    fun getLogsBetweenDates(startDateIso: String, endDateIso: String): Flow<List<CompletionLogEntity>>

    @Query("SELECT * FROM completion_logs WHERE itemId = :itemId ORDER BY dateIso ASC, timestamp ASC")
    fun getLogsByItemId(itemId: String): Flow<List<CompletionLogEntity>>

    @Query("SELECT * FROM completion_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<CompletionLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: CompletionLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogs(logs: List<CompletionLogEntity>)

    @Query("DELETE FROM completion_logs WHERE itemId = :itemId AND dateIso = :dateIso")
    suspend fun deleteLogForItemAndDate(itemId: String, dateIso: String)

    @Query("DELETE FROM completion_logs WHERE itemId = :itemId")
    suspend fun deleteLogsForItem(itemId: String)

    @Query("DELETE FROM completion_logs WHERE id = :id")
    suspend fun deleteLog(id: Int)

    @Query("DELETE FROM completion_logs WHERE dateIso = :dateIso")
    suspend fun deleteLogsForDate(dateIso: String)

    @Query("DELETE FROM completion_logs")
    suspend fun clearAllLogs()
}

@Dao
interface DailySummaryDao {
    @Query("SELECT * FROM daily_summaries WHERE dateIso = :dateIso LIMIT 1")
    fun getSummary(dateIso: String): Flow<DailySummaryEntity?>

    @Query("SELECT * FROM daily_summaries ORDER BY dateIso ASC")
    fun getAllSummaries(): Flow<List<DailySummaryEntity>>

    @Query("SELECT * FROM daily_summaries WHERE dateIso LIKE :yearMonthPrefix ORDER BY dateIso ASC")
    fun getSummariesForMonth(yearMonthPrefix: String): Flow<List<DailySummaryEntity>>

    @Query("SELECT * FROM daily_summaries WHERE dateIso BETWEEN :startDateIso AND :endDateIso ORDER BY dateIso ASC")
    fun getSummariesBetweenDates(startDateIso: String, endDateIso: String): Flow<List<DailySummaryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSummary(summary: DailySummaryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSummaries(summaries: List<DailySummaryEntity>)

    @Query("DELETE FROM daily_summaries")
    suspend fun clearAllSummaries()
}

@Dao
interface AiMessageDao {
    @Query("SELECT * FROM ai_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<AiMessageEntity>>

    @Query("SELECT * FROM ai_messages WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoriteMessages(): Flow<List<AiMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: AiMessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<AiMessageEntity>)

    @Update
    suspend fun updateMessage(message: AiMessageEntity)

    @Query("UPDATE ai_messages SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: String, isFavorite: Boolean)

    @Query("DELETE FROM ai_messages WHERE id = :id")
    suspend fun deleteMessage(id: String)

    @Query("DELETE FROM ai_messages")
    suspend fun clearAllMessages()
}

@Dao
interface QuickNoteDao {
    @Query("SELECT * FROM quick_notes WHERE isProcessed = 0 ORDER BY createdAt DESC")
    fun getActiveNotes(): Flow<List<QuickNoteEntity>>

    @Query("SELECT * FROM quick_notes ORDER BY createdAt DESC")
    fun getAllNotes(): Flow<List<QuickNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: QuickNoteEntity)

    @Update
    suspend fun updateNote(note: QuickNoteEntity)

    @Query("DELETE FROM quick_notes WHERE id = :id")
    suspend fun deleteNote(id: String)

    @Query("DELETE FROM quick_notes")
    suspend fun clearAllNotes()
}

@Dao
interface FocusSessionDao {
    @Query("SELECT * FROM focus_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<FocusSessionEntity>>

    @Query("SELECT * FROM focus_sessions WHERE timestamp >= :sinceTimestamp ORDER BY timestamp DESC")
    fun getSessionsSince(sinceTimestamp: Long): Flow<List<FocusSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: FocusSessionEntity)

    @Query("SELECT SUM(durationMinutes) FROM focus_sessions WHERE isCompleted = 1")
    fun getTotalFocusMinutes(): Flow<Int?>

    @Query("DELETE FROM focus_sessions")
    suspend fun clearAllSessions()
}

