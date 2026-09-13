package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.DailySummaryEntity
import com.example.data.model.*
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.StringsProvider
import com.example.ui.theme.*
import com.example.ui.viewmodel.AetherUiState
import com.example.ui.viewmodel.AetherViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryDialog(
    state: AetherUiState,
    viewModel: AetherViewModel,
    onDismiss: () -> Unit
) {
    val strings = remember(state.currentLanguage) { StringsProvider(state.currentLanguage) }
    val isSpanish = state.currentLanguage == AppLanguage.SPANISH
    val locale = if (isSpanish) Locale.forLanguageTag("es-ES") else Locale.ENGLISH

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = strings.historyHeaderTitle,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = when (state.historyViewMode) {
                                    HistoryViewMode.YEAR -> "${strings.historyViewYear}: ${state.selectedHistoryYear}"
                                    HistoryViewMode.MONTH -> "${strings.historyViewMonth}: ${
                                        YearMonth.of(state.selectedHistoryYear, state.selectedHistoryMonth)
                                            .month.getDisplayName(TextStyle.FULL, locale).replaceFirstChar { it.uppercase() }
                                    } ${state.selectedHistoryYear}"
                                    HistoryViewMode.DAY -> "${strings.historyViewDay}: ${state.selectedHistoryDateIso}"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                when (state.historyViewMode) {
                                    HistoryViewMode.DAY -> viewModel.setHistoryViewMode(HistoryViewMode.MONTH)
                                    HistoryViewMode.MONTH -> viewModel.setHistoryViewMode(HistoryViewMode.YEAR)
                                    HistoryViewMode.YEAR -> onDismiss()
                                }
                            },
                            modifier = Modifier.testTag("history_back_button")
                        ) {
                            Icon(
                                imageVector = if (state.historyViewMode == HistoryViewMode.YEAR) Icons.Default.Close else Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = strings.btnClose
                            )
                        }
                    },
                    actions = {
                        // Navigation chips between modes
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            FilterChip(
                                selected = state.historyViewMode == HistoryViewMode.YEAR,
                                onClick = { viewModel.setHistoryViewMode(HistoryViewMode.YEAR) },
                                label = { Text("Año", fontSize = 11.sp) }
                            )
                            FilterChip(
                                selected = state.historyViewMode == HistoryViewMode.MONTH,
                                onClick = { viewModel.setHistoryViewMode(HistoryViewMode.MONTH) },
                                label = { Text("Mes", fontSize = 11.sp) }
                            )
                            FilterChip(
                                selected = state.historyViewMode == HistoryViewMode.DAY,
                                onClick = { viewModel.setHistoryViewMode(HistoryViewMode.DAY) },
                                label = { Text("Día", fontSize = 11.sp) }
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (state.historyViewMode) {
                    HistoryViewMode.YEAR -> {
                        YearHistoryView(
                            state = state,
                            strings = strings,
                            locale = locale,
                            onSelectMonth = { year, month ->
                                viewModel.selectHistoryMonth(year, month)
                            },
                            onSelectYear = { year ->
                                viewModel.selectHistoryYear(year)
                            },
                            onSelectDate = { dateIso ->
                                viewModel.selectHistoryDate(dateIso)
                            }
                        )
                    }
                    HistoryViewMode.MONTH -> {
                        MonthHistoryView(
                            state = state,
                            strings = strings,
                            locale = locale,
                            onSelectDate = { dateIso ->
                                viewModel.selectHistoryDate(dateIso)
                            },
                            onMonthChange = { newMonth, newYear ->
                                viewModel.selectHistoryMonth(newYear, newMonth)
                            }
                        )
                    }
                    HistoryViewMode.DAY -> {
                        DayHistoryView(
                            state = state,
                            strings = strings,
                            locale = locale,
                            onBackToMonth = {
                                viewModel.setHistoryViewMode(HistoryViewMode.MONTH)
                            }
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// LEVEL 1: YEAR VIEW (12 MONTH CARDS WITH REAL CALENDAR ALIGNMENT + 52-WEEK HEATMAP)
// -------------------------------------------------------------------------------------------------
@Composable
fun YearHistoryView(
    state: AetherUiState,
    strings: StringsProvider,
    locale: Locale,
    onSelectMonth: (year: Int, month: Int) -> Unit,
    onSelectYear: (year: Int) -> Unit,
    onSelectDate: (dateIso: String) -> Unit = {}
) {
    val currentYear = LocalDate.now().year
    val isSpanish = locale.language == "es"
    val availableYears = remember(state.historySummaries) {
        val yearsInDb = state.historySummaries.mapNotNull {
            it.dateIso.split("-").getOrNull(0)?.toIntOrNull()
        }.toSet()
        val all = (yearsInDb + setOf(currentYear - 1, currentYear, currentYear + 1)).sorted()
        all
    }

    val yearPrefix = "${state.selectedHistoryYear}-"
    val yearSummaries = remember(state.historySummaries, yearPrefix) {
        state.historySummaries.filter { it.dateIso.startsWith(yearPrefix) }
    }
    val activeDaysInYear = remember(yearSummaries) { yearSummaries.count { it.completedCount > 0 } }
    val avgYearRatio = remember(yearSummaries) {
        if (yearSummaries.isNotEmpty()) yearSummaries.map { it.ratio }.average().toFloat() else 0f
    }

    var subViewMode by remember { mutableStateOf(0) } // 0 = 12 Meses Calendario, 1 = Muro 52 Semanas

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Horizontal Year Selector & Navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = strings.historySelectYear,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            // Year KPI Quick Pill
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            ) {
                Text(
                    text = "$activeDaysInYear ${strings.historyActiveDaysYear} • ${(avgYearRatio * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        // Year Chips Carousel
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(availableYears) { year ->
                val isSelected = year == state.selectedHistoryYear
                ElevatedFilterChip(
                    selected = isSelected,
                    onClick = { onSelectYear(year) },
                    label = {
                        Text(
                            text = year.toString(),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    leadingIcon = if (isSelected) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null,
                    colors = FilterChipDefaults.elevatedFilterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }

        // Sub-view mode selector: 12 Meses vs Muro Anual 52 Semanas
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = subViewMode == 0,
                onClick = { subViewMode = 0 },
                leadingIcon = {
                    Icon(
                        Icons.Default.CalendarViewMonth,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                label = { Text(strings.historySubYearMonths) },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = subViewMode == 1,
                onClick = { subViewMode = 1 },
                leadingIcon = {
                    Icon(
                        Icons.Default.ViewWeek,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                label = { Text(strings.historySubYearTimeline) },
                modifier = Modifier.weight(1f)
            )
        }

        if (subViewMode == 0) {
            // 12 Months Grid with True Calendar Alignment
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 155.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items((1..12).toList()) { month ->
                    val ym = YearMonth.of(state.selectedHistoryYear, month)
                    val monthName = ym.month.getDisplayName(TextStyle.FULL, locale).replaceFirstChar { it.uppercase() }
                    val prefix = String.format(Locale.US, "%04d-%02d", state.selectedHistoryYear, month)
                    val monthSummaries = state.historySummaries.filter { it.dateIso.startsWith(prefix) }
                    
                    val avgRatio = if (monthSummaries.isNotEmpty()) {
                        monthSummaries.map { it.ratio }.average().toFloat()
                    } else 0f

                    MonthSummaryCard(
                        monthName = monthName,
                        monthNumber = month,
                        year = state.selectedHistoryYear,
                        summaries = monthSummaries,
                        avgRatio = avgRatio,
                        isSpanish = isSpanish,
                        onSelectDate = onSelectDate,
                        onClick = { onSelectMonth(state.selectedHistoryYear, month) }
                    )
                }
            }
        } else {
            // Continuous 52-Week Annual Heatmap Wall
            AnnualContinuousHeatmapCard(
                year = state.selectedHistoryYear,
                summaries = yearSummaries,
                strings = strings,
                isSpanish = isSpanish,
                onSelectDate = onSelectDate
            )
        }
    }
}

@Composable
fun MonthSummaryCard(
    monthName: String,
    monthNumber: Int,
    year: Int,
    summaries: List<DailySummary>,
    avgRatio: Float,
    isSpanish: Boolean = true,
    onSelectDate: ((String) -> Unit)? = null,
    onClick: () -> Unit
) {
    val ym = remember(year, monthNumber) { YearMonth.of(year, monthNumber) }
    val daysInMonth = ym.lengthOfMonth()
    val summaryMap = remember(summaries) { summaries.associateBy { it.dateIso } }
    val todayIso = remember { LocalDate.now().toString() }
    val today = remember { LocalDate.now() }

    // First day of month offset (1 = Monday, 7 = Sunday)
    val firstDayOfWeek = remember(year, monthNumber) {
        LocalDate.of(year, monthNumber, 1).dayOfWeek.value
    }
    val offset = firstDayOfWeek - 1 // 0 for Monday, 6 for Sunday
    val totalCells = offset + daysInMonth
    val totalRows = (totalCells + 6) / 7

    val weekdayHeaders = remember(isSpanish) {
        if (isSpanish) listOf("L", "M", "X", "J", "V", "S", "D")
        else listOf("M", "T", "W", "T", "F", "S", "S")
    }

    val activeDaysCount = remember(summaries) { summaries.count { it.completedCount > 0 } }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header: Month name + completed days and % pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = monthName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (activeDaysCount > 0) {
                        Text(
                            text = "$activeDaysCount/$daysInMonth d",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            fontSize = 10.sp
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (summaries.isNotEmpty()) getRatioColor(avgRatio).copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
                ) {
                    Text(
                        text = if (summaries.isNotEmpty()) "${(avgRatio * 100).toInt()}%" else "—",
                        color = if (summaries.isNotEmpty()) getRatioColor(avgRatio) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                        fontSize = 11.sp
                    )
                }
            }

            // Weekday Headers: L M X J V S D with Weekend Highlighting
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                weekdayHeaders.forEachIndexed { index, letter ->
                    val isWeekend = index >= 5
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 1.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                if (isWeekend) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.14f)
                                else Color.Transparent
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = letter,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isWeekend) FontWeight.ExtraBold else FontWeight.Medium
                            ),
                            color = if (isWeekend) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(vertical = 1.5.dp)
                        )
                    }
                }
            }

            // Real Calendar Days Grid (Properly offset by firstDayOfWeek)
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                for (r in 0 until totalRows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (c in 0 until 7) {
                            val cellIndex = r * 7 + c
                            val dayNum = cellIndex - offset + 1
                            val isWeekend = c >= 5

                            if (dayNum in 1..daysInMonth) {
                                val dateIso = String.format(Locale.US, "%04d-%02d-%02d", year, monthNumber, dayNum)
                                val summary = summaryMap[dateIso]
                                val isToday = dateIso == todayIso
                                val cellDate = LocalDate.of(year, monthNumber, dayNum)
                                val isFuture = cellDate.isAfter(today)

                                val cellColor = when {
                                    summary != null && summary.ratio > 0f -> getRatioColor(summary.ratio)
                                    summary != null && summary.ratio == 0f && summary.totalCount > 0 -> Color(0xFFEF4444).copy(alpha = 0.6f)
                                    isFuture -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f)
                                    isWeekend -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.08f)
                                    else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .padding(1.5.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(cellColor)
                                        .then(
                                            if (isToday) Modifier.border(1.2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(3.dp))
                                            else if (isWeekend && (summary == null || summary.ratio == 0f) && !isFuture) {
                                                Modifier.border(0.6.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.25f), RoundedCornerShape(3.dp))
                                            } else Modifier
                                        )
                                        .clickable {
                                            if (onSelectDate != null) onSelectDate(dateIso)
                                            else onClick()
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isToday) {
                                        Box(
                                            modifier = Modifier
                                                .size(3.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary)
                                        )
                                    }
                                }
                            } else {
                                Spacer(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .padding(1.5.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnnualContinuousHeatmapCard(
    year: Int,
    summaries: List<DailySummary>,
    strings: StringsProvider,
    isSpanish: Boolean,
    onSelectDate: (String) -> Unit
) {
    val summaryMap = remember(summaries) { summaries.associateBy { it.dateIso } }
    val todayIso = remember { LocalDate.now().toString() }
    val today = remember { LocalDate.now() }
    
    // We compute 53 weeks starting from the Monday of the first week of the year
    val jan1 = remember(year) { LocalDate.of(year, 1, 1) }
    val firstMonday = remember(jan1) {
        val dow = jan1.dayOfWeek.value // 1 = Mon, 7 = Sun
        jan1.minusDays((dow - 1).toLong())
    }
    
    var hoveredDateIso by remember { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isSpanish) "Muro Anual de Consistencia (52 Semanas)" else "Annual Consistency Wall (52 Weeks)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isSpanish) "Ritmo continuo de lunes a domingo • Fines de semana destacados" 
                               else "Continuous Monday-Sunday rhythm • Weekends highlighted",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Interactive date banner if a square is selected
            if (hoveredDateIso != null) {
                val hoveredSummary = summaryMap[hoveredDateIso]
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📅 $hoveredDateIso: ${if (hoveredSummary != null) "${hoveredSummary.completedCount}/${hoveredSummary.totalCount} completadas (${(hoveredSummary.ratio * 100).toInt()}%)" else if (isSpanish) "Sin registros" else "No entries"}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        TextButton(
                            onClick = { onSelectDate(hoveredDateIso!!) },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(if (isSpanish) "Ver día →" else "View day →", fontSize = 11.sp)
                        }
                    }
                }
            }

            // Horizontally scrollable heatmap
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
                    .padding(vertical = 4.dp)
            ) {
                // Fixed Left labels column for days of week
                Column(
                    modifier = Modifier.padding(end = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    // Spacer to match month headers height
                    Spacer(modifier = Modifier.height(16.dp))
                    val rowDayLabels = if (isSpanish) listOf("L", "M", "X", "J", "V", "S", "D") else listOf("M", "T", "W", "T", "F", "S", "S")
                    rowDayLabels.forEachIndexed { idx, label ->
                        val isWeekend = idx >= 5
                        Box(
                            modifier = Modifier.size(13.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 8.sp,
                                fontWeight = if (isWeekend) FontWeight.Bold else FontWeight.Normal,
                                color = if (isWeekend) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                // 53 week columns
                for (w in 0 until 53) {
                    val weekMonday = firstMonday.plusWeeks(w.toLong())
                    
                    // Check if a new month starts in this week to render month label
                    val monthLabel = (0..6).map { weekMonday.plusDays(it.toLong()) }
                        .firstOrNull { it.year == year && it.dayOfMonth == 1 }
                        ?.let {
                            if (isSpanish) when (it.monthValue) {
                                1 -> "Ene"; 2 -> "Feb"; 3 -> "Mar"; 4 -> "Abr"; 5 -> "May"; 6 -> "Jun"
                                7 -> "Jul"; 8 -> "Ago"; 9 -> "Sep"; 10 -> "Oct"; 11 -> "Nov"; else -> "Dic"
                            } else when (it.monthValue) {
                                1 -> "Jan"; 2 -> "Feb"; 3 -> "Mar"; 4 -> "Apr"; 5 -> "May"; 6 -> "Jun"
                                7 -> "Jul"; 8 -> "Aug"; 9 -> "Sep"; 10 -> "Oct"; 11 -> "Nov"; else -> "Dec"
                            }
                        }

                    Column(
                        modifier = Modifier.padding(horizontal = 1.5.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        // Month label cell
                        Box(
                            modifier = Modifier
                                .width(13.dp)
                                .height(16.dp),
                            contentAlignment = Alignment.BottomStart
                        ) {
                            if (monthLabel != null) {
                                Text(
                                    text = monthLabel,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }

                        // 7 Day cells in this week (Mon to Sun)
                        for (d in 0..6) {
                            val currentDay = weekMonday.plusDays(d.toLong())
                            val isInYear = currentDay.year == year
                            val dateIso = currentDay.toString()
                            val summary = summaryMap[dateIso]
                            val isWeekend = d >= 5
                            val isToday = dateIso == todayIso
                            val isFuture = currentDay.isAfter(today)

                            if (isInYear) {
                                val cellColor = when {
                                    summary != null && summary.ratio > 0f -> getRatioColor(summary.ratio)
                                    summary != null && summary.ratio == 0f && summary.totalCount > 0 -> Color(0xFFEF4444).copy(alpha = 0.6f)
                                    isFuture -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f)
                                    isWeekend -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.09f)
                                    else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.09f)
                                }

                                Box(
                                    modifier = Modifier
                                        .size(13.dp)
                                        .clip(RoundedCornerShape(2.5.dp))
                                        .background(cellColor)
                                        .then(
                                            if (isToday) Modifier.border(1.2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(2.5.dp))
                                            else if (isWeekend && !isFuture && (summary == null || summary.ratio == 0f)) {
                                                Modifier.border(0.5.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f), RoundedCornerShape(2.5.dp))
                                            } else Modifier
                                        )
                                        .clickable {
                                            hoveredDateIso = dateIso
                                        }
                                )
                            } else {
                                Spacer(modifier = Modifier.size(13.dp))
                            }
                        }
                    }
                }
            }

            // Heatmap Legend and Weekend Callout
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isSpanish) "Menos" else "Less",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)))
                    Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFFEF4444).copy(alpha = 0.7f)))
                    Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFFF59E0B)))
                    Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFF10B981)))
                    Text(
                        text = if (isSpanish) "Más" else "More",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = if (isSpanish) "S & D = Fin de semana" else "S & S = Weekend",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// LEVEL 2: MONTH VIEW (CALENDAR GRID + HEATMAP + METRICS)
// -------------------------------------------------------------------------------------------------
@Composable
fun MonthHistoryView(
    state: AetherUiState,
    strings: StringsProvider,
    locale: Locale,
    onSelectDate: (dateIso: String) -> Unit,
    onMonthChange: (newMonth: Int, newYear: Int) -> Unit
) {
    val ym = remember(state.selectedHistoryYear, state.selectedHistoryMonth) {
        YearMonth.of(state.selectedHistoryYear, state.selectedHistoryMonth)
    }
    val monthName = ym.month.getDisplayName(TextStyle.FULL, locale).replaceFirstChar { it.uppercase() }
    val prefix = String.format(Locale.US, "%04d-%02d", state.selectedHistoryYear, state.selectedHistoryMonth)
    val monthSummaries = remember(state.historySummaries, prefix) {
        state.historySummaries.filter { it.dateIso.startsWith(prefix) }
    }
    val summaryMap = remember(monthSummaries) { monthSummaries.associateBy { it.dateIso } }

    // Metrics computation
    val totalCompleted = monthSummaries.sumOf { it.completedCount }
    val avgRatio = if (monthSummaries.isNotEmpty()) monthSummaries.map { it.ratio }.average().toFloat() else 0f
    val bestDay = monthSummaries.maxByOrNull { it.ratio }
    val bestDayStr = if (bestDay != null) "${bestDay.dateIso.takeLast(2)} (${(bestDay.ratio * 100).toInt()}%)" else "—"

    // Max streak in this month
    val daysInMonth = ym.lengthOfMonth()
    var currentStreak = 0
    var maxStreak = 0
    for (d in 1..daysInMonth) {
        val dateIso = String.format(Locale.US, "%04d-%02d-%02d", state.selectedHistoryYear, state.selectedHistoryMonth, d)
        val s = summaryMap[dateIso]
        if (s != null && s.ratio >= 0.7f) {
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
        // Month Header with Prev/Next
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Mes anterior")
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$monthName ${state.selectedHistoryYear}",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${(avgRatio * 100).toInt()}% ${strings.historyAvgCompletion}",
                            style = MaterialTheme.typography.labelMedium,
                            color = getRatioColor(avgRatio)
                        )
                    }

                    IconButton(
                        onClick = {
                            val next = ym.plusMonths(1)
                            onMonthChange(next.monthValue, next.year)
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Mes siguiente")
                    }
                }
            }
        }

        // Metrics Banner (4 KPI boxes)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricKpiCard(
                    title = strings.historyTotalCompleted,
                    value = totalCompleted.toString(),
                    icon = Icons.Default.CheckCircle,
                    color = Color(0xFF10B981),
                    modifier = Modifier.weight(1f)
                )
                MetricKpiCard(
                    title = strings.historyMaxStreak,
                    value = "$maxStreak d",
                    icon = Icons.Default.LocalFireDepartment,
                    color = Color(0xFFF59E0B),
                    modifier = Modifier.weight(1f)
                )
                MetricKpiCard(
                    title = strings.historyBestDay,
                    value = bestDayStr,
                    icon = Icons.Default.Star,
                    color = Color(0xFF3B82F6),
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
                LegendItem(color = Color(0xFF10B981), label = strings.historyLegendGreen)
                LegendItem(color = Color(0xFFF59E0B), label = strings.historyLegendAmber)
                LegendItem(color = Color(0xFFEF4444), label = strings.historyLegendRed)
                LegendItem(color = Color.Gray.copy(alpha = 0.3f), label = strings.historyLegendEmpty)
            }
        }

        // Calendar Grid
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Day of week headers: L, M, X, J, V, S, D
                    val daysHeader = listOf("L", "M", "X", "J", "V", "S", "D")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        daysHeader.forEach { dayLetter ->
                            Text(
                                text = dayLetter,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // First day of month offset (1 = Monday, 7 = Sunday)
                    val firstDayOfWeek = LocalDate.of(state.selectedHistoryYear, state.selectedHistoryMonth, 1).dayOfWeek.value
                    val offset = firstDayOfWeek - 1 // 0 for Mon, 6 for Sun

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
                                    val dateIso = String.format(Locale.US, "%04d-%02d-%02d", state.selectedHistoryYear, state.selectedHistoryMonth, dayNum)
                                    val summary = summaryMap[dateIso]
                                    val ratio = summary?.ratio ?: -1f
                                    val cellColor = if (summary != null) getRatioColor(ratio) else Color.Gray.copy(alpha = 0.15f)

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .padding(2.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(cellColor.copy(alpha = if (summary != null) 0.85f else 0.3f))
                                            .clickable { onSelectDate(dateIso) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = dayNum.toString(),
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                                color = if (summary != null) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            if (summary != null) {
                                                Text(
                                                    text = "${(ratio * 100).toInt()}%",
                                                    fontSize = 8.sp,
                                                    color = Color.White.copy(alpha = 0.9f)
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

        // Tap day hint
        item {
            Text(
                text = strings.historyTapDayHint,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun MetricKpiCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f)),
        modifier = modifier.border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// -------------------------------------------------------------------------------------------------
// LEVEL 3: DAY VIEW (COMPLETION BREAKDOWN WITH HOURLY TIMESTAMPS)
// -------------------------------------------------------------------------------------------------
@Composable
fun DayHistoryView(
    state: AetherUiState,
    strings: StringsProvider,
    locale: Locale,
    onBackToMonth: () -> Unit
) {
    val dateIso = state.selectedHistoryDateIso
    val date = remember(dateIso) {
        try { LocalDate.parse(dateIso) } catch (e: Exception) { LocalDate.now() }
    }
    val dateFormatted = remember(date, locale) {
        val d = date.dayOfWeek.getDisplayName(TextStyle.FULL, locale).replaceFirstChar { it.uppercase() }
        val m = date.month.getDisplayName(TextStyle.FULL, locale).replaceFirstChar { it.uppercase() }
        "$d, ${date.dayOfMonth} de $m de ${date.year}"
    }

    val logs = state.historyLogsForSelectedDay
    val completedLogs = remember(logs) { logs.filter { it.status == CompletionStatus.COMPLETED } }
    val partialLogs = remember(logs) { logs.filter { it.status == CompletionStatus.PARTIAL } }
    val missedLogs = remember(logs) { logs.filter { it.status == CompletionStatus.MISSED } }

    val nonTaskLogs = remember(logs) { logs.filter { it.itemType != CompletionItemType.TASK } }
    val nonTaskCompleted = remember(completedLogs) { completedLogs.filter { it.itemType != CompletionItemType.TASK } }
    val nonTaskPartial = remember(partialLogs) { partialLogs.filter { it.itemType != CompletionItemType.TASK } }

    val daySummary = state.historySummaries.find { it.dateIso == dateIso }
    val ratio = daySummary?.ratio ?: if (nonTaskLogs.isNotEmpty()) {
        ((nonTaskCompleted.size.toFloat() + nonTaskPartial.size.toFloat() * 0.5f) / nonTaskLogs.size.toFloat()).coerceIn(0f, 1f)
    } else 0f

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Date Header Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = dateFormatted,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${strings.historyAvgCompletion}: ${(ratio * 100).toInt()}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = getRatioColor(ratio),
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = getRatioColor(ratio).copy(alpha = 0.2f),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "${(ratio * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = getRatioColor(ratio)
                                )
                            }
                        }
                    }

                    LinearProgressIndicator(
                        progress = { ratio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = getRatioColor(ratio),
                        trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                }
            }
        }

        if (logs.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth().padding(top = 24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EventBusy,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = strings.historyNoDayLogs,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            // Completed Items Section
            if (completedLogs.isNotEmpty()) {
                item {
                    Text(
                        text = "${strings.historyCompletedSection} (${completedLogs.size})",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF10B981)
                    )
                }
                items(completedLogs) { log ->
                    CompletionLogItemCard(log = log, statusColor = Color(0xFF10B981), icon = Icons.Default.CheckCircle)
                }
            }

            // Grace / Partial Items Section
            if (partialLogs.isNotEmpty()) {
                item {
                    Text(
                        text = "${strings.historyGraceSection} (${partialLogs.size})",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFFF59E0B)
                    )
                }
                items(partialLogs) { log ->
                    CompletionLogItemCard(log = log, statusColor = Color(0xFFF59E0B), icon = Icons.Default.Shield)
                }
            }

            // Missed Items Section
            if (missedLogs.isNotEmpty()) {
                item {
                    Text(
                        text = "${strings.historyMissedSection} (${missedLogs.size})",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFFEF4444)
                    )
                }
                items(missedLogs) { log ->
                    CompletionLogItemCard(log = log, statusColor = Color(0xFFEF4444), icon = Icons.Default.Cancel)
                }
            }
        }
    }
}

@Composable
fun CompletionLogItemCard(
    log: CompletionLog,
    statusColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    val timeFormatted = remember(log.timestamp) {
        val instant = java.time.Instant.ofEpochMilli(log.timestamp)
        val zdt = instant.atZone(java.time.ZoneId.systemDefault())
        String.format(Locale.US, "%02d:%02d", zdt.hour, zdt.minute)
    }

    val typeBadge = when (log.itemType) {
        CompletionItemType.TASK -> "Tarea"
        CompletionItemType.HABIT -> "Hábito"
        CompletionItemType.MEAL -> "Nutrición"
        CompletionItemType.TIME_BLOCK -> "Bloque"
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = statusColor,
                modifier = Modifier.size(24.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = log.title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = typeBadge,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                    Text(
                        text = "• $timeFormatted",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

fun getRatioColor(ratio: Float): Color {
    return when {
        ratio >= 0.70f -> Color(0xFF10B981) // Green (>=70%)
        ratio >= 0.30f -> Color(0xFFF59E0B) // Amber (30-69%)
        ratio > 0.0f -> Color(0xFFEF4444)   // Red (<30%)
        else -> Color.Gray.copy(alpha = 0.4f)
    }
}
