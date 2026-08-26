package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AiMessage
import com.example.data.model.AiQuickAction
import com.example.data.model.Chronotype
import com.example.ui.components.ExportShareCard
import com.example.ui.components.OfflineAiNoticeCard
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.StringsProvider
import com.example.ui.theme.*
import com.example.ui.viewmodel.AetherUiState
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AetherAiScreen(
    state: AetherUiState,
    onOrchestrate: () -> Unit,
    onRequestReframe: (String) -> Unit,
    onToggleRecovery: () -> Unit,
    onUpdateChronotype: (Chronotype) -> Unit,
    onSendChatMessage: (String) -> Unit = {},
    onSendQuickAction: (AiQuickAction) -> Unit = {},
    onToggleFavorite: (AiMessage) -> Unit = {},
    onDeleteMessage: (String) -> Unit = {},
    onClearChatHistory: () -> Unit = {},
    onSelectTab: (Int) -> Unit = {},
    getExportJson: () -> String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val strings = remember(state.currentLanguage) { StringsProvider(state.currentLanguage) }
    var chatInput by remember { mutableStateOf("") }
    var reframeInput by remember { mutableStateOf("") }
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    val rawJson = remember(state.dailyPlan, state.tasks, state.timeBlocks, state.pantryItems) {
        getExportJson()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AetherDarkBackground)
    ) {
        // Top Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Text(
                        text = strings.aiHeader,
                        style = MaterialTheme.typography.titleLarge,
                        color = AetherCyan,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.1.sp
                    )
                    Text(
                        text = strings.aiSub,
                        style = MaterialTheme.typography.labelSmall,
                        color = AetherTextMuted
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilledTonalButton(
                        onClick = onOrchestrate,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = AetherCyan.copy(alpha = 0.2f),
                            contentColor = AetherCyan
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("ai_reorchestrate_btn")
                    ) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(strings.btnReplan, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // AI Status & Bio Context Badges Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Engine Status
                Surface(
                    color = when (state.aiEngineStatus) {
                        com.example.data.model.AiStatus.LIVE, com.example.data.model.AiStatus.SUCCESS -> AetherEmerald.copy(alpha = 0.15f)
                        com.example.data.model.AiStatus.FALLBACK, com.example.data.model.AiStatus.ORCHESTRATING -> AetherAmber.copy(alpha = 0.15f)
                        com.example.data.model.AiStatus.ERROR -> Color(0xFFEF4444).copy(alpha = 0.15f)
                        else -> AetherBorderLight.copy(alpha = 0.15f)
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = when (state.aiEngineStatus) {
                            com.example.data.model.AiStatus.LIVE -> if (state.currentLanguage == AppLanguage.SPANISH) "● Gemini AI en Vivo" else "● Gemini AI Live"
                            com.example.data.model.AiStatus.SUCCESS -> if (state.currentLanguage == AppLanguage.SPANISH) "● Plan Sintetizado" else "● Plan Synthesized"
                            com.example.data.model.AiStatus.ORCHESTRATING -> if (state.currentLanguage == AppLanguage.SPANISH) "● Orquestando..." else "● Orchestrating..."
                            com.example.data.model.AiStatus.FALLBACK -> if (state.currentLanguage == AppLanguage.SPANISH) "● Motor Circadiano Respaldo" else "● Deterministic Fallback"
                            com.example.data.model.AiStatus.ERROR -> if (state.currentLanguage == AppLanguage.SPANISH) "● Error IA / En Respaldo" else "● AI Error / Fallback"
                            else -> if (state.currentLanguage == AppLanguage.SPANISH) "● Motor Listo" else "● Engine Ready"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = when (state.aiEngineStatus) {
                            com.example.data.model.AiStatus.LIVE, com.example.data.model.AiStatus.SUCCESS -> AetherEmerald
                            com.example.data.model.AiStatus.FALLBACK, com.example.data.model.AiStatus.ORCHESTRATING -> AetherAmber
                            com.example.data.model.AiStatus.ERROR -> Color(0xFFEF4444)
                            else -> AetherTextSecondary
                        },
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                // Live Readiness Badge
                Surface(
                    color = AetherSurfaceElevated,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "⚡ Readiness: ${state.biometric.readinessScore}/100",
                        style = MaterialTheme.typography.labelSmall,
                        color = AetherTextSecondary,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                // Live Deep Work Badge
                Surface(
                    color = if (state.isCeilingExceeded) Color(0xFFEF4444).copy(alpha = 0.15f) else AetherSurfaceElevated,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "🧠 Deep Work: ${state.deepWorkMinutesAllocated}/210m",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (state.isCeilingExceeded) Color(0xFFF87171) else AetherTextSecondary,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }

        // Segmented Tabs Row
        TabRow(
            selectedTabIndex = state.selectedAiTab,
            containerColor = AetherSurface,
            contentColor = AetherCyan,
            divider = { HorizontalDivider(color = AetherBorder) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = state.selectedAiTab == 0,
                onClick = { onSelectTab(0) },
                text = {
                    Text(
                        strings.aiTabChat,
                        fontWeight = if (state.selectedAiTab == 0) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 12.sp
                    )
                },
                modifier = Modifier.testTag("ai_tab_chat")
            )
            Tab(
                selected = state.selectedAiTab == 1,
                onClick = { onSelectTab(1) },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            strings.aiTabFavorites,
                            fontWeight = if (state.selectedAiTab == 1) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp
                        )
                        if (state.favoriteAiMessages.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Surface(
                                color = AetherAmber.copy(alpha = 0.25f),
                                shape = CircleShape
                            ) {
                                Text(
                                    text = state.favoriteAiMessages.size.toString(),
                                    color = AetherAmber,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                },
                modifier = Modifier.testTag("ai_tab_favorites")
            )
            Tab(
                selected = state.selectedAiTab == 2,
                onClick = { onSelectTab(2) },
                text = {
                    Text(
                        strings.aiTabInspector,
                        fontWeight = if (state.selectedAiTab == 2) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 12.sp
                    )
                },
                modifier = Modifier.testTag("ai_tab_inspector")
            )
        }

        // Tab Content
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (state.selectedAiTab) {
                0 -> AiChatTabContent(
                    state = state,
                    strings = strings,
                    chatInput = chatInput,
                    onChatInputChange = { chatInput = it },
                    onSendMessage = {
                        if (chatInput.isNotBlank()) {
                            onSendChatMessage(chatInput)
                            chatInput = ""
                        }
                    },
                    onSendQuickAction = onSendQuickAction,
                    onToggleFavorite = onToggleFavorite,
                    onDeleteMessage = onDeleteMessage,
                    onClearHistoryClick = { showClearConfirmDialog = true },
                    onCopyText = { text ->
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Aether AI", text)
                        clipboard.setPrimaryClip(clip)
                    }
                )
                1 -> AiFavoritesTabContent(
                    state = state,
                    strings = strings,
                    onToggleFavorite = onToggleFavorite,
                    onDeleteMessage = onDeleteMessage,
                    onCopyText = { text ->
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Aether Note", text)
                        clipboard.setPrimaryClip(clip)
                    }
                )
                2 -> AiSchemaInspectorTabContent(
                    state = state,
                    strings = strings,
                    rawJson = rawJson,
                    reframeInput = reframeInput,
                    onReframeInputChange = { reframeInput = it },
                    onRequestReframe = onRequestReframe,
                    onOrchestrate = onOrchestrate,
                    onCopyJson = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("AetherDailyPlan", rawJson)
                        clipboard.setPrimaryClip(clip)
                    }
                )
            }
        }
    }

    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            containerColor = AetherSurfaceElevated,
            title = {
                Text(
                    text = strings.aiClearHistory,
                    color = AetherTextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = strings.aiClearHistoryConfirm,
                    color = AetherTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onClearChatHistory()
                        showClearConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text(if (state.currentLanguage == AppLanguage.SPANISH) "Borrar Todo" else "Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text(if (state.currentLanguage == AppLanguage.SPANISH) "Cancelar" else "Cancel", color = AetherTextMuted)
                }
            }
        )
    }
}

/**
 * 5.1, 5.2, 5.3, 5.4, 5.5: Conversational AI Tab with live streaming and quick buttons.
 */
@Composable
private fun AiChatTabContent(
    state: AetherUiState,
    strings: StringsProvider,
    chatInput: String,
    onChatInputChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onSendQuickAction: (AiQuickAction) -> Unit,
    onToggleFavorite: (AiMessage) -> Unit,
    onDeleteMessage: (String) -> Unit,
    onClearHistoryClick: () -> Unit,
    onCopyText: (String) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Auto scroll to bottom when new messages or streaming chunks arrive
    LaunchedEffect(state.aiMessages.size, state.activeStreamingContent) {
        if (state.aiMessages.isNotEmpty() || state.isAiStreaming) {
            val targetIndex = (state.aiMessages.size + (if (state.isAiStreaming) 1 else 0)).coerceAtLeast(1) - 1
            listState.animateScrollToItem(targetIndex)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Chat Message List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 8.dp)
        ) {
            // Empty State
            if (state.aiMessages.isEmpty() && !state.isAiStreaming) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = AetherSurfaceCard),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                color = AetherCyan.copy(alpha = 0.15f),
                                shape = CircleShape,
                                modifier = Modifier.size(54.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Psychology,
                                    contentDescription = null,
                                    tint = AetherCyan,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = strings.aiEmptyChatTitle,
                                style = MaterialTheme.typography.titleMedium,
                                color = AetherTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = strings.aiEmptyChatDesc,
                                style = MaterialTheme.typography.bodySmall,
                                color = AetherTextMuted,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Historical Persistent Messages (5.1)
            items(state.aiMessages, key = { it.id }) { message ->
                AiChatMessageBubble(
                    message = message,
                    strings = strings,
                    onToggleFavorite = { onToggleFavorite(message) },
                    onDelete = { onDeleteMessage(message.id) },
                    onCopy = { onCopyText(message.content) }
                )
            }

            // Live Streaming Response (5.4)
            if (state.isAiStreaming) {
                item(key = "streaming_bubble") {
                    AiChatStreamingBubble(
                        isThinking = state.isAiThinking,
                        streamingContent = state.activeStreamingContent,
                        strings = strings
                    )
                }
            }
        }

        // 5.2 Quick Context Action Buttons
        Surface(
            color = AetherSurfaceElevated,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    QuickActionChip(
                        label = strings.quickActionPlanDay,
                        icon = Icons.Default.RocketLaunch,
                        tint = AetherCyan,
                        onClick = { onSendQuickAction(AiQuickAction.PLAN_DAY) },
                        tag = "quick_action_plan_day"
                    )
                    QuickActionChip(
                        label = strings.quickActionBreakDown,
                        icon = Icons.Default.AccountTree,
                        tint = AetherCyan,
                        onClick = { onSendQuickAction(AiQuickAction.BREAK_DOWN_TASK) },
                        tag = "quick_action_break_down"
                    )
                    QuickActionChip(
                        label = strings.quickActionNoMotivation,
                        icon = Icons.Default.Bolt,
                        tint = AetherAmber,
                        onClick = { onSendQuickAction(AiQuickAction.NO_MOTIVATION) },
                        tag = "quick_action_no_motivation"
                    )
                    QuickActionChip(
                        label = strings.quickActionOverwhelmed,
                        icon = Icons.Default.Spa,
                        tint = Color(0xFF38BDF8),
                        onClick = { onSendQuickAction(AiQuickAction.OVERWHELMED) },
                        tag = "quick_action_overwhelmed"
                    )
                    QuickActionChip(
                        label = strings.quickActionMicroStep,
                        icon = Icons.Default.Adjust,
                        tint = AetherEmerald,
                        onClick = { onSendQuickAction(AiQuickAction.MICRO_STEP) },
                        tag = "quick_action_micro_step"
                    )
                    QuickActionChip(
                        label = strings.quickActionEmotionalSupport,
                        icon = Icons.Default.Favorite,
                        tint = Color(0xFFF472B6),
                        onClick = { onSendQuickAction(AiQuickAction.EMOTIONAL_SUPPORT) },
                        tag = "quick_action_emotional_support"
                    )
                    QuickActionChip(
                        label = strings.quickActionGentlePlan,
                        icon = Icons.Default.WbSunny,
                        tint = AetherEmerald,
                        onClick = { onSendQuickAction(AiQuickAction.GENTLE_PLAN) },
                        tag = "quick_action_gentle_plan"
                    )
                    QuickActionChip(
                        label = strings.quickActionLowEnergy,
                        icon = Icons.Default.BatteryAlert,
                        tint = AetherAmber,
                        onClick = { onSendQuickAction(AiQuickAction.LOW_ENERGY) },
                        tag = "quick_action_low_energy"
                    )
                    QuickActionChip(
                        label = strings.quickActionWeeklyReview,
                        icon = Icons.Default.Assessment,
                        tint = AetherEmerald,
                        onClick = { onSendQuickAction(AiQuickAction.WEEKLY_REVIEW) },
                        tag = "quick_action_weekly_review"
                    )
                    QuickActionChip(
                        label = strings.quickAction30Min,
                        icon = Icons.Default.Timer,
                        tint = Color(0xFFA78BFA),
                        onClick = { onSendQuickAction(AiQuickAction.THIRTY_MIN_TASK) },
                        tag = "quick_action_30_min"
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Input Box & Send Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = chatInput,
                        onValueChange = onChatInputChange,
                        placeholder = {
                            Text(
                                text = strings.aiChatInputPlaceholder,
                                style = MaterialTheme.typography.bodySmall,
                                color = AetherTextMuted
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AetherCyan,
                            unfocusedBorderColor = AetherBorder,
                            focusedContainerColor = AetherSurfaceCard,
                            unfocusedContainerColor = AetherSurfaceCard,
                            focusedTextColor = AetherTextPrimary,
                            unfocusedTextColor = AetherTextPrimary
                        ),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = false,
                        maxLines = 3,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = {
                            if (chatInput.isNotBlank() && !state.isAiStreaming) {
                                onSendMessage()
                                keyboardController?.hide()
                            }
                        }),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("ai_chat_input")
                    )

                    IconButton(
                        onClick = {
                            if (chatInput.isNotBlank() && !state.isAiStreaming) {
                                onSendMessage()
                                keyboardController?.hide()
                            }
                        },
                        enabled = chatInput.isNotBlank() && !state.isAiStreaming,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = if (chatInput.isNotBlank() && !state.isAiStreaming) AetherCyan else AetherBorderLight,
                            contentColor = if (chatInput.isNotBlank() && !state.isAiStreaming) Color(0xFF030712) else AetherTextMuted
                        ),
                        modifier = Modifier
                            .size(46.dp)
                            .testTag("ai_chat_send_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = strings.aiChatSend,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    if (state.aiMessages.isNotEmpty()) {
                        IconButton(
                            onClick = onClearHistoryClick,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = strings.aiClearHistory,
                                tint = AetherTextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    onClick: () -> Unit,
    tag: String
) {
    Surface(
        onClick = onClick,
        color = tint.copy(alpha = 0.15f),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, tint.copy(alpha = 0.4f)),
        modifier = Modifier.testTag(tag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
            Text(
                text = label,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun AiChatMessageBubble(
    message: AiMessage,
    strings: StringsProvider,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit,
    onCopy: () -> Unit
) {
    val isUser = message.isUser
    val timeFormatted = remember(message.timestamp) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp))
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Surface(
                color = AetherCyan.copy(alpha = 0.2f),
                shape = CircleShape,
                modifier = Modifier
                    .size(32.dp)
                    .align(Alignment.Top)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = AetherCyan,
                    modifier = Modifier.padding(6.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) Color(0xFF1E293B) else AetherSurfaceCard
            ),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            border = if (!isUser) androidx.compose.foundation.BorderStroke(1.dp, AetherBorder) else null,
            modifier = Modifier.widthIn(max = 310.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                    color = AetherTextPrimary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = timeFormatted,
                        style = MaterialTheme.typography.labelSmall,
                        color = AetherTextMuted,
                        fontSize = 10.sp
                    )

                    if (!isUser) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // 5.5 Favorite Toggle Button
                            IconButton(
                                onClick = onToggleFavorite,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = if (message.isFavorite) Icons.Default.Star else Icons.Outlined.BookmarkBorder,
                                    contentDescription = strings.aiSaveFavorite,
                                    tint = if (message.isFavorite) AetherAmber else AetherTextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            // Copy Button
                            IconButton(
                                onClick = onCopy,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = strings.aiCopyResponse,
                                    tint = AetherTextMuted,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AiChatStreamingBubble(
    isThinking: Boolean,
    streamingContent: String,
    strings: StringsProvider
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Surface(
            color = AetherCyan.copy(alpha = 0.2f),
            shape = CircleShape,
            modifier = Modifier
                .size(32.dp)
                .align(Alignment.Top)
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = AetherCyan,
                modifier = Modifier.padding(6.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = AetherSurfaceCard),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, AetherCyan.copy(alpha = 0.3f)),
            modifier = Modifier.widthIn(max = 310.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (isThinking && streamingContent.isBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            color = AetherCyan,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = strings.aiThinking,
                            style = MaterialTheme.typography.bodySmall,
                            color = AetherCyan,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    Text(
                        text = streamingContent,
                        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                        color = AetherTextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = AetherCyan,
                            shape = CircleShape,
                            modifier = Modifier.size(6.dp)
                        ) {}
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = strings.aiStreaming,
                            style = MaterialTheme.typography.labelSmall,
                            color = AetherCyan,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * 5.5 Favorite Saved Notes Tab Content
 */
@Composable
private fun AiFavoritesTabContent(
    state: AetherUiState,
    strings: StringsProvider,
    onToggleFavorite: (AiMessage) -> Unit,
    onDeleteMessage: (String) -> Unit,
    onCopyText: (String) -> Unit
) {
    if (state.favoriteAiMessages.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = AetherSurfaceCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.StarBorder,
                        contentDescription = null,
                        tint = AetherAmber,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = strings.aiNoFavoritesTitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = AetherTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = strings.aiNoFavoritesDesc,
                        style = MaterialTheme.typography.bodySmall,
                        color = AetherTextMuted,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
        ) {
            items(state.favoriteAiMessages, key = { it.id }) { note ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = AetherSurfaceCard),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AetherAmber.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = AetherAmber, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = SimpleDateFormat("dd MMM yyyy • HH:mm", Locale.getDefault()).format(Date(note.timestamp)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AetherAmber,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { onCopyText(note.content) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = strings.aiCopyResponse, tint = AetherTextSecondary, modifier = Modifier.size(16.dp))
                                }
                                IconButton(
                                    onClick = { onToggleFavorite(note) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.BookmarkRemove, contentDescription = strings.aiRemoveFavorite, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                                }
                            }
                        }

                        HorizontalDivider(color = AetherBorder, modifier = Modifier.padding(vertical = 8.dp))

                        Text(
                            text = note.content,
                            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                            color = AetherTextPrimary
                        )
                    }
                }
            }
        }
    }
}

/**
 * Tab 2: Raw Schema Inspector & Operational Laws
 */
@Composable
private fun AiSchemaInspectorTabContent(
    state: AetherUiState,
    strings: StringsProvider,
    rawJson: String,
    reframeInput: String,
    onReframeInputChange: (String) -> Unit,
    onRequestReframe: (String) -> Unit,
    onOrchestrate: () -> Unit,
    onCopyJson: () -> Unit
) {
    var copiedFeedback by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 48.dp)
    ) {
        // Offline Notice if applicable
        item {
            OfflineAiNoticeCard(
                aiStatus = state.aiEngineStatus,
                language = state.currentLanguage,
                onRetryClick = onOrchestrate
            )
        }

        // Export Share Card
        item {
            ExportShareCard(
                jsonContent = rawJson,
                language = state.currentLanguage,
                onCopyJson = onCopyJson
            )
        }

        // Operational Laws Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = AetherSurfaceElevated),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = strings.operatingLawsTitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = AetherCyan,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(strings.law1, style = MaterialTheme.typography.bodySmall, color = AetherTextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(strings.law2, style = MaterialTheme.typography.bodySmall, color = AetherTextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(strings.law3, style = MaterialTheme.typography.bodySmall, color = AetherTextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(strings.law4, style = MaterialTheme.typography.bodySmall, color = AetherTextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(strings.law5, style = MaterialTheme.typography.bodySmall, color = AetherTextPrimary)
                }
            }
        }

        // Cognitive Reframer Interactive Tool
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = AetherSurfaceCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Psychology, contentDescription = null, tint = AetherEmerald)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = strings.cognitiveReframeTitle,
                            style = MaterialTheme.typography.titleMedium,
                            color = AetherTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = reframeInput,
                        onValueChange = onReframeInputChange,
                        label = { Text(strings.reframeInputLabel) },
                        placeholder = { Text(strings.reframePlaceholder) },
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AetherEmerald,
                            unfocusedBorderColor = AetherBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reframe_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            if (reframeInput.isNotBlank()) {
                                onRequestReframe(reframeInput)
                            }
                        },
                        enabled = reframeInput.isNotBlank() && !state.isReframing,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AetherEmerald,
                            contentColor = Color(0xFF003919)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (state.isReframing) {
                            CircularProgressIndicator(color = Color(0xFF003919), modifier = Modifier.size(16.dp))
                        } else {
                            Icon(imageVector = Icons.Default.Spa, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(strings.btnGenerateReframe, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (!state.reframeResponse.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = AetherEmerald.copy(alpha = 0.12f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = state.reframeResponse,
                                style = MaterialTheme.typography.bodyMedium,
                                color = AetherTextPrimary,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }
        }

        // RAW JSON MASTER SCHEMA INSPECTOR
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF030712)),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(AetherBorderLight)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("json_inspector_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Code, contentDescription = null, tint = AetherCyan)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "AetherDailyPlan.json",
                                style = MaterialTheme.typography.labelMedium,
                                color = AetherCyan,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        IconButton(
                            onClick = {
                                onCopyJson()
                                copiedFeedback = true
                            },
                            modifier = Modifier.testTag("copy_json_btn")
                        ) {
                            Icon(
                                imageVector = if (copiedFeedback) Icons.Default.Check else Icons.Default.ContentCopy,
                                contentDescription = strings.btnCopyJson,
                                tint = if (copiedFeedback) AetherEmerald else AetherTextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 280.dp)
                            .background(Color(0xFF0B0F19), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = rawJson,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            ),
                            color = AetherTextSecondary,
                            modifier = Modifier
                                .verticalScroll(rememberScrollState())
                                .horizontalScroll(rememberScrollState())
                        )
                    }
                }
            }
        }
    }
}
