package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.StringsProvider
import com.example.ui.theme.*
import java.io.File
import java.io.FileOutputStream

@Composable
fun ExportShareCard(
    jsonContent: String,
    language: AppLanguage,
    onCopyJson: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val strings = remember(language) { StringsProvider(language) }
    val isSpanish = language == AppLanguage.SPANISH

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("export_share_card"),
        colors = CardDefaults.cardColors(containerColor = AetherSurfaceElevated),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(AetherCyan.copy(alpha = 0.3f))
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    tint = AetherCyan,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isSpanish) "COMPARTIR Y EXPORTAR PLAN DIARIO" else "SHARE & EXPORT DAILY PLAN",
                    style = MaterialTheme.typography.labelSmall,
                    color = AetherCyan,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.1.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isSpanish)
                    "Exporta tu esquema circadiano AetherDailyPlan.json completo a otras aplicaciones, guárdalo como archivo local o envíalo por mensajería."
                else
                    "Export your full AetherDailyPlan.json circadian schema to external apps, save as local file, or share via messaging.",
                style = MaterialTheme.typography.bodySmall,
                color = AetherTextSecondary
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Button 1: Share via Android Intent
                Button(
                    onClick = {
                        sharePlanIntent(context, jsonContent, isSpanish)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AetherCyan,
                        contentColor = Color(0xFF00363D)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("share_plan_intent_btn")
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isSpanish) "Compartir" else "Share Plan",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                // Button 2: Save to Local Storage File
                OutlinedButton(
                    onClick = {
                        savePlanToFile(context, jsonContent, isSpanish)
                    },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = AetherEmerald
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("save_plan_file_btn")
                ) {
                    Icon(imageVector = Icons.Default.SaveAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isSpanish) "Guardar Archivo" else "Save File",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

private fun sharePlanIntent(context: Context, json: String, isSpanish: Boolean) {
    try {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, json)
            putExtra(Intent.EXTRA_TITLE, "AetherDailyPlan.json")
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, if (isSpanish) "Compartir Plan Circadiano" else "Share Circadian Plan")
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    } catch (e: Exception) {
        Toast.makeText(context, if (isSpanish) "Error al compartir" else "Error sharing", Toast.LENGTH_SHORT).show()
    }
}

private fun savePlanToFile(context: Context, json: String, isSpanish: Boolean) {
    try {
        val fileName = "AetherDailyPlan_${System.currentTimeMillis()}.json"
        val file = File(context.getExternalFilesDir(null) ?: context.filesDir, fileName)
        FileOutputStream(file).use { out ->
            out.write(json.toByteArray())
        }
        Toast.makeText(
            context,
            if (isSpanish) "💾 Guardado con éxito: ${file.name}" else "💾 Saved successfully: ${file.name}",
            Toast.LENGTH_LONG
        ).show()
    } catch (e: Exception) {
        Toast.makeText(context, if (isSpanish) "Error al guardar archivo" else "Error saving file", Toast.LENGTH_SHORT).show()
    }
}
