package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyRolloverResult
import com.example.ui.i18n.AppLanguage
import com.example.ui.theme.*

/**
 * Shown once when a new day has been rolled over, summarising what happened to the previous
 * day (or days, if the app wasn't opened for a while) so the reset never feels like data loss.
 *
 * Before this card existed, the rollover result was computed and stored in UI state but never
 * rendered anywhere: the user saw their habit ticks and tasks silently cleared with only a
 * generic "new day started" toast, which reads like the app forgot everything.
 */
@Composable
fun DailyRolloverSummaryCard(
    notice: DailyRolloverResult,
    language: AppLanguage,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isSpanish = language == AppLanguage.SPANISH
    val multiDay = notice.daysDiff > 1

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("daily_rollover_summary_card")
            .background(AetherEmerald.copy(alpha = 0.10f), RoundedCornerShape(16.dp))
            .border(1.dp, AetherEmerald.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.WbTwilight,
                    contentDescription = null,
                    tint = AetherEmerald,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(
                        text = if (isSpanish) "RESUMEN DEL CIERRE" else "ROLLOVER SUMMARY",
                        color = AetherEmerald,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = if (multiDay) {
                            if (isSpanish) "Se cerraron ${notice.daysDiff} días"
                            else "${notice.daysDiff} days closed out"
                        } else {
                            if (isSpanish) "Día anterior cerrado"
                            else "Previous day closed out"
                        },
                        color = AetherTextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .size(28.dp)
                    .testTag("dismiss_rollover_summary_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = if (isSpanish) "Descartar resumen" else "Dismiss summary",
                    tint = AetherTextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RolloverStat(
                icon = Icons.Default.CheckCircle,
                value = notice.completedHabitsCount,
                label = if (isSpanish) "Hábitos" else "Habits",
                tint = AetherEmerald,
                modifier = Modifier.weight(1f)
            )
            RolloverStat(
                icon = Icons.Default.Task,
                value = notice.completedTasksCount,
                label = if (isSpanish) "Tareas" else "Tasks",
                tint = AetherCyan,
                modifier = Modifier.weight(1f)
            )
            RolloverStat(
                icon = Icons.Default.Restaurant,
                value = notice.completedMealsCount,
                label = if (isSpanish) "Comidas" else "Meals",
                tint = AetherAmber,
                modifier = Modifier.weight(1f)
            )
        }

        if (notice.preservedHabitStreaksCount > 0 || notice.brokenHabitStreaksCount > 0 || notice.rolledOverTasksCount > 0) {
            Spacer(Modifier.height(10.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (notice.preservedHabitStreaksCount > 0) {
                    RolloverDetailLine(
                        text = if (isSpanish) "🛡️ ${notice.preservedHabitStreaksCount} racha(s) protegidas con día de gracia"
                        else "🛡️ ${notice.preservedHabitStreaksCount} streak(s) protected by grace day",
                        color = AetherEmerald
                    )
                }
                if (notice.brokenHabitStreaksCount > 0) {
                    RolloverDetailLine(
                        text = if (isSpanish) "💔 ${notice.brokenHabitStreaksCount} racha(s) reiniciadas"
                        else "💔 ${notice.brokenHabitStreaksCount} streak(s) reset",
                        color = AetherTextSecondary
                    )
                }
                if (notice.rolledOverTasksCount > 0) {
                    RolloverDetailLine(
                        text = if (isSpanish) "📋 ${notice.rolledOverTasksCount} tarea(s) siguen pendientes hoy"
                        else "📋 ${notice.rolledOverTasksCount} task(s) still pending today",
                        color = AetherCyan
                    )
                }
            }
        }

        if (multiDay) {
            Spacer(Modifier.height(10.dp))
            Text(
                text = if (isSpanish)
                    "No abriste la app durante ese periodo, así que esos días quedan registrados como no completados en tu historial."
                else
                    "The app wasn't opened during that period, so those days are recorded as not completed in your history.",
                color = AetherTextSecondary,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
private fun RolloverStat(
    icon: ImageVector,
    value: Int,
    label: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(AetherSurfaceElevated, RoundedCornerShape(10.dp))
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "$value",
            color = AetherTextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            color = AetherTextSecondary,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun RolloverDetailLine(text: String, color: Color) {
    Text(
        text = text,
        color = color,
        fontSize = 11.sp,
        lineHeight = 15.sp
    )
}
