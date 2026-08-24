package com.example.data.di

import android.content.Context
import com.example.data.local.AetherDatabase
import com.example.data.remote.AetherGeminiEngine
import com.example.data.repository.AetherRepository

/**
 * Dependency Injection Container for providing singletons and decoupled dependencies
 * across the application.
 */
interface AppContainer {
    val database: AetherDatabase
    val geminiEngine: AetherGeminiEngine
    val repository: AetherRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    override val database: AetherDatabase by lazy {
        AetherDatabase.getDatabase(context)
    }

    override val geminiEngine: AetherGeminiEngine by lazy {
        AetherGeminiEngine()
    }

    override val repository: AetherRepository by lazy {
        AetherRepository(database, geminiEngine)
    }
}
