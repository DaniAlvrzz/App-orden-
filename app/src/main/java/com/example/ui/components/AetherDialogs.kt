package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.StringsProvider
import com.example.ui.theme.*

@Composable
fun QuickAddTaskDialog(
    initialTask: TaskItem? = null,
    language: AppLanguage = AppLanguage.SPANISH,
    onDismiss: () -> Unit,
    onSave: (title: String, desc: String, energy: EnergyLevel, priority: PriorityType, minutes: Int, category: String, makeFrog: Boolean, isPermanent: Boolean) -> Unit
) {
    val strings = remember(language) { StringsProvider(language) }
    var title by remember { mutableStateOf(initialTask?.title ?: "") }
    var description by remember { mutableStateOf(initialTask?.description ?: "") }
    var energyLevel by remember { mutableStateOf(initialTask?.energyLevel ?: EnergyLevel.MEDIUM) }
    var priorityType by remember { mutableStateOf(initialTask?.priorityType ?: PriorityType.MEDIUM) }
    var estimatedMinutesText by remember { mutableStateOf((initialTask?.estimatedMinutes ?: 30).toString()) }
    var isPermanent by remember { mutableStateOf(initialTask?.isPermanent ?: false) }
    var category by remember { 
        mutableStateOf(initialTask?.category ?: if (language == AppLanguage.SPANISH) "Trabajo Profundo" else "Deep Work") 
    }
    var isFrog by remember { mutableStateOf(initialTask?.isFrog ?: (initialTask?.priorityType == PriorityType.FROG)) }

    val isSpanish = language == AppLanguage.SPANISH

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (initialTask != null) Icons.Default.Edit else Icons.Default.Bolt, 
                    contentDescription = null, 
                    tint = AetherCyan
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (initialTask != null) strings.editTaskTitle else strings.quickAddTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = AetherTextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Task Type Selector: Ephemeral vs Permanent
                Text(
                    text = if (isSpanish) "Tipo de Tarea en Bandeja" else "Task Persistence Type",
                    style = MaterialTheme.typography.labelSmall,
                    color = AetherCyan,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = !isPermanent,
                        onClick = { isPermanent = false },
                        label = {
                            Text(
                                text = if (isSpanish) "⚡ Puntual (se archiva)" else "⚡ Ephemeral",
                                fontSize = 11.sp
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AetherCyan.copy(alpha = 0.2f),
                            selectedLabelColor = AetherCyan
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = isPermanent,
                        onClick = { isPermanent = true },
                        label = {
                            Text(
                                text = if (isSpanish) "📌 Fija (permanece)" else "📌 Persistent",
                                fontSize = 11.sp
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AetherPurple.copy(alpha = 0.2f),
                            selectedLabelColor = AetherPurple
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(strings.taskTitleLabel) },
                    placeholder = { 
                        Text(if (isSpanish) "Ej: Diseñar arquitectura / Comprar víveres" else "e.g. Write dispatch tests / Buy groceries") 
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AetherCyan,
                        unfocusedBorderColor = AetherBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("task_title_input")
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(strings.taskDescLabel) },
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AetherCyan,
                        unfocusedBorderColor = AetherBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Bio Energy Selection
                Text(
                    text = strings.taskEnergyLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = AetherCyan,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    EnergyLevel.entries.forEach { level ->
                        val isSelected = energyLevel == level
                        val chipColor = when (level) {
                            EnergyLevel.HIGH -> AetherCoral
                            EnergyLevel.MEDIUM -> AetherAmber
                            EnergyLevel.LOW -> AetherEmerald
                        }
                        val labelText = when (level) {
                            EnergyLevel.HIGH -> if (isSpanish) "Alta" else "High"
                            EnergyLevel.MEDIUM -> if (isSpanish) "Media" else "Medium"
                            EnergyLevel.LOW -> if (isSpanish) "Baja" else "Low"
                        }
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                energyLevel = level
                                if (level == EnergyLevel.HIGH) {
                                    isFrog = true
                                    priorityType = PriorityType.FROG
                                } else if (level == EnergyLevel.MEDIUM && priorityType == PriorityType.FROG) {
                                    priorityType = PriorityType.MEDIUM
                                    isFrog = false
                                } else if (level == EnergyLevel.LOW && priorityType == PriorityType.FROG) {
                                    priorityType = PriorityType.QUICK
                                    isFrog = false
                                }
                            },
                            label = { Text(labelText, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = chipColor.copy(alpha = 0.2f),
                                selectedLabelColor = chipColor
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // 1-3-5 Priority Selection
                Text(
                    text = if (isSpanish) "Triaje 1-3-5 (Destino en Nexus)" else "1-3-5 Priority (Nexus Menu Target)",
                    style = MaterialTheme.typography.labelSmall,
                    color = AetherCyan,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PriorityType.entries.forEach { pType ->
                        val isSelected = priorityType == pType
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                priorityType = pType
                                isFrog = (pType == PriorityType.FROG)
                                if (pType == PriorityType.FROG) {
                                    energyLevel = EnergyLevel.HIGH
                                } else if (pType == PriorityType.MEDIUM) {
                                    energyLevel = EnergyLevel.MEDIUM
                                } else if (pType == PriorityType.QUICK) {
                                    energyLevel = EnergyLevel.LOW
                                }
                            },
                            label = {
                                Text(
                                    when (pType) {
                                        PriorityType.FROG -> if (isSpanish) "1 Frog (Tipo A)" else "1 Frog (Type A)"
                                        PriorityType.MEDIUM -> if (isSpanish) "3 Media (Tipo B)" else "3 Medium (Type B)"
                                        PriorityType.QUICK -> if (isSpanish) "5 Micro (Tipo C)" else "5 Micro (Type C)"
                                    },
                                    fontSize = 11.sp
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Custom Duration Input (Free numeric input)
                Text(
                    text = if (isSpanish) "Para tú poner el tiempo (minutos)" else "Custom estimated duration (minutes)",
                    style = MaterialTheme.typography.labelSmall,
                    color = AetherTextSecondary,
                    fontWeight = FontWeight.SemiBold
                )
                OutlinedTextField(
                    value = estimatedMinutesText,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() } && input.length <= 4) {
                            estimatedMinutesText = input
                        }
                    },
                    label = { Text(if (isSpanish) "Minutos estimados" else "Estimated minutes") },
                    placeholder = { Text("Ej: 25") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AetherCyan,
                        unfocusedBorderColor = AetherBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Quick preset duration chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(10, 15, 30, 45, 60, 90).forEach { mins ->
                        val isSelected = estimatedMinutesText == mins.toString()
                        FilterChip(
                            selected = isSelected,
                            onClick = { estimatedMinutesText = mins.toString() },
                            label = { Text("${mins}m", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val parsedMinutes = estimatedMinutesText.toIntOrNull()?.coerceAtLeast(1) ?: 30
                        onSave(title, description, energyLevel, priorityType, parsedMinutes, category, isFrog, isPermanent)
                    }
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = AetherCyan, contentColor = Color(0xFF00363D)),
                modifier = Modifier.testTag("save_task_btn")
            ) {
                Text(if (initialTask != null) strings.btnSave else strings.btnCapture, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.btnCancel, color = AetherTextSecondary)
            }
        },
        containerColor = AetherSurfaceElevated
    )
}

@Composable
fun AddPantryDialog(
    initialItem: PantryItem? = null,
    language: AppLanguage = AppLanguage.SPANISH,
    onDismiss: () -> Unit,
    onSave: (name: String, category: PantryCategory, inStock: Boolean, isBatchBase: Boolean, qty: String) -> Unit
) {
    val strings = remember(language) { StringsProvider(language) }
    var name by remember { mutableStateOf(initialItem?.name ?: "") }
    var category by remember { mutableStateOf(initialItem?.category ?: PantryCategory.PROTEIN) }
    var inStock by remember { mutableStateOf(initialItem?.inStock ?: true) }
    var isBatchBase by remember { mutableStateOf(initialItem?.isBatchBase ?: false) }
    var quantityDesc by remember { 
        mutableStateOf(initialItem?.quantityDesc ?: if (language == AppLanguage.SPANISH) "Disponible" else "Available") 
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (initialItem != null) Icons.Default.Edit else Icons.Default.Kitchen,
                    contentDescription = null,
                    tint = AetherCyan
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (initialItem != null) strings.editPantryTitle else strings.addPantryTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = AetherTextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(strings.itemNameLabel) },
                    placeholder = { 
                        Text(if (language == AppLanguage.SPANISH) "Ej: Quinoa Tricolor Cocida" else "e.g. Pre-cooked Brown Rice") 
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = strings.itemCategoryLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = AetherCyan,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    PantryCategory.entries.forEach { cat ->
                        val catDisplay = when (cat) {
                            PantryCategory.PROTEIN -> if (language == AppLanguage.SPANISH) "Proteína" else "Protein"
                            PantryCategory.CARB_BASE -> if (language == AppLanguage.SPANISH) "Base Carbo" else "Carb Base"
                            PantryCategory.PRODUCE -> if (language == AppLanguage.SPANISH) "Vegetal" else "Produce"
                            PantryCategory.HEALTHY_FAT -> if (language == AppLanguage.SPANISH) "Grasa Sana" else "Healthy Fat"
                            PantryCategory.SEASONING -> if (language == AppLanguage.SPANISH) "Especias" else "Seasoning"
                        }
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(catDisplay, fontSize = 9.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.itemBatchBaseCheck,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = isBatchBase,
                        onCheckedChange = { isBatchBase = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = AetherCyan)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.itemInStockCheck,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = inStock,
                        onCheckedChange = { inStock = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = AetherEmerald)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(name, category, inStock, isBatchBase, quantityDesc)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AetherCyan, contentColor = Color(0xFF00363D))
            ) {
                Text(strings.btnSave, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(strings.btnCancel, color = AetherTextSecondary) }
        },
        containerColor = AetherSurfaceElevated
    )
}

@Composable
fun CognitiveReframeDialog(
    language: AppLanguage = AppLanguage.SPANISH,
    reframeText: String?,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onSubmitNew: (String) -> Unit
) {
    val strings = remember(language) { StringsProvider(language) }
    var userInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Spa, contentDescription = null, tint = AetherEmerald)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = strings.cognitiveReframeTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = AetherTextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = strings.cognitiveReframeSub,
                    style = MaterialTheme.typography.bodySmall,
                    color = AetherTextSecondary
                )

                OutlinedTextField(
                    value = userInput,
                    onValueChange = { userInput = it },
                    label = { 
                        Text(if (language == AppLanguage.SPANISH) "¿Qué estás experimentando?" else "What are you feeling?") 
                    },
                    placeholder = { Text(strings.reframePlaceholder) },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AetherEmerald)
                    }
                } else if (!reframeText.isNullOrBlank()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = AetherEmerald.copy(alpha = 0.12f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = reframeText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = AetherTextPrimary,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (userInput.isNotBlank()) {
                        onSubmitNew(userInput)
                    }
                },
                enabled = userInput.isNotBlank() && !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = AetherEmerald, contentColor = Color(0xFF003919))
            ) {
                Text(strings.btnSubmitReframe, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(strings.btnClose, color = AetherTextSecondary) }
        },
        containerColor = AetherSurfaceElevated
    )
}

fun parseTimeToMinutes(timeStr: String): Int {
    return try {
        val clean = timeStr.trim()
        val parts = clean.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 0
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
        (hour * 60 + minute).coerceIn(0, 1439)
    } catch (e: Exception) {
        0
    }
}

@Composable
fun AddTimeBlockDialog(
    initialBlock: TimeBlock? = null,
    language: AppLanguage = AppLanguage.SPANISH,
    existingBlocks: List<TimeBlock> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (startTime: String, endTime: String, blockType: BlockType, title: String, notes: String) -> Unit
) {
    val strings = remember(language) { StringsProvider(language) }
    var startTime by remember { mutableStateOf(initialBlock?.startTime ?: "09:00") }
    var endTime by remember { mutableStateOf(initialBlock?.endTime ?: "10:30") }
    var blockType by remember { mutableStateOf(initialBlock?.blockType ?: BlockType.DEEP_WORK) }
    var title by remember { mutableStateOf(initialBlock?.title ?: "") }
    var notes by remember { mutableStateOf(initialBlock?.notes ?: "") }
    var conflictingBlock by remember { mutableStateOf<TimeBlock?>(null) }
    var showOverlapWarning by remember { mutableStateOf(false) }

    if (showOverlapWarning && conflictingBlock != null) {
        val isSpanish = language == AppLanguage.SPANISH
        AlertDialog(
            onDismissRequest = { showOverlapWarning = false },
            icon = { Icon(imageVector = Icons.Default.WarningAmber, contentDescription = null, tint = AetherAmber) },
            title = {
                Text(
                    text = if (isSpanish) "Solapamiento de Horario" else "Time Overlap Detected",
                    fontWeight = FontWeight.Bold,
                    color = AetherTextPrimary
                )
            },
            text = {
                Text(
                    text = if (isSpanish) {
                        "Este bloque ($startTime - $endTime) se solapa con \"${conflictingBlock?.title}\" (${conflictingBlock?.startTime} - ${conflictingBlock?.endTime}). ¿Deseas continuar y guardarlo de todas formas?"
                    } else {
                        "This block ($startTime - $endTime) overlaps with \"${conflictingBlock?.title}\" (${conflictingBlock?.startTime} - ${conflictingBlock?.endTime}). Do you want to proceed and save anyway?"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = AetherTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showOverlapWarning = false
                        onSave(startTime, endTime, blockType, title, notes)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AetherAmber, contentColor = Color(0xFF332000))
                ) {
                    Text(if (isSpanish) "Guardar de todas formas" else "Save Anyway", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showOverlapWarning = false }) {
                    Text(if (isSpanish) "Modificar horas" else "Adjust Times", color = AetherCyan)
                }
            },
            containerColor = AetherSurfaceElevated
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (initialBlock != null) Icons.Default.Edit else Icons.Default.Schedule,
                    contentDescription = null, 
                    tint = AetherCyan
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (initialBlock != null) strings.editTimeBlockTitle else strings.addTimeBlockTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = AetherTextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(strings.blockTitleLabel) },
                    placeholder = {
                        Text(if (language == AppLanguage.SPANISH) "Ej: Bloque de Trabajo Profundo" else "e.g. Deep Architecture Focus")
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        label = { Text(strings.blockStartTimeLabel) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { endTime = it },
                        label = { Text(strings.blockEndTimeLabel) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Text(
                    text = strings.blockTypeLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = AetherCyan,
                    fontWeight = FontWeight.Bold
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(BlockType.DEEP_WORK, BlockType.MEAL, BlockType.COGNITIVE_RECOVERY_BUFFER).forEach { bType ->
                            val label = when (bType) {
                                BlockType.DEEP_WORK -> if (language == AppLanguage.SPANISH) "Deep Work" else "Deep Work"
                                BlockType.MEAL -> if (language == AppLanguage.SPANISH) "Comida" else "Meal"
                                BlockType.COGNITIVE_RECOVERY_BUFFER -> if (language == AppLanguage.SPANISH) "Pausa" else "Recovery"
                                else -> bType.name
                            }
                            FilterChip(
                                selected = blockType == bType,
                                onClick = { blockType = bType },
                                label = { Text(label, fontSize = 10.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(BlockType.MEETING, BlockType.ADMIN_SLOT, BlockType.CUSTOM).forEach { bType ->
                            val label = when (bType) {
                                BlockType.MEETING -> if (language == AppLanguage.SPANISH) "Reunión" else "Meeting"
                                BlockType.ADMIN_SLOT -> if (language == AppLanguage.SPANISH) "Admin" else "Admin"
                                BlockType.CUSTOM -> if (language == AppLanguage.SPANISH) "Personalizado" else "Custom"
                                else -> bType.name
                            }
                            FilterChip(
                                selected = blockType == bType,
                                onClick = { blockType = bType },
                                label = { Text(label, fontSize = 10.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(strings.blockNotesLabel) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val newStartMin = parseTimeToMinutes(startTime)
                        val newEndMin = parseTimeToMinutes(endTime)
                        val actualEndMin = if (newEndMin <= newStartMin) newStartMin + 60 else newEndMin

                        val otherBlocks = existingBlocks.filter { it.id != (initialBlock?.id ?: "") }
                        val overlap = otherBlocks.firstOrNull { existing ->
                            val exStart = parseTimeToMinutes(existing.startTime)
                            val exEnd = parseTimeToMinutes(existing.endTime)
                            val actualExEnd = if (exEnd <= exStart) exStart + 60 else exEnd
                            // Overlap condition: startA < endB and startB < endA
                            newStartMin < actualExEnd && exStart < actualEndMin
                        }

                        if (overlap != null) {
                            conflictingBlock = overlap
                            showOverlapWarning = true
                        } else {
                            onSave(startTime, endTime, blockType, title, notes)
                        }
                    }
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = AetherCyan, contentColor = Color(0xFF00363D))
            ) {
                Text(strings.btnSave, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(strings.btnCancel, color = AetherTextSecondary) }
        },
        containerColor = AetherSurfaceElevated
    )
}

@Composable
fun AddMealDialog(
    initialMeal: MealItem? = null,
    language: AppLanguage = AppLanguage.SPANISH,
    onDismiss: () -> Unit,
    onSave: (
        slot: MealSlot,
        title: String,
        desc: String,
        prepTime: Int,
        ingredients: List<String>,
        usesBatch: Boolean,
        inStock: Boolean,
        bioImpact: BioGlycemicImpact,
        customSlotName: String?,
        proteinGrams: Int,
        carbsGrams: Int,
        fatGrams: Int,
        caloriesKcal: Int
    ) -> Unit
) {
    val strings = remember(language) { StringsProvider(language) }
    val isSpanish = language == AppLanguage.SPANISH

    var slot by remember { mutableStateOf(initialMeal?.slot ?: MealSlot.LUNCH) }
    var customSlotName by remember { mutableStateOf(initialMeal?.customSlotName ?: "") }
    var title by remember { mutableStateOf(initialMeal?.title ?: "") }
    var description by remember { mutableStateOf(initialMeal?.description ?: "") }
    var prepTimeMinutes by remember { mutableIntStateOf(initialMeal?.prepTimeMinutes ?: 10) }
    var ingredientsRaw by remember { mutableStateOf(initialMeal?.ingredients?.joinToString(", ") ?: "") }
    var usesBatchCookedBase by remember { mutableStateOf(initialMeal?.usesBatchCookedBase ?: false) }
    var allIngredientsInStock by remember { mutableStateOf(initialMeal?.allIngredientsInStock ?: true) }
    var bioImpact by remember { mutableStateOf(initialMeal?.bioImpact ?: BioGlycemicImpact.MODERATE_STEADY) }

    var proteinStr by remember { mutableStateOf(initialMeal?.proteinGrams?.takeIf { it > 0 }?.toString() ?: "") }
    var carbsStr by remember { mutableStateOf(initialMeal?.carbsGrams?.takeIf { it > 0 }?.toString() ?: "") }
    var fatStr by remember { mutableStateOf(initialMeal?.fatGrams?.takeIf { it > 0 }?.toString() ?: "") }

    val protein = proteinStr.toIntOrNull() ?: 0
    val carbs = carbsStr.toIntOrNull() ?: 0
    val fat = fatStr.toIntOrNull() ?: 0
    val computedKcal = (protein * 4) + (carbs * 4) + (fat * 9)

    val quickSlotSuggestions = if (isSpanish) {
        listOf("Pre-Entreno", "Post-Entreno", "Merienda", "Media Mañana", "Cena Tardía", "Brunch")
    } else {
        listOf("Pre-Workout", "Post-Workout", "Afternoon Snack", "Mid-Morning", "Late Dinner", "Brunch")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (initialMeal != null) Icons.Default.Edit else Icons.Default.Restaurant, 
                    contentDescription = null, 
                    tint = AetherAmber
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (initialMeal != null) (if (isSpanish) "Editar Comida" else "Edit Meal") else strings.addMealTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = AetherTextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(strings.mealTitleLabel) },
                    placeholder = {
                        Text(if (language == AppLanguage.SPANISH) "Ej: Bowl de Quinoa y Salmón" else "e.g. Quinoa Salmon Power Bowl")
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(strings.mealDescLabel) },
                    placeholder = {
                        Text(if (language == AppLanguage.SPANISH) "Ej: Energía constante sin pico de glucosa" else "e.g. Steady dopamine release")
                    },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                // Moment / Meal Slot Selector
                Text(
                    text = strings.mealSlotLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = AetherCyan,
                    fontWeight = FontWeight.Bold
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(MealSlot.BREAKFAST, MealSlot.LUNCH, MealSlot.DINNER).forEach { mSlot ->
                            val slotLabel = when (mSlot) {
                                MealSlot.BREAKFAST -> strings.slotBreakfast
                                MealSlot.LUNCH -> if (isSpanish) "Almuerzo" else "Lunch"
                                MealSlot.DINNER -> strings.slotDinner
                                else -> mSlot.name
                            }
                            FilterChip(
                                selected = slot == mSlot,
                                onClick = { slot = mSlot },
                                label = { Text(slotLabel, fontSize = 10.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(MealSlot.SNACK, MealSlot.CUSTOM).forEach { mSlot ->
                            val slotLabel = when (mSlot) {
                                MealSlot.SNACK -> "Snack"
                                MealSlot.CUSTOM -> strings.slotCustom
                                else -> mSlot.name
                            }
                            FilterChip(
                                selected = slot == mSlot,
                                onClick = { 
                                    slot = mSlot 
                                    if (mSlot == MealSlot.CUSTOM && customSlotName.isBlank()) {
                                        customSlotName = quickSlotSuggestions.first()
                                    }
                                },
                                label = { Text(slotLabel, fontSize = 10.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Custom Moment Name Input & Quick Suggestions
                if (slot == MealSlot.CUSTOM) {
                    OutlinedTextField(
                        value = customSlotName,
                        onValueChange = { customSlotName = it },
                        label = { Text(strings.customSlotNameLabel) },
                        placeholder = { Text("Ej: Pre-Entreno, Merienda") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = strings.customSlotSuggestions,
                        style = MaterialTheme.typography.labelSmall,
                        color = AetherTextMuted,
                        fontSize = 10.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        quickSlotSuggestions.take(3).forEach { suggestion ->
                            SuggestionChip(
                                onClick = { customSlotName = suggestion },
                                label = { Text(suggestion, fontSize = 9.sp) }
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        quickSlotSuggestions.drop(3).forEach { suggestion ->
                            SuggestionChip(
                                onClick = { customSlotName = suggestion },
                                label = { Text(suggestion, fontSize = 9.sp) }
                            )
                        }
                    }
                }

                // MACROS (Protein, Carbs, Fats)
                Text(
                    text = "MACRONUTRIENTES (g)",
                    style = MaterialTheme.typography.labelSmall,
                    color = AetherAmber,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedTextField(
                        value = proteinStr,
                        onValueChange = { if (it.all { c -> c.isDigit() }) proteinStr = it.take(3) },
                        label = { Text("Prot (g)", color = AetherCyan, fontSize = 10.sp) },
                        placeholder = { Text("30") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = carbsStr,
                        onValueChange = { if (it.all { c -> c.isDigit() }) carbsStr = it.take(3) },
                        label = { Text("Carb (g)", color = AetherAmber, fontSize = 10.sp) },
                        placeholder = { Text("45") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = fatStr,
                        onValueChange = { if (it.all { c -> c.isDigit() }) fatStr = it.take(3) },
                        label = { Text("Gras (g)", color = AetherCoral, fontSize = 10.sp) },
                        placeholder = { Text("15") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Live Calories Computation Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = AetherSurface),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = strings.macroCalculatedKcal,
                            style = MaterialTheme.typography.labelSmall,
                            color = AetherTextMuted
                        )
                        Text(
                            text = "🔥 $computedKcal kcal",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (computedKcal > 0) AetherAmber else AetherTextMuted
                        )
                    }
                }

                // Prep Time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${strings.mealPrepTimeLabel}: ${prepTimeMinutes} min",
                        style = MaterialTheme.typography.bodySmall,
                        color = AetherTextPrimary
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { if (prepTimeMinutes > 2) prepTimeMinutes -= 2 },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Remove, contentDescription = "Menos", tint = AetherCyan)
                        }
                        IconButton(
                            onClick = { prepTimeMinutes += 2 },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Más", tint = AetherCyan)
                        }
                    }
                }

                OutlinedTextField(
                    value = ingredientsRaw,
                    onValueChange = { ingredientsRaw = it },
                    label = { Text(strings.mealIngredientsLabel) },
                    placeholder = {
                        Text(if (language == AppLanguage.SPANISH) "Huevos, Espinacas, Aceite de Oliva" else "Eggs, Spinach, Olive Oil")
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.mealUsesBatchBase,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = usesBatchCookedBase,
                        onCheckedChange = { usesBatchCookedBase = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = AetherCyan)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.mealInStockCheck,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = allIngredientsInStock,
                        onCheckedChange = { allIngredientsInStock = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = AetherEmerald)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val ings = ingredientsRaw.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        val finalCustomSlot = if (slot == MealSlot.CUSTOM) {
                            customSlotName.trim().ifEmpty { if (isSpanish) "Momento Especial" else "Special Moment" }
                        } else null

                        onSave(
                            slot,
                            title.trim(),
                            description.trim(),
                            prepTimeMinutes,
                            ings,
                            usesBatchCookedBase,
                            allIngredientsInStock,
                            bioImpact,
                            finalCustomSlot,
                            protein,
                            carbs,
                            fat,
                            computedKcal
                        )
                    }
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = AetherAmber, contentColor = Color(0xFF3B2D00))
            ) {
                Text(strings.btnSave, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(strings.btnCancel, color = AetherTextSecondary) }
        },
        containerColor = AetherSurfaceElevated
    )
}

@Composable
fun DuplicateMealDialog(
    meal: MealItem,
    language: AppLanguage = AppLanguage.SPANISH,
    onDismiss: () -> Unit,
    onDuplicate: (targetOffsetDays: Int) -> Unit
) {
    val strings = remember(language) { StringsProvider(language) }
    var selectedOffset by remember { mutableIntStateOf(1) } // Default tomorrow

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = null,
                tint = AetherAmber,
                modifier = Modifier.size(28.dp)
            )
        },
        title = {
            Text(
                text = strings.duplicateMealTitle,
                style = MaterialTheme.typography.titleMedium,
                color = AetherTextPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = strings.duplicateMealSubtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = AetherTextSecondary
                )

                // Meal preview chip
                Card(
                    colors = CardDefaults.cardColors(containerColor = AetherSurfaceCard),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = meal.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = AetherTextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "P: ${meal.proteinGrams}g • C: ${meal.carbsGrams}g • G: ${meal.fatGrams}g",
                                style = MaterialTheme.typography.labelSmall,
                                color = AetherCyan,
                                fontSize = 10.sp
                            )
                            Text(
                                text = "• ${meal.computedCalories} kcal",
                                style = MaterialTheme.typography.labelSmall,
                                color = AetherAmber,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Quick Target Day Options
                val options = listOf(
                    1 to strings.duplicateTomorrow,
                    2 to strings.duplicateIn2Days,
                    3 to strings.duplicateIn3Days,
                    0 to strings.duplicateToday
                )

                options.forEach { (offset, label) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(),
                        onClick = { selectedOffset = offset },
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedOffset == offset) AetherAmber.copy(alpha = 0.18f) else AetherSurfaceCard
                        ),
                        border = if (selectedOffset == offset) androidx.compose.foundation.BorderStroke(1.dp, AetherAmber) else null,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (selectedOffset == offset) AetherAmber else AetherTextPrimary,
                                fontWeight = if (selectedOffset == offset) FontWeight.Bold else FontWeight.Normal
                            )
                            RadioButton(
                                selected = selectedOffset == offset,
                                onClick = { selectedOffset = offset },
                                colors = RadioButtonDefaults.colors(selectedColor = AetherAmber)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onDuplicate(selectedOffset) },
                colors = ButtonDefaults.buttonColors(containerColor = AetherAmber, contentColor = Color(0xFF3B2D00))
            ) {
                Text(strings.btnDuplicateMeal, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(strings.btnCancel, color = AetherTextSecondary) }
        },
        containerColor = AetherSurfaceElevated
    )
}


@Composable
fun AddEditHabitDialog(
    initialHabit: HabitAnchor? = null,
    language: AppLanguage = AppLanguage.SPANISH,
    onDismiss: () -> Unit,
    onSave: (title: String, desc: String, anchor: CircadianAnchor, streakDays: Int, reframingTip: String) -> Unit
) {
    val strings = remember(language) { StringsProvider(language) }
    var title by remember { mutableStateOf(initialHabit?.title ?: "") }
    var description by remember { mutableStateOf(initialHabit?.description ?: "") }
    var anchor by remember { mutableStateOf(initialHabit?.anchor ?: CircadianAnchor.MORNING_LIGHT) }
    var streakDays by remember { mutableIntStateOf(initialHabit?.streakDays ?: 0) }
    var reframingTip by remember { 
        mutableStateOf(
            initialHabit?.reframingTip ?: if (language == AppLanguage.SPANISH) 
                "La consistencia biológica es un patrón de retorno, no de perfección." 
            else 
                "Biological consistency is a pattern of return, not perfection."
        ) 
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (initialHabit != null) Icons.Default.Edit else Icons.Default.Spa,
                    contentDescription = null,
                    tint = AetherEmerald
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (initialHabit != null) strings.editHabitTitle else strings.addHabitTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = AetherTextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(strings.habitTitleLabel) },
                    placeholder = {
                        Text(if (language == AppLanguage.SPANISH) "Ej: Luz Solar Temprana (10 min)" else "e.g. Early Sunlight Exposure (10 min)")
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(strings.habitDescLabel) },
                    placeholder = {
                        Text(if (language == AppLanguage.SPANISH) "Ej: Salir a la terraza o parque en los primeros 30 min" else "e.g. Step outside within 30 min of waking")
                    },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = strings.habitAnchorLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = AetherCyan,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    CircadianAnchor.entries.forEach { a ->
                        val label = when (a) {
                            CircadianAnchor.MORNING_LIGHT -> if (language == AppLanguage.SPANISH) "Luz Solar" else "Morning Light"
                            CircadianAnchor.HYDRATION_ELECTROLYTES -> if (language == AppLanguage.SPANISH) "Hidratación" else "Hydration"
                            CircadianAnchor.ZONE_2_MOVEMENT -> if (language == AppLanguage.SPANISH) "Zona 2" else "Zone 2"
                            CircadianAnchor.CAFFEINE_CUTOFF -> if (language == AppLanguage.SPANISH) "Corte Café" else "Caffeine"
                            CircadianAnchor.DIGITAL_SUNSET -> if (language == AppLanguage.SPANISH) "Ocaso Digital" else "Sunset"
                        }
                        FilterChip(
                            selected = anchor == a,
                            onClick = { anchor = a },
                            label = { Text(label, fontSize = 9.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.habitStreakLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = AetherTextSecondary
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = { if (streakDays > 0) streakDays-- },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "-", tint = AetherCyan)
                        }
                        Text(
                            text = "$streakDays",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = AetherEmerald
                        )
                        IconButton(
                            onClick = { streakDays++ },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "+", tint = AetherCyan)
                        }
                    }
                }

                OutlinedTextField(
                    value = reframingTip,
                    onValueChange = { reframingTip = it },
                    label = { Text(strings.habitTipLabel) },
                    placeholder = {
                        Text(if (language == AppLanguage.SPANISH) "Frase motivacional o científica" else "Motivational bio-principle")
                    },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(title, description, anchor, streakDays, reframingTip)
                    }
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = AetherEmerald, contentColor = Color(0xFF003919))
            ) {
                Text(strings.btnSave, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(strings.btnCancel, color = AetherTextSecondary) }
        },
        containerColor = AetherSurfaceElevated
    )
}

@Composable
fun ImportDietDialog(
    language: AppLanguage = AppLanguage.SPANISH,
    isLoading: Boolean = false,
    onDismiss: () -> Unit,
    onImport: (String) -> Unit
) {
    val isSpanish = language == AppLanguage.SPANISH
    var dietText by remember { mutableStateOf("") }

    val samplePlaceholder = if (isSpanish) {
        """Desayuno: Tortilla de 3 huevos con espinacas y aguacate (350 kcal, 25g proteina, 5g carbos, 22g grasa)
Almuerzo: 200g Salmón a la plancha con quinoa y brócoli (520 kcal, 40g proteina, 45g carbos, 16g grasa)
Cena: Pechuga de pavo con crema de calabacín (380 kcal, 35g proteina, 12g carbos, 10g grasa)
Snack: Yogur griego con nueces y arándanos (200 kcal, 15g proteina)"""
    } else {
        """Breakfast: 3 Scrambled Eggs with Avocado & Spinach (380 kcal, 28g protein, 8g carbs, 26g fat)
Lunch: Grilled Salmon with Quinoa Bowl (520 kcal, 40g protein, 45g carbs, 16g fat)
Dinner: Roasted Sweet Potato with Chicken & Veggies (450 kcal, 35g protein, 50g carbs, 12g fat)
Snack: Greek Yogurt with Berries and Walnuts (210 kcal, 15g protein)"""
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = AetherAmber
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isSpanish) "Importar Dieta desde IA" else "Import Diet from External AI",
                    style = MaterialTheme.typography.titleMedium,
                    color = AetherTextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (isSpanish) 
                        "Pega el menú sugerido por ChatGPT, Claude o tu nutricionista (en texto libre o JSON). Aether OS extraerá automáticamente los momentos, ingredientes y macronutrientes." 
                    else 
                        "Paste any meal plan generated by ChatGPT, Claude, or your coach (plain text or JSON). Aether OS will automatically parse slots, ingredients, and macros.",
                    style = MaterialTheme.typography.bodySmall,
                    color = AetherTextSecondary
                )

                OutlinedTextField(
                    value = dietText,
                    onValueChange = { dietText = it },
                    label = { Text(if (isSpanish) "Plan nutricional o JSON" else "Diet Plan or JSON") },
                    placeholder = { Text(samplePlaceholder, fontSize = 11.sp, color = AetherTextMuted) },
                    minLines = 6,
                    maxLines = 12,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AetherAmber,
                        unfocusedBorderColor = AetherBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("import_diet_input")
                )

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AetherAmber)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onImport(dietText) },
                enabled = dietText.isNotBlank() && !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = AetherAmber, contentColor = Color(0xFF3E2800)),
                modifier = Modifier.testTag("submit_import_diet_btn")
            ) {
                Text(if (isSpanish) "Importar Comidas" else "Import Meals", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isSpanish) "Cancelar" else "Cancel", color = AetherTextSecondary)
            }
        },
        containerColor = AetherSurfaceElevated
    )
}

@Composable
fun MorningCheckInDialog(
    unfinishedHabits: List<HabitAnchor>,
    unfinishedTasks: List<TaskItem>,
    language: AppLanguage,
    onConfirmHabit: (HabitAnchor) -> Unit,
    onConfirmTask: (TaskItem) -> Unit,
    onDismiss: () -> Unit
) {
    val isSpanish = language == AppLanguage.SPANISH
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🌅", fontSize = 24.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isSpanish) "Check-in Retroactivo" else "Morning Check-In",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AetherTextPrimary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (isSpanish)
                        "¿Completaste estas actividades anoche antes de dormir? Márcalas para proteger tu racha sin estrés."
                    else
                        "Did you complete these activities last night before sleeping? Log them to shield your streak guilt-free.",
                    style = MaterialTheme.typography.bodySmall,
                    color = AetherTextSecondary
                )

                if (unfinishedHabits.isNotEmpty()) {
                    Text(
                        text = if (isSpanish) "Hábitos de ayer:" else "Yesterday's Habits:",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium,
                        color = AetherCyan
                    )
                    unfinishedHabits.forEach { habit ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = AetherSurfaceCard,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = habit.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = AetherTextPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                Button(
                                    onClick = { onConfirmHabit(habit) },
                                    colors = ButtonDefaults.buttonColors(containerColor = AetherCyan, contentColor = Color(0xFF00363D)),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(if (isSpanish) "Hecho ✓" else "Done ✓", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                if (unfinishedTasks.isNotEmpty()) {
                    Text(
                        text = if (isSpanish) "Tareas de ayer:" else "Yesterday's Tasks:",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium,
                        color = AetherAmber
                    )
                    unfinishedTasks.forEach { task ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = AetherSurfaceCard,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = task.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = AetherTextPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                Button(
                                    onClick = { onConfirmTask(task) },
                                    colors = ButtonDefaults.buttonColors(containerColor = AetherAmber, contentColor = Color(0xFF3E2723)),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(if (isSpanish) "Hecho ✓" else "Done ✓", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = AetherCyan, contentColor = Color(0xFF00363D))
            ) {
                Text(if (isSpanish) "Continuar al Día de Hoy" else "Continue to Today", fontWeight = FontWeight.Bold)
            }
        },
        containerColor = AetherSurfaceElevated
    )
}

@Composable
fun BreathworkDialog(
    language: AppLanguage,
    onDismiss: () -> Unit
) {
    val isSpanish = language == AppLanguage.SPANISH
    var phase by remember { mutableStateOf(if (isSpanish) "Inhala (4s)" else "Inhale (4s)") }
    var secondsLeft by remember { mutableIntStateOf(4) }

    LaunchedEffect(Unit) {
        val phases = listOf(
            (if (isSpanish) "Inhala (4s)" else "Inhale (4s)") to 4,
            (if (isSpanish) "Mantén (4s)" else "Hold (4s)") to 4,
            (if (isSpanish) "Exhala (4s)" else "Exhale (4s)") to 4,
            (if (isSpanish) "Pausa (4s)" else "Pause (4s)") to 4
        )
        var idx = 0
        while (true) {
            val (name, duration) = phases[idx % phases.size]
            phase = name
            for (s in duration downTo 1) {
                secondsLeft = s
                kotlinx.coroutines.delay(1000L)
            }
            idx++
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Spa, contentDescription = null, tint = AetherCyan)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isSpanish) "Respiración Cuadrada (Box Breathing)" else "Box Breathing Protocol",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AetherTextPrimary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isSpanish) "Regulación parasimpática del nervio vago." else "Parasympathetic vagus nerve reset.",
                    style = MaterialTheme.typography.bodySmall,
                    color = AetherTextSecondary
                )
                Spacer(modifier = Modifier.height(24.dp))
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(AetherCyan.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = phase,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = AetherCyan
                        )
                        Text(
                            text = "$secondsLeft",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Black,
                            color = AetherTextPrimary
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = AetherCyan, contentColor = Color(0xFF00363D))
            ) {
                Text(if (isSpanish) "Finalizar Sesión" else "Finish Session", fontWeight = FontWeight.Bold)
            }
        },
        containerColor = AetherSurfaceElevated
    )
}

@Composable
fun CompassionModeBanner(
    language: AppLanguage,
    onOpenBreathwork: () -> Unit,
    onDismissMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isSpanish = language == AppLanguage.SPANISH
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("compassion_mode_banner"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2E1C0A)),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, AetherAmber.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🛡️", fontSize = 28.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isSpanish) "Modo Compasión y Recuperación" else "Compassion & Recovery Mode",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = AetherAmber
                )
                Text(
                    text = if (isSpanish)
                        "Readiness bajo detectado. Se han pausado las exigencias altas. Prioriza tu bienestar hoy."
                    else
                        "Low readiness detected. High demands suspended. Prioritize your well-being today.",
                    style = MaterialTheme.typography.bodySmall,
                    color = AetherTextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onOpenBreathwork,
                        colors = ButtonDefaults.buttonColors(containerColor = AetherAmber, contentColor = Color(0xFF3E2723)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Spa, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isSpanish) "Respiración 4x4" else "Box Breathing", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = onDismissMode,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AetherTextMuted)
                    ) {
                        Text(if (isSpanish) "Entendido" else "Dismiss", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

