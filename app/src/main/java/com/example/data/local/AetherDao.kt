package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE isFrog = 1 LIMIT 1")
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
