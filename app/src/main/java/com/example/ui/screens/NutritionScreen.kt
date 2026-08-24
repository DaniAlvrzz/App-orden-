package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.StringsProvider
import com.example.ui.theme.*
import com.example.ui.viewmodel.AetherUiState

@Composable
fun NutritionScreen(
    state: AetherUiState,
    onToggleMeal: (MealItem) -> Unit,
    onDeleteMeal: (String) -> Unit = {},
    onOpenAddMeal: () -> Unit = {},
    onTogglePantryStock: (String, Boolean) -> Unit,
    onDeletePantryItem: (String) -> Unit,
    onOpenAddPantry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = remember(state.currentLanguage) { StringsProvider(state.currentLanguage) }
    var selectedSection by remember { mutableIntStateOf(0) } // 0: Meals, 1: Despensa / Stock, 2: Batch Cooking

    val missingItems = state.pantryItems.filter { !it.inStock }
    val batchBases = state.pantryItems.filter { it.isBatchBase }

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
                        text = strings.nutritionHeader,
                        style = MaterialTheme.typography.titleLarge,
                        color = AetherCyan,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = strings.nutritionSub,
                        style = MaterialTheme.typography.labelSmall,
                        color = AetherTextMuted
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (selectedSection == 0) {
                        FilledTonalButton(
                            onClick = onOpenAddMeal,
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = AetherAmber.copy(alpha = 0.2f),
                                contentColor = AetherAmber
                            ),
                            modifier = Modifier.testTag("add_meal_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(strings.addMealTitle)
                        }
                    } else {
                        FilledTonalButton(
                            onClick = onOpenAddPantry,
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = AetherCyan.copy(alpha = 0.15f),
                                contentColor = AetherCyan
                            ),
                            modifier = Modifier.testTag("add_pantry_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(strings.btnAddPantry)
                        }
                    }
                }
            }
        }

        // Section Tabs
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedSection == 0,
                    onClick = { selectedSection = 0 },
                    label = { Text("${strings.tabDailyMeals} (${state.meals.size})", fontSize = 11.sp) },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = selectedSection == 1,
                    onClick = { selectedSection = 1 },
                    label = { Text("${strings.tabPantry} (${state.pantryItems.size})", fontSize = 11.sp) },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = selectedSection == 2,
                    onClick = { selectedSection = 2 },
                    label = { Text("${strings.tabBatchBases} (${batchBases.size})", fontSize = 11.sp) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // SECTION 0: DAILY MEALS
        if (selectedSection == 0) {
            if (state.meals.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = AetherSurfaceElevated),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Restaurant,
                                contentDescription = null,
                                tint = AetherAmber.copy(alpha = 0.8f),
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = strings.emptyMealsClean,
                                style = MaterialTheme.typography.bodyMedium,
                                color = AetherTextSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = onOpenAddMeal,
                                colors = ButtonDefaults.buttonColors(containerColor = AetherAmber, contentColor = Color(0xFF3B2D00)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(strings.addMealTitle, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                items(state.meals, key = { it.id }) { meal ->
                    MealCard(
                        meal = meal,
                        language = state.currentLanguage,
                        onToggle = { onToggleMeal(meal) },
                        onDelete = { onDeleteMeal(meal.id) }
                    )
                }
            }
        }

        // SECTION 1: PANTRY INVENTORY & MISSING ITEMS
        if (selectedSection == 1) {
            // Missing Stock / Shopping Alert
            if (missingItems.isNotEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = AetherCoral.copy(alpha = 0.12f)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = null, tint = AetherCoral)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${strings.shoppingListTitle} (${missingItems.size})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AetherCoral,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            missingItems.forEach { item ->
                                Text(
                                    text = "• ${item.name} (${item.quantityDesc})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AetherTextPrimary
                                )
                            }
                        }
                    }
                }
            }

            items(state.pantryItems, key = { it.id }) { item ->
                PantryItemCard(
                    item = item,
                    language = state.currentLanguage,
                    onToggleStock = { onTogglePantryStock(item.id, item.inStock) },
                    onDelete = { onDeletePantryItem(item.id) }
                )
            }
        }

        // SECTION 2: BATCH COOKING BASES
        if (selectedSection == 2) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = AetherEmerald.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Kitchen, contentDescription = null, tint = AetherEmerald)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = strings.batchCookTitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = AetherEmerald,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = strings.batchCookDesc,
                            style = MaterialTheme.typography.bodySmall,
                            color = AetherTextSecondary
                        )
                    }
                }
            }

            items(batchBases, key = { it.id }) { base ->
                PantryItemCard(
                    item = base,
                    language = state.currentLanguage,
                    onToggleStock = { onTogglePantryStock(base.id, base.inStock) },
                    onDelete = { onDeletePantryItem(base.id) }
                )
            }
        }
    }
}

@Composable
fun MealCard(
    meal: MealItem,
    language: AppLanguage,
    onToggle: () -> Unit,
    onDelete: () -> Unit = {}
) {
    val strings = remember(language) { StringsProvider(language) }
    val isSpanish = language == AppLanguage.SPANISH

    val slotLabel = when (meal.slot) {
        MealSlot.BREAKFAST -> if (isSpanish) "DESAYUNO" else "BREAKFAST"
        MealSlot.LUNCH -> if (isSpanish) "ALMUERZO" else "LUNCH"
        MealSlot.DINNER -> if (isSpanish) "CENA" else "DINNER"
        MealSlot.SNACK -> if (isSpanish) "SNACK BIO" else "SNACK"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("meal_card_${meal.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (meal.isCompleted) AetherSurface.copy(alpha = 0.6f) else AetherSurfaceCard
        ),
        shape = RoundedCornerShape(16.dp)
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
                            .background(AetherCyan.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = slotLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = AetherCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (meal.usesBatchCookedBase) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(AetherEmerald.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = strings.badgeBatchBase,
                                style = MaterialTheme.typography.labelSmall,
                                color = AetherEmerald,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${meal.prepTimeMinutes}m ${strings.prepMinutesSuffix}",
                        style = MaterialTheme.typography.labelSmall,
                        color = AetherTextMuted
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete Meal",
                            tint = AetherTextMuted.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = meal.isCompleted,
                    onCheckedChange = { onToggle() },
                    colors = CheckboxDefaults.colors(checkedColor = AetherEmerald, uncheckedColor = AetherBorderLight)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = meal.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (meal.isCompleted) AetherTextMuted else AetherTextPrimary,
                        textDecoration = if (meal.isCompleted) TextDecoration.LineThrough else null,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = meal.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = AetherTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Ingredients Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                meal.ingredients.take(4).forEach { ing ->
                    Box(
                        modifier = Modifier
                            .background(AetherSurfaceElevated, RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = ing,
                            style = MaterialTheme.typography.labelSmall,
                            color = AetherTextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PantryItemCard(
    item: PantryItem,
    language: AppLanguage,
    onToggleStock: () -> Unit,
    onDelete: () -> Unit
) {
    val strings = remember(language) { StringsProvider(language) }
    val isSpanish = language == AppLanguage.SPANISH

    val categoryLabel = when (item.category) {
        PantryCategory.PROTEIN -> if (isSpanish) "Proteína" else "Protein"
        PantryCategory.CARB_BASE -> if (isSpanish) "Base de Carbohidrato" else "Carb Base"
        PantryCategory.HEALTHY_FAT -> if (isSpanish) "Grasa Saludable" else "Healthy Fat"
        PantryCategory.PRODUCE -> if (isSpanish) "Vegetales / Fruta" else "Produce"
        PantryCategory.SEASONING -> if (isSpanish) "Especias / Salsas" else "Seasoning / Spices"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("pantry_item_${item.id}"),
        colors = CardDefaults.cardColors(containerColor = AetherSurfaceCard),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (item.inStock) AetherTextPrimary else AetherTextMuted,
                        fontWeight = FontWeight.Medium
                    )
                    if (item.isBatchBase) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "[Base]",
                            style = MaterialTheme.typography.labelSmall,
                            color = AetherEmerald,
                            fontSize = 10.sp
                        )
                    }
                }
                Text(
                    text = "$categoryLabel • ${item.quantityDesc}",
                    style = MaterialTheme.typography.labelSmall,
                    color = AetherTextMuted,
                    fontSize = 10.sp
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                FilterChip(
                    selected = item.inStock,
                    onClick = onToggleStock,
                    label = { Text(if (item.inStock) strings.inStockLabel else strings.neededLabel, fontSize = 10.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AetherEmerald.copy(alpha = 0.2f),
                        selectedLabelColor = AetherEmerald
                    )
                )
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Delete", tint = AetherTextMuted, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
