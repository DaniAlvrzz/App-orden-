package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.*
import com.example.ui.i18n.AppLanguage
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

data class AetherAiContext(
    val dateIso: String,
    val language: AppLanguage,
    val readinessScore: Int,
    val perceivedEnergy: Int,
    val sleepHours: Double,
    val sleepQuality: Int,
    val chronotype: Chronotype,
    val isRecoveryMode: Boolean,
    val isGraceDayActive: Boolean,
    val pendingTasks: List<TaskItem>,
    val habits: List<HabitAnchor>,
    val timeBlocks: List<TimeBlock>,
    val inStockPantry: List<PantryItem>,
    val meals: List<MealItem>,
    val deepWorkMinutesAllocated: Int,
    val maxCognitiveCeilingMinutes: Int = 210,
    val recentSummaries: List<DailySummary> = emptyList()
)

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

    /**
     * 5.3 & 5.4: Live streaming conversational response with full automatic real-time context.
     * Emits progressively formatted text tokens to the Flow.
     */
    fun streamChatResponse(
        userPrompt: String,
        context: AetherAiContext
    ): Flow<String> = flow {
        val apiKey = BuildConfig.GEMINI_API_KEY
        var fullResponseText: String? = null

        if (!apiKey.isNullOrEmpty() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val systemPrompt = buildSystemContextPrompt(context)
                val request = GeminiRequest(
                    contents = listOf(
                        GeminiContent(
                            role = "user",
                            parts = listOf(GeminiPart(text = userPrompt))
                        )
                    ),
                    generationConfig = GeminiGenerationConfig(
                        temperature = 0.6f,
                        responseMimeType = "text/plain"
                    ),
                    systemInstruction = GeminiContent(
                        role = "system",
                        parts = listOf(GeminiPart(text = systemPrompt))
                    )
                )

                val response = GeminiApiClient.service.generateContent(apiKey, request)
                val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!text.isNullOrBlank()) {
                    fullResponseText = text.trim()
                }
            } catch (e: Exception) {
                Log.e("AetherGeminiEngine", "Gemini Chat API call failed, falling back to deterministic response: ${e.message}")
            }
        }

        // If live API was unavailable or errored, generate intelligent bio-grounded deterministic response
        val responseToStream = fullResponseText ?: generateDeterministicChatResponse(userPrompt, context)

        // 5.4 Progressive token streaming simulation
        val chunks = responseToStream.split(Regex("(?<=\\s)|(?<=\\n)"))
        val stringBuffer = StringBuilder()

        for (chunk in chunks) {
            stringBuffer.append(chunk)
            emit(stringBuffer.toString())
            delay(18) // Smooth typewriter streaming feel
        }
    }.flowOn(Dispatchers.IO)

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

    private fun buildSystemContextPrompt(context: AetherAiContext): String {
        val lang = if (context.language == AppLanguage.SPANISH) "Spanish" else "English"
        val frog = context.pendingTasks.firstOrNull { it.isFrog }
        val highEnergyTasks = context.pendingTasks.filter { it.energyLevel == EnergyLevel.HIGH && !it.isFrog }
        val mediumTasks = context.pendingTasks.filter { it.energyLevel == EnergyLevel.MEDIUM }
        val lowTasks = context.pendingTasks.filter { it.energyLevel == EnergyLevel.LOW }
        val pantry = context.inStockPantry.filter { it.inStock }.joinToString { it.name }.ifEmpty { "Despensa estándar" }

        val habitsInfo = context.habits.joinToString("; ") { 
            "${it.title} [${if (it.isCompleted) "Hecho" else "Pendiente"}] (Racha: ${it.streakDays}d)" 
        }

        val blocksInfo = context.timeBlocks.take(6).joinToString("; ") { 
            "${it.startTime}-${it.endTime} ${it.title} [${if (it.isCompleted) "Completado" else "En curso"}]" 
        }

        return """
        You are Aether OS Bioenergetic Assistant, an elite neuro-chronobiology copilot.
        Leyes Operativas de Aether OS:
        1. Ley del Frog: Máximo 1 tarea de Alta Demanda (Tipo A) al día.
        2. Techo Cognitivo: Máximo 3.5h (210 min) de Deep Work diario.
        3. Protocolo de Recuperación: Si readiness < 60, cero tareas Tipo A, asignar buffers y descanso parasimpático.
        4. Nutrición Relacional: Priorizar comidas con ingredientes disponibles y bajo impacto glucémico.
        5. Disciplina sin Culpa: Usar Grace Days y reencuadre compasivo ante desvíos.

        CONTEXTO BIOLÓGICO REAL DEL USUARIO HOY:
        - Fecha: ${context.dateIso}
        - Biometría: Readiness Score ${context.readinessScore}/100 | Energía Percibida ${context.perceivedEnergy}/100 | Sueño: ${context.sleepHours}h (Calidad: ${context.sleepQuality}/5) | Cronotipo: ${context.chronotype.name} | Modo Recuperación: ${context.isRecoveryMode} | Grace Day Activo: ${context.isGraceDayActive}
        - Deep Work Asignado: ${context.deepWorkMinutesAllocated}/${context.maxCognitiveCeilingMinutes} min
        - Tarea FROG actual: ${frog?.title ?: "Ninguna designada"}
        - Backlog Tareas Pendientes:
          * Alta energía: ${highEnergyTasks.joinToString { it.title }.ifEmpty { "Ninguna" }}
          * Media energía: ${mediumTasks.joinToString { it.title }.ifEmpty { "Ninguna" }}
          * Baja energía (Quick Wins): ${lowTasks.joinToString { it.title }.ifEmpty { "Ninguna" }}
        - Estado de Hábitos Circadianos: $habitsInfo
        - Bloques del Cronograma: $blocksInfo
        - Ingredientes en Despensa: $pantry

        REGLAS DE RESPUESTA:
        - Idioma obligatorio: $lang
        - Estilo: Directo, empático, basado en ritmos circadianos y evidencia biológica. Formato estructurado y visualmente limpio (con emojis moderados y listas con viñetas).
        - Nunca des sermones moralistas. Da pautas accionables de 1 a 3 pasos con tiempos concretos.
        """.trimIndent()
    }

    /**
     * Intelligent Deterministic Engine for offline/fallback chat responses.
     */
    private fun generateDeterministicChatResponse(
        userPrompt: String,
        context: AetherAiContext
    ): String {
        val isSpanish = context.language == AppLanguage.SPANISH
        val lowerPrompt = userPrompt.lowercase()
        val frog = context.pendingTasks.firstOrNull { it.isFrog }
        val lowTasks = context.pendingTasks.filter { it.energyLevel == EnergyLevel.LOW }
        val mediumTasks = context.pendingTasks.filter { it.energyLevel == EnergyLevel.MEDIUM }

        // Quick Action 1: "Planifica mi día"
        if (lowerPrompt.contains("planifica") || lowerPrompt.contains("plan my day") || lowerPrompt.contains("plan")) {
            return if (isSpanish) {
                """
                🗓️ **Plan Circadiano Sincronizado para Hoy** (${context.dateIso})
                
                ⚡ **Estado Biológico:** Readiness ${context.readinessScore}/100 (${if (context.readinessScore < 60) "Modo Recuperación Activo 🛡️" else "Óptimo para Enfoque 🚀"})
                
                1. **🌅 Anclaje Matutino (07:00 - 09:00):**
                   • Luz solar directa de 10-15 min + Hidratación con electrolitos.
                   • Desayuno proteico de bajo impacto glucémico.
                
                2. **🔥 Bloque de Foco Profundo (09:00 - 11:30):**
                   ${if (frog != null) "• **FROG DEL DÍA:** ${frog.title} (${frog.estimatedMinutes} min)." else "• Trabajo en tareas de media energía sin exceder 90 min continuos."}
                   • *Techo cognitivo asignado:* ${context.deepWorkMinutesAllocated} / 210 min.
                
                3. **🥗 Recarga & Movimiento (12:30 - 16:00):**
                   • Almuerzo balanceado con bases cocinadas disponibles.
                   • Tareas administrativas ligeras o Quick Wins (${lowTasks.take(2).joinToString { it.title }.ifEmpty { "Revisión Inbox Zero" }}).
                
                4. **🌙 Cierre Parasimpático (21:30+):**
                   • Atardecer digital y preparación para el sueño restaurador.
                """.trimIndent()
            } else {
                """
                🗓️ **Synchronized Circadian Plan for Today** (${context.dateIso})
                
                ⚡ **Bio Baseline:** Readiness ${context.readinessScore}/100 (${if (context.readinessScore < 60) "Recovery Mode Active 🛡️" else "Peak Focus Ready 🚀"})
                
                1. **🌅 Morning Anchor (07:00 - 09:00):**
                   • 10-15 min morning light + electrolyte hydration.
                   • Steady-fuel protein breakfast.
                
                2. **🔥 Deep Work Block (09:00 - 11:30):**
                   ${if (frog != null) "• **FROG TASK:** ${frog.title} (${frog.estimatedMinutes} min)." else "• Focus on core priority tasks under 90-min ultradian cycles."}
                   • *Cognitive ceiling:* ${context.deepWorkMinutesAllocated} / 210 min.
                
                3. **🥗 Midday Fuel & Quick Wins (12:30 - 16:00):**
                   • Relational lunch bowl.
                   • Admin slots or quick wins (${lowTasks.take(2).joinToString { it.title }.ifEmpty { "Inbox Zero / Logistics" }}).
                
                4. **🌙 Parasympathetic Wind-down (21:30+):**
                   • Digital sunset & restorative sleep prep.
                """.trimIndent()
            }
        }

        // Quick Action 2: "Tengo poca energía"
        if (lowerPrompt.contains("poca energía") || lowerPrompt.contains("baja energía") || lowerPrompt.contains("low energy") || lowerPrompt.contains("cansad")) {
            return if (isSpanish) {
                """
                🌿 **Protocolo de Regulación Fisiológica (Cero Culpa)**
                
                Tu nivel de readiness actual es de **${context.readinessScore}/100**. La fatiga es una señal biológica de conservación, no una falla personal.
                
                **Protocolo de 3 Pasos Inmediatos:**
                1. 💧 **Carga de Hidratación:** Bebe 400ml de agua con una pizca de sal marina o electrolitos y haz 5 respiraciones fisiológicas (doble inhalación nasal, exhalación larga).
                2. 🛡️ **Pospón el Frog sin culpa:** Hoy se activa el *Protocolo de Protección*. Las tareas de alta demanda se reprograman.
                3. ⚡ **Micro-Tarea Tipo C (5-10 min):**
                   ${if (lowTasks.isNotEmpty()) "• Haz solo esta tarea ligera para romper inercia: **${lowTasks.first().title}**." else "• Haz una caminata suave en Zona 1 o descanso ocular de 15 minutos."}
                
                *Recuerda: Un día de baja demanda bien gestionado evita dos semanas de burnout.*
                """.trimIndent()
            } else {
                """
                🌿 **Physiological Regulation Protocol (Zero Guilt)**
                
                Your current readiness is **${context.readinessScore}/100**. Fatigue is a biological conservation signal, not a willpower failure.
                
                **Immediate 3-Step Protocol:**
                1. 💧 **Hydration Reset:** Drink 400ml water with minerals + 5 physiological sighs (double inhale, long exhale).
                2. 🛡️ **Gracefully Postpone High-Demand Tasks:** Switch to Recovery Protocol. No Type A tasks required today.
                3. ⚡ **Micro-Action (5-10 min):**
                   ${if (lowTasks.isNotEmpty()) "• Tackle just this low-demand task: **${lowTasks.first().title}**." else "• Take a 15-minute Zone 1 stroll or NSDR rest."}
                
                *A well-managed low-energy day prevents two weeks of chronic burnout.*
                """.trimIndent()
            }
        }

        // Quick Action 3: "Revisión semanal"
        if (lowerPrompt.contains("semanal") || lowerPrompt.contains("weekly") || lowerPrompt.contains("review") || lowerPrompt.contains("resumen")) {
            val completedHabitsCount = context.habits.count { it.isCompleted }
            val totalHabits = context.habits.size
            return if (isSpanish) {
                """
                📊 **Auditoría Bioenergética y Consistencia**
                
                • **Hábitos Circadianos:** $completedHabitsCount/$totalHabits completados hoy. Racha promedio de ${context.habits.map { it.streakDays }.average().toInt()} días.
                • **Carga Cognitiva:** ${context.deepWorkMinutesAllocated} minutos de trabajo profundo asignados (Límite saludable: 210 min).
                • **Calidad de Recuperación:** ${context.sleepHours}h de sueño promedio con calidad ${context.sleepQuality}/5.
                • **Tokens de Gracia:** ${context.habits.sumOf { it.graceDaysUsed }} Grace Days utilizados para mantener la identidad sin fractura.
                
                ✨ **Recomendación para el Próximo Ciclo:**
                Mantén el anclaje de luz matutina antes de las 08:30 y protege tu primer bloque de 90 min de foco profundo sin distracciones digitales.
                """.trimIndent()
            } else {
                """
                📊 **Bioenergetic Weekly Consistency Audit**
                
                • **Circadian Habits:** $completedHabitsCount/$totalHabits completed today. Average streak: ${context.habits.map { it.streakDays }.average().toInt()} days.
                • **Cognitive Load:** ${context.deepWorkMinutesAllocated} deep work minutes allocated (Healthy ceiling: 210 min).
                • **Sleep Baseline:** ${context.sleepHours}h average sleep with quality ${context.sleepQuality}/5.
                • **Grace Tokens:** ${context.habits.sumOf { it.graceDaysUsed }} Grace Days used to preserve streak identity without guilt.
                
                ✨ **Next Cycle Recommendation:**
                Lock in your morning photon anchor before 08:30 and guard your 90-min deep work block from digital interruptions.
                """.trimIndent()
            }
        }

        // Quick Action 4: "¿Qué hago ahora con 30 min?"
        if (lowerPrompt.contains("30 min") || lowerPrompt.contains("30min") || lowerPrompt.contains("qué hago") || lowerPrompt.contains("what should i do")) {
            val candidateTask = lowTasks.firstOrNull() ?: mediumTasks.firstOrNull()
            return if (isSpanish) {
                """
                ⏱️ **Optimización de Bloque de 30 Minutos**
                
                Con tu nivel de readiness actual (${context.readinessScore}/100), esta es la mejor acción de alto rendimiento biológico:
                
                ${if (candidateTask != null) {
                    "🎯 **Acción Recomendada:**\n• **${candidateTask.title}** (${candidateTask.category})\n• *Demanda:* ${candidateTask.energyLevel.name} (${candidateTask.estimatedMinutes} min est.)\n• *Estrategia:* Activa el Temporizador Focus de 25 min y cierra todas las pestañas secundarias."
                } else {
                    "🌿 **Buffer Restaurador de 30 Min:**\n• 15 min de caminata en naturaleza Zona 1 (sin auriculares).\n• 15 min de preparación de ingredientes base en la cocina para la cena."
                }}
                """.trimIndent()
            } else {
                """
                ⏱️ **30-Minute High-Yield Action Sprint**
                
                Given your readiness (${context.readinessScore}/100), here is your highest biological return action:
                
                ${if (candidateTask != null) {
                    "🎯 **Recommended Sprint:**\n• **${candidateTask.title}** (${candidateTask.category})\n• *Demand:* ${candidateTask.energyLevel.name} (${candidateTask.estimatedMinutes} min est.)\n• *Execution:* Launch the 25-min Focus Timer and eliminate secondary browser tabs."
                } else {
                    "🌿 **30-Min Restorative Buffer:**\n• 15 min Zone 1 walk without earbuds.\n• 15 min batch meal prep in the kitchen for evening recovery."
                }}
                """.trimIndent()
            }
        }

        // General fallback answer
        return if (isSpanish) {
            """
            🧠 **Análisis Biológico Aether OS**
            
            Entendido: *"userPrompt"*.
            
            Tomando en cuenta tu preparación fisiológica (${context.readinessScore}/100) y tus tareas pendientes (${context.pendingTasks.size}):
            
            1. **Alineación:** Asegura que tu foco se concentre en tareas Tipo ${if (context.readinessScore > 70) "A/B" else "C"} según tu curva hormonal actual.
            2. **Acción Inmediata:** ${if (frog != null && context.readinessScore >= 60) "Prioriza el avance de tu tarea Frog: **${frog.title}**." else "Ejecuta micro-avances de 15 minutos en tareas de baja fricción."}
            3. **Regulación:** Mantén intervalos de recuperación parasimpática cada 90 minutos para evitar fatiga de mantenimiento.
            """.trimIndent()
        } else {
            """
            🧠 **Aether OS Bioenergetic Analysis**
            
            Regarding: *"userPrompt"*.
            
            Synthesizing your live biometrics (${context.readinessScore}/100) and backlog (${context.pendingTasks.size} tasks):
            
            1. **Circadian Alignment:** Direct your executive bandwidth to Type ${if (context.readinessScore > 70) "A/B" else "C"} actions matching your diurnal curve.
            2. **Immediate Step:** ${if (frog != null && context.readinessScore >= 60) "Drive forward your Frog priority: **${frog.title}**." else "Execute low-friction 15-minute quick wins."}
            3. **Biological Buffer:** Take 5-10 min parasympathetic breaks every 90 minutes to prevent maintenance fatigue.
            """.trimIndent()
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
