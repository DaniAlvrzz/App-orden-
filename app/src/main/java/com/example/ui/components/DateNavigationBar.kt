package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.i18n.AppLanguage
import com.example.ui.theme.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateNavigationBar(
    selectedDateIso: String,
    language: AppLanguage,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onGoToToday: () -> Unit,
    onSelectDate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isSpanish = language == AppLanguage.SPANISH
    val locale = if (isSpanish) Locale.forLanguageTag("es-ES") else Locale.ENGLISH

    val today = LocalDate.now()
    val todayIso = today.toString()
    val selectedDate = remember(selectedDateIso) {
        try { LocalDate.parse(selectedDateIso) } catch (e: Exception) { today }
    }
    val isToday = selectedDateIso == todayIso
    val isYesterday = selectedDate == today.minusDays(1)
    val isTomorrow = selectedDate == today.plusDays(1)

    var showDatePicker by remember { mutableStateOf(false) }

    val dateLabel = remember(selectedDate, isToday, isYesterday, isTomorrow, isSpanish, locale) {
        val dayName = selectedDate.dayOfWeek.getDisplayName(TextStyle.SHORT, locale).replaceFirstChar { it.uppercase() }
        val monthName = selectedDate.month.getDisplayName(TextStyle.SHORT, locale).replaceFirstChar { it.uppercase() }
        val baseDateStr = "$dayName, ${selectedDate.dayOfMonth} $monthName"

        when {
            isToday -> if (isSpanish) "Hoy • $baseDateStr" else "Today • $baseDateStr"
            isYesterday -> if (isSpanish) "Ayer • $baseDateStr" else "Yesterday • $baseDateStr"
            isTomorrow -> if (isSpanish) "Mañana • $baseDateStr" else "Tomorrow • $baseDateStr"
            else -> "$baseDateStr ${selectedDate.year}"
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isToday) AetherSurfaceElevated else AetherCyan.copy(alpha = 0.08f))
            .border(
                1.dp,
                if (isToday) AetherBorder else AetherCyan.copy(alpha = 0.35f),
                RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag("date_navigation_bar")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Previous Day Button
            IconButton(
                onClick = onPreviousDay,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("date_nav_prev_btn")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = if (isSpanish) "Día anterior" else "Previous day",
                    tint = if (isToday) AetherTextSecondary else AetherCyan,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Center Date Selector Pill
            Surface(
                color = if (isToday) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else AetherCyan.copy(alpha = 0.18f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 6.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { showDatePicker = true }
                    .testTag("date_nav_select_btn")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = if (isToday) AetherTextSecondary else AetherCyan,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = dateLabel,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (isToday) AetherTextPrimary else AetherCyan,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = if (isToday) AetherTextMuted else AetherCyan,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Next Day Button
            IconButton(
                onClick = onNextDay,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("date_nav_next_btn")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = if (isSpanish) "Día siguiente" else "Next day",
                    tint = if (isToday) AetherTextSecondary else AetherCyan,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Context Banner if not viewing today
        AnimatedVisibility(
            visible = !isToday,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp, start = 4.dp, end = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.HistoryEdu,
                        contentDescription = null,
                        tint = AetherAmber,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isSpanish)
                            "Visualizando y editando registro de este día"
                        else
                            "Viewing & logging for this date",
                        style = MaterialTheme.typography.labelSmall,
                        color = AetherAmber,
                        fontSize = 11.sp
                    )
                }

                FilledTonalButton(
                    onClick = onGoToToday,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = AetherCyan.copy(alpha = 0.2f),
                        contentColor = AetherCyan
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier
                        .height(26.dp)
                        .testTag("date_nav_go_today_btn")
                ) {
                    Text(
                        text = if (isSpanish) "Ir a Hoy" else "Go to Today",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        fontSize = 10.sp
                    )
                }
            }
        }
    }

    // Material 3 Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val millis = datePickerState.selectedDateMillis
                        if (millis != null) {
                            val pickedLocalDate = Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate()
                            onSelectDate(pickedLocalDate.toString())
                        }
                        showDatePicker = false
                    },
                    modifier = Modifier.testTag("confirm_date_pick_btn")
                ) {
                    Text(if (isSpanish) "Aceptar" else "OK", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(if (isSpanish) "Cancelar" else "Cancel")
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                title = {
                    Text(
                        text = if (isSpanish) "Seleccionar día para ver o registrar" else "Select day to view or log",
                        modifier = Modifier.padding(start = 24.dp, end = 12.dp, top = 16.dp),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            )
        }
    }
}
