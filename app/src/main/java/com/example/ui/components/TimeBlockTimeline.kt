package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
                fontWeight = FontWeight.Bold
            )
            IconButton(
                onClick = onAddBlockClick,
                modifier = Modifier
                    .size(28.dp)
                    .testTag("add_timeblock_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.AddCircle,
                    contentDescription = strings.btnAddTimeBlock,
                    tint = AetherCyan
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
                Text(
                    text = strings.emptyTimeBlocks,
                    style = MaterialTheme.typography.bodySmall,
                    color = AetherTextSecondary,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            blocks.forEach { block ->
                TimeBlockItemRow(
                    block = block,
                    language = language,
                    onToggle = { onToggleBlock(block) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun TimeBlockItemRow(
    block: TimeBlock,
    language: AppLanguage,
    onToggle: () -> Unit
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
    }

    val typeLabel = when (block.blockType) {
        BlockType.DEEP_WORK -> if (isSpanish) "TRABAJO PROFUNDO" else "DEEP WORK"
        BlockType.COGNITIVE_RECOVERY_BUFFER -> if (isSpanish) "PAUSA RECUPERACIÓN" else "RECOVERY BUFFER"
        BlockType.HABIT_ANCHOR -> if (isSpanish) "ANCLA CIRCADIANA" else "HABIT ANCHOR"
        BlockType.MEAL -> if (isSpanish) "NUTRICIÓN" else "MEAL FUEL"
        BlockType.MEETING -> if (isSpanish) "COORDINACIÓN" else "MEETING"
        BlockType.ADMIN_SLOT -> if (isSpanish) "ADMIN / TRIAJE" else "ADMIN / TRIAGE"
        BlockType.SLEEP -> if (isSpanish) "SUEÑO Y DESCANSO" else "SLEEP"
    }

    val icon = when (block.blockType) {
        BlockType.DEEP_WORK -> Icons.Default.Psychology
        BlockType.COGNITIVE_RECOVERY_BUFFER -> Icons.Default.Spa
        BlockType.HABIT_ANCHOR -> Icons.Default.WbSunny
        BlockType.MEAL -> Icons.Default.Restaurant
        BlockType.MEETING -> Icons.Default.Groups
        BlockType.ADMIN_SLOT -> Icons.Default.TaskAlt
        BlockType.SLEEP -> Icons.Default.Bedtime
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
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
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Time Column
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(55.dp)
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

            Spacer(modifier = Modifier.width(8.dp))

            // Vertical indicator
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(36.dp)
                    .clip(CircleShape)
                    .background(blockColor)
            )

            Spacer(modifier = Modifier.width(12.dp))

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
