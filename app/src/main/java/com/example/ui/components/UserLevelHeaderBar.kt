package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserLevelInfo
import com.example.ui.i18n.AppLanguage
import com.example.ui.theme.*

/**
 * 4.4 Barra de nivel y XP en header con animación al subir.
 */
@Composable
fun UserLevelHeaderBar(
    levelInfo: UserLevelInfo,
    language: AppLanguage,
    onOpenAchievements: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isSpanish = language == AppLanguage.SPANISH

    val animatedProgress by animateFloatAsState(
        targetValue = levelInfo.progressToNextLevel,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "user_level_xp_progress"
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = AetherSurfaceElevated),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                listOf(AetherViolet.copy(alpha = 0.5f), AetherCyan.copy(alpha = 0.4f))
            ),
            width = 1.2.dp
        ),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onOpenAchievements() }
            .testTag("user_level_header_bar")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Level Badge
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(AetherViolet, AetherCyan)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "L${levelInfo.currentLevel}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = levelInfo.title(isSpanish),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = AetherTextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = AetherAmber,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                        Text(
                            text = if (isSpanish) "Nivel ${levelInfo.currentLevel} • ${levelInfo.totalXp} XP Total"
                            else "Level ${levelInfo.currentLevel} • ${levelInfo.totalXp} Total XP",
                            style = MaterialTheme.typography.labelSmall,
                            color = AetherTextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }

                // XP to Next Level
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${levelInfo.xpInCurrentLevel}/${levelInfo.xpRequiredForNextLevel} XP",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = AetherCyan,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = onOpenAchievements,
                        modifier = Modifier.size(24.dp).testTag("open_achievements_from_bar")
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "Achievements",
                            tint = AetherAmber,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Animated XP Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(AetherSurfaceCard)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress.coerceIn(0.02f, 1f))
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(
                            Brush.horizontalGradient(
                                listOf(AetherViolet, AetherCyan, AetherEmerald)
                            )
                        )
                )
            }
        }
    }
}
