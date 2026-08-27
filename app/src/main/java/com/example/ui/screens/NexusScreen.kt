package com.example.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BiometricBaseline
import com.example.data.model.Chronotype
import com.example.data.model.SystemMode
import com.example.data.model.TaskItem
import com.example.ui.components.*
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.StringsProvider
import com.example.ui.theme.*
import com.example.ui.viewmodel.AetherUiState

@Composable
fun NexusScreen(
    state: AetherUiState,
    onReadinessChanged: (Int) -> Unit,
    onChronotypeChanged: (Chronotype) -> Unit,
    onToggleRecoveryMode: () -> Unit,
    onToggleTask: (TaskItem) -> Unit,
    onStartFocus: (TaskItem) -> Unit,
    onAddTaskClick: () -> Unit,
    onEditTask: (TaskItem) -> Unit = {},
    onDeleteTask: (TaskItem) -> Unit = {},
    onMoveMediumTask: (fromIndex: Int, toIndex: Int) -> Unit = { _, _ -> },
    onMoveQuickTask: (fromIndex: Int, toIndex: Int) -> Unit = { _, _ -> },
    onOrchestrateClick: () -> Unit = {},
    onToggleTimeBlock: (com.example.data.model.TimeBlock) -> Unit = {},
    onAddTimeBlockClick: () -> Unit = {},
    onEditTimeBlock: (com.example.data.model.TimeBlock) -> Unit = {},
    onDeleteTimeBlock: (com.example.data.model.TimeBlock) -> Unit = {},
    onMoveTimeBlock: (fromIndex: Int, toIndex: Int) -> Unit = { _, _ -> },
    onOpenReframe: () -> Unit,
    onOpenHistory: () -> Unit = {},
    onOpenSettings: () -> Unit,
    onOpenTutorial: () -> Unit,
    onToggleLanguage: () -> Unit,
    onSaveBiometric: (BiometricBaseline) -> Unit = {},
    onConfirmRetroactiveHabit: (com.example.data.model.HabitAnchor) -> Unit = {},
    onConfirmRetroactiveTask: (TaskItem) -> Unit = {},
    onDismissMorningCheckIn: () -> Unit = {},
    onOpenBreathwork: () -> Unit = {},
    onDismissBreathwork: () -> Unit = {},
    onDismissCompassionMode: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val strings = remember(state.currentLanguage) { StringsProvider(state.currentLanguage) }
    var showSmartCheckInDialog by remember { mutableStateOf(false) }

    if (state.showMorningCheckInDialog) {
        com.example.ui.components.MorningCheckInDialog(
            unfinishedHabits = state.yesterdayUnfinishedHabits,
            unfinishedTasks = state.yesterdayUnfinishedTasks,
            language = state.currentLanguage,
            onConfirmHabit = onConfirmRetroactiveHabit,
            onConfirmTask = onConfirmRetroactiveTask,
            onDismiss = onDismissMorningCheckIn
        )
    }

    if (state.showBreathworkDialog) {
        com.example.ui.components.BreathworkDialog(
            language = state.currentLanguage,
            onDismiss = onDismissBreathwork
        )
    }

    if (showSmartCheckInDialog) {
        SmartCheckInDialog(
            currentBiometric = state.biometric,
            language = state.currentLanguage,
            onDismiss = { showSmartCheckInDialog = false },
            onSave = { updatedBaseline ->
                onSaveBiometric(updatedBaseline)
                showSmartCheckInDialog = false
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
    ) {
        // App Header & Top Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "AETHER",
                            style = MaterialTheme.typography.titleLarge,
                            color = AetherCyan,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = " OS",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Light,
                            letterSpacing = 2.sp
                        )
                    }
                    Text(
                        text = "${strings.engineSub} • ${com.example.data.util.AetherDateUtils.getFormattedToday(state.currentLanguage)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = AetherTextMuted
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Quick Language Switch Chip
                    Surface(
                        color = AetherSurfaceElevated,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .clickable { onToggleLanguage() }
                            .border(1.dp, AetherBorder, RoundedCornerShape(8.dp))
                            .testTag("lang_toggle_chip")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(text = state.currentLanguage.flag, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (state.currentLanguage == AppLanguage.SPANISH) "ES" else "EN",
                                style = MaterialTheme.typography.labelSmall,
                                color = AetherCyan,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Persistent History Button (Module 2)
                    IconButton(
                        onClick = onOpenHistory,
                        modifier = Modifier.testTag("nexus_history_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = strings.historyTitle,
                            tint = AetherCyan
                        )
                    }

                    // Interactive Tutorial Button
                    IconButton(
                        onClick = onOpenTutorial,
                        modifier = Modifier.testTag("tutorial_top_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = strings.tutorialSectionTitle,
                            tint = AetherAmber
                        )
                    }

                    // Settings Button
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier.testTag("settings_top_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = strings.settingsTitle,
                            tint = AetherTextSecondary
                        )
                    }
                }
            }
        }

        // Cognitive Reframing / Grace Day Banner if active
        if (state.biometric.systemMode == SystemMode.RECOVERY || state.biometric.graceDayActive) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = AetherEmerald.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Spa, contentDescription = null, tint = AetherEmerald)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = strings.recoveryBannerTitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = AetherEmerald,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = state.dailyPlan?.cognitive_reframing_message
                                    ?: strings.recoveryBannerSub,
                                style = MaterialTheme.typography.bodySmall,
                                color = AetherTextPrimary
                            )
                        }
                    }
                }
            }
        }

        // Compassion Mode Banner
        if (state.compassionModeState.isActive || state.biometric.readinessScore < 50) {
            item {
                com.example.ui.components.CompassionModeBanner(
                    language = state.currentLanguage,
                    onOpenBreathwork = onOpenBreathwork,
                    onDismissMode = onDismissCompassionMode
                )
            }
        }

        // 1. Biometric Readiness & Mode Banner
        item {
            ReadinessBanner(
                biometric = state.biometric,
                onReadinessChanged = onReadinessChanged,
                onChronotypeChanged = onChronotypeChanged,
                onToggleRecoveryMode = onToggleRecoveryMode,
                onOpenSmartCheckIn = { showSmartCheckInDialog = true },
                language = state.currentLanguage
            )
        }

        // 2. Circadian Energy Curve Canvas
        item {
            CircadianEnergyCanvas(
                readinessScore = state.biometric.readinessScore,
                chronotype = state.biometric.chronotype,
                systemMode = state.biometric.systemMode
            )
        }

        // 3. Cognitive Ceiling Law Gauge (Calculated from active high-demand tasks)
        item {
            CognitiveCeilingGauge(
                allocatedMinutes = state.deepWorkMinutesAllocated,
                maxCeilingMinutes = state.maxCognitiveCeilingMinutes,
                language = state.currentLanguage
            )
        }

        // 4. 1-3-5 Priorities Matrix (1 Frog, 3 Medium Demand, 5 Quick Wins / Micro-victories)
        item {
            PriorityMatrix135(
                systemMode = state.biometric.systemMode,
                frogTask = state.frogTask,
                mediumTasks = state.mediumTasks,
                quickWins = state.quickTasks,
                onToggleTask = onToggleTask,
                onStartFocus = onStartFocus,
                onAddTaskClick = onAddTaskClick,
                onEditTask = onEditTask,
                onDeleteTask = onDeleteTask,
                onMoveMediumTask = onMoveMediumTask,
                onMoveQuickTask = onMoveQuickTask,
                language = state.currentLanguage
            )
        }
    }
}
