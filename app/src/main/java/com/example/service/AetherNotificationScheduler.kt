package com.example.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.model.TimeBlock
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.Locale

object AetherNotificationScheduler {

    private const val TAG = "AetherScheduler"

    fun scheduleTimeBlockAlerts(context: Context, blocks: List<TimeBlock>) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        blocks.forEachIndexed { index, block ->
            try {
                val triggerMillis = parseStartTimeToEpochMillis(block.startTime)
                val nowMillis = System.currentTimeMillis()

                if (triggerMillis > nowMillis) {
                    val intent = Intent(context, TimeBlockNotificationReceiver::class.java).apply {
                        putExtra(TimeBlockNotificationReceiver.EXTRA_TITLE, "⏱️ Bloque: ${block.title}")
                        putExtra(TimeBlockNotificationReceiver.EXTRA_MESSAGE, "Horario: ${block.startTime} - ${block.endTime}. Mantén el foco en este intervalo.")
                        putExtra(TimeBlockNotificationReceiver.EXTRA_NOTIFICATION_ID, 2000 + index)
                    }

                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
                        2000 + index,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerMillis,
                            pendingIntent
                        )
                    } else {
                        alarmManager.setExact(
                            AlarmManager.RTC_WAKEUP,
                            triggerMillis,
                            pendingIntent
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error scheduling alert for block ${block.title}", e)
            }
        }
    }

    private fun parseStartTimeToEpochMillis(timeStr: String): Long {
        val clean = timeStr.trim().uppercase(Locale.US)
        val timeOnly = clean.replace(Regex("[^0-9:]"), "")
        val parts = timeOnly.split(":")
        var hour = parts.getOrNull(0)?.toIntOrNull() ?: 0
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
        if (clean.contains("PM") && hour < 12) hour += 12
        if (clean.contains("AM") && hour == 12) hour = 0

        val localTime = LocalTime.of(hour.coerceIn(0, 23), minute.coerceIn(0, 59))
        val todayDateTime = LocalDate.now().atTime(localTime)
        return todayDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
}
