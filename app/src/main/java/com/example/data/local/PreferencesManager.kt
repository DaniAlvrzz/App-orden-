package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.ui.i18n.AppLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.aetherDataStore: DataStore<Preferences> by preferencesDataStore(name = "aether_prefs")

interface PreferencesManager {
    val languageFlow: Flow<AppLanguage>
    suspend fun getLanguage(): AppLanguage
    suspend fun saveLanguage(language: AppLanguage)
    suspend fun getLastActiveDate(): String?
    suspend fun saveLastActiveDate(dateIso: String)
}

class DataStorePreferencesManager(private val context: Context) : PreferencesManager {

    companion object {
        private val LANGUAGE_KEY = stringPreferencesKey("app_language")
        private val LAST_ACTIVE_DATE_KEY = stringPreferencesKey("last_active_date")
    }

    override val languageFlow: Flow<AppLanguage> = context.aetherDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val langStr = preferences[LANGUAGE_KEY] ?: AppLanguage.SPANISH.name
            try {
                AppLanguage.valueOf(langStr)
            } catch (e: Exception) {
                AppLanguage.SPANISH
            }
        }

    override suspend fun getLanguage(): AppLanguage {
        return try {
            val prefs = context.aetherDataStore.data.first()
            val langStr = prefs[LANGUAGE_KEY] ?: AppLanguage.SPANISH.name
            AppLanguage.valueOf(langStr)
        } catch (e: Exception) {
            AppLanguage.SPANISH
        }
    }

    override suspend fun saveLanguage(language: AppLanguage) {
        context.aetherDataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = language.name
        }
    }

    override suspend fun getLastActiveDate(): String? {
        return try {
            val prefs = context.aetherDataStore.data.first()
            prefs[LAST_ACTIVE_DATE_KEY]
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun saveLastActiveDate(dateIso: String) {
        context.aetherDataStore.edit { prefs ->
            prefs[LAST_ACTIVE_DATE_KEY] = dateIso
        }
    }
}
