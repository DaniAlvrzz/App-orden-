package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.StringsProvider
import com.example.ui.theme.*

/**
 * 4.2 Racha visual:
 * - 0 días: Animación suave de "renacer" (brote verde / fénix de renovación, nunca castigo visual agresivo).
 * - 1-6 días: Llama pequeña esmeralda/ámbar sutil.
 * - 7-29 días: Llama mediana con pulso rítmico dinámico.
 * - 30+ días: Llama grande con brillo/glow y gradiente coral/dorado.
 */
@Composable
fun AnimatedStreakFlameBadge(
    streakDays: Int,
    language: AppLanguage = AppLanguage.SPANISH,
    modifier: Modifier = Modifier
) {
    val strings = remember(language) { StringsProvider(language) }
    val isSpanish = language == AppLanguage.SPANISH

    val infiniteTransition = rememberInfiniteTransition(label = "streak_flame_anim")

    when {
        // 0 días: Renacer suave (Soft Rebirth)
        streakDays == 0 -> {
            val rebirthPulse by infiniteTransition.animateFloat(
                initialValue = 0.92f,
                targetValue = 1.05f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1800, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "rebirth_pulse"
            )

            Surface(
                color = AetherEmerald.copy(alpha = 0.12f),
                shape = RoundedCornerShape(10.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = SolidColor(AetherEmerald.copy(alpha = 0.4f)),
                    width = 1.dp
                ),
                modifier = modifier.scale(rebirthPulse).testTag("streak_rebirth_badge")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Spa,
                        contentDescription = "Renacer",
                        tint = AetherEmerald,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isSpanish) "🌱 Renacer" else "🌱 Fresh Start",
                        style = MaterialTheme.typography.labelSmall,
                        color = AetherEmerald,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.5.sp
                    )
                }
            }
        }

        // 1-6 días: Llama pequeña
        streakDays in 1..6 -> {
            Surface(
                color = AetherEmerald.copy(alpha = 0.15f),
                shape = RoundedCornerShape(10.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = SolidColor(AetherEmerald.copy(alpha = 0.5f)),
                    width = 1.dp
                ),
                modifier = modifier.testTag("streak_badge_small")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "🔥", fontSize = 11.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$streakDays ${strings.streakDaysSuffix}",
                        style = MaterialTheme.typography.labelSmall,
                        color = AetherEmerald,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // 7-29 días: Llama mediana con pulso rítmico
        streakDays in 7..29 -> {
            val pulseScale by infiniteTransition.animateFloat(
                initialValue = 0.96f,
                targetValue = 1.08f,
                animationSpec = infiniteRepeatable(
                    animation = tween(900, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "flame_pulse_medium"
            )

            Surface(
                color = AetherAmber.copy(alpha = 0.18f),
                shape = RoundedCornerShape(10.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(
                        listOf(AetherAmber.copy(alpha = 0.8f), AetherCoral.copy(alpha = 0.5f))
                    ),
                    width = 1.2.dp
                ),
                modifier = modifier.testTag("streak_badge_medium")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.scale(pulseScale)) {
                        Text(text = "🔥", fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$streakDays ${strings.streakDaysSuffix}",
                        style = MaterialTheme.typography.labelSmall,
                        color = AetherAmber,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // 30+ días: Llama grande con brillo cósmico
        else -> {
            val glowAlpha by infiniteTransition.animateFloat(
                initialValue = 0.4f,
                targetValue = 0.9f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "glow_alpha"
            )
            val glowScale by infiniteTransition.animateFloat(
                initialValue = 1.0f,
                targetValue = 1.12f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "glow_scale"
            )

            Surface(
                color = AetherCoral.copy(alpha = 0.22f),
                shape = RoundedCornerShape(10.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(
                        listOf(AetherCoral, AetherAmber, AetherCyan)
                    ),
                    width = 1.5.dp
                ),
                modifier = modifier.testTag("streak_badge_large")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .scale(glowScale)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        AetherCoral.copy(alpha = glowAlpha),
                                        Color.Transparent
                                    )
                                )
                            )
                            .padding(1.dp)
                    ) {
                        Text(text = "⚡🔥", fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$streakDays ${strings.streakDaysSuffix}",
                        style = MaterialTheme.typography.labelSmall,
                        color = AetherCoral,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}
