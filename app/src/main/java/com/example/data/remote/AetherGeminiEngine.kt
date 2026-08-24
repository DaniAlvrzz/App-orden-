package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class AetherGeminiEngine {

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val dailyPlanAdapter = moshi.adapter(AetherDailyPlan::class.java)

    /**
     * Orchestrates daily plan using Gemini API or intelligent deterministic fallback.
     */
    suspend fun orchestratePlan(
        readinessScore: Int,
        chronotype: Chronotype,
        existingTasks: List<TaskItem>,
        pantryItems: List<PantryItem>,
        todayDate: String = "2026-08-22"
    ): AetherDailyPlan = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val isRecovery = readinessScore < 60

        if (!apiKey.isNullOrEmpty() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val prompt = buildPlanPrompt(readinessScore, chronotype, existingTasks, pantryItems, todayDate)
                val request = GeminiRequest(
                    contents = listOf(
                        GeminiContent(
                            role = "user",
                            parts = listOf(GeminiPart(text = prompt))
                        )
                    ),
                    generationConfig = GeminiGenerationConfig(
                        temperature = 0.3f,
                        responseMimeType = "application/json"
                    ),
                    systemInstruction = GeminiContent(
                        role = "system",
                        parts = listOf(
                            GeminiPart(
                                text = "You are Aether OS, an automated Life Management Engine. You must return ONLY valid JSON matching AetherDailyPlan. Laws: 1) Frog Law: Maximum 1 Type A task. 2) Cognitive Ceiling Law: Max 3.5h (210 mins) Deep Work. 3) Recovery Protocol: If readiness < 60, zero Type A tasks, assign Type C admin tasks + recovery buffers. 4) Relational nutrition: use in-stock pantry items."
                            )
                        )
                    )
                )

                val response = GeminiApiClient.service.generateContent(apiKey, request)
                val rawJson = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text

                if (!rawJson.isNullOrEmpty()) {
                    val parsed = parsePlanJson(rawJson)
                    if (parsed != null) {
                        return@withContext parsed
                    }
                }
            } catch (e: Exception) {
                Log.e("AetherGeminiEngine", "Gemini API call failed, using deterministic engine: ${e.message}")
            }
        }

        // Deterministic High-Fidelity Engine
        return@withContext generateDeterministicPlan(readinessScore, chronotype, existingTasks, pantryItems, todayDate, isRecovery)
    }

    suspend fun generateCognitiveReframe(
        userFeeling: String,
        readinessScore: Int
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (!apiKey.isNullOrEmpty() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val prompt = "User statement: '$userFeeling'. User readiness score: $readinessScore/100. Provide a 2-3 sentence cognitive reframing based on biological regulation, zero guilt, and graceful adjustment. Do not lecture."
                val request = GeminiRequest(
                    contents = listOf(GeminiContent(role = "user", parts = listOf(GeminiPart(text = prompt)))),
                    generationConfig = GeminiGenerationConfig(temperature = 0.5f, responseMimeType = "text/plain")
                )
                val response = GeminiApiClient.service.generateContent(apiKey, request)
                val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!text.isNullOrBlank()) return@withContext text.trim()
            } catch (e: Exception) {
                Log.e("AetherGeminiEngine", "Reframe error: ${e.message}")
            }
        }

        // Deterministic Compassionate Reframes
        return@withContext when {
            readinessScore < 60 -> "Your nervous system is signaling genuine physiological fatigue, not a lack of willpower. Switching to Recovery Protocol protects tomorrow's performance."
            userFeeling.contains("guilt", ignoreCase = true) || userFeeling.contains("failed", ignoreCase = true) -> "A missed session is simply data, not identity. Grace Days exist to maintain long-term compliance without neuroticism."
            else -> "Energy naturally fluctuates in 90-minute ultradian cycles. Downshift to a 5-minute Type C micro-task to break inertia without taxing executive function."
        }
    }

    private fun parsePlanJson(json: String): AetherDailyPlan? {
        return try {
            val cleanJson = json.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
            dailyPlanAdapter.fromJson(cleanJson)
        } catch (e: Exception) {
            Log.e("AetherGeminiEngine", "JSON parse error: ${e.message}")
            null
        }
    }

    private fun buildPlanPrompt(
        readinessScore: Int,
        chronotype: Chronotype,
        tasks: List<TaskItem>,
        pantry: List<PantryItem>,
        date: String
    ): String {
        val inStockPantry = pantry.filter { it.inStock }.joinToString { it.name }
        val taskTitles = tasks.take(8).joinToString("; ") { "${it.title} (${it.energyLevel})" }
        return """
        Generate an AetherDailyPlan for Date: $date.
        User Biometrics: Readiness Score: $readinessScore, Chronotype: ${chronotype.name}.
        In-stock Pantry Items: $inStockPantry.
        Current Backlog Tasks: $taskTitles.
        Ensure strictly:
        - Max 1 frog task (0 if readiness < 60).
        - Max 210 minutes deep work total.
        - Relational meals with available ingredients.
        - Realistic time blocks covering 07:00 to 23:00.
        """.trimIndent()
    }

    private fun generateDeterministicPlan(
        readiness: Int,
        chronotype: Chronotype,
        tasks: List<TaskItem>,
        pantry: List<PantryItem>,
        date: String,
        isRecovery: Boolean
    ): AetherDailyPlan {
        val energyCurve = listOf(
            EnergyCurvePoint("06:00", if (chronotype == Chronotype.LION) 70 else 40),
            EnergyCurvePoint("08:00", if (chronotype == Chronotype.LION) 90 else if (chronotype == Chronotype.BEAR) 75 else 50),
            EnergyCurvePoint("10:00", if (isRecovery) 45 else if (chronotype == Chronotype.BEAR) 95 else 80),
            EnergyCurvePoint("12:00", if (isRecovery) 40 else 70),
            EnergyCurvePoint("14:00", 50), // Post-prandial dip
            EnergyCurvePoint("16:00", if (chronotype == Chronotype.WOLF) 90 else 65),
            EnergyCurvePoint("18:00", if (chronotype == Chronotype.WOLF) 85 else 55),
            EnergyCurvePoint("20:00", 40),
            EnergyCurvePoint("22:00", 20)
        )

        val frogTask = if (isRecovery) {
            null
        } else {
            tasks.firstOrNull { it.isFrog || it.energyLevel == EnergyLevel.HIGH }
                ?: TaskItem(
                    id = "frog-auto-1",
                    title = "Deep Focus: Complete Primary System Module",
                    description = "Protected high-demand focus slot.",
                    energyLevel = EnergyLevel.HIGH,
                    priorityType = PriorityType.FROG,
                    estimatedMinutes = 90,
                    isFrog = true,
                    category = "Deep Work"
                )
        }

        val mediumTasks = if (isRecovery) {
            listOf(
                TaskItem("rec-1", "Gentle Hydration & Zone 1 Walk", "Parasympathetic activation", EnergyLevel.LOW, PriorityType.MEDIUM, 25, false, false, "10:30 AM", "Recovery"),
                TaskItem("rec-2", "Low-Cognitive Inbox Sweep", "Sort emails without responding", EnergyLevel.LOW, PriorityType.MEDIUM, 15, false, false, "02:00 PM", "Admin")
            )
        } else {
            listOf(
                TaskItem("med-1", "Review System Specifications & Test Coverage", "Examine metrics", EnergyLevel.MEDIUM, PriorityType.MEDIUM, 40, false, false, "11:30 AM", "Review"),
                TaskItem("med-2", "Assemble Batch Base Quinoa & Protein Dinner", "Relational nutrition prep", EnergyLevel.MEDIUM, PriorityType.MEDIUM, 30, false, false, "06:00 PM", "Nutrition"),
                TaskItem("med-3", "Async Project Coordination & Review", "Team alignment ping", EnergyLevel.MEDIUM, PriorityType.MEDIUM, 30, false, false, "03:30 PM", "Admin")
            )
        }

        val quickWins = listOf(
            TaskItem("qk-1", "Archive 5 stale tabs", "", EnergyLevel.LOW, PriorityType.QUICK, 5, false, false, null, "Admin"),
            TaskItem("qk-2", "Refill electrolyte flask", "", EnergyLevel.LOW, PriorityType.QUICK, 3, true, false, null, "Habit"),
            TaskItem("qk-3", "Log morning readiness score", "", EnergyLevel.LOW, PriorityType.QUICK, 2, true, false, null, "Bio"),
            TaskItem("qk-4", "Check pantry staples", "", EnergyLevel.LOW, PriorityType.QUICK, 5, false, false, null, "Pantry"),
            TaskItem("qk-5", "10-minute eye relaxation protocol", "", EnergyLevel.LOW, PriorityType.QUICK, 10, false, false, null, "Recovery")
        )

        val timeBlocks = if (isRecovery) {
            listOf(
                TimeBlock("tb-1", "07:30", "08:30", BlockType.HABIT_ANCHOR, "Gentle Sunlight & Mineral Hydration"),
                TimeBlock("tb-2", "08:30", "09:15", BlockType.MEAL, "Warm Anti-Inflammatory Breakfast"),
                TimeBlock("tb-3", "09:30", "10:45", BlockType.COGNITIVE_RECOVERY_BUFFER, "Restorative Buffer / Gentle Reading"),
                TimeBlock("tb-4", "11:00", "12:00", BlockType.ADMIN_SLOT, "Low-Demand Admin & Filing"),
                TimeBlock("tb-5", "12:30", "13:30", BlockType.MEAL, "Nourishing Warm Broth & Quinoa Bowl"),
                TimeBlock("tb-6", "14:00", "15:00", BlockType.COGNITIVE_RECOVERY_BUFFER, "Zone 1 Nature Stroll & Audio"),
                TimeBlock("tb-7", "16:00", "17:00", BlockType.ADMIN_SLOT, "Passive Sorting / Light Logistics"),
                TimeBlock("tb-8", "21:30", "22:30", BlockType.SLEEP, "Early Biological Sleep Prep")
            )
        } else {
            listOf(
                TimeBlock("tb-1", "07:00", "08:00", BlockType.HABIT_ANCHOR, "Photonic Sunlight & Hydration Charge", true),
                TimeBlock("tb-2", "08:00", "08:45", BlockType.MEAL, "Low-Glycemic Sustained Fuel Breakfast", true),
                TimeBlock("tb-3", "09:00", "10:45", BlockType.DEEP_WORK, "🔥 FROG FOCUS: ${frogTask?.title ?: "Deep Work"}", false, frogTask?.id),
                TimeBlock("tb-4", "10:45", "11:15", BlockType.COGNITIVE_RECOVERY_BUFFER, "Active Eye-Rest & Hydration"),
                TimeBlock("tb-5", "11:15", "12:15", BlockType.DEEP_WORK, "Secondary Deep Block: System Architecture", false),
                TimeBlock("tb-6", "12:30", "13:30", BlockType.MEAL, "Batch Base Quinoa Power Bowl"),
                TimeBlock("tb-7", "14:00", "15:00", BlockType.MEETING, "Async Sync & Coordination"),
                TimeBlock("tb-8", "15:30", "16:30", BlockType.ADMIN_SLOT, "Admin Triage & Zero-Inbox"),
                TimeBlock("tb-9", "22:00", "23:00", BlockType.HABIT_ANCHOR, "Digital Sunset & Melatonin Buffer")
            )
        }

        val meals = DailyMealsPlan(
            breakfast = MealItem(
                id = "m-b",
                slot = MealSlot.BREAKFAST,
                title = "Avocado & Pastured Egg Scramble",
                description = "Low-glycemic dopamine baseline.",
                prepTimeMinutes = 10,
                ingredients = listOf("Organic Eggs", "Baby Spinach", "Avocados"),
                usesBatchCookedBase = false,
                allIngredientsInStock = true,
                bioImpact = BioGlycemicImpact.LOW_GLYCEMIC_FOCUS,
                isCompleted = true
            ),
            lunch = MealItem(
                id = "m-l",
                slot = MealSlot.LUNCH,
                title = "Batch Base Quinoa Bowl with Wild Salmon",
                description = "Pre-cooked base eliminates cooking overhead.",
                prepTimeMinutes = 8,
                ingredients = listOf("Cooked Tricolor Quinoa", "Wild Salmon Fillet", "Olive Oil"),
                usesBatchCookedBase = true,
                allIngredientsInStock = true,
                bioImpact = BioGlycemicImpact.MODERATE_STEADY,
                isCompleted = false
            ),
            dinner = MealItem(
                id = "m-d",
                slot = MealSlot.DINNER,
                title = "Roasted Sweet Potato Base & Steamed Greens",
                description = "Complex carbohydrate replenishment.",
                prepTimeMinutes = 12,
                ingredients = listOf("Roasted Sweet Potatoes", "Baby Spinach", "Chia Seeds"),
                usesBatchCookedBase = true,
                allIngredientsInStock = true,
                bioImpact = BioGlycemicImpact.DEEP_RECOVERY,
                isCompleted = false
            ),
            snack = MealItem(
                id = "m-s",
                slot = MealSlot.SNACK,
                title = "Polyphenol Green Matcha & Walnuts",
                description = "Clean steady mental stamina.",
                prepTimeMinutes = 3,
                ingredients = listOf("Chia Seeds"),
                usesBatchCookedBase = false,
                allIngredientsInStock = true,
                bioImpact = BioGlycemicImpact.LOW_GLYCEMIC_FOCUS,
                isCompleted = false
            )
        )

        val totalDeepWork = if (isRecovery) 0 else 165 // 105 + 60 = 165 min (< 210 min limit)

        return AetherDailyPlan(
            date = date,
            biometric_baseline = BiometricBaseline(
                readinessScore = readiness,
                perceivedEnergy = if (isRecovery) 45 else 80,
                sleepHours = if (isRecovery) 6.0 else 7.8,
                sleepQuality = if (isRecovery) 2 else 4,
                chronotype = chronotype,
                estimatedEnergyCurve = energyCurve,
                recoveryModeTriggered = isRecovery,
                graceDayActive = isRecovery
            ),
            top_3_priorities_1_3_5 = Top3Priorities(
                frog_task = frogTask,
                medium_tasks = mediumTasks,
                quick_wins = quickWins
            ),
            time_blocks = timeBlocks,
            suggested_tasks_by_energy_menu = SuggestedEnergyMenu(
                high_energy_backlog = tasks.filter { it.energyLevel == EnergyLevel.HIGH && !it.isFrog },
                medium_energy_backlog = tasks.filter { it.energyLevel == EnergyLevel.MEDIUM },
                low_energy_backlog = tasks.filter { it.energyLevel == EnergyLevel.LOW }
            ),
            daily_meals = meals,
            deep_work_minutes_allocated = totalDeepWork,
            max_cognitive_ceiling_minutes = 210,
            active_mode_label = if (isRecovery) "Recovery Protocol Mode" else "Balanced Circadian Mode",
            cognitive_reframing_message = if (isRecovery)
                "Recovery mode engaged. High-demand tasks deferred without friction. Today is for physiological restoration."
            else
                "Energy zenith synchronized. Protect your 1 Frog task during morning cortisol peak."
        )
    }
}
