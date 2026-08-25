package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.i18n.AppLanguage

enum class AchievementId(
    val titleEs: String,
    val titleEn: String,
    val descEs: String,
    val descEn: String,
    val icon: ImageVector,
    val xpReward: Int = 20
) {
    FIRST_FROG(
        titleEs = "Primera FROG",
        titleEn = "First FROG",
        descEs = "Completa tu primera tarea de máxima prioridad (Tipo A)",
        descEn = "Complete your first high-priority FROG task",
        icon = Icons.Default.LocalFireDepartment,
        xpReward = 30
    ),
    FROGS_10(
        titleEs = "Cazador de FROGS (10)",
        titleEn = "FROG Hunter (10)",
        descEs = "Conquista 10 tareas biológicas Tipo A de alto impacto",
        descEn = "Conquer 10 biological high-impact Type A FROG tasks",
        icon = Icons.Default.Whatshot,
        xpReward = 100
    ),
    FIRST_HABIT(
        titleEs = "Primer Anclaje",
        titleEn = "First Habit Anchor",
        descEs = "Completa tu primer hábito circadiano del día",
        descEn = "Complete your first circadian habit of the day",
        icon = Icons.Default.Spa,
        xpReward = 20
    ),
    STREAK_7_DAYS(
        titleEs = "Racha de 7 Días",
        titleEn = "7-Day Habit Streak",
        descEs = "Mantén un anclaje circadiano durante 7 días seguidos",
        descEn = "Maintain a circadian habit for 7 consecutive days",
        icon = Icons.Default.Bolt,
        xpReward = 50
    ),
    STREAK_30_DAYS(
        titleEs = "Maestro de Hábitos",
        titleEn = "30-Day Habit Master",
        descEs = "Alcanza una racha de más de 30 días en cualquier anclaje",
        descEn = "Reach a 30+ day streak on any circadian anchor",
        icon = Icons.Default.WorkspacePremium,
        xpReward = 150
    ),
    PERFECT_DAY(
        titleEs = "Día Perfecto (100%)",
        titleEn = "Perfect Day (100%)",
        descEs = "Completa el 100% de todos tus hábitos circadianos en un día",
        descEn = "Complete 100% of all your circadian habits in a single day",
        icon = Icons.Default.Star,
        xpReward = 80
    ),
    PERFECT_WEEK(
        titleEs = "Semana Perfecta",
        titleEn = "Perfect Week",
        descEs = "Alcanza 7 días con 100% de anclajes completados o con Grace Days",
        descEn = "Achieve 7 days with 100% habit completion or active Grace Days",
        icon = Icons.Default.EmojiEvents,
        xpReward = 200
    ),
    TASKS_10(
        titleEs = "10 Tareas Conquistadas",
        titleEn = "10 Tasks Conquered",
        descEs = "Completa 10 tareas en el gestor de energía",
        descEn = "Complete 10 tasks in the energy manager",
        icon = Icons.Default.CheckCircle,
        xpReward = 40
    ),
    TASKS_100(
        titleEs = "100 Tareas Conquistadas",
        titleEn = "100 Tasks Conquered",
        descEs = "Alcanza el hito de 100 tareas finalizadas sin burnout",
        descEn = "Reach the milestone of 100 completed tasks",
        icon = Icons.Default.MilitaryTech,
        xpReward = 200
    ),
    GRACE_DAY_ACTIVATED(
        titleEs = "Protección Sin Culpa",
        titleEn = "Guilt-Free Grace",
        descEs = "Aplica un Día de Gracia para proteger una racha",
        descEn = "Apply a Grace Day to protect your habit streak",
        icon = Icons.Default.Shield,
        xpReward = 25
    ),
    PANTRY_5_ITEMS(
        titleEs = "Despensa Bioenergética",
        titleEn = "Bio-Pantry Stocked",
        descEs = "Registra 5 o más ingredientes disponibles en la despensa",
        descEn = "Have 5 or more ingredients in stock in your pantry",
        icon = Icons.Default.Restaurant,
        xpReward = 20
    ),
    FOCUS_BLOCK_DONE(
        titleEs = "Enfoque Profundo",
        titleEn = "Deep Focus Master",
        descEs = "Completa tu primer bloque del temporizador de enfoque",
        descEn = "Complete your first focus timer protocol block",
        icon = Icons.Default.Timer,
        xpReward = 30
    ),
    CIRCADIAN_SYNC(
        titleEs = "Sincronía Circadiana",
        titleEn = "Circadian Sync",
        descEs = "Configura tu cronotipo y ajusta tu readiness biológico",
        descEn = "Tune your chronotype and biological readiness",
        icon = Icons.Default.WbSunny,
        xpReward = 20
    ),
    COGNITIVE_REFRAME(
        titleEs = "Mente Imperturbable",
        titleEn = "Stoic Reframe",
        descEs = "Realiza una reformulación cognitiva con la IA",
        descEn = "Perform a cognitive reframe with Aether AI",
        icon = Icons.Default.Spa,
        xpReward = 25
    ),
    AI_ORCHESTRATION(
        titleEs = "Orquestación IA",
        titleEn = "AI Orchestration",
        descEs = "Sintetiza tu plan circadiano diario con el motor Aether",
        descEn = "Synthesize your daily circadian plan with the Aether engine",
        icon = Icons.Default.AutoAwesome,
        xpReward = 30
    );

    fun title(language: AppLanguage): String = if (language == AppLanguage.SPANISH) titleEs else titleEn
    fun description(language: AppLanguage): String = if (language == AppLanguage.SPANISH) descEs else descEn
}

data class AchievementItem(
    val id: AchievementId,
    val isUnlocked: Boolean = false,
    val unlockedAtTimestamp: Long? = null
)
