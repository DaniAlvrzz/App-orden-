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
    category = category
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
    category = category
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

fun MealEntity.toModel() = MealItem(
    id = id,
    slot = slot,
    title = title,
    description = description,
    prepTimeMinutes = prepTimeMinutes,
    ingredients = ingredients,
    usesBatchCookedBase = usesBatchCookedBase,
    allIngredientsInStock = allIngredientsInStock,
    bioImpact = bioImpact,
    isCompleted = isCompleted
)

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
    isCompleted = isCompleted
)

fun HabitEntity.toModel() = HabitAnchor(
    id = id,
    title = title,
    description = description,
    anchor = anchor,
    isCompleted = isCompleted,
    streakDays = streakDays,
    graceDaysUsed = graceDaysUsed,
    reframingTip = reframingTip
)

fun HabitAnchor.toEntity() = HabitEntity(
    id = id,
    title = title,
    description = description,
    anchor = anchor,
    isCompleted = isCompleted,
    streakDays = streakDays,
    graceDaysUsed = graceDaysUsed,
    reframingTip = reframingTip
)

fun BiometricEntity.toModel() = BiometricBaseline(
    readinessScore = readinessScore,
    perceivedEnergy = perceivedEnergy,
    sleepHours = sleepHours,
    sleepQuality = sleepQuality,
    chronotype = chronotype,
    recoveryModeTriggered = recoveryModeTriggered,
    graceDayActive = graceDayActive
)

fun BiometricBaseline.toEntity(date: String = com.example.data.util.AetherDateUtils.getTodayIso()) = BiometricEntity(
    date = date,
    readinessScore = readinessScore,
    perceivedEnergy = perceivedEnergy,
    sleepHours = sleepHours,
    sleepQuality = sleepQuality,
    chronotype = chronotype,
    recoveryModeTriggered = recoveryModeTriggered,
    graceDayActive = graceDayActive
)
