package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*

/**
 * Checkbox interactivo con tick animado y efecto de pulso al completar.
 * Cumple con las guías de accesibilidad de Material Design (área de toque mínima de 48dp).
 */
@Composable
fun AnimatedDopamineCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 26.dp,
    contentDescription: String = if (checked) "Completado" else "No completado"
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
        targetValue = if (checked) 1f else 0.94f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "checkbox_scale"
    )

    val activeBorderColor = AetherEmerald
    val inactiveBorderColor = MaterialTheme.colorScheme.outlineVariant

    val bgBorderColor by animateColorAsState(
        targetValue = if (checked) activeBorderColor else inactiveBorderColor,
        animationSpec = tween(300),
        label = "checkbox_border_color"
    )

    // Touch target wrapper of at least 48dp x 48dp for accessibility standards
    Box(
        modifier = modifier
            .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false, radius = 24.dp),
                role = Role.Checkbox,
                onClick = { onCheckedChange(!checked) }
            )
            .semantics(mergeDescendants = true) {
                // Merges child visual tree and semantics
            }
            .testTag("animated_dopamine_checkbox"),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .scale(scaleProgress)
                .clip(RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = this.size.width
                val h = this.size.height
                val cornerRadius = CornerRadius(w * 0.28f, h * 0.28f)

                // Background gradient fill when checked
                if (checkProgress > 0.05f) {
                    drawRoundRect(
                        brush = Brush.linearGradient(
                            listOf(AetherEmerald, Color(0xFF00E5FF))
                        ),
                        size = this.size,
                        cornerRadius = cornerRadius,
                        alpha = checkProgress
                    )
                }

                // Border
                drawRoundRect(
                    color = bgBorderColor,
                    size = this.size,
                    cornerRadius = cornerRadius,
                    style = Stroke(width = (w * 0.07f).coerceIn(1.5f.dp.toPx(), 3.dp.toPx()))
                )

                // Animated Checkmark Tick Path - perfectly scales with canvas width and height
                if (checkProgress > 0.1f) {
                    val tickPath = Path().apply {
                        val p1 = Offset(w * 0.22f, h * 0.52f)
                        val p2 = Offset(w * 0.44f, h * 0.73f)
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
                            width = (w * 0.11f).coerceIn(2.dp.toPx(), 4.5.dp.toPx()),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            }
        }
    }
}
