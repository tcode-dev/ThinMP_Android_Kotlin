package dev.tcode.thinmp.config

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private const val PREFERENCES_NAME = "thinmp_preferences"
private const val PREFERENCES_REPEAT_KEY = "repeat"
private const val PREFERENCES_SHUFFLE_KEY = "shuffle"
private const val PREFERENCES_SHORTCUT_KEY = "shortcut"
private const val PREFERENCES_RECENTLY_ALBUMS_KEY = "recentlyAlbums"

enum class RepeatState {
    OFF, ONE, ALL
}

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(PREFERENCES_NAME)

/**
 * Every accessor is suspend. They used to wrap DataStore in runBlocking, which blocked whichever
 * thread called them - in practice the main thread, from MusicService.onCreate() and from the
 * repeat/shuffle buttons, where a save means a blocking fsync.
 *
 * Do not add withContext(Dispatchers.IO) around these: DataStore already does its file access on
 * its own dispatcher, for the same reason suspend Room calls are not wrapped either.
 */
class ConfigStore(private val context: Context) {
    suspend fun getRepeat(): RepeatState {
        val value = getInt(PREFERENCES_REPEAT_KEY) ?: return RepeatState.OFF

        // Stored as an ordinal, so a value left behind by a build with more states than this one
        // must not index out of bounds.
        return RepeatState.entries.getOrElse(value) { RepeatState.OFF }
    }

    suspend fun saveRepeat(value: RepeatState) {
        saveInt(PREFERENCES_REPEAT_KEY, value.ordinal)
    }

    suspend fun getShuffle(): Boolean {
        return getBoolean(PREFERENCES_SHUFFLE_KEY) ?: false
    }

    suspend fun saveShuffle(value: Boolean) {
        saveBoolean(PREFERENCES_SHUFFLE_KEY, value)
    }

    suspend fun getMainMenuVisibility(key: String): Boolean {
        return getBoolean(key) ?: true
    }

    suspend fun saveMainMenuVisibility(key: String, value: Boolean) {
        saveBoolean(key, value)
    }

    suspend fun getShortcutVisibility(): Boolean {
        return getBoolean(PREFERENCES_SHORTCUT_KEY) ?: true
    }

    suspend fun saveShortcutVisibility(value: Boolean) {
        saveBoolean(PREFERENCES_SHORTCUT_KEY, value)
    }

    suspend fun getRecentlyAlbumsVisibility(): Boolean {
        return getBoolean(PREFERENCES_RECENTLY_ALBUMS_KEY) ?: true
    }

    suspend fun saveRecentlyAlbumsVisibility(value: Boolean) {
        saveBoolean(PREFERENCES_RECENTLY_ALBUMS_KEY, value)
    }

    private suspend fun getInt(key: String): Int? {
        val preferences = context.dataStore.data.first()
        val preferencesKey = intPreferencesKey(key)

        return preferences[preferencesKey]
    }

    private suspend fun saveInt(key: String, value: Int) {
        val preferencesKey = intPreferencesKey(key)

        context.dataStore.edit { preferences ->
            preferences[preferencesKey] = value
        }
    }

    private suspend fun getBoolean(key: String): Boolean? {
        val preferences = context.dataStore.data.first()
        val preferencesKey = booleanPreferencesKey(key)

        return preferences[preferencesKey]
    }

    private suspend fun saveBoolean(key: String, value: Boolean) {
        val preferencesKey = booleanPreferencesKey(key)

        context.dataStore.edit { preferences ->
            preferences[preferencesKey] = value
        }
    }
}
