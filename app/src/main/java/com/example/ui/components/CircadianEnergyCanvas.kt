package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Chronotype
import com.example.data.model.SystemMode
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.StringsProvider
import com.example.ui.theme.*

@Composable
fun CircadianEnergyCanvas(
    readinessScore: Int,
    chronotype: Chronotype,
    systemMode: SystemMode,
    language: AppLanguage = AppLanguage.SPANISH,
    modifier: Modifier = Modifier
) {
    val strings = remember(language) { StringsProvider(language) }
    val isSpanish = language == AppLanguage.SPANISH

    val curveColor = when (systemMode) {
        SystemMode.RECOVERY -> AetherEmerald
        SystemMode.HIGH_PERFORMANCE -> AetherCyan
        SystemMode.BALANCED -> AetherElectricBlue
    }

    val chronoName = when (chronotype) {
        Chronotype.LION -> if (isSpanish) "Cronotipo León (Madrugador)" else "Lion Chronotype (Early Riser)"
        Chronotype.BEAR -> if (isSpanish) "Cronotipo Oso (Seguidor Solar)" else "Bear Chronotype (Solar Rhythm)"
        Chronotype.WOLF -> if (isSpanish) "Cronotipo Lobo (Pico Nocturno)" else "Wolf Chronotype (Late Peak)"
        Chronotype.DOLPHIN -> if (isSpanish) "Cronotipo Delfín (Sueño Ligero)" else "Dolphin Chronotype (Light Sleeper)"
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(AetherSurfaceCard, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = strings.circadianCurveTitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = AetherCyan,
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = "$chronoName • ${strings.peakHoursPrefix}: ${chronotype.peakHours}",
                    style = MaterialTheme.typography.bodySmall,
                    color = AetherTextSecondary
                )
            }
            Box(
                modifier = Modifier
                    .background(curveColor.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "$readinessScore% ${strings.bioReadyLabel}",
                    style = MaterialTheme.typography.labelMedium,
                    color = curveColor
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
        ) {
            val width = size.width
            val height = size.height
            val pointsCount = 9

            // Multipliers based on chronotype
            val energyLevels = when (chronotype) {
                Chronotype.LION -> listOf(65f, 95f, 85f, 60f, 45f, 55f, 40f, 30f, 15f)
                Chronotype.BEAR -> listOf(40f, 75f, 95f, 70f, 50f, 65f, 55f, 35f, 20f)
                Chronotype.WOLF -> listOf(25f, 45f, 65f, 60f, 50f, 90f, 85f, 60f, 35f)
                Chronotype.DOLPHIN -> listOf(45f, 60f, 70f, 55f, 50f, 80f, 65f, 40f, 20f)
            }.map { it * (readinessScore / 100f) }

            val stepX = width / (pointsCount - 1)
            val path = Path()
            val fillPath = Path()

            val coordinates = energyLevels.mapIndexed { index, level ->
                val x = index * stepX
                val normalizedY = height - (level / 100f * (height - 24f)) - 12f
                Offset(x, normalizedY)
            }

            path.moveTo(coordinates[0].x, coordinates[0].y)
            fillPath.moveTo(coordinates[0].x, height)
            fillPath.lineTo(coordinates[0].x, coordinates[0].y)

            for (i in 0 until coordinates.size - 1) {
                val current = coordinates[i]
                val next = coordinates[i + 1]
                val controlX1 = current.x + (next.x - current.x) / 2f
                val controlY1 = current.y
                val controlX2 = current.x + (next.x - current.x) / 2f
                val controlY2 = next.y
                path.cubicTo(controlX1, controlY1, controlX2, controlY2, next.x, next.y)
                fillPath.cubicTo(controlX1, controlY1, controlX2, controlY2, next.x, next.y)
            }

            fillPath.lineTo(width, height)
            fillPath.close()

            // Draw Background Gradient Fill
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        curveColor.copy(alpha = 0.35f),
                        curveColor.copy(alpha = 0.02f)
                    ),
                    startY = 0f,
                    endY = height
                )
            )

            // Draw Energy Wave Line
            drawPath(
                path = path,
                color = curveColor,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // Draw Data Points & Peak Indicators
            coordinates.forEachIndexed { index, point ->
                val isPeak = energyLevels[index] == energyLevels.maxOrNull()
                if (isPeak) {
                    drawCircle(
                        color = AetherAmber,
                        radius = 6.dp.toPx(),
                        center = point
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 3.dp.toPx(),
                        center = point
                    )
                } else {
                    drawCircle(
                        color = curveColor,
                        radius = 3.dp.toPx(),
                        center = point
                    )
                }
            }

            // Draw Guideline at 14h (Post-prandial dip)
            val dipX = 4 * stepX
            drawLine(
                color = AetherBorderLight.copy(alpha = 0.5f),
                start = Offset(dipX, 0f),
                end = Offset(dipX, height),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Time Markers Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val dipText = if (isSpanish) "14:00 (Bajón)" else "14:00 (Dip)"
            val hours = listOf("06:00", "09:00", "12:00", dipText, "17:00", "21:00")
            hours.forEach { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = if (label.contains("Dip") || label.contains("Bajón")) AetherAmber else AetherTextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Circadian Action Windows Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Surface(
                color = AetherSurfaceElevated,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "🔥 ${chronotype.peakHours}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = AetherAmber, fontSize = 10.sp)
                    Text(text = if (isSpanish) "Foco Profundo" else "Deep Focus", style = MaterialTheme.typography.labelSmall, color = AetherTextSecondary, fontSize = 9.sp)
                }
            }

            Surface(
                color = AetherSurfaceElevated,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "🥗 13:00 - 15:00", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = AetherCyan, fontSize = 10.sp)
                    Text(text = if (isSpanish) "Recarga & Digestión" else "Fuel & Digest", style = MaterialTheme.typography.labelSmall, color = AetherTextSecondary, fontSize = 9.sp)
                }
            }

            Surface(
                color = AetherSurfaceElevated,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "🌙 21:30+", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = AetherEmerald, fontSize = 10.sp)
                    Text(text = if (isSpanish) "Viento Abajo" else "Wind-down", style = MaterialTheme.typography.labelSmall, color = AetherTextSecondary, fontSize = 9.sp)
                }
            }
        }
    }
}
