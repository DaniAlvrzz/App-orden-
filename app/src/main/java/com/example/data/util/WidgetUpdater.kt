package com.example.data.util

import android.content.Context
import com.example.widget.FrogTaskWidgetProvider

/**
 * Decoupled interface for notifying system widgets about data state updates.
 * Eliminates direct Context coupling from clean domain repositories.
 */
interface WidgetUpdater {
    fun updateWidgets()
}

class AndroidWidgetUpdater(private val context: Context) : WidgetUpdater {
    override fun updateWidgets() {
        try {
            FrogTaskWidgetProvider.updateAllWidgets(context)
        } catch (e: Exception) {
            // Gracefully ignore in non-widget test contexts
        }
    }
}

object NoOpWidgetUpdater : WidgetUpdater {
    override fun updateWidgets() {}
}
