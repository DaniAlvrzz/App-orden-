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
import com.example.data.model.AiStatus
import com.example.data.model.BiometricBaseline
import com.example.data.model.Chronotype
import com.example.data.model.SystemMode
import com.example.data.model.TaskItem
import com.example.data.model.TimeBlock
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
    onOrchestrateClick: () -> Unit,
    onToggleTask: (TaskItem) -> Unit,
    onStartFocus: (TaskItem) -> Unit,
    onAddTaskClick: () -> Unit,
    onEditTask: (TaskItem) -> Unit = {},
    onDeleteTask: (TaskItem) -> Unit = {},
    onMoveMediumTask: (fromIndex: Int, toIndex: Int) -> Unit = { _, _ -> },
    onMoveQuickTask: (fromIndex: Int, toIndex: Int) -> Unit = { _, _ -> },
    onToggleTimeBlock: (TimeBlock) -> Unit,
    onAddTimeBlockClick: () -> Unit,
    onEditTimeBlock: (TimeBlock) -> Unit = {},
    onDeleteTimeBlock: (TimeBlock) -> Unit = {},
    onMoveTimeBlock: (fromIndex: Int, toIndex: Int) -> Unit = { _, _ -> },
    onOpenReframe: () -> Unit,
    onOpenHistory: () -> Unit = {},
    onOpenSettings: () -> Unit,
    onOpenTutorial: () -> Unit,
    onToggleLanguage: () -> Unit,
    onSaveBiometric: (BiometricBaseline) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val strings = remember(state.currentLanguage) { StringsProvider(state.currentLanguage) }
    val isSpanish = state.currentLanguage == AppLanguage.SPANISH
    var showSmartCheckInDialog by remember { mutableStateOf(false) }

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
                        text = "${strings.engineSub} • ${com.example.data.util.AetherDateUtils.getTodayIso()}",
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

        // AI Engine Source & Orchestration Status Banner with Trigger Button
        item {
            val statusColor = when (state.aiEngineStatus) {
                AiStatus.LIVE -> AetherEmerald
                AiStatus.FALLBACK -> AetherAmber
                AiStatus.ERROR -> Color(0xFFEF4444)
                AiStatus.IDLE -> AetherTextMuted
            }

            val statusBg = when (state.aiEngineStatus) {
                AiStatus.LIVE -> AetherEmerald.copy(alpha = 0.12f)
                AiStatus.FALLBACK -> AetherAmber.copy(alpha = 0.12f)
                AiStatus.ERROR -> Color(0xFFEF4444).copy(alpha = 0.12f)
                AiStatus.IDLE -> AetherSurfaceElevated
            }

            val statusIcon = when (state.aiEngineStatus) {
                AiStatus.LIVE -> Icons.Default.AutoAwesome
                AiStatus.FALLBACK -> Icons.Default.Bolt
                AiStatus.ERROR -> Icons.Default.Warning
                AiStatus.IDLE -> Icons.Default.Memory
            }

            val statusLabel = when (state.aiEngineStatus) {
                AiStatus.LIVE -> if (isSpanish) "Sintetizado en Vivo con Gemini AI" else "Live Gemini AI Synthesis"
                AiStatus.FALLBACK -> if (isSpanish) "Motor Circadiano de Respaldo Determinista" else "Deterministic Circadian Fallback"
                AiStatus.ERROR -> if (isSpanish) "Modo Respaldo (Error de Servicio IA)" else "Fallback Mode (AI Service Error)"
                AiStatus.IDLE -> if (isSpanish) "Motor Circadiano Listo" else "Circadian Engine Ready"
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = statusBg),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().testTag("ai_engine_status_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f, fill = false),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (state.isOrchestrating) {
                            CircularProgressIndicator(
                                color = AetherCyan,
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isSpanish) "Sintetizando cronobiología con Gemini AI..." else "Synthesizing chronobiology with Gemini AI...",
                                style = MaterialTheme.typography.labelSmall,
                                color = AetherCyan,
                                fontWeight = FontWeight.SemiBold
                            )
                        } else {
                            Icon(
                                imageVector = statusIcon,
                                contentDescription = null,
                                tint = statusColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = statusLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = statusColor,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // AI Quick Orchestrate Button
                    FilledTonalButton(
                        onClick = onOrchestrateClick,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = AetherCyan.copy(alpha = 0.2f),
                            contentColor = AetherCyan
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("orchestrate_btn")
                    ) {
                        if (state.isOrchestrating) {
                            CircularProgressIndicator(
                                color = AetherCyan,
                                modifier = Modifier.size(14.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (state.isOrchestrating) strings.btnAligning else strings.btnOrchestrate,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
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

        // 1. Biometric Readiness & Mode Banner
        item {
            ReadinessBanner(
                biometric = state.biometric,
                onReadinessChanged = onReadinessChanged,
                onChronotypeChanged = onChronotypeChanged,
                onToggleRecoveryMode = onToggleRecoveryMode,
                onOrchestrateClick = onOrchestrateClick,
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

        // 3. Cognitive Ceiling Law Gauge (Max 3.5h Deep Work)
        item {
            CognitiveCeilingGauge(
                allocatedMinutes = state.deepWorkMinutesAllocated,
                maxCeilingMinutes = state.maxCognitiveCeilingMinutes
            )
        }

        // 4. 1-3-5 Priorities Matrix (1 Frog, 3 Medium, 5 Quick)
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

        // 5. Circadian Time Blocks Timeline
        item {
            TimeBlockTimeline(
                blocks = state.timeBlocks,
                onToggleBlock = onToggleTimeBlock,
                onAddBlockClick = onAddTimeBlockClick,
                onEditBlock = onEditTimeBlock,
                onDeleteBlock = onDeleteTimeBlock,
                onMoveBlock = onMoveTimeBlock,
                language = state.currentLanguage
            )
        }
    }
}
