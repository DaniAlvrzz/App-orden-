package com.example.ui.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, AetherBorderLight)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 1. System Mode Status Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(modeColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = modeTitle,
                        style = MaterialTheme.typography.labelMedium,
                        color = modeColor,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    color = modeColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = when (mode) {
                            SystemMode.RECOVERY -> if (isSpanish) "🛡️ Protección" else "🛡️ Guard"
                            SystemMode.HIGH_PERFORMANCE -> if (isSpanish) "⚡ Máximo" else "⚡ Peak"
                            SystemMode.BALANCED -> if (isSpanish) "⚖️ Estable" else "⚖️ Steady"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = modeColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = modeDesc,
                style = MaterialTheme.typography.bodySmall,
                color = AetherTextSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Main Interactive Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Button 1: Smart Check-In Calibration
                Surface(
                    onClick = onOpenSmartCheckIn,
                    color = AetherCyan.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, AetherCyan.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("smart_checkin_trigger_btn")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = if (isSpanish) "Calibración" else "Calibration",
                            tint = AetherCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isSpanish) "Calibrar" else "Calibrate",
                            style = MaterialTheme.typography.labelMedium,
                            color = AetherCyan,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }

                // Button 2: Recovery Mode Switch
                val isRecovery = mode == SystemMode.RECOVERY
                Surface(
                    onClick = onToggleRecoveryMode,
                    color = if (isRecovery) AetherEmerald.copy(alpha = 0.22f) else AetherSurfaceCard,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(
                        1.dp,
                        if (isRecovery) AetherEmerald.copy(alpha = 0.6f) else AetherBorder
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("recovery_mode_toggle")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (isRecovery) Icons.Default.Spa else Icons.Default.Bedtime,
                            contentDescription = if (isRecovery) "Modo Recuperación" else "Modo Estándar",
                            tint = if (isRecovery) AetherEmerald else AetherTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isRecovery) {
                                if (isSpanish) "Recuperación" else "Recovery"
                            } else {
                                if (isSpanish) "Modo Suave" else "Gentle Mode"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isRecovery) AetherEmerald else AetherTextSecondary,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Score & Cognitive Ceiling Row
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
                        fontSize = 10.sp,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
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
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, AetherBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🎯 Techo: ${(biometric.dynamicCognitiveCeilingMinutes / 60.0)}h Foco",
                                style = MaterialTheme.typography.labelSmall,
                                color = AetherTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "🛌 ${String.format("%.1f", biometric.sleepHours)}h (⭐${biometric.sleepQuality}) • ⚡ Estrés: ${biometric.stressLevel}/10",
                        style = MaterialTheme.typography.labelSmall,
                        color = AetherTextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 4. Readiness Slider
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

            Spacer(modifier = Modifier.height(8.dp))

            // 5. Chronotype Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.chronotypeSelectorTitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = AetherTextMuted,
                    fontSize = 10.sp,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Pico: ${biometric.chronotype.peakHours}",
                    style = MaterialTheme.typography.labelSmall,
                    color = AetherCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Chronotype.entries.forEach { chrono ->
                    val isSelected = biometric.chronotype == chrono
                    val (emoji, localizedChronoName) = when (chrono) {
                        Chronotype.LION -> "🦁" to if (isSpanish) "León" else "Lion"
                        Chronotype.BEAR -> "🐻" to if (isSpanish) "Oso" else "Bear"
                        Chronotype.WOLF -> "🐺" to if (isSpanish) "Lobo" else "Wolf"
                        Chronotype.DOLPHIN -> "🐬" to if (isSpanish) "Delfín" else "Dolphin"
                    }

                    Surface(
                        onClick = { onChronotypeChanged(chrono) },
                        color = if (isSelected) AetherCyan.copy(alpha = 0.2f) else AetherSurfaceCard,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) AetherCyan else AetherBorder
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = emoji, fontSize = 14.sp)
                            Text(
                                text = localizedChronoName,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) AetherCyan else AetherTextSecondary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}
