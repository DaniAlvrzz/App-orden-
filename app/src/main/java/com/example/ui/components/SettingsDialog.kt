package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.AppThemeMode
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.StringsProvider
import com.example.ui.theme.*

@Composable
fun SettingsDialog(
    currentLanguage: AppLanguage,
    currentThemeMode: AppThemeMode = AppThemeMode.DARK,
    unlockedAchievementsCount: Int = 0,
    totalAchievementsCount: Int = 10,
    wipeHistoryWithCleanSlate: Boolean = false,
    onLanguageSelected: (AppLanguage) -> Unit,
    onThemeModeSelected: (AppThemeMode) -> Unit = {},
    onOpenTutorial: () -> Unit,
    onOpenAchievements: () -> Unit = {},
    onToggleWipeHistory: () -> Unit = {},
    onExportFullBackup: () -> Unit = {},
    onOpenRestoreBackupDialog: () -> Unit = {},
    onResetToCleanSlate: () -> Unit,
    onLoadDemoData: () -> Unit,
    onDismiss: () -> Unit
) {
    val strings = remember(currentLanguage) { StringsProvider(currentLanguage) }
    val isSpanish = currentLanguage == AppLanguage.SPANISH

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.90f)
                .testTag("settings_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = AetherSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = AetherCyan,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = strings.settingsTitle,
                                style = MaterialTheme.typography.titleMedium,
                                color = AetherTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = strings.settingsSub,
                                style = MaterialTheme.typography.labelSmall,
                                color = AetherTextMuted
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_settings_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = strings.btnClose, tint = AetherTextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Scrollable Body
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. Achievements Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = AetherSurfaceElevated),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.EmojiEvents, contentDescription = null, tint = AetherAmber, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isSpanish) "Logros & Hitos" else "Achievements & Milestones",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = AetherAmber,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = AetherAmber.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "🏆 $unlockedAchievementsCount / $totalAchievementsCount",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = AetherAmber,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (isSpanish) "Desbloquea hitos bioenergéticos mientras mantienes tus rachas y conquistas tareas." else "Unlock bioenergetic milestones as you build streaks and conquer daily tasks.",
                                style = MaterialTheme.typography.bodySmall,
                                color = AetherTextSecondary,
                                lineHeight = 18.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    onDismiss()
                                    onOpenAchievements()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AetherAmber, contentColor = Color(0xFF332000)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("view_achievements_btn")
                            ) {
                                Icon(imageVector = Icons.Default.EmojiEvents, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isSpanish) "Ver Colección de Logros" else "View Achievements", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // 2. Language Selector Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = AetherSurfaceElevated),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = strings.languageSectionTitle,
                                style = MaterialTheme.typography.labelMedium,
                                color = AetherCyan,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            AppLanguage.entries.forEach { lang ->
                                val isSelected = currentLanguage == lang
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) AetherCyan.copy(alpha = 0.15f) else Color.Transparent)
                                        .clickable { onLanguageSelected(lang) }
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = lang.flag, fontSize = 20.sp)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = lang.displayName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (isSelected) AetherCyan else AetherTextPrimary,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { onLanguageSelected(lang) },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = AetherCyan,
                                            unselectedColor = AetherTextMuted
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // 2.5 Theme Mode Selector Card (Sistema / Claro / Oscuro)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth().testTag("theme_selector_card")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.BrightnessMedium,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isSpanish) "Tema Visual de la Aplicación" else "Application Visual Theme",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))

                            AppThemeMode.entries.forEach { mode ->
                                val isSelected = currentThemeMode == mode
                                val title = if (isSpanish) mode.titleEs else mode.titleEn
                                val desc = when (mode) {
                                    AppThemeMode.SYSTEM -> if (isSpanish) "Sigue la configuración del sistema Android" else "Follow Android system setting"
                                    AppThemeMode.LIGHT -> if (isSpanish) "Modo claro de alto contraste diurno" else "Daytime high-contrast light mode"
                                    AppThemeMode.DARK -> if (isSpanish) "Modo Cyber-Zen oscuro para descanso visual" else "Cyber-Zen dark mode for visual calm"
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent)
                                        .clickable { onThemeModeSelected(mode) }
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(text = mode.icon, fontSize = 20.sp)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = title,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                            Text(
                                                text = desc,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { onThemeModeSelected(mode) },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = MaterialTheme.colorScheme.primary,
                                            unselectedColor = MaterialTheme.colorScheme.outline
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // 3. Full Backup & Restore Card (Module 2)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = AetherSurfaceElevated),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Backup, contentDescription = null, tint = AetherCyan, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = strings.fullBackupTitle,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = AetherCyan,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = strings.fullBackupDesc,
                                style = MaterialTheme.typography.bodySmall,
                                color = AetherTextSecondary
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = onExportFullBackup,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AetherSurfaceCard,
                                        contentColor = AetherCyan
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("export_full_backup_btn")
                                ) {
                                    Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(strings.btnExportFullBackup, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        onDismiss()
                                        onOpenRestoreBackupDialog()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AetherSurfaceCard,
                                        contentColor = AetherAmber
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("restore_full_backup_btn")
                                ) {
                                    Icon(imageVector = Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(strings.btnRestoreFullBackup, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // 4. Interactive Tutorial Launcher Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = AetherSurfaceElevated),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.School, contentDescription = null, tint = AetherAmber, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = strings.tutorialSectionTitle,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = AetherAmber,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = strings.tutorialLaunchSub,
                                style = MaterialTheme.typography.bodySmall,
                                color = AetherTextSecondary,
                                lineHeight = 18.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    onDismiss()
                                    onOpenTutorial()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AetherAmber,
                                    contentColor = Color(0xFF332000)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("launch_tutorial_btn")
                            ) {
                                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = strings.tutorialLaunchBtn, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // 5. System Laws & Philosophy
                    Card(
                        colors = CardDefaults.cardColors(containerColor = AetherSurfaceElevated),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = AetherEmerald, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = strings.systemLawsTitle,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = AetherEmerald,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            listOf(strings.law1Text, strings.law2Text, strings.law3Text, strings.law4Text).forEach { law ->
                                Text(
                                    text = law,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AetherTextPrimary,
                                    lineHeight = 18.sp,
                                    modifier = Modifier.padding(vertical = 3.dp)
                                )
                            }
                        }
                    }

                    // 6. Data State Management (Clean Slate vs Demo Mode)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = AetherSurfaceElevated),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = strings.demoDataSection,
                                style = MaterialTheme.typography.labelMedium,
                                color = AetherCyan,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = strings.cleanSlateDesc,
                                style = MaterialTheme.typography.bodySmall,
                                color = AetherTextSecondary
                            )

                            // Wipe history checkbox (unmarked by default)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onToggleWipeHistory() }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = wipeHistoryWithCleanSlate,
                                    onCheckedChange = { onToggleWipeHistory() },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = AetherCoral,
                                        uncheckedColor = AetherTextMuted
                                    ),
                                    modifier = Modifier.testTag("wipe_history_checkbox")
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = strings.wipeHistoryCheckboxLabel,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = if (wipeHistoryWithCleanSlate) AetherCoral else AetherTextPrimary
                                    )
                                    Text(
                                        text = strings.wipeHistoryNote,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = AetherTextMuted
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = onResetToCleanSlate,
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = AetherCoral
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("reset_clean_slate_btn")
                                ) {
                                    Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(strings.btnCleanSlate, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = onLoadDemoData,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AetherSurfaceCard,
                                        contentColor = AetherCyan
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("load_demo_data_btn")
                                ) {
                                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(strings.btnLoadDemo, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = AetherCyan, contentColor = Color(0xFF00363D)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(strings.btnClose, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
