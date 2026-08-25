package com.example.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import com.example.ui.components.AetherSwipeToDismissContainer
import com.example.ui.components.DuplicateMealDialog
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.StringsProvider
import com.example.ui.theme.*
import com.example.ui.viewmodel.AetherUiState

@Composable
fun NutritionScreen(
    state: AetherUiState,
    onToggleMeal: (MealItem) -> Unit,
    onDeleteMeal: (MealItem) -> Unit = {},
    onEditMeal: (MealItem) -> Unit = {},
    onDuplicateMeal: (MealItem, Int) -> Unit = { _, _ -> },
    onOpenAddMeal: () -> Unit = {},
    onTogglePantryStock: (String, Boolean) -> Unit,
    onDeletePantryItem: (PantryItem) -> Unit,
    onEditPantryItem: (PantryItem) -> Unit = {},
    onOpenAddPantry: () -> Unit,
    onOpenHistory: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val strings = remember(state.currentLanguage) { StringsProvider(state.currentLanguage) }
    var selectedSection by remember { mutableIntStateOf(0) } // 0: Meals, 1: Despensa / Stock, 2: Batch Cooking
    var mealToDuplicate by remember { mutableStateOf<MealItem?>(null) }

    val missingItems = state.pantryItems.filter { !it.inStock }
    val batchBases = state.pantryItems.filter { it.isBatchBase }

    if (mealToDuplicate != null) {
        DuplicateMealDialog(
            meal = mealToDuplicate!!,
            language = state.currentLanguage,
            onDismiss = { mealToDuplicate = null },
            onDuplicate = { offsetDays ->
                onDuplicateMeal(mealToDuplicate!!, offsetDays)
                mealToDuplicate = null
            }
        )
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

                Spacer(modifier = Modifier.width(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onOpenHistory,
                        modifier = Modifier.testTag("nutrition_history_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = strings.historyTitle,
                            tint = AetherCyan
                        )
                    }

                    if (selectedSection == 0) {
                        FilledTonalButton(
                            onClick = onOpenAddMeal,
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = AetherAmber.copy(alpha = 0.2f),
                                contentColor = AetherAmber
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            modifier = Modifier.testTag("add_meal_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (state.currentLanguage == AppLanguage.SPANISH) "+ Comida" else "+ Meal",
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    } else {
                        FilledTonalButton(
                            onClick = onOpenAddPantry,
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = AetherCyan.copy(alpha = 0.15f),
                                contentColor = AetherCyan
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            modifier = Modifier.testTag("add_pantry_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (state.currentLanguage == AppLanguage.SPANISH) "+ Despensa" else "+ Stock",
                                fontWeight = FontWeight.SemiBold
                            )
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
            // Daily Macro Summary Card
            if (state.meals.isNotEmpty()) {
                item {
                    DailyMacroSummaryCard(
                        meals = state.meals,
                        language = state.currentLanguage
                    )
                }
            }

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
                    AetherSwipeToDismissContainer(
                        onDismiss = { onDeleteMeal(meal) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        MealCard(
                            meal = meal,
                            language = state.currentLanguage,
                            onToggle = { onToggleMeal(meal) },
                            onEdit = { onEditMeal(meal) },
                            onDuplicate = { mealToDuplicate = meal },
                            onDelete = { onDeleteMeal(meal) }
                        )
                    }
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
                AetherSwipeToDismissContainer(
                    onDismiss = { onDeletePantryItem(item) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    PantryItemCard(
                        item = item,
                        language = state.currentLanguage,
                        onToggleStock = { onTogglePantryStock(item.id, item.inStock) },
                        onEdit = { onEditPantryItem(item) },
                        onDelete = { onDeletePantryItem(item) }
                    )
                }
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
                AetherSwipeToDismissContainer(
                    onDismiss = { onDeletePantryItem(base) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    PantryItemCard(
                        item = base,
                        language = state.currentLanguage,
                        onToggleStock = { onTogglePantryStock(base.id, base.inStock) },
                        onEdit = { onEditPantryItem(base) },
                        onDelete = { onDeletePantryItem(base) }
                    )
                }
            }
        }
    }
}

/**
 * Visual Daily Macro Summary Card displaying:
 * - Total daily calories & macronutrients (Protein, Carbs, Fats)
 * - Proportional multi-segment colored bar with Aether palette
 */
@Composable
fun DailyMacroSummaryCard(
    meals: List<MealItem>,
    language: AppLanguage
) {
    val strings = remember(language) { StringsProvider(language) }

    val totalProtein = meals.sumOf { it.proteinGrams }
    val totalCarbs = meals.sumOf { it.carbsGrams }
    val totalFat = meals.sumOf { it.fatGrams }
    val totalKcal = meals.sumOf { it.computedCalories }

    val totalMacroGrams = (totalProtein + totalCarbs + totalFat).coerceAtLeast(1)
    val proteinRatio = (totalProtein.toFloat() / totalMacroGrams).coerceIn(0f, 1f)
    val carbsRatio = (totalCarbs.toFloat() / totalMacroGrams).coerceIn(0f, 1f)
    val fatRatio = (totalFat.toFloat() / totalMacroGrams).coerceIn(0f, 1f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("daily_macro_summary_card"),
        colors = CardDefaults.cardColors(containerColor = AetherSurfaceElevated),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Title & Total Calories
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = strings.macroSummaryTitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = AetherAmber,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = strings.macroSummarySub,
                        style = MaterialTheme.typography.labelSmall,
                        color = AetherTextMuted,
                        fontSize = 10.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(AetherAmber.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = AetherAmber,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$totalKcal ${strings.macroCaloriesSuffix}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = AetherAmber
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Multi-segment Proportional Macro Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(AetherBorderLight)
            ) {
                if (proteinRatio > 0f) {
                    Box(
                        modifier = Modifier
                            .weight(proteinRatio.coerceAtLeast(0.01f))
                            .fillMaxHeight()
                            .background(AetherCyan)
                    )
                }
                if (carbsRatio > 0f) {
                    Box(
                        modifier = Modifier
                            .weight(carbsRatio.coerceAtLeast(0.01f))
                            .fillMaxHeight()
                            .background(AetherAmber)
                    )
                }
                if (fatRatio > 0f) {
                    Box(
                        modifier = Modifier
                            .weight(fatRatio.coerceAtLeast(0.01f))
                            .fillMaxHeight()
                            .background(AetherCoral)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3 Column Macro Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MacroPill(
                    label = strings.macroProteinLabel,
                    grams = totalProtein,
                    ratioPercent = (proteinRatio * 100).toInt(),
                    color = AetherCyan,
                    modifier = Modifier.weight(1f)
                )
                MacroPill(
                    label = strings.macroCarbsLabel,
                    grams = totalCarbs,
                    ratioPercent = (carbsRatio * 100).toInt(),
                    color = AetherAmber,
                    modifier = Modifier.weight(1f)
                )
                MacroPill(
                    label = strings.macroFatLabel,
                    grams = totalFat,
                    ratioPercent = (fatRatio * 100).toInt(),
                    color = AetherCoral,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun MacroPill(
    label: String,
    grams: Int,
    ratioPercent: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = color,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$ratioPercent%",
                    style = MaterialTheme.typography.labelSmall,
                    color = AetherTextMuted,
                    fontSize = 9.sp
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${grams}g",
                style = MaterialTheme.typography.titleSmall,
                color = AetherTextPrimary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MealCard(
    meal: MealItem,
    language: AppLanguage,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    val strings = remember(language) { StringsProvider(language) }
    val isSpanish = language == AppLanguage.SPANISH

    val slotLabel = when (meal.slot) {
        MealSlot.BREAKFAST -> if (isSpanish) "DESAYUNO" else "BREAKFAST"
        MealSlot.LUNCH -> if (isSpanish) "ALMUERZO" else "LUNCH"
        MealSlot.DINNER -> if (isSpanish) "CENA" else "DINNER"
        MealSlot.SNACK -> if (isSpanish) "SNACK BIO" else "BIO SNACK"
        MealSlot.CUSTOM -> meal.customSlotName?.uppercase() ?: (if (isSpanish) "MOMENTO" else "CUSTOM")
    }

    val mealTotalGrams = (meal.proteinGrams + meal.carbsGrams + meal.fatGrams).coerceAtLeast(1)
    val hasMacros = meal.proteinGrams > 0 || meal.carbsGrams > 0 || meal.fatGrams > 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onToggle,
                onLongClick = onEdit
            )
            .testTag("meal_card_${meal.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (meal.isCompleted) AetherSurface.copy(alpha = 0.6f) else AetherSurfaceCard
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Bar: Slot label, Badges, and Quick Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                if (meal.slot == MealSlot.CUSTOM) AetherAmber.copy(alpha = 0.2f) else AetherCyan.copy(alpha = 0.15f),
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = slotLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (meal.slot == MealSlot.CUSTOM) AetherAmber else AetherCyan,
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

                    if (!meal.allIngredientsInStock) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(AetherCoral.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.WarningAmber,
                                    contentDescription = "Missing Ingredients",
                                    tint = AetherCoral,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = if (isSpanish) "⚠️ Faltan ingr." else "⚠️ Missing",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AetherCoral,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${meal.prepTimeMinutes}m ${strings.prepMinutesSuffix}",
                        style = MaterialTheme.typography.labelSmall,
                        color = AetherTextMuted
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = onDuplicate,
                        modifier = Modifier.size(26.dp).testTag("duplicate_meal_${meal.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = strings.btnDuplicateMeal,
                            tint = AetherCyan,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(26.dp).testTag("edit_meal_${meal.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Meal",
                            tint = AetherTextMuted,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(26.dp).testTag("delete_meal_${meal.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete Meal",
                            tint = AetherCoral.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Checkbox and Title
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = meal.isCompleted,
                    onCheckedChange = { onToggle() },
                    colors = CheckboxDefaults.colors(checkedColor = AetherEmerald, uncheckedColor = AetherBorderLight),
                    modifier = Modifier.testTag("meal_checkbox_${meal.id}")
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
                    if (meal.description.isNotBlank()) {
                        Text(
                            text = meal.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = AetherTextSecondary
                        )
                    }
                }
            }

            // Macro Visualization per Meal
            if (hasMacros) {
                Spacer(modifier = Modifier.height(8.dp))

                // Proportional Mini-Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(AetherBorderLight)
                ) {
                    if (meal.proteinGrams > 0) {
                        Box(
                            modifier = Modifier
                                .weight((meal.proteinGrams.toFloat() / mealTotalGrams).coerceAtLeast(0.01f))
                                .fillMaxHeight()
                                .background(AetherCyan)
                        )
                    }
                    if (meal.carbsGrams > 0) {
                        Box(
                            modifier = Modifier
                                .weight((meal.carbsGrams.toFloat() / mealTotalGrams).coerceAtLeast(0.01f))
                                .fillMaxHeight()
                                .background(AetherAmber)
                        )
                    }
                    if (meal.fatGrams > 0) {
                        Box(
                            modifier = Modifier
                                .weight((meal.fatGrams.toFloat() / mealTotalGrams).coerceAtLeast(0.01f))
                                .fillMaxHeight()
                                .background(AetherCoral)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Macro badges
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🍗 P: ${meal.proteinGrams}g",
                        style = MaterialTheme.typography.labelSmall,
                        color = AetherCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "🌾 C: ${meal.carbsGrams}g",
                        style = MaterialTheme.typography.labelSmall,
                        color = AetherAmber,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "🥑 G: ${meal.fatGrams}g",
                        style = MaterialTheme.typography.labelSmall,
                        color = AetherCoral,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "🔥 ${meal.computedCalories} kcal",
                        style = MaterialTheme.typography.labelSmall,
                        color = AetherTextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Ingredients Chips
            if (meal.ingredients.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
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
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PantryItemCard(
    item: PantryItem,
    language: AppLanguage,
    onToggleStock: () -> Unit,
    onEdit: () -> Unit,
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
            .combinedClickable(
                onClick = onToggleStock,
                onLongClick = onEdit
            )
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
                IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = AetherTextMuted, modifier = Modifier.size(14.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "Delete", tint = AetherCoral.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
