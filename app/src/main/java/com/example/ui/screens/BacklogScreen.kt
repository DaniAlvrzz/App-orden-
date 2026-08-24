package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
    onDeleteTask: (String) -> Unit,
    onSetEnergyFilter: (EnergyLevel?) -> Unit,
    onSetSearchQuery: (String) -> Unit,
    onStartFocusTimer: (TaskItem?) -> Unit,
    onPauseFocusTimer: () -> Unit,
    onResetFocusTimer: () -> Unit,
    onOpenQuickAdd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = remember(state.currentLanguage) { StringsProvider(state.currentLanguage) }

    val filteredTasks = state.tasks.filter { task ->
        val matchesEnergy = state.filterEnergyLevel == null || task.energyLevel == state.filterEnergyLevel
        val matchesQuery = state.searchQuery.isBlank() ||
                task.title.contains(state.searchQuery, ignoreCase = true) ||
                task.category.contains(state.searchQuery, ignoreCase = true)
        matchesEnergy && matchesQuery
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
                Column {
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

                FilledTonalButton(
                    onClick = onOpenQuickAdd,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = AetherCyan.copy(alpha = 0.15f),
                        contentColor = AetherCyan
                    ),
                    modifier = Modifier.testTag("quick_capture_btn")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(strings.btnCapture)
                }
            }
        }

        // Focus Timer Module
        item {
            FocusTimerCard(
                isRunning = state.isFocusTimerRunning,
                secondsRemaining = state.focusSecondsRemaining,
                activeTask = state.activeFocusTask,
                onStart = { onStartFocusTimer(state.activeFocusTask) },
                onPause = onPauseFocusTimer,
                onReset = onResetFocusTimer
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

        // Task Items List
        if (filteredTasks.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = AetherSurfaceElevated),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = strings.emptyBacklog,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AetherTextSecondary,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            }
        } else {
            items(filteredTasks, key = { it.id }) { task ->
                BacklogTaskCard(
                    task = task,
                    language = state.currentLanguage,
                    onToggle = { onToggleTask(task) },
                    onPromoteToFrog = { onPromoteToFrog(task.id) },
                    onStartFocus = { onStartFocusTimer(task) },
                    onDelete = { onDeleteTask(task.id) }
                )
            }
        }
    }
}

@Composable
fun BacklogTaskCard(
    task: TaskItem,
    language: AppLanguage,
    onToggle: () -> Unit,
    onPromoteToFrog: () -> Unit,
    onStartFocus: () -> Unit,
    onDelete: () -> Unit
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
            .testTag("backlog_card_${task.id}"),
        colors = CardDefaults.cardColors(containerColor = AetherSurfaceCard),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (task.isFrog) AetherAmber else Color.Transparent
            )
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
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

                Row {
                    if (!task.isFrog) {
                        TextButton(
                            onClick = onPromoteToFrog,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Text(strings.btnMakeFrog, style = MaterialTheme.typography.labelSmall, color = AetherAmber)
                        }
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete",
                            tint = AetherTextMuted,
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
