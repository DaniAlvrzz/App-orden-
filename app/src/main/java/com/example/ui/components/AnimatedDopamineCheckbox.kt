package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*

/**
 * 4.1 Checkbox interactivo con tick animado y efecto de pulso al completar
 */
@Composable
fun AnimatedDopamineCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 26.dp
) {
    val checkProgress by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "tick_anim_progress"
    )

    val scaleProgress by animateFloatAsState(
        targetValue = if (checked) 1f else 0.92f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "checkbox_scale"
    )

    val bgBorderColor by animateColorAsState(
        targetValue = if (checked) AetherEmerald else AetherBorderLight,
        animationSpec = tween(300),
        label = "checkbox_border_color"
    )

    Box(
        modifier = modifier
            .size(size)
            .scale(scaleProgress)
            .clip(RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onCheckedChange(!checked) }
            )
            .testTag("animated_dopamine_checkbox"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height

            // Background box
            if (checkProgress > 0.05f) {
                drawRoundRect(
                    brush = Brush.linearGradient(
                        listOf(AetherEmerald, Color(0xFF00E5FF))
                    ),
                    size = this.size,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx(), 8.dp.toPx()),
                    alpha = checkProgress
                )
            }

            // Border
            drawRoundRect(
                color = bgBorderColor,
                size = this.size,
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx(), 8.dp.toPx()),
                style = Stroke(width = 1.5.dp.toPx())
            )

            // Animated Checkmark Tick Path
            if (checkProgress > 0.1f) {
                val tickPath = Path().apply {
                    // Checkmark points relative to size
                    val p1 = Offset(w * 0.24f, h * 0.52f)
                    val p2 = Offset(w * 0.44f, h * 0.72f)
                    val p3 = Offset(w * 0.78f, h * 0.30f)

                    moveTo(p1.x, p1.y)

                    if (checkProgress <= 0.5f) {
                        val subT = checkProgress / 0.5f
                        val currX = p1.x + (p2.x - p1.x) * subT
                        val currY = p1.y + (p2.y - p1.y) * subT
                        lineTo(currX, currY)
                    } else {
                        lineTo(p2.x, p2.y)
                        val subT = (checkProgress - 0.5f) / 0.5f
                        val currX = p2.x + (p3.x - p2.x) * subT
                        val currY = p2.y + (p3.y - p2.y) * subT
                        lineTo(currX, currY)
                    }
                }

                drawPath(
                    path = tickPath,
                    color = Color(0xFF003919),
                    style = Stroke(
                        width = 2.4.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = androidx.compose.ui.graphics.StrokeJoin.Round
                    )
                )
            }
        }
    }
}
