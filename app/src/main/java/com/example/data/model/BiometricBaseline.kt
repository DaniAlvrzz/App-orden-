package com.example.data.model

import com.squareup.moshi.JsonClass
import kotlin.math.roundToInt

enum class Chronotype(val title: String, val peakHours: String, val description: String) {
    LION("Lion (Early Bird)", "07:00 - 11:00", "Early morning focus peak, early crash"),
    BEAR("Bear (Circadian Standard)", "09:30 - 13:30", "Follows solar flow, mid-morning energy zenith"),
    WOLF("Wolf (Night Owl)", "16:00 - 21:00", "Late starter, peak creative burst late afternoon & night"),
    DOLPHIN("Dolphin (Variable/Light)", "14:00 - 18:00", "Sensitive nervous system, best in afternoon micro-bursts")
}

enum class SystemMode(val title: String, val badgeColorHex: Long, val description: String) {
    HIGH_PERFORMANCE("High Performance Mode", 0xFF00E5FF, "Optimal bio-energetics: 1 Frog + Deep focus slots enabled."),
    BALANCED("Balanced Circadian Mode", 0xFF2979FF, "Steady cadence: moderate demand with protective recovery buffers."),
    RECOVERY("Recovery Protocol Mode", 0xFF00E676, "Readiness < 60: Type A tasks suspended, restorative logistics only.")
}

enum class WakeUpFeeling(val titleEs: String, val titleEn: String, val emoji: String) {
    RESTED("Descansado / Lleno de energía", "Rested & Refreshed", "⚡"),
    NORMAL("Normal / Estable", "Normal & Neutral", "🙂"),
    GROGGY("Pesado / Inercia de sueño", "Groggy & Sluggish", "🥱"),
    EXHAUSTED("Agotado / Sin energía", "Exhausted / Low Energy", "😫")
}

enum class CaffeineIntake(val titleEs: String, val titleEn: String) {
    NONE("Ninguna cafeína", "No caffeine"),
    MODERATE("Moderada (1-2 tazas)", "Moderate (1-2 cups)"),
    HIGH("Alta o tardía (3+ tazas)", "High or late (3+ cups)")
}

enum class MealRegularity(val titleEs: String, val titleEn: String) {
    REGULAR("Comida equilibrada / a tiempo", "Balanced & On Time"),
    IRREGULAR("Irregular / Copiosa", "Irregular / Heavy"),
    FASTING("En ayuno intermitente", "Intermittent Fasting")
}

@JsonClass(generateAdapter = true)
data class CompassionModeState(
    val isActive: Boolean = false,
    val activatedDate: String = "",
    val reason: String = ""
)

@JsonClass(generateAdapter = true)
data class EnergyCurvePoint(
    val hour: String, // "06:00", "09:00", etc.
    val energyLevel: Int, // 0 - 100
    val phaseName: String = "",
    val phaseExplanation: String = "",
    val suggestedActivity: String = ""
)

@JsonClass(generateAdapter = true)
data class BiometricBaseline(
    val readinessScore: Int = 75, // 0 - 100
    val computedReadinessScore: Int = 75, // 0 - 100 (from Smart Check-in)
    val perceivedEnergy: Int = 75, // 0 - 100
    val sleepStartTime: String = "23:00",
    val sleepEndTime: String = "07:30",
    val sleepHours: Double = 7.5,
    val sleepInterruptionsCount: Int = 0,
    val sleepQuality: Int = 4, // 1 - 5
    val wakeUpFeeling: WakeUpFeeling = WakeUpFeeling.RESTED,
    val currentEnergyLevel: Int = 7, // 1 - 10
    val stressLevel: Int = 3, // 1 - 10
    val motivationLevel: Int = 7, // 1 - 10
    val caffeineIntake: CaffeineIntake = CaffeineIntake.MODERATE,
    val exerciseDone: Boolean = false,
    val mealRegularity: MealRegularity = MealRegularity.REGULAR,
    val mentalOverload: Boolean = false,
    val emotionalConcern: String = "",
    val chronotype: Chronotype = Chronotype.BEAR,
    val dynamicCognitiveCeilingMinutes: Int = 180,
    val cognitiveCeilingReason: String = "Línea base estándar ajustada por biometría.",
    val estimatedEnergyCurve: List<EnergyCurvePoint> = emptyList(),
    val recoveryModeTriggered: Boolean = false,
    val graceDayActive: Boolean = false
) {
    val systemMode: SystemMode
        get() = when {
            readinessScore < 60 || recoveryModeTriggered -> SystemMode.RECOVERY
            readinessScore >= 80 && stressLevel <= 5 -> SystemMode.HIGH_PERFORMANCE
            else -> SystemMode.BALANCED
        }

    companion object {
        /**
         * Calculates an objective readiness score (0 - 100) using multi-factor bioenergetics.
         */
        fun calculateObjectiveReadiness(
            sleepHours: Double,
            sleepQuality: Int,
            sleepInterruptions: Int,
            wakeFeeling: WakeUpFeeling,
            energyLevel1to10: Int,
            stressLevel1to10: Int,
            motivation1to10: Int,
            caffeine: CaffeineIntake,
            exerciseDone: Boolean,
            mealRegularity: MealRegularity,
            mentalOverload: Boolean,
            hasEmotionalConcern: Boolean
        ): Int {
            // 1. Sleep subscore (40%)
            val durationScore = when {
                sleepHours in 7.3..8.5 -> 100.0
                sleepHours in 6.5..7.2 || sleepHours in 8.6..9.2 -> 85.0
                sleepHours in 5.5..6.4 -> 65.0
                sleepHours in 4.5..5.4 -> 45.0
                sleepHours < 4.5 -> 25.0
                else -> 75.0 // Oversleep inertia
            }
            val qualityScore = (sleepQuality.coerceIn(1, 5) / 5.0) * 100.0
            val wakeScore = when (wakeFeeling) {
                WakeUpFeeling.RESTED -> 100.0
                WakeUpFeeling.NORMAL -> 70.0
                WakeUpFeeling.GROGGY -> 50.0
                WakeUpFeeling.EXHAUSTED -> 30.0
            }
            val interruptionPenalty = (sleepInterruptions.coerceAtLeast(0) * 6.0).coerceAtMost(25.0)
            val sleepSubtotal = ((durationScore * 0.40 + qualityScore * 0.35 + wakeScore * 0.25) - interruptionPenalty).coerceIn(10.0, 100.0)

            // 2. Nervous system & mental state (35%)
            val energyScore = energyLevel1to10.coerceIn(1, 10) * 10.0
            val motivationScore = motivation1to10.coerceIn(1, 10) * 10.0
            val stressScore = ((11 - stressLevel1to10.coerceIn(1, 10)) * 10.0)
            val overloadPenalty = if (mentalOverload) 15.0 else 0.0
            val stateSubtotal = ((energyScore * 0.40 + stressScore * 0.35 + motivationScore * 0.25) - overloadPenalty).coerceIn(10.0, 100.0)

            // 3. Regulators & Lifestyle (25%)
            val mealScore = when (mealRegularity) {
                MealRegularity.REGULAR -> 95.0
                MealRegularity.FASTING -> 85.0
                MealRegularity.IRREGULAR -> 65.0
            }
            val caffeineScore = when (caffeine) {
                CaffeineIntake.MODERATE -> 95.0
                CaffeineIntake.NONE -> 90.0
                CaffeineIntake.HIGH -> 70.0
            }
            val exerciseBoost = if (exerciseDone) 8.0 else 0.0
            val emotionalPenalty = if (hasEmotionalConcern) 12.0 else 0.0
            val lifestyleSubtotal = ((mealScore * 0.50 + caffeineScore * 0.50) + exerciseBoost - emotionalPenalty).coerceIn(10.0, 100.0)

            val total = (sleepSubtotal * 0.40 + stateSubtotal * 0.35 + lifestyleSubtotal * 0.25).roundToInt()
            return total.coerceIn(15, 100)
        }

        /**
         * Computes the dynamic cognitive ceiling budget (in minutes) and its clinical rationale.
         */
        fun calculateCognitiveCeiling(
            readiness: Int,
            sleepHours: Double = 7.5,
            sleepQuality: Int = 4,
            stressLevel: Int = 3,
            chronotype: Chronotype = Chronotype.BEAR,
            mentalOverload: Boolean = false
        ): Pair<Int, String> {
            return when {
                readiness >= 88 && sleepHours >= 7.2 && stressLevel <= 4 && !mentalOverload -> {
                    240 to "⚡ Modo Alto Rendimiento habilitado (+4.0h foco). Excelente arquitectura de sueño y bajo cortisol."
                }
                readiness >= 80 && stressLevel <= 5 && !mentalOverload -> {
                    210 to "🚀 Capacidad cognitiva óptima (3.5h foco). Sistema nervioso recuperado para 1 Frog + Bloques Deep Work."
                }
                readiness in 68..79 -> {
                    180 to "⚖️ Capacidad estándar balanceada (3.0h foco). Adecuada para foco sostenido con pausas ultradianas de 10 min."
                }
                readiness in 58..67 || mentalOverload -> {
                    135 to "⚠️ Presupuesto cognitivo moderado (2.25h foco). Limita las tareas complejas para no sobrecargar la corteza prefrontal."
                }
                readiness in 45..57 -> {
                    90 to "🛡️ Protocolo suave (1.5h foco). Fatiga acumulada detectada; prioriza micro-victorias y tareas Tipo C."
                }
                else -> {
                    45 to "🌿 Modo Restauración Activa (45 min máx). Reserva de energía baja; foco en recuperación fisiológica."
                }
            }
        }

        /**
         * Builds a detailed circadian energy curve with phase names, explanations, and activity tips.
         */
        fun generateDynamicEnergyCurve(
            readiness: Int,
            chronotype: Chronotype,
            sleepEndTime: String = "07:30"
        ): List<EnergyCurvePoint> {
            val ratio = (readiness / 100f).coerceIn(0.2f, 1.0f)
            return when (chronotype) {
                Chronotype.LION -> listOf(
                    EnergyCurvePoint("06:00", (75 * ratio).roundToInt(), "Anclaje Matutino", "Elevación temprana de cortisol y temperatura corporal.", "Luz solar y desayuno proteico."),
                    EnergyCurvePoint("08:00", (98 * ratio).roundToInt(), "Cénit Cognitivo", "Pico máximo de alerta y función ejecutiva.", "🔥 Tarea Frog / Trabajo Profundo."),
                    EnergyCurvePoint("10:30", (85 * ratio).roundToInt(), "Foco Secundario", "Mantenimiento de alta atención.", "Reuniones estratégicas o código complejo."),
                    EnergyCurvePoint("13:00", (50 * ratio).roundToInt(), "Valle Postprandial", "Digestión y bajón circadiano de adenosina.", "Almuerzo ligero y desconexión."),
                    EnergyCurvePoint("15:30", (65 * ratio).roundToInt(), "Rebote Vespertino", "Recuperación moderada para tareas operativas.", "Triaje administrativo y Quick Wins."),
                    EnergyCurvePoint("18:00", (45 * ratio).roundToInt(), "Desaceleración", "Disminución de la activación simpática.", "Caminata Zona 2 y logística suave."),
                    EnergyCurvePoint("21:00", (25 * ratio).roundToInt(), "Atardecer Digital", "Apertura de la ventana de melatonina.", "Lectura y preparación para dormir.")
                )
                Chronotype.BEAR -> listOf(
                    EnergyCurvePoint("07:00", (50 * ratio).roundToInt(), "Despertar Solar", "Calibración gradual del núcleo supraquiasmático.", "Agua con electrolitos y luz exterior."),
                    EnergyCurvePoint("09:30", (95 * ratio).roundToInt(), "Cénit Bioenergético", "Pico óptimo de dopamina y claridad ejecutiva.", "🔥 Tarea Frog / Trabajo Profundo."),
                    EnergyCurvePoint("12:00", (75 * ratio).roundToInt(), "Ventana Sostenida", "Enfoque productivo estable.", "Resolución de problemas y diseño."),
                    EnergyCurvePoint("14:00", (48 * ratio).roundToInt(), "Dip Circadiano", "Bajón postprandial de temperatura central.", "Almuerzo y descanso ocular activo."),
                    EnergyCurvePoint("16:30", (70 * ratio).roundToInt(), "Segunda Ventana", "Foco táctico antes de cerrar la jornada laboral.", "Quick Wins, emails y coordinación."),
                    EnergyCurvePoint("19:00", (55 * ratio).roundToInt(), "Transición Parasimpática", "Descanso cognitivo y cena reparadora.", "Movimiento suave y cena en familia."),
                    EnergyCurvePoint("22:00", (20 * ratio).roundToInt(), "Inducción Melatonina", "Preparación biológica del descanso profundo.", "Apagado de pantallas y relajación.")
                )
                Chronotype.WOLF -> listOf(
                    EnergyCurvePoint("08:30", (35 * ratio).roundToInt(), "Despertar Lento", "Inercia del sueño prolongada en cronotipo tardío.", "Hidratación y luz fuerte sin exigencia."),
                    EnergyCurvePoint("11:00", (65 * ratio).roundToInt(), "Activación Cognitiva", "Ascenso progresivo de la atención ejecutiva.", "Planificación, tareas medianas y lecturas."),
                    EnergyCurvePoint("14:00", (55 * ratio).roundToInt(), "Almuerzo Sincronizado", "Digestión y pausa metabólica.", "Nutrición rica en grasas saludables."),
                    EnergyCurvePoint("16:30", (92 * ratio).roundToInt(), "Cénit Creativo", "Pico circadiano principal de lucidez y creatividad.", "🔥 Tarea Frog / Trabajo Profundo."),
                    EnergyCurvePoint("19:30", (88 * ratio).roundToInt(), "Flujo Vespertino", "Alta concentración continuada.", "Proyectos complejos y desarrollo."),
                    EnergyCurvePoint("22:00", (60 * ratio).roundToInt(), "Cierre Creativo", "Canalización de ideas finales.", "Diario de ideas y organización pasiva."),
                    EnergyCurvePoint("00:00", (25 * ratio).roundToInt(), "Descanso Nocturno", "Transición al sueño reparador.", "Desconexión total y oscuridad.")
                )
                Chronotype.DOLPHIN -> listOf(
                    EnergyCurvePoint("07:30", (45 * ratio).roundToInt(), "Despertar Sensible", "Sistema nervioso en alerta temprana.", "Respiración diafragmática y sol."),
                    EnergyCurvePoint("10:00", (75 * ratio).roundToInt(), "Micro-Pico Matutino", "Primera ventana corta de alta claridad.", "🔥 Sprint de foco de 45-60 min."),
                    EnergyCurvePoint("13:00", (50 * ratio).roundToInt(), "Pausa Reparadora", "Evitar sobrecarga sensorial.", "Comida templada y paseo tranquilo."),
                    EnergyCurvePoint("15:30", (85 * ratio).roundToInt(), "Cénit Principal", "Ventana óptima de foco en cronotipo variable.", "Trabajo de alta demanda sin interrupciones."),
                    EnergyCurvePoint("18:30", (60 * ratio).roundToInt(), "Triaje Ligero", "Gestión de tareas de baja demanda.", "Micro-victorias y organización."),
                    EnergyCurvePoint("21:30", (30 * ratio).roundToInt(), "Descompresión", "Calma del sistema nervioso simpático.", "Baño tibio y lectura suave.")
                )
            }
        }
    }
}
