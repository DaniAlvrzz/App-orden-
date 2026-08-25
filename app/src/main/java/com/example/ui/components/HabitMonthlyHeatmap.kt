package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import java.time.LocalDate

/**
 * 4.6 Mini heatmap mensual/semanal de consistencia dentro de cada tarjeta de hábito
 * Cuadrados con colores según estado histórico (Verde = Completado, Naranja = Grace/Parcial, Rojo/Gris = Pendiente/Perdido)
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
    val isSpanish = language == AppLanguage.SPANISH
    val today = remember { LocalDate.now() }

    // Display last 28 days (4 weeks x 7 days)
    val daysCount = 28
    val habitLogsByDate = remember(logs, habitId) {
        logs.filter { it.itemId == habitId }.associateBy { it.dateIso }
    }

    val summaryByDate = remember(recentSummaries) {
        recentSummaries.associateBy { it.dateIso }
    }

    val daysData = remember(daysCount, habitId, streakDays, isCompletedToday, habitLogsByDate, summaryByDate) {
        (daysCount - 1 downTo 0).map { offset ->
            val date = today.minusDays(offset.toLong())
            val dateIso = date.toString()
            val isToday = offset == 0

            // Determine status for this day
            val statusColor: Color = when {
                isToday -> {
                    if (isCompletedToday) AetherEmerald else AetherBorder.copy(alpha = 0.5f)
                }
                habitLogsByDate.containsKey(dateIso) -> {
                    val log = habitLogsByDate[dateIso]
                    when (log?.status) {
                        CompletionStatus.COMPLETED -> AetherEmerald
                        CompletionStatus.PARTIAL -> AetherAmber
                        CompletionStatus.MISSED -> Color(0xFFEF4444).copy(alpha = 0.6f)
                        null -> AetherSurfaceCard
                    }
                }
                summaryByDate.containsKey(dateIso) -> {
                    val summary = summaryByDate[dateIso]
                    if (summary != null && summary.ratio >= 0.7f) AetherEmerald
                    else if (summary != null && summary.ratio > 0f) AetherAmber
                    else AetherSurfaceCard
                }
                // Fallback simulation based on streak
                offset <= streakDays && streakDays > 0 -> {
                    AetherEmerald.copy(alpha = (0.6f + (offset.toFloat() / (streakDays + 1) * 0.4f)).coerceIn(0.5f, 0.95f))
                }
                else -> {
                    AetherSurfaceCard.copy(alpha = 0.4f)
                }
            }

            date to statusColor
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("habit_monthly_heatmap_$habitId")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isSpanish) "Consistencia 28 días:" else "28-Day Consistency:",
                style = MaterialTheme.typography.labelSmall,
                color = AetherTextMuted,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(6.dp).clip(RoundedCornerShape(1.5.dp)).background(AetherEmerald))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(text = if (isSpanish) "Cumplido" else "Done", fontSize = 8.sp, color = AetherTextMuted)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(6.dp).clip(RoundedCornerShape(1.5.dp)).background(AetherAmber))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(text = if (isSpanish) "Grace" else "Grace", fontSize = 8.sp, color = AetherTextMuted)
                }
            }
        }

        Spacer(modifier = Modifier.height(5.dp))

        // Heatmap Grid: 4 rows of 7 days
        Column(
            verticalArrangement = Arrangement.spacedBy(3.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            for (row in 0 until 4) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    for (col in 0 until 7) {
                        val index = (row * 7) + col
                        val (date, color) = daysData.getOrElse(index) { today to AetherSurfaceCard }
                        val isToday = date == today

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(8.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(color)
                        )
                    }
                }
            }
        }
    }
}
