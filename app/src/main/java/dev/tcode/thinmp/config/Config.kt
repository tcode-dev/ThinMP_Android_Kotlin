package dev.tcode.thinmp.config

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

private const val PREFERENCES_NAME = "thinmp_preferences"
private const val PREFERENCES_REPEAT_KEY = "repeat"

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(PREFERENCES_NAME)

class Config(private val context: Context) {
    fun getRepeat(): Int = runBlocking {
        val preferencesKey = intPreferencesKey(PREFERENCES_REPEAT_KEY)
        val preferences = context.dataStore.data.first()

        preferences[preferencesKey] ?: 0
    }

    suspend fun saveRepeat(value: Int) {
        val preferencesKey = intPreferencesKey(PREFERENCES_REPEAT_KEY)
        context.dataStore.edit { preferences ->
            preferences[preferencesKey] = value
        }
    }
}