package com.example.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.StringsProvider
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.AetherViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AetherApp(
    viewModel: AetherViewModel = viewModel(factory = AetherViewModel.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val strings = remember(state.currentLanguage) { StringsProvider(state.currentLanguage) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Status Message Feedback
    LaunchedEffect(state.statusMessage) {
        state.statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(
                message = msg,
                duration = SnackbarDuration.Short
            )
            viewModel.clearStatusMessage()
        }
    }

    // 5-Second Swipe-to-Dismiss / Delete Undo Snackbar
    LaunchedEffect(state.undoMessage) {
        state.undoMessage?.let { msg ->
            val actionLabel = if (state.currentLanguage == AppLanguage.SPANISH) "Deshacer" else "Undo"
            val result = snackbarHostState.showSnackbar(
                message = msg,
                actionLabel = actionLabel,
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.restoreLastDeletedItem()
            } else {
                viewModel.dismissUndo()
            }
        }
    }

    val navTitles = listOf(
        strings.tabNexus,
        strings.tabBacklog,
        strings.tabNutritionNav,
        strings.tabHabits,
        strings.tabAi
    )

    val navIcons = listOf(
        Icons.Default.Dashboard,
        Icons.Default.Bolt,
        Icons.Default.Restaurant,
        Icons.Default.WbSunny,
        Icons.Default.AutoAwesome
    )

    val navTags = listOf(
        "tab_nexus",
        "tab_backlog",
        "tab_nutrition",
        "tab_habits",
        "tab_ai"
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                containerColor = AetherSurface,
                contentColor = AetherTextPrimary,
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("bottom_navigation_bar")
            ) {
                navTitles.forEachIndexed { index, title ->
                    val isSelected = state.activeTab == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.selectTab(index) },
                        icon = {
                            Icon(
                                imageVector = navIcons[index],
                                contentDescription = title,
                                tint = if (isSelected) AetherCyan else AetherTextMuted
                            )
                        },
                        label = {
                            Text(
                                text = title,
                                fontSize = 10.sp,
                                color = if (isSelected) AetherCyan else AetherTextMuted
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AetherCyan,
                            selectedTextColor = AetherCyan,
                            unselectedIconColor = AetherTextMuted,
                            unselectedTextColor = AetherTextMuted,
                            indicatorColor = AetherCyan.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag(navTags[index])
                    )
                }
            }
        },
        containerColor = AetherDarkBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(AetherDarkBackground)
        ) {
            Crossfade(targetState = state.activeTab, label = "tab_crossfade") { tab ->
                when (tab) {
                    0 -> NexusScreen(
                        state = state,
                        onReadinessChanged = { viewModel.updateReadiness(it) },
                        onChronotypeChanged = { viewModel.updateChronotype(it) },
                        onToggleRecoveryMode = { viewModel.toggleRecoveryMode() },
                        onOrchestrateClick = { viewModel.triggerOrchestration() },
                        onToggleTask = { viewModel.toggleTask(it) },
                        onStartFocus = {
                            viewModel.startFocusTimer(it)
                            viewModel.selectTab(1)
                        },
                        onAddTaskClick = { viewModel.setShowQuickAdd(true) },
                        onEditTask = { viewModel.setEditingTask(it) },
                        onDeleteTask = { viewModel.deleteTaskWithUndo(it) },
                        onMoveMediumTask = { from, to -> viewModel.moveMediumTask(from, to) },
                        onMoveQuickTask = { from, to -> viewModel.moveQuickTask(from, to) },
                        onToggleTimeBlock = { viewModel.toggleTimeBlock(it) },
                        onAddTimeBlockClick = { viewModel.setShowAddTimeBlock(true) },
                        onEditTimeBlock = { viewModel.setEditingTimeBlock(it) },
                        onDeleteTimeBlock = { viewModel.deleteTimeBlockWithUndo(it) },
                        onMoveTimeBlock = { from, to -> viewModel.moveTimeBlock(from, to) },
                        onOpenReframe = { viewModel.setShowReframe(true) },
                        onOpenHistory = { viewModel.openHistory() },
                        onOpenSettings = { viewModel.openSettings() },
                        onOpenTutorial = { viewModel.openTutorial(0) },
                        onToggleLanguage = {
                            val nextLang = if (state.currentLanguage == AppLanguage.SPANISH) AppLanguage.ENGLISH else AppLanguage.SPANISH
                            viewModel.setLanguage(nextLang)
                        }
                    )
                    1 -> BacklogScreen(
                        state = state,
                        onToggleTask = { viewModel.toggleTask(it) },
                        onPromoteToFrog = { viewModel.promoteToFrog(it) },
                        onDeleteTask = { viewModel.deleteTaskWithUndo(it) },
                        onEditTask = { viewModel.setEditingTask(it) },
                        onMoveTask = { from, to -> viewModel.moveTask(from, to) },
                        onSetEnergyFilter = { viewModel.setEnergyFilter(it) },
                        onSetSearchQuery = { viewModel.setSearchQuery(it) },
                        onStartFocusTimer = { viewModel.startFocusTimer(it) },
                        onPauseFocusTimer = { viewModel.stopFocusTimer() },
                        onResetFocusTimer = { viewModel.resetFocusTimer() },
                        onOpenQuickAdd = { viewModel.setShowQuickAdd(true) },
                        onOpenHistory = { viewModel.openHistory() }
                    )
                    2 -> NutritionScreen(
                        state = state,
                        onToggleMeal = { viewModel.toggleMeal(it) },
                        onDeleteMeal = { viewModel.deleteMealWithUndo(it) },
                        onEditMeal = { viewModel.setEditingMeal(it) },
                        onDuplicateMeal = { meal, offset -> viewModel.duplicateMeal(meal, offset) },
                        onOpenAddMeal = { viewModel.setShowAddMeal(true) },
                        onTogglePantryStock = { id, inStock -> viewModel.togglePantryStock(id, inStock) },
                        onDeletePantryItem = { viewModel.deletePantryItemWithUndo(it) },
                        onEditPantryItem = { viewModel.setEditingPantryItem(it) },
                        onOpenAddPantry = { viewModel.setShowPantryAdd(true) },
                        onOpenHistory = { viewModel.openHistory() }
                    )
                    3 -> HabitsScreen(
                        state = state,
                        onToggleHabit = { viewModel.toggleHabit(it) },
                        onApplyGraceDay = { viewModel.applyGraceDay(it) },
                        onEditHabit = { viewModel.setEditingHabit(it) },
                        onDeleteHabit = { viewModel.deleteHabitWithUndo(it) },
                        onOpenAddHabit = { viewModel.setShowAddHabit(true) },
                        onOpenReframe = { viewModel.setShowReframe(true) },
                        onOpenHistory = { viewModel.openHistory() },
                        onOpenAchievements = { viewModel.setShowAchievementsDialog(true) }
                    )
                    4 -> AetherAiScreen(
                        state = state,
                        onOrchestrate = { viewModel.triggerOrchestration() },
                        onRequestReframe = { viewModel.requestCognitiveReframe(it) },
                        onToggleRecovery = { viewModel.toggleRecoveryMode() },
                        onUpdateChronotype = { viewModel.updateChronotype(it) },
                        onSendChatMessage = { viewModel.sendChatMessage(it) },
                        onSendQuickAction = { viewModel.sendQuickAction(it) },
                        onToggleFavorite = { viewModel.toggleAiMessageFavorite(it) },
                        onDeleteMessage = { viewModel.deleteAiMessage(it) },
                        onClearChatHistory = { viewModel.clearAiChatHistory() },
                        onSelectTab = { viewModel.setAiTab(it) },
                        getExportJson = { viewModel.getExportJson() }
                    )
                }
            }

            // 1. Task Creation Dialog
            if (state.showQuickAddDialog) {
                QuickAddTaskDialog(
                    initialTask = null,
                    language = state.currentLanguage,
                    onDismiss = { viewModel.setShowQuickAdd(false) },
                    onSave = { title, desc, energy, priority, minutes, category, makeFrog ->
                        viewModel.quickAddTask(title, desc, energy, priority, minutes, category, makeFrog)
                    }
                )
            }

            // 2. Task Edit Dialog
            state.editingTask?.let { taskToEdit: TaskItem ->
                QuickAddTaskDialog(
                    initialTask = taskToEdit,
                    language = state.currentLanguage,
                    onDismiss = { viewModel.setEditingTask(null) },
                    onSave = { title, desc, energy, priority, minutes, category, makeFrog ->
                        viewModel.updateTask(
                            taskToEdit.copy(
                                title = title,
                                description = desc,
                                energyLevel = energy,
                                priorityType = priority,
                                estimatedMinutes = minutes,
                                category = category,
                                isFrog = makeFrog
                            )
                        )
                    }
                )
            }

            // 3. TimeBlock Creation Dialog
            if (state.showAddTimeBlockDialog) {
                AddTimeBlockDialog(
                    initialBlock = null,
                    language = state.currentLanguage,
                    existingBlocks = state.timeBlocks,
                    onDismiss = { viewModel.setShowAddTimeBlock(false) },
                    onSave = { start, end, type, title, notes ->
                        viewModel.addTimeBlock(start, end, type, title, notes)
                    }
                )
            }

            // 4. TimeBlock Edit Dialog
            state.editingTimeBlock?.let { blockToEdit: TimeBlock ->
                AddTimeBlockDialog(
                    initialBlock = blockToEdit,
                    language = state.currentLanguage,
                    existingBlocks = state.timeBlocks,
                    onDismiss = { viewModel.setEditingTimeBlock(null) },
                    onSave = { start, end, type, title, notes ->
                        viewModel.updateTimeBlock(
                            blockToEdit.copy(
                                startTime = start,
                                endTime = end,
                                blockType = type,
                                title = title,
                                notes = notes
                            )
                        )
                    }
                )
            }

            // 5. Pantry Item Creation Dialog
            if (state.showPantryAddDialog) {
                AddPantryDialog(
                    initialItem = null,
                    language = state.currentLanguage,
                    onDismiss = { viewModel.setShowPantryAdd(false) },
                    onSave = { name, cat, inStock, isBase, qty ->
                        viewModel.addPantryItem(name, cat, inStock, isBase, qty)
                    }
                )
            }

            // 6. Pantry Item Edit Dialog
            state.editingPantryItem?.let { pantryToEdit: PantryItem ->
                AddPantryDialog(
                    initialItem = pantryToEdit,
                    language = state.currentLanguage,
                    onDismiss = { viewModel.setEditingPantryItem(null) },
                    onSave = { name, cat, inStock, isBase, qty ->
                        viewModel.updatePantryItem(
                            pantryToEdit.copy(
                                name = name,
                                category = cat,
                                inStock = inStock,
                                isBatchBase = isBase,
                                quantityDesc = qty
                            )
                        )
                    }
                )
            }

            // 7. Custom Meal Creation Dialog
            if (state.showAddMealDialog) {
                AddMealDialog(
                    initialMeal = null,
                    language = state.currentLanguage,
                    onDismiss = { viewModel.setShowAddMeal(false) },
                    onSave = { slot, title, desc, prepTime, ings, usesBatch, inStock, impact, customSlot, protein, carbs, fat, calories ->
                        viewModel.addCustomMeal(slot, title, desc, prepTime, ings, usesBatch, inStock, impact, customSlot, protein, carbs, fat, calories)
                    }
                )
            }

            // 8. Custom Meal Edit Dialog
            state.editingMeal?.let { mealToEdit: MealItem ->
                AddMealDialog(
                    initialMeal = mealToEdit,
                    language = state.currentLanguage,
                    onDismiss = { viewModel.setEditingMeal(null) },
                    onSave = { slot, title, desc, prepTime, ings, usesBatch, inStock, impact, customSlot, protein, carbs, fat, calories ->
                        viewModel.updateMeal(
                            mealToEdit.copy(
                                slot = slot,
                                title = title,
                                description = desc,
                                prepTimeMinutes = prepTime,
                                ingredients = ings,
                                usesBatchCookedBase = usesBatch,
                                allIngredientsInStock = inStock,
                                bioImpact = impact,
                                customSlotName = customSlot,
                                proteinGrams = protein,
                                carbsGrams = carbs,
                                fatGrams = fat,
                                caloriesKcal = calories
                            )
                        )
                    }
                )
            }

            // 9. Habit Creation Dialog
            if (state.showAddHabitDialog) {
                AddEditHabitDialog(
                    initialHabit = null,
                    language = state.currentLanguage,
                    onDismiss = { viewModel.setShowAddHabit(false) },
                    onSave = { title, desc, anchor, streakDays, tip ->
                        viewModel.addHabit(title, desc, anchor, streakDays, tip)
                    }
                )
            }

            // 10. Habit Edit Dialog
            state.editingHabit?.let { habitToEdit: HabitAnchor ->
                AddEditHabitDialog(
                    initialHabit = habitToEdit,
                    language = state.currentLanguage,
                    onDismiss = { viewModel.setEditingHabit(null) },
                    onSave = { title, desc, anchor, streakDays, tip ->
                        viewModel.updateHabit(
                            habitToEdit.copy(
                                title = title,
                                description = desc,
                                anchor = anchor,
                                streakDays = streakDays,
                                reframingTip = tip
                            )
                        )
                    }
                )
            }

            // 11. Cognitive Reframe Dialog
            if (state.showReframeDialog) {
                CognitiveReframeDialog(
                    language = state.currentLanguage,
                    reframeText = state.reframeResponse,
                    isLoading = state.isReframing,
                    onDismiss = { viewModel.setShowReframe(false) },
                    onSubmitNew = { viewModel.requestCognitiveReframe(it) }
                )
            }

            // 12. Settings & Language Dialog
            if (state.showSettingsDialog) {
                SettingsDialog(
                    currentLanguage = state.currentLanguage,
                    unlockedAchievementsCount = state.achievements.count { it.isUnlocked },
                    totalAchievementsCount = state.achievements.size,
                    wipeHistoryWithCleanSlate = state.wipeHistoryWithCleanSlate,
                    onLanguageSelected = { lang ->
                        viewModel.setLanguage(lang)
                    },
                    onOpenTutorial = {
                        viewModel.closeSettings()
                        viewModel.openTutorial(0)
                    },
                    onOpenAchievements = {
                        viewModel.closeSettings()
                        viewModel.setShowAchievementsDialog(true)
                    },
                    onToggleWipeHistory = {
                        viewModel.toggleWipeHistoryWithCleanSlate()
                    },
                    onExportFullBackup = {
                        viewModel.exportFullBackup()
                    },
                    onOpenRestoreBackupDialog = {
                        viewModel.openRestoreBackupDialog()
                    },
                    onResetToCleanSlate = {
                        viewModel.resetToCleanSlate()
                    },
                    onLoadDemoData = {
                        viewModel.loadDemoData()
                    },
                    onDismiss = { viewModel.closeSettings() }
                )
            }

            // 13. Restore Backup Dialog
            if (state.showRestoreBackupDialog) {
                RestoreBackupDialog(
                    currentLanguage = state.currentLanguage,
                    onRestore = { jsonString ->
                        viewModel.restoreFullBackupFromJson(jsonString)
                    },
                    onDismiss = { viewModel.closeRestoreBackupDialog() }
                )
            }

            // 14. Persistent History Dialog (Module 2)
            if (state.showHistoryDialog) {
                HistoryDialog(
                    state = state,
                    viewModel = viewModel,
                    onDismiss = { viewModel.closeHistory() }
                )
            }

            // 15. Achievements Dialog
            if (state.showAchievementsDialog) {
                AchievementsDialog(
                    achievements = state.achievements,
                    language = state.currentLanguage,
                    onDismiss = { viewModel.setShowAchievementsDialog(false) }
                )
            }

            // 16. Full Comprehensive Interactive Tutorial Dialog
            if (state.showTutorialDialog) {
                AetherTutorialDialog(
                    language = state.currentLanguage,
                    currentStepIndex = state.tutorialStepIndex,
                    onStepChange = { step -> viewModel.setTutorialStep(step) },
                    onDismiss = { viewModel.closeTutorial() },
                    onNavigateToTab = { tabIndex ->
                        viewModel.selectTab(tabIndex)
                    }
                )
            }

            // Achievement Unlock Floating Banner
            AchievementUnlockBanner(
                achievement = state.newlyUnlockedAchievement,
                language = state.currentLanguage,
                modifier = Modifier.align(Alignment.TopCenter)
            )

            // 4.5 Diálogo animado de logro desbloqueado
            AchievementUnlockCelebrationDialog(
                achievement = state.newlyUnlockedAchievementModal,
                language = state.currentLanguage,
                onDismiss = { viewModel.dismissAchievementModal() }
            )

            // 4.4 Toast de celebración de subida de nivel
            LevelUpCelebrationToast(
                newLevel = state.levelUpCelebrationLevel,
                language = state.currentLanguage,
                modifier = Modifier.align(Alignment.TopCenter)
            )

            // FROG Completion Celebration Overlay
            CelebrationOverlay(
                visible = state.showFrogCelebration,
                taskTitle = state.celebratingFrogTaskTitle,
                language = state.currentLanguage,
                onDismiss = { viewModel.dismissFrogCelebration() }
            )
        }
    }
}
