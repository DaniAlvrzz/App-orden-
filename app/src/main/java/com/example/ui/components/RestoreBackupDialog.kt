package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.StringsProvider
import com.example.ui.theme.AetherCyan
import com.example.ui.theme.AetherSurface
import com.example.ui.theme.AetherSurfaceElevated

@Composable
fun RestoreBackupDialog(
    currentLanguage: AppLanguage,
    onRestore: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = remember(currentLanguage) { StringsProvider(currentLanguage) }
    var jsonText by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.80f)
                .testTag("restore_backup_dialog"),
            shape = RoundedCornerShape(20.dp),
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
                            imageVector = Icons.Default.Restore,
                            contentDescription = null,
                            tint = AetherCyan,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = strings.restoreDialogTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = strings.btnClose)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = strings.restoreDialogDesc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = jsonText,
                    onValueChange = { jsonText = it },
                    placeholder = { Text(strings.restorePastePlaceholder) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("restore_json_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = AetherSurfaceElevated,
                        unfocusedContainerColor = AetherSurfaceElevated
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(strings.btnCancel)
                    }

                    Button(
                        onClick = {
                            if (jsonText.isNotBlank()) {
                                onRestore(jsonText.trim())
                            }
                        },
                        enabled = jsonText.isNotBlank(),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("confirm_restore_backup_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = AetherCyan),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(strings.btnConfirmRestore, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
