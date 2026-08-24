package com.example.ui.components

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AiStatus
import com.example.ui.i18n.AppLanguage
import com.example.ui.theme.*

@Composable
fun OfflineAiNoticeCard(
    aiStatus: AiStatus,
    language: AppLanguage,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (aiStatus == AiStatus.LIVE) return // Only show when offline/fallback/error

    val isSpanish = language == AppLanguage.SPANISH

    val (badgeText, titleText, descText, containerBg, tintColor, icon) = when (aiStatus) {
        AiStatus.FALLBACK -> Tuple6(
            if (isSpanish) "MODO OFFLINE / DETERMINISTA" else "OFFLINE / DETERMINISTIC MODE",
            if (isSpanish) "Operando con Algoritmo Circadiano Local" else "Operating on Local Circadian Algorithm",
            if (isSpanish) "Sin conexión o clave API ausente. Aether OS calcula tu día con 100% de precisión bioenergética determinista local."
            else "No internet connection or missing API key. Aether OS calculates your schedule with 100% deterministic bio-rules.",
            AetherAmber.copy(alpha = 0.12f),
            AetherAmber,
            Icons.Default.CloudOff
        )
        AiStatus.ERROR -> Tuple6(
            if (isSpanish) "AVISO DE CONEXIÓN IA" else "AI CONNECTION NOTICE",
            if (isSpanish) "Fallo de Comunicación con Gemini" else "Gemini Communication Interruption",
            if (isSpanish) "Se ha activado automáticamente el motor determinista de respaldo sin pérdida de tus datos ni interrupción de tu flujo."
            else "Fallback engine activated automatically without data loss or workflow disruption.",
            Color(0xFFEF4444).copy(alpha = 0.12f),
            Color(0xFFEF4444),
            Icons.Default.WifiOff
        )
        AiStatus.IDLE -> Tuple6(
            if (isSpanish) "MOTOR DETERMINISTA LISTO" else "DETERMINISTIC ENGINE READY",
            if (isSpanish) "Línea Base Lista para Orquestar" else "Baseline Ready to Orchestrate",
            if (isSpanish) "Puedes orquestar tu plan con IA en vivo o mediante el motor determinista local."
            else "You can orchestrate your schedule via live AI or local deterministic engine.",
            AetherSurfaceElevated,
            AetherCyan,
            Icons.Default.Memory
        )
        else -> Tuple6("", "", "", Color.Transparent, Color.Transparent, Icons.Default.Info)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("offline_ai_notice_card"),
        colors = CardDefaults.cardColors(containerColor = containerBg),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(tintColor.copy(alpha = 0.3f))
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(tintColor.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tintColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = badgeText,
                    style = MaterialTheme.typography.labelSmall,
                    color = tintColor,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = titleText,
                    style = MaterialTheme.typography.titleSmall,
                    color = AetherTextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = descText,
                    style = MaterialTheme.typography.bodySmall,
                    color = AetherTextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onRetryClick,
                modifier = Modifier.testTag("retry_ai_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = if (isSpanish) "Reintentar conexión" else "Retry AI",
                    tint = tintColor
                )
            }
        }
    }
}

private data class Tuple6<A, B, C, D, E, F>(
    val a: A, val b: B, val c: C, val d: D, val e: E, val f: F
)
