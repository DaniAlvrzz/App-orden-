package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CompletionLog
import com.example.data.model.CompletionStatus
import com.example.data.model.DailySummary
import com.example.ui.i18n.AppLanguage
import com.example.ui.theme.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

/**
 * Visualizador de consistencia de la SEMANA ACTUAL (Lunes a Domingo) para la tarjeta de hábito.
 * Muestra el estado día a día de la semana en curso con etiquetas claras de días y estados.
 */
@Composable
fun HabitMonthlyHeatmap(
    habitId: String,
    streakDays: Int,
    isCompletedToday: Boolean,
    recentSummaries: List<DailySummary>,
    logs: List<CompletionLog> = emptyList(),
    language: AppLanguage = AppLanguage.SPANISH,
    modifier: Modifier = Modifier
) {
    HabitWeeklyConsistency(
        habitId = habitId,
        streakDays = streakDays,
        isCompletedToday = isCompletedToday,
        recentSummaries = recentSummaries,
        logs = logs,
        language = language,
        modifier = modifier
    )
}

@Composable
fun HabitWeeklyConsistency(
    habitId: String,
    streakDays: Int,
    isCompletedToday: Boolean,
    recentSummaries: List<DailySummary>,
    logs: List<CompletionLog> = emptyList(),
    language: AppLanguage = AppLanguage.SPANISH,
    modifier: Modifier = Modifier
) {
    val isSpanish = language == AppLanguage.SPANISH
    val today = remember { LocalDate.now() }

    // Start of the CURRENT week (Monday)
    val startOfWeek = remember(today) {
        today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    }

    val habitLogsByDate = remember(logs, habitId) {
        logs.filter { it.itemId == habitId }.associateBy { it.dateIso }
    }

    // The global daily summary is intentionally NOT consulted for per-habit day states:
    // it aggregates every habit, task and meal, so it can't tell us anything about whether
    // THIS habit was done. Kept as a parameter for API compatibility with existing callers.
    val weekDays = remember(startOfWeek, today, habitId, isCompletedToday, habitLogsByDate) {
        (0..6).map { dayOffset ->
            val date = startOfWeek.plusDays(dayOffset.toLong())
            val dateIso = date.toString()
            val isToday = date == today
            val isFuture = date.isAfter(today)
            val isPast = date.isBefore(today)

            val (status, color) = when {
                isToday -> {
                    if (isCompletedToday) {
                        DayCompletionState.COMPLETED to AetherEmerald
                    } else {
                        DayCompletionState.PENDING_TODAY to AetherSurfaceElevated
                    }
                }
                isFuture -> {
                    DayCompletionState.FUTURE to AetherSurfaceCard.copy(alpha = 0.25f)
                }
                habitLogsByDate.containsKey(dateIso) -> {
                    val log = habitLogsByDate[dateIso]
                    when (log?.status) {
                        CompletionStatus.COMPLETED -> DayCompletionState.COMPLETED to AetherEmerald
                        CompletionStatus.PARTIAL -> DayCompletionState.GRACE to AetherAmber
                        CompletionStatus.MISSED -> DayCompletionState.MISSED to AetherCoral.copy(alpha = 0.6f)
                        null -> DayCompletionState.NO_DATA to AetherSurfaceCard.copy(alpha = 0.35f)
                    }
                }
                // No per-habit log for this past date.
                //
                // Previously this fell back to (a) the GLOBAL daily summary and (b) a simulation
                // derived from the current streak. Both were wrong for a per-habit view:
                // the daily summary aggregates EVERY habit, task and meal, so completing any
                // other item that day produced ratio > 0 and painted THIS habit — untouched —
                // as an amber "grace day used", which is a state the user never triggered.
                // The streak-based simulation had the mirror problem, inventing green ticks
                // for days that were never actually logged.
                //
                // With the rollover now backfilling explicit MISSED logs for skipped days, a
                // genuine miss always has its own record. So an absent record here means only
                // one thing: this habit wasn't being tracked that day (created later, or before
                // logging began). That is NO_DATA, not a miss and certainly not a grace day.
                isPast -> {
                    DayCompletionState.NO_DATA to AetherSurfaceCard.copy(alpha = 0.35f)
                }
                else -> DayCompletionState.FUTURE to AetherSurfaceCard.copy(alpha = 0.25f)
            }

            WeekDayItem(
                date = date,
                dayName = getDayAbbreviation(date.dayOfWeek, isSpanish),
                dayNumber = date.dayOfMonth,
                isToday = isToday,
                isFuture = isFuture,
                status = status,
                color = color
            )
        }
    }

    val completedThisWeekCount = weekDays.count { it.status == DayCompletionState.COMPLETED || it.status == DayCompletionState.GRACE }
    val daysPassedThisWeek = weekDays.count { !it.isFuture }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("habit_weekly_consistency_$habitId")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isSpanish) "Semana actual" else "This week",
                    style = MaterialTheme.typography.labelSmall,
                    color = AetherTextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "• $completedThisWeekCount/$daysPassedThisWeek " + (if (isSpanish) "días" else "days"),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (completedThisWeekCount > 0) AetherEmerald else AetherTextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(AetherEmerald)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = if (isSpanish) "Hecho" else "Done",
                        fontSize = 8.5.sp,
                        color = AetherTextMuted
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(AetherAmber)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "Grace",
                        fontSize = 8.5.sp,
                        color = AetherTextMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Row of 7 days: Monday to Sunday of the current week
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(AetherSurfaceElevated.copy(alpha = 0.5f))
                .padding(vertical = 5.dp, horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            weekDays.forEach { item ->
                DayPill(item = item)
            }
        }
    }
}

@Composable
private fun RowScope.DayPill(item: WeekDayItem) {
    Column(
        modifier = Modifier
            .weight(1f)
            .padding(horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = item.dayName,
            style = MaterialTheme.typography.labelSmall,
            color = if (item.isToday) AetherCyan else if (item.isFuture) AetherTextMuted.copy(alpha = 0.5f) else AetherTextMuted,
            fontSize = 9.sp,
            fontWeight = if (item.isToday) FontWeight.Bold else FontWeight.Normal
        )

        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(
                    when (item.status) {
                        DayCompletionState.COMPLETED -> AetherEmerald.copy(alpha = 0.2f)
                        DayCompletionState.GRACE -> AetherAmber.copy(alpha = 0.2f)
                        DayCompletionState.MISSED -> AetherSurfaceCard.copy(alpha = 0.35f)
                        DayCompletionState.NO_DATA -> Color.Transparent
                        DayCompletionState.PENDING_TODAY -> AetherCyan.copy(alpha = 0.15f)
                        DayCompletionState.FUTURE -> Color.Transparent
                    }
                )
                .then(
                    if (item.isToday) {
                        Modifier.border(
                            1.dp,
                            if (item.status == DayCompletionState.COMPLETED) AetherEmerald else AetherCyan,
                            CircleShape
                        )
                    } else if (item.isFuture) {
                        Modifier.border(
                            0.5.dp,
                            AetherBorder.copy(alpha = 0.25f),
                            CircleShape
                        )
                    } else {
                        Modifier
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            when (item.status) {
                DayCompletionState.COMPLETED -> {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completado",
                        tint = AetherEmerald,
                        modifier = Modifier.size(13.dp)
                    )
                }
                DayCompletionState.GRACE -> {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Grace Day",
                        tint = AetherAmber,
                        modifier = Modifier.size(11.dp)
                    )
                }
                DayCompletionState.PENDING_TODAY -> {
                    Text(
                        text = item.dayNumber.toString(),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = AetherCyan
                    )
                }
                DayCompletionState.MISSED -> {
                    Text(
                        text = item.dayNumber.toString(),
                        fontSize = 9.sp,
                        color = AetherTextMuted.copy(alpha = 0.6f)
                    )
                }
                DayCompletionState.NO_DATA -> {
                    // A dash, not a number: visually says "nothing was tracked here" rather
                    // than implying the day was assessed and failed.
                    Text(
                        text = "–",
                        fontSize = 9.sp,
                        color = AetherTextMuted.copy(alpha = 0.3f)
                    )
                }
                DayCompletionState.FUTURE -> {
                    Text(
                        text = item.dayNumber.toString(),
                        fontSize = 9.sp,
                        color = AetherTextMuted.copy(alpha = 0.35f)
                    )
                }
            }
        }
    }
}

private enum class DayCompletionState {
    COMPLETED,
    GRACE,
    PENDING_TODAY,
    MISSED,
    /**
     * No record exists for this habit on this date — typically a day before the habit was
     * created, or before the app started logging. Deliberately distinct from MISSED: "we have
     * no data" is not the same claim as "the user failed to do it", and rendering the two
     * identically makes the history lie about days the user was never even tracked on.
     */
    NO_DATA,
    FUTURE
}

private data class WeekDayItem(
    val date: LocalDate,
    val dayName: String,
    val dayNumber: Int,
    val isToday: Boolean,
    val isFuture: Boolean,
    val status: DayCompletionState,
    val color: Color
)

private fun getDayAbbreviation(dayOfWeek: DayOfWeek, isSpanish: Boolean): String {
    return if (isSpanish) {
        when (dayOfWeek) {
            DayOfWeek.MONDAY -> "L"
            DayOfWeek.TUESDAY -> "M"
            DayOfWeek.WEDNESDAY -> "X"
            DayOfWeek.THURSDAY -> "J"
            DayOfWeek.FRIDAY -> "V"
            DayOfWeek.SATURDAY -> "S"
            DayOfWeek.SUNDAY -> "D"
        }
    } else {
        when (dayOfWeek) {
            DayOfWeek.MONDAY -> "M"
            DayOfWeek.TUESDAY -> "T"
            DayOfWeek.WEDNESDAY -> "W"
            DayOfWeek.THURSDAY -> "T"
            DayOfWeek.FRIDAY -> "F"
            DayOfWeek.SATURDAY -> "S"
            DayOfWeek.SUNDAY -> "S"
        }
    }
}
