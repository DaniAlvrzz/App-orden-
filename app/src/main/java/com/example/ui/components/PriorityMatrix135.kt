package com.example.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EnergyLevel
import com.example.data.model.PriorityType
import com.example.data.model.SystemMode
import com.example.data.model.TaskItem
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.StringsProvider
import com.example.ui.theme.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PriorityMatrix135(
    systemMode: SystemMode,
    frogTask: TaskItem?,
    mediumTasks: List<TaskItem>,
    quickWins: List<TaskItem>,
    onToggleTask: (TaskItem) -> Unit,
    onStartFocus: (TaskItem) -> Unit,
    onAddTaskClick: () -> Unit,
    onEditTask: (TaskItem) -> Unit = {},
    onDeleteTask: (TaskItem) -> Unit = {},
    onMoveMediumTask: (fromIndex: Int, toIndex: Int) -> Unit = { _, _ -> },
    onMoveQuickTask: (fromIndex: Int, toIndex: Int) -> Unit = { _, _ -> },
    language: AppLanguage = AppLanguage.SPANISH,
    modifier: Modifier = Modifier
) {
    val strings = remember(language) { StringsProvider(language) }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = strings.prioritiesMatrixTitle,
                style = MaterialTheme.typography.labelSmall,
                color = AetherCyan,
                letterSpacing = 1.1.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f, fill = false)
            )
            IconButton(
                onClick = onAddTaskClick,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("add_task_priority_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.AddCircle,
                    contentDescription = strings.btnAddTask,
                    tint = AetherCyan,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // --- 1 FROG TASK SECTION (TIPO A) ---
        if (systemMode == SystemMode.RECOVERY) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("recovery_frog_placeholder"),
                colors = CardDefaults.cardColors(containerColor = AetherEmerald.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(AetherEmerald.copy(alpha = 0.4f)))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Spa,
                        contentDescription = "Recovery",
                        tint = AetherEmerald,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = strings.recoveryZeroFrogsTitle,
                            style = MaterialTheme.typography.labelMedium,
                            color = AetherEmerald,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = strings.recoveryZeroFrogsDesc,
                            style = MaterialTheme.typography.bodySmall,
                            color = AetherTextSecondary
                        )
                    }
                }
            }
        } else if (frogTask != null) {
            AetherSwipeToDismissContainer(
                onDismiss = { onDeleteTask(frogTask) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = { onToggleTask(frogTask) },
                            onLongClick = { onEditTask(frogTask) }
                        )
                        .testTag("frog_task_card"),
                    colors = CardDefaults.cardColors(containerColor = AetherSurfaceCard),
                    shape = RoundedCornerShape(16.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(AetherAmber.copy(alpha = 0.7f)))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(AetherAmber.copy(alpha = 0.2f))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = strings.frogBadge,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = AetherAmber,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${frogTask.estimatedMinutes}m ${strings.deepFocusSuffix}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AetherTextMuted
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { onStartFocus(frogTask) },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .testTag("frog_focus_timer_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = strings.btnFocusTimer,
                                        tint = AetherCyan
                                    )
                                }
                                IconButton(
                                    onClick = { onEditTask(frogTask) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = strings.btnEdit,
                                        tint = AetherTextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { onDeleteTask(frogTask) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = AetherCoral.copy(alpha = 0.8f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = frogTask.isCompleted,
                                onCheckedChange = { onToggleTask(frogTask) },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = AetherEmerald,
                                    uncheckedColor = AetherTextSecondary
                                ),
                                modifier = Modifier.testTag("frog_checkbox")
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = frogTask.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (frogTask.isCompleted) AetherTextMuted else AetherTextPrimary,
                                    textDecoration = if (frogTask.isCompleted) TextDecoration.LineThrough else null,
                                    fontWeight = FontWeight.SemiBold
                                )
                                if (frogTask.description.isNotBlank()) {
                                    Text(
                                        text = frogTask.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = AetherTextSecondary,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = { onAddTaskClick() },
                        onLongClick = {}
                    ),
                colors = CardDefaults.cardColors(containerColor = AetherSurfaceElevated),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = AetherCyan)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = strings.assignFrogPlaceholder,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AetherCyan
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- 3 MEDIUM TASKS (TIPO B) ---
        Text(
            text = strings.mediumTasksHeader,
            style = MaterialTheme.typography.labelSmall,
            color = AetherTextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(6.dp))

        val displayMedium = mediumTasks.take(3)
        displayMedium.forEachIndexed { index, task ->
            AetherSwipeToDismissContainer(
                onDismiss = { onDeleteTask(task) },
                modifier = Modifier.fillMaxWidth()
            ) {
                TaskCompactRow(
                    task = task,
                    canMoveUp = index > 0,
                    canMoveDown = index < displayMedium.lastIndex,
                    onToggle = { onToggleTask(task) },
                    onEdit = { onEditTask(task) },
                    onDelete = { onDeleteTask(task) },
                    onMoveUp = { onMoveMediumTask(index, index - 1) },
                    onMoveDown = { onMoveMediumTask(index, index + 1) },
                    onStartTimer = { onStartFocus(task) }
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        Spacer(modifier = Modifier.height(10.dp))

        // --- 5 QUICK WINS (TIPO C) ---
        Text(
            text = strings.quickWinsHeader,
            style = MaterialTheme.typography.labelSmall,
            color = AetherTextMuted,
            fontSize = 11.sp
        )
        Spacer(modifier = Modifier.height(6.dp))

        val displayQuick = quickWins.take(5)
        displayQuick.forEachIndexed { index, task ->
            AetherSwipeToDismissContainer(
                onDismiss = { onDeleteTask(task) },
                modifier = Modifier.fillMaxWidth()
            ) {
                TaskMicroRow(
                    task = task,
                    canMoveUp = index > 0,
                    canMoveDown = index < displayQuick.lastIndex,
                    onToggle = { onToggleTask(task) },
                    onEdit = { onEditTask(task) },
                    onDelete = { onDeleteTask(task) },
                    onMoveUp = { onMoveQuickTask(index, index - 1) },
                    onMoveDown = { onMoveQuickTask(index, index + 1) }
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskCompactRow(
    task: TaskItem,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onStartTimer: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onToggle,
                onLongClick = onEdit
            )
            .testTag("task_row_${task.id}"),
        colors = CardDefaults.cardColors(containerColor = AetherSurfaceCard),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Drag / Reorder Handle
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (canMoveUp) {
                    IconButton(onClick = onMoveUp, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Move up", tint = AetherTextMuted, modifier = Modifier.size(16.dp))
                    }
                }
                if (canMoveDown) {
                    IconButton(onClick = onMoveDown, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Move down", tint = AetherTextMuted, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = AetherCyan,
                    uncheckedColor = AetherBorderLight
                )
            )
            Spacer(modifier = Modifier.width(4.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (task.isCompleted) AetherTextMuted else AetherTextPrimary,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${task.estimatedMinutes}m",
                        style = MaterialTheme.typography.labelSmall,
                        color = AetherTextMuted,
                        fontSize = 10.sp
                    )
                    if (task.category.isNotBlank()) {
                        Text(
                            text = " • ${task.category}",
                            style = MaterialTheme.typography.labelSmall,
                            color = AetherTextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            IconButton(
                onClick = onStartTimer,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Focus",
                    tint = AetherTextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
            IconButton(
                onClick = onEdit,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = AetherTextMuted,
                    modifier = Modifier.size(14.dp)
                )
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = AetherCoral.copy(alpha = 0.7f),
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskMicroRow(
    task: TaskItem,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(AetherSurface.copy(alpha = 0.5f))
            .combinedClickable(
                onClick = onToggle,
                onLongClick = onEdit
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (canMoveUp) {
            IconButton(onClick = onMoveUp, modifier = Modifier.size(20.dp)) {
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Move up", tint = AetherTextMuted, modifier = Modifier.size(14.dp))
            }
        }
        if (canMoveDown) {
            IconButton(onClick = onMoveDown, modifier = Modifier.size(20.dp)) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Move down", tint = AetherTextMuted, modifier = Modifier.size(14.dp))
            }
        }

        Checkbox(
            checked = task.isCompleted,
            onCheckedChange = { onToggle() },
            colors = CheckboxDefaults.colors(
                checkedColor = AetherEmerald,
                uncheckedColor = AetherBorderLight
            ),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = task.title,
            style = MaterialTheme.typography.bodySmall,
            color = if (task.isCompleted) AetherTextMuted else AetherTextPrimary,
            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "${task.estimatedMinutes}m",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = AetherTextMuted
        )
        IconButton(onClick = onEdit, modifier = Modifier.size(24.dp)) {
            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = AetherTextMuted, modifier = Modifier.size(12.dp))
        }
        IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = AetherCoral.copy(alpha = 0.6f), modifier = Modifier.size(12.dp))
        }
    }
}
