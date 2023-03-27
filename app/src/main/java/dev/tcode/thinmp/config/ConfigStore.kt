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
private const val PREFERENCES_SHUFFLE_KEY = "shuffle"

enum class RepeatState {
    OFF, ONE, ALL
}

enum class ShuffleState {
    OFF, ON
}

enum class MainMenuVisibilityState {
    INVISIBLE, VISIBLE
}

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(PREFERENCES_NAME)

class ConfigStore(private val context: Context) {
    fun getRepeat(): RepeatState {
        val values = RepeatState.values()
        val value = getInt(PREFERENCES_REPEAT_KEY)

        return if (value != null) {
            values[value]
        } else {
            RepeatState.OFF
        }
    }

    fun saveRepeat(value: RepeatState) {
        saveInt(PREFERENCES_REPEAT_KEY, value.ordinal)
    }

    fun getShuffle(): ShuffleState {
        val values = ShuffleState.values()
        val value = getInt(PREFERENCES_SHUFFLE_KEY)

        return if (value != null) {
            values[value]
        } else {
            ShuffleState.OFF
        }
    }

    fun saveShuffle(value: ShuffleState) {
        saveInt(PREFERENCES_SHUFFLE_KEY, value.ordinal)
    }

    fun saveMainMenuVisibility(key: String, value: MainMenuVisibilityState) {
        saveInt(key, value.ordinal)
    }

    fun getMainMenuVisibility(key: String): Boolean {
        val values = MainMenuVisibilityState.values()
        val value = getInt(key)

        return if (value != null) {
            values[value] == MainMenuVisibilityState.VISIBLE
        } else {
            true
        }
    }

    private fun getInt(key: String): Int? {
        val preferences = runBlocking { context.dataStore.data.first() }
        val preferencesKey = intPreferencesKey(key)

        return preferences[preferencesKey]
    }

    private fun saveInt(key: String, value: Int) {
        val preferencesKey = intPreferencesKey(key)

        runBlocking {
            context.dataStore.edit { preferences ->
                preferences[preferencesKey] = value
            }
        }
    }
}