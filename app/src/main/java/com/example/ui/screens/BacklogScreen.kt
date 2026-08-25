package com.example.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EnergyLevel
import com.example.data.model.PriorityType
import com.example.data.model.TaskItem
import com.example.ui.components.AetherSwipeToDismissContainer
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.FocusTimerCard
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.StringsProvider
import com.example.ui.theme.*
import com.example.ui.viewmodel.AetherUiState

@Composable
fun BacklogScreen(
    state: AetherUiState,
    onToggleTask: (TaskItem) -> Unit,
    onPromoteToFrog: (String) -> Unit,
    onDeleteTask: (TaskItem) -> Unit,
    onEditTask: (TaskItem) -> Unit = {},
    onMoveTask: (fromIndex: Int, toIndex: Int) -> Unit = { _, _ -> },
    onSetEnergyFilter: (EnergyLevel?) -> Unit,
    onSetSearchQuery: (String) -> Unit,
    onStartFocusTimer: (TaskItem?) -> Unit,
    onPauseFocusTimer: () -> Unit,
    onResetFocusTimer: () -> Unit,
    onOpenQuickAdd: () -> Unit,
    onOpenHistory: () -> Unit = {},
    onPermissionDenied: () -> Unit = {},
    onAddQuickNote: (String) -> Unit = {},
    onDeleteQuickNote: (String) -> Unit = {},
    onConvertQuickNoteToTask: (com.example.data.model.QuickNoteItem) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val strings = remember(state.currentLanguage) { StringsProvider(state.currentLanguage) }
    val isSpanish = state.currentLanguage == AppLanguage.SPANISH
    var onlyPending by remember { mutableStateOf(false) }

    val filteredTasks = state.tasks.filter { task ->
        val matchesEnergy = state.filterEnergyLevel == null || task.energyLevel == state.filterEnergyLevel
        val matchesPending = !onlyPending || !task.isCompleted
        val matchesQuery = state.searchQuery.isBlank() ||
                task.title.contains(state.searchQuery, ignoreCase = true) ||
                task.description.contains(state.searchQuery, ignoreCase = true) ||
                task.category.contains(state.searchQuery, ignoreCase = true)
        matchesEnergy && matchesPending && matchesQuery
    }

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
                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Text(
                        text = strings.backlogHeader,
                        style = MaterialTheme.typography.titleLarge,
                        color = AetherCyan,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = strings.backlogSub,
                        style = MaterialTheme.typography.labelSmall,
                        color = AetherTextMuted
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onOpenHistory,
                        modifier = Modifier.testTag("backlog_history_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = strings.historyTitle,
                            tint = AetherCyan
                        )
                    }

                    FilledTonalButton(
                        onClick = onOpenQuickAdd,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = AetherCyan,
                            contentColor = Color(0xFF00363D)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("backlog_capture_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(strings.btnCapture, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // Quick Notes / Brain Dump Inbox
        item {
            com.example.ui.components.QuickNotesInboxCard(
                notes = state.quickNotes,
                language = state.currentLanguage,
                onAddNote = onAddQuickNote,
                onDeleteNote = onDeleteQuickNote,
                onConvertToTask = onConvertQuickNoteToTask
            )
        }

        // Focus Timer Module
        item {
            FocusTimerCard(
                isRunning = state.isFocusTimerRunning,
                secondsRemaining = state.focusSecondsRemaining,
                activeTask = state.activeFocusTask,
                onStart = { onStartFocusTimer(state.activeFocusTask) },
                onPause = onPauseFocusTimer,
                onReset = onResetFocusTimer,
                currentRound = state.currentPomodoroRound,
                pomodoroPhase = state.pomodoroPhase,
                totalFocusMinutes = state.totalFocusMinutes,
                language = state.currentLanguage,
                onPermissionDenied = onPermissionDenied
            )
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = onSetSearchQuery,
                placeholder = { Text(strings.searchTasksPlaceholder) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = AetherTextSecondary)
                },
                trailingIcon = {
                    if (state.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSetSearchQuery("") }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Clear", tint = AetherTextSecondary)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AetherCyan,
                    unfocusedBorderColor = AetherBorder,
                    focusedContainerColor = AetherSurfaceCard,
                    unfocusedContainerColor = AetherSurfaceCard
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("backlog_search_input")
            )
        }

        // Energy Filters Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = state.filterEnergyLevel == null,
                    onClick = { onSetEnergyFilter(null) },
                    label = { Text("${strings.filterAll} (${state.tasks.size})", fontSize = 11.sp) },
                    modifier = Modifier.weight(1f)
                )

                FilterChip(
                    selected = state.filterEnergyLevel == EnergyLevel.HIGH,
                    onClick = { onSetEnergyFilter(EnergyLevel.HIGH) },
                    label = { Text(strings.filterHigh, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AetherCoral.copy(alpha = 0.2f),
                        selectedLabelColor = AetherCoral
                    ),
                    modifier = Modifier.weight(1f)
                )

                FilterChip(
                    selected = state.filterEnergyLevel == EnergyLevel.MEDIUM,
                    onClick = { onSetEnergyFilter(EnergyLevel.MEDIUM) },
                    label = { Text(strings.filterMed, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AetherAmber.copy(alpha = 0.2f),
                        selectedLabelColor = AetherAmber
                    ),
                    modifier = Modifier.weight(1f)
                )

                FilterChip(
                    selected = state.filterEnergyLevel == EnergyLevel.LOW,
                    onClick = { onSetEnergyFilter(EnergyLevel.LOW) },
                    label = { Text(strings.filterLow, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AetherEmerald.copy(alpha = 0.2f),
                        selectedLabelColor = AetherEmerald
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Sub-filters & Results Counter Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = onlyPending,
                    onClick = { onlyPending = !onlyPending },
                    leadingIcon = {
                        Icon(
                            imageVector = if (onlyPending) Icons.Default.Check else Icons.Default.FilterList,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    },
                    label = {
                        Text(
                            text = if (isSpanish) "Solo Pendientes" else "Only Pending",
                            fontSize = 11.sp
                        )
                    }
                )

                Text(
                    text = if (isSpanish) {
                        "Mostrando ${filteredTasks.size} de ${state.tasks.size} tareas"
                    } else {
                        "Showing ${filteredTasks.size} of ${state.tasks.size} tasks"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = AetherTextMuted,
                    fontSize = 11.sp
                )
            }
        }

        // Task Items List
        if (filteredTasks.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Default.Bolt,
                    iconTint = MaterialTheme.colorScheme.primary,
                    title = if (state.tasks.isEmpty()) 
                        (if (isSpanish) "Bandeja de Tareas Vacía" else "Task Backlog Clean")
                    else 
                        (if (isSpanish) "Sin Resultados con este Filtro" else "No Tasks Match This Filter"),
                    description = if (state.tasks.isEmpty()) strings.emptyBacklogClean else strings.emptyBacklog,
                    actionLabel = if (state.tasks.isEmpty()) strings.btnAddTask else null,
                    onAction = if (state.tasks.isEmpty()) onOpenQuickAdd else null,
                    testTag = "empty_backlog_card"
                )
            }
        } else {
            itemsIndexed(filteredTasks, key = { _, task -> task.id }) { index, task ->
                AetherSwipeToDismissContainer(
                    onDismiss = { onDeleteTask(task) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    BacklogTaskCard(
                        task = task,
                        canMoveUp = index > 0,
                        canMoveDown = index < filteredTasks.lastIndex,
                        language = state.currentLanguage,
                        onToggle = { onToggleTask(task) },
                        onPromoteToFrog = { onPromoteToFrog(task.id) },
                        onStartFocus = { onStartFocusTimer(task) },
                        onEdit = { onEditTask(task) },
                        onDelete = { onDeleteTask(task) },
                        onMoveUp = { onMoveTask(index, index - 1) },
                        onMoveDown = { onMoveTask(index, index + 1) }
                    )
                }
            }
        }

        // Section for Archived Tasks from Previous Days
        if (state.archivedTasks.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                var showArchived by remember { mutableStateOf(false) }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = AetherSurfaceCard.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showArchived = !showArchived },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Archive,
                                    contentDescription = null,
                                    tint = AetherTextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isSpanish) "Tareas Archivadas de Días Anteriores (${state.archivedTasks.size})"
                                    else "Archived Tasks from Previous Days (${state.archivedTasks.size})",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = AetherTextMuted,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Icon(
                                imageVector = if (showArchived) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = AetherTextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        if (showArchived) {
                            Spacer(modifier = Modifier.height(8.dp))
                            state.archivedTasks.forEach { archivedTask ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = AetherEmerald.copy(alpha = 0.8f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = archivedTask.title,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = AetherTextMuted,
                                            textDecoration = TextDecoration.LineThrough
                                        )
                                        if (archivedTask.completedDate.isNotBlank()) {
                                            Text(
                                                text = (if (isSpanish) "Completada el " else "Completed on ") + archivedTask.completedDate,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontSize = 9.sp,
                                                color = AetherTextMuted.copy(alpha = 0.7f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BacklogTaskCard(
    task: TaskItem,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    language: AppLanguage,
    onToggle: () -> Unit,
    onPromoteToFrog: () -> Unit,
    onStartFocus: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    val strings = remember(language) { StringsProvider(language) }
    val isSpanish = language == AppLanguage.SPANISH

    val energyColor = when (task.energyLevel) {
        EnergyLevel.HIGH -> AetherCoral
        EnergyLevel.MEDIUM -> AetherAmber
        EnergyLevel.LOW -> AetherEmerald
    }

    val energyLabel = when (task.energyLevel) {
        EnergyLevel.HIGH -> if (isSpanish) "ALTA ENERGÍA" else "HIGH ENERGY"
        EnergyLevel.MEDIUM -> if (isSpanish) "ENERGÍA MEDIA" else "MEDIUM ENERGY"
        EnergyLevel.LOW -> if (isSpanish) "BAJA ENERGÍA" else "LOW ENERGY"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onToggle,
                onLongClick = onEdit
            )
            .testTag("backlog_card_${task.id}"),
        colors = CardDefaults.cardColors(containerColor = AetherSurfaceCard),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (task.isFrog) AetherAmber else Color.Transparent
            )
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Reorder handle buttons
                    if (canMoveUp) {
                        IconButton(onClick = onMoveUp, modifier = Modifier.size(22.dp)) {
                            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Move up", tint = AetherTextMuted, modifier = Modifier.size(16.dp))
                        }
                    }
                    if (canMoveDown) {
                        IconButton(onClick = onMoveDown, modifier = Modifier.size(22.dp)) {
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Move down", tint = AetherTextMuted, modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Box(
                        modifier = Modifier
                            .background(energyColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = energyLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = energyColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (task.isFrog) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(AetherAmber.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "🔥 1 FROG",
                                style = MaterialTheme.typography.labelSmall,
                                color = AetherAmber,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!task.isFrog) {
                        TextButton(
                            onClick = onPromoteToFrog,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Text(strings.btnMakeFrog, style = MaterialTheme.typography.labelSmall, color = AetherAmber)
                        }
                    }
                    IconButton(onClick = onEdit, modifier = Modifier.size(26.dp)) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = strings.btnEdit,
                            tint = AetherTextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(26.dp)) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete",
                            tint = AetherCoral.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = task.isCompleted,
                    onCheckedChange = { onToggle() },
                    colors = CheckboxDefaults.colors(checkedColor = AetherCyan, uncheckedColor = AetherBorderLight)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (task.isCompleted) AetherTextMuted else AetherTextPrimary,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (task.description.isNotBlank()) {
                        Text(
                            text = task.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = AetherTextSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                IconButton(
                    onClick = onStartFocus,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Start Focus",
                        tint = AetherCyan
                    )
                }
            }
        }
    }
}
