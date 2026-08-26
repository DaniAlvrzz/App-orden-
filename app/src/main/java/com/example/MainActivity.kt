package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.ui.AetherApp

class MainActivity : ComponentActivity() {

  private val notificationPermissionLauncher =
    registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* no-op: reminders
      simply won't fire if denied — nothing else in the app depends on this permission. */ }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    requestNotificationPermissionIfNeeded()
    setContent {
      AetherApp()
    }
  }

  /**
   * On Android 13+ (API 33+), POST_NOTIFICATIONS is a runtime permission. Without requesting it
   * explicitly, it defaults to denied and every reminder (periodic task/habit nudges, daily
   * end-of-day summary) would silently never appear — the feature would look broken with no
   * error anywhere. Declaring it in the manifest alone is not enough.
   */
  private fun requestNotificationPermissionIfNeeded() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      val alreadyGranted = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.POST_NOTIFICATIONS
      ) == PackageManager.PERMISSION_GRANTED
      if (!alreadyGranted) {
        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
      }
    }
  }
}
