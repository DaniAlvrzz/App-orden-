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
import androidx.compose.ui.window.Dialog
import com.example.data.model.*
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.StringsProvider
import com.example.ui.theme.*

@Composable
fun QuickAddTaskDialog(
    language: AppLanguage = AppLanguage.SPANISH,
    onDismiss: () -> Unit,
    onSave: (title: String, desc: String, energy: EnergyLevel, priority: PriorityType, minutes: Int, category: String, makeFrog: Boolean) -> Unit
) {
    val strings = remember(language) { StringsProvider(language) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var energyLevel by remember { mutableStateOf(EnergyLevel.MEDIUM) }
    var priorityType by remember { mutableStateOf(PriorityType.MEDIUM) }
    var estimatedMinutes by remember { mutableIntStateOf(30) }
    var category by remember { mutableStateOf(if (language == AppLanguage.SPANISH) "Trabajo Profundo" else "Deep Work") }
    var isFrog by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Bolt, contentDescription = null, tint = AetherCyan)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = strings.quickAddTitle,
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
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(strings.taskTitleLabel) },
                    placeholder = { 
                        Text(if (language == AppLanguage.SPANISH) "Ej: Diseñar arquitectura del motor" else "e.g. Write core dispatch tests") 
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
                            EnergyLevel.HIGH -> if (language == AppLanguage.SPANISH) "Alta" else "High"
                            EnergyLevel.MEDIUM -> if (language == AppLanguage.SPANISH) "Media" else "Medium"
                            EnergyLevel.LOW -> if (language == AppLanguage.SPANISH) "Baja" else "Low"
                        }
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                energyLevel = level
                                if (level == EnergyLevel.HIGH) {
                                    isFrog = true
                                    priorityType = PriorityType.FROG
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
                    text = strings.taskPriorityLabel,
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
                            },
                            label = {
                                Text(
                                    when (pType) {
                                        PriorityType.FROG -> if (language == AppLanguage.SPANISH) "1 Frog" else "1 Frog"
                                        PriorityType.MEDIUM -> if (language == AppLanguage.SPANISH) "3 Media" else "3 Medium"
                                        PriorityType.QUICK -> if (language == AppLanguage.SPANISH) "5 Rápida" else "5 Quick"
                                    },
                                    fontSize = 11.sp
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Duration Selector
                Text(
                    text = strings.taskDurationLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = AetherTextSecondary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(15, 30, 45, 90).forEach { mins ->
                        val isSelected = estimatedMinutes == mins
                        FilterChip(
                            selected = isSelected,
                            onClick = { estimatedMinutes = mins },
                            label = { Text("${mins}m") },
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
                        onSave(title, description, energyLevel, priorityType, estimatedMinutes, category, isFrog)
                    }
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = AetherCyan, contentColor = Color(0xFF00363D)),
                modifier = Modifier.testTag("save_task_btn")
            ) {
                Text(strings.btnCapture, fontWeight = FontWeight.Bold)
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
    language: AppLanguage = AppLanguage.SPANISH,
    onDismiss: () -> Unit,
    onSave: (name: String, category: PantryCategory, inStock: Boolean, isBatchBase: Boolean, qty: String) -> Unit
) {
    val strings = remember(language) { StringsProvider(language) }
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(PantryCategory.PROTEIN) }
    var inStock by remember { mutableStateOf(true) }
    var isBatchBase by remember { mutableStateOf(false) }
    var quantityDesc by remember { mutableStateOf(if (language == AppLanguage.SPANISH) "Disponible" else "Available") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = strings.addPantryTitle,
                style = MaterialTheme.typography.titleMedium,
                color = AetherTextPrimary,
                fontWeight = FontWeight.Bold
            )
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

@Composable
fun AddTimeBlockDialog(
    language: AppLanguage = AppLanguage.SPANISH,
    onDismiss: () -> Unit,
    onSave: (startTime: String, endTime: String, blockType: BlockType, title: String, notes: String) -> Unit
) {
    val strings = remember(language) { StringsProvider(language) }
    var startTime by remember { mutableStateOf("09:00") }
    var endTime by remember { mutableStateOf("10:30") }
    var blockType by remember { mutableStateOf(BlockType.DEEP_WORK) }
    var title by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Schedule, contentDescription = null, tint = AetherCyan)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = strings.addTimeBlockTitle,
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
                        listOf(BlockType.MEETING, BlockType.ADMIN_SLOT, BlockType.HABIT_ANCHOR).forEach { bType ->
                            val label = when (bType) {
                                BlockType.MEETING -> if (language == AppLanguage.SPANISH) "Reunión" else "Meeting"
                                BlockType.ADMIN_SLOT -> if (language == AppLanguage.SPANISH) "Admin" else "Admin"
                                BlockType.HABIT_ANCHOR -> if (language == AppLanguage.SPANISH) "Anclaje" else "Anchor"
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
                        onSave(startTime, endTime, blockType, title, notes)
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
    language: AppLanguage = AppLanguage.SPANISH,
    onDismiss: () -> Unit,
    onSave: (slot: MealSlot, title: String, desc: String, prepTime: Int, ingredients: List<String>, usesBatch: Boolean, inStock: Boolean, bioImpact: BioGlycemicImpact) -> Unit
) {
    val strings = remember(language) { StringsProvider(language) }
    var slot by remember { mutableStateOf(MealSlot.LUNCH) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var prepTimeMinutes by remember { mutableIntStateOf(10) }
    var ingredientsRaw by remember { mutableStateOf("") }
    var usesBatchCookedBase by remember { mutableStateOf(false) }
    var allIngredientsInStock by remember { mutableStateOf(true) }
    var bioImpact by remember { mutableStateOf(BioGlycemicImpact.MODERATE_STEADY) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Restaurant, contentDescription = null, tint = AetherAmber)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = strings.addMealTitle,
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

                Text(
                    text = strings.mealSlotLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = AetherCyan,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    MealSlot.entries.forEach { mSlot ->
                        val slotLabel = when (mSlot) {
                            MealSlot.BREAKFAST -> if (language == AppLanguage.SPANISH) "Desayuno" else "Breakfast"
                            MealSlot.LUNCH -> if (language == AppLanguage.SPANISH) "Almuerzo" else "Lunch"
                            MealSlot.DINNER -> if (language == AppLanguage.SPANISH) "Cena" else "Dinner"
                            MealSlot.SNACK -> if (language == AppLanguage.SPANISH) "Snack" else "Snack"
                        }
                        FilterChip(
                            selected = slot == mSlot,
                            onClick = { slot = mSlot },
                            label = { Text(slotLabel, fontSize = 10.sp) },
                            modifier = Modifier.weight(1f)
                        )
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
                        onSave(slot, title, description, prepTimeMinutes, ings, usesBatchCookedBase, allIngredientsInStock, bioImpact)
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
