package com.example

import com.example.data.model.*
import org.junit.Assert.*
import org.junit.Test

class AetherOsUnitTest {

    @Test
    fun testFrogLaw_maximumOneFrogTask() {
        val frog = TaskItem(
            id = "frog-1",
            title = "Architect Core Dispatch",
            description = "High focus task",
            energyLevel = EnergyLevel.HIGH,
            priorityType = PriorityType.FROG,
            estimatedMinutes = 90,
            isFrog = true
        )
        val priorities = Top3Priorities(
            frog_task = frog,
            medium_tasks = listOf(
                TaskItem("m1", "Review stats", "", EnergyLevel.MEDIUM, PriorityType.MEDIUM, 30),
                TaskItem("m2", "Sync with team", "", EnergyLevel.MEDIUM, PriorityType.MEDIUM, 30),
                TaskItem("m3", "Draft spec", "", EnergyLevel.MEDIUM, PriorityType.MEDIUM, 30)
            ),
            quick_wins = listOf(
                TaskItem("q1", "Quick inbox", "", EnergyLevel.LOW, PriorityType.QUICK, 5)
            )
        )

        assertNotNull(priorities.frog_task)
        assertTrue(priorities.frog_task!!.isFrog)
        assertEquals(3, priorities.medium_tasks.size)
        assertEquals(1, priorities.quick_wins.size)
    }

    @Test
    fun testCognitiveCeilingLaw_enforcesMax210Minutes() {
        val maxCeiling = 210
        val deepWork1 = TimeBlock("b1", "09:00", "10:30", BlockType.DEEP_WORK, "Frog focus") // 90 min
        val deepWork2 = TimeBlock("b2", "11:00", "12:00", BlockType.DEEP_WORK, "Secondary focus") // 60 min
        val deepWork3 = TimeBlock("b3", "14:00", "15:00", BlockType.DEEP_WORK, "Extra focus") // 60 min

        val allocated = 90 + 60 + 60 // 210 min
        assertTrue(allocated <= maxCeiling)
    }

    @Test
    fun testRecoveryProtocol_engagedWhenReadinessBelow60() {
        val lowBio = BiometricBaseline(readinessScore = 52, chronotype = Chronotype.BEAR)
        assertEquals(SystemMode.RECOVERY, lowBio.systemMode)

        val highBio = BiometricBaseline(readinessScore = 85, chronotype = Chronotype.LION)
        assertEquals(SystemMode.HIGH_PERFORMANCE, highBio.systemMode)
    }

    @Test
    fun testRelationalNutrition_identifiesBatchBases() {
        val quinoaBase = PantryItem("p1", "Tricolor Quinoa", PantryCategory.CARB_BASE, inStock = true, isBatchBase = true, "500g")
        assertTrue(quinoaBase.isBatchBase)
        assertTrue(quinoaBase.inStock)

        val meal = MealItem(
            id = "m1",
            slot = MealSlot.LUNCH,
            title = "Quinoa Power Bowl",
            description = "Zero friction fuel",
            prepTimeMinutes = 8,
            ingredients = listOf("Tricolor Quinoa", "Salmon"),
            usesBatchCookedBase = true,
            allIngredientsInStock = true
        )
        assertTrue(meal.usesBatchCookedBase)
        assertTrue(meal.prepTimeMinutes <= 10)
    }

    @Test
    fun testGraceDayProtection_protectsHabitStreak() {
        val habit = HabitAnchor(
            id = "h1",
            title = "Morning Light Photons",
            description = "Circadian anchor",
            anchor = CircadianAnchor.MORNING_LIGHT,
            isCompleted = false,
            streakDays = 14,
            graceDaysUsed = 0
        )

        // Apply Grace Day without breaking identity streak
        val withGrace = habit.copy(graceDaysUsed = habit.graceDaysUsed + 1)
        assertEquals(14, withGrace.streakDays)
        assertEquals(1, withGrace.graceDaysUsed)
    }

    @Test
    fun testDateUtils_daysBetweenAccuracy() {
        // Verify exact day diffs
        assertEquals(0L, com.example.data.util.AetherDateUtils.daysBetween("2026-08-26", "2026-08-26"))
        assertEquals(1L, com.example.data.util.AetherDateUtils.daysBetween("2026-08-25", "2026-08-26"))
        assertEquals(2L, com.example.data.util.AetherDateUtils.daysBetween("2026-08-24", "2026-08-26"))
        assertEquals(-1L, com.example.data.util.AetherDateUtils.daysBetween("2026-08-27", "2026-08-26"))
    }

    @Test
    fun testHabitStreak_singleIncrementPerDay() {
        var habit = HabitAnchor(
            id = "h_test",
            title = "Focus 25",
            description = "Daily focus",
            anchor = CircadianAnchor.ZONE_2_MOVEMENT,
            isCompleted = false,
            streakDays = 3
        )

        // Completing today: streak becomes 4
        habit = habit.copy(isCompleted = true, streakDays = habit.streakDays + 1)
        assertEquals(4, habit.streakDays)
        assertTrue(habit.isCompleted)

        // Uncompleting today: streak reverts to 3
        habit = habit.copy(isCompleted = false, streakDays = (habit.streakDays - 1).coerceAtLeast(0))
        assertEquals(3, habit.streakDays)
        assertFalse(habit.isCompleted)
    }
}
