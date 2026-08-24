package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AetherGeminiEngine {

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val dailyPlanAdapter = moshi.adapter(AetherDailyPlan::class.java)

    /**
     * Orchestrates daily plan using Gemini API or intelligent deterministic fallback.
     * Returns an [AetherEngineResult] containing the plan, the [AiStatus], and error details if any.
     */
    suspend fun orchestratePlan(
        readinessScore: Int,
        chronotype: Chronotype,
        existingTasks: List<TaskItem>,
        pantryItems: List<PantryItem>,
        todayDate: String = com.example.data.util.AetherDateUtils.getTodayIso()
    ): AetherEngineResult = withContext(Dispatchers.IO) {
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
                        return@withContext AetherEngineResult(
                            plan = parsed,
                            status = AiStatus.LIVE,
                            errorMessage = null
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("AetherGeminiEngine", "Gemini API call failed, using deterministic engine: ${e.message}")
                val fallbackPlan = generateDeterministicPlan(readinessScore, chronotype, existingTasks, pantryItems, todayDate, isRecovery)
                return@withContext AetherEngineResult(
                    plan = fallbackPlan,
                    status = AiStatus.FALLBACK,
                    errorMessage = e.localizedMessage ?: "Gemini API error"
                )
            }
        }

        // Deterministic High-Fidelity Engine (No API Key or Offline)
        val plan = generateDeterministicPlan(readinessScore, chronotype, existingTasks, pantryItems, todayDate, isRecovery)
        return@withContext AetherEngineResult(
            plan = plan,
            status = AiStatus.FALLBACK,
            errorMessage = null
        )
    }

    suspend fun generateCognitiveReframe(
        userFeeling: String,
        readinessScore: Int
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (!apiKey.isNullOrEmpty() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val prompt = "User statement: '$userFeeling'. User readiness score: $readinessScore/100. Provide a 2-3 sentence cognitive reframing based on biological regulation, zero guilt, and graceful adjustment in Spanish. Do not lecture."
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

        // Deterministic Compassionate Reframes (Spanish)
        return@withContext when {
            readinessScore < 60 -> "Tu sistema nervioso está señalando fatiga fisiológica real, no falta de fuerza de voluntad. Cambiar al Protocolo de Recuperación protege el rendimiento de mañana."
            userFeeling.contains("culpa", ignoreCase = true) || userFeeling.contains("fallé", ignoreCase = true) || userFeeling.contains("guilt", ignoreCase = true) -> "Una sesión omitida es simplemente un dato biológico, no tu identidad. Los Días de Gracia existen para mantener la adherencia a largo plazo sin neurosis."
            else -> "La energía fluctúa de forma natural en ciclos ultradianos de 90 minutos. Cambia a una micro-tarea Tipo C de 5 minutos para romper la inercia sin sobrecargar la función ejecutiva."
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

        // Only select a real task if present in user backlog (never invent fake tasks)
        val frogTask = if (isRecovery) {
            null
        } else {
            tasks.firstOrNull { it.isFrog || it.energyLevel == EnergyLevel.HIGH }
        }

        val mediumTasks = if (isRecovery) {
            tasks.filter { !it.isFrog && (it.energyLevel == EnergyLevel.LOW || it.energyLevel == EnergyLevel.MEDIUM) }.take(2)
        } else {
            tasks.filter { !it.isFrog && it.energyLevel == EnergyLevel.MEDIUM }.take(3)
        }

        val quickWins = tasks.filter { !it.isFrog && it.energyLevel == EnergyLevel.LOW }.take(5)

        val timeBlocks = if (isRecovery) {
            listOf(
                TimeBlock("tb-1", "07:30", "08:30", BlockType.HABIT_ANCHOR, "Luz Solar Suave & Hidratación Mineral"),
                TimeBlock("tb-2", "08:30", "09:15", BlockType.MEAL, "Desayuno Templado Antiinflamatorio"),
                TimeBlock("tb-3", "09:30", "10:45", BlockType.COGNITIVE_RECOVERY_BUFFER, "Buffer Restaurador / Lectura Suave"),
                TimeBlock("tb-4", "11:00", "12:00", BlockType.ADMIN_SLOT, "Administración de Baja Demanda"),
                TimeBlock("tb-5", "12:30", "13:30", BlockType.MEAL, "Caldo Nutritivo & Bowl Ligero"),
                TimeBlock("tb-6", "14:00", "15:00", BlockType.COGNITIVE_RECOVERY_BUFFER, "Paseo en Naturaleza Zona 1"),
                TimeBlock("tb-7", "16:00", "17:00", BlockType.ADMIN_SLOT, "Organización Pasiva / Logística Ligera"),
                TimeBlock("tb-8", "21:30", "22:30", BlockType.SLEEP, "Preparación Biológica Temprana para el Sueño")
            )
        } else {
            listOf(
                TimeBlock("tb-1", "07:00", "08:00", BlockType.HABIT_ANCHOR, "Anclaje Fotónico & Carga de Hidratación", true),
                TimeBlock("tb-2", "08:00", "08:45", BlockType.MEAL, "Desayuno de Bajo Impacto Glucémico (Combustible Estable)", true),
                TimeBlock(
                    id = "tb-3",
                    startTime = "09:00",
                    endTime = "10:45",
                    blockType = BlockType.DEEP_WORK,
                    title = if (frogTask != null) "🔥 ENFOQUE FROG: ${frogTask.title}" else "🔥 Trabajo Profundo: Bloque de Máximo Foco",
                    isCompleted = false,
                    linkedTaskId = frogTask?.id
                ),
                TimeBlock("tb-4", "10:45", "11:15", BlockType.COGNITIVE_RECOVERY_BUFFER, "Descanso Ocular Activo & Hidratación"),
                TimeBlock("tb-5", "11:15", "12:15", BlockType.DEEP_WORK, "Bloque Secundario de Trabajo Profundo", false),
                TimeBlock("tb-6", "12:30", "13:30", BlockType.MEAL, "Almuerzo Relacional / Bowl Energético"),
                TimeBlock("tb-7", "14:00", "15:00", BlockType.MEETING, "Alineación Asíncrona & Coordinación"),
                TimeBlock("tb-8", "15:30", "16:30", BlockType.ADMIN_SLOT, "Triaje Administrativo & Inbox Zero"),
                TimeBlock("tb-9", "22:00", "23:00", BlockType.HABIT_ANCHOR, "Atardecer Digital & Disociación de Pantallas")
            )
        }

        val inStockProteins = pantry.filter { it.category == PantryCategory.PROTEIN && it.inStock }.map { it.name }
        val inStockCarbs = pantry.filter { it.category == PantryCategory.CARB_BASE && it.inStock }.map { it.name }
        val inStockFats = pantry.filter { it.category == PantryCategory.HEALTHY_FAT && it.inStock }.map { it.name }

        val meals = DailyMealsPlan(
            breakfast = MealItem(
                id = "m-b",
                slot = MealSlot.BREAKFAST,
                title = if (inStockProteins.isNotEmpty()) "Desayuno con ${inStockProteins.first()} y Grasas Saludables" else "Revuelto de Huevos y Aguacate",
                description = "Línea base de dopamina sin pico glucémico.",
                prepTimeMinutes = 10,
                ingredients = (inStockProteins.take(1) + inStockFats.take(1)).ifEmpty { listOf("Huevos", "Aguacate") },
                usesBatchCookedBase = false,
                allIngredientsInStock = true,
                bioImpact = BioGlycemicImpact.LOW_GLYCEMIC_FOCUS,
                isCompleted = false
            ),
            lunch = MealItem(
                id = "m-l",
                slot = MealSlot.LUNCH,
                title = if (inStockCarbs.isNotEmpty()) "Bowl Base de ${inStockCarbs.first()}" else "Bowl Energético de Base Cocinada",
                description = "Aprovecha la base cocinada para eliminar fricción.",
                prepTimeMinutes = 8,
                ingredients = (inStockCarbs.take(1) + inStockProteins.take(1)).ifEmpty { listOf("Quinoa", "Proteína") },
                usesBatchCookedBase = inStockCarbs.isNotEmpty(),
                allIngredientsInStock = true,
                bioImpact = BioGlycemicImpact.MODERATE_STEADY,
                isCompleted = false
            ),
            dinner = MealItem(
                id = "m-d",
                slot = MealSlot.DINNER,
                title = "Recarga Ligera & Hojas Verdes",
                description = "Carbohidratos complejos para activación parasimpática.",
                prepTimeMinutes = 12,
                ingredients = inStockCarbs.take(1).ifEmpty { listOf("Verduras", "Grasas Saludables") },
                usesBatchCookedBase = inStockCarbs.isNotEmpty(),
                allIngredientsInStock = true,
                bioImpact = BioGlycemicImpact.DEEP_RECOVERY,
                isCompleted = false
            ),
            snack = MealItem(
                id = "m-s",
                slot = MealSlot.SNACK,
                title = "Polifenoles & Frutos Secos",
                description = "Energía cerebral estable y limpia.",
                prepTimeMinutes = 3,
                ingredients = inStockFats.take(1).ifEmpty { listOf("Nueces / Infusión") },
                usesBatchCookedBase = false,
                allIngredientsInStock = true,
                bioImpact = BioGlycemicImpact.LOW_GLYCEMIC_FOCUS,
                isCompleted = false
            )
        )

        val totalDeepWork = if (isRecovery) 0 else 165

        val cognitiveMessage = when {
            tasks.isEmpty() -> "Horario circadiano optimizado. Captura tu primera tarea Frog (Tipo A) en el Backlog para anclarla en tu bloque de trabajo profundo."
            isRecovery -> "Protocolo de recuperación activado. Las tareas de alta demanda se han pospuesto sin fricción. Hoy el foco es la restauración fisiológica."
            else -> "Cenit de energía sincronizado. Protege tu bloque de máxima demanda durante el pico de cortisol matutino."
        }

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
            active_mode_label = if (isRecovery) "Modo Protocolo de Recuperación" else "Modo Circadiano Balanceado",
            cognitive_reframing_message = cognitiveMessage
        )
    }
}
