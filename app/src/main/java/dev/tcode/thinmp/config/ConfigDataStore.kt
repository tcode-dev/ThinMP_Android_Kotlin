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

enum class RepeatState {
    OFF, ONE, ALL
}

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(PREFERENCES_NAME)

class ConfigDataStore(private val context: Context) {
    fun getRepeat(): Int {
        val preferences = runBlocking { context.dataStore.data.first() }
        val preferencesKey = intPreferencesKey(PREFERENCES_REPEAT_KEY)

        return preferences[preferencesKey] ?: 0
    }

    fun saveRepeat(value: Int) {
        val preferencesKey = intPreferencesKey(PREFERENCES_REPEAT_KEY)

        runBlocking {
            context.dataStore.edit { preferences ->
                preferences[preferencesKey] = value
            }
        }
    }
}