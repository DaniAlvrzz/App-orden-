package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.example.MainActivity
import com.example.data.local.AetherDatabase
import com.example.data.local.PreferencesManager
import com.example.ui.i18n.AppLanguage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class PeriodicReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val database = AetherDatabase.getDatabase(applicationContext)
            val prefs: PreferencesManager = com.example.data.local.DataStorePreferencesManager(applicationContext)
            val language = prefs.languageFlow.first()
            val isSpanish = language == AppLanguage.SPANISH

            val taskDao = database.taskDao()
            val habitDao = database.habitDao()

            val activeTasks = taskDao.getActiveTasks().first()
            val pendingTasks = activeTasks.filter { !it.isCompleted }

            val activeHabits = habitDao.getAllHabits().first()
            val pendingHabits = activeHabits.filter { !it.isCompleted }

            // Only notify if there are pending tasks or habits
            if (pendingTasks.isNotEmpty() || pendingHabits.isNotEmpty()) {
                sendReminderNotification(
                    context = applicationContext,
                    pendingTasksCount = pendingTasks.size,
                    pendingHabitsCount = pendingHabits.size,
                    frogTaskTitle = pendingTasks.firstOrNull { it.isFrog }?.title,
                    isSpanish = isSpanish
                )
            }

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error in PeriodicReminderWorker", e)
            Result.retry()
        }
    }

    private fun sendReminderNotification(
        context: Context,
        pendingTasksCount: Int,
        pendingHabitsCount: Int,
        frogTaskTitle: String?,
        isSpanish: Boolean
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create Channel for Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Recordatorios periódicos de tareas pendientes y hábitos cada 6 horas"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (isSpanish) {
            "⚡ Aether OS • Estado de Foco"
        } else {
            "⚡ Aether OS • Focus Status"
        }

        val contentText = buildString {
            if (isSpanish) {
                append("Tienes $pendingTasksCount tareas y $pendingHabitsCount hábitos pendientes.")
                if (!frogTaskTitle.isNullOrBlank()) {
                    append(" 🔥 Frog: $frogTaskTitle")
                }
            } else {
                append("You have $pendingTasksCount tasks and $pendingHabitsCount habits pending.")
                if (!frogTaskTitle.isNullOrBlank()) {
                    append(" 🔥 Frog: $frogTaskTitle")
                }
            }
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        const val TAG = "PeriodicReminderWorker"
        const val WORK_NAME = "aether_6h_periodic_reminder"
        const val CHANNEL_ID = "aether_periodic_reminders_channel"
        const val CHANNEL_NAME = "AetherOS Recordatorios (6h)"
        const val NOTIFICATION_ID = 5001

        fun schedulePeriodicReminders(context: Context) {
            try {
                val periodicWorkRequest = PeriodicWorkRequestBuilder<PeriodicReminderWorker>(
                    6, TimeUnit.HOURS,
                    15, TimeUnit.MINUTES // Flex interval
                )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresBatteryNotLow(false)
                        .build()
                )
                .addTag(TAG)
                .build()

                WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    periodicWorkRequest
                )
                Log.d(TAG, "Scheduled 6-hour PeriodicReminderWorker successfully.")
            } catch (e: Exception) {
                Log.w(TAG, "WorkManager periodic scheduling error: ${e.message}")
            }
        }
    }
}
