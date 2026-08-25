package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class Particle(
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val color: Color,
    val size: Float,
    val rotationSpeed: Float,
    val isCircle: Boolean
)

@Composable
fun HabitConfettiCanvas(
    triggerKey: Any?,
    modifier: Modifier = Modifier
) {
    if (triggerKey == null) return

    val progress = remember(triggerKey) { Animatable(0f) }
    val particles = remember(triggerKey) {
        val colors = listOf(
            AetherCyan,
            AetherEmerald,
            AetherAmber,
            AetherCoral,
            AetherViolet,
            Color(0xFFFFEB3B),
            Color(0xFFE040FB),
            Color(0xFF00E5FF)
        )
        val rnd = Random(System.currentTimeMillis())
        List(55) {
            val angle = rnd.nextDouble(0.0, Math.PI * 2)
            val speed = rnd.nextFloat() * 450f + 200f
            Particle(
                x = 0.5f,
                y = 0.45f,
                vx = (cos(angle) * speed).toFloat(),
                vy = (sin(angle) * speed - 180f).toFloat(),
                color = colors[rnd.nextInt(colors.size)],
                size = rnd.nextFloat() * 9f + 6f,
                rotationSpeed = rnd.nextFloat() * 720f - 360f,
                isCircle = rnd.nextBoolean()
            )
        }
    }

    LaunchedEffect(triggerKey) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1500, easing = LinearEasing)
        )
    }

    if (progress.value < 1f && progress.value > 0f) {
        Canvas(modifier = modifier.fillMaxSize()) {
            val t = progress.value
            val alpha = (1f - t).coerceIn(0f, 1f)
            val w = size.width
            val h = size.height

            particles.forEach { p ->
                val px = (p.x * w) + (p.vx * t)
                val py = (p.y * h) + (p.vy * t) + (480f * t * t) // gravity
                val rot = p.rotationSpeed * t

                if (px in -50f..(w + 50f) && py in -50f..(h + 50f)) {
                    val pColor = p.color.copy(alpha = alpha)
                    rotate(rot, pivot = Offset(px, py)) {
                        if (p.isCircle) {
                            drawCircle(
                                color = pColor,
                                radius = p.size / 2f,
                                center = Offset(px, py)
                            )
                        } else {
                            drawRect(
                                color = pColor,
                                topLeft = Offset(px - p.size / 2f, py - p.size / 2f),
                                size = Size(p.size, p.size * 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}
