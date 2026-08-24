package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Chronotype
import com.example.data.model.SystemMode
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.StringsProvider
import com.example.ui.theme.*
import com.example.ui.viewmodel.AetherUiState

@Composable
fun AetherAiScreen(
    state: AetherUiState,
    onOrchestrate: () -> Unit,
    onRequestReframe: (String) -> Unit,
    onToggleRecovery: () -> Unit,
    onUpdateChronotype: (Chronotype) -> Unit,
    getExportJson: () -> String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val strings = remember(state.currentLanguage) { StringsProvider(state.currentLanguage) }
    var reframeInput by remember { mutableStateOf("") }
    var copiedFeedback by remember { mutableStateOf(false) }

    val rawJson = remember(state.dailyPlan, state.tasks, state.timeBlocks, state.pantryItems) {
        getExportJson()
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
                Column {
                    Text(
                        text = strings.aiHeader,
                        style = MaterialTheme.typography.titleLarge,
                        color = AetherCyan,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = strings.aiSub,
                        style = MaterialTheme.typography.labelSmall,
                        color = AetherTextMuted
                    )
                }

                FilledTonalButton(
                    onClick = onOrchestrate,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = AetherCyan.copy(alpha = 0.2f),
                        contentColor = AetherCyan
                    ),
                    modifier = Modifier.testTag("ai_reorchestrate_btn")
                ) {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(strings.btnReplan)
                }
            }
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
                        onValueChange = { reframeInput = it },
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

        // RAW JSON MASTER SCHEMA INSPECTOR (Complies strictly with user schema format!)
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
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("AetherDailyPlan", rawJson)
                                clipboard.setPrimaryClip(clip)
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
