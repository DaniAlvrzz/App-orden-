package com.example.data.mapper

import com.example.data.local.*
import com.example.data.model.*

/**
 * Clean domain-to-entity and entity-to-domain transformation mappers.
 */

fun TaskEntity.toModel() = TaskItem(
    id = id,
    title = title,
    description = description,
    energyLevel = energyLevel,
    priorityType = priorityType,
    estimatedMinutes = estimatedMinutes,
    isCompleted = isCompleted,
    isFrog = isFrog,
    scheduledTime = scheduledTime,
    category = category,
    isArchived = isArchived,
    completedDate = completedDate,
    isPermanent = isPermanent
)

fun TaskItem.toEntity() = TaskEntity(
    id = id,
    title = title,
    description = description,
    energyLevel = energyLevel,
    priorityType = priorityType,
    estimatedMinutes = estimatedMinutes,
    isCompleted = isCompleted,
    isFrog = isFrog,
    scheduledTime = scheduledTime,
    category = category,
    isArchived = isArchived,
    completedDate = completedDate,
    isPermanent = isPermanent
)

fun TimeBlockEntity.toModel() = TimeBlock(
    id = id,
    startTime = startTime,
    endTime = endTime,
    blockType = blockType,
    title = title,
    isCompleted = isCompleted,
    linkedTaskId = linkedTaskId,
    notes = notes
)

fun TimeBlock.toEntity() = TimeBlockEntity(
    id = id,
    startTime = startTime,
    endTime = endTime,
    blockType = blockType,
    title = title,
    isCompleted = isCompleted,
    linkedTaskId = linkedTaskId,
    notes = notes
)

fun PantryEntity.toModel() = PantryItem(
    id = id,
    name = name,
    category = category,
    inStock = inStock,
    isBatchBase = isBatchBase,
    quantityDesc = quantityDesc
)

fun PantryItem.toEntity() = PantryEntity(
    id = id,
    name = name,
    category = category,
    inStock = inStock,
    isBatchBase = isBatchBase,
    quantityDesc = quantityDesc
)

fun MealEntity.toModel(inStock: Boolean? = null) = MealItem(
    id = id,
    slot = slot,
    title = title,
    description = description,
    prepTimeMinutes = prepTimeMinutes,
    ingredients = ingredients,
    usesBatchCookedBase = usesBatchCookedBase,
    allIngredientsInStock = inStock ?: allIngredientsInStock,
    bioImpact = bioImpact,
    isCompleted = isCompleted,
    customSlotName = customSlotName,
    proteinGrams = proteinGrams,
    carbsGrams = carbsGrams,
    fatGrams = fatGrams,
    caloriesKcal = caloriesKcal,
    dateIso = dateIso
)

private val STOP_WORDS = setOf("de", "del", "la", "el", "los", "las", "un", "una", "unos", "unas", "y", "en", "con", "sin", "para", "por", "al", "a", "of", "the", "and", "in", "with", "for", "to")

private fun normalizeFoodText(text: String): String {
    val stripped = java.text.Normalizer.normalize(text.trim().lowercase(java.util.Locale.ROOT), java.text.Normalizer.Form.NFD)
        .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
    return stripped.replace(Regex("[^a-z0-9\\s]"), " ").replace(Regex("\\s+"), " ").trim()
}

private fun extractSignificantTokens(normalizedText: String): List<String> {
    return normalizedText.split(" ")
        .map { it.trim() }
        .filter { it.isNotEmpty() && !STOP_WORDS.contains(it) }
}

fun calculateMealIngredientsInStock(ingredients: List<String>, inStockPantryNames: Set<String>): Boolean {
    if (ingredients.isEmpty()) return true
    val normalizedPantryList = inStockPantryNames.map { pantryRaw ->
        val norm = normalizeFoodText(pantryRaw)
        val tokens = extractSignificantTokens(norm)
        Triple(pantryRaw, norm, tokens)
    }

    return ingredients.all { ingredient ->
        val normIng = normalizeFoodText(ingredient)
        val ingTokens = extractSignificantTokens(normIng).toSet()

        normalizedPantryList.any { (_, normPantry, pantryTokens) ->
            when {
                // 1. Exact full normalized equality
                normIng == normPantry -> true
                // 2. Both have significant tokens and pantry is a multi-word key matching inside ingredient
                pantryTokens.isNotEmpty() && pantryTokens.size > 1 -> {
                    pantryTokens.all { token -> ingTokens.contains(token) }
                }
                // 3. Pantry is a single significant token (e.g. "sal", "huevo", "arroz")
                pantryTokens.size == 1 -> {
                    val singlePantryToken = pantryTokens.first()
                    // Exact token match in ingredient's set of tokens (avoids "sal" matching "salmon" or "salsa")
                    ingTokens.contains(singlePantryToken) ||
                    // Plural/singular normalization: "huevos" <-> "huevo"
                    (singlePantryToken.endsWith("s") && ingTokens.contains(singlePantryToken.dropLast(1))) ||
                    ingTokens.any { it.endsWith("s") && it.dropLast(1) == singlePantryToken }
                }
                else -> false
            }
        }
    }
}

fun MealItem.toEntity() = MealEntity(
    id = id,
    slot = slot,
    title = title,
    description = description,
    prepTimeMinutes = prepTimeMinutes,
    ingredients = ingredients,
    usesBatchCookedBase = usesBatchCookedBase,
    allIngredientsInStock = allIngredientsInStock,
    bioImpact = bioImpact,
    isCompleted = isCompleted,
    customSlotName = customSlotName,
    proteinGrams = proteinGrams,
    carbsGrams = carbsGrams,
    fatGrams = fatGrams,
    caloriesKcal = caloriesKcal,
    dateIso = dateIso
)

fun HabitEntity.toModel() = HabitAnchor(
    id = id,
    title = title,
    description = description,
    anchor = anchor,
    isCompleted = isCompleted,
    streakDays = streakDays,
    graceDaysUsed = graceDaysUsed,
    maxGraceDaysPerPeriod = maxGraceDaysPerPeriod,
    graceDayLastUsedDate = graceDayLastUsedDate,
    lastCompletedDate = lastCompletedDate,
    reframingTip = reframingTip,
    pendingStreakBeforeReset = pendingStreakBeforeReset,
    bestStreakDays = bestStreakDays
)

fun HabitAnchor.toEntity() = HabitEntity(
    id = id,
    title = title,
    description = description,
    anchor = anchor,
    isCompleted = isCompleted,
    streakDays = streakDays,
    graceDaysUsed = graceDaysUsed,
    maxGraceDaysPerPeriod = maxGraceDaysPerPeriod,
    graceDayLastUsedDate = graceDayLastUsedDate,
    lastCompletedDate = lastCompletedDate,
    reframingTip = reframingTip,
    pendingStreakBeforeReset = pendingStreakBeforeReset,
    bestStreakDays = bestStreakDays
)

fun QuickNoteEntity.toModel() = QuickNoteItem(
    id = id,
    content = content,
    createdAt = createdAt,
    isProcessed = isProcessed,
    convertedToTaskId = convertedToTaskId
)

fun QuickNoteItem.toEntity() = QuickNoteEntity(
    id = id,
    content = content,
    createdAt = createdAt,
    isProcessed = isProcessed,
    convertedToTaskId = convertedToTaskId
)

fun FocusSessionEntity.toModel() = FocusSession(
    id = id,
    taskTitle = taskTitle,
    durationMinutes = durationMinutes,
    timestamp = timestamp,
    isCompleted = isCompleted,
    linkedTaskId = linkedTaskId,
    roundNumber = roundNumber
)

fun FocusSession.toEntity() = FocusSessionEntity(
    id = id,
    taskTitle = taskTitle,
    durationMinutes = durationMinutes,
    timestamp = timestamp,
    isCompleted = isCompleted,
    linkedTaskId = linkedTaskId,
    roundNumber = roundNumber
)

fun BiometricEntity.toModel(): BiometricBaseline {
    val curve = BiometricBaseline.generateDynamicEnergyCurve(
        readiness = readinessScore,
        chronotype = chronotype,
        sleepEndTime = sleepEndTime
    )
    return BiometricBaseline(
        readinessScore = readinessScore,
        computedReadinessScore = computedReadinessScore,
        perceivedEnergy = perceivedEnergy,
        sleepStartTime = sleepStartTime,
        sleepEndTime = sleepEndTime,
        sleepHours = sleepHours,
        sleepInterruptionsCount = sleepInterruptionsCount,
        sleepQuality = sleepQuality,
        wakeUpFeeling = wakeUpFeeling,
        currentEnergyLevel = currentEnergyLevel,
        stressLevel = stressLevel,
        motivationLevel = motivationLevel,
        caffeineIntake = caffeineIntake,
        exerciseDone = exerciseDone,
        mealRegularity = mealRegularity,
        mentalOverload = mentalOverload,
        emotionalConcern = emotionalConcern,
        chronotype = chronotype,
        dynamicCognitiveCeilingMinutes = dynamicCognitiveCeilingMinutes,
        cognitiveCeilingReason = cognitiveCeilingReason,
        estimatedEnergyCurve = curve,
        recoveryModeTriggered = recoveryModeTriggered,
        graceDayActive = graceDayActive
    )
}

fun BiometricBaseline.toEntity(date: String = com.example.data.util.AetherDateUtils.getTodayIso()) = BiometricEntity(
    date = date,
    readinessScore = readinessScore,
    computedReadinessScore = computedReadinessScore,
    perceivedEnergy = perceivedEnergy,
    sleepStartTime = sleepStartTime,
    sleepEndTime = sleepEndTime,
    sleepHours = sleepHours,
    sleepInterruptionsCount = sleepInterruptionsCount,
    sleepQuality = sleepQuality,
    wakeUpFeeling = wakeUpFeeling,
    currentEnergyLevel = currentEnergyLevel,
    stressLevel = stressLevel,
    motivationLevel = motivationLevel,
    caffeineIntake = caffeineIntake,
    exerciseDone = exerciseDone,
    mealRegularity = mealRegularity,
    mentalOverload = mentalOverload,
    emotionalConcern = emotionalConcern,
    chronotype = chronotype,
    dynamicCognitiveCeilingMinutes = dynamicCognitiveCeilingMinutes,
    cognitiveCeilingReason = cognitiveCeilingReason,
    recoveryModeTriggered = recoveryModeTriggered,
    graceDayActive = graceDayActive
)

fun CompletionLogEntity.toModel() = CompletionLog(
    id = id,
    dateIso = dateIso,
    itemType = itemType,
    itemId = itemId,
    title = title,
    status = status,
    timestamp = timestamp
)

fun CompletionLog.toEntity() = CompletionLogEntity(
    id = id,
    dateIso = dateIso,
    itemType = itemType,
    itemId = itemId,
    title = title,
    status = status,
    timestamp = timestamp
)

fun DailySummaryEntity.toModel() = DailySummary(
    dateIso = dateIso,
    totalCount = totalCount,
    completedCount = completedCount,
    partialCount = partialCount,
    ratio = ratio
)

fun DailySummary.toEntity() = DailySummaryEntity(
    dateIso = dateIso,
    totalCount = totalCount,
    completedCount = completedCount,
    partialCount = partialCount,
    ratio = ratio
)

fun AiMessageEntity.toModel(isStreaming: Boolean = false) = AiMessage(
    id = id,
    role = role,
    content = content,
    timestamp = timestamp,
    isFavorite = isFavorite,
    isStreaming = isStreaming
)

fun AiMessage.toEntity() = AiMessageEntity(
    id = id,
    role = role,
    content = content,
    timestamp = timestamp,
    isFavorite = isFavorite
)

