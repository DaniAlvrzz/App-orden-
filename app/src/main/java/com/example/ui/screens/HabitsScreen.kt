package com.example.ui.screens

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CircadianAnchor
import com.example.data.model.CompletionLog
import com.example.data.model.CompletionStatus
import com.example.data.model.DailySummary
import com.example.data.model.HabitAnchor
import com.example.data.util.AetherDateUtils
import com.example.ui.components.*
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.StringsProvider
import com.example.ui.theme.*
import com.example.ui.viewmodel.AetherUiState

@Composable
fun HabitsScreen(
    state: AetherUiState,
    onToggleHabit: (HabitAnchor) -> Unit,
    onMarkHabitNotDone: (HabitAnchor) -> Unit = {},
    onApplyGraceDay: (HabitAnchor) -> Unit,
    onEditHabit: (HabitAnchor) -> Unit = {},
    onDeleteHabit: (HabitAnchor) -> Unit = {},
    onOpenAddHabit: () -> Unit = {},
    onOpenReframe: () -> Unit,
    onOpenHistory: () -> Unit = {},
    onOpenHabitHistory: (HabitAnchor) -> Unit = {},
    onOpenAchievements: () -> Unit = {},
    onPreviousDay: () -> Unit = {},
    onNextDay: () -> Unit = {},
    onGoToToday: () -> Unit = {},
    onSelectDate: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val strings = remember(state.currentLanguage) { StringsProvider(state.currentLanguage) }
    val view = LocalView.current

    val todayIso = remember { AetherDateUtils.getTodayIso() }
    val isViewingToday = state.selectedDateIso == todayIso

    val completedCount = remember(state.habits, state.selectedDateLogs, isViewingToday) {
        if (isViewingToday) {
            state.habits.count { it.isCompleted }
        } else {
            state.habits.count { habit ->
                state.selectedDateLogs.any { it.itemId == habit.id && it.status == CompletionStatus.COMPLETED }
            }
        }
    }
    val totalCount = state.habits.size
    val totalGraceDays = state.habits.sumOf { it.graceDaysUsed }
    val consistencyPct = if (totalCount > 0) ((completedCount + totalGraceDays).coerceAtMost(totalCount) * 100 / totalCount) else 0
    var showOverflowMenu by remember { mutableStateOf(false) }
    var habitPendingDeletion by remember { mutableStateOf<com.example.data.model.HabitAnchor?>(null) }
    val isSpanish = state.currentLanguage == AppLanguage.SPANISH

    val groupedHabits = remember(state.habits) {
        CircadianAnchor.entries.associateWith { anchor ->
            state.habits.filter { it.anchor == anchor }
        }
    }
    var expandedAnchors by remember { mutableStateOf(emptySet<CircadianAnchor>()) }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
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
                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Text(
                            text = strings.habitsHeader,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.2.sp
                        )
                        Text(
                            text = strings.habitsSub,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilledTonalButton(
                            onClick = onOpenAddHabit,
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier
                                .height(38.dp)
                                .testTag("add_habit_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = if (state.currentLanguage == AppLanguage.SPANISH) "Añadir Hábito" else "Add Habit",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (state.currentLanguage == AppLanguage.SPANISH) "+ Hábito" else "+ Habit",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        FilledTonalButton(
                            onClick = onOpenReframe,
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = AetherEmerald.copy(alpha = 0.16f),
                                contentColor = AetherEmerald
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier
                                .height(38.dp)
                                .testTag("open_reframe_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Spa,
                                contentDescription = strings.btnReframe,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = strings.btnReframe,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box {
                            IconButton(
                                onClick = { showOverflowMenu = true },
                                modifier = Modifier
                                    .size(40.dp)
                                    .testTag("habits_overflow_menu_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Opciones de hábitos",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            DropdownMenu(
                                expanded = showOverflowMenu,
                                onDismissRequest = { showOverflowMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(strings.historyTitle) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.History,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    onClick = {
                                        showOverflowMenu = false
                                        onOpenHistory()
                                    },
                                    modifier = Modifier.testTag("habits_history_btn")
                                )
                                DropdownMenuItem(
                                    text = { Text(if (state.currentLanguage == AppLanguage.SPANISH) "Logros y Gamificación" else "Achievements & Badges") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.EmojiEvents,
                                            contentDescription = null,
                                            tint = AetherAmber
                                        )
                                    },
                                    onClick = {
                                        showOverflowMenu = false
                                        onOpenAchievements()
                                    },
                                    modifier = Modifier.testTag("habits_achievements_btn")
                                )
                            }
                        }
                    }
                }
            }

            // 4.4 XP & Level Header Bar
            item {
                UserLevelHeaderBar(
                    levelInfo = state.userLevelInfo,
                    language = state.currentLanguage,
                    onOpenAchievements = onOpenAchievements
                )
            }

            // Universal Date Navigation Bar (Navigate to any day to view & log habits/tasks)
            item {
                DateNavigationBar(
                    selectedDateIso = state.selectedDateIso,
                    language = state.currentLanguage,
                    onPreviousDay = onPreviousDay,
                    onNextDay = onNextDay,
                    onGoToToday = onGoToToday,
                    onSelectDate = onSelectDate
                )
            }

            // 4.3 Circular Progress Ring + Grace & Consistency Overview Card
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
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 4.3 Circular Progress Ring
                        DailyHabitProgressRing(
                            completedCount = completedCount,
                            totalCount = totalCount
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        // Stats Columns
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$completedCount/$totalCount",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = AetherCyan,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = strings.anchorsCountLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AetherTextSecondary,
                                    fontSize = 10.sp
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(30.dp)
                                    .background(AetherBorder)
                            )

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$totalGraceDays",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = AetherEmerald,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = strings.graceDaysActiveLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AetherTextSecondary,
                                    fontSize = 10.sp
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(30.dp)
                                    .background(AetherBorder)
                            )

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${consistencyPct}%",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = AetherAmber,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = strings.guiltFreeMetricLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AetherTextSecondary,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }

            // Bioenergetic History & Trends (7d / 30d Readiness & Habit Streaks)
            item {
                BioHistoryAnalyticsCard(
                    recentBiometrics = state.recentBiometrics,
                    habits = state.habits,
                    language = state.currentLanguage
                )
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

            // Habit Cards Grouped by Circadian Anchors
            if (state.habits.isEmpty()) {
                item {
                    EmptyStateCard(
                        icon = Icons.Default.Spa,
                        iconTint = AetherEmerald,
                        title = if (state.currentLanguage == AppLanguage.SPANISH) "Sin Hábitos Anclados" else "No Habit Anchors",
                        description = if (state.currentLanguage == AppLanguage.SPANISH) 
                            "Construye micro-hábitos sincronizados con tus anclas circadianas sin culpa ni fricción." 
                        else 
                            "Build micro-habits synchronized with your circadian anchors without guilt or friction.",
                        actionLabel = strings.addHabitTitle,
                        onAction = onOpenAddHabit,
                        testTag = "empty_habits_card"
                    )
                }
            } else {
                CircadianAnchor.entries.forEach { anchor ->
                    val habitsForAnchor = groupedHabits[anchor] ?: emptyList()
                    if (habitsForAnchor.isNotEmpty()) {
                        val (anchorTitle, anchorIcon, anchorColor) = getAnchorDetails(anchor, isSpanish)
                        val isExpanded = expandedAnchors.contains(anchor)
                        val completedInAnchor = habitsForAnchor.count { it.isCompleted }
                        val totalInAnchor = habitsForAnchor.size

                        item(key = "anchor_header_${anchor.name}") {
                            Spacer(modifier = Modifier.height(4.dp))
                            HabitAnchorSectionHeader(
                                title = anchorTitle,
                                completedCount = completedInAnchor,
                                totalCount = totalInAnchor,
                                icon = anchorIcon,
                                iconTint = anchorColor,
                                isExpanded = isExpanded,
                                onToggle = {
                                    expandedAnchors = if (isExpanded) {
                                        expandedAnchors - anchor
                                    } else {
                                        expandedAnchors + anchor
                                    }
                                },
                                testTag = "toggle_anchor_${anchor.name}"
                            )
                        }

                        if (isExpanded) {
                            items(habitsForAnchor, key = { it.id }) { habit ->
                                val requestDelete = {
                                    if (habit.streakDays > 0) {
                                        habitPendingDeletion = habit
                                    } else {
                                        onDeleteHabit(habit)
                                    }
                                }

                                val logForDay = state.selectedDateLogs.firstOrNull { it.itemId == habit.id }
                                val isCompletedForDate = if (isViewingToday) {
                                    habit.isCompleted || (logForDay?.status == CompletionStatus.COMPLETED)
                                } else {
                                    logForDay?.status == CompletionStatus.COMPLETED
                                }
                                val isMissedForDate = logForDay?.status == CompletionStatus.MISSED

                                AetherSwipeToDismissContainer(
                                    onDismiss = requestDelete,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    HabitAnchorCard(
                                        habit = habit,
                                        language = state.currentLanguage,
                                        recentSummaries = state.historySummaries,
                                        logs = state.selectedDateLogs,
                                        isCompletedForDate = isCompletedForDate,
                                        isMissedForDate = isMissedForDate,
                                        onToggle = {
                                            try {
                                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                            } catch (_: Exception) {}
                                            onToggleHabit(habit)
                                        },
                                        onMarkNotDone = {
                                            try {
                                                view.performHapticFeedback(HapticFeedbackConstants.REJECT)
                                            } catch (_: Exception) {}
                                            onMarkHabitNotDone(habit)
                                        },
                                        onApplyGrace = { onApplyGraceDay(habit) },
                                        onOpenHistory = { onOpenHabitHistory(habit) },
                                        onEdit = { onEditHabit(habit) },
                                        onDelete = requestDelete
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Habit Streak Deletion Confirmation Dialog
        habitPendingDeletion?.let { habitToDelete ->
            val isSpanish = state.currentLanguage == AppLanguage.SPANISH
            AlertDialog(
                onDismissRequest = { habitPendingDeletion = null },
                title = {
                    Text(
                        text = if (isSpanish) "🔥 ¿Eliminar hábito con racha activa?" else "🔥 Delete habit with active streak?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AetherCoral
                    )
                },
                text = {
                    Text(
                        text = if (isSpanish)
                            "El hábito \"${habitToDelete.title}\" tiene una racha activa de ${habitToDelete.streakDays} días. Si lo eliminas, perderás este progreso histórico."
                        else
                            "The habit \"${habitToDelete.title}\" currently has an active streak of ${habitToDelete.streakDays} days. Deleting it will permanently remove this streak history.",
                        style = MaterialTheme.typography.bodySmall,
                        color = AetherTextSecondary
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val h = habitPendingDeletion
                            habitPendingDeletion = null
                            if (h != null) onDeleteHabit(h)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AetherCoral, contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("confirm_delete_habit_btn")
                    ) {
                        Text(if (isSpanish) "Eliminar Hábito" else "Delete Habit", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { habitPendingDeletion = null },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(strings.btnCancel, color = AetherTextSecondary)
                    }
                },
                containerColor = AetherSurfaceElevated,
                shape = RoundedCornerShape(16.dp)
            )
        }

        // 4.1 Confetti Canvas Layer (1.5s particles)
        HabitConfettiCanvas(
            triggerKey = state.habitConfettiKey,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun StreakBadge(
    streakDays: Int,
    language: AppLanguage = AppLanguage.SPANISH,
    modifier: Modifier = Modifier
) {
    AnimatedStreakFlameBadge(
        streakDays = streakDays,
        language = language,
        modifier = modifier
    )
}

@Composable
fun GraceDayBadge(
    graceDaysUsed: Int,
    language: AppLanguage = AppLanguage.SPANISH,
    modifier: Modifier = Modifier
) {
    val strings = remember(language) { StringsProvider(language) }
    Surface(
        color = AetherEmerald.copy(alpha = 0.15f),
        shape = RoundedCornerShape(8.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(AetherEmerald.copy(alpha = 0.4f)),
            width = 1.dp
        ),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "🛡️", fontSize = 12.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "$graceDaysUsed ${strings.graceTagSuffix}",
                style = MaterialTheme.typography.labelSmall,
                color = AetherEmerald,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Personal best streak badge. Kept visually quieter than the active streak flame so it reads
 * as a record to aim back at rather than a competing "current" status.
 */
@Composable
fun BestStreakBadge(
    bestStreakDays: Int,
    language: AppLanguage = AppLanguage.SPANISH,
    modifier: Modifier = Modifier
) {
    val isSpanish = language == AppLanguage.SPANISH
    Surface(
        color = AetherAmber.copy(alpha = 0.12f),
        shape = RoundedCornerShape(8.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(AetherAmber.copy(alpha = 0.35f)),
            width = 1.dp
        ),
        modifier = modifier.testTag("best_streak_badge")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "🏆", fontSize = 12.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (isSpanish) "Récord $bestStreakDays" else "Best $bestStreakDays",
                style = MaterialTheme.typography.labelSmall,
                color = AetherAmber,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * 4.1 Habit Card with Spring Bounce Animation on completion toggle
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HabitAnchorCard(
    habit: HabitAnchor,
    language: AppLanguage,
    recentSummaries: List<DailySummary> = emptyList(),
    logs: List<CompletionLog> = emptyList(),
    isCompletedForDate: Boolean = habit.isCompleted,
    isMissedForDate: Boolean = false,
    onToggle: () -> Unit,
    onMarkNotDone: () -> Unit = {},
    onApplyGrace: () -> Unit,
    onOpenHistory: () -> Unit = {},
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    val strings = remember(language) { StringsProvider(language) }
    val isSpanish = language == AppLanguage.SPANISH

    // 4.1 Card spring bounce animation
    val cardScale by animateFloatAsState(
        targetValue = if (isCompletedForDate) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "card_spring_bounce"
    )

    val windowLabel = when (habit.anchor) {
        CircadianAnchor.MORNING_LIGHT -> if (isSpanish) "AL DESPERTAR" else "MORNING (06:00-08:00)"
        CircadianAnchor.HYDRATION_ELECTROLYTES -> if (isSpanish) "DESPERTAR INMEDIATO" else "EARLY MORNING"
        CircadianAnchor.CAFFEINE_CUTOFF -> if (isSpanish) "LÍMITE 14:00" else "CUTOFF 14:00"
        CircadianAnchor.ZONE_2_MOVEMENT -> if (isSpanish) "TARDE CIRCADIANO" else "AFTERNOON (16:00-18:00)"
        CircadianAnchor.DIGITAL_SUNSET -> if (isSpanish) "NOCHE 22:00" else "NIGHT (22:00)"
        CircadianAnchor.ALL_DAY -> if (isSpanish) "TODO EL DÍA (FLEXIBLE)" else "ALL DAY (FLEXIBLE)"
    }

    val (anchorIcon, anchorTint) = when (habit.anchor) {
        CircadianAnchor.MORNING_LIGHT -> Pair(Icons.Default.WbSunny, AetherAmber)
        CircadianAnchor.HYDRATION_ELECTROLYTES -> Pair(Icons.Default.WaterDrop, AetherCyan)
        CircadianAnchor.CAFFEINE_CUTOFF -> Pair(Icons.Default.Schedule, AetherCoral)
        CircadianAnchor.ZONE_2_MOVEMENT -> Pair(Icons.Default.DirectionsRun, AetherEmerald)
        CircadianAnchor.DIGITAL_SUNSET -> Pair(Icons.Default.NightsStay, AetherPurple)
        CircadianAnchor.ALL_DAY -> Pair(Icons.Default.AllInclusive, AetherCyan)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(cardScale)
            .combinedClickable(
                onClick = onToggle,
                onLongClick = onEdit
            )
            .testTag("habit_card_${habit.id}"),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isCompletedForDate -> AetherSurface.copy(alpha = 0.6f)
                isMissedForDate -> AetherCoral.copy(alpha = 0.05f)
                else -> AetherSurfaceCard
            }
        ),
        border = if (isMissedForDate) androidx.compose.foundation.BorderStroke(1.dp, AetherCoral.copy(alpha = 0.35f)) else null,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = anchorIcon,
                        contentDescription = null,
                        tint = anchorTint,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = windowLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = anchorTint,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // 4.2 Animated Streak Flame Badge
                    AnimatedStreakFlameBadge(streakDays = habit.streakDays, language = language)
                    // Personal best — only worth showing once it exceeds the current streak,
                    // otherwise the flame badge already conveys the same number.
                    if (habit.bestStreakDays > habit.streakDays) {
                        BestStreakBadge(bestStreakDays = habit.bestStreakDays, language = language)
                    }
                    if (habit.graceDaysUsed > 0) {
                        GraceDayBadge(graceDaysUsed = habit.graceDaysUsed, language = language)
                    }
                    IconButton(
                        onClick = onOpenHistory,
                        modifier = Modifier
                            .size(26.dp)
                            .testTag("habit_history_btn_${habit.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = if (isSpanish) "Historial" else "History",
                            tint = AetherTextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(onClick = onEdit, modifier = Modifier.size(26.dp)) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = AetherTextMuted, modifier = Modifier.size(15.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(26.dp)) {
                        Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "Delete", tint = AetherCoral.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                // 4.1 Animated Dopamine Tick Checkbox
                AnimatedDopamineCheckbox(
                    checked = isCompletedForDate,
                    onCheckedChange = { onToggle() },
                    modifier = Modifier.testTag("habit_checkbox_${habit.id}")
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = habit.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isCompletedForDate) AetherTextMuted else AetherTextPrimary,
                            textDecoration = if (isCompletedForDate) TextDecoration.LineThrough else null,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (isMissedForDate) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = AetherCoral.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = if (isSpanish) "No Hecha" else "Missed",
                                    color = AetherCoral,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = habit.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = AetherTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Consistencia de la Semana Actual (L-D) (Clickable para abrir historial completo)
            HabitWeeklyConsistency(
                habitId = habit.id,
                streakDays = habit.streakDays,
                isCompletedToday = isCompletedForDate,
                recentSummaries = recentSummaries,
                logs = logs,
                language = language,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onOpenHistory() }
            )

            Spacer(modifier = Modifier.height(8.dp))

            val todayIso = remember { com.example.data.util.AetherDateUtils.getTodayIso() }
            val isGraceUsedToday = habit.graceDayLastUsedDate == todayIso
            val isGraceLimitReached = habit.graceDaysUsed >= habit.maxGraceDaysPerPeriod
            val canApplyGrace = !isCompletedForDate && !isGraceUsedToday && !isGraceLimitReached
            val remainingGrace = (habit.maxGraceDaysPerPeriod - habit.graceDaysUsed).coerceAtLeast(0)

            val graceBtnText = when {
                isGraceUsedToday -> if (isSpanish) "🛡️ Gracia Activa" else "🛡️ Grace Active"
                isGraceLimitReached -> if (isSpanish) "🛡️ Límite (0/${habit.maxGraceDaysPerPeriod})" else "🛡️ Limit (0/${habit.maxGraceDaysPerPeriod})"
                isCompletedForDate -> if (isSpanish) "✓ Cumplido" else "✓ Done"
                else -> if (isSpanish) "🛡️ Usar Gracia ($remainingGrace/${habit.maxGraceDaysPerPeriod})" else "🛡️ Apply Grace ($remainingGrace/${habit.maxGraceDaysPerPeriod})"
            }

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

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // "Marcar como no hecha" explicit button
                    FilledTonalButton(
                        onClick = onMarkNotDone,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (isMissedForDate) AetherCoral.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            contentColor = if (isMissedForDate) AetherCoral else AetherTextSecondary
                        ),
                        modifier = Modifier
                            .height(28.dp)
                            .testTag("habit_mark_not_done_btn_${habit.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = if (isMissedForDate) AetherCoral else AetherTextSecondary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isMissedForDate) {
                                if (isSpanish) "No Hecha" else "Missed"
                            } else {
                                if (isSpanish) "✕ No hecha" else "✕ Not done"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 11.sp,
                            fontWeight = if (isMissedForDate) FontWeight.Bold else FontWeight.Normal
                        )
                    }

                    TextButton(
                        onClick = onApplyGrace,
                        enabled = canApplyGrace,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = AetherEmerald,
                            disabledContentColor = AetherTextMuted.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = if (canApplyGrace) AetherEmerald else AetherTextMuted.copy(alpha = 0.6f),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = graceBtnText,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (canApplyGrace) AetherEmerald else AetherTextMuted.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

fun getAnchorDetails(anchor: CircadianAnchor, isSpanish: Boolean): Triple<String, androidx.compose.ui.graphics.vector.ImageVector, Color> {
    return when (anchor) {
        CircadianAnchor.MORNING_LIGHT -> Triple(
            if (isSpanish) "Luz Solar Matutina (Mañana)" else "Morning Sunlight (Early)",
            Icons.Default.WbSunny,
            AetherAmber
        )
        CircadianAnchor.HYDRATION_ELECTROLYTES -> Triple(
            if (isSpanish) "Hidratación y Carga Mineral" else "Hydration & Mineral Charge",
            Icons.Default.WaterDrop,
            AetherCyan
        )
        CircadianAnchor.ZONE_2_MOVEMENT -> Triple(
            if (isSpanish) "Movimiento Aeróbico Zona 2" else "Zone 2 Movement",
            Icons.Default.DirectionsRun,
            AetherEmerald
        )
        CircadianAnchor.CAFFEINE_CUTOFF -> Triple(
            if (isSpanish) "Límite de Cafeína (14:00)" else "Caffeine Cutoff (14:00)",
            Icons.Default.Schedule,
            AetherCoral
        )
        CircadianAnchor.DIGITAL_SUNSET -> Triple(
            if (isSpanish) "Ocaso Digital (Noche)" else "Digital Sunset (Night)",
            Icons.Default.NightsStay,
            AetherPurple
        )
        CircadianAnchor.ALL_DAY -> Triple(
            if (isSpanish) "A lo Largo del Día (Flexible)" else "Throughout the Day (Flexible)",
            Icons.Default.AllInclusive,
            AetherCyan
        )
    }
}

@Composable
fun HabitAnchorSectionHeader(
    title: String,
    completedCount: Int,
    totalCount: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    val allDone = totalCount > 0 && completedCount == totalCount

    Surface(
        onClick = onToggle,
        modifier = modifier
            .fillMaxWidth()
            .then(if (testTag.isNotBlank()) Modifier.testTag(testTag) else Modifier),
        shape = RoundedCornerShape(12.dp),
        color = AetherSurfaceElevated,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (allDone) AetherEmerald.copy(alpha = 0.5f) else AetherBorder.copy(alpha = 0.6f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = AetherTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .background(
                            if (allDone) AetherEmerald.copy(alpha = 0.2f) else iconTint.copy(alpha = 0.15f),
                            RoundedCornerShape(10.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "$completedCount/$totalCount",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (allDone) AetherEmerald else iconTint,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }

            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = if (isExpanded) "Colapsar" else "Expandir",
                tint = AetherTextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

