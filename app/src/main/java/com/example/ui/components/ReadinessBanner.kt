package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.data.model.BiometricBaseline
import com.example.data.model.Chronotype
import com.example.data.model.SystemMode
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.StringsProvider
import com.example.ui.theme.*

@Composable
fun ReadinessBanner(
    biometric: BiometricBaseline,
    onReadinessChanged: (Int) -> Unit,
    onChronotypeChanged: (Chronotype) -> Unit,
    onToggleRecoveryMode: () -> Unit,
    onOrchestrateClick: () -> Unit,
    onOpenSmartCheckIn: () -> Unit = {},
    language: AppLanguage = AppLanguage.SPANISH,
    modifier: Modifier = Modifier
) {
    val strings = remember(language) { StringsProvider(language) }
    val isSpanish = language == AppLanguage.SPANISH

    val mode = biometric.systemMode
    val modeColor = when (mode) {
        SystemMode.RECOVERY -> AetherEmerald
        SystemMode.HIGH_PERFORMANCE -> AetherCyan
        SystemMode.BALANCED -> AetherElectricBlue
    }

    val modeTitle = when (mode) {
        SystemMode.RECOVERY -> if (isSpanish) "MODO RECUPERACIÓN" else "RECOVERY MODE"
        SystemMode.HIGH_PERFORMANCE -> if (isSpanish) "MODO ALTO RENDIMIENTO" else "HIGH PERFORMANCE"
        SystemMode.BALANCED -> if (isSpanish) "FLUJO EQUILIBRADO" else "BALANCED FLOW"
    }

    val modeDesc = when (mode) {
        SystemMode.RECOVERY -> if (isSpanish) "Carga reducida • Enfoque en restauración y amortiguadores" else "Low load • Focus on biological recovery and rest"
        SystemMode.HIGH_PERFORMANCE -> if (isSpanish) "Capacidad pico • Ideal para tareas cognitivas intensas (Tipo A)" else "Peak capacity • Ideal for high-demand cognitive tasks"
        SystemMode.BALANCED -> if (isSpanish) "Ritmo circadiano estable • 1 Tarea Frog + ritmo sostenido" else "Steady baseline • 1 Frog task + sustained execution"
    }

    var sliderValue by remember(biometric.readinessScore) {
        mutableFloatStateOf(biometric.readinessScore.toFloat())
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("readiness_banner"),
        colors = CardDefaults.cardColors(containerColor = AetherSurfaceElevated),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Mode Header Row & Action Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(modeColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = modeTitle,
                        style = MaterialTheme.typography.labelMedium,
                        color = modeColor,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Smart Check-in Button
                    FilledTonalButton(
                        onClick = onOpenSmartCheckIn,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = AetherCyan.copy(alpha = 0.15f),
                            contentColor = AetherCyan
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("smart_checkin_trigger_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = strings.btnSmartCheckIn,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Recovery Mode Quick Switch
                    FilterChip(
                        selected = mode == SystemMode.RECOVERY,
                        onClick = onToggleRecoveryMode,
                        label = {
                            Text(
                                text = if (mode == SystemMode.RECOVERY) strings.recoveryModeChipActive else strings.recoveryModeChipInactive,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = if (mode == SystemMode.RECOVERY) Icons.Default.Spa else Icons.Default.Bedtime,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = if (mode == SystemMode.RECOVERY) AetherEmerald else AetherTextSecondary
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AetherEmerald.copy(alpha = 0.2f),
                            selectedLabelColor = AetherEmerald
                        ),
                        modifier = Modifier.testTag("recovery_mode_toggle")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Score & Quick Metrics Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = strings.readinessTitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = AetherTextMuted,
                        fontSize = 10.sp
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "${sliderValue.toInt()}",
                            style = MaterialTheme.typography.displaySmall,
                            color = modeColor,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = " / 100",
                            style = MaterialTheme.typography.titleSmall,
                            color = AetherTextMuted,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Surface(
                        color = AetherSurfaceCard,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🎯 Techo Dinámico: ${(biometric.dynamicCognitiveCeilingMinutes / 60.0)}h",
                                style = MaterialTheme.typography.labelSmall,
                                color = AetherTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "🛌 ${String.format("%.1f", biometric.sleepHours)}h (⭐${biometric.sleepQuality}) • ⚡ Estrés: ${biometric.stressLevel}/10",
                        style = MaterialTheme.typography.labelSmall,
                        color = AetherTextSecondary
                    )
                }
            }

            Slider(
                value = sliderValue,
                onValueChange = { sliderValue = it },
                onValueChangeFinished = { onReadinessChanged(sliderValue.toInt()) },
                valueRange = 20f..100f,
                steps = 15,
                colors = SliderDefaults.colors(
                    thumbColor = modeColor,
                    activeTrackColor = modeColor,
                    inactiveTrackColor = AetherSurfaceCard
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("readiness_slider")
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Chronotype Selector Row
            Text(
                text = strings.chronotypeSelectorTitle,
                style = MaterialTheme.typography.labelSmall,
                color = AetherTextMuted,
                fontSize = 10.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Chronotype.entries.forEach { chrono ->
                    val isSelected = biometric.chronotype == chrono
                    val localizedChronoName = when (chrono) {
                        Chronotype.LION -> if (isSpanish) "León" else "Lion"
                        Chronotype.BEAR -> if (isSpanish) "Oso" else "Bear"
                        Chronotype.WOLF -> if (isSpanish) "Lobo" else "Wolf"
                        Chronotype.DOLPHIN -> if (isSpanish) "Delfín" else "Dolphin"
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) AetherCyan.copy(alpha = 0.2f) else AetherSurfaceCard)
                            .border(
                                1.dp,
                                if (isSelected) AetherCyan else Color.Transparent,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { onChronotypeChanged(chrono) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = localizedChronoName,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) AetherCyan else AetherTextSecondary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}
