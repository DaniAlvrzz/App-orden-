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
    private const val MAX_TRACKED_BLOCKS = 50

    fun scheduleTimeBlockAlerts(context: Context, blocks: List<TimeBlock>) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        // Cancel all previous timeblock alarms first to avoid ghost notifications
        for (i in 0 until MAX_TRACKED_BLOCKS) {
            val intent = Intent(context, TimeBlockNotificationReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                2000 + i,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        }

        blocks.forEachIndexed { index, block ->
            if (index >= MAX_TRACKED_BLOCKS) return@forEachIndexed
            if (block.isCompleted) return@forEachIndexed

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

    fun dismissNotification(context: Context, notificationId: Int) {
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? android.app.NotificationManager
            notificationManager?.cancel(notificationId)
        } catch (e: Exception) {
            Log.e(TAG, "Error dismissing notification $notificationId", e)
        }
    }

    fun dismissAllNotifications(context: Context) {
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? android.app.NotificationManager
            for (i in 0 until MAX_TRACKED_BLOCKS) {
                notificationManager?.cancel(2000 + i)
            }
            notificationManager?.cancel(3001) // Focus timer notification ID
        } catch (e: Exception) {
            Log.e(TAG, "Error dismissing all notifications", e)
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
