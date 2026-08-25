package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.example.MainActivity
import java.util.concurrent.TimeUnit

class FocusTimerWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val taskTitle = inputData.getString(KEY_TASK_TITLE) ?: "Enfoque Profundo"
        val isFrog = inputData.getBoolean(KEY_IS_FROG, false)
        val isSpanish = inputData.getBoolean(KEY_IS_SPANISH, true)

        val title = if (isFrog) {
            if (isSpanish) "🔥 ¡Bloque FROG Completado!" else "🔥 FROG Focus Block Complete!"
        } else {
            if (isSpanish) "🎯 ¡Bloque de Enfoque Completado!" else "🎯 Focus Block Complete!"
        }

        val message = if (isSpanish) {
            "Has finalizado tu sesión en \"$taskTitle\". Tómate 5 minutos de recuperación cognitiva."
        } else {
            "You completed your session on \"$taskTitle\". Take a 5-minute cognitive rest."
        }

        showNotification(title, message)
        return Result.success()
    }

    private fun showNotification(title: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones de finalización del temporizador de enfoque"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        const val WORK_TAG = "aether_focus_timer_work"
        const val CHANNEL_ID = "aether_focus_channel"
        const val CHANNEL_NAME = "AetherOS Focus Timer"
        const val NOTIFICATION_ID = 3001

        const val KEY_TASK_TITLE = "key_task_title"
        const val KEY_IS_FROG = "key_is_frog"
        const val KEY_IS_SPANISH = "key_is_spanish"

        fun scheduleFocusTimer(
            context: Context,
            durationSeconds: Long,
            taskTitle: String,
            isFrog: Boolean,
            isSpanish: Boolean
        ) {
            val inputData = workDataOf(
                KEY_TASK_TITLE to taskTitle,
                KEY_IS_FROG to isFrog,
                KEY_IS_SPANISH to isSpanish
            )

            val workRequest = OneTimeWorkRequestBuilder<FocusTimerWorker>()
                .setInitialDelay(durationSeconds, TimeUnit.SECONDS)
                .setInputData(inputData)
                .addTag(WORK_TAG)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_TAG,
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
        }

        fun cancelFocusTimer(context: Context) {
            try {
                WorkManager.getInstance(context).cancelUniqueWork(WORK_TAG)
            } catch (e: Exception) {
                // Ignore if WorkManager not initialized
            }
        }
    }
}
