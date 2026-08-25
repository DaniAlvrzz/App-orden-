package com.example.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.AchievementId
import com.example.data.model.AchievementItem
import com.example.data.model.UserLevelInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.achievementDataStore by preferencesDataStore(name = "aether_achievements")

data class XpAddResult(
    val previousTotalXp: Int,
    val newTotalXp: Int,
    val previousLevel: Int,
    val newLevel: Int,
    val didLevelUp: Boolean,
    val addedXp: Int
)

class AchievementRepository(private val context: Context) {

    private val KEY_UNLOCKED_ACHIEVEMENTS = stringSetPreferencesKey("unlocked_achievements_set")
    private val KEY_TOTAL_XP = intPreferencesKey("user_total_xp")

    val achievements: Flow<List<AchievementItem>> = context.achievementDataStore.data.map { prefs ->
        val unlockedIds = prefs[KEY_UNLOCKED_ACHIEVEMENTS] ?: emptySet()
        AchievementId.entries.map { id ->
            AchievementItem(
                id = id,
                isUnlocked = unlockedIds.contains(id.name)
            )
        }
    }

    val userLevelInfo: Flow<UserLevelInfo> = context.achievementDataStore.data.map { prefs ->
        val totalXp = prefs[KEY_TOTAL_XP] ?: 0
        UserLevelInfo.calculate(totalXp)
    }

    suspend fun unlockAchievement(id: AchievementId): Boolean {
        var newlyUnlocked = false
        context.achievementDataStore.edit { prefs ->
            val current = prefs[KEY_UNLOCKED_ACHIEVEMENTS]?.toMutableSet() ?: mutableSetOf()
            if (!current.contains(id.name)) {
                current.add(id.name)
                prefs[KEY_UNLOCKED_ACHIEVEMENTS] = current
                newlyUnlocked = true
                // Also award XP for unlocking achievements
                val currentXp = prefs[KEY_TOTAL_XP] ?: 0
                prefs[KEY_TOTAL_XP] = currentXp + id.xpReward
            }
        }
        return newlyUnlocked
    }

    suspend fun addXp(amount: Int): XpAddResult {
        if (amount <= 0) {
            val current = 0
            val levelInfo = UserLevelInfo.calculate(current)
            return XpAddResult(current, current, levelInfo.currentLevel, levelInfo.currentLevel, false, 0)
        }

        var prevXp = 0
        var newXp = 0
        var prevLevel = 1
        var newLevel = 1
        var didLevelUp = false

        context.achievementDataStore.edit { prefs ->
            prevXp = prefs[KEY_TOTAL_XP] ?: 0
            newXp = prevXp + amount
            prefs[KEY_TOTAL_XP] = newXp

            prevLevel = UserLevelInfo.calculate(prevXp).currentLevel
            newLevel = UserLevelInfo.calculate(newXp).currentLevel
            didLevelUp = newLevel > prevLevel
        }

        return XpAddResult(
            previousTotalXp = prevXp,
            newTotalXp = newXp,
            previousLevel = prevLevel,
            newLevel = newLevel,
            didLevelUp = didLevelUp,
            addedXp = amount
        )
    }

    suspend fun resetAchievements() {
        context.achievementDataStore.edit { prefs ->
            prefs.remove(KEY_UNLOCKED_ACHIEVEMENTS)
            prefs.remove(KEY_TOTAL_XP)
        }
    }
}
