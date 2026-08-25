package com.example.data.model

data class UserLevelInfo(
    val totalXp: Int = 0,
    val currentLevel: Int = 1,
    val xpInCurrentLevel: Int = 0,
    val xpRequiredForNextLevel: Int = 100,
    val progressToNextLevel: Float = 0f,
    val rankTitleEs: String = "Iniciado Circadiano",
    val rankTitleEn: String = "Circadian Initiate"
) {
    companion object {
        const val XP_PER_LEVEL = 100

        fun calculate(totalXp: Int): UserLevelInfo {
            val safeXp = totalXp.coerceAtLeast(0)
            val level = (safeXp / XP_PER_LEVEL) + 1
            val currentLevelXp = safeXp % XP_PER_LEVEL
            val progress = (currentLevelXp.toFloat() / XP_PER_LEVEL.toFloat()).coerceIn(0f, 1f)

            val (titleEs, titleEn) = when (level) {
                1 -> "Iniciado Circadiano" to "Circadian Initiate"
                2 -> "Sincronizador Solar" to "Solar Synchronizer"
                3 -> "Arquitecto Bioenergético" to "Bioenergetic Architect"
                4 -> "Conquistador de FROGS" to "FROG Conqueror"
                5 -> "Maestro de Enfoque Profundo" to "Deep Focus Master"
                6 -> "Centinela Circadiano" to "Circadian Sentinel"
                7 -> "Alquimista Dopamínico" to "Dopaminergic Alchemist"
                8 -> "Estratega Bio-Resiliente" to "Bio-Resilient Strategist"
                9 -> "Sabio de la Elasticidad" to "Elasticity Sage"
                else -> "Gran Maestro Aether (Nv. $level)" to "Grand Aether Master (Lvl. $level)"
            }

            return UserLevelInfo(
                totalXp = safeXp,
                currentLevel = level,
                xpInCurrentLevel = currentLevelXp,
                xpRequiredForNextLevel = XP_PER_LEVEL,
                progressToNextLevel = progress,
                rankTitleEs = titleEs,
                rankTitleEn = titleEn
            )
        }
    }

    fun title(isSpanish: Boolean): String = if (isSpanish) rankTitleEs else rankTitleEn
}
