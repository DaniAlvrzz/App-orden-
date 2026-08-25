package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.StringsProvider
import com.example.ui.i18n.TutorialStep
import com.example.ui.theme.*

@Composable
fun AetherTutorialDialog(
    language: AppLanguage,
    currentStepIndex: Int,
    onStepChange: (Int) -> Unit,
    onDismiss: () -> Unit,
    onNavigateToTab: ((Int) -> Unit)? = null
) {
    val strings = remember(language) { StringsProvider(language) }
    val steps = remember(language) { strings.getTutorialSteps() }
    val safeIndex = currentStepIndex.coerceIn(0, steps.size - 1)
    val step = steps[safeIndex]

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f)
                .testTag("tutorial_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = AetherSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header with Step Counter & Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = AetherCyan.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (language == AppLanguage.SPANISH) 
                                "CAPÍTULO ${safeIndex + 1} DE ${steps.size}" 
                            else 
                                "CHAPTER ${safeIndex + 1} OF ${steps.size}",
                            style = MaterialTheme.typography.labelMedium,
                            color = AetherCyan,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_tutorial_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = strings.btnClose,
                            tint = AetherTextSecondary
                        )
                    }
                }

                // Progress Bar
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { (safeIndex + 1).toFloat() / steps.size.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = AetherCyan,
                    trackColor = AetherBorder
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Step Content Scrollable Area
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Title & Icon Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(AetherCyan.copy(alpha = 0.25f), AetherEmerald.copy(alpha = 0.25f))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            val iconVector = getIconForName(step.iconName)
                            Icon(
                                imageVector = iconVector,
                                contentDescription = null,
                                tint = AetherCyan,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = step.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = AetherTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = step.subtitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = AetherCyan
                            )
                        }
                    }

                    // Summary Box
                    Card(
                        colors = CardDefaults.cardColors(containerColor = AetherSurfaceElevated),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = step.summary,
                            style = MaterialTheme.typography.bodyMedium,
                            color = AetherTextPrimary,
                            lineHeight = 22.sp,
                            modifier = Modifier.padding(14.dp)
                        )
                    }

                    // Key Points List
                    Text(
                        text = if (language == AppLanguage.SPANISH) "Aspectos Clave & Mecánicas:" else "Key Features & Mechanics:",
                        style = MaterialTheme.typography.labelMedium,
                        color = AetherTextSecondary,
                        fontWeight = FontWeight.Bold
                    )

                    step.bulletPoints.forEach { point ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 6.dp)
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(AetherCyan)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = point,
                                style = MaterialTheme.typography.bodySmall,
                                color = AetherTextPrimary,
                                lineHeight = 19.sp
                            )
                        }
                    }

                    // Real Scenario Card (if present)
                    step.exampleScenario?.let { example ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = AetherAmber.copy(alpha = 0.12f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lightbulb,
                                    contentDescription = null,
                                    tint = AetherAmber,
                                    modifier = Modifier.size(20.dp).padding(top = 2.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = example,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AetherTextPrimary,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }

                    // Biological Principle Banner
                    Card(
                        colors = CardDefaults.cardColors(containerColor = AetherEmerald.copy(alpha = 0.12f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                tint = AetherEmerald,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = step.bioPrinciple,
                                style = MaterialTheme.typography.labelSmall,
                                color = AetherEmerald,
                                fontWeight = FontWeight.SemiBold,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    // Pro Action Tip (if present)
                    step.actionTip?.let { tip ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = AetherCyan.copy(alpha = 0.08f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = AetherCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = tip,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AetherTextPrimary,
                                    lineHeight = 17.sp
                                )
                            }
                        }
                    }

                    // Direct Tab Jump Button
                    step.targetTab?.let { target ->
                        val tabName = when (target) {
                            0 -> strings.tabNexus
                            1 -> strings.tabBacklog
                            2 -> strings.tabNutrition
                            3 -> strings.tabHabits
                            4 -> strings.tabAi
                            else -> ""
                        }
                        OutlinedButton(
                            onClick = {
                                onNavigateToTab?.invoke(target)
                                onDismiss()
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AetherCyan),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (language == AppLanguage.SPANISH) 
                                    "Ir a la pestaña '$tabName' ahora" 
                                else 
                                    "Jump to '$tabName' tab now",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Step Selector Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    steps.forEachIndexed { idx, _ ->
                        val isSelected = idx == safeIndex
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 3.dp)
                                .size(if (isSelected) 24.dp else 8.dp, 8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isSelected) AetherCyan else AetherBorder)
                                .clickable { onStepChange(idx) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Navigation Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Previous Button
                    if (safeIndex > 0) {
                        OutlinedButton(
                            onClick = { onStepChange(safeIndex - 1) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AetherTextPrimary),
                            modifier = Modifier.testTag("tutorial_prev_btn")
                        ) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(strings.btnPrev)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(10.dp))
                    }

                    // Next / Finish Button
                    if (safeIndex < steps.size - 1) {
                        Button(
                            onClick = { onStepChange(safeIndex + 1) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AetherCyan,
                                contentColor = Color(0xFF00363D)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("tutorial_next_btn")
                        ) {
                            Text(strings.btnNext, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    } else {
                        Button(
                            onClick = {
                                step.targetTab?.let { onNavigateToTab?.invoke(it) }
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AetherEmerald,
                                contentColor = Color(0xFF003822)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("tutorial_finish_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(strings.btnFinishTutorial, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

private fun getIconForName(name: String): ImageVector {
    return when (name) {
        "AutoAwesome" -> Icons.Default.AutoAwesome
        "Dashboard" -> Icons.Default.Dashboard
        "Speed" -> Icons.Default.Speed
        "Bolt" -> Icons.Default.Bolt
        "Restaurant" -> Icons.Default.Restaurant
        "WbSunny" -> Icons.Default.WbSunny
        "Psychology" -> Icons.Default.Psychology
        "EmojiEvents" -> Icons.Default.EmojiEvents
        else -> Icons.Default.Info
    }
}
