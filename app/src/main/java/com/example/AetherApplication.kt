package com.example

import android.app.Application
import com.example.data.di.AppContainer
import com.example.data.di.DefaultAppContainer
import com.example.service.DailyEndOfDayWorker
import com.example.service.PeriodicReminderWorker

class AetherApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
        DailyEndOfDayWorker.scheduleDailyEndOfDayWork(this)
        PeriodicReminderWorker.schedulePeriodicReminders(this)
    }
}
