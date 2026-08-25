package com.example.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BlockType
import com.example.data.model.TimeBlock
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.StringsProvider
import com.example.ui.theme.*

@Composable
fun TimeBlockTimeline(
    blocks: List<TimeBlock>,
    onToggleBlock: (TimeBlock) -> Unit,
    onAddBlockClick: () -> Unit,
    onEditBlock: (TimeBlock) -> Unit = {},
    onDeleteBlock: (TimeBlock) -> Unit = {},
    onMoveBlock: (fromIndex: Int, toIndex: Int) -> Unit = { _, _ -> },
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
                text = strings.timeBlocksTitle,
                style = MaterialTheme.typography.labelSmall,
                color = AetherCyan,
                letterSpacing = 1.1.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f, fill = false)
            )
            IconButton(
                onClick = onAddBlockClick,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("add_timeblock_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.AddCircle,
                    contentDescription = strings.btnAddTimeBlock,
                    tint = AetherCyan,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (blocks.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = AetherSurfaceElevated),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = AetherCyan.copy(alpha = 0.7f),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = strings.emptyTimeBlocksClean,
                        style = MaterialTheme.typography.bodySmall,
                        color = AetherTextSecondary
                    )
                }
            }
        } else {
            blocks.forEachIndexed { index, block ->
                AetherSwipeToDismissContainer(
                    onDismiss = { onDeleteBlock(block) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TimeBlockItemRow(
                        block = block,
                        canMoveUp = index > 0,
                        canMoveDown = index < blocks.lastIndex,
                        language = language,
                        onToggle = { onToggleBlock(block) },
                        onEdit = { onEditBlock(block) },
                        onDelete = { onDeleteBlock(block) },
                        onMoveUp = { onMoveBlock(index, index - 1) },
                        onMoveDown = { onMoveBlock(index, index + 1) }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TimeBlockItemRow(
    block: TimeBlock,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    language: AppLanguage,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    val isSpanish = language == AppLanguage.SPANISH

    val blockColor = when (block.blockType) {
        BlockType.DEEP_WORK -> AetherCoral
        BlockType.COGNITIVE_RECOVERY_BUFFER -> AetherEmerald
        BlockType.HABIT_ANCHOR -> AetherAmber
        BlockType.MEAL -> AetherCyan
        BlockType.MEETING -> AetherViolet
        BlockType.ADMIN_SLOT -> AetherElectricBlue
        BlockType.SLEEP -> AetherPurple
        BlockType.CUSTOM -> AetherCyan
    }

    val typeLabel = when (block.blockType) {
        BlockType.DEEP_WORK -> if (isSpanish) "TRABAJO PROFUNDO" else "DEEP WORK"
        BlockType.COGNITIVE_RECOVERY_BUFFER -> if (isSpanish) "PAUSA RECUPERACIÓN" else "RECOVERY BUFFER"
        BlockType.HABIT_ANCHOR -> if (isSpanish) "ANCLA CIRCADIANA" else "HABIT ANCHOR"
        BlockType.MEAL -> if (isSpanish) "NUTRICIÓN" else "MEAL FUEL"
        BlockType.MEETING -> if (isSpanish) "COORDINACIÓN" else "MEETING"
        BlockType.ADMIN_SLOT -> if (isSpanish) "ADMIN / TRIAJE" else "ADMIN / TRIAGE"
        BlockType.SLEEP -> if (isSpanish) "SUEÑO Y DESCANSO" else "SLEEP"
        BlockType.CUSTOM -> if (isSpanish) "PERSONALIZADO" else "CUSTOM"
    }

    val icon = when (block.blockType) {
        BlockType.DEEP_WORK -> Icons.Default.Psychology
        BlockType.COGNITIVE_RECOVERY_BUFFER -> Icons.Default.Spa
        BlockType.HABIT_ANCHOR -> Icons.Default.WbSunny
        BlockType.MEAL -> Icons.Default.Restaurant
        BlockType.MEETING -> Icons.Default.Groups
        BlockType.ADMIN_SLOT -> Icons.Default.TaskAlt
        BlockType.SLEEP -> Icons.Default.Bedtime
        BlockType.CUSTOM -> Icons.Default.Schedule
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onToggle,
                onLongClick = onEdit
            )
            .testTag("timeblock_${block.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (block.isCompleted) AetherSurface.copy(alpha = 0.5f) else AetherSurfaceCard
        ),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (block.blockType == BlockType.DEEP_WORK) AetherCoral.copy(alpha = 0.3f) else Color.Transparent
            )
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Reorder handles
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

            // Time Column
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(52.dp)
            ) {
                Text(
                    text = block.startTime,
                    style = MaterialTheme.typography.labelMedium,
                    color = AetherTextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = block.endTime,
                    style = MaterialTheme.typography.labelSmall,
                    color = AetherTextMuted,
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Vertical indicator
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(36.dp)
                    .clip(CircleShape)
                    .background(blockColor)
            )

            Spacer(modifier = Modifier.width(10.dp))

            // Title & Badge
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = blockColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = typeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = blockColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = block.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (block.isCompleted) AetherTextMuted else AetherTextPrimary,
                    textDecoration = if (block.isCompleted) TextDecoration.LineThrough else null,
                    fontWeight = FontWeight.Medium
                )
            }

            // Edit button
            IconButton(
                onClick = onEdit,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = if (isSpanish) "Editar Bloque" else "Edit Block",
                    tint = AetherTextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }

            // Trash Button for deleting time block
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(28.dp)
                    .testTag("delete_timeblock_${block.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = if (isSpanish) "Eliminar Bloque" else "Delete Block",
                    tint = AetherCoral.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(2.dp))

            Checkbox(
                checked = block.isCompleted,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = AetherEmerald,
                    uncheckedColor = AetherBorderLight
                )
            )
        }
    }
}
