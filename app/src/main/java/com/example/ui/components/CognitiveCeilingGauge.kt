package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.StringsProvider
import com.example.ui.theme.*

@Composable
fun CognitiveCeilingGauge(
    allocatedMinutes: Int,
    maxCeilingMinutes: Int = 210, // 3.5 hours hard limit
    language: AppLanguage = AppLanguage.SPANISH,
    modifier: Modifier = Modifier
) {
    val strings = remember(language) { StringsProvider(language) }
    val progress = (allocatedMinutes.toFloat() / maxCeilingMinutes.toFloat()).coerceIn(0f, 1.2f)
    val animatedProgress by animateFloatAsState(targetValue = progress.coerceAtMost(1f), label = "CeilingProgress")

    val hoursAllocated = allocatedMinutes / 60f
    val hoursMax = maxCeilingMinutes / 60f
    val isExceeded = allocatedMinutes > maxCeilingMinutes

    val barColor = when {
        isExceeded -> AetherCoral
        progress > 0.8f -> AetherAmber
        else -> AetherCyan
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("cognitive_ceiling_gauge"),
        colors = CardDefaults.cardColors(containerColor = AetherSurfaceCard),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = "Cognitive Ceiling",
                        tint = AetherCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = strings.cognitiveCeilingTitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = AetherCyan,
                        letterSpacing = 1.1.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "${String.format("%.1f", hoursAllocated)}h / ${String.format("%.1f", hoursMax)}h ${strings.ceilingMaxSuffix}",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isExceeded) AetherCoral else AetherTextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(AetherSurfaceElevated)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(barColor)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isExceeded) Icons.Default.Warning else Icons.Default.Info,
                    contentDescription = null,
                    tint = if (isExceeded) AetherCoral else AetherTextMuted,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isExceeded) strings.ceilingExceededWarning else strings.ceilingNormalInfo,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = if (isExceeded) AetherCoral else AetherTextSecondary
                )
            }
        }
    }
}
