package com.example.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.*
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.StringsProvider
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.AetherViewModel

data class NavItem(
    val titleKey: String,
    val icon: ImageVector,
    val tag: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AetherApp(
    viewModel: AetherViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val strings = remember(state.currentLanguage) { StringsProvider(state.currentLanguage) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.statusMessage) {
        state.statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
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
                        onToggleTimeBlock = { viewModel.toggleTimeBlock(it) },
                        onAddTimeBlockClick = { viewModel.setShowAddTimeBlock(true) },
                        onDeleteTimeBlock = { viewModel.deleteTimeBlock(it.id) },
                        onOpenReframe = { viewModel.setShowReframe(true) },
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
                        onDeleteTask = { viewModel.deleteTask(it) },
                        onSetEnergyFilter = { viewModel.setEnergyFilter(it) },
                        onSetSearchQuery = { viewModel.setSearchQuery(it) },
                        onStartFocusTimer = { viewModel.startFocusTimer(it) },
                        onPauseFocusTimer = { viewModel.stopFocusTimer() },
                        onResetFocusTimer = { viewModel.resetFocusTimer() },
                        onOpenQuickAdd = { viewModel.setShowQuickAdd(true) }
                    )
                    2 -> NutritionScreen(
                        state = state,
                        onToggleMeal = { viewModel.toggleMeal(it) },
                        onDeleteMeal = { viewModel.deleteMeal(it) },
                        onOpenAddMeal = { viewModel.setShowAddMeal(true) },
                        onTogglePantryStock = { id, inStock -> viewModel.togglePantryStock(id, inStock) },
                        onDeletePantryItem = { viewModel.deletePantryItem(it) },
                        onOpenAddPantry = { viewModel.setShowPantryAdd(true) }
                    )
                    3 -> HabitsScreen(
                        state = state,
                        onToggleHabit = { viewModel.toggleHabit(it) },
                        onApplyGraceDay = { viewModel.applyGraceDay(it) },
                        onOpenReframe = { viewModel.setShowReframe(true) }
                    )
                    4 -> AetherAiScreen(
                        state = state,
                        onOrchestrate = { viewModel.triggerOrchestration() },
                        onRequestReframe = { viewModel.requestCognitiveReframe(it) },
                        onToggleRecovery = { viewModel.toggleRecoveryMode() },
                        onUpdateChronotype = { viewModel.updateChronotype(it) },
                        getExportJson = { viewModel.getExportJson() }
                    )
                }
            }

            // Quick Add Dialog
            if (state.showQuickAddDialog) {
                QuickAddTaskDialog(
                    language = state.currentLanguage,
                    onDismiss = { viewModel.setShowQuickAdd(false) },
                    onSave = { title, desc, energy, priority, minutes, category, makeFrog ->
                        viewModel.quickAddTask(title, desc, energy, priority, minutes, category, makeFrog)
                    }
                )
            }

            // Add Pantry Dialog
            if (state.showPantryAddDialog) {
                AddPantryDialog(
                    language = state.currentLanguage,
                    onDismiss = { viewModel.setShowPantryAdd(false) },
                    onSave = { name, cat, inStock, isBase, qty ->
                        viewModel.addPantryItem(name, cat, inStock, isBase, qty)
                    }
                )
            }

            // Add TimeBlock Dialog
            if (state.showAddTimeBlockDialog) {
                AddTimeBlockDialog(
                    language = state.currentLanguage,
                    onDismiss = { viewModel.setShowAddTimeBlock(false) },
                    onSave = { start, end, type, title, notes ->
                        viewModel.addTimeBlock(start, end, type, title, notes)
                    }
                )
            }

            // Add Custom Meal Dialog
            if (state.showAddMealDialog) {
                AddMealDialog(
                    language = state.currentLanguage,
                    onDismiss = { viewModel.setShowAddMeal(false) },
                    onSave = { slot, title, desc, prepTime, ings, usesBatch, inStock, impact ->
                        viewModel.addCustomMeal(slot, title, desc, prepTime, ings, usesBatch, inStock, impact)
                    }
                )
            }

            // Cognitive Reframe Dialog
            if (state.showReframeDialog) {
                CognitiveReframeDialog(
                    language = state.currentLanguage,
                    reframeText = state.reframeResponse,
                    isLoading = state.isReframing,
                    onDismiss = { viewModel.setShowReframe(false) },
                    onSubmitNew = { viewModel.requestCognitiveReframe(it) }
                )
            }

            // Settings & Language Dialog
            if (state.showSettingsDialog) {
                SettingsDialog(
                    currentLanguage = state.currentLanguage,
                    onLanguageSelected = { lang ->
                        viewModel.setLanguage(lang)
                    },
                    onOpenTutorial = {
                        viewModel.closeSettings()
                        viewModel.openTutorial(0)
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

            // Full Comprehensive Interactive Tutorial Dialog
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
        }
    }
}
