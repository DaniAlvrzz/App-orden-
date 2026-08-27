package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.*
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.StringsProvider
import com.example.ui.theme.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

enum class IndividualHistoryViewMode {
    WEEK,
    MONTH,
    YEAR
}

/**
 * Historial individual completo para cualquier Tarea Fija, Tarea Estándar, Hábito o Elemento
 * con navegación y vistas de Semana, Mes y Año con cuadrículas de cuadrados térmicos (heatmaps).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IndividualHistoryDialog(
    target: IndividualHistoryTarget,
    logs: List<CompletionLog>,
    language: AppLanguage,
    onDismiss: () -> Unit
) {
    val isSpanish = language == AppLanguage.SPANISH
    val locale = if (isSpanish) Locale.forLanguageTag("es-ES") else Locale.ENGLISH

    var viewMode by remember { mutableStateOf(IndividualHistoryViewMode.MONTH) }
    var selectedWeekOffset by remember { mutableIntStateOf(0) }
    var selectedYear by remember { mutableIntStateOf(LocalDate.now().year) }
    var selectedMonth by remember { mutableIntStateOf(LocalDate.now().monthValue) }
    var selectedDateIso by remember { mutableStateOf<String?>(null) }

    // Map logs by dateIso
    val logsByDate = remember(logs) {
        logs.associateBy { it.dateIso }
    }

    // Colors and icon for target type
    val (typeIcon, typeColor, typeName) = remember(target, isSpanish) {
        when (target.itemType) {
            CompletionItemType.TASK -> {
                if (target.isPermanent) {
                    Triple(Icons.Default.PushPin, AetherPurple, if (isSpanish) "Tarea Fija / Recurrente" else "Persistent Task")
                } else if (target.isFrog) {
                    Triple(Icons.Default.LocalFireDepartment, AetherAmber, if (isSpanish) "Tarea Frog" else "Frog Task")
                } else {
                    Triple(Icons.Default.CheckCircleOutline, AetherCyan, if (isSpanish) "Tarea" else "Task")
                }
            }
            CompletionItemType.HABIT -> {
                when (target.anchor) {
                    CircadianAnchor.MORNING_LIGHT -> Triple(Icons.Default.WbSunny, AetherAmber, if (isSpanish) "Hábito • Luz Solar" else "Habit • Sunlight")
                    CircadianAnchor.HYDRATION_ELECTROLYTES -> Triple(Icons.Default.WaterDrop, AetherCyan, if (isSpanish) "Hábito • Hidratación" else "Habit • Hydration")
                    CircadianAnchor.CAFFEINE_CUTOFF -> Triple(Icons.Default.Schedule, AetherCoral, if (isSpanish) "Hábito • Límite Cafeína" else "Habit • Caffeine")
                    CircadianAnchor.ZONE_2_MOVEMENT -> Triple(Icons.Default.DirectionsRun, AetherEmerald, if (isSpanish) "Hábito • Movimiento" else "Habit • Movement")
                    CircadianAnchor.DIGITAL_SUNSET -> Triple(Icons.Default.NightsStay, AetherPurple, if (isSpanish) "Hábito • Ocaso Digital" else "Habit • Digital Sunset")
                    CircadianAnchor.ALL_DAY, null -> Triple(Icons.Default.AllInclusive, AetherCyan, if (isSpanish) "Hábito Diario" else "Daily Habit")
                }
            }
            CompletionItemType.MEAL -> Triple(Icons.Default.Restaurant, AetherEmerald, if (isSpanish) "Nutrición" else "Meal")
            CompletionItemType.TIME_BLOCK -> Triple(Icons.Default.HourglassTop, AetherCyan, if (isSpanish) "Bloque de Tiempo" else "Time Block")
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .testTag("individual_history_dialog_${target.id}"),
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = typeIcon,
                                    contentDescription = null,
                                    tint = typeColor,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = target.title,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = AetherTextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Text(
                                text = target.subtitle.ifBlank { typeName },
                                style = MaterialTheme.typography.bodySmall,
                                color = AetherTextMuted,
                                fontSize = 11.sp
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("close_individual_history_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = if (isSpanish) "Cerrar" else "Close",
                                tint = AetherTextPrimary
                            )
                        }
                    },
                    actions = {
                        // Selector de Vista: Semana | Mes | Año
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            FilterChip(
                                selected = viewMode == IndividualHistoryViewMode.WEEK,
                                onClick = { viewMode = IndividualHistoryViewMode.WEEK },
                                label = { Text(if (isSpanish) "Semana" else "Week", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = typeColor.copy(alpha = 0.25f),
                                    selectedLabelColor = typeColor
                                )
                            )
                            FilterChip(
                                selected = viewMode == IndividualHistoryViewMode.MONTH,
                                onClick = { viewMode = IndividualHistoryViewMode.MONTH },
                                label = { Text(if (isSpanish) "Mes" else "Month", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = typeColor.copy(alpha = 0.25f),
                                    selectedLabelColor = typeColor
                                )
                            )
                            FilterChip(
                                selected = viewMode == IndividualHistoryViewMode.YEAR,
                                onClick = { viewMode = IndividualHistoryViewMode.YEAR },
                                label = { Text(if (isSpanish) "Año" else "Year", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = typeColor.copy(alpha = 0.25f),
                                    selectedLabelColor = typeColor
                                )
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = AetherSurfaceElevated
                    )
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (viewMode) {
                    IndividualHistoryViewMode.WEEK -> {
                        IndividualWeekHistoryView(
                            target = target,
                            logsByDate = logsByDate,
                            weekOffset = selectedWeekOffset,
                            isSpanish = isSpanish,
                            locale = locale,
                            typeColor = typeColor,
                            selectedDateIso = selectedDateIso,
                            onSelectDate = { selectedDateIso = it },
                            onOffsetChange = { selectedWeekOffset = it }
                        )
                    }
                    IndividualHistoryViewMode.MONTH -> {
                        IndividualMonthHistoryView(
                            target = target,
                            logsByDate = logsByDate,
                            year = selectedYear,
                            month = selectedMonth,
                            isSpanish = isSpanish,
                            locale = locale,
                            typeColor = typeColor,
                            selectedDateIso = selectedDateIso,
                            onSelectDate = { selectedDateIso = it },
                            onMonthChange = { newMonth, newYear ->
                                selectedMonth = newMonth
                                selectedYear = newYear
                            }
                        )
                    }
                    IndividualHistoryViewMode.YEAR -> {
                        IndividualYearHistoryView(
                            target = target,
                            logsByDate = logsByDate,
                            year = selectedYear,
                            isSpanish = isSpanish,
                            locale = locale,
                            typeColor = typeColor,
                            onSelectMonth = { yr, mo ->
                                selectedYear = yr
                                selectedMonth = mo
                                viewMode = IndividualHistoryViewMode.MONTH
                            },
                            onYearChange = { newYear ->
                                selectedYear = newYear
                            }
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// LEVEL 1: INDIVIDUAL WEEK VIEW (7 DAYS SQUARES WITH TIMESTAMPS)
// -------------------------------------------------------------------------------------------------
@Composable
fun IndividualWeekHistoryView(
    target: IndividualHistoryTarget,
    logsByDate: Map<String, CompletionLog>,
    weekOffset: Int,
    isSpanish: Boolean,
    locale: Locale,
    typeColor: Color,
    selectedDateIso: String?,
    onSelectDate: (String) -> Unit,
    onOffsetChange: (Int) -> Unit
) {
    val today = remember { LocalDate.now() }
    val todayIso = today.toString()

    val currentMonday = remember(weekOffset) {
        today.plusWeeks(weekOffset.toLong()).with(DayOfWeek.MONDAY)
    }
    val currentSunday = remember(currentMonday) {
        currentMonday.plusDays(6)
    }

    val daysOfWeek = remember(currentMonday) {
        (0..6).map { currentMonday.plusDays(it.toLong()) }
    }

    val weekCompletedCount = remember(daysOfWeek, logsByDate, todayIso) {
        daysOfWeek.count { date ->
            val iso = date.toString()
            val log = logsByDate[iso]
            log?.status == CompletionStatus.COMPLETED || (iso == todayIso && target.streakDays > 0 && log != null)
        }
    }
    val weekGraceCount = remember(daysOfWeek, logsByDate) {
        daysOfWeek.count { date ->
            val log = logsByDate[date.toString()]
            log?.status == CompletionStatus.PARTIAL
        }
    }

    val weekRangeStr = remember(currentMonday, currentSunday, locale) {
        val startM = currentMonday.month.getDisplayName(TextStyle.SHORT, locale).replaceFirstChar { it.uppercase() }
        val endM = currentSunday.month.getDisplayName(TextStyle.SHORT, locale).replaceFirstChar { it.uppercase() }
        if (currentMonday.month == currentSunday.month) {
            "${currentMonday.dayOfMonth} - ${currentSunday.dayOfMonth} $startM ${currentMonday.year}"
        } else {
            "${currentMonday.dayOfMonth} $startM - ${currentSunday.dayOfMonth} $endM ${currentSunday.year}"
        }
    }

    val weekRatio = weekCompletedCount.toFloat() / 7f

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Week Navigator Header
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AetherSurfaceElevated),
                border = androidx.compose.foundation.BorderStroke(1.dp, AetherBorder.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { onOffsetChange(weekOffset - 1) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Semana anterior", tint = AetherTextPrimary)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = weekRangeStr,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = AetherTextPrimary
                        )
                        if (weekOffset == 0) {
                            Text(
                                text = if (isSpanish) "Semana Actual" else "Current Week",
                                style = MaterialTheme.typography.labelSmall,
                                color = typeColor,
                                fontWeight = FontWeight.SemiBold
                            )
                        } else {
                            TextButton(
                                onClick = { onOffsetChange(0) },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(
                                    text = if (isSpanish) "Volver a hoy" else "Back to today",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = typeColor
                                )
                            }
                        }
                    }

                    IconButton(onClick = { onOffsetChange(weekOffset + 1) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Semana siguiente", tint = AetherTextPrimary)
                    }
                }
            }
        }

        // Weekly KPI Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricKpiCard(
                    title = if (isSpanish) "Días Cumplidos" else "Completed Days",
                    value = "$weekCompletedCount / 7",
                    icon = Icons.Default.CheckCircle,
                    color = AetherEmerald,
                    modifier = Modifier.weight(1f)
                )
                MetricKpiCard(
                    title = if (isSpanish) "Consistencia" else "Consistency",
                    value = "${(weekRatio * 100).toInt()}%",
                    icon = Icons.Default.ShowChart,
                    color = getRatioColor(weekRatio),
                    modifier = Modifier.weight(1f)
                )
                if (weekGraceCount > 0) {
                    MetricKpiCard(
                        title = if (isSpanish) "Gracia Usada" else "Grace Used",
                        value = "$weekGraceCount d",
                        icon = Icons.Default.Shield,
                        color = AetherAmber,
                        modifier = Modifier.weight(1f)
                    )
                } else if (target.streakDays > 0) {
                    MetricKpiCard(
                        title = if (isSpanish) "Racha Activa" else "Active Streak",
                        value = "${target.streakDays} d",
                        icon = Icons.Default.LocalFireDepartment,
                        color = AetherCoral,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 7 Day Squares Grid
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AetherSurfaceElevated),
                border = androidx.compose.foundation.BorderStroke(1.dp, AetherBorder.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (isSpanish) "Registro Diario de la Semana (Toca para ver detalle)" else "Weekly Daily Record (Tap to inspect)",
                        style = MaterialTheme.typography.labelMedium,
                        color = AetherTextMuted,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val dayNames = if (isSpanish) listOf("L", "M", "X", "J", "V", "S", "D") else listOf("M", "T", "W", "T", "F", "S", "S")

                        daysOfWeek.forEachIndexed { idx, date ->
                            val dateIso = date.toString()
                            val isToday = date == today
                            val isFuture = date.isAfter(today)
                            val log = logsByDate[dateIso]
                            val isSelected = selectedDateIso == dateIso

                            val (cellColor, statusIcon, statusLabel) = when {
                                log?.status == CompletionStatus.COMPLETED -> Triple(AetherEmerald, Icons.Default.Check, if (isSpanish) "Cumplido" else "Done")
                                log?.status == CompletionStatus.PARTIAL -> Triple(AetherAmber, Icons.Default.Shield, if (isSpanish) "Gracia" else "Grace")
                                log?.status == CompletionStatus.MISSED -> Triple(AetherCoral, Icons.Default.Close, if (isSpanish) "Perdido" else "Missed")
                                isFuture -> Triple(AetherSurfaceCard.copy(alpha = 0.3f), null, if (isSpanish) "Futuro" else "Future")
                                else -> Triple(AetherSurfaceCard.copy(alpha = 0.6f), null, if (isSpanish) "Pendiente" else "Pending")
                            }

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onSelectDate(dateIso) }
                                    .padding(horizontal = 2.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = dayNames[idx],
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isToday) typeColor else AetherTextMuted,
                                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                                )
                                Spacer(modifier = Modifier.height(4.dp))

                                // Square Box
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(cellColor)
                                        .then(
                                            if (isSelected) Modifier.border(2.dp, Color.White, RoundedCornerShape(8.dp))
                                            else if (isToday) Modifier.border(1.5.dp, typeColor, RoundedCornerShape(8.dp))
                                            else Modifier
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (statusIcon != null) {
                                        Icon(
                                            imageVector = statusIcon,
                                            contentDescription = statusLabel,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    } else {
                                        Text(
                                            text = date.dayOfMonth.toString(),
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (isFuture) AetherTextMuted.copy(alpha = 0.4f) else AetherTextSecondary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${date.dayOfMonth}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isToday) typeColor else AetherTextMuted,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Selected Day Details / Log info
        item {
            val inspectedDateIso = selectedDateIso ?: todayIso
            val inspectedDate = try { LocalDate.parse(inspectedDateIso) } catch (e: Exception) { today }
            val inspectedLog = logsByDate[inspectedDateIso]

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AetherSurfaceElevated),
                border = androidx.compose.foundation.BorderStroke(1.dp, AetherBorder.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${inspectedDate.dayOfWeek.getDisplayName(TextStyle.FULL, locale).replaceFirstChar { it.uppercase() }}, ${inspectedDate.dayOfMonth} de ${inspectedDate.month.getDisplayName(TextStyle.FULL, locale)}",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = AetherTextPrimary
                        )

                        if (inspectedLog != null) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = when (inspectedLog.status) {
                                    CompletionStatus.COMPLETED -> AetherEmerald.copy(alpha = 0.2f)
                                    CompletionStatus.PARTIAL -> AetherAmber.copy(alpha = 0.2f)
                                    CompletionStatus.MISSED -> AetherCoral.copy(alpha = 0.2f)
                                }
                            ) {
                                Text(
                                    text = when (inspectedLog.status) {
                                        CompletionStatus.COMPLETED -> if (isSpanish) "✓ Completado" else "✓ Completed"
                                        CompletionStatus.PARTIAL -> if (isSpanish) "🛡️ Gracia Aplicada" else "🛡️ Grace Applied"
                                        CompletionStatus.MISSED -> if (isSpanish) "✕ No Realizado" else "✕ Missed"
                                    },
                                    color = when (inspectedLog.status) {
                                        CompletionStatus.COMPLETED -> AetherEmerald
                                        CompletionStatus.PARTIAL -> AetherAmber
                                        CompletionStatus.MISSED -> AetherCoral
                                    },
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        } else {
                            Text(
                                text = if (inspectedDate.isAfter(today)) (if (isSpanish) "Día futuro" else "Future day") else (if (isSpanish) "Sin registro" else "No log"),
                                style = MaterialTheme.typography.labelSmall,
                                color = AetherTextMuted
                            )
                        }
                    }

                    if (inspectedLog != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        val timeStr = remember(inspectedLog.timestamp) {
                            val instant = java.time.Instant.ofEpochMilli(inspectedLog.timestamp)
                            val zdt = instant.atZone(java.time.ZoneId.systemDefault())
                            String.format(Locale.US, "%02d:%02d", zdt.hour, zdt.minute)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccessTime, contentDescription = null, tint = AetherTextMuted, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isSpanish) "Completado a las $timeStr" else "Completed at $timeStr",
                                style = MaterialTheme.typography.bodySmall,
                                color = AetherTextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// LEVEL 2: INDIVIDUAL MONTH VIEW (CALENDAR SQUARES HEATMAP + MONTHLY METRICS)
// -------------------------------------------------------------------------------------------------
@Composable
fun IndividualMonthHistoryView(
    target: IndividualHistoryTarget,
    logsByDate: Map<String, CompletionLog>,
    year: Int,
    month: Int,
    isSpanish: Boolean,
    locale: Locale,
    typeColor: Color,
    selectedDateIso: String?,
    onSelectDate: (String) -> Unit,
    onMonthChange: (newMonth: Int, newYear: Int) -> Unit
) {
    val ym = remember(year, month) { YearMonth.of(year, month) }
    val monthName = ym.month.getDisplayName(TextStyle.FULL, locale).replaceFirstChar { it.uppercase() }
    val daysInMonth = ym.lengthOfMonth()
    val today = remember { LocalDate.now() }
    val todayIso = today.toString()

    // Calculate completions for this month
    val monthPrefix = String.format(Locale.US, "%04d-%02d", year, month)
    val monthLogs = remember(logsByDate, monthPrefix) {
        logsByDate.filter { it.key.startsWith(monthPrefix) }
    }

    val completedDaysCount = remember(monthLogs) {
        monthLogs.count { it.value.status == CompletionStatus.COMPLETED }
    }
    val graceDaysCount = remember(monthLogs) {
        monthLogs.count { it.value.status == CompletionStatus.PARTIAL }
    }

    val passedDaysInMonth = if (year == today.year && month == today.monthValue) {
        today.dayOfMonth
    } else if (YearMonth.of(year, month).isBefore(YearMonth.now())) {
        daysInMonth
    } else 0

    val monthRatio = if (passedDaysInMonth > 0) {
        (completedDaysCount.toFloat() + graceDaysCount * 0.5f) / passedDaysInMonth.toFloat()
    } else 0f

    // Max consecutive streak in this month
    var currentStreak = 0
    var maxStreak = 0
    for (d in 1..daysInMonth) {
        val dateIso = String.format(Locale.US, "%04d-%02d-%02d", year, month, d)
        val log = logsByDate[dateIso]
        if (log?.status == CompletionStatus.COMPLETED || log?.status == CompletionStatus.PARTIAL) {
            currentStreak++
            if (currentStreak > maxStreak) maxStreak = currentStreak
        } else {
            currentStreak = 0
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Month Navigation Header
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AetherSurfaceElevated),
                border = androidx.compose.foundation.BorderStroke(1.dp, AetherBorder.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            val prev = ym.minusMonths(1)
                            onMonthChange(prev.monthValue, prev.year)
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Mes anterior", tint = AetherTextPrimary)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$monthName $year",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = AetherTextPrimary
                        )
                        Text(
                            text = "${(monthRatio * 100).toInt()}% ${if (isSpanish) "Cumplimiento del mes" else "Monthly Completion"}",
                            style = MaterialTheme.typography.labelMedium,
                            color = getRatioColor(monthRatio),
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    IconButton(
                        onClick = {
                            val next = ym.plusMonths(1)
                            onMonthChange(next.monthValue, next.year)
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Mes siguiente", tint = AetherTextPrimary)
                    }
                }
            }
        }

        // Monthly KPI Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricKpiCard(
                    title = if (isSpanish) "Total Cumplido" else "Completed Total",
                    value = "$completedDaysCount d",
                    icon = Icons.Default.CheckCircle,
                    color = AetherEmerald,
                    modifier = Modifier.weight(1f)
                )
                MetricKpiCard(
                    title = if (isSpanish) "Racha del Mes" else "Month Streak",
                    value = "$maxStreak d",
                    icon = Icons.Default.LocalFireDepartment,
                    color = AetherAmber,
                    modifier = Modifier.weight(1f)
                )
                MetricKpiCard(
                    title = if (isSpanish) "Gracia Usada" else "Grace Used",
                    value = "$graceDaysCount d",
                    icon = Icons.Default.Shield,
                    color = AetherPurple,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Heatmap Legend
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendItem(color = AetherEmerald, label = if (isSpanish) "Cumplido" else "Done")
                LegendItem(color = AetherAmber, label = if (isSpanish) "Gracia" else "Grace")
                LegendItem(color = AetherCoral, label = if (isSpanish) "Perdido" else "Missed")
                LegendItem(color = AetherSurfaceCard.copy(alpha = 0.5f), label = if (isSpanish) "Sin registro" else "Empty")
            }
        }

        // Calendar Grid of Squares
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AetherSurfaceElevated),
                border = androidx.compose.foundation.BorderStroke(1.dp, AetherBorder.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Header row for Days: L, M, X, J, V, S, D
                    val daysHeader = if (isSpanish) listOf("L", "M", "X", "J", "V", "S", "D") else listOf("M", "T", "W", "T", "F", "S", "S")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        daysHeader.forEach { dayLetter ->
                            Text(
                                text = dayLetter,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = AetherTextMuted,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // First day of month offset (1 = Monday, 7 = Sunday)
                    val firstDayOfWeek = LocalDate.of(year, month, 1).dayOfWeek.value
                    val offset = firstDayOfWeek - 1 // 0 for Monday

                    val totalCells = offset + daysInMonth
                    val rows = (totalCells + 6) / 7

                    for (r in 0 until rows) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            for (c in 0 until 7) {
                                val cellIndex = r * 7 + c
                                val dayNum = cellIndex - offset + 1
                                if (dayNum in 1..daysInMonth) {
                                    val dateIso = String.format(Locale.US, "%04d-%02d-%02d", year, month, dayNum)
                                    val thisDate = LocalDate.of(year, month, dayNum)
                                    val isToday = thisDate == today
                                    val isFuture = thisDate.isAfter(today)
                                    val log = logsByDate[dateIso]
                                    val isSelected = selectedDateIso == dateIso

                                    val (cellColor, statusIcon) = when {
                                        log?.status == CompletionStatus.COMPLETED -> Pair(AetherEmerald, Icons.Default.Check)
                                        log?.status == CompletionStatus.PARTIAL -> Pair(AetherAmber, Icons.Default.Shield)
                                        log?.status == CompletionStatus.MISSED -> Pair(AetherCoral, Icons.Default.Close)
                                        isFuture -> Pair(AetherSurfaceCard.copy(alpha = 0.3f), null)
                                        else -> Pair(AetherSurfaceCard.copy(alpha = 0.6f), null)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .padding(2.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(cellColor)
                                            .then(
                                                if (isSelected) Modifier.border(2.dp, Color.White, RoundedCornerShape(8.dp))
                                                else if (isToday) Modifier.border(1.5.dp, typeColor, RoundedCornerShape(8.dp))
                                                else Modifier
                                            )
                                            .clickable { onSelectDate(dateIso) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = dayNum.toString(),
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                                color = if (log != null) Color.White else if (isFuture) AetherTextMuted.copy(alpha = 0.4f) else AetherTextSecondary
                                            )
                                            if (statusIcon != null) {
                                                Icon(
                                                    imageVector = statusIcon,
                                                    contentDescription = null,
                                                    tint = Color.White.copy(alpha = 0.9f),
                                                    modifier = Modifier.size(10.dp)
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Selected Day Details
        if (selectedDateIso != null) {
            val inspectedDate = try { LocalDate.parse(selectedDateIso) } catch (e: Exception) { today }
            val inspectedLog = logsByDate[selectedDateIso]

            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AetherSurfaceElevated),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AetherBorder.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${inspectedDate.dayOfWeek.getDisplayName(TextStyle.FULL, locale).replaceFirstChar { it.uppercase() }}, ${inspectedDate.dayOfMonth} de ${inspectedDate.month.getDisplayName(TextStyle.FULL, locale)}",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = AetherTextPrimary
                            )

                            if (inspectedLog != null) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = when (inspectedLog.status) {
                                        CompletionStatus.COMPLETED -> AetherEmerald.copy(alpha = 0.2f)
                                        CompletionStatus.PARTIAL -> AetherAmber.copy(alpha = 0.2f)
                                        CompletionStatus.MISSED -> AetherCoral.copy(alpha = 0.2f)
                                    }
                                ) {
                                    Text(
                                        text = when (inspectedLog.status) {
                                            CompletionStatus.COMPLETED -> if (isSpanish) "✓ Cumplido" else "✓ Completed"
                                            CompletionStatus.PARTIAL -> if (isSpanish) "🛡️ Gracia" else "🛡️ Grace"
                                            CompletionStatus.MISSED -> if (isSpanish) "✕ No Realizado" else "✕ Missed"
                                        },
                                        color = when (inspectedLog.status) {
                                            CompletionStatus.COMPLETED -> AetherEmerald
                                            CompletionStatus.PARTIAL -> AetherAmber
                                            CompletionStatus.MISSED -> AetherCoral
                                        },
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }

                        if (inspectedLog != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            val timeStr = remember(inspectedLog.timestamp) {
                                val instant = java.time.Instant.ofEpochMilli(inspectedLog.timestamp)
                                val zdt = instant.atZone(java.time.ZoneId.systemDefault())
                                String.format(Locale.US, "%02d:%02d", zdt.hour, zdt.minute)
                            }
                            Text(
                                text = if (isSpanish) "Registrado a las $timeStr" else "Recorded at $timeStr",
                                style = MaterialTheme.typography.bodySmall,
                                color = AetherTextSecondary
                            )
                        } else {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (inspectedDate.isAfter(today)) (if (isSpanish) "Día futuro sin completar" else "Future day") else (if (isSpanish) "Sin registro para este día" else "No completion logged"),
                                style = MaterialTheme.typography.bodySmall,
                                color = AetherTextMuted
                            )
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// LEVEL 3: INDIVIDUAL YEAR VIEW (12 MONTH CARDS WITH MINI HEATMAP SQUARES)
// -------------------------------------------------------------------------------------------------
@Composable
fun IndividualYearHistoryView(
    target: IndividualHistoryTarget,
    logsByDate: Map<String, CompletionLog>,
    year: Int,
    isSpanish: Boolean,
    locale: Locale,
    typeColor: Color,
    onSelectMonth: (year: Int, month: Int) -> Unit,
    onYearChange: (newYear: Int) -> Unit
) {
    val currentYear = LocalDate.now().year

    // Year completions count
    val yearPrefix = "$year-"
    val yearLogs = remember(logsByDate, yearPrefix) {
        logsByDate.filter { it.key.startsWith(yearPrefix) }
    }
    val totalYearCompleted = remember(yearLogs) {
        yearLogs.count { it.value.status == CompletionStatus.COMPLETED }
    }
    val totalYearGrace = remember(yearLogs) {
        yearLogs.count { it.value.status == CompletionStatus.PARTIAL }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Year Navigation Header
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = AetherSurfaceElevated),
            border = androidx.compose.foundation.BorderStroke(1.dp, AetherBorder.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onYearChange(year - 1) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Año anterior", tint = AetherTextPrimary)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$year",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = AetherTextPrimary
                    )
                    Text(
                        text = "$totalYearCompleted ${if (isSpanish) "días completados en el año" else "completed days this year"}",
                        style = MaterialTheme.typography.labelMedium,
                        color = AetherEmerald,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                IconButton(onClick = { onYearChange(year + 1) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Año siguiente", tint = AetherTextPrimary)
                }
            }
        }

        // 12 Months Grid with Mini-Heatmap Squares
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 145.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items((1..12).toList()) { month ->
                val ym = YearMonth.of(year, month)
                val monthName = ym.month.getDisplayName(TextStyle.SHORT, locale).replaceFirstChar { it.uppercase() }
                val daysInMonth = ym.lengthOfMonth()
                val prefix = String.format(Locale.US, "%04d-%02d", year, month)
                val monthLogsCount = logsByDate.filter { it.key.startsWith(prefix) && it.value.status == CompletionStatus.COMPLETED }.size

                IndividualMonthMiniHeatmapCard(
                    monthName = monthName,
                    monthNumber = month,
                    year = year,
                    daysInMonth = daysInMonth,
                    completedCount = monthLogsCount,
                    logsByDate = logsByDate,
                    onClick = { onSelectMonth(year, month) }
                )
            }
        }
    }
}

@Composable
fun IndividualMonthMiniHeatmapCard(
    monthName: String,
    monthNumber: Int,
    year: Int,
    daysInMonth: Int,
    completedCount: Int,
    logsByDate: Map<String, CompletionLog>,
    onClick: () -> Unit
) {
    val today = remember { LocalDate.now() }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = AetherSurfaceElevated),
        border = androidx.compose.foundation.BorderStroke(1.dp, AetherBorder.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = monthName,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = AetherTextPrimary
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (completedCount > 0) AetherEmerald.copy(alpha = 0.2f) else AetherSurfaceCard
                ) {
                    Text(
                        text = "$completedCount d",
                        color = if (completedCount > 0) AetherEmerald else AetherTextMuted,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp),
                        fontSize = 10.sp
                    )
                }
            }

            // Mini Heatmap Squares (7 columns x total weeks)
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                val totalWeeks = (daysInMonth + 6) / 7
                for (w in 0 until totalWeeks) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (d in 1..7) {
                            val dayNum = w * 7 + d
                            if (dayNum <= daysInMonth) {
                                val dateIso = String.format(Locale.US, "%04d-%02d-%02d", year, monthNumber, dayNum)
                                val log = logsByDate[dateIso]
                                val date = LocalDate.of(year, monthNumber, dayNum)
                                val isFuture = date.isAfter(today)

                                val color = when {
                                    log?.status == CompletionStatus.COMPLETED -> AetherEmerald
                                    log?.status == CompletionStatus.PARTIAL -> AetherAmber
                                    log?.status == CompletionStatus.MISSED -> AetherCoral.copy(alpha = 0.6f)
                                    isFuture -> AetherSurfaceCard.copy(alpha = 0.2f)
                                    else -> AetherSurfaceCard.copy(alpha = 0.45f)
                                }

                                Box(
                                    modifier = Modifier
                                        .size(9.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(color)
                                )
                            } else {
                                Spacer(modifier = Modifier.size(9.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

