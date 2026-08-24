package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CircadianAnchor
import com.example.data.model.HabitAnchor
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.StringsProvider
import com.example.ui.theme.*
import com.example.ui.viewmodel.AetherUiState

@Composable
fun HabitsScreen(
    state: AetherUiState,
    onToggleHabit: (HabitAnchor) -> Unit,
    onApplyGraceDay: (HabitAnchor) -> Unit,
    onOpenReframe: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = remember(state.currentLanguage) { StringsProvider(state.currentLanguage) }

    val completedCount = state.habits.count { it.isCompleted }
    val totalCount = state.habits.size
    val totalGraceDays = state.habits.sumOf { it.graceDaysUsed }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = strings.habitsHeader,
                        style = MaterialTheme.typography.titleLarge,
                        color = AetherCyan,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = strings.habitsSub,
                        style = MaterialTheme.typography.labelSmall,
                        color = AetherTextMuted
                    )
                }

                FilledTonalButton(
                    onClick = onOpenReframe,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = AetherEmerald.copy(alpha = 0.15f),
                        contentColor = AetherEmerald
                    ),
                    modifier = Modifier.testTag("open_reframe_btn")
                ) {
                    Icon(imageVector = Icons.Default.Spa, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(strings.btnReframe)
                }
            }
        }

        // Grace & Consistency Overview Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = AetherSurfaceElevated),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$completedCount / $totalCount",
                            style = MaterialTheme.typography.headlineSmall,
                            color = AetherCyan,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = strings.anchorsCountLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = AetherTextSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(36.dp)
                            .background(AetherBorder)
                    ) { }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$totalGraceDays",
                            style = MaterialTheme.typography.headlineSmall,
                            color = AetherEmerald,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = strings.graceDaysActiveLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = AetherTextSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(36.dp)
                            .background(AetherBorder)
                    ) { }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "100%",
                            style = MaterialTheme.typography.headlineSmall,
                            color = AetherAmber,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = strings.guiltFreeMetricLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = AetherTextSecondary
                        )
                    }
                }
            }
        }

        // Philosophy Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = AetherSurfaceCard),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = AetherCyan)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = strings.graceDayLawTitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = AetherCyan,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = strings.graceDayLawDesc,
                            style = MaterialTheme.typography.bodySmall,
                            color = AetherTextSecondary
                        )
                    }
                }
            }
        }

        // Habit Cards
        items(state.habits, key = { it.id }) { habit ->
            HabitAnchorCard(
                habit = habit,
                language = state.currentLanguage,
                onToggle = { onToggleHabit(habit) },
                onApplyGrace = { onApplyGraceDay(habit) }
            )
        }
    }
}

@Composable
fun HabitAnchorCard(
    habit: HabitAnchor,
    language: AppLanguage,
    onToggle: () -> Unit,
    onApplyGrace: () -> Unit
) {
    val strings = remember(language) { StringsProvider(language) }
    val isSpanish = language == AppLanguage.SPANISH

    val windowLabel = when (habit.anchor) {
        CircadianAnchor.MORNING_LIGHT -> if (isSpanish) "AL DESPERTAR" else "MORNING (06:00-08:00)"
        CircadianAnchor.HYDRATION_ELECTROLYTES -> if (isSpanish) "DESPERTAR INMEDIATO" else "EARLY MORNING"
        CircadianAnchor.CAFFEINE_CUTOFF -> if (isSpanish) "LÍMITE 14:00" else "CUTOFF 14:00"
        CircadianAnchor.ZONE_2_MOVEMENT -> if (isSpanish) "TARDE CIRCADIANO" else "AFTERNOON (16:00-18:00)"
        CircadianAnchor.DIGITAL_SUNSET -> if (isSpanish) "NOCHE 22:00" else "NIGHT (22:00)"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("habit_card_${habit.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (habit.isCompleted) AetherSurface.copy(alpha = 0.6f) else AetherSurfaceCard
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.WbSunny,
                        contentDescription = null,
                        tint = AetherAmber,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = windowLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = AetherAmber,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🔥 ${habit.streakDays}${strings.streakDaysSuffix}",
                        style = MaterialTheme.typography.labelSmall,
                        color = AetherCyan,
                        fontWeight = FontWeight.Bold
                    )
                    if (habit.graceDaysUsed > 0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "🛡️ ${habit.graceDaysUsed} ${strings.graceTagSuffix}",
                            style = MaterialTheme.typography.labelSmall,
                            color = AetherEmerald,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = habit.isCompleted,
                    onCheckedChange = { onToggle() },
                    colors = CheckboxDefaults.colors(checkedColor = AetherEmerald, uncheckedColor = AetherBorderLight)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = habit.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (habit.isCompleted) AetherTextMuted else AetherTextPrimary,
                        textDecoration = if (habit.isCompleted) TextDecoration.LineThrough else null,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = habit.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = AetherTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = habit.reframingTip,
                    style = MaterialTheme.typography.labelSmall,
                    color = AetherTextMuted,
                    fontSize = 10.sp,
                    modifier = Modifier.weight(1f)
                )

                TextButton(
                    onClick = onApplyGrace,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = AetherEmerald, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(strings.btnApplyGrace, style = MaterialTheme.typography.labelSmall, color = AetherEmerald)
                }
            }
        }
    }
}
