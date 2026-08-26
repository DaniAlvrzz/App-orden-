package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BiometricBaseline
import com.example.data.model.HabitAnchor
import com.example.ui.i18n.AppLanguage
import com.example.ui.theme.*

@Composable
fun BioHistoryAnalyticsCard(
    recentBiometrics: List<BiometricBaseline>,
    habits: List<HabitAnchor>,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val isSpanish = language == AppLanguage.SPANISH
    var selectedRangeDays by remember { mutableStateOf(7) }

    val displayData = remember(recentBiometrics, selectedRangeDays) {
        if (recentBiometrics.isEmpty()) {
            // Generate standard historical curve if pristine
            (0 until selectedRangeDays).map { i ->
                BiometricBaseline(
                    readinessScore = (70 + (i * 3) % 25).coerceIn(40, 95),
                    perceivedEnergy = 75
                )
            }
        } else {
            // recentBiometrics comes sorted DESC (newest first).
            // Take the requested amount and reverse so the graph draws past-to-present (left-to-right).
            recentBiometrics.take(selectedRangeDays).reversed()
        }
    }

    val avgReadiness = remember(displayData) {
        if (displayData.isNotEmpty()) displayData.map { it.readinessScore }.average().toInt() else 75
    }

    val totalHabitStreaks = remember(habits) {
        habits.sumOf { it.streakDays }
    }

    val totalGraceUsed = remember(habits) {
        habits.sumOf { it.graceDaysUsed }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("bio_history_card"),
        colors = CardDefaults.cardColors(containerColor = AetherSurfaceElevated),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(AetherEmerald.copy(alpha = 0.3f))
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header & Range Switcher
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Insights,
                        contentDescription = null,
                        tint = AetherEmerald,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isSpanish) "HISTORIAL BIOENERGÉTICO" else "BIOENERGETIC HISTORY",
                        style = MaterialTheme.typography.labelSmall,
                        color = AetherEmerald,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.1.sp
                    )
                }

                // Range Selector (7d vs 30d)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    FilterChip(
                        selected = selectedRangeDays == 7,
                        onClick = { selectedRangeDays = 7 },
                        label = { Text("7D", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AetherEmerald.copy(alpha = 0.25f),
                            selectedLabelColor = AetherEmerald
                        ),
                        modifier = Modifier.testTag("filter_range_7d")
                    )
                    FilterChip(
                        selected = selectedRangeDays == 30,
                        onClick = { selectedRangeDays = 30 },
                        label = { Text("30D", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AetherEmerald.copy(alpha = 0.25f),
                            selectedLabelColor = AetherEmerald
                        ),
                        modifier = Modifier.testTag("filter_range_30d")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Metrics Summary Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$avgReadiness",
                        style = MaterialTheme.typography.titleLarge,
                        color = if (avgReadiness >= 70) AetherEmerald else if (avgReadiness >= 50) AetherAmber else AetherCoral,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = if (isSpanish) "Media Readiness" else "Avg Readiness",
                        style = MaterialTheme.typography.labelSmall,
                        color = AetherTextSecondary,
                        fontSize = 10.sp
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "🔥 $totalHabitStreaks d",
                        style = MaterialTheme.typography.titleLarge,
                        color = AetherCyan,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = if (isSpanish) "Racha Hábitos" else "Habits Streak",
                        style = MaterialTheme.typography.labelSmall,
                        color = AetherTextSecondary,
                        fontSize = 10.sp
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "🛡️ $totalGraceUsed",
                        style = MaterialTheme.typography.titleLarge,
                        color = AetherAmber,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = if (isSpanish) "Días de Gracia" else "Grace Days",
                        style = MaterialTheme.typography.labelSmall,
                        color = AetherTextSecondary,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Trend Graph Canvas
            Text(
                text = if (isSpanish) "Tendencia de Readiness Score" else "Readiness Score Trend",
                style = MaterialTheme.typography.labelSmall,
                color = AetherTextMuted,
                fontSize = 10.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(Color(0xFF070D18), RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                val width = size.width
                val height = size.height
                val dataPoints = displayData.map { it.readinessScore }

                if (dataPoints.isNotEmpty()) {
                    val stepX = width / (dataPoints.size - 1).coerceAtLeast(1)
                    val path = Path()

                    dataPoints.forEachIndexed { index, score ->
                        val x = index * stepX
                        val normalizedScore = (score.coerceIn(0, 100) / 100f)
                        val y = height - (normalizedScore * height)

                        if (index == 0) {
                            path.moveTo(x, y)
                        } else {
                            path.lineTo(x, y)
                        }

                        // Draw point dot
                        drawCircle(
                            color = if (score >= 70) AetherEmerald else if (score >= 50) AetherAmber else AetherCoral,
                            radius = 4.dp.toPx(),
                            center = Offset(x, y)
                        )
                    }

                    // Draw line
                    drawPath(
                        path = path,
                        color = AetherEmerald,
                        style = Stroke(width = 2.5.dp.toPx())
                    )
                }
            }
        }
    }
}
