package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.*
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.StringsProvider
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartCheckInDialog(
    currentBiometric: BiometricBaseline,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onSave: (BiometricBaseline) -> Unit
) {
    val strings = remember(language) { StringsProvider(language) }
    val isSpanish = language == AppLanguage.SPANISH

    // Local form state
    var sleepStartTime by remember { mutableStateOf(currentBiometric.sleepStartTime) }
    var sleepEndTime by remember { mutableStateOf(currentBiometric.sleepEndTime) }
    var sleepHours by remember { mutableStateOf(currentBiometric.sleepHours) }
    var sleepQuality by remember { mutableStateOf(currentBiometric.sleepQuality) }
    var sleepInterruptions by remember { mutableStateOf(currentBiometric.sleepInterruptionsCount) }
    var wakeUpFeeling by remember { mutableStateOf(currentBiometric.wakeUpFeeling) }
    
    var currentEnergy by remember { mutableStateOf(currentBiometric.currentEnergyLevel) }
    var stressLevel by remember { mutableStateOf(currentBiometric.stressLevel) }
    var motivationLevel by remember { mutableStateOf(currentBiometric.motivationLevel) }
    var mentalOverload by remember { mutableStateOf(currentBiometric.mentalOverload) }
    
    var caffeineIntake by remember { mutableStateOf(currentBiometric.caffeineIntake) }
    var exerciseDone by remember { mutableStateOf(currentBiometric.exerciseDone) }
    var mealRegularity by remember { mutableStateOf(currentBiometric.mealRegularity) }
    var emotionalConcern by remember { mutableStateOf(currentBiometric.emotionalConcern) }
    var chronotype by remember { mutableStateOf(currentBiometric.chronotype) }

    // Live objective calculations
    val calculatedScore by remember(
        sleepHours, sleepQuality, sleepInterruptions, wakeUpFeeling,
        currentEnergy, stressLevel, motivationLevel, mentalOverload,
        caffeineIntake, exerciseDone, mealRegularity, emotionalConcern
    ) {
        derivedStateOf {
            BiometricBaseline.calculateObjectiveReadiness(
                sleepHours = sleepHours,
                sleepQuality = sleepQuality,
                sleepInterruptions = sleepInterruptions,
                wakeFeeling = wakeUpFeeling,
                energyLevel1to10 = currentEnergy,
                stressLevel1to10 = stressLevel,
                motivation1to10 = motivationLevel,
                caffeine = caffeineIntake,
                exerciseDone = exerciseDone,
                mealRegularity = mealRegularity,
                mentalOverload = mentalOverload,
                hasEmotionalConcern = emotionalConcern.isNotBlank()
            )
        }
    }

    val ceilingData by remember(calculatedScore, stressLevel, mentalOverload, sleepHours) {
        derivedStateOf {
            BiometricBaseline.calculateCognitiveCeiling(
                readiness = calculatedScore,
                sleepHours = sleepHours,
                stressLevel = stressLevel,
                mentalOverload = mentalOverload
            )
        }
    }
    val dynamicCeilingMinutes = ceilingData.first
    val ceilingReason = ceilingData.second

    val systemMode = when {
        calculatedScore < 60 -> SystemMode.RECOVERY
        calculatedScore >= 78 -> SystemMode.HIGH_PERFORMANCE
        else -> SystemMode.BALANCED
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f)
                .testTag("smart_checkin_dialog"),
            colors = CardDefaults.cardColors(containerColor = AetherSurface),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, AetherBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(AetherCyan.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                tint = AetherCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = strings.smartCheckInTitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = if (isSpanish) "Calibración fisiológica y neurobiológica" else "Physiological & neurobiological calibration",
                                style = MaterialTheme.typography.labelSmall,
                                color = AetherTextMuted
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = strings.btnClose, tint = AetherTextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // LIVE PREVIEW SUMMARY BANNER
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = when (systemMode) {
                                SystemMode.HIGH_PERFORMANCE -> AetherCyan.copy(alpha = 0.12f)
                                SystemMode.RECOVERY -> AetherEmerald.copy(alpha = 0.12f)
                                else -> AetherAmber.copy(alpha = 0.12f)
                            }
                        ),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            when (systemMode) {
                                SystemMode.HIGH_PERFORMANCE -> AetherCyan.copy(alpha = 0.4f)
                                SystemMode.RECOVERY -> AetherEmerald.copy(alpha = 0.4f)
                                else -> AetherAmber.copy(alpha = 0.4f)
                            }
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = strings.calculatedScoreLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = AetherTextMuted,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Row(verticalAlignment = Alignment.Bottom) {
                                        Text(
                                            text = "$calculatedScore",
                                            style = MaterialTheme.typography.headlineMedium,
                                            fontWeight = FontWeight.Black,
                                            color = when {
                                                calculatedScore >= 75 -> AetherCyan
                                                calculatedScore >= 60 -> AetherAmber
                                                else -> AetherEmerald
                                            }
                                        )
                                        Text(
                                            text = " / 100",
                                            style = MaterialTheme.typography.titleSmall,
                                            color = AetherTextMuted,
                                            modifier = Modifier.padding(bottom = 4.dp)
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Surface(
                                        color = when (systemMode) {
                                            SystemMode.HIGH_PERFORMANCE -> AetherCyan.copy(alpha = 0.2f)
                                            SystemMode.RECOVERY -> AetherEmerald.copy(alpha = 0.2f)
                                            else -> AetherAmber.copy(alpha = 0.2f)
                                        },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = when (systemMode) {
                                                SystemMode.HIGH_PERFORMANCE -> "⚡ ${strings.modeHighPerf}"
                                                SystemMode.RECOVERY -> "🛡️ ${strings.modeRecovery}"
                                                else -> "⚖️ Flujo Balanceado"
                                            },
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "🎯 Techo: ${(dynamicCeilingMinutes / 60.0)}h (${dynamicCeilingMinutes} min)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = AetherTextSecondary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = ceilingReason,
                                style = MaterialTheme.typography.bodySmall,
                                color = AetherTextPrimary
                            )
                        }
                    }

                    // 1. SECTION: SLEEP ARCHITECTURE
                    Text(
                        text = strings.sleepSectionTitle,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = AetherCyan,
                        letterSpacing = 1.sp
                    )

                    // Sleep Hours Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = strings.sleepHoursLabel,
                                style = MaterialTheme.typography.bodySmall,
                                color = AetherTextSecondary
                            )
                            Text(
                                text = "${String.format("%.1f", sleepHours)} h",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Slider(
                            value = sleepHours.toFloat(),
                            onValueChange = { sleepHours = Math.round(it * 2) / 2.0 },
                            valueRange = 3f..12f,
                            steps = 17,
                            colors = SliderDefaults.colors(
                                thumbColor = AetherCyan,
                                activeTrackColor = AetherCyan,
                                inactiveTrackColor = AetherSurfaceElevated
                            ),
                            modifier = Modifier.testTag("sleep_hours_slider")
                        )
                    }

                    // Sleep Quality (Stars)
                    Column {
                        Text(
                            text = strings.sleepQualityLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = AetherTextSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            (1..5).forEach { star ->
                                IconButton(
                                    onClick = { sleepQuality = star },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = if (star <= sleepQuality) Icons.Default.Star else Icons.Default.StarBorder,
                                        contentDescription = "Star $star",
                                        tint = if (star <= sleepQuality) AetherAmber else AetherTextMuted
                                    )
                                }
                            }
                            Text(
                                text = when (sleepQuality) {
                                    1 -> "Pésimo"
                                    2 -> "Inquieto"
                                    3 -> "Aceptable"
                                    4 -> "Profundo"
                                    5 -> "Restaurador Total"
                                    else -> ""
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = AetherAmber,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Wake-up Feeling Selection
                    Column {
                        Text(
                            text = strings.wakeFeelingLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = AetherTextSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            WakeUpFeeling.values().forEach { feeling ->
                                val selected = wakeUpFeeling == feeling
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { wakeUpFeeling = feeling }
                                        .border(
                                            1.dp,
                                            if (selected) AetherCyan else AetherBorder,
                                            RoundedCornerShape(8.dp)
                                        ),
                                    color = if (selected) AetherCyan.copy(alpha = 0.15f) else AetherSurfaceElevated,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = when (feeling) {
                                                WakeUpFeeling.RESTED -> "⚡"
                                                WakeUpFeeling.NORMAL -> "🙂"
                                                WakeUpFeeling.GROGGY -> "🥱"
                                                WakeUpFeeling.EXHAUSTED -> "😫"
                                            },
                                            fontSize = 18.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = when (feeling) {
                                                WakeUpFeeling.RESTED -> if (isSpanish) "Descansado" else "Rested"
                                                WakeUpFeeling.NORMAL -> if (isSpanish) "Normal" else "Normal"
                                                WakeUpFeeling.GROGGY -> if (isSpanish) "Pesado" else "Groggy"
                                                WakeUpFeeling.EXHAUSTED -> if (isSpanish) "Agotado" else "Exhausted"
                                            },
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (selected) AetherCyan else AetherTextPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Sleep Interruptions Stepper
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = strings.sleepInterruptionsLabel,
                                style = MaterialTheme.typography.bodySmall,
                                color = AetherTextSecondary
                            )
                            Text(
                                text = if (sleepInterruptions == 0) "Sueño continuo" else "$sleepInterruptions despertares",
                                style = MaterialTheme.typography.labelSmall,
                                color = AetherTextMuted
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            FilledTonalIconButton(
                                onClick = { if (sleepInterruptions > 0) sleepInterruptions-- },
                                colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = AetherSurfaceElevated)
                            ) {
                                Text("-", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Text(
                                text = "$sleepInterruptions",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 14.dp)
                            )
                            FilledTonalIconButton(
                                onClick = { if (sleepInterruptions < 10) sleepInterruptions++ },
                                colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = AetherSurfaceElevated)
                            ) {
                                Text("+", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }

                    HorizontalDivider(color = AetherBorder.copy(alpha = 0.5f))

                    // 2. SECTION: INTERNAL STATE & NEUROLOGY
                    Text(
                        text = strings.internalStateTitle,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = AetherAmber,
                        letterSpacing = 1.sp
                    )

                    // Current Energy (1-10)
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = strings.energyLevelLabel, style = MaterialTheme.typography.bodySmall, color = AetherTextSecondary)
                            Text(text = "$currentEnergy / 10", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = AetherCyan)
                        }
                        Slider(
                            value = currentEnergy.toFloat(),
                            onValueChange = { currentEnergy = it.toInt() },
                            valueRange = 1f..10f,
                            steps = 8,
                            colors = SliderDefaults.colors(thumbColor = AetherCyan, activeTrackColor = AetherCyan)
                        )
                    }

                    // Stress Level (1-10)
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = strings.stressLevelLabel, style = MaterialTheme.typography.bodySmall, color = AetherTextSecondary)
                            Text(
                                text = "$stressLevel / 10 (${if (stressLevel <= 3) "Bajo" else if (stressLevel <= 6) "Moderado" else "Elevado"})",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = if (stressLevel <= 3) AetherEmerald else if (stressLevel <= 6) AetherAmber else Color(0xFFEF4444)
                            )
                        }
                        Slider(
                            value = stressLevel.toFloat(),
                            onValueChange = { stressLevel = it.toInt() },
                            valueRange = 1f..10f,
                            steps = 8,
                            colors = SliderDefaults.colors(
                                thumbColor = if (stressLevel <= 3) AetherEmerald else if (stressLevel <= 6) AetherAmber else Color(0xFFEF4444),
                                activeTrackColor = if (stressLevel <= 3) AetherEmerald else if (stressLevel <= 6) AetherAmber else Color(0xFFEF4444)
                            )
                        )
                    }

                    // Motivation Level (1-10)
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = strings.motivationLevelLabel, style = MaterialTheme.typography.bodySmall, color = AetherTextSecondary)
                            Text(text = "$motivationLevel / 10", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = AetherAmber)
                        }
                        Slider(
                            value = motivationLevel.toFloat(),
                            onValueChange = { motivationLevel = it.toInt() },
                            valueRange = 1f..10f,
                            steps = 8,
                            colors = SliderDefaults.colors(thumbColor = AetherAmber, activeTrackColor = AetherAmber)
                        )
                    }

                    // Mental Overload Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = strings.mentalOverloadLabel, style = MaterialTheme.typography.bodyMedium, color = Color.White)
                            Text(
                                text = if (isSpanish) "Reduce automáticamente el techo cognitivo para evitar sobrecarga" else "Dynamically adjusts cognitive ceiling to prevent burnout",
                                style = MaterialTheme.typography.labelSmall,
                                color = AetherTextMuted
                            )
                        }
                        Switch(
                            checked = mentalOverload,
                            onCheckedChange = { mentalOverload = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFEF4444), checkedTrackColor = Color(0xFFEF4444).copy(alpha = 0.3f))
                        )
                    }

                    HorizontalDivider(color = AetherBorder.copy(alpha = 0.5f))

                    // 3. SECTION: MODULATORS & LIFESTYLE
                    Text(
                        text = strings.modulatorsTitle,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = AetherEmerald,
                        letterSpacing = 1.sp
                    )

                    // Caffeine Intake
                    Column {
                        Text(text = strings.caffeineLabel, style = MaterialTheme.typography.bodySmall, color = AetherTextSecondary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CaffeineIntake.values().forEach { intake ->
                                val selected = caffeineIntake == intake
                                FilterChip(
                                    selected = selected,
                                    onClick = { caffeineIntake = intake },
                                    label = {
                                        Text(
                                            when (intake) {
                                                CaffeineIntake.NONE -> if (isSpanish) "0 Cafeína" else "None"
                                                CaffeineIntake.MODERATE -> if (isSpanish) "Moderada (1-2)" else "Moderate (1-2)"
                                                CaffeineIntake.HIGH -> if (isSpanish) "Alta (3+)" else "High (3+)"
                                            }
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = AetherAmber.copy(alpha = 0.2f),
                                        selectedLabelColor = AetherAmber
                                    )
                                )
                            }
                        }
                    }

                    // Exercise toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = strings.exerciseLabel, style = MaterialTheme.typography.bodyMedium, color = Color.White)
                        Switch(
                            checked = exerciseDone,
                            onCheckedChange = { exerciseDone = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = AetherEmerald, checkedTrackColor = AetherEmerald.copy(alpha = 0.3f))
                        )
                    }

                    // Meal Regularity
                    Column {
                        Text(text = strings.mealRegularityLabel, style = MaterialTheme.typography.bodySmall, color = AetherTextSecondary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            MealRegularity.values().forEach { meal ->
                                val selected = mealRegularity == meal
                                FilterChip(
                                    selected = selected,
                                    onClick = { mealRegularity = meal },
                                    label = {
                                        Text(
                                            when (meal) {
                                                MealRegularity.REGULAR -> if (isSpanish) "Regular / Batch" else "Regular"
                                                MealRegularity.IRREGULAR -> if (isSpanish) "Irregular" else "Irregular"
                                                MealRegularity.FASTING -> if (isSpanish) "Ayuno" else "Fasting"
                                            }
                                        )
                                    }
                                )
                            }
                        }
                    }

                    // Emotional Concern Field
                    OutlinedTextField(
                        value = emotionalConcern,
                        onValueChange = { emotionalConcern = it },
                        label = { Text(strings.emotionalConcernLabel) },
                        placeholder = { Text(if (isSpanish) "Ej. Mucha carga acumulada, reunión difícil a las 16h..." else "E.g. accumulated fatigue, tough meeting at 4pm...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AetherCyan,
                            unfocusedBorderColor = AetherBorder
                        ),
                        maxLines = 2
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(strings.btnCancel, color = AetherTextSecondary)
                    }

                    Button(
                        onClick = {
                            val updated = currentBiometric.copy(
                                readinessScore = calculatedScore,
                                computedReadinessScore = calculatedScore,
                                perceivedEnergy = (currentEnergy * 10).coerceIn(10, 100),
                                sleepStartTime = sleepStartTime,
                                sleepEndTime = sleepEndTime,
                                sleepHours = sleepHours,
                                sleepQuality = sleepQuality,
                                sleepInterruptionsCount = sleepInterruptions,
                                wakeUpFeeling = wakeUpFeeling,
                                currentEnergyLevel = currentEnergy,
                                stressLevel = stressLevel,
                                motivationLevel = motivationLevel,
                                mentalOverload = mentalOverload,
                                caffeineIntake = caffeineIntake,
                                exerciseDone = exerciseDone,
                                mealRegularity = mealRegularity,
                                emotionalConcern = emotionalConcern,
                                chronotype = chronotype,
                                dynamicCognitiveCeilingMinutes = dynamicCeilingMinutes,
                                cognitiveCeilingReason = ceilingReason,
                                recoveryModeTriggered = calculatedScore < 60
                            )
                            onSave(updated)
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("save_smart_checkin_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = AetherCyan),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.btnApplyCheckIn, color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
