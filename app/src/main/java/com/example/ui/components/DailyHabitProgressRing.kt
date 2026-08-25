package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

/**
 * 4.3 Anillo de progreso circular diario en el header de Hábitos con % en el centro, animado al cambiar.
 */
@Composable
fun DailyHabitProgressRing(
    completedCount: Int,
    totalCount: Int,
    modifier: Modifier = Modifier,
    size: Dp = 82.dp,
    strokeWidth: Dp = 7.dp
) {
    val targetRatio = if (totalCount > 0) (completedCount.toFloat() / totalCount.toFloat()).coerceIn(0f, 1f) else 0f

    val animatedRatio by animateFloatAsState(
        targetValue = targetRatio,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "daily_habit_progress_ratio"
    )

    val percent = (animatedRatio * 100).toInt()

    val progressGradient = when {
        animatedRatio >= 1.0f -> listOf(AetherEmerald, Color(0xFF00E5FF), AetherAmber)
        animatedRatio >= 0.5f -> listOf(AetherCyan, AetherEmerald)
        animatedRatio > 0f -> listOf(AetherAmber, AetherCyan)
        else -> listOf(AetherBorderLight, AetherBorder)
    }

    Box(
        modifier = modifier
            .size(size)
            .testTag("daily_habit_progress_ring"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(strokeWidth / 2)) {
            val strokePx = strokeWidth.toPx()
            val arcSize = Size(this.size.width, this.size.height)

            // Background Track
            drawArc(
                color = AetherBorder.copy(alpha = 0.5f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Animated Foreground Arc
            if (animatedRatio > 0.001f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = progressGradient,
                        center = Offset(this.size.width / 2, this.size.height / 2)
                    ),
                    startAngle = -90f,
                    sweepAngle = 360f * animatedRatio,
                    useCenter = false,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }
        }

        // Center Percentage
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${percent}%",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = if (animatedRatio >= 1f) AetherEmerald else AetherTextPrimary,
                fontSize = 17.sp,
                letterSpacing = (-0.5).sp
            )
            Text(
                text = "$completedCount/$totalCount",
                style = MaterialTheme.typography.labelSmall,
                color = AetherTextSecondary,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
