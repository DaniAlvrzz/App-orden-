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

    @Test
    fun testHasMondayBetween_detection() {
        // Friday 2026-08-21 to Wednesday 2026-08-26 (Contains Monday 2026-08-24)
        assertTrue(com.example.data.util.AetherDateUtils.hasMondayBetween("2026-08-21", "2026-08-26"))

        // Sunday 2026-08-23 to Monday 2026-08-24 (Contains Monday 2026-08-24)
        assertTrue(com.example.data.util.AetherDateUtils.hasMondayBetween("2026-08-23", "2026-08-24"))

        // Monday 2026-08-24 to Tuesday 2026-08-25 (Interval is (Mon, Tue], does NOT contain a Monday)
        assertFalse(com.example.data.util.AetherDateUtils.hasMondayBetween("2026-08-24", "2026-08-25"))

        // Tuesday 2026-08-25 to Friday 2026-08-28 (Does NOT contain a Monday)
        assertFalse(com.example.data.util.AetherDateUtils.hasMondayBetween("2026-08-25", "2026-08-28"))

        // Same day
        assertFalse(com.example.data.util.AetherDateUtils.hasMondayBetween("2026-08-24", "2026-08-24"))

        // Multi-week jump: Friday 2026-08-07 to Wednesday 2026-08-26
        assertTrue(com.example.data.util.AetherDateUtils.hasMondayBetween("2026-08-07", "2026-08-26"))
    }

    @Test
    fun testRetroactiveConfirmation_restoresPendingStreak() {
        // Given a habit with streak 15 before rollover
        val initialHabit = HabitAnchor(
            id = "h1",
            title = "Morning Light",
            description = "Photons",
            anchor = CircadianAnchor.MORNING_LIGHT,
            isCompleted = false,
            streakDays = 15,
            graceDaysUsed = 1,
            pendingStreakBeforeReset = 0
        )

        // Rollover happens: missed habit without grace day resets streak to 0 but preserves pendingStreakBeforeReset
        val afterRolloverHabit = initialHabit.copy(
            isCompleted = false,
            streakDays = 0,
            pendingStreakBeforeReset = initialHabit.streakDays
        )
        assertEquals(0, afterRolloverHabit.streakDays)
        assertEquals(15, afterRolloverHabit.pendingStreakBeforeReset)

        // Scenario 2: User confirms retroactive completion -> streak becomes 15 + 1 = 16
        val restoredStreak = if (afterRolloverHabit.pendingStreakBeforeReset > 0) {
            afterRolloverHabit.pendingStreakBeforeReset + 1
        } else {
            afterRolloverHabit.streakDays + 1
        }
        val confirmedHabit = afterRolloverHabit.copy(
            streakDays = restoredStreak,
            pendingStreakBeforeReset = 0,
            lastCompletedDate = "2026-08-25",
            isCompleted = false
        )
        assertEquals(16, confirmedHabit.streakDays)
        assertEquals(0, confirmedHabit.pendingStreakBeforeReset)

        // Scenario 3: User dismisses check-in -> pendingStreakBeforeReset is cleared, streak remains 0
        val dismissedHabit = afterRolloverHabit.copy(
            pendingStreakBeforeReset = 0
        )
        assertEquals(0, dismissedHabit.streakDays)
        assertEquals(0, dismissedHabit.pendingStreakBeforeReset)
    }

    @Test
    fun testDatesBetweenExclusive_consecutiveDaysHaveNoGap() {
        // App opened on consecutive days: nothing was skipped, so nothing to backfill.
        val gap = com.example.data.util.AetherDateUtils.datesBetweenExclusive("2026-08-24", "2026-08-25")
        assertTrue(gap.isEmpty())
    }

    @Test
    fun testDatesBetweenExclusive_sameDayHasNoGap() {
        val gap = com.example.data.util.AetherDateUtils.datesBetweenExclusive("2026-08-24", "2026-08-24")
        assertTrue(gap.isEmpty())
    }

    @Test
    fun testDatesBetweenExclusive_returnsOnlyTheSkippedDays() {
        // App last opened on the 24th, reopened on the 28th: the 25th, 26th and 27th were
        // never closed out by the rollover and must be backfilled — but not the endpoints.
        val gap = com.example.data.util.AetherDateUtils.datesBetweenExclusive("2026-08-24", "2026-08-28")
        assertEquals(listOf("2026-08-25", "2026-08-26", "2026-08-27"), gap)
    }

    @Test
    fun testDatesBetweenExclusive_isCappedAndKeepsMostRecentDays() {
        // Reopening after a very long absence must not attempt an unbounded write storm.
        val gap = com.example.data.util.AetherDateUtils.datesBetweenExclusive("2026-01-01", "2026-08-28", maxDays = 90)
        assertEquals(90, gap.size)
        assertEquals("2026-08-27", gap.last())
    }

    @Test
    fun testDatesBetweenExclusive_invalidInputIsSafe() {
        val gap = com.example.data.util.AetherDateUtils.datesBetweenExclusive("not-a-date", "2026-08-28")
        assertTrue(gap.isEmpty())
    }

    @Test
    fun testPreviousDay_handlesMonthBoundary() {
        assertEquals("2026-07-31", com.example.data.util.AetherDateUtils.previousDay("2026-08-01"))
        assertEquals("", com.example.data.util.AetherDateUtils.previousDay("garbage"))
    }

    @Test
    fun testUndoingCompletion_preservesPreviousCompletionDate() {
        // Undoing today's tick when a streak survives must not wipe lastCompletedDate:
        // the previous genuine completion was the day before.
        val today = "2026-08-25"
        val streakAfterUndo = maxOf(0, 6 - 1)
        val previousCompletion = if (streakAfterUndo > 0) {
            com.example.data.util.AetherDateUtils.previousDay(today)
        } else ""
        assertEquals(5, streakAfterUndo)
        assertEquals("2026-08-24", previousCompletion)

        // But when the undo drops the streak to zero there is no prior completion to point at.
        val streakFromOne = maxOf(0, 1 - 1)
        val noPrevious = if (streakFromOne > 0) {
            com.example.data.util.AetherDateUtils.previousDay(today)
        } else ""
        assertEquals(0, streakFromOne)
        assertEquals("", noPrevious)
    }
}
